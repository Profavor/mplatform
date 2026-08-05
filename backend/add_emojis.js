const fs = require('fs');

const path = 'C:\\dev\\ai\\backend\\src\\main\\resources\\default_codes.json';
const data = JSON.parse(fs.readFileSync(path, 'utf8'));

const emojiMap = {
  // TARGET_TYPE
  "RECORD": "🔍", "RECORD_HISTORY": "📜", "APPROVAL_REQUEST": "📝",
  // MASKING_PATTERN
  "GENERIC": "🛡️", "CARD": "💳", "RRN": "🆔", "PHONE": "📱", "EMAIL": "✉️",
  // DQ_RULE_TYPE
  "COMPLETENESS": "🧩", "VALIDITY": "✅", "ACCURACY": "🎯", "CONSISTENCY": "⚖️", "UNIQUENESS": "💎",
  // DQ_SEVERITY
  "CRITICAL": "🚨", "HIGH": "🔴", "MEDIUM": "🟡", "LOW": "🔵",
  // APPROVAL_STATUS
  "PENDING": "⏳", "IN_PROGRESS": "🔄", "APPROVED": "✅", "REJECTED": "❌", "CANCELLED": "🚫",
  // MATCHING_STATUS
  "MATCHED": "🔗",
  // BATCH_JOB_STATUS
  "QUEUED": "📥", "RUNNING": "⚙️", "COMPLETED": "✅", "FAILED": "❌",
  // FIELD_TYPE
  "TEXT": "🔤", "NUMBER": "🔢", "DATE": "📅", "BOOLEAN": "🔘", "JSON": "📄", "ENUM": "📋",
  // INTEGRATION_TYPE
  "WEB_SERVICE": "🌐", "JDBC": "🗄️", "MESSAGE_QUEUE": "📬",
  // INTEGRATION_STATUS
  "SUCCESS": "✅", "FAIL": "❌", "DEAD_LETTER": "☠️",
  // NOTIFICATION_TYPE
  "INFO": "ℹ️", "WARNING": "⚠️", "APPROVAL": "📝", "DQ_VIOLATION": "🚨",
  // RECORD_STATUS
  "DRAFT": "📝", "PENDING_APPROVAL": "⏳", "ACTIVE": "🟢", "INACTIVE": "🔴", "MISMATCHED": "⚠️", 
  // SCHEMA_ACTION
  "CREATE": "🆕", "UPDATE": "🛠️", "DELETE": "🗑️",
  // STAGING_STATUS
  "VALIDATED": "✅", "ERROR": "❌", "COMMITTED": "💾",
  // USER_ROLE
  "ROLE_USER": "👤", "ROLE_ADMIN": "👨‍💼", "ROLE_SUPERADMIN": "👑", "ROLE_DATA_STEWARD": "🛡️", "ROLE_APPROVER": "✍️",
  // INTEGRATION_DIRECTION
  "OUTBOUND": "📤", "INBOUND": "📥",
  // HTTP_METHOD
  "GET": "📥", "POST": "📤", "PUT": "🔄", "PATCH": "🩹",
  // SURVIVORSHIP_STRATEGY
  "SOURCE_PRIORITY": "🏅", "MOST_RECENT": "🕒", "MOST_COMPLETE": "🧩",
  // MATCH_TYPE
  "EXACT": "🎯", "FUZZY": "🔮"
};

// Also apply to WORKFLOW_ACTION explicitly if needed, but they are CREATE/UPDATE which are covered

data.forEach(group => {
  group.details.forEach(detail => {
    const emoji = emojiMap[detail.detailCode];
    if (emoji) {
      if (detail.name.ko && !detail.name.ko.includes(emoji) && !detail.name.ko.match(/^[\u{1F300}-\u{1F9FF}]/u)) {
        // Remove existing emojis if any
        detail.name.ko = detail.name.ko.replace(/^[\u{1000}-\u{1FFFF}]+\s*/gu, '');
        detail.name.ko = `${emoji} ${detail.name.ko}`;
      }
      if (detail.name.en && !detail.name.en.includes(emoji) && !detail.name.en.match(/^[\u{1F300}-\u{1F9FF}]/u)) {
        detail.name.en = detail.name.en.replace(/^[\u{1000}-\u{1FFFF}]+\s*/gu, '');
        detail.name.en = `${emoji} ${detail.name.en}`;
      }
    }
  });
});

fs.writeFileSync(path, JSON.stringify(data, null, 2), 'utf8');
console.log('Done mapping emojis to codes!');
