const fs = require('fs');
const path = require('path');

const srcDir = 'c:/dev/ai/frontend/i18n/locales';
const destDir = 'c:/dev/ai/mobile/lib/core/l10n';
fs.mkdirSync(destDir, { recursive: true });

function toCamelCase(str) {
  let cleaned = str
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_|_$/g, '');
  
  if (!cleaned) cleaned = 'key';
  if (/^[0-9]/.test(cleaned)) cleaned = 'k_' + cleaned;

  const parts = cleaned.split('_').filter(Boolean);
  if (parts.length === 0) return 'key';
  
  return parts[0].toLowerCase() + parts.slice(1).map(p => 
    p.charAt(0).toUpperCase() + p.slice(1)
  ).join('');
}

const dartKeywords = [
  'default', 'new', 'assert', 'null', 'true', 'false', 'return', 'if', 'else', 'case', 
  'continue', 'break', 'switch', 'for', 'while', 'do', 'in', 'is', 'as', 'try', 'catch', 
  'finally', 'throw', 'void', 'var', 'final', 'const', 'late', 'class', 'enum', 'extends', 
  'implements', 'with', 'super', 'this', 'get', 'set', 'dynamic', 'int', 'double', 'String', 'bool'
];

function flattenObject(obj, prefix = '') {
  const result = {};
  for (const [key, val] of Object.entries(obj)) {
    const fullKey = prefix ? `${prefix}_${key}` : key;
    if (val && typeof val === 'object' && !Array.isArray(val)) {
      // Add both bare key and prefixed key to maximize compatibility
      Object.assign(result, flattenObject(val, fullKey));
      Object.assign(result, flattenObject(val, ''));
    } else if (typeof val === 'string') {
      result[fullKey] = val;
      if (prefix) {
        result[key] = val;
      }
    }
  }
  return result;
}

const mobileCustomKo = {
  navTabRecords: "레코드",
  navTabHome: "홈",
  navTabApprovals: "승인",
  navTabNotifications: "알림",
  navTabChat: "채팅",
  homeWelcomeTitle: "거버넌스 포털 대시보드",
  homeTodoTitle: "나의 처리 대기 현황",
  homeRecentActivity: "최근 변경 및 승인 활동",
  homeUnreadMessages: "미안독 채팅 메시지",
  homeNoActivity: "최근 활동 이력이 없습니다.",
  notificationsTitle: "시스템 알림 센터",
  notificationsEmpty: "수신된 새로운 알림이 없습니다.",
  notificationsMarkAllRead: "전체 읽음 처리",
  chatCreateRoom: "대화방 만들기",
  chatRoomTitlePlaceholder: "대화방 제목을 입력하세요",
  chatTitle: "실시간 협업 메신저",
  chatEmptyRooms: "참여 중인 대화방이 없습니다.",
  chatSelectMembers: "대화 상대 선택 (1명 이상 필수)",
  chatSearchSelectUser: "사용자 검색/선택",
  chatNoUserSelected: "선택된 사용자가 없습니다.",
  chatUserMe: "{username} (나)",
  chatCreateRoomFailed: "대화방 생성에 실패했습니다.",
  chatSearchUserHint: "사용자명, 역할, 부서 검색...",
  chatConfirmBtn: "확인",
  allCategories: "전체 (All)",
  recordData: "레코드 데이터",
  viewReasonTitle: "열람 사유 입력",
  viewReasonHint: "사유를 입력하세요 (예: 본인확인용)",
  viewReasonEmpty: "사유를 입력해야 합니다.",
  decryptSuccessNotice: "복호화가 완료되었습니다. (30초 후 다시 마스킹됩니다)",
  decryptFailedNotice: "복호화 실패:",
  keyInfo: "키 정보",
  generalInfo: "일반정보",
  viewHistory: "이력 보기",
  userProfileTitle: "사용자 프로필",
  inboxTitle: "문서함",
  loginWithKeycloak: "Keycloak SSO로 로그인",
  loginDividerOr: "또는 일반 계정으로 로그인",
  loginStandard: "일반 계정 로그인",
  loginSsoError: "SSO 로그인 중 오류가 발생했습니다.",
};

