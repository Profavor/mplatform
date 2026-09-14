use crate::models::chat::SendMessageRequest;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{
        ws::{Message, WebSocket, WebSocketUpgrade},
        Query, State,
    },
    response::IntoResponse,
};
use futures_util::{SinkExt, StreamExt};
use serde::Deserialize;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
pub struct WsQuery {
    pub token: Option<String>,
    pub user_id: Option<String>,
}

pub async fn ws_handler(
    ws: WebSocketUpgrade,
    State(state): State<AppState>,
    Query(query): Query<WsQuery>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, state, query))
}

async fn handle_socket(socket: WebSocket, state: AppState, query: WsQuery) {
    let (mut sender, mut receiver) = socket.split();
    let mut rx = state.broadcast_tx.subscribe();

    let user_id = query.user_id.unwrap_or_else(|| "anonymous".to_string());

    // Task to forward broadcast channel events to client
    let mut send_task = tokio::spawn(async move {
        while let Ok(msg) = rx.recv().await {
            if sender.send(Message::Text(msg)).await.is_err() {
                break;
            }
        }
    });

    // Task to receive messages from client
    let state_clone = state.clone();
    let user_id_clone = user_id.clone();
    let mut recv_task = tokio::spawn(async move {
        while let Some(Ok(msg)) = receiver.next().await {
            match msg {
                Message::Text(text) => {
                    // Support JSON commands like { "action": "send", "roomId": "...", "content": "..." }
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
                    // Handled automatically by axum ws
                }
                Message::Close(_) => {
                    break;
                }
                _ => {}
            }
        }
    });

    // If either task finishes, abort the other
    tokio::select! {
        _ = (&mut send_task) => recv_task.abort(),
        _ = (&mut recv_task) => send_task.abort(),
    };
}
