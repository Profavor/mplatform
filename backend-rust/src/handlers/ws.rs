use crate::models::chat::SendMessageRequest;
use crate::state::AppState;
use axum::{
    extract::{
        ws::{Message, WebSocket, WebSocketUpgrade},
        Query, State,
    },
    response::IntoResponse,
};
use futures_util::{SinkExt, StreamExt};
use serde::Deserialize;
use std::collections::HashMap;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use tokio::sync::{mpsc, Mutex};
use uuid::Uuid;

#[derive(Debug, Deserialize)]
pub struct WsQuery {
    pub token: Option<String>,
    pub user_id: Option<String>,
}

#[derive(Debug)]
struct StompFrame {
    command: String,
    headers: HashMap<String, String>,
    body: String,
}

fn parse_stomp_frames(text: &str) -> Vec<StompFrame> {
    let mut frames = Vec::new();
    for raw in text.split('\0') {
        let trimmed = raw.trim_matches(|c| c == '\r' || c == '\n');
        if trimmed.is_empty() {
            continue;
        }
        let mut lines = trimmed.lines();
        let command = match lines.next() {
            Some(cmd) if !cmd.trim().is_empty() => cmd.trim().to_uppercase(),
            _ => continue,
        };
        let mut headers = HashMap::new();
        let mut body = String::new();
        let mut in_body = false;

        for line in lines {
            if in_body {
                if !body.is_empty() {
                    body.push('\n');
                }
                body.push_str(line);
            } else if line.is_empty() || line == "\r" {
                in_body = true;
            } else if let Some((k, v)) = line.split_once(':') {
                headers.insert(k.trim().to_string(), v.trim().to_string());
            }
        }
        frames.push(StompFrame {
            command,
            headers,
            body,
        });
    }
    frames
}

pub async fn ws_handler(
    ws: WebSocketUpgrade,
    State(state): State<AppState>,
    Query(query): Query<WsQuery>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, state, query))
}

