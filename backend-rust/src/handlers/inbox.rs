use axum::extract::{Path, Query, State};
use axum::http::StatusCode;
use axum::response::IntoResponse;
use axum::Json;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::{PgPool, Row};
use uuid::Uuid;

use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::record::PageResponse;
use crate::repositories::user_repo::UserRepository;
use crate::state::AppState;

const TRANSPARENT_1X1_GIF: &[u8] = &[
    0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 1, 0, 1, 0, 0x80, 0, 0, 0, 0, 0, 0xff, 0xff, 0xff,
    0x21, 0xf9, 4, 1, 0, 0, 0, 0, 0x2c, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 2, 0x44, 1, 0, 0x3b,
];

#[derive(Debug, Deserialize)]
pub struct InboxQuery {
    pub folder: Option<String>,
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub keyword: Option<String>,
}

#[derive(Debug, Deserialize)]
pub struct DeleteInboxQuery {
    pub permanent: Option<bool>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct InboxMessageRequest {
    pub subject: Option<String>,
    pub body: Option<String>,
    pub importance: Option<String>,
    pub message_type: Option<String>,
    pub parent_message_id: Option<Uuid>,
    pub related_approval_id: Option<Uuid>,
    pub is_draft: Option<bool>,
    pub to_recipients: Option<Vec<String>>,
    pub cc_recipients: Option<Vec<String>>,
    pub bcc_recipients: Option<Vec<String>>,
    pub attachment_ids: Option<Vec<Uuid>>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct RecipientInfo {
    pub user_id: Option<String>,
    pub name: Option<String>,
    pub email: Option<String>,
    pub recipient_type: String,
    pub is_read: bool,
    pub read_at: Option<NaiveDateTime>,
    pub is_recalled: bool,
    pub recalled_at: Option<NaiveDateTime>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct AttachmentInfo {
    pub id: Uuid,
    pub file_name: String,
    pub file_size: i64,
    pub content_type: Option<String>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct InboxMessageResponse {
    pub id: Uuid,
    pub recipient_id: Option<Uuid>,
    pub sender_id: Option<String>,
    pub sender_name: Option<String>,
    pub sender_email: Option<String>,
    pub subject: String,
    pub body: Option<String>,
    pub importance: String,
    pub message_type: String,
    pub parent_message_id: Option<Uuid>,
    pub root_message_id: Option<Uuid>,
    pub related_approval_id: Option<Uuid>,
    pub is_draft: bool,
    pub is_read: bool,
    pub is_starred: bool,
    pub folder: String,
    pub has_attachments: bool,
    pub attachment_count: usize,
    pub recipient_count: usize,
    pub thread_count: usize,
    pub to_recipients: Vec<RecipientInfo>,
    pub cc_recipients: Vec<RecipientInfo>,
    pub attachments: Vec<AttachmentInfo>,
    pub sent_at: Option<NaiveDateTime>,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ToggleReadRequest {
    pub is_read: Option<bool>,
}

#[derive(Debug, Deserialize)]
pub struct MoveFolderRequest {
    pub folder: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BulkActionRequest {
    pub action: String,
    pub message_ids: Vec<Uuid>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecallResultResponse {
    pub message_id: Uuid,
    pub total_recipients: usize,
    pub recalled_before_read_count: usize,
    pub recalled_after_read_count: usize,
    pub external_count: usize,
    pub details: Vec<RecipientRecallDetail>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecipientRecallDetail {
    pub user_id: Option<String>,
    pub name: Option<String>,
    pub email: Option<String>,
    pub recipient_type: String,
    pub was_read: bool,
    pub read_at: Option<NaiveDateTime>,
    pub is_recalled: bool,
    pub status: String,
}

pub async fn resolve_user_identities(pool: &PgPool, auth: &AuthUser) -> (String, String, String) {
    if let Ok(Some(u)) = UserRepository::find_by_username(pool, &auth.claims.sub).await {
        let uid = u.id;
        let uname = u.username.unwrap_or_else(|| auth.username.clone());
        let uemail = u.email.unwrap_or_default();
        return (uid, uname, uemail);
    }
    if let Ok(Some(u)) = UserRepository::find_by_id(pool, &auth.user_id).await {
        let uid = u.id;
        let uname = u.username.unwrap_or_else(|| auth.username.clone());
        let uemail = u.email.unwrap_or_default();
        return (uid, uname, uemail);
    }
    (auth.user_id.clone(), auth.username.clone(), String::new())
}

pub async fn get_inbox_unread_count(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;

    let count: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(*)
        FROM inbox_recipient r
        WHERE (r.user_id = $1 OR r.user_id = $2 OR (r.email IS NOT NULL AND r.email != '' AND r.email = $3))
          AND r.folder = 'INBOX'
          AND r.is_read = false
          AND r.is_deleted = false
        "#
    )
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .fetch_one(&state.db)
    .await
    .unwrap_or((0,));

    Ok(Json(serde_json::json!({
        "unreadCount": count.0
    })))
}

pub async fn get_inbox_folder_counts(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;

    let row = sqlx::query(
        r#"
        SELECT 
          COUNT(*) FILTER (WHERE folder = 'INBOX' AND is_deleted = false) as inbox_total,
          COUNT(*) FILTER (WHERE folder = 'INBOX' AND is_read = false AND is_deleted = false) as inbox_unread,
          COUNT(*) FILTER (WHERE folder = 'SENT' AND is_deleted = false) as sent_total,
          COUNT(*) FILTER (WHERE folder = 'SENT' AND is_read = false AND is_deleted = false) as sent_unread,
          COUNT(*) FILTER (WHERE (folder = 'DRAFT' OR folder = 'DRAFTS') AND is_deleted = false) as draft_total,
          COUNT(*) FILTER (WHERE (folder = 'DRAFT' OR folder = 'DRAFTS') AND is_read = false AND is_deleted = false) as draft_unread,
          COUNT(*) FILTER (WHERE is_starred = true AND is_deleted = false) as starred_total,
          COUNT(*) FILTER (WHERE is_starred = true AND is_read = false AND is_deleted = false) as starred_unread,
          COUNT(*) FILTER (WHERE folder = 'ARCHIVE' AND is_deleted = false) as archive_total,
          COUNT(*) FILTER (WHERE folder = 'ARCHIVE' AND is_read = false AND is_deleted = false) as archive_unread,
          COUNT(*) FILTER (WHERE (folder = 'TRASH' OR is_deleted = true)) as trash_total,
          COUNT(*) FILTER (WHERE (folder = 'TRASH' OR is_deleted = true) AND is_read = false) as trash_unread
        FROM inbox_recipient r
        WHERE (r.user_id = $1 OR r.user_id = $2 OR (r.email IS NOT NULL AND r.email != '' AND r.email = $3))
        "#
    )
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .fetch_one(&state.db)
    .await;

    let (inbox_t, inbox_u, sent_t, sent_u, draft_t, draft_u, star_t, star_u, arch_t, arch_u, trash_t, trash_u): (i64, i64, i64, i64, i64, i64, i64, i64, i64, i64, i64, i64) = match row {
        Ok(r) => (
            r.get::<Option<i64>, _>("inbox_total").unwrap_or(0),
            r.get::<Option<i64>, _>("inbox_unread").unwrap_or(0),
            r.get::<Option<i64>, _>("sent_total").unwrap_or(0),
            r.get::<Option<i64>, _>("sent_unread").unwrap_or(0),
            r.get::<Option<i64>, _>("draft_total").unwrap_or(0),
            r.get::<Option<i64>, _>("draft_unread").unwrap_or(0),
            r.get::<Option<i64>, _>("starred_total").unwrap_or(0),
            r.get::<Option<i64>, _>("starred_unread").unwrap_or(0),
            r.get::<Option<i64>, _>("archive_total").unwrap_or(0),
            r.get::<Option<i64>, _>("archive_unread").unwrap_or(0),
            r.get::<Option<i64>, _>("trash_total").unwrap_or(0),
            r.get::<Option<i64>, _>("trash_unread").unwrap_or(0),
        ),
        Err(_) => (0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    };

    let results = vec![
        serde_json::json!({ "folder": "INBOX", "total": inbox_t, "unread": inbox_u }),
        serde_json::json!({ "folder": "SENT", "total": sent_t, "unread": sent_u }),
        serde_json::json!({ "folder": "DRAFT", "total": draft_t, "unread": draft_u }),
        serde_json::json!({ "folder": "DRAFTS", "total": draft_t, "unread": draft_u }),
        serde_json::json!({ "folder": "STARRED", "total": star_t, "unread": star_u }),
        serde_json::json!({ "folder": "ARCHIVE", "total": arch_t, "unread": arch_u }),
        serde_json::json!({ "folder": "TRASH", "total": trash_t, "unread": trash_u }),
    ];

    Ok(Json(results))
}

pub async fn get_inbox_messages(
    State(state): State<AppState>,
    Query(query): Query<InboxQuery>,
    auth: AuthUser,
) -> Result<Json<PageResponse<InboxMessageResponse>>, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;
    let folder = query.folder.unwrap_or_else(|| "INBOX".to_string()).to_uppercase();
    let page = query.page.unwrap_or(0).max(0);
    let size = query.size.unwrap_or(20).clamp(1, 100);
    let offset = page * size;
    let keyword = query.keyword.unwrap_or_default().trim().to_lowercase();

    let total: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(*)
        FROM inbox_recipient r
        JOIN inbox_message m ON r.message_id = m.id
        WHERE (r.user_id = $1 OR r.user_id = $2 OR (r.email IS NOT NULL AND r.email != '' AND r.email = $3))
          AND (
            ($4 = 'STARRED' AND r.is_starred = true AND r.is_deleted = false)
            OR ($4 IN ('DRAFT', 'DRAFTS') AND r.folder IN ('DRAFT', 'DRAFTS') AND r.is_deleted = false)
            OR ($4 = 'TRASH' AND (r.folder = 'TRASH' OR r.is_deleted = true))
            OR ($4 NOT IN ('STARRED', 'DRAFT', 'DRAFTS', 'TRASH') AND r.folder = $4 AND r.is_deleted = false)
          )
          AND (
            $5 = ''
            OR LOWER(m.subject) LIKE CONCAT('%', $5, '%')
            OR LOWER(COALESCE(m.body, '')) LIKE CONCAT('%', $5, '%')
          )
        "#
    )
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .bind(&folder)
    .bind(&keyword)
    .fetch_one(&state.db)
    .await
    .unwrap_or((0,));

    let rows = sqlx::query(
        r#"
        SELECT m.id, m.subject, m.body, m.sender_id, m.sender_email, m.created_at, m.sent_at,
               m.importance, m.is_draft, m.message_type, m.parent_message_id, m.root_message_id, m.related_approval_id,
               r.id as recipient_id, r.is_read, r.is_starred, r.folder,
               u.username as sender_name
        FROM inbox_recipient r
        JOIN inbox_message m ON r.message_id = m.id
        LEFT JOIN users u ON (m.sender_id = u.id OR m.sender_id = u.username)
        WHERE (r.user_id = $1 OR r.user_id = $2 OR (r.email IS NOT NULL AND r.email != '' AND r.email = $3))
          AND (
            ($4 = 'STARRED' AND r.is_starred = true AND r.is_deleted = false)
            OR ($4 IN ('DRAFT', 'DRAFTS') AND r.folder IN ('DRAFT', 'DRAFTS') AND r.is_deleted = false)
            OR ($4 = 'TRASH' AND (r.folder = 'TRASH' OR r.is_deleted = true))
            OR ($4 NOT IN ('STARRED', 'DRAFT', 'DRAFTS', 'TRASH') AND r.folder = $4 AND r.is_deleted = false)
          )
          AND (
            $5 = ''
            OR LOWER(m.subject) LIKE CONCAT('%', $5, '%')
            OR LOWER(COALESCE(m.body, '')) LIKE CONCAT('%', $5, '%')
          )
        ORDER BY COALESCE(m.sent_at, m.created_at) DESC
        LIMIT $6 OFFSET $7
        "#
    )
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .bind(&folder)
    .bind(&keyword)
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    if rows.is_empty() {
        return Ok(Json(PageResponse::new(Vec::new(), total.0, page, size)));
    }

    let message_ids: Vec<Uuid> = rows.iter().map(|r| r.get("id")).collect();

    // Query all recipients for these messages
    let rec_rows = sqlx::query(
        r#"
        SELECT r.id, r.message_id, r.user_id, r.email, r.recipient_type, r.is_read, r.read_at, r.is_recalled, r.recalled_at,
               u.username as user_name
        FROM inbox_recipient r
        LEFT JOIN users u ON (r.user_id = u.id OR r.user_id = u.username)
        WHERE r.message_id = ANY($1)
        ORDER BY r.created_at ASC
        "#
    )
    .bind(&message_ids)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    // Query attachments for these messages
    let att_rows = sqlx::query(
        r#"
        SELECT id, message_id, file_name, file_size, content_type
        FROM inbox_attachment
        WHERE message_id = ANY($1)
        ORDER BY created_at ASC
        "#
    )
    .bind(&message_ids)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let mut content = Vec::new();
    for row in rows {
        let msg_id: Uuid = row.get("id");
        let recipient_id: Uuid = row.get("recipient_id");
        let subject: String = row.get("subject");
        let body: Option<String> = row.get("body");
        let sender_id: Option<String> = row.get("sender_id");
        let sender_email: Option<String> = row.get("sender_email");
        let sender_name: Option<String> = row.get("sender_name");
        let created_at: Option<NaiveDateTime> = row.get("created_at");
        let sent_at: Option<NaiveDateTime> = row.get("sent_at");
        let importance: Option<String> = row.get("importance");
        let is_draft: Option<bool> = row.get("is_draft");
        let message_type: Option<String> = row.get("message_type");
        let parent_message_id: Option<Uuid> = row.get("parent_message_id");
        let root_message_id: Option<Uuid> = row.get("root_message_id");
        let related_approval_id: Option<Uuid> = row.get("related_approval_id");
        let is_read: Option<bool> = row.get("is_read");
        let is_starred: Option<bool> = row.get("is_starred");
        let folder_val: String = row.get("folder");

        let mut to_recipients = Vec::new();
        let mut cc_recipients = Vec::new();
        let mut recipient_count = 0;

        for rr in &rec_rows {
            let mid: Uuid = rr.get("message_id");
            if mid == msg_id {
                let r_type: String = rr.get("recipient_type");
                let r_uid: Option<String> = rr.get("user_id");
                let r_email: Option<String> = rr.get("email");
                let r_uname: Option<String> = rr.get("user_name");
                let r_read: Option<bool> = rr.get("is_read");
                let r_read_at: Option<NaiveDateTime> = rr.get("read_at");
                let r_recalled: Option<bool> = rr.get("is_recalled");
                let r_recalled_at: Option<NaiveDateTime> = rr.get("recalled_at");

                let name = r_uname.or_else(|| r_email.clone()).or_else(|| r_uid.clone());
                let info = RecipientInfo {
                    user_id: r_uid,
                    name,
                    email: r_email,
                    recipient_type: r_type.clone(),
                    is_read: r_read.unwrap_or(false),
                    read_at: r_read_at,
                    is_recalled: r_recalled.unwrap_or(false),
                    recalled_at: r_recalled_at,
                };

                if r_type == "TO" {
                    to_recipients.push(info);
                    recipient_count += 1;
                } else if r_type == "CC" {
                    cc_recipients.push(info);
                    recipient_count += 1;
                } else if r_type == "BCC" {
                    recipient_count += 1;
                }
            }
        }

        let mut attachments = Vec::new();
        for ar in &att_rows {
            let mid: Option<Uuid> = ar.get("message_id");
            if mid == Some(msg_id) {
                attachments.push(AttachmentInfo {
                    id: ar.get("id"),
                    file_name: ar.get("file_name"),
                    file_size: ar.get::<Option<i64>, _>("file_size").unwrap_or(0),
                    content_type: ar.get("content_type"),
                });
            }
        }

        let has_attachments = !attachments.is_empty();
        let attachment_count = attachments.len();

        content.push(InboxMessageResponse {
            id: msg_id,
            recipient_id: Some(recipient_id),
            sender_id: sender_id.clone(),
            sender_name: sender_name.or_else(|| sender_id.clone()),
            sender_email,
            subject,
            body,
            importance: importance.unwrap_or_else(|| "NORMAL".to_string()),
            message_type: message_type.unwrap_or_else(|| "INTERNAL".to_string()),
            parent_message_id,
            root_message_id,
            related_approval_id,
            is_draft: is_draft.unwrap_or(false),
            is_read: is_read.unwrap_or(false),
            is_starred: is_starred.unwrap_or(false),
            folder: folder_val,
            has_attachments,
            attachment_count,
            recipient_count,
            thread_count: 1,
            to_recipients,
            cc_recipients,
            attachments,
            sent_at,
            created_at,
        });
    }

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn get_inbox_message(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<InboxMessageResponse>, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;

    // Fetch the message and user's recipient status
    let row = sqlx::query(
        r#"
        SELECT m.id, m.subject, m.body, m.sender_id, m.sender_email, m.created_at, m.sent_at,
               m.importance, m.is_draft, m.message_type, m.parent_message_id, m.root_message_id, m.related_approval_id,
               r.id as recipient_id, r.is_read, r.is_starred, r.folder,
               u.username as sender_name
        FROM inbox_message m
        LEFT JOIN inbox_recipient r ON (r.message_id = m.id AND (r.user_id = $2 OR r.user_id = $3 OR (r.email IS NOT NULL AND r.email != '' AND r.email = $4)))
        LEFT JOIN users u ON (m.sender_id = u.id OR m.sender_id = u.username)
        WHERE m.id = $1
        "#
    )
    .bind(id)
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .fetch_optional(&state.db)
    .await?
    .ok_or_else(|| AppError::NotFound("Message not found".to_string()))?;

    // Mark as read for this user if currently unread
    let recipient_id: Option<Uuid> = row.get("recipient_id");
    let is_read: bool = row.get::<Option<bool>, _>("is_read").unwrap_or(false);

    if let Some(r_id) = recipient_id {
        if !is_read {
            let _ = sqlx::query("UPDATE inbox_recipient SET is_read = true, read_at = NOW() WHERE id = $1")
                .bind(r_id)
                .execute(&state.db)
                .await;
        }
    }

    // Recipients
    let rec_rows = sqlx::query(
        r#"
        SELECT r.id, r.message_id, r.user_id, r.email, r.recipient_type, r.is_read, r.read_at, r.is_recalled, r.recalled_at,
               u.username as user_name
        FROM inbox_recipient r
        LEFT JOIN users u ON (r.user_id = u.id OR r.user_id = u.username)
        WHERE r.message_id = $1
        ORDER BY r.created_at ASC
        "#
    )
    .bind(id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let mut to_recipients = Vec::new();
    let mut cc_recipients = Vec::new();
    let mut recipient_count = 0;

    for rr in rec_rows {
        let r_type: String = rr.get("recipient_type");
        let r_uid: Option<String> = rr.get("user_id");
        let r_email: Option<String> = rr.get("email");
        let r_uname: Option<String> = rr.get("user_name");
        let r_read_val: Option<bool> = rr.get("is_read");
        let r_read_at: Option<NaiveDateTime> = rr.get("read_at");
        let r_recalled: Option<bool> = rr.get("is_recalled");
        let r_recalled_at: Option<NaiveDateTime> = rr.get("recalled_at");

        let name = r_uname.or_else(|| r_email.clone()).or_else(|| r_uid.clone());
        let info = RecipientInfo {
            user_id: r_uid,
            name,
            email: r_email,
            recipient_type: r_type.clone(),
            is_read: r_read_val.unwrap_or(false),
            read_at: r_read_at,
            is_recalled: r_recalled.unwrap_or(false),
            recalled_at: r_recalled_at,
        };

        if r_type == "TO" {
            to_recipients.push(info);
            recipient_count += 1;
        } else if r_type == "CC" {
            cc_recipients.push(info);
            recipient_count += 1;
        } else if r_type == "BCC" {
            recipient_count += 1;
        }
    }

    // Attachments
    let att_rows = sqlx::query(
        r#"
        SELECT id, file_name, file_size, content_type
        FROM inbox_attachment
        WHERE message_id = $1
        ORDER BY created_at ASC
        "#
    )
    .bind(id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let attachments: Vec<AttachmentInfo> = att_rows
        .into_iter()
        .map(|ar| AttachmentInfo {
            id: ar.get("id"),
            file_name: ar.get("file_name"),
            file_size: ar.get::<Option<i64>, _>("file_size").unwrap_or(0),
            content_type: ar.get("content_type"),
        })
        .collect();

    let has_attachments = !attachments.is_empty();
    let attachment_count = attachments.len();
    let sender_id: Option<String> = row.get("sender_id");
    let sender_name: Option<String> = row.get("sender_name");
    let folder_val: Option<String> = row.get("folder");

    Ok(Json(InboxMessageResponse {
        id,
        recipient_id,
        sender_id: sender_id.clone(),
        sender_name: sender_name.or_else(|| sender_id.clone()),
        sender_email: row.get("sender_email"),
        subject: row.get("subject"),
        body: row.get("body"),
        importance: row.get::<Option<String>, _>("importance").unwrap_or_else(|| "NORMAL".to_string()),
        message_type: row.get::<Option<String>, _>("message_type").unwrap_or_else(|| "INTERNAL".to_string()),
        parent_message_id: row.get("parent_message_id"),
        root_message_id: row.get("root_message_id"),
        related_approval_id: row.get("related_approval_id"),
        is_draft: row.get::<Option<bool>, _>("is_draft").unwrap_or(false),
        is_read: true,
        is_starred: row.get::<Option<bool>, _>("is_starred").unwrap_or(false),
        folder: folder_val.unwrap_or_else(|| "INBOX".to_string()),
        has_attachments,
        attachment_count,
        recipient_count,
        thread_count: 1,
        to_recipients,
        cc_recipients,
        attachments,
        sent_at: row.get("sent_at"),
        created_at: row.get("created_at"),
    }))
}

pub async fn send_inbox_message(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<InboxMessageRequest>,
) -> Result<Json<InboxMessageResponse>, AppError> {
    process_send_message(&state.db, &auth, payload, None).await
}

pub async fn reply_message(
    State(state): State<AppState>,
    Path(parent_id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<InboxMessageRequest>,
) -> Result<Json<InboxMessageResponse>, AppError> {
    process_send_message(&state.db, &auth, payload, Some(parent_id)).await
}

pub async fn reply_all_message(
    State(state): State<AppState>,
    Path(parent_id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<InboxMessageRequest>,
) -> Result<Json<InboxMessageResponse>, AppError> {
    process_send_message(&state.db, &auth, payload, Some(parent_id)).await
}

pub async fn forward_message(
    State(state): State<AppState>,
    Path(parent_id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<InboxMessageRequest>,
) -> Result<Json<InboxMessageResponse>, AppError> {
    process_send_message(&state.db, &auth, payload, Some(parent_id)).await
}

async fn process_send_message(
    pool: &PgPool,
    auth: &AuthUser,
    payload: InboxMessageRequest,
    forced_parent_id: Option<Uuid>,
) -> Result<Json<InboxMessageResponse>, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(pool, auth).await;
    let new_id = Uuid::new_v4();
    let is_draft = payload.is_draft.unwrap_or(false);
    let subject = payload.subject.filter(|s| !s.trim().is_empty()).unwrap_or_else(|| "(제목 없음)".to_string());
    let body = payload.body;
    let importance = payload.importance.unwrap_or_else(|| "NORMAL".to_string());
    let message_type = payload.message_type.unwrap_or_else(|| "INTERNAL".to_string());
    let parent_id = forced_parent_id.or(payload.parent_message_id);

    // Resolve root message id if parent exists
    let mut root_id = None;
    if let Some(pid) = parent_id {
        let parent_row: Option<(Option<Uuid>,)> = sqlx::query_as("SELECT root_message_id FROM inbox_message WHERE id = $1")
            .bind(pid)
            .fetch_optional(pool)
            .await
            .unwrap_or(None);
        if let Some((p_root,)) = parent_row {
            root_id = Some(p_root.unwrap_or(pid));
        } else {
            root_id = Some(pid);
        }
    }

    // Insert inbox_message
    sqlx::query(
        r#"
        INSERT INTO inbox_message (
            id, subject, body, sender_id, sender_email, importance, is_draft, message_type,
            parent_message_id, root_message_id, related_approval_id, sent_at, created_at, updated_at, version
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, (CASE WHEN $7 THEN NULL ELSE NOW() END), NOW(), NOW(), 1)
        "#
    )
    .bind(new_id)
    .bind(&subject)
    .bind(&body)
    .bind(&uid)
    .bind(&uemail)
    .bind(&importance)
    .bind(is_draft)
    .bind(&message_type)
    .bind(parent_id)
    .bind(root_id)
    .bind(payload.related_approval_id)
    .execute(pool)
    .await?;

    // Link attachments if provided
    if let Some(att_ids) = &payload.attachment_ids {
        if !att_ids.is_empty() {
            let _ = sqlx::query("UPDATE inbox_attachment SET message_id = $1 WHERE id = ANY($2)")
                .bind(new_id)
                .bind(att_ids)
                .execute(pool)
                .await;
        }
    }

    // Insert sender's recipient record (SENT or DRAFT)
    let sender_rec_id = Uuid::new_v4();
    let sender_folder = if is_draft { "DRAFT" } else { "SENT" };
    sqlx::query(
        r#"
        INSERT INTO inbox_recipient (
            id, message_id, user_id, email, folder, recipient_type, is_read, read_at, is_deleted, is_starred, is_recalled, created_at
        )
        VALUES ($1, $2, $3, $4, $5, 'FROM', true, NOW(), false, false, false, NOW())
        "#
    )
    .bind(sender_rec_id)
    .bind(new_id)
    .bind(&uid)
    .bind(&uemail)
    .bind(sender_folder)
    .execute(pool)
    .await?;

    let mut to_recipients = Vec::new();
    let mut cc_recipients = Vec::new();
    let mut recipient_count = 0;

    // If not draft, insert destination recipients
    if !is_draft {
        let recipient_groups = vec![
            (payload.to_recipients.unwrap_or_default(), "TO"),
            (payload.cc_recipients.unwrap_or_default(), "CC"),
            (payload.bcc_recipients.unwrap_or_default(), "BCC"),
        ];

        for (rec_list, r_type) in recipient_groups {
            for raw_rec in rec_list {
                let rec = raw_rec.trim();
                if rec.is_empty() {
                    continue;
                }

                // Resolve user
                let user_row: Option<(String, Option<String>, Option<String>)> = if rec.contains('@') {
                    sqlx::query_as("SELECT id, username, email FROM users WHERE LOWER(email) = LOWER($1) LIMIT 1")
                        .bind(rec)
                        .fetch_optional(pool)
                        .await
                        .unwrap_or(None)
                } else {
                    sqlx::query_as("SELECT id, username, email FROM users WHERE username = $1 OR id = $1 LIMIT 1")
                        .bind(rec)
                        .fetch_optional(pool)
                        .await
                        .unwrap_or(None)
                };

                let (target_uid, target_uname, target_email) = match user_row {
                    Some((u_id, u_name, u_email)) => (Some(u_id), u_name, u_email),
                    None => {
                        if rec.contains('@') {
                            (None, None, Some(rec.to_string()))
                        } else {
                            (Some(rec.to_string()), None, None)
                        }
                    }
                };

                let rec_id = Uuid::new_v4();
                let _ = sqlx::query(
                    r#"
                    INSERT INTO inbox_recipient (
                        id, message_id, user_id, email, folder, recipient_type, is_read, is_deleted, is_starred, is_recalled, created_at
                    )
                    VALUES ($1, $2, $3, $4, 'INBOX', $5, false, false, false, false, NOW())
                    "#
                )
                .bind(rec_id)
                .bind(new_id)
                .bind(&target_uid)
                .bind(&target_email)
                .bind(r_type)
                .execute(pool)
                .await;

                let name = target_uname.or_else(|| target_email.clone()).or_else(|| target_uid.clone());
                let info = RecipientInfo {
                    user_id: target_uid,
                    name,
                    email: target_email,
                    recipient_type: r_type.to_string(),
                    is_read: false,
                    read_at: None,
                    is_recalled: false,
                    recalled_at: None,
                };

                if r_type == "TO" {
                    to_recipients.push(info);
                    recipient_count += 1;
                } else if r_type == "CC" {
                    cc_recipients.push(info);
                    recipient_count += 1;
                } else if r_type == "BCC" {
                    recipient_count += 1;
                }
            }
        }
    }

    Ok(Json(InboxMessageResponse {
        id: new_id,
        recipient_id: Some(sender_rec_id),
        sender_id: Some(uid),
        sender_name: Some(uname),
        sender_email: Some(uemail),
        subject,
        body,
        importance,
        message_type,
        parent_message_id: parent_id,
        root_message_id: root_id,
        related_approval_id: payload.related_approval_id,
        is_draft,
        is_read: true,
        is_starred: false,
        folder: sender_folder.to_string(),
        has_attachments: false,
        attachment_count: 0,
        recipient_count,
        thread_count: 1,
        to_recipients,
        cc_recipients,
        attachments: Vec::new(),
        sent_at: if is_draft { None } else { Some(chrono::Utc::now().naive_utc()) },
        created_at: Some(chrono::Utc::now().naive_utc()),
    }))
}

pub async fn update_draft(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<InboxMessageRequest>,
) -> Result<Json<InboxMessageResponse>, AppError> {
    let (uid, uname, _) = resolve_user_identities(&state.db, &auth).await;

    // Check draft ownership
    let is_owner: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM inbox_message WHERE id = $1 AND (sender_id = $2 OR sender_id = $3) AND is_draft = true"
    )
    .bind(id)
    .bind(&uid)
    .bind(&uname)
    .fetch_one(&state.db)
    .await
    .unwrap_or((0,));

    if is_owner.0 == 0 {
        return Err(AppError::Forbidden("Cannot update this draft".to_string()));
    }

    let subject = payload.subject.filter(|s| !s.trim().is_empty()).unwrap_or_else(|| "(제목 없음)".to_string());
    let body = payload.body;
    let importance = payload.importance.unwrap_or_else(|| "NORMAL".to_string());

    sqlx::query(
        "UPDATE inbox_message SET subject = $1, body = $2, importance = $3, updated_at = NOW() WHERE id = $4"
    )
    .bind(&subject)
    .bind(&body)
    .bind(&importance)
    .bind(id)
    .execute(&state.db)
    .await?;

    get_inbox_message(State(state), Path(id), auth).await
}

pub async fn delete_inbox_message(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    Query(query): Query<DeleteInboxQuery>,
    auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;
    let permanent = query.permanent.unwrap_or(false);

    if permanent {
        sqlx::query(
            r#"
            DELETE FROM inbox_recipient
            WHERE message_id = $1
              AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
            "#
        )
        .bind(id)
        .bind(&uid)
        .bind(&uname)
        .bind(&uemail)
        .execute(&state.db)
        .await?;
    } else {
        sqlx::query(
            r#"
            UPDATE inbox_recipient
            SET folder = 'TRASH', is_deleted = false
            WHERE message_id = $1
              AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
            "#
        )
        .bind(id)
        .bind(&uid)
        .bind(&uname)
        .bind(&uemail)
        .execute(&state.db)
        .await?;
    }

    Ok(StatusCode::OK)
}

pub async fn toggle_read(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<ToggleReadRequest>,
) -> Result<StatusCode, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;
    let is_read = payload.is_read.unwrap_or(true);

    sqlx::query(
        r#"
        UPDATE inbox_recipient
        SET is_read = $1, read_at = (CASE WHEN $1 THEN NOW() ELSE NULL END)
        WHERE message_id = $2
          AND (user_id = $3 OR user_id = $4 OR (email IS NOT NULL AND email != '' AND email = $5))
        "#
    )
    .bind(is_read)
    .bind(id)
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .execute(&state.db)
    .await?;

    Ok(StatusCode::OK)
}

pub async fn toggle_star(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;

    sqlx::query(
        r#"
        UPDATE inbox_recipient
        SET is_starred = NOT COALESCE(is_starred, false)
        WHERE message_id = $1
          AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
        "#
    )
    .bind(id)
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .execute(&state.db)
    .await?;

    Ok(StatusCode::OK)
}

pub async fn move_to_folder(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<MoveFolderRequest>,
) -> Result<StatusCode, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;

    sqlx::query(
        r#"
        UPDATE inbox_recipient
        SET folder = $1, is_deleted = false
        WHERE message_id = $2
          AND (user_id = $3 OR user_id = $4 OR (email IS NOT NULL AND email != '' AND email = $5))
        "#
    )
    .bind(&payload.folder)
    .bind(id)
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .execute(&state.db)
    .await?;

    Ok(StatusCode::OK)
}

pub async fn bulk_inbox_action(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<BulkActionRequest>,
) -> Result<StatusCode, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;
    let ids = &payload.message_ids;

    if ids.is_empty() {
        return Ok(StatusCode::OK);
    }

    match payload.action.as_str() {
        "MARK_READ" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET is_read = true, read_at = NOW()
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "MARK_UNREAD" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET is_read = false, read_at = NULL
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "STAR" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET is_starred = true
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "UNSTAR" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET is_starred = false
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "MOVE_TO_TRASH" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET folder = 'TRASH', is_deleted = false
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "MOVE_TO_ARCHIVE" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET folder = 'ARCHIVE', is_deleted = false
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "RESTORE" => {
            sqlx::query(
                r#"
                UPDATE inbox_recipient
                SET folder = (CASE WHEN recipient_type = 'FROM' THEN 'SENT' ELSE 'INBOX' END), is_deleted = false
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        "PERMANENT_DELETE" => {
            sqlx::query(
                r#"
                DELETE FROM inbox_recipient
                WHERE message_id = ANY($1)
                  AND (user_id = $2 OR user_id = $3 OR (email IS NOT NULL AND email != '' AND email = $4))
                "#
            )
            .bind(ids)
            .bind(&uid)
            .bind(&uname)
            .bind(&uemail)
            .execute(&state.db)
            .await?;
        }
        _ => {}
    }

    Ok(StatusCode::OK)
}

pub async fn get_thread(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<Vec<InboxMessageResponse>>, AppError> {
    let (uid, uname, uemail) = resolve_user_identities(&state.db, &auth).await;

    // Find root message id
    let root_info: Option<(Option<Uuid>,)> = sqlx::query_as(
        "SELECT COALESCE(root_message_id, id) FROM inbox_message WHERE id = $1"
    )
    .bind(id)
    .fetch_optional(&state.db)
    .await?;

    let root_id = match root_info {
        Some((Some(r_id),)) => r_id,
        _ => id,
    };

    let rows = sqlx::query(
        r#"
        SELECT m.id, m.subject, m.body, m.sender_id, m.sender_email, m.created_at, m.sent_at,
               m.importance, m.is_draft, m.message_type, m.parent_message_id, m.root_message_id, m.related_approval_id,
               r.id as recipient_id, r.is_read, r.is_starred, r.folder,
               u.username as sender_name
        FROM inbox_message m
        LEFT JOIN inbox_recipient r ON (r.message_id = m.id AND (r.user_id = $2 OR r.user_id = $3 OR (r.email IS NOT NULL AND r.email != '' AND r.email = $4)))
        LEFT JOIN users u ON (m.sender_id = u.id OR m.sender_id = u.username)
        WHERE m.id = $1 OR m.root_message_id = $1 OR m.parent_message_id = $1
        ORDER BY COALESCE(m.sent_at, m.created_at) ASC
        "#
    )
    .bind(root_id)
    .bind(&uid)
    .bind(&uname)
    .bind(&uemail)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let mut thread = Vec::new();
    for row in rows {
        let msg_id: Uuid = row.get("id");
        let sender_id: Option<String> = row.get("sender_id");
        let sender_name: Option<String> = row.get("sender_name");

        thread.push(InboxMessageResponse {
            id: msg_id,
            recipient_id: row.get("recipient_id"),
            sender_id: sender_id.clone(),
            sender_name: sender_name.or_else(|| sender_id.clone()),
            sender_email: row.get("sender_email"),
            subject: row.get("subject"),
            body: row.get("body"),
            importance: row.get::<Option<String>, _>("importance").unwrap_or_else(|| "NORMAL".to_string()),
            message_type: row.get::<Option<String>, _>("message_type").unwrap_or_else(|| "INTERNAL".to_string()),
            parent_message_id: row.get("parent_message_id"),
            root_message_id: row.get("root_message_id"),
            related_approval_id: row.get("related_approval_id"),
            is_draft: row.get::<Option<bool>, _>("is_draft").unwrap_or(false),
            is_read: row.get::<Option<bool>, _>("is_read").unwrap_or(false),
            is_starred: row.get::<Option<bool>, _>("is_starred").unwrap_or(false),
            folder: row.get::<Option<String>, _>("folder").unwrap_or_else(|| "INBOX".to_string()),
            has_attachments: false,
            attachment_count: 0,
            recipient_count: 0,
            thread_count: 1,
            to_recipients: Vec::new(),
            cc_recipients: Vec::new(),
            attachments: Vec::new(),
            sent_at: row.get("sent_at"),
            created_at: row.get("created_at"),
        });
    }

    Ok(Json(thread))
}

pub async fn recall_message(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<RecallResultResponse>, AppError> {
    let (uid, uname, _) = resolve_user_identities(&state.db, &auth).await;

    // Check sender
    let msg: Option<(Option<String>,)> = sqlx::query_as("SELECT sender_id FROM inbox_message WHERE id = $1")
        .bind(id)
        .fetch_optional(&state.db)
        .await?;

    let sender = msg.and_then(|m| m.0).unwrap_or_default();
    if sender != uid && sender != uname {
        return Err(AppError::Forbidden("Only the sender can recall this message".to_string()));
    }

    // Recipients
    let recs = sqlx::query(
        r#"
        SELECT r.id, r.user_id, r.email, r.recipient_type, r.is_read, r.read_at, r.is_recalled,
               u.username as user_name
        FROM inbox_recipient r
        LEFT JOIN users u ON (r.user_id = u.id OR r.user_id = u.username)
        WHERE r.message_id = $1 AND r.recipient_type != 'FROM'
        "#
    )
    .bind(id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let mut details = Vec::new();
    let mut before_read_count = 0;
    let mut after_read_count = 0;
    let mut external_count = 0;

    for r in recs {
        let rec_id: Uuid = r.get("id");
        let r_uid: Option<String> = r.get("user_id");
        let r_email: Option<String> = r.get("email");
        let r_uname: Option<String> = r.get("user_name");
        let r_type: String = r.get("recipient_type");
        let was_read: bool = r.get::<Option<bool>, _>("is_read").unwrap_or(false);
        let read_at: Option<NaiveDateTime> = r.get("read_at");

        let is_external = r_uid.is_none() && r_email.is_some();
        if is_external {
            external_count += 1;
        }

        let name = r_uname.or_else(|| r_email.clone()).or_else(|| r_uid.clone());

        if !was_read {
            before_read_count += 1;
            let _ = sqlx::query("UPDATE inbox_recipient SET is_recalled = true, recalled_at = NOW() WHERE id = $1")
                .bind(rec_id)
                .execute(&state.db)
                .await;
            details.push(RecipientRecallDetail {
                user_id: r_uid,
                name,
                email: r_email,
                recipient_type: r_type,
                was_read: false,
                read_at: None,
                is_recalled: true,
                status: "RECALLED".to_string(),
            });
        } else {
            after_read_count += 1;
            details.push(RecipientRecallDetail {
                user_id: r_uid,
                name,
                email: r_email,
                recipient_type: r_type,
                was_read: true,
                read_at,
                is_recalled: false,
                status: "ALREADY_READ".to_string(),
            });
        }
    }

    Ok(Json(RecallResultResponse {
        message_id: id,
        total_recipients: details.len(),
        recalled_before_read_count: before_read_count,
        recalled_after_read_count: after_read_count,
        external_count,
        details,
    }))
}

pub async fn track_open(
    State(state): State<AppState>,
    Path(recipient_id): Path<Uuid>,
) -> impl IntoResponse {
    let _ = sqlx::query(
        "UPDATE inbox_recipient SET is_read = true, read_at = NOW() WHERE id = $1 AND is_read = false"
    )
    .bind(recipient_id)
    .execute(&state.db)
    .await;

    ([(axum::http::header::CONTENT_TYPE, "image/gif")], TRANSPARENT_1X1_GIF)
}