const mobileCustomEn = {
  navTabRecords: "Records",
  navTabHome: "Home",
  navTabApprovals: "Approvals",
  navTabNotifications: "Notifications",
  navTabChat: "Chat",
  homeWelcomeTitle: "Governance Portal Dashboard",
  homeTodoTitle: "My Pending Tasks",
  homeRecentActivity: "Recent Activities & Approvals",
  homeUnreadMessages: "Unread Messages",
  homeNoActivity: "No recent activities.",
  notificationsTitle: "Notification Center",
  notificationsEmpty: "No new notifications.",
  notificationsMarkAllRead: "Mark all as read",
  chatCreateRoom: "Create Room",
  chatRoomTitlePlaceholder: "Enter chat room title",
  chatTitle: "Real-time Messenger",
  chatEmptyRooms: "No active chat rooms.",
  chatSelectMembers: "Select Members (at least 1 required)",
  chatSearchSelectUser: "Search/Select User",
  chatNoUserSelected: "No user selected.",
  chatUserMe: "{username} (Me)",
  chatCreateRoomFailed: "Failed to create chat room.",
  chatSearchUserHint: "Search by username, role, department...",
  chatConfirmBtn: "Confirm",
  allCategories: "All Categories",
  recordData: "Record Data",
  viewReasonTitle: "Enter View Reason",
  viewReasonHint: "Enter reason (e.g. Identity verification)",
  viewReasonEmpty: "Reason is required.",
  decryptSuccessNotice: "Decrypted successfully. (Will be masked again in 30 seconds)",
  decryptFailedNotice: "Decryption failed:",
  keyInfo: "Key Information",
  generalInfo: "General Information",
  viewHistory: "View History",
  userProfileTitle: "User Profile",
  inboxTitle: "Inbox",
  loginWithKeycloak: "Login with Keycloak SSO",
  loginDividerOr: "Or login with standard account",
  loginStandard: "Standard Account Login",
  loginSsoError: "An error occurred during SSO login.",
};

function processLocale(locale) {
  const dir = path.join(srcDir, locale);
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.json'));
  const merged = { "@@locale": locale };

  for (const file of files) {
    const rawContent = JSON.parse(fs.readFileSync(path.join(dir, file), 'utf8'));
    const content = flattenObject(rawContent);
    for (let [rawKey, val] of Object.entries(content)) {
      if (typeof val !== 'string') continue;
      
      val = val.replace(/\{['"]?@['"]?\}/g, '@');

      let camelKey = toCamelCase(rawKey);
      if (dartKeywords.includes(camelKey)) {
        camelKey = camelKey + 'Key';
      }

      merged[camelKey] = val;

      const matches = val.match(/\{([a-zA-Z0-9_]+)\}/g);
      if (matches) {
        const placeholders = {};
        for (const m of matches) {
          const p = m.slice(1, -1);
          if (!/^[a-zA-Z]/.test(p)) continue;
          placeholders[p] = { type: 'Object' };
        }
        if (Object.keys(placeholders).length > 0) {
          merged['@' + camelKey] = {
            description: `Translated from ${rawKey}`,
            placeholders
          };
        }
      }
    }
  }

  // Merge mobile custom keys
  const customMap = locale === 'ko' ? mobileCustomKo : mobileCustomEn;
  for (const [key, val] of Object.entries(customMap)) {
    merged[key] = val;
    const matches = val.match(/\{([a-zA-Z0-9_]+)\}/g);
    if (matches) {
      const placeholders = {};
      for (const m of matches) {
        const p = m.slice(1, -1);
        placeholders[p] = { type: 'String' };
      }
      merged['@' + key] = { placeholders };
    }
  }

  const outPath = path.join(destDir, `app_${locale}.arb`);
  fs.writeFileSync(outPath, JSON.stringify(merged, null, 2), 'utf8');
  console.log(`Generated ${outPath} (${Object.keys(merged).length} entries)`);
}

processLocale('en');
processLocale('ko');