async fn handle_socket(socket: WebSocket, state: AppState, query: WsQuery) {
    let (mut ws_sender, mut ws_receiver) = socket.split();
    let (tx_out, mut rx_out) = mpsc::channel::<Message>(128);

    let is_stomp = Arc::new(AtomicBool::new(false));
    let subscriptions = Arc::new(Mutex::new(Vec::<(String, String)>::new())); // (sub_id, destination)
    let user_id = query.user_id.unwrap_or_else(|| "anonymous".to_string());

    // Task 1: Dedicated writer to WebSocket sink
    let mut writer_task = tokio::spawn(async move {
        while let Some(msg) = rx_out.recv().await {
            if ws_sender.send(msg).await.is_err() {
                break;
            }
        }
    });

    // Task 2: Broadcast listener forwarding events to this socket
    let is_stomp_broadcast = is_stomp.clone();
    let subs_broadcast = subscriptions.clone();
    let tx_out_broadcast = tx_out.clone();
    let mut rx_broadcast = state.broadcast_tx.subscribe();

    let mut broadcast_task = tokio::spawn(async move {
        while let Ok(msg) = rx_broadcast.recv().await {
            if is_stomp_broadcast.load(Ordering::Relaxed) {
                // STOMP Client: inspect event and route to matching subscriptions
                let subs = subs_broadcast.lock().await.clone();
                if let Ok(val) = serde_json::from_str::<serde_json::Value>(&msg) {
                    let has_room_id = val.get("roomId").is_some();
                    let event_type = val.get("eventType").and_then(|e| e.as_str()).unwrap_or("");
                    let target_user = val
                        .get("userId")
                        .or_else(|| val.get("targetUserId"))
                        .and_then(|u| u.as_str());

                    for (sub_id, dest) in &subs {
                        let matches = if dest == "/topic/chat/messages" {
                            has_room_id
                                || event_type == "CHAT_MESSAGE"
                                || event_type == "MESSAGE_DELETED"
                                || event_type == "ROOM_READ"
                                || event_type == "MUSIC_PLAY"
                                || event_type == "MUSIC_STOP"
                        } else if dest == "/topic/chat/presence" {
                            event_type == "PRESENCE_UPDATE"
                        } else if dest.starts_with("/topic/notifications/") {
                            if let Some(uid) = target_user {
                                dest.ends_with(uid)
                            } else {
                                event_type.contains("NOTIFICATION")
                                    || event_type == "INBOX_MESSAGE"
                                    || event_type == "NEW_MESSAGE"
                                    || event_type == "APPROVAL"
                            }
                        } else {
                            false
                        };

                        if matches {
                            let msg_id = Uuid::new_v4().to_string();
                            let stomp_frame = format!(
                                "MESSAGE\ndestination:{}\nsubscription:{}\nmessage-id:{}\ncontent-type:application/json;charset=UTF-8\n\n{}\0",
                                dest, sub_id, msg_id, msg
                            );
                            if tx_out_broadcast
                                .send(Message::Text(stomp_frame))
                                .await
                                .is_err()
                            {
                                return;
                            }
                        }
                    }
                }
            } else {
                // Raw WebSocket client (JSON)
                if tx_out_broadcast.send(Message::Text(msg)).await.is_err() {
                    return;
                }
            }
        }
    });

    // Task 3: Reader task handling incoming frames from client
    let state_clone = state.clone();
    let user_id_clone = user_id.clone();
    let is_stomp_reader = is_stomp.clone();
    let subs_reader = subscriptions.clone();
    let tx_out_reader = tx_out.clone();

    let mut reader_task = tokio::spawn(async move {
        while let Some(Ok(msg)) = ws_receiver.next().await {
            match msg {
                Message::Text(text) => {
                    // Check if heartbeat
                    if text == "\n" || text == "\r\n" {
                        let _ = tx_out_reader.send(Message::Text("\n".to_string())).await;
                        continue;
                    }

                    // Check if STOMP frame
                    let trimmed = text.trim_start();
                    if trimmed.starts_with("CONNECT")
                        || trimmed.starts_with("STOMP")
                        || trimmed.starts_with("SUBSCRIBE")
                        || trimmed.starts_with("UNSUBSCRIBE")
                        || trimmed.starts_with("SEND")
                        || trimmed.starts_with("DISCONNECT")
                    {
                        is_stomp_reader.store(true, Ordering::Relaxed);
                        let frames = parse_stomp_frames(&text);
                        for frame in frames {
                            match frame.command.as_str() {
                                "CONNECT" | "STOMP" => {
                                    let connected_frame =
                                        "CONNECTED\nversion:1.2\nheart-beat:10000,10000\n\n\0"
                                            .to_string();
                                    let _ = tx_out_reader.send(Message::Text(connected_frame)).await;
                                }
                                "SUBSCRIBE" => {
                                    if let (Some(id), Some(dest)) =
                                        (frame.headers.get("id"), frame.headers.get("destination"))
                                    {
                                        let mut subs = subs_reader.lock().await;
                                        subs.retain(|(sub_id, _)| sub_id != id);
                                        subs.push((id.clone(), dest.clone()));
                                        tracing::debug!("STOMP subscribed: id={}, dest={}", id, dest);
                                    }
                                }
                                "UNSUBSCRIBE" => {
                                    if let Some(id) = frame.headers.get("id") {
                                        let mut subs = subs_reader.lock().await;
                                        subs.retain(|(sub_id, _)| sub_id != id);
                                        tracing::debug!("STOMP unsubscribed: id={}", id);
                                    }
                                }
                                "SEND" => {
                                    if let Ok(val) = serde_json::from_str::<serde_json::Value>(&frame.body) {
                                        if let Some(room_id_str) =
                                            val.get("roomId").and_then(|r| r.as_str())
                                        {
                                            if let Ok(room_id) = Uuid::parse_str(room_id_str) {
                                                let content = val
                                                    .get("content")
                                                    .and_then(|c| c.as_str())
                                                    .map(|s| s.to_string());
                                                let message_type = val
                                                    .get("messageType")
                                                    .and_then(|m| m.as_str())
                                                    .map(|s| s.to_string());
                                                let _ = state_clone
                                                    .chat_service
                                                    .send_message(
                                                        room_id,
                                                        &user_id_clone,
                                                        Some(&user_id_clone),
                                                        SendMessageRequest {
                                                            room_id: Some(room_id),
                                                            message_type,
                                                            content,
                                                            file_url: None,
                                                            file_name: None,
                                                            file_size: None,
                                                        },
                                                    )
                                                    .await;
                                            }
                                        }
                                    }
                                }
                                "DISCONNECT" => {
                                    if let Some(receipt) = frame.headers.get("receipt") {
                                        let receipt_frame =
                                            format!("RECEIPT\nreceipt-id:{}\n\n\0", receipt);
                                        let _ = tx_out_reader.send(Message::Text(receipt_frame)).await;
                                    }
                                    return;
                                }
                                _ => {}
                            }
                        }
                        continue;
                    }

                    // Raw JSON client handler
                    if let Ok(val) = serde_json::from_str::<serde_json::Value>(&text) {
                        if let Some(action) = val.get("action").and_then(|a| a.as_str()) {
                            if action == "send" {
                                if let Some(room_id_str) =
                                    val.get("roomId").and_then(|r| r.as_str())
                                {
                                    if let Ok(room_id) = Uuid::parse_str(room_id_str) {
                                        let content = val
                                            .get("content")
                                            .and_then(|c| c.as_str())
                                            .map(|s| s.to_string());
                                        let message_type = val
                                            .get("messageType")
                                            .and_then(|m| m.as_str())
                                            .map(|s| s.to_string());
                                        let _ = state_clone
                                            .chat_service
                                            .send_message(
                                                room_id,
                                                &user_id_clone,
                                                Some(&user_id_clone),
                                                SendMessageRequest {
                                                    room_id: Some(room_id),
                                                    message_type,
                                                    content,
                                                    file_url: None,
                                                    file_name: None,
                                                    file_size: None,
                                                },
                                            )
                                            .await;
                                    }
                                }
                            }
                        }
                    }
                }
                Message::Ping(_) => {
                    // Axum automatically answers Pong
                }
                Message::Close(_) => {
                    break;
                }
                _ => {}
            }
        }
    });

    // Terminate tasks when any task exits
    tokio::select! {
        _ = (&mut writer_task) => {
            broadcast_task.abort();
            reader_task.abort();
        },
        _ = (&mut broadcast_task) => {
            writer_task.abort();
            reader_task.abort();
        },
        _ = (&mut reader_task) => {
            writer_task.abort();
            broadcast_task.abort();
        }
    };
}
