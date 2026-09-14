use crate::models::chat::*;
use chrono::Utc;
use sqlx::{PgPool, Row};
use uuid::Uuid;

pub struct ChatRepository;

impl ChatRepository {
    pub async fn get_user_rooms(pool: &PgPool, user_id: &str) -> Result<Vec<ChatMessageRoom>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            SELECT r.id, r.name, r.is_group, r.created_by, r.created_at,
                   r.last_message, r.last_message_at, r.version,
                   (
                       SELECT count(*)
                       FROM chat_message msg
                       WHERE msg.room_id = r.id
                         AND msg.sender_id != $1
                         AND (m.last_read_at IS NULL OR msg.created_at > m.last_read_at)
                   ) AS unread_count
            FROM chat_message_room r
            JOIN chat_message_room_member m ON r.id = m.room_id
            WHERE m.user_id = $1
            ORDER BY COALESCE(r.last_message_at, r.created_at) DESC NULLS LAST
            "#
        )
        .bind(user_id)
        .fetch_all(pool)
        .await?;

        let rooms = rows.into_iter().map(|r| ChatMessageRoom {
            id: r.get("id"),
            name: r.get("name"),
            is_group: r.get("is_group"),
            created_by: r.get("created_by"),
            created_at: r.get("created_at"),
            last_message: r.get("last_message"),
            last_message_at: r.get("last_message_at"),
            version: r.get("version"),
            unread_count: Some(r.get::<i64, _>("unread_count")),
        }).collect();

        Ok(rooms)
    }

    pub async fn create_room(
        pool: &PgPool,
        name: &str,
        is_group: bool,
        creator_id: &str,
        member_ids: &[String],
    ) -> Result<ChatMessageRoom, sqlx::Error> {
        let room_id = Uuid::new_v4();
        let now = Utc::now();

        sqlx::query(
            r#"
            INSERT INTO chat_message_room (id, name, is_group, created_by, created_at, version)
            VALUES ($1, $2, $3, $4, $5, 1)
            "#
        )
        .bind(room_id)
        .bind(name)
        .bind(is_group)
        .bind(creator_id)
        .bind(now)
        .execute(pool)
        .await?;

        // Add creator and members with canonical ID resolution
        let mut all_members = vec![creator_id.to_string()];
        for m in member_ids {
            if !all_members.contains(m) {
                all_members.push(m.clone());
            }
        }

        let mut resolved_members: Vec<String> = Vec::new();
        for uid in all_members {
            let canon_id: Option<String> = sqlx::query_scalar(
                "SELECT id FROM users WHERE id = $1 OR username = $1"
            )
            .bind(&uid)
            .fetch_optional(pool)
            .await
            .unwrap_or(None);

            let final_uid = canon_id.unwrap_or(uid);
            if !resolved_members.contains(&final_uid) {
                resolved_members.push(final_uid);
            }
        }

        for uid in resolved_members {
            let member_id = Uuid::new_v4();
            let _ = sqlx::query(
                r#"
                INSERT INTO chat_message_room_member (id, room_id, user_id, joined_at, last_read_at)
                VALUES ($1, $2, $3, $4, $4)
                "#
            )
            .bind(member_id)
            .bind(room_id)
            .bind(uid)
            .bind(now)
            .execute(pool)
            .await;
        }

        Ok(ChatMessageRoom {
            id: room_id,
            name: name.to_string(),
            is_group,
            created_by: Some(creator_id.to_string()),
            created_at: Some(now.naive_utc()),
            last_message: None,
            last_message_at: None,
            version: 1,
            unread_count: Some(0),
        })
    }

    pub async fn get_room_members(pool: &PgPool, room_id: Uuid) -> Result<Vec<RoomMemberDto>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            SELECT user_id, username, joined_at, last_read_at
            FROM (
                SELECT DISTINCT ON (COALESCE(u.id, m.user_id))
                       m.user_id,
                       COALESCE(u.username, m.user_id) AS username,
                       m.joined_at,
                       m.last_read_at
                FROM chat_message_room_member m
                LEFT JOIN users u ON m.user_id = u.id OR m.user_id = u.username
                WHERE m.room_id = $1
                ORDER BY COALESCE(u.id, m.user_id), m.joined_at ASC
            ) deduped
            ORDER BY joined_at ASC
            "#
        )
        .bind(room_id)
        .fetch_all(pool)
        .await?;

        let members = rows.into_iter().map(|r| RoomMemberDto {
            user_id: r.get("user_id"),
            username: r.get("username"),
            joined_at: r.get("joined_at"),
            last_read_at: r.get("last_read_at"),
        }).collect();

        Ok(members)
    }

    pub async fn get_room_messages(pool: &PgPool, room_id: Uuid, user_id: &str) -> Result<Vec<ChatMessage>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            SELECT msg.id, msg.room_id, msg.sender_id, msg.sender_name, msg.message_type,
                   msg.content, msg.file_url, msg.file_name, msg.file_size, msg.created_at,
                   (
                       SELECT count(*)
                       FROM chat_message_room_member mem
                       WHERE mem.room_id = msg.room_id
                         AND mem.user_id != msg.sender_id
                         AND (mem.last_read_at IS NULL OR mem.last_read_at < msg.created_at - INTERVAL '1 second')
                   ) AS unread_count
            FROM chat_message msg
            WHERE msg.room_id = $1
            ORDER BY msg.created_at ASC
            "#
        )
        .bind(room_id)
        .fetch_all(pool)
        .await?;

        let messages = rows.into_iter().map(|r| ChatMessage {
            id: r.get("id"),
            room_id: r.get("room_id"),
            sender_id: r.get("sender_id"),
            sender_name: r.get("sender_name"),
            message_type: r.get("message_type"),
            content: r.get("content"),
            file_url: r.get("file_url"),
            file_name: r.get("file_name"),
            file_size: r.get("file_size"),
            created_at: r.get("created_at"),
            unread_count: Some(r.get::<i64, _>("unread_count")),
        }).collect();

        Ok(messages)
    }

    pub async fn mark_room_as_read(pool: &PgPool, room_id: Uuid, user_id: &str) -> Result<(), sqlx::Error> {
        sqlx::query(
            r#"
            UPDATE chat_message_room_member
            SET last_read_at = NOW()
            WHERE room_id = $1 AND (user_id = $2 OR user_id IN (SELECT id FROM users WHERE username = $2))
            "#
        )
        .bind(room_id)
        .bind(user_id)
        .execute(pool)
        .await?;

        Ok(())
    }

    pub async fn leave_room(pool: &PgPool, room_id: Uuid, user_id: &str) -> Result<(), sqlx::Error> {
        sqlx::query(
            "DELETE FROM chat_message_room_member WHERE room_id = $1 AND (user_id = $2 OR user_id IN (SELECT id FROM users WHERE username = $2))"
        )
        .bind(room_id)
        .bind(user_id)
        .execute(pool)
        .await?;

        Ok(())
    }

    pub async fn delete_room(pool: &PgPool, room_id: Uuid) -> Result<(), sqlx::Error> {
        sqlx::query("DELETE FROM chat_message WHERE room_id = $1").bind(room_id).execute(pool).await?;
        sqlx::query("DELETE FROM chat_message_room_member WHERE room_id = $1").bind(room_id).execute(pool).await?;
        sqlx::query("DELETE FROM chat_message_room WHERE id = $1").bind(room_id).execute(pool).await?;
        Ok(())
    }

    pub async fn get_total_unread_count(pool: &PgPool, user_id: &str) -> Result<i64, sqlx::Error> {
        let count: i64 = sqlx::query_scalar(
            r#"
            SELECT count(*)
            FROM chat_message msg
            JOIN chat_message_room_member m ON msg.room_id = m.room_id
            WHERE (m.user_id = $1 OR m.user_id IN (SELECT id FROM users WHERE username = $1))
              AND msg.sender_id != $1
              AND (m.last_read_at IS NULL OR msg.created_at > m.last_read_at)
            "#
        )
        .bind(user_id)
        .fetch_one(pool)
        .await
        .unwrap_or(0);

        Ok(count)
    }

    pub async fn save_message(
        pool: &PgPool,
        room_id: Uuid,
        sender_id: &str,
        sender_name: Option<&str>,
        message_type: &str,
        content: Option<&str>,
        file_url: Option<&str>,
        file_name: Option<&str>,
        file_size: Option<i64>,
    ) -> Result<ChatMessage, sqlx::Error> {
        let msg_id = Uuid::new_v4();
        let now = Utc::now();

        sqlx::query(
            r#"
            INSERT INTO chat_message (
                id, room_id, sender_id, sender_name, message_type, content,
                file_url, file_name, file_size, created_at, version
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 1)
            "#
        )
        .bind(msg_id)
        .bind(room_id)
        .bind(sender_id)
        .bind(sender_name)
        .bind(message_type)
        .bind(content)
        .bind(file_url)
        .bind(file_name)
        .bind(file_size)
        .bind(now)
        .execute(pool)
        .await?;

        let last_summary = match message_type {
            "IMAGE" => "[이미지]".to_string(),
            "FILE" => format!("[파일] {}", file_name.unwrap_or("")),
            _ => content.unwrap_or("").to_string(),
        };

        let display_summary = if let Some(sn) = sender_name {
            format!("{}: {}", sn, last_summary)
        } else {
            last_summary
        };

        sqlx::query(
            r#"
            UPDATE chat_message_room
            SET last_message = $1, last_message_at = $2, version = version + 1
            WHERE id = $3
            "#
        )
        .bind(display_summary)
        .bind(now)
        .bind(room_id)
        .execute(pool)
        .await?;

        Ok(ChatMessage {
            id: msg_id,
            room_id,
            sender_id: sender_id.to_string(),
            sender_name: sender_name.map(|s| s.to_string()),
            message_type: message_type.to_string(),
            content: content.map(|s| s.to_string()),
            file_url: file_url.map(|s| s.to_string()),
            file_name: file_name.map(|s| s.to_string()),
            file_size,
            created_at: Some(now.naive_utc()),
            unread_count: Some(0),
        })
    }

    pub async fn delete_message(pool: &PgPool, message_id: Uuid) -> Result<Option<Uuid>, sqlx::Error> {
        let room_id: Option<Uuid> = sqlx::query_scalar("DELETE FROM chat_message WHERE id = $1 RETURNING room_id")
            .bind(message_id)
            .fetch_optional(pool)
            .await?;

        Ok(room_id)
    }
}
