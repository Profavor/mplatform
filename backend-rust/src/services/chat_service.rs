use crate::error::{AppError, AppResult};
use crate::models::chat::*;
use crate::repositories::chat_repo::ChatRepository;
use sqlx::PgPool;
use std::sync::Arc;
use tokio::sync::broadcast;
use uuid::Uuid;

#[derive(Clone)]
pub struct ChatService {
    pool: PgPool,
    broadcast_tx: broadcast::Sender<String>,
}

impl ChatService {
    pub fn new(pool: PgPool, broadcast_tx: broadcast::Sender<String>) -> Self {
        Self { pool, broadcast_tx }
    }

    pub async fn get_user_rooms(&self, user_id: &str) -> AppResult<Vec<ChatMessageRoom>> {
        let rooms = ChatRepository::get_user_rooms(&self.pool, user_id).await?;
        Ok(rooms)
    }

    pub async fn create_room(
        &self,
        name: &str,
        is_group: bool,
        creator_id: &str,
        member_ids: &[String],
    ) -> AppResult<ChatMessageRoom> {
        let room = ChatRepository::create_room(&self.pool, name, is_group, creator_id, member_ids).await?;
        Ok(room)
    }

    pub async fn get_room_members(&self, room_id: Uuid) -> AppResult<Vec<RoomMemberDto>> {
        let members = ChatRepository::get_room_members(&self.pool, room_id).await?;
        Ok(members)
    }

    pub async fn get_room_messages(&self, room_id: Uuid, user_id: &str) -> AppResult<Vec<ChatMessage>> {
        let messages = ChatRepository::get_room_messages(&self.pool, room_id, user_id).await?;
        Ok(messages)
    }

    pub async fn mark_room_as_read(&self, room_id: Uuid, user_id: &str) -> AppResult<()> {
        ChatRepository::mark_room_as_read(&self.pool, room_id, user_id).await?;
        Ok(())
    }

    pub async fn leave_room(&self, room_id: Uuid, user_id: &str) -> AppResult<()> {
        ChatRepository::leave_room(&self.pool, room_id, user_id).await?;
        Ok(())
    }

    pub async fn delete_room(&self, room_id: Uuid) -> AppResult<()> {
        ChatRepository::delete_room(&self.pool, room_id).await?;
        Ok(())
    }

    pub async fn get_total_unread_count(&self, user_id: &str) -> AppResult<i64> {
        let count = ChatRepository::get_total_unread_count(&self.pool, user_id).await?;
        Ok(count)
    }

    pub async fn send_message(
        &self,
        room_id: Uuid,
        sender_id: &str,
        sender_name: Option<&str>,
        req: SendMessageRequest,
    ) -> AppResult<ChatMessage> {
        let msg_type = req.message_type.unwrap_or_else(|| "TEXT".to_string());
        let msg = ChatRepository::save_message(
            &self.pool,
            room_id,
            sender_id,
            sender_name,
            &msg_type,
            req.content.as_deref(),
            req.file_url.as_deref(),
            req.file_name.as_deref(),
            req.file_size,
        )
        .await?;

        // Broadcast to realtime WebSocket channel
        let event = serde_json::json!({
            "eventType": "CHAT_MESSAGE",
            "roomId": room_id,
            "message": &msg
        });

        if let Ok(serialized) = serde_json::to_string(&event) {
            let _ = self.broadcast_tx.send(serialized);
        }

        Ok(msg)
    }

    pub async fn delete_message(&self, message_id: Uuid) -> AppResult<()> {
        if let Some(room_id) = ChatRepository::delete_message(&self.pool, message_id).await? {
            let event = serde_json::json!({
                "eventType": "MESSAGE_DELETED",
                "roomId": room_id,
                "messageId": message_id
            });
            if let Ok(serialized) = serde_json::to_string(&event) {
                let _ = self.broadcast_tx.send(serialized);
            }
        }
        Ok(())
    }
}
