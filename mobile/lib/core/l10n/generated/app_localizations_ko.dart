// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Korean (`ko`).
class AppLocalizationsKo extends AppLocalizations {
  AppLocalizationsKo([String locale = 'ko']) : super(locale);

  @override
  String get emptyNotification => '수정된 데이터가 없습니다.';

  @override
  String get beforeValue => '변경 전 (Previous Value)';

  @override
  String get afterValue => '변경 후 (New Value)';

  @override
  String get approvalDraft => '기안';

  @override
  String get processedStatus => '처리됨';

  @override
  String get systemAutoApprovedNotice => '시스템 자동 승인 (결재선 미설정)';

  @override
  String get targetTypeRecordUpdate => '마스터 레코드 수정';

  @override
  String get targetTypeRecordCreate => '마스터 레코드 등록';

  @override
  String get targetTypeRecordDelete => '마스터 레코드 삭제';

  @override
  String get targetTypeSchemaChange => '스키마 변경';

  @override
  String get targetTypeSandbox => '샌드박스';

  @override
  String get viewOriginal => '원문 보기';

  @override
  String get hideOriginal => '원문 숨기기';

  @override
  String get fontSizeSetting => '글자 크기 설정';

  @override
  String get fontSizeSmall => '작게';

  @override
  String get fontSizeMedium => '보통';

  @override
  String get fontSizeLarge => '크게';

  @override
  String get fontSizeXlarge => '아주 크게';

  @override
  String get maskingPattern => '마스킹 포맷 (Masking Pattern)';

  @override
  String get encryptedField => '암호화 필드';

  @override
  String get sensitiveAccessLogs => 'Decryption Logs';

  @override
  String get topUsers => '상위 사용자';

  @override
  String get accessLogViewer => '열람자';

  @override
  String get accessLogTargetType => '대상 유형';

  @override
  String get accessLogTargetId => '대상 ID';

  @override
  String get accessLogFields => '열람 필드';

  @override
  String get accessLogReason => '접근 사유';

  @override
  String get approvalREQUEST => '결재 요청';

  @override
  String get record => '직접 조회';

  @override
  String get recordHISTORY => '변경 이력 조회';

  @override
  String get accessLogIp => 'IP 주소';

  @override
  String get accessLogTime => '열람 시각';

  @override
  String get maskingPatternGeneric => '일반 마스킹 (GENERIC)';

  @override
  String get maskingPatternCard => '카드번호 (1234-****-****-5678)';

  @override
  String get maskingPatternRrn => '주민등록번호 (900101-1******)';

  @override
  String get maskingPatternPhone => '전화번호 (010-****-5678)';

  @override
  String get maskingPatternEmail => '이메일 (u***@example.com)';

  @override
  String get addNewDept => '부서 신규 등록';

  @override
  String get addNewTeam => '신규 팀 추가';

  @override
  String get addRole => '역할 추가';

  @override
  String get addRootDept => '최상위 부서 추가';

  @override
  String get addSubdept => '하위 부서 추가';

  @override
  String get addTeam => '팀 추가';

  @override
  String get admin => '관리자';

  @override
  String get adminMonitor => '관리자 모니터';

  @override
  String get applicantRole => '신청 역할';

  @override
  String get applicantUser => '신청 사용자';

  @override
  String get assignDept => '부서 등록';

  @override
  String get assigneeRole => '역할';

  @override
  String get assigneeUser => '사용자';

  @override
  String get auditSourceSystem => '출처 시스템';

  @override
  String get belongsToDept => '소속 부서';

  @override
  String get belongsToOrg => '소속 조직';

  @override
  String get companyOrg => '회사 / 조직';

  @override
  String get checkDuplicate => '중복 확인';

  @override
  String get createNewOrg => '신규 조직 등록';

  @override
  String get createOrganization => '신규 조직 등록';

  @override
  String get createRoleTitle => '조직 RBAC 역할 신규 등록';

  @override
  String get currentDept => '현재 부서';

  @override
  String get deleteDept => '부서/조직 삭제';

  @override
  String get deleteOrganization => '조직 삭제';

  @override
  String get dept => '부서';

  @override
  String get deptAssignCol => '부서 지정';

  @override
  String get deptMembers => '부서 구성원 지정 및 관리';

  @override
  String get deptMembersDesc => '선택된 부서에 구성원을 신규 추가하거나 할당 해제합니다.';

  @override
  String get deptName => '부서/조직명';

  @override
  String get deptRoles => '부서 역할 (다중 선택 가능)';

  @override
  String get deptStatusCol => '소속 상태';

  @override
  String get deptStructure => '소속 부서 및 조직 계층 구조 (Tree View)';

  @override
  String get deptStructureDesc => '조직 - 상위 부서 - 하위 부서 N단계 계층 구조';

  @override
  String get deptTeamManagement => '부서 / 팀 관리';

  @override
  String get editDept => '부서/조직 정보 수정';

  @override
  String get editRoleTitle => '조직 RBAC 역할 정보 수정';

  @override
  String get effectiveRoles => '유효 통합 권한 (Effective Roles)';

  @override
  String get encrypted => '암호화';

  @override
  String get errorUsernameExists => '이미 사용 중인 사용자 아이디입니다.';

  @override
  String get formulaSettings => '수식 설정 (Formula)';

  @override
  String get installAdminName => '관리자 성명 (Display Name)';

  @override
  String get installAdminPwd => '비밀번호 (Password)';

  @override
  String get installAdminPwdConfirm => '비밀번호 확인 (Confirm)';

  @override
  String get installAdminUsername => '관리자 아이디 (Username)';

  @override
  String get installAdminEmail => '이메일 주소 (Email)';

  @override
  String get installRequireEmail => '이메일 주소를 입력해주세요.';

  @override
  String get installRequireEmailValid => '올바른 이메일 형식이 아닙니다.';

  @override
  String get sourceSystemNode => '소스 시스템';

  @override
  String get installOrgEn => '대표 마스터 조직명 (영문)';

  @override
  String get installOrgEnPlaceholder => 'e.g. Enterprise HQ';

  @override
  String get installOrgKo => '대표 마스터 조직명 (한글)';

  @override
  String get installOrgKoPlaceholder => '예: (주)엔터프라이즈 본사';

  @override
  String get installOrgEmailDomain => '조직 기본 이메일 도메인';

  @override
  String get installOrgEmailDomainTip =>
      '조직 구성원의 기본 이메일 도메인을 설정합니다. (예: company.com, 선택 사항)';

  @override
  String get installOrgTip =>
      '생성된 대표 조직에 시스템 표준 7종 역할(Role) 및 와일드카드(*) 권한이 자동 부여됩니다.';

  @override
  String get installRequireOrgEn => '영문 조직명을 입력해주세요.';

  @override
  String get installRequireOrgKo => '한글 조직명을 입력해주세요.';

  @override
  String get installRequireUsername => '아이디를 입력해주세요.';

  @override
  String get integrationChannelSystem => '연계 채널/시스템';

  @override
  String get labelRole => '역할';

  @override
  String get labelUsername => '아이디';

  @override
  String get menuAccessLogs => '메뉴 접근 로그';

  @override
  String get msgUsernameAvailable => '사용 가능한 아이디입니다.';

  @override
  String get msgUsernameCheckRequired => '아이디 중복 확인을 진행해 주세요.';

  @override
  String get msgUsernameExists => '이미 사용 중인 아이디입니다.';

  @override
  String get noDeptAssignedTip => '(부서 미할당 - [조직 관리] 메뉴에서 부서 지정 가능)';

  @override
  String get noDeptsAdded => '등록된 부서가 없습니다. [+ 부서 추가] 버튼을 클릭해 보세요.';

  @override
  String get noOrgHistory => '기록된 조직 이동 이력이 없습니다.';

  @override
  String get optionsSettings => '옵션 설정';

  @override
  String get orgCodePlaceholder => '시스템 식별 코드 (예: acme_corp)';

  @override
  String get orgCreatedSuccess => '조직이 성공적으로 생성되었습니다.';

  @override
  String get orgDeleteFailed => '조직 삭제 중 오류가 발생했습니다.';

  @override
  String get orgDeleteSuccess => '조직이 성공적으로 삭제되었습니다.';

  @override
  String get orgDescription => '조직 설명';

  @override
  String get orgDisplayName => '조직 표시명';

  @override
  String get orgDisplayNamePlaceholder => '조직 표시명 (예: Acme Corporation)';

  @override
  String get orgHistoryTitle => '소속 조직 이동 / 변경 이력';

  @override
  String get orgIcon => '조직 아이콘';

  @override
  String get orgInfoTitle => '소속 조직 정보';

  @override
  String get orgList => '조직 목록';

  @override
  String get orgManagementDesc =>
      '멀티 테넌트 조직(Organization), 계층형 부서/팀 및 RBAC 역할/권한 체계를 통합 관리합니다.';

  @override
  String get orgSysCode => '시스템 코드명 (Unique)';

  @override
  String get orgTenantManagement => '조직 및 부서 관리';

  @override
  String get orgUpdatedSuccess => '조직 정보가 성공적으로 수정되었습니다.';

  @override
  String get organization => '조직';

  @override
  String get organizationManagement => '조직 관리';

  @override
  String get otherDept => '타부서 소속';

  @override
  String get parentDept => '상위 부서 (미선택 시 최상위 부서)';

  @override
  String get personalSettings => '개인 설정';

  @override
  String get placeholderUsername => '아이디를 입력하세요';

  @override
  String get rbacRoleManagement => '역할 / 권한 관리';

  @override
  String get requiredRoles => '필요 역할 (다중 선택)';

  @override
  String get roleAdmin => '시스템 관리자';

  @override
  String get roleAssignCol => '부서 할당 역할 지정';

  @override
  String get roleCodeLabel => '역할 코드명 (예: CUSTOM_MANAGER, DQ_OPERATOR)';

  @override
  String get roleCreationPlaceholder => '조직 전용 역할 추가 기능 준비 중입니다.';

  @override
  String get roleDescriptionLabel => 'ROLE DESCRIPTION (역할 상세 설명)';

  @override
  String get roleDisplayNameLabel => 'ROLE DISPLAY NAME (역할 표시명)';

  @override
  String get roleDomainEditor => '도메인 편집자';

  @override
  String get roleDomainViewer => '도메인 조회자';

  @override
  String get roleUser => '일반 사용자';

  @override
  String get saveRole => '역할 저장';

  @override
  String get searchUserBtn => '사용자 검색 및 등록';

  @override
  String get searchUserModalTitle => '부서 구성원 검색 및 선택';

  @override
  String get searchUserPlaceholder => '사용자명 또는 역할 검색...';

  @override
  String get selectApproverRole => '결재 역할 선택';

  @override
  String get selectApproverUser => '결재자 선택';

  @override
  String get selectRole => '역할 선택';

  @override
  String get selectRoleToAdd => '역할을 선택하여 추가하세요';

  @override
  String get selectUser => '사용자 선택';

  @override
  String get syncDefaultRoles => '기본 역할/권한 동기화';

  @override
  String get syncDefaultRolesConfirmAll =>
      '전체 조직의 8대 기본 시스템 역할 및 누락된 권한을 표준 명세와 동기화하시겠습니까?';

  @override
  String syncDefaultRolesConfirmOrg(Object name) {
    return '$name 조직의 8대 기본 시스템 역할 및 누락된 권한을 표준 명세와 동기화하시겠습니까?';
  }

  @override
  String get syncDefaultRolesError => '오류가 발생했습니다.';

  @override
  String get syncDefaultRolesFail => '기본 역할 동기화에 실패했습니다.';

  @override
  String get syncDefaultRolesSuccess => '기본 역할 및 퍼미션 동기화가 완료되었습니다.';

  @override
  String get systemapplied => '시스템 반영';

  @override
  String get systemcancelled => '취소됨';

  @override
  String get systemcomplete => '완료';

  @override
  String get systemCustomRoles => '조직 RBAC 역할 및 부여 권한';

  @override
  String get systemLogs => '시스템 로그';

  @override
  String get systemLogsDesc =>
      '시스템 작업 이력, 사용자 로그인 기록, 예외 에러 및 연계 채널 전송 로그를 실시간 관제합니다.';

  @override
  String get systemLogsTitle => '시스템 로그 및 연계 관제';

  @override
  String get systemNotification => '시스템 알림';

  @override
  String get systemOrgInfo => '시스템 조직 정보';

  @override
  String get team => '팀';

  @override
  String get teamName => '팀명';

  @override
  String get tempPassword => '임시 비밀번호';

  @override
  String get tempPasswordCheck => '임시 비밀번호 확인';

  @override
  String get viewTempPassword => '임시 비밀번호 보기';

  @override
  String get tempPasswordWarning =>
      '이 비밀번호는 다시 표시되지 않으므로 반드시 복사하여 사용자에게 전달해 주세요.';

  @override
  String get updateRole => '역할 변경';

  @override
  String get userManagement => '사용자 관리';

  @override
  String get userManagementDesc =>
      '사용자 계정 정보, 소속 조직/부서 및 시스템 역할 및 도메인 접근 권한을 관리합니다.';

  @override
  String get userProfileTitle => '사용자 프로필';

  @override
  String get userInfoAndRole => '사용자 기본 정보 및 시스템 역할';

  @override
  String get userEmail => '이메일 주소';

  @override
  String get saveUserInfo => '사용자 정보 저장';

  @override
  String get userInfoUpdatedSuccess => '사용자 정보(이메일 및 역할)가 성공적으로 저장되었습니다.';

  @override
  String get userInfoUpdateFailed => '사용자 정보 저장 실패: ';

  @override
  String get invalidEmailFormat => '올바른 이메일 형식을 입력해주세요.';

  @override
  String get userRole => '사용자 역할';

  @override
  String get userRoles => '사용자 시스템 역할 (다중 선택 가능)';

  @override
  String get username => '사용자명';

  @override
  String get usernameCol => '사용자명 (Username)';

  @override
  String get viewUserProfile => '사용자 프로필 보기';

  @override
  String get backupSeedFiles => '현재 상태 백업 (Seed)';

  @override
  String get codeManagementTitle => '공통 코드 관리 (Data Dictionary)';

  @override
  String get title => '스키마 변경 이력';

  @override
  String get codeManagementDesc =>
      '시스템 전반에서 사용되는 공통 코드 그룹 및 하위 상세 코드를 계층적으로 관리합니다.';

  @override
  String get desc => '도메인별 중복 레코드 판별을 위한 EXACT / FUZZY 매칭 규칙 및 유사도 임계값을 설정합니다.';

  @override
  String get codeManagementExportJson => 'JSON 엑스포트';

  @override
  String get exportJson => 'JSON 엑스포트';

  @override
  String get codeManagementImportJson => 'JSON 임포트';

  @override
  String get importJson => 'JSON 임포트';

  @override
  String get codeManagementCodeGroups => '코드 그룹';

  @override
  String get codeGroups => '코드 그룹';

  @override
  String get codeManagementCodeDetails => '상세 코드';

  @override
  String get codeDetails => '상세 코드';

  @override
  String get codeManagementAdd => '추가';

  @override
  String get add => '채널 등록';

  @override
  String get codeManagementGroupCode => '그룹 코드';

  @override
  String get groupCode => '그룹 코드';

  @override
  String get codeManagementName => '이름';

  @override
  String get name => '이름';

  @override
  String get codeManagementStatus => '상태';

  @override
  String get status => '상태';

  @override
  String get codeManagementManage => '관리';

  @override
  String get manage => '관리';

  @override
  String get codeManagementDetailCode => '상세 코드';

  @override
  String get detailCode => '상세 코드';

  @override
  String get codeManagementSortOrder => '정렬 순서';

  @override
  String get sortOrder => '정렬 순서';

  @override
  String get codeManagementEditGroup => '그룹 수정';

  @override
  String get editGroup => '그룹 수정';

  @override
  String get codeManagementAddGroup => '그룹 추가';

  @override
  String get addGroup => '그룹 추가';

  @override
  String get codeManagementNameKo => '이름 (한국어)';

  @override
  String get nameKo => '이름 (한국어)';

  @override
  String get codeManagementNameEn => '이름 (영어)';

  @override
  String get nameEn => '이름 (영어)';

  @override
  String get codeManagementDescKo => '설명 (한국어)';

  @override
  String get descKo => '설명 (한국어)';

  @override
  String get codeManagementDescEn => '설명 (영어)';

  @override
  String get descEn => '설명 (영어)';

  @override
  String get codeManagementActive => '사용 여부';

  @override
  String get active => '활성';

  @override
  String get codeManagementCancel => '취소';

  @override
  String get cancel => '취소';

  @override
  String get codeManagementSave => '저장';

  @override
  String get save => '저장';

  @override
  String get codeManagementEditDetail => '상세 코드 수정';

  @override
  String get editDetail => '상세 코드 수정';

  @override
  String get codeManagementAddDetail => '상세 코드 추가';

  @override
  String get addDetail => '상세 코드 추가';

  @override
  String get codeManagementSelectGroupMsg => '좌측에서 코드 그룹을 선택하세요.';

  @override
  String get selectGroupMsg => '좌측에서 코드 그룹을 선택하세요.';

  @override
  String codeManagementConfirmDeleteGroup(Object code) {
    return '$code 그룹을 삭제하시겠습니까?';
  }

  @override
  String confirmDeleteGroup(Object code) {
    return '$code 그룹을 삭제하시겠습니까?';
  }

  @override
  String codeManagementConfirmDeleteDetail(Object code) {
    return '$code 상세 코드를 삭제하시겠습니까?';
  }

  @override
  String confirmDeleteDetail(Object code) {
    return '$code 상세 코드를 삭제하시겠습니까?';
  }

  @override
  String get codeManagementExportFailed => 'JSON 엑스포트에 실패했습니다.';

  @override
  String get exportFailed => 'JSON 엑스포트에 실패했습니다.';

  @override
  String get codeManagementImportSuccess => '코드가 성공적으로 임포트되었습니다.';

  @override
  String importSuccess(Object nodes, Object fields) {
    return '도메인 패키지가 성공적으로 가져와졌습니다. (노드 $nodes개, 필드 $fields개 생성)';
  }

  @override
  String get codeManagementImportFailed =>
      'JSON 임포트에 실패했습니다. 형식이 잘못되었거나 서버 오류가 발생했습니다.';

  @override
  String get importFailed => '패키지 가져오기에 실패했습니다.';

  @override
  String get codeManagementSaveSuccess => '성공적으로 저장되었습니다.';

  @override
  String get saveSuccess => '병합 생존 규칙이 저장되었습니다.';

  @override
  String get codeManagementSaveFailed => '저장에 실패했습니다.';

  @override
  String get saveFailed => '저장에 실패했습니다.';

  @override
  String get codeManagementDeleteSuccess => '성공적으로 삭제되었습니다.';

  @override
  String get deleteSuccess => '삭제 완료';

  @override
  String get codeManagementDeleteFailed => '삭제에 실패했습니다.';

  @override
  String get deleteFailed => '삭제에 실패했습니다.';

  @override
  String get codeManagementLoadFailed => '데이터를 불러오지 못했습니다.';

  @override
  String get loadFailed => '데이터를 불러오지 못했습니다.';

  @override
  String get codeManagementSyncCodes => '기본 코드 동기화';

  @override
  String get syncCodes => '기본 코드 동기화';

  @override
  String get codeManagementDumpCodes => '기본 코드 백업';

  @override
  String get dumpCodes => '기본 코드 백업';

  @override
  String get globalSearchPlaceholder => '전역 검색 (Search any data...)';

  @override
  String get searchMinLength => '최소 2글자 이상 입력해주세요.';

  @override
  String get searchNoResults => '검색 결과가 없습니다.';

  @override
  String get searchNoData => '데이터 없음';

  @override
  String get matchingrulesTitle => '매칭 규칙 관리';

  @override
  String get matchingrulesSubtitle =>
      '도메인별 중복 레코드 판별을 위한 EXACT / FUZZY 매칭 규칙 및 유사도 임계값을 설정합니다.';

  @override
  String get subtitle => '내부 메시지 및 이메일 통합 문서함';

  @override
  String get matchingrulesSelectDomainPlaceholder => '도메인 선택';

  @override
  String get selectDomainPlaceholder => '도메인 선택';

  @override
  String get matchingrulesAddRule => '규칙 추가';

  @override
  String get addRule => '비즈니스 룰 추가';

  @override
  String get matchingrulesRefresh => '새로고침';

  @override
  String get refresh => '새로고침';

  @override
  String get matchingrulesTotalReviewed => '총 검토 건수';

  @override
  String get totalReviewed => '총 검토 건수';

  @override
  String get matchingrulesPrecision => '정탐률';

  @override
  String get precision => '정탐률';

  @override
  String get matchingrulesConfirmed => '정탐(Confirmed)';

  @override
  String get confirmed => '정탐(Confirmed)';

  @override
  String get matchingrulesRejected => '오탐(Rejected)';

  @override
  String get rejected => '오탐(Rejected)';

  @override
  String get matchingrulesCurrentThreshold => '현재 Threshold';

  @override
  String get currentThreshold => '현재 Threshold';

  @override
  String get matchingrulesRecommendedThreshold => '추천 Threshold';

  @override
  String get recommendedThreshold => '추천 Threshold';

  @override
  String get matchingrulesRuleList => '매칭 규칙 목록';

  @override
  String get ruleList => '규칙 목록';

  @override
  String matchingrulesItemsCount(Object count) {
    return '$count개 항목';
  }

  @override
  String itemsCount(Object count) {
    return '개 항목';
  }

  @override
  String get matchingrulesEmptyNoRules => '등록된 매칭 규칙이 없습니다.';

  @override
  String get emptyNoRules => '등록된 서바이버십 규칙이 없습니다.';

  @override
  String get matchingrulesEmptySelectDomain => '상단 드롭다운에서 도메인을 먼저 선택해 주세요.';

  @override
  String get emptySelectDomain => '상단 드롭다운에서 도메인을 먼저 선택해 주세요.';

  @override
  String get matchingrulesEmptyNoRulesDesc =>
      '우측 상단의 \'+ 규칙 추가\' 버튼을 클릭하여 중복 레코드 판별을 위한 새로운 매칭 규칙을 생성하세요.';

  @override
  String get emptyNoRulesDesc =>
      '우측 상단의 \'+ 규칙 추가\' 버튼을 클릭하여 중복 레코드 판별을 위한 새로운 매칭 규칙을 생성하세요.';

  @override
  String get matchingrulesEmptySelectDomainDesc =>
      '도메인을 선택하면 해당 도메인의 중복 레코드 판단 규칙 목록이 AG-Grid에 표시됩니다.';

  @override
  String get emptySelectDomainDesc =>
      '도메인을 선택하면 해당 도메인의 중복 레코드 판단 규칙 목록이 AG-Grid에 표시됩니다.';

  @override
  String get matchingrulesAddFirstRule => '첫 번째 매칭 규칙 추가하기';

  @override
  String get addFirstRule => '첫 번째 규칙 추가하기';

  @override
  String get matchingrulesCreateTitle => '새 매칭 규칙 추가';

  @override
  String get createTitle => '새 매칭 규칙 추가';

  @override
  String get matchingrulesEditTitle => '매칭 규칙 수정';

  @override
  String get editTitle => '매칭 규칙 수정';

  @override
  String get matchingrulesRuleName => '규칙명';

  @override
  String get ruleName => '규칙 명칭';

  @override
  String get matchingrulesRuleNamePlaceholder => '예: 이름 및 연락처 일치 규칙';

  @override
  String get ruleNamePlaceholder => '예: 이름 및 연락처 일치 규칙';

  @override
  String get matchingrulesMatchType => '매칭 방식';

  @override
  String get matchType => '매칭 방식';

  @override
  String get matchingrulesTargetFields => '대상 필드 다중 선택';

  @override
  String get targetFields => '대상 필드 다중 선택';

  @override
  String get matchingrulesTargetFieldsCsv => '대상 필드 키 (쉼표로 구분)';

  @override
  String get targetFieldsCsv => '대상 필드 키 (쉼표로 구분)';

  @override
  String get matchingrulesSimilarityThreshold => '유사도 임계값 (0.5 ~ 1.0)';

  @override
  String get similarityThreshold => '유사도 임계값 (0.5 ~ 1.0)';

  @override
  String get matchingrulesIsActive => '규칙 활성화 여부';

  @override
  String get isActive => '사용 여부';

  @override
  String get matchingrulesCancel => '취소';

  @override
  String get matchingrulesSave => '저장';

  @override
  String get matchingrulesActive => '활성';

  @override
  String get matchingrulesInactive => '비활성';

  @override
  String get inactive => '비활성';

  @override
  String get matchingrulesSaveSuccess => '매칭 규칙이 성공적으로 저장되었습니다.';

  @override
  String get matchingrulesSaveFailed => '매칭 규칙 저장에 실패했습니다.';

  @override
  String matchingrulesDeleteConfirm(Object name) {
    return '\'$name\' 매칭 규칙을 삭제하시겠습니까?';
  }

  @override
  String deleteConfirm(Object name) {
    return '\'$name\' 매칭 규칙을 삭제하시겠습니까?';
  }

  @override
  String get matchingrulesDeleteSuccess => '매칭 규칙이 삭제되었습니다.';

  @override
  String get matchingrulesDeleteFailed => '매칭 규칙 삭제에 실패했습니다.';

  @override
  String get labelEmail => '이메일 주소';

  @override
  String get placeholderEmail => '예: user@company.com';

  @override
  String get emailDomain => '이메일 도메인';

  @override
  String get placeholderEmailDomain => '예: company.com';

  @override
  String get orgEmailDomainDesc => '조직의 기본 이메일 도메인을 설정합니다.';

  @override
  String get ruleTypeBusinessNoChecksum => '사업자등록번호 체크섬 검증 (10자리)';

  @override
  String get ruleTypeCorporateNoChecksum => '법인등록번호 체크섬 검증 (13자리)';

  @override
  String get channelHealth => '채널 헬스';

  @override
  String get channelMetrics => '실시간 처리량 & DLQ 모니터링';

  @override
  String get healthHealthy => '정상 (HEALTHY)';

  @override
  String get healthDegraded => '주의 (DEGRADED)';

  @override
  String get healthUnhealthy => '장애 (UNHEALTHY)';

  @override
  String get pingTest => 'Ping 테스트';

  @override
  String get pingTesting => 'Ping 확인 중...';

  @override
  String get avgLatency => '평균 응답속도';

  @override
  String get timeSlot => '시간대';

  @override
  String get smartMapping => '스마트 자동 매핑 추천';

  @override
  String get smartMappingDesc =>
      '외부 샘플 페이로드의 키를 분석하여 도메인 필드와 한글 자모 퍼지 유사도로 자동 매핑합니다.';

  @override
  String get samplePayload => '샘플 페이로드 (JSON)';

  @override
  String get samplePayloadPlaceholder => 'JSON 형식의 외부 샘플 페이로드를 입력하세요.';

  @override
  String get recommendMapping => '자동 매핑 추천 실행';

  @override
  String get applyRecommendations => '추천 매핑 적용';

  @override
  String get sourceField => '외부 필드';

  @override
  String get targetField => '매핑할 시스템 필드';

  @override
  String get confidenceScore => '신뢰도';

  @override
  String get matchReason => '매칭 근거';

  @override
  String get dlqHub => '연계 채널 실패 큐(DLQ) 관리 & 재시도 허브';

  @override
  String get dlqHubDesc =>
      '연계 채널 통신 중 실패/Dead-Letter 상태로 전환된 전송 로그를 조회하고 지능형으로 일괄 재시도합니다.';

  @override
  String get retrySelected => '선택 항목 재시도';

  @override
  String get retryAll => '전체 실패 건 일괄 재시도';

  @override
  String get retryCount => '재시도 횟수';

  @override
  String get errorMessage => '에러 메시지';

  @override
  String get noDlqItems => '실패 큐(DLQ)에 대기 중인 오류 로그가 없습니다.';

  @override
  String get webhookHub => '실시간 이벤트 웹훅 디스패처';

  @override
  String get webhookHubDesc =>
      '마스터 레코드 생성/수정/승인 이벤트를 외부 슬랙, 메신저 및 타 시스템으로 실시간 자동 발송합니다.';

  @override
  String get addWebhook => '웹훅 등록';

  @override
  String get targetUrl => '웹훅 수신 URL';

  @override
  String get subscribedEvents => '구독 이벤트';

  @override
  String get testWebhook => '웹훅 테스트';

  @override
  String get noWebhooks => '등록된 웹훅 구독이 없습니다.';

  @override
  String get dataRetention => '데이터 보존 정책 & GDPR 자동 파기 엔진';

  @override
  String get dataRetentionDesc =>
      '법적 보존 연한이 만료되었거나 파기 요청된 마스터 데이터를 안전하게 비식별화/영구 삭제 처리합니다.';

  @override
  String get retentionYears => '보존 연한 (년)';

  @override
  String get scanExpired => '만료 대상 데이터 스캔';

  @override
  String get purgeType => '파기 방식';

  @override
  String get purgeAnonymize => '안전 비식별화 (Soft Anonymize)';

  @override
  String get purgeHardDelete => '영구 물리 삭제 (Hard Delete)';

  @override
  String get executePurge => '안전 파기 실행';

  @override
  String get expiredCount => '만료 탐지 건수';

  @override
  String get anomalyDetection => '비인가 접근 및 대량 유출 실시간 탐지기';

  @override
  String get anomalyDetectionDesc =>
      '단시간 대량 다운로드 또는 비인가 IP/시간대의 이상 접근 행위를 제로트러스트 기반으로 실시간 탐지합니다.';

  @override
  String get threatScore => '보안 위협 지수';

  @override
  String get activeThreats => '활성 위협 건수';

  @override
  String get threatLevel => '위협 수준';

  @override
  String get blockActor => '접근 즉시 차단';

  @override
  String get blockedBadge => '차단 완료';

  @override
  String get syncPipeline => '도메인 간 데이터 동기화 파이프라인 스케줄러';

  @override
  String get syncPipelineDesc =>
      '복수 도메인 간의 주기적 데이터 동기화 규칙과 cron 스케줄을 설정하고 실행 상태를 오케스트레이션합니다.';

  @override
  String get pipelineName => '파이프라인 명칭';

  @override
  String get cronSchedule => '스케줄 주기';

  @override
  String get lastSynced => '최근 동기화';

  @override
  String get triggerPipeline => '파이프라인 즉시 실행';

  @override
  String get noPipelines => '등록된 동기화 파이프라인이 없습니다.';

  @override
  String get apiKeyMgmt => '연계 API 키 수명주기 & 세부 스코프 관리기';

  @override
  String get apiKeyMgmtDesc =>
      '외부 연계 채널별 API Key 발급, 유효기간, IP 화이트리스트, 도메인별 세부 권한 스코프를 설정 및 폐기 관리합니다.';

  @override
  String get issueApiKey => 'API Key 신규 발급';

  @override
  String get keyName => '키 명칭';

  @override
  String get validDays => '유효 기간(일)';

  @override
  String get allowedIps => '허용 IP 대역 (CIDR)';

  @override
  String get permissionScopes => '권한 스코프';

  @override
  String get revokeKey => '키 폐기';

  @override
  String get confirmRevokeKey =>
      '이 API Key를 즉시 영구 폐기하시겠습니까? 연계 채널의 호출이 즉시 차단됩니다.';

  @override
  String get systemDiagnostics => '글로벌 시스템 종합 헬스체인 진단기';

  @override
  String get systemDiagnosticsDesc =>
      'DB, 캐시, 메시지 큐, 스토리지 등 MDM 백본 인프라 전반의 실시간 레이턴시와 상태를 통합 진단합니다.';

  @override
  String get componentName => '컴포넌트 명칭';

  @override
  String get latency => '응답 지연(ms)';

  @override
  String get averageLatency => '평균 응답속도';

  @override
  String get overallStatus => '종합 상태';

  @override
  String get runDiagnostics => '진단 새로고침';

  @override
  String get workspaceWidgets => '거버넌스 대시보드 위젯 커스터마이저';

  @override
  String get workspaceWidgetsDesc =>
      '사용자의 역할과 업무 스타일에 맞게 DQ 품질, 결재 대기, 보안 위협, DLQ 모니터 위젯을 개인화 배치합니다.';

  @override
  String get widgetGallery => '위젯 갤러리';

  @override
  String get widgetEnabled => '활성화됨';

  @override
  String get widgetDisabled => '비활성화됨';

  @override
  String get saveLayout => '개인화 레이아웃 저장';

  @override
  String get coldStorage => '콜드 스토리지 아카이브';

  @override
  String get coldStorageDesc =>
      '전사 모든 도메인, 스키마, 결재선, 용어사전, 감사 원장 데이터를 단일 암호화 아카이브로 동결 백업하고 DR 무결성을 시뮬레이션합니다.';

  @override
  String get createArchive => '원클릭 동결 아카이브 생성';

  @override
  String get archiveName => '아카이브 명칭';

  @override
  String get checksumSha256 => '무결성 체크섬 (SHA-256)';

  @override
  String get compressionRatio => '압축 및 암호화';

  @override
  String get simulateDr => 'DR 복원 시뮬레이션';

  @override
  String get regulatoryCompliance => '규제 컴플라이언스 진단';

  @override
  String get regulatoryComplianceDesc =>
      'ISMS-P, 개인정보보호법, GDPR 등 정보보호 규제 통제항목별로 시스템의 암호화, 감사원장, 파기 체계를 자동 점검합니다.';

  @override
  String get complianceScore => '컴플라이언스 준수율';

  @override
  String get certificationReadiness => '인증 준비도';

  @override
  String get controlCode => '통제 항목';

  @override
  String get evidence => '구축 증적 (Evidence)';

  @override
  String get runAudit => '자체 감사 실행';

  @override
  String get volumeRadar => '이상 트래픽 레이더';

  @override
  String get volumeRadarDesc =>
      '시간대별 레코드 변경 및 API 호출량을 Z-score 이상치 모델로 실시간 분석하여 비정상 급증(Spike)을 탐지합니다.';

  @override
  String get currentThroughput => '현재 처리량';

  @override
  String get baselineThroughput => '기준 처리량';

  @override
  String get volumeHistory => '시간대별 볼륨 추이';

  @override
  String get spikeAlert => '트래픽 급증 경보';

  @override
  String get normalTraffic => '트래픽 정상';

  @override
  String get governanceMaturity => '거버넌스 성숙도 진단';

  @override
  String get governanceMaturityDesc =>
      'DMM/CMMI 프레임워크를 기반으로 전사 데이터 품질 KPI 및 거버넌스 성숙도 레벨(Level 1~5)을 종합 진단합니다.';

  @override
  String get overallMaturityLevel => '종합 거버넌스 성숙도 레벨';

  @override
  String get maturityDimensions => '5대 성숙도 차원';

  @override
  String get completenessKpi => '완전성 (Completeness)';

  @override
  String get timelinessKpi => '적시성 (Timeliness)';

  @override
  String get consistencyKpi => '일관성 (Consistency)';

  @override
  String get validityKpi => '유효성 (Validity)';

  @override
  String get multiTenant => '멀티 테넌트 라우터';

  @override
  String get multiTenantDesc =>
      '본사, 해외법인, 계열사별로 동일 도메인 내에서 행/열 단위 격리 정책 및 가상 데이터 라우팅을 관리합니다.';

  @override
  String get tenantName => '테넌트 / 법인명';

  @override
  String get partitionType => '파티셔닝 유형';

  @override
  String get routingExpression => '라우팅 필터 표현식';

  @override
  String get targetDomains => '적용 도메인 수';

  @override
  String get dataSla => 'SLA 트래커';

  @override
  String get dataSlaDesc =>
      '각 도메인 및 연계 채널 간 데이터 제공 응답속도, 가용성, 품질 보증 기준(SLA)을 실시간 추적하고 계약 위반을 방지합니다.';

  @override
  String get slaContractName => 'SLA 협약명 / 대상';

  @override
  String get latencySla => '응답 지연시간 (Latency)';

  @override
  String get availabilitySla => '가용성 (Availability)';

  @override
  String get qualitySla => '품질 준수율 (DQ)';

  @override
  String get meetingSla => 'SLA 정상 준수';

  @override
  String get masterOrchestrator => '오케스트레이터 허브';

  @override
  String get masterOrchestratorDesc =>
      '플랫폼 내 구축된 50대 전사 마스터 데이터 거버넌스 핵심 기능의 가동 상태 및 헬스맵을 단일 관제 화면에서 오케스트레이션합니다.';

  @override
  String get totalFeaturesCount => '총 거버넌스 기능 수';

  @override
  String get healthyFeaturesCount => '정상 가동 기능';

  @override
  String get featureNo => '기능 번호';

  @override
  String get featureName => '기능 명칭';

  @override
  String get featureCategory => '카테고리';

  @override
  String get pipelineSelfHealing => '파이프라인 자율 복구';

  @override
  String get pipelineSelfHealingDesc =>
      '연계 채널의 스키마 불일치, 네트워크 지연, 포맷 오류를 AI 에이전트가 진단하고 자율 복구 및 경로 우회를 수행합니다.';

  @override
  String get healingActionId => '복구 액션 ID';

  @override
  String get diagnosedCause => 'AI 진단 원인';

  @override
  String get healingStrategy => '복구 전략';

  @override
  String get recoveredRecords => '구제 레코드 수';

  @override
  String get triggerHealing => '자율 복구 수동 실행';

  @override
  String get healingTriggered => '자율 복구 프로세스가 성공적으로 트리거되었습니다.';

  @override
  String get freshnessHeatmap => '데이터 신선도 히트맵';

  @override
  String get freshnessHeatmapDesc =>
      '각 도메인의 최종 갱신 시각과 실시간 지연 시간을 모니터링하여 데이터 노후화(Stale)를 방지합니다.';

  @override
  String get freshnessScore => '신선도 점수';

  @override
  String get lastUpdatedTime => '최종 갱신 시각';

  @override
  String get delayMinutes => '지연 시간';

  @override
  String get freshStatus => '초신선 (Fresh)';

  @override
  String get multiRegionConflict => '멀티 리전 충돌 해소';

  @override
  String get multiRegionConflictDesc =>
      '서울, 북미, 유럽 등 다중 리전 간 동시 수정 충돌(Conflict)을 벡터 클록 및 비즈니스 우선순위로 자율 해소합니다.';

  @override
  String get regionPair => '충돌 리전';

  @override
  String get resolutionStrategy => '해소 전략';

  @override
  String get resolvedValue => '자율 해소 확정값';

  @override
  String get autoResolved => '자율 해소 완료';

  @override
  String get governanceCopilot => '거버넌스 AI Copilot';

  @override
  String get governanceCopilotDesc =>
      '전사 품질 점수, SLA 계약 위반 위험, 멀티리전 동기화 및 장애 자율 복구 등 거버넌스 전반을 대화형으로 질의하고 즉각 조치합니다.';

  @override
  String get copilotPlaceholder =>
      '거버넌스에 대해 무엇이든 질문하세요 (예: 전사 품질 요약해줘, SLA 상태는?)';

  @override
  String get copilotSend => '질문 전송';

  @override
  String get quickQuestions => '추천 질문';

  @override
  String get alldone => '모든 결재/합의가 완료되었습니다.';

  @override
  String get noparsable => '파싱 가능한 데이터가 없습니다.';

  @override
  String get addcomment => '코멘트 추가';

  @override
  String get actionTitle => '결재 처리';

  @override
  String bulkApprove(Object count) {
    return '일괄 승인';
  }

  @override
  String bulkReject(Object count) {
    return '일괄 반려';
  }

  @override
  String bulkApproveConfirm(Object count) {
    return '선택한 $count건을 일괄 승인하시겠습니까?';
  }

  @override
  String bulkRejectConfirm(Object count) {
    return '선택한 $count건을 일괄 반려하시겠습니까?';
  }

  @override
  String get bulkApproveLoading => '일괄 승인 처리 중...';

  @override
  String get bulkRejectLoading => '일괄 반려 처리 중...';

  @override
  String processing(Object percent) {
    return '데이터 처리 중... $percent%';
  }

  @override
  String get approvalLine => '결재선';

  @override
  String get targetType => '대상 유형';

  @override
  String get stepType => '스텝 유형';

  @override
  String get action => '변경 유형';

  @override
  String get statusDraft => '임시 저장 (Draft)';

  @override
  String get statusPending => '검토 대기';

  @override
  String get statusWaiting => '대기';

  @override
  String get recordCreate => '마스터 레코드 신규 등록';

  @override
  String get recordUpdate => '마스터 레코드 수정';

  @override
  String get recordDelete => '마스터 레코드 삭제';

  @override
  String get domainRecordCreate => '도메인 레코드 등록';

  @override
  String get targetTypeMEMO => '메모 결재';

  @override
  String get targetTypeMemo => '메모 결재';

  @override
  String get memoApproval => '메모 결재';

  @override
  String get noComment => '의견 없음';

  @override
  String get cancelReason => '상신 취소 사유';

  @override
  String get rejectionReason => '반려 사유';

  @override
  String get cancellationNotice => '이 결재는 기안자에 의해 상신 취소되었습니다.';

  @override
  String get statusCancelled => '상신 취소';

  @override
  String get noReasonSpecified => '입력된 사유가 없습니다.';

  @override
  String get observers => '통보 대상자';

  @override
  String get stepscheduled => '예정';

  @override
  String get coldomain => '도메인';

  @override
  String get colclassification => '분류 체계';

  @override
  String get colidattr => '식별자';

  @override
  String get colnameattr => '명칭';

  @override
  String get colsummary => '요약 정보';

  @override
  String get actionApprove => '승인';

  @override
  String get actionReject => '반려';

  @override
  String actionProcessing(Object action) {
    return '$action 처리 중입니다...';
  }

  @override
  String actionSuccess(Object action) {
    return '결재가 성공적으로 $action 되었습니다.';
  }

  @override
  String get review => '심사하기';

  @override
  String get created => '생성 일시';

  @override
  String get close => '닫기';

  @override
  String get general => '일반';

  @override
  String get fields => '기본 필드';

  @override
  String get summary => '요약';

  @override
  String get details => '상세 정보';

  @override
  String get id => '식별 ID';

  @override
  String get targetTypeApprovalRequest => '결재 요청';

  @override
  String get confirmAndSubmit => '영향도 확인 및 결재 상신';

  @override
  String get confirmSafetyApply => '안전 확인 및 변경 사항 적용';

  @override
  String get confirmSafetySubmit => '안전 확인 및 결재 상신';

  @override
  String get approvalSubmittedTitle => '결재 상신 완료';

  @override
  String get confirmRiskApply => '경고 확인 및 변경 사항 최종 적용';

  @override
  String get confirmRiskDesc =>
      '위험도 및 경고 사항을 모두 확인하였으며, 스키마 변경을 최종 승인 및 적용합니다.';

  @override
  String get addApprovalStep => '+ 승인 스텝 추가';

  @override
  String get approval => '결재';

  @override
  String get approvallinestatus => '결재선 현황';

  @override
  String get approvallinesummary => '결재라인 (요약):';

  @override
  String get approvalHistory => '결재 내역';

  @override
  String get approvalHistoryBtn => '결재 내역';

  @override
  String get approvalHistoryDetail => '결재 내역 상세';

  @override
  String get approvalInProgress => '결재 진행중';

  @override
  String get approvalLineStatus => '결재선 현황';

  @override
  String get approvalLineSummary => '결재라인 (요약)';

  @override
  String get approvalLineTitle => '다단계 결재 승인선 (Approval Line)';

  @override
  String get approvalMonitor => '결재 관리 대시보드';

  @override
  String get approvalMonitoring => '결재 모니터링';

  @override
  String get approvalProgressStep => '결재 진행 단계';

  @override
  String get approvalReview => '결재 심사';

  @override
  String approvalStatsSummary(Object approved, Object rejected) {
    return '승인 $approved건 / 반려 $rejected건';
  }

  @override
  String get approvalStepsCol => '승인 단계';

  @override
  String get approvalSuccessRate => '결재 승인율';

  @override
  String get approvalTrendTitle => '최근 7일 결재 요청 및 처리 추이';

  @override
  String get approvals => '결재 승인';

  @override
  String get approvalsTitle => '결재 & 승인 관리';

  @override
  String get approve => '승인';

  @override
  String get approver => '결재자';

  @override
  String get btnSubmit => '상신';

  @override
  String get cancelRequest => '신청 취소';

  @override
  String confirmBatchApprove(Object count) {
    return '선택한 $count건을 일괄 승인하시겠습니까?';
  }

  @override
  String confirmBatchReject(Object count) {
    return '선택한 $count건을 일괄 반려하시겠습니까?';
  }

  @override
  String get confirmDelete => '정말 삭제하시겠습니까?';

  @override
  String deleteWorkflowConfirm(Object name) {
    return '정말로 \'$name\' 워크플로우 서식을 삭제하시겠습니까?';
  }

  @override
  String get editDisabledApproval => '⚠️ 이 레코드는 현재 변경 결재가 진행 중이므로 수정할 수 없습니다.';

  @override
  String get finalapproval => '최종 결재';

  @override
  String get installBtnSubmit => '시스템 설치 및 계정 등록 완료';

  @override
  String get labelConfirmPassword => '비밀번호 확인';

  @override
  String get mysubmitted => '내가 상신한 결재 내역';

  @override
  String get mySubmittedRequests => '내가 상신한 결재 내역';

  @override
  String get noapprovalline => '결재라인이 없습니다.';

  @override
  String get norequests => '현재 대기 중인 요청이 없습니다.';

  @override
  String get nosubmitted => '상신한 결재가 없습니다.';

  @override
  String get noApprovalLine => '결재선 없음';

  @override
  String get noApprovalSteps => '등록된 승인 스텝이 없습니다. (자동 승인 처리됨)';

  @override
  String get noPendingRequests => '대기 중인 요청이 없습니다.';

  @override
  String get noRequestsSubmittedYet => '상신한 결재가 없습니다.';

  @override
  String get pendingapprovals => '승인 대기 결재 목록';

  @override
  String get pendingApproval => '결재 진행중';

  @override
  String get pendingApprovalAssignee => '현재 대기중인 결재자:';

  @override
  String get pendingApprovalNotice => '⚠️ 이 레코드는 현재 변경 결재가 진행 중이므로 수정할 수 없습니다.';

  @override
  String get pendingApprovals => '결재 대기 중';

  @override
  String get pendingRequests => '신청중인 도메인';

  @override
  String get placeholderConfirmPassword => '비밀번호를 다시 입력하세요';

  @override
  String get reject => '반려';

  @override
  String get requestDate => '상신 일시';

  @override
  String get requestInfo => '상신 정보';

  @override
  String get requesteddata => '요청 데이터';

  @override
  String get requestedAccessTo => '접근 권한 요청:';

  @override
  String get requester => '기안자';

  @override
  String get selectApproval => '선택 승인';

  @override
  String get selectRejection => '선택 반려';

  @override
  String get statusApproved => '승인';

  @override
  String get statusRejected => '거절됨';

  @override
  String get stepapproved => '승인됨';

  @override
  String get steprejected => '반려됨';

  @override
  String get stepApproval => '결재';

  @override
  String get stepTypeApproval => '결재';

  @override
  String get submitRequest => '신청하기';

  @override
  String get typeapproval => '결재';

  @override
  String get viewApprovalHistory => '결재 내역 보기';

  @override
  String get approvalInbox => '결재함';

  @override
  String pendingCount(Object count) {
    return '검토 대기: $count건';
  }

  @override
  String itemCount(Object count) {
    return '$count건';
  }

  @override
  String get consensus => '합의';

  @override
  String get draft => '기안';

  @override
  String get draftCompleted => '상신완료';

  @override
  String get processed => '처리됨';

  @override
  String get observersList => '참조자 목록';

  @override
  String get approvalDelegation => '결재 위임 관리';

  @override
  String get approvalDelegationDesc =>
      '휴가, 출장 등으로 부재 시 결재 업무를 대신 처리할 대결자를 지정합니다.';

  @override
  String get delegatedByMe => '내가 위임한 내역';

  @override
  String get delegatedToMe => '나에게 위임된 내역';

  @override
  String get delegatee => '대결자 (위임 대상)';

  @override
  String get delegator => '위임자';

  @override
  String get delegationPeriod => '위임 기간';

  @override
  String get delegationReason => '위임 사유';

  @override
  String get addDelegation => '결재 위임 등록';

  @override
  String get revokeDelegation => '위임 해제';

  @override
  String get delegationActive => '위임 활성';

  @override
  String get delegationExpired => '위임 만료';

  @override
  String proxyBadge(Object name) {
    return '대결 (위임: $name)';
  }

  @override
  String get delegationSuccess => '결재 위임이 성공적으로 등록되었습니다.';

  @override
  String get revokeSuccess => '결재 위임이 해제되었습니다.';

  @override
  String slaDue(Object time) {
    return 'SLA 처리 기한: $time';
  }

  @override
  String get slaExpired => 'SLA 기한 만료됨';

  @override
  String slaEscalatedBadge(Object name) {
    return '에스컬레이션 이관됨 (원 결재자: $name)';
  }

  @override
  String get scanEscalation => 'SLA 지연 건 에스컬레이션 즉시 실행';

  @override
  String escalationSuccess(Object count) {
    return '총 $count건의 지연 결재가 관리자에게 자동 에스컬레이션 이관되었습니다.';
  }

  @override
  String get approvalSandbox => '결재 데이터 사전 시뮬레이션 샌드박스';

  @override
  String get approvalSandboxDesc =>
      '결재 승인 시 마스터 데이터에 실제로 어떻게 반영되는지 변경 전/후를 가상으로 시뮬레이션하여 비교합니다.';

  @override
  String get previewDiff => '사전 시뮬레이션';

  @override
  String get simulatedResult => '승인 후 반영될 가상 데이터';

  @override
  String get dynamicRouting => '조건부 동적 결재 라우팅 & 워크플로우 템플릿';

  @override
  String get dynamicRoutingDesc =>
      '데이터의 중요도, 민감도, 특정 필드값에 따라 결재 승인 단계를 규칙 기반으로 자동 분기 및 배정합니다.';

  @override
  String get templateName => '템플릿 명칭';

  @override
  String get conditionField => '조건 필드';

  @override
  String get conditionOperator => '조건 연산자';

  @override
  String get conditionValue => '조건 값';

  @override
  String get approvalSteps => '결재 단계';

  @override
  String get addTemplate => '결재선 템플릿 추가';

  @override
  String get rejectionAnalytics => '결재 반려 사유 지능형 분석 & 재신청 가이드';

  @override
  String get rejectionAnalyticsDesc =>
      '과거 반려 이력의 사유를 텍스트 분석하여 주요 반려 원인 통계 및 재신청 체크리스트 가이드를 제공합니다.';

  @override
  String get rejectionCauseDistribution => '주요 반려 원인 분포';

  @override
  String get resubmitChecklist => '재신청 전 필수 확인 체크리스트';

  @override
  String get actionGuide => '개선 권장사항';

  @override
  String get loginCount => '로그인 횟수';

  @override
  String get btnLogin => '로그인';

  @override
  String get btnRegister => '회원가입';

  @override
  String get login => '로그인';

  @override
  String get loginFailed => '로그인 실패. 자격 증명을 확인하세요.';

  @override
  String get loginTitleSub => '안전한 데이터 분류 및 거버넌스 플랫폼';

  @override
  String get logout => '로그아웃';

  @override
  String get newWorkflowRegister => '+ 신규 워크플로우 등록';

  @override
  String get noOrgsRegistered => '등록된 조직이 없습니다.';

  @override
  String get registerRoleBtn => '역할 등록';

  @override
  String get registeredDomains => '등록된 마스터 도메인';

  @override
  String get tabLogin => '로그인';

  @override
  String get tabRegister => '회원가입';

  @override
  String get userLoginLogs => '사용자 로그인 로그';

  @override
  String get forcePasswordChange => '비밀번호 강제 변경';

  @override
  String get forcePasswordChangeDesc =>
      '보안을 위해 초기 비밀번호를 변경해야 합니다. 새로운 비밀번호를 설정해 주세요.';

  @override
  String get createUser => '사용자 등록';

  @override
  String get userCreated => '사용자 등록 완료';

  @override
  String get oldPassword => '현재 비밀번호';

  @override
  String get newPassword => '새 비밀번호';

  @override
  String get confirmNewPassword => '새 비밀번호 확인';

  @override
  String get changePassword => '비밀번호 변경';

  @override
  String get tempPasswordIssued => '임시 비밀번호가 발급되었습니다';

  @override
  String get authLoginErrorMessage => '인증에 실패했습니다. 계정 정보를 다시 확인해주세요.';

  @override
  String get requiredField => '필수 항목입니다.';

  @override
  String get min8Chars => '8자 이상 입력해주세요.';

  @override
  String get passwordsDoNotMatch => '비밀번호가 일치하지 않습니다.';

  @override
  String get fillAllFields => '모든 필드를 입력해주세요.';

  @override
  String get passwordChangeFailed => '비밀번호 변경에 실패했습니다.';

  @override
  String get accessCount => '접근 횟수';

  @override
  String get dispatchedTO => '외부 전파';

  @override
  String get decryptionTrendLast7Days => '복호화 추이 (최근 7일)';

  @override
  String get evolvedTO => '통합 갱신';

  @override
  String get modifiedTO => '정보 변경';

  @override
  String get typeRatios => '조회 유형 비율';

  @override
  String get recordItem => '데이터 레코드';

  @override
  String get imageFile => '이미지 파일';

  @override
  String get attachment => 'Attachment';

  @override
  String get richText => '서식 텍스트';

  @override
  String get previewImage => '이미지 미리보기';

  @override
  String get downloadFile => '완료된 파일 다운로드';

  @override
  String imageCount(Object count) {
    return '$count개의 이미지';
  }

  @override
  String fileCount(Object count) {
    return '파일 $count건';
  }

  @override
  String get accessReason => '접근 사유';

  @override
  String get accessReasonPlaceholder => '예: 업무 처리, 고객 요청 등';

  @override
  String get accessReasonRequired => '접근 사유를 입력해 주세요.';

  @override
  String get actionRequired => '⚠️ 즉시 조치 필요';

  @override
  String get actionTypeAll => '전체';

  @override
  String get actionTypeCol => '작업 구분';

  @override
  String get actionTypeCreate => '신규 등록 (CREATE)';

  @override
  String get actionTypeCreateShort => '신규 등록';

  @override
  String get actionTypeDelete => '삭제 / 폐기 (DELETE)';

  @override
  String get actionTypeDeleteShort => '삭제/폐기';

  @override
  String get actionTypeMerge => '골든레코드 병합 (MERGE)';

  @override
  String get actionTypeMergeShort => '레코드 병합';

  @override
  String get actionTypeUpdate => '정보 변경 (UPDATE)';

  @override
  String get actionTypeUpdateShort => '정보 변경';

  @override
  String get actions => '트리 관리 / 관리';

  @override
  String get actionsCol => '관리';

  @override
  String get activeStatus => '활성';

  @override
  String get approvalprogress => '결재 진행 단계';

  @override
  String get propertyFieldName => '속성 / 필드명';

  @override
  String get previousValue => '변경 전 (Previous Value)';

  @override
  String get newValue => '변경 후 (New Value)';

  @override
  String get addDepartment => '부서 등록';

  @override
  String get addFilter => '필터 추가';

  @override
  String get addMenu => '신규 메뉴 등록';

  @override
  String get addNewGroupBtn => '신규 그룹 추가';

  @override
  String get addNewMember => '신규 구성원 추가';

  @override
  String get addNewPermGroupTitle => '신규 권한 그룹 생성';

  @override
  String get addOption => '옵션 추가';

  @override
  String get addPermBtn => '권한 추가';

  @override
  String get addPermToGroupTitle => '그룹 내 신규 권한 추가';

  @override
  String get addRootMenu => '+ 최상위 메뉴 추가';

  @override
  String get addRow => '+ 행 추가';

  @override
  String get affectedChannels => '영향받는 연계 채널';

  @override
  String get afterChange => '변경 후 (New Value)';

  @override
  String get agGridUnifiedList => 'AG-Grid 통합 목록';

  @override
  String get allTasksCleared => '✅ 모든 처리 완료';

  @override
  String get approvalDetailTitle => '결재 내역 상세';

  @override
  String get approvalMonitorTitle => '결재 관리 모니터링';

  @override
  String get assigned => '소속됨';

  @override
  String get assignedAt => '조직 배치 일시';

  @override
  String get assignedMembersList => '소속 구성원 목록';

  @override
  String get auditChangeType => '변경 유형';

  @override
  String get auditChangedBy => '변경자';

  @override
  String get auditNewData => '변경 후 데이터';

  @override
  String get auditNoHistory => '이력 내역이 없습니다.';

  @override
  String get auditPreviousData => '변경 전 데이터';

  @override
  String get auditTrail => '변경 이력 (Audit Trail)';

  @override
  String get axisActions => '트리 관리 / 관리';

  @override
  String get axisAddAxis => '분류 축 추가';

  @override
  String get addAxis => '분류 축 추가';

  @override
  String get axisAddChildNode => '+ 하위노드';

  @override
  String get addChildNode => '+ 하위노드';

  @override
  String get axisAddRootNode => '루트 노드 추가';

  @override
  String get addRootNode => '루트 노드 추가';

  @override
  String get axisAssignModalTitle => '레코드 보조 분류 노드 할당';

  @override
  String get assignModalTitle => '레코드 보조 분류 노드 할당';

  @override
  String get axisAssignSecondaryNodes => '보조 노드 할당 / 수정';

  @override
  String get assignSecondaryNodes => '보조 노드 할당 / 수정';

  @override
  String get axisAxisAdded => '새 분류 축이 추가되었습니다.';

  @override
  String get axisAdded => '새 분류 축이 추가되었습니다.';

  @override
  String get axisAxisCodeLabel => '축 코드 (Axis Code)';

  @override
  String get axisCodeLabel => '축 코드 (Axis Code)';

  @override
  String get axisAxisDeleted => '분류 축이 삭제되었습니다.';

  @override
  String get axisDeleted => '분류 축이 삭제되었습니다.';

  @override
  String get axisAxisLabel => '축';

  @override
  String get axisLabel => '축';

  @override
  String get axisAxisNameLabel => '축 이름 (Axis Name)';

  @override
  String get axisNameLabel => '축 이름 (Axis Name)';

  @override
  String get axisAxisUpdated => '분류 축이 수정되었습니다.';

  @override
  String get axisUpdated => '분류 축이 수정되었습니다.';

  @override
  String get axisCode => '축 코드';

  @override
  String get code => '축 코드';

  @override
  String get axisCodeBadge => '코드';

  @override
  String get codeBadge => '코드';

  @override
  String get axisDeleteAxisConfirm => '축을 삭제하시겠습니까? 축에 속한 계층 트리도 모두 삭제됩니다.';

  @override
  String get deleteAxisConfirm => '축을 삭제하시겠습니까? 축에 속한 계층 트리도 모두 삭제됩니다.';

  @override
  String get axisDeleteNode => '삭제';

  @override
  String get deleteNode => '노드 삭제';

  @override
  String get axisDeleteNodeConfirm => '노드를 삭제하시겠습니까?';

  @override
  String get deleteNodeConfirm => '노드를 삭제하시겠습니까?';

  @override
  String get axisDescription => '설명';

  @override
  String get description =>
      '중복 레코드 병합 시 골든 레코드를 생성하기 위한 필드별 생존 우선순위 및 충돌 해결 전략을 설정합니다.';

  @override
  String get axisEditAxis => '분류 축 수정';

  @override
  String get editAxis => '분류 축 수정';

  @override
  String get axisEditNode => '수정';

  @override
  String get editNode => '노드 수정';

  @override
  String get axisEnterCodeName => '코드와 축 이름을 입력해 주세요.';

  @override
  String get enterCodeName => '코드와 축 이름을 입력해 주세요.';

  @override
  String get axisInvalidDomain => '도메인 정보가 올바르지 않습니다.';

  @override
  String get invalidDomain => '도메인 정보가 올바르지 않습니다.';

  @override
  String get axisLoadingTree => '트리 노드 정보를 불러오는 중입니다...';

  @override
  String get loadingTree => '트리 노드 정보를 불러오는 중입니다...';

  @override
  String get axisManagementDesc =>
      '주 분류체계 외에 조직, 지역, 산업군 등 다차원 독립 분류 축을 등록하고 축별 트리를 구성합니다.';

  @override
  String get managementDesc =>
      '주 분류체계 외에 조직, 지역, 산업군 등 다차원 독립 분류 축을 등록하고 축별 트리를 구성합니다.';

  @override
  String get axisManagementTitle => '다축 분류체계 축 관리 (Classification Axes)';

  @override
  String get managementTitle => '다축 분류체계 축 관리 (Classification Axes)';

  @override
  String get axisName => '축 이름';

  @override
  String get axisNoAxes => '등록된 보조 분류 축이 없습니다.';

  @override
  String get noAxes => '등록된 보조 분류 축이 없습니다.';

  @override
  String get axisNoNodesDesc =>
      '축에 등록된 분류 노드가 없습니다. 상단의 [루트 노드 추가] 버튼을 눌러 독립 트리를 구성하세요.';

  @override
  String get noNodesDesc =>
      '축에 등록된 분류 노드가 없습니다. 상단의 [루트 노드 추가] 버튼을 눌러 독립 트리를 구성하세요.';

  @override
  String get axisNoNodesRegistered => '등록된 노드 없음';

  @override
  String get noNodesRegistered => '등록된 노드 없음';

  @override
  String get axisNoSecondaryNodes => '할당된 보조 분류 노드가 없습니다.';

  @override
  String get noSecondaryNodes => '할당된 보조 분류 노드가 없습니다.';

  @override
  String get axisNodeAdded => '새 분류 노드가 추가되었습니다.';

  @override
  String get nodeAdded => '새 분류 노드가 추가되었습니다.';

  @override
  String get axisNodeDeleted => '분류 노드가 삭제되었습니다.';

  @override
  String get nodeDeleted => '분류 노드가 삭제되었습니다.';

  @override
  String get axisNodeIcon => 'Node Icon';

  @override
  String get nodeIcon => '노드 아이콘';

  @override
  String get axisNodeManagementTitle => '축 전용 분류체계 트리 노드 관리';

  @override
  String get nodeManagementTitle => '축 전용 분류체계 트리 노드 관리';

  @override
  String get axisNodeNameEn => '노드 이름 (EN)';

  @override
  String get nodeNameEn => '노드명 (EN)';

  @override
  String get axisNodeNameKo => '노드 이름 (KO)';

  @override
  String get nodeNameKo => '노드명 (KO)';

  @override
  String get axisNodeUpdated => '분류 노드가 수정되었습니다.';

  @override
  String get nodeUpdated => '분류 노드가 수정되었습니다.';

  @override
  String get axisPrimaryTree => '주 분류체계';

  @override
  String get primaryTree => '주 분류체계';

  @override
  String get axisRefresh => '새로고침';

  @override
  String get axisSaveSecondaryNodes => '보조 노드 저장';

  @override
  String get saveSecondaryNodes => '보조 노드 저장';

  @override
  String get axisSecondaryMappingDesc =>
      '주 분류 노드 이외에 레코드가 속한 보조 분류 노드 할당 정보를 확인 및 편집합니다.';

  @override
  String get secondaryMappingDesc =>
      '주 분류 노드 이외에 레코드가 속한 보조 분류 노드 할당 정보를 확인 및 편집합니다.';

  @override
  String get axisSecondaryMappingTitle => '레코드 보조 분류 노드 매핑';

  @override
  String get secondaryMappingTitle => '레코드 보조 분류 노드 매핑';

  @override
  String get axisSecondaryNodesSaveFailed => '보조 분류 노드 저장 실패';

  @override
  String get secondaryNodesSaveFailed => '보조 분류 노드 저장 실패';

  @override
  String get axisSecondaryNodesSaved => '보조 분류 노드가 성공적으로 할당되었습니다.';

  @override
  String get secondaryNodesSaved => '보조 분류 노드가 성공적으로 할당되었습니다.';

  @override
  String get axisSelectAxis => '분류 축 선택';

  @override
  String get selectAxis => '분류 축 선택';

  @override
  String get axisSelectIcon => 'Select Icon';

  @override
  String get selectIcon => '아이콘 선택';

  @override
  String get axisSelectNodesForAxis => '축별 분류 노드 선택';

  @override
  String get selectNodesForAxis => '축별 분류 노드 선택';

  @override
  String get axisSelectNodesPlaceholder => '노드 선택';

  @override
  String get selectNodesPlaceholder => '노드 선택';

  @override
  String get axisSortOrder => '정렬순서';

  @override
  String get axisTreeManage => '트리 관리';

  @override
  String get treeManage => '트리 관리';

  @override
  String get bulkReclassify => '일괄 분류 변경';

  @override
  String bulkReclassifyDesc(Object count) {
    return '선택한 $count개 레코드의 분류 노드를 일괄 변경합니다.';
  }

  @override
  String get bulkReclassifySuccess => '일괄 분류 변경이 완료되었습니다.';

  @override
  String get bulkReclassifyFail => '일괄 분류 변경에 실패했습니다.';

  @override
  String get bulkReclassifyTargetNode => '대상 분류 노드 선택';

  @override
  String get businessRuleBuilder => '비즈니스 규칙 빌더';

  @override
  String selectedRecordTarget(Object code) {
    return '대상 레코드: $code';
  }

  @override
  String get tableView => '속성 비교 테이블';

  @override
  String get jsonView => 'JSON 원본';

  @override
  String get diffFieldName => '필드명';

  @override
  String get diffBefore => '변경 전';

  @override
  String get diffAfter => '변경 후';

  @override
  String get diffStatus => '변경 상태';

  @override
  String get noDiffData => '표시할 속성 데이터가 없습니다.';

  @override
  String get selectCdcEventGuide => '좌측 목록에서 CDC 이벤트를 선택하세요.';

  @override
  String get noCdcEventsInDomain => '이 도메인에 감지된 실시간 변경 데이터 이력이 없습니다.';

  @override
  String get exportDownloadSuccess => '내보내기 파일 다운로드가 완료되었습니다.';

  @override
  String get exportDownloadFailed => '파일 다운로드에 실패했습니다.';

  @override
  String emailDomainAutoHint(Object domain) {
    return '소속 조직 도메인($domain)으로 자동 완성됩니다.';
  }

  @override
  String get emailDefaultFallbackHint => '이메일 주소를 입력하세요.';

  @override
  String get backupMenuSeed => '현재 상태 백업';

  @override
  String get menuChildrenRoleUnionNotice =>
      '하위 메뉴가 존재하여 하위 메뉴의 필요 역할들이 자동으로 합집합(Union)되어 적용됩니다 (수동 변경 불가).';

  @override
  String get menuDumpSeedConfirm =>
      '현재 메뉴 설정(순서, 활성화 여부, 필요 권한 등)을 기본 시드 파일로 백업하시겠습니까? (이 기능은 관리자 전용입니다)';

  @override
  String get menuDumpSeedSuccess => '메뉴 시드 파일 백업이 완료되었습니다.';

  @override
  String get menuDumpSeedFailed => '메뉴 시드 파일 백업에 실패했습니다.';

  @override
  String get menuSyncSeedConfirm =>
      '시드 파일(default_menus.json)의 내용으로 현재 메뉴 체계를 동기화하시겠습니까? (없는 메뉴는 추가되고 기존 메뉴는 업데이트됩니다)';

  @override
  String get menuSyncSeedSuccess => '메뉴 동기화가 완료되었습니다.';

  @override
  String get menuSyncSeedFailed => '메뉴 동기화에 실패했습니다.';

  @override
  String get dqTargetField => '대상 필드 (Target Field)';

  @override
  String get dqSelectFieldPlaceholder => '트리에서 노드를 선택한 후 필드를 선택하세요.';

  @override
  String get dqSelectNodeFieldGuide => '노드 및 필드를 선택하여 데이터 품질 검칙 규칙을 관리하세요.';

  @override
  String get dqNoRulesFound => '등록된 DQ 검칙 규칙이 없습니다.';

  @override
  String get dqRuleSaveFailed => '규칙 저장에 실패했습니다.';

  @override
  String get dqRuleDeleteConfirm => '정말로 이 규칙을 삭제하시겠습니까?';

  @override
  String get dqRuleDeletedSuccess => '규칙이 삭제되었습니다.';

  @override
  String get dqRuleDeleteFailed => '규칙 삭제에 실패했습니다.';

  @override
  String userDeleteConfirm(Object username) {
    return '정말로 사용자 \'$username\'를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.';
  }

  @override
  String get userDeleteSuccess => '사용자가 성공적으로 삭제되었습니다.';

  @override
  String get userDeleteConflictError =>
      '해당 사용자가 생성한 레코드나 결재 이력 등 연결된 데이터가 존재하여 삭제할 수 없습니다.';

  @override
  String userDeleteFailed(Object error) {
    return '사용자 삭제 중 오류가 발생했습니다: $error';
  }

  @override
  String get userTempPasswordNotFound => '임시 비밀번호를 조회할 수 없습니다.';

  @override
  String get userTempPasswordQueryFailed =>
      '조회 실패: 해당 사용자의 임시 비밀번호가 존재하지 않거나 권한이 없습니다.';

  @override
  String dumpSeedFilesConfirmOrg(Object name) {
    return '현재 \'$name\' 조직에 셋팅된 모든 역할 상태를 시스템 기본값(Seed) JSON 파일로 백업/덮어쓰시겠습니까?\n(이 작업은 소스코드 디렉토리 내의 파일을 직접 수정합니다)';
  }

  @override
  String get dumpSeedFilesConfirmAll =>
      '현재 전체 DB에 저장된 모든 역할 상태를 시스템 기본값(Seed) JSON 파일로 백업/덮어쓰시겠습니까?\n(이 작업은 소스코드 디렉토리 내의 파일을 직접 수정합니다)';

  @override
  String get dumpSeedFilesSuccess => '기본 시드(Seed) 파일 갱신 완료';

  @override
  String get dumpSeedFilesFail => '기본 시드 파일 갱신 실패';

  @override
  String get dumpSeedFilesError => '기본 시드 파일 갱신 중 오류 발생';

  @override
  String get baselineBadge => '기준 레코드';

  @override
  String get basicInfo => '기본 정보';

  @override
  String get beforeChange => '변경 전 (Previous Value)';

  @override
  String get boolean => '체크박스';

  @override
  String get btndetails => '상세보기';

  @override
  String get btnCancel => '취소';

  @override
  String get calculated => '수식';

  @override
  String get calculatedSuffix => '(계산됨)';

  @override
  String get changeContent => '변경내용';

  @override
  String get changeDetails => '변경 내역';

  @override
  String get changeHistory => '변경내역';

  @override
  String get changeHistoryTab => '변경 이력';

  @override
  String get changeType => '유형';

  @override
  String get changedBy => '변경자';

  @override
  String get chartRatio => '비율';

  @override
  String get chatTableTitle => '엑셀 데이터 표';

  @override
  String get classificationNode => '분류 노드';

  @override
  String get clickImageToExpandTip => '클릭하면 원본 이미지를 큰 모달창으로 확대합니다';

  @override
  String get clickTableToExpandTip => '클릭하면 큰 모달창 데이터 뷰어로 확대 조회합니다';

  @override
  String get colaction => '작업';

  @override
  String get colcreatedat => '기안 일시';

  @override
  String get colrequester => '기안자';

  @override
  String get colstatus => '상태';

  @override
  String get coltargettype => '요청 유형';

  @override
  String get collapse => '접기';

  @override
  String get comment => '의견';

  @override
  String get commonLoading => '데이터 처리 중입니다...';

  @override
  String get compareChanges => '변경 내역 비교';

  @override
  String comparingCount(Object count) {
    return '$count개 레코드 비교 중';
  }

  @override
  String get confirm => '확인';

  @override
  String get copyAsMarkdown => '📝 마크다운 표로 복사';

  @override
  String get copyCell => '셀 복사';

  @override
  String get copyTableBtn => '표 데이터 복사';

  @override
  String get copyTableBtnTitle => '클립보드에 엑셀 표 데이터 복사';

  @override
  String get create => '생성';

  @override
  String get createGroupBtn => '그룹 생성';

  @override
  String get createWorkflowTitle => '🆕 신규 워크플로우 서식 등록';

  @override
  String get createdat => '생성일시';

  @override
  String get creationSuccess => '생성 완료';

  @override
  String get currentAffiliation => '현재 소속 정보:';

  @override
  String get dashboard => '대시보드';

  @override
  String get dashboardSubtitle =>
      '마스터 데이터 거버넌스 종합 현황, 품질 진단 지표 및 핵심 연계 상태를 관제합니다.';

  @override
  String get date => '날짜';

  @override
  String get dateTime => '일시';

  @override
  String get decryptFailed => '복호화에 실패했습니다.';

  @override
  String get deduplicationCandidateRecord => '중복 후보 레코드';

  @override
  String get candidateRecord => '중복 후보 레코드';

  @override
  String get deduplicationCompareAndAction => '비교 및 처리';

  @override
  String get compareAndAction => '비교 및 처리';

  @override
  String get deduplicationConfirmMerge => '마스터로 병합 승인 (Merge)';

  @override
  String get confirmMerge => '병합 검토';

  @override
  String get deduplicationDuplicateCandidate => '중복 후보 레코드 (병합 대상)';

  @override
  String get duplicateCandidate => '중복 후보 레코드 (병합 대상)';

  @override
  String get deduplicationKeepSeparate => '별도 레코드로 유지 (Ignore)';

  @override
  String get keepSeparate => '별도 레코드로 유지 (Ignore)';

  @override
  String get deduplicationMasterRecord => '마스터 레코드 (유지)';

  @override
  String get masterRecord => '마스터 레코드 (유지)';

  @override
  String get deduplicationModalTitle => 'Side-by-Side 레코드 필드 비교';

  @override
  String get modalTitle => 'Side-by-Side 레코드 필드 비교';

  @override
  String get deduplicationNoCandidates => '검토할 중복 후보 레코드가 없습니다.';

  @override
  String get noCandidates => '검토할 중복 후보 레코드가 없습니다.';

  @override
  String deduplicationPendingCount(Object count) {
    return '검토 대기: $count건';
  }

  @override
  String get deduplicationRuleDefault => '기본 퍼지 매칭 룰';

  @override
  String get ruleDefault => '기본 퍼지 매칭 룰';

  @override
  String get deduplicationSimilarity => '유사도';

  @override
  String get similarity => '유사도';

  @override
  String get deduplicationSubtitle =>
      '퍼지 매칭 및 매칭 룰에 의해 발견된 유사 중복 데이터를 검토하고 병합 또는 분리를 처리합니다.';

  @override
  String get deduplicationTargetRecord => '기준 마스터 레코드';

  @override
  String get targetRecord => '기준 마스터 레코드';

  @override
  String get deduplicationTitle => '중복 후보 검토 큐';

  @override
  String get defaultBadge => '⭐ 기본';

  @override
  String get delete => '삭제';

  @override
  String get deleteErrorTitle => '삭제 오류';

  @override
  String get deletedStatus => '삭제됨';

  @override
  String get department => '부서';

  @override
  String get descriptionCol => '상세 설명';

  @override
  String get detailsInfo => '상세 정보';

  @override
  String get diffCountSuffix => '개 항목 다름';

  @override
  String get doReview => '심사하기';

  @override
  String get draftCommentOptional => '(선택사항) 결재권자에게 남길 기안 의견을 작성해주세요';

  @override
  String get draftCommentPlaceholder => '의견을 입력하세요...';

  @override
  String get draftCommentTitle => '기안 의견 작성';

  @override
  String eGAbsKeyAKeyB2100(Object KEY_A, Object KEY_B) {
    return '예: ABS($KEY_A + $KEY_B / 2) * 100';
  }

  @override
  String get edit => '채널 수정';

  @override
  String get editMenu => '메뉴 정보 수정';

  @override
  String get editWorkflowTitle => '✏️ 워크플로우 서식 수정';

  @override
  String get empNoPrefix => '사번';

  @override
  String get endDate => '종료일자';

  @override
  String get enterFormula => '수식을 입력해주세요.';

  @override
  String get enterKeyAllOptions => '모든 옵션에 Key를 입력해주세요.';

  @override
  String get error => '오류';

  @override
  String errorDedupFailed(Object field) {
    return '중복 데이터 발생: \'$field\' 속성을 기준으로 이미 동일한 레코드가 존재합니다.';
  }

  @override
  String get errorDeletePendingCreation => '생성 결재가 대기 중인 레코드는 삭제할 수 없습니다.';

  @override
  String get errorFetching => '데이터 조회 중 오류가 발생했습니다.';

  @override
  String get errorInvalidCredentials => '아이디 또는 비밀번호가 올바르지 않습니다.';

  @override
  String get errorNotAssignee => '해당 결재 단계의 처리 권한이 없습니다.';

  @override
  String get errorSaving => '저장 중 오류가 발생했습니다.';

  @override
  String get errorStepNotPending => '현재 대기 중인 결재 단계가 아닙니다.';

  @override
  String get errorUpdatePendingCreation => '생성 결재가 대기 중인 레코드는 수정할 수 없습니다.';

  @override
  String get errorUpdatePendingUpdate => '이미 수정 결재가 진행 중인 레코드입니다.';

  @override
  String get errorUploadDirFail => '업로드된 파일을 저장할 디렉토리를 생성할 수 없습니다.';

  @override
  String get errorUploadFileFail => '파일을 저장할 수 없습니다. 다시 시도해 주세요!';

  @override
  String get expand => '펼치기';

  @override
  String get failedLoadApprovalDetails => '결재 상세 내역을 불러오지 못했습니다.';

  @override
  String get failedLoadHistory => '이력 데이터를 불러오는데 실패했습니다.';

  @override
  String get file => '파일';

  @override
  String get fileUploadButton => '내 PC에서 선택';

  @override
  String get fileUploadDropzone => '여기로 파일을 드래그 하거나 ';

  @override
  String get finalPermValue => '최종 권한 값';

  @override
  String get formDescription => '서식 상세 설명';

  @override
  String get formDescriptionPlaceholder => '서식 목적 및 승인 절차 설명...';

  @override
  String get formNameEn => '서식명 (English)';

  @override
  String get formNameEnPlaceholder => 'e.g. Domestic Stock Creation Form';

  @override
  String get formNameKo => '서식명 (한국어) *';

  @override
  String get formNameKoPlaceholder => '예: 국내주식 신규 등록 서식';

  @override
  String get formulaGuide => '수식 작성 가이드';

  @override
  String get governanceHealthTitle => '데이터 거버넌스 & 품질 조치 현황';

  @override
  String get grant => '권한 부여';

  @override
  String get grantNewPermission => '신규 권한 부여';

  @override
  String get grantPermissionSuccess => '선택한 도메인 권한이 성공적으로 부여되었습니다.';

  @override
  String get group => '그룹';

  @override
  String get groupCodeLabel => '그룹 코드명 (예: report, api)';

  @override
  String get groupIconLabel => '이모지 아이콘 (예: 📊, 🔑, ⚙️)';

  @override
  String get groupTitleEnLabel =>
      '그룹 영문 명칭 (예: Report Permissions, API Permissions)';

  @override
  String get groupTitleLabel => '그룹 한국어 명칭 (예: 리포트 권한, API 권한)';

  @override
  String get hidden => '숨김';

  @override
  String get history => '변경이력';

  @override
  String get historyVersionDiffDetail => '이력 버전 변경 항목 상세 비교';

  @override
  String get immutable => '수정 금지';

  @override
  String get impactAnalysis => '영향도 사전 분석';

  @override
  String get impactAnalysisPreview => '사전 영향도 시뮬레이션';

  @override
  String get impactCheckTitle => '변경 사전 영향도 검토';

  @override
  String get impactSafetyNotice => '검토 사항';

  @override
  String impactSummaryDelete(Object field, Object count) {
    return '속성 필드 \'$field\' 삭제 시, 실제 데이터가 존재하는 $count건의 마스터 레코드 내 해당 값이 영구 손실됩니다.';
  }

  @override
  String impactSummaryDeleteEmpty(Object field) {
    return '선택한 \'$field\' 속성 필드에 등록된 실데이터가 0건이므로 레코드 데이터 손실 위험 없이 안전하게 삭제 가능합니다.';
  }

  @override
  String impactSummaryModify(Object field, Object count) {
    return '속성 필드 \'$field\' 변경 사항이 실데이터를 보유한 $count건의 레코드에 적용됩니다.';
  }

  @override
  String impactSummaryModifyEmpty(Object field) {
    return '선택한 \'$field\' 속성 필드에 등록된 실데이터가 0건이므로 데이터 파기 위험 없이 안전하게 변경 가능합니다.';
  }

  @override
  String get impactWarnings => '경고 및 주의사항';

  @override
  String get inactiveBadge => '비활성';

  @override
  String get inactiveStatus => '비활성';

  @override
  String get incomingPayloadTitle => 'incoming_payload.json (수신 원본 데이터)';

  @override
  String get info => '안내';

  @override
  String get infoMsg => '좌측 트리에서 도메인 노드를 선택하여 레코드를 조회하세요.';

  @override
  String get initialCreated => '초기 생성됨';

  @override
  String get initialCreation => '초기 생성';

  @override
  String get installBtnNext => '다음 (관리자 계정 설정)';

  @override
  String get installBtnPrev => '이전';

  @override
  String get installRequirePwdLen => '비밀번호는 최소 6자 이상이어야 합니다.';

  @override
  String get installStep1Label => '대표 조직 설정';

  @override
  String get installStep2Label => '최고 관리자 계정 생성';

  @override
  String get installSubtitle => '초기 시스템 설치 및 최고 관리자(Super Admin) 계정을 등록합니다.';

  @override
  String get installTitle => '시스템 설치 마법사';

  @override
  String get integrationChannelsAdd => '채널 등록';

  @override
  String get integrationChannelsAddField => '필드 추가';

  @override
  String get addField => '필드 추가';

  @override
  String get integrationChannelsAddHeader => '헤더 추가';

  @override
  String get addHeader => '헤더 추가';

  @override
  String get integrationChannelsApprovalDetailTitle => '결재 내역 상세';

  @override
  String get integrationChannelsAuthApiKey => 'API Key 헤더';

  @override
  String get authApiKey => 'API Key 헤더';

  @override
  String get integrationChannelsAuthBearer => 'Bearer Token (추천)';

  @override
  String get authBearer => 'Bearer Token (추천)';

  @override
  String get integrationChannelsAuthHeaderExample => '요청 인증 헤더';

  @override
  String get authHeaderExample => '요청 인증 헤더';

  @override
  String get integrationChannelsAuthNone => '인증 없음 (미사용)';

  @override
  String get authNone => '인증 없음 (미사용)';

  @override
  String get integrationChannelsAuthType => 'Inbound 인증 방식';

  @override
  String get authType => 'Inbound 인증 방식';

  @override
  String get integrationChannelsAutoMapFields => '도메인 필드 자동 매핑 (다국어 포함)';

  @override
  String get autoMapFields => '도메인 필드 자동 매핑 (다국어 포함)';

  @override
  String get integrationChannelsBasicConfig => '기본 정보 & 연동 설정';

  @override
  String get basicConfig => '기본 정보 & 연동 설정';

  @override
  String get integrationChannelsChannelCode => '채널 코드';

  @override
  String get channelCode => '채널 코드';

  @override
  String get integrationChannelsChannelName => '채널명';

  @override
  String get channelName => '채널명';

  @override
  String get integrationChannelsConfirmDeleteChannel => '정말 이 채널을 삭제하시겠습니까?';

  @override
  String get confirmDeleteChannel => '정말 이 채널을 삭제하시겠습니까?';

  @override
  String get integrationChannelsCopied => '복사되었습니다.';

  @override
  String get copied => '복사되었습니다.';

  @override
  String get integrationChannelsCopyCurl => 'cURL 복사';

  @override
  String get copyCurl => 'cURL 복사';

  @override
  String get integrationChannelsCopyHeader => '헤더 복사';

  @override
  String get copyHeader => '헤더 복사';

  @override
  String get integrationChannelsCopyJson => 'JSON 복사';

  @override
  String get copyJson => 'JSON 복사';

  @override
  String get integrationChannelsCopyValue => '값 복사';

  @override
  String get copyValue => '값 복사';

  @override
  String get integrationChannelsCreatedAt => '생성일';

  @override
  String get createdAt => '생성일';

  @override
  String get integrationChannelsCurlCopied => 'cURL 호출 샘플이 클립보드에 복사되었습니다.';

  @override
  String get curlCopied => 'cURL 호출 샘플이 클립보드에 복사되었습니다.';

  @override
  String get integrationChannelsDbPassword => '비밀번호';

  @override
  String get dbPassword => '비밀번호';

  @override
  String get integrationChannelsDbTable => '대상 테이블명';

  @override
  String get dbTable => '대상 테이블명';

  @override
  String get integrationChannelsDbUrl => 'DB 접속 URL';

  @override
  String get dbUrl => 'DB 접속 URL';

  @override
  String get integrationChannelsDbUser => '사용자명';

  @override
  String get dbUser => '사용자명';

  @override
  String get integrationChannelsDeptRoles =>
      'Department Roles (Multi-selectable)';

  @override
  String get integrationChannelsDesc =>
      '외부 시스템과의 데이터 연동 파이프라인 및 인터페이스 채널 설정을 관리합니다.';

  @override
  String get integrationChannelsDescription => '채널을 관리할 수 있습니다.';

  @override
  String get integrationChannelsDetailConfig => '채널 상세 설정';

  @override
  String get detailConfig => '채널 상세 설정';

  @override
  String get integrationChannelsDirection => '연계 방향';

  @override
  String get direction => '연계 방향';

  @override
  String get integrationChannelsDomainField => '도메인 필드 (선택)';

  @override
  String get domainField => '도메인 필드 (선택)';

  @override
  String get integrationChannelsDomainRequiredForInbound =>
      'Inbound 연계 시 도메인 선택은 필수입니다.';

  @override
  String get domainRequiredForInbound => 'Inbound 연계 시 도메인 선택은 필수입니다.';

  @override
  String get integrationChannelsEdit => '채널 수정';

  @override
  String get integrationChannelsErrDbUrlRequired => 'DB 접속 URL을 입력해주세요.';

  @override
  String get errDbUrlRequired => 'DB 접속 URL을 입력해주세요.';

  @override
  String get integrationChannelsErrMqBrokerRequired => '브로커 URL을 입력해주세요.';

  @override
  String get errMqBrokerRequired => '브로커 URL을 입력해주세요.';

  @override
  String get integrationChannelsErrTestConnection =>
      '연결 테스트 중 네트워크/서버 오류가 발생했습니다.';

  @override
  String get errTestConnection => '연결 테스트 중 네트워크/서버 오류가 발생했습니다.';

  @override
  String get integrationChannelsErrWsUrlRequired => '엔드포인트 URL을 입력해주세요.';

  @override
  String get errWsUrlRequired => '엔드포인트 URL을 입력해주세요.';

  @override
  String get integrationChannelsFieldMapping => '데이터 필드 맵핑';

  @override
  String get fieldMapping => '데이터 필드 맵핑';

  @override
  String get integrationChannelsGenerateToken => '토큰 생성';

  @override
  String get generateToken => '토큰 생성';

  @override
  String get integrationChannelsHeaderCopied => '인증 헤더가 클립보드에 복사되었습니다.';

  @override
  String get headerCopied => '인증 헤더가 클립보드에 복사되었습니다.';

  @override
  String get integrationChannelsHeaderValueCopied =>
      '헤더 값(Bearer 토큰)이 클립보드에 복사되었습니다.';

  @override
  String get headerValueCopied => '헤더 값(Bearer 토큰)이 클립보드에 복사되었습니다.';

  @override
  String get integrationChannelsInbound => 'Inbound (수신)';

  @override
  String get inbound => 'Inbound (수신)';

  @override
  String get integrationChannelsInboundNotice =>
      '외부 시스템에서 아래 Webhook URL로 JSON Payload를 POST 요청하면 설정된 매핑 규칙에 따라 데이터가 처리됩니다.';

  @override
  String get inboundNotice =>
      '외부 시스템에서 아래 Webhook URL로 JSON Payload를 POST 요청하면 설정된 매핑 규칙에 따라 데이터가 처리됩니다.';

  @override
  String get integrationChannelsIntegrationDetailTitle => '연계 내역 상세';

  @override
  String get integrationDetailTitle => '연동 상세 내역';

  @override
  String get integrationChannelsIsActive => '활성화 여부';

  @override
  String get integrationChannelsJsonCopied => '샘플 JSON Payload가 클립보드에 복사되었습니다.';

  @override
  String get jsonCopied => '샘플 JSON Payload가 클립보드에 복사되었습니다.';

  @override
  String get integrationChannelsManagement => '관리';

  @override
  String get management => '관리';

  @override
  String get integrationChannelsMappingDesc =>
      '타겟 필드명과 소스 표현식을 자유롭게 편집하세요. 도메인을 선택하면 도메인 필드 드롭다운이 활성화됩니다.';

  @override
  String get mappingDesc =>
      '타겟 필드명과 소스 표현식을 자유롭게 편집하세요. 도메인을 선택하면 도메인 필드 드롭다운이 활성화됩니다.';

  @override
  String get integrationChannelsMappingDescInbound =>
      '배열 다중 처리 시 Root Path를 입력하세요 (예: payload[\'data\']). 표현식은 단일 객체(#this) 기준으로 작성하세요.';

  @override
  String get mappingDescInbound =>
      '배열 다중 처리 시 Root Path를 입력하세요 (예: payload[\'data\']). 표현식은 단일 객체(#this) 기준으로 작성하세요.';

  @override
  String get integrationChannelsMappingRootPath => '배열 데이터 루트 경로 (Root Path)';

  @override
  String get mappingRootPath => '배열 데이터 루트 경로 (Root Path)';

  @override
  String get integrationChannelsMappingRootPathPlaceholder =>
      '예: payload[\'data\'] 또는 payload.data';

  @override
  String get mappingRootPathPlaceholder =>
      '예: payload[\'data\'] 또는 payload.data';

  @override
  String get integrationChannelsMqBroker => '브로커 URL';

  @override
  String get mqBroker => '브로커 URL';

  @override
  String get integrationChannelsMqTopic => '토픽명';

  @override
  String get mqTopic => '토픽명';

  @override
  String get integrationChannelsName => '채널명';

  @override
  String get integrationChannelsNoHeaders => '설정된 헤더가 없습니다.';

  @override
  String get noHeaders => '설정된 헤더가 없습니다.';

  @override
  String get integrationChannelsNodeRequiredForInbound =>
      'Inbound 연계 시 분류 노드 선택은 필수입니다.';

  @override
  String get nodeRequiredForInbound => 'Inbound 연계 시 분류 노드 선택은 필수입니다.';

  @override
  String get integrationChannelsOutbound => 'Outbound (발신)';

  @override
  String get outbound => 'Outbound (발신)';

  @override
  String get integrationChannelsRequiresApproval => '승인 절차 필수 적용';

  @override
  String get requiresApproval => '승인 절차 필수 적용';

  @override
  String get integrationChannelsSamplePayloadNotice =>
      '매핑 탭에서 구성한 소스 표현식과 Root Path 정보가 실시간으로 반영된 요청 Payload 예시입니다.';

  @override
  String get samplePayloadNotice =>
      '매핑 탭에서 구성한 소스 표현식과 Root Path 정보가 실시간으로 반영된 요청 Payload 예시입니다.';

  @override
  String get integrationChannelsSamplePayloadTitle =>
      '요청 JSON Payload 샘플 (실시간 매핑 반영)';

  @override
  String get samplePayloadTitle => '요청 JSON Payload 샘플 (실시간 매핑 반영)';

  @override
  String get integrationChannelsSecretToken => '시크릿 토큰 (Secret Token)';

  @override
  String get secretToken => '시크릿 토큰 (Secret Token)';

  @override
  String get integrationChannelsSelectDomain => '도메인 선택';

  @override
  String get selectDomain => '도메인 선택';

  @override
  String get integrationChannelsSelectDomainNode => '연계 대상 도메인 (노드) 선택';

  @override
  String get selectDomainNode => '연계 대상 도메인 (노드) 선택';

  @override
  String get integrationChannelsSelectNode => '노드 선택 (선택 시 해당 노드만 연계)';

  @override
  String get selectNode => '노드 선택 (선택 시 해당 노드만 연계)';

  @override
  String get integrationChannelsSourceExpr => '소스 추출 표현식';

  @override
  String get sourceExpr => '소스 추출 표현식';

  @override
  String get integrationChannelsSourceFieldInbound =>
      '외부 수신(소스) 필드명 (Source Field)';

  @override
  String get sourceFieldInbound => '외부 수신(소스) 필드명 (Source Field)';

  @override
  String get integrationChannelsStatus => '상태';

  @override
  String get integrationChannelsSystemNotification => 'System Notification';

  @override
  String get integrationChannelsTargetField => '타겟 필드명 (Target Field)';

  @override
  String get integrationChannelsTargetFieldInbound =>
      '내부 도메인 필드 (Target Field)';

  @override
  String get targetFieldInbound => '내부 도메인 필드 (Target Field)';

  @override
  String get integrationChannelsTestConnection => '연결 테스트';

  @override
  String get testConnection => '연결 테스트';

  @override
  String get integrationChannelsTitle => '연계 채널 관리';

  @override
  String get integrationChannelsType => '채널 타입';

  @override
  String get type => '유형';

  @override
  String get integrationChannelsWebhookCopy => 'URL 복사';

  @override
  String get webhookCopy => 'URL 복사';

  @override
  String get integrationChannelsWebhookUrl => '수신 Webhook URL';

  @override
  String get webhookUrl => '수신 Webhook URL';

  @override
  String get integrationChannelsWsMethod => 'HTTP Method';

  @override
  String get wsMethod => 'HTTP Method';

  @override
  String get integrationChannelsWsUrl => '엔드포인트 URL';

  @override
  String get wsUrl => '엔드포인트 URL';

  @override
  String get channelsAdd => '채널 등록';

  @override
  String get channelsAddField => '필드 추가';

  @override
  String get channelsAddHeader => '헤더 추가';

  @override
  String get channelsApprovalDetailTitle => '결재 내역 상세';

  @override
  String get channelsAuthApiKey => 'API Key 헤더';

  @override
  String get channelsAuthBearer => 'Bearer Token (추천)';

  @override
  String get channelsAuthHeaderExample => '요청 인증 헤더';

  @override
  String get channelsAuthNone => '인증 없음 (미사용)';

  @override
  String get channelsAuthType => 'Inbound 인증 방식';

  @override
  String get channelsAutoMapFields => '도메인 필드 자동 매핑 (다국어 포함)';

  @override
  String get channelsBasicConfig => '기본 정보 & 연동 설정';

  @override
  String get channelsChannelCode => '채널 코드';

  @override
  String get channelsChannelName => '채널명';

  @override
  String get channelsConfirmDeleteChannel => '정말 이 채널을 삭제하시겠습니까?';

  @override
  String get channelsCopied => '복사되었습니다.';

  @override
  String get channelsCopyCurl => 'cURL 복사';

  @override
  String get channelsCopyHeader => '헤더 복사';

  @override
  String get channelsCopyJson => 'JSON 복사';

  @override
  String get channelsCopyValue => '값 복사';

  @override
  String get channelsCreatedAt => '생성일';

  @override
  String get channelsCurlCopied => 'cURL 호출 샘플이 클립보드에 복사되었습니다.';

  @override
  String get channelsDbPassword => '비밀번호';

  @override
  String get channelsDbTable => '대상 테이블명';

  @override
  String get channelsDbUrl => 'DB 접속 URL';

  @override
  String get channelsDbUser => '사용자명';

  @override
  String get channelsDeptRoles => 'Department Roles (Multi-selectable)';

  @override
  String get channelsDesc => '외부 시스템과의 데이터 연동 파이프라인 및 인터페이스 채널 설정을 관리합니다.';

  @override
  String get channelsDescription => '채널을 관리할 수 있습니다.';

  @override
  String get channelsDetailConfig => '채널 상세 설정';

  @override
  String get channelsDirection => '연계 방향';

  @override
  String get channelsDomainField => '도메인 필드 (선택)';

  @override
  String get channelsDomainRequiredForInbound => 'Inbound 연계 시 도메인 선택은 필수입니다.';

  @override
  String get channelsEdit => '채널 수정';

  @override
  String get channelsErrDbUrlRequired => 'DB 접속 URL을 입력해주세요.';

  @override
  String get channelsErrMqBrokerRequired => '브로커 URL을 입력해주세요.';

  @override
  String get channelsErrTestConnection => '연결 테스트 중 네트워크/서버 오류가 발생했습니다.';

  @override
  String get channelsErrWsUrlRequired => '엔드포인트 URL을 입력해주세요.';

  @override
  String get channelsFieldMapping => '데이터 필드 맵핑';

  @override
  String get channelsGenerateToken => '토큰 생성';

  @override
  String get channelsHeaderCopied => '인증 헤더가 클립보드에 복사되었습니다.';

  @override
  String get channelsHeaderValueCopied => '헤더 값(Bearer 토큰)이 클립보드에 복사되었습니다.';

  @override
  String get channelsInbound => 'Inbound (수신)';

  @override
  String get channelsInboundNotice =>
      '외부 시스템에서 아래 Webhook URL로 JSON Payload를 POST 요청하면 설정된 매핑 규칙에 따라 데이터가 처리됩니다.';

  @override
  String get channelsIntegrationDetailTitle => '연계 내역 상세';

  @override
  String get channelsIsActive => '활성화 여부';

  @override
  String get channelsJsonCopied => '샘플 JSON Payload가 클립보드에 복사되었습니다.';

  @override
  String get channelsManagement => '관리';

  @override
  String get channelsMappingDesc =>
      '타겟 필드명과 소스 표현식을 자유롭게 편집하세요. 도메인을 선택하면 도메인 필드 드롭다운이 활성화됩니다.';

  @override
  String get channelsMappingDescInbound =>
      '배열 다중 처리 시 Root Path를 입력하세요 (예: payload[\'data\']). 표현식은 단일 객체(#this) 기준으로 작성하세요.';

  @override
  String get channelsMappingRootPath => '배열 데이터 루트 경로 (Root Path)';

  @override
  String get channelsMappingRootPathPlaceholder =>
      '예: payload[\'data\'] 또는 payload.data';

  @override
  String get channelsMqBroker => '브로커 URL';

  @override
  String get channelsMqTopic => '토픽명';

  @override
  String get channelsName => '채널명';

  @override
  String get channelsNoHeaders => '설정된 헤더가 없습니다.';

  @override
  String get channelsNodeRequiredForInbound => 'Inbound 연계 시 분류 노드 선택은 필수입니다.';

  @override
  String get channelsOutbound => 'Outbound (발신)';

  @override
  String get channelsRequiresApproval => '승인 절차 필수 적용';

  @override
  String get channelsSamplePayloadNotice =>
      '매핑 탭에서 구성한 소스 표현식과 Root Path 정보가 실시간으로 반영된 요청 Payload 예시입니다.';

  @override
  String get channelsSamplePayloadTitle => '요청 JSON Payload 샘플 (실시간 매핑 반영)';

  @override
  String get channelsSecretToken => '시크릿 토큰 (Secret Token)';

  @override
  String get channelsSelectDomain => '도메인 선택';

  @override
  String get channelsSelectDomainNode => '연계 대상 도메인 (노드) 선택';

  @override
  String get channelsSelectNode => '노드 선택 (선택 시 해당 노드만 연계)';

  @override
  String get channelsSourceExpr => '소스 추출 표현식';

  @override
  String get channelsSourceFieldInbound => '외부 수신(소스) 필드명 (Source Field)';

  @override
  String get channelsStatus => '상태';

  @override
  String get channelsSystemNotification => 'System Notification';

  @override
  String get channelsTargetField => '타겟 필드명 (Target Field)';

  @override
  String get channelsTargetFieldInbound => '내부 도메인 필드 (Target Field)';

  @override
  String get channelsTestConnection => '연결 테스트';

  @override
  String get channelsTitle => '연계 채널 관리';

  @override
  String get channelsType => '채널 타입';

  @override
  String get channelsWebhookCopy => 'URL 복사';

  @override
  String get channelsWebhookUrl => '수신 Webhook URL';

  @override
  String get channelsWsMethod => 'HTTP Method';

  @override
  String get channelsWsUrl => '엔드포인트 URL';

  @override
  String get integrationChannels => '연계 채널 관리';

  @override
  String get integrationDirection => '연계 방향';

  @override
  String get integrationHistoryBtn => '연계 내역';

  @override
  String get integrationLogDetail => '연계 모니터링 로그 상세';

  @override
  String get integrationLogInfo => '연계 상세 정보 (Integration Log)';

  @override
  String get integrationMappedPayload => '매핑 변환 후 Payload (Mapped Payload)';

  @override
  String get integrationOriginalPayload =>
      '외부 수신 원본 Payload (Original Payload)';

  @override
  String get integrationReceivedAt => '수신 처리시각';

  @override
  String get integrationStatus => '처리 상태';

  @override
  String get isActiveLabel => '사용 여부 (Active)';

  @override
  String get isActiveStatus => '🟢 활성화 상태';

  @override
  String get koLang => '한국어';

  @override
  String get labelDrafter => '기안자';

  @override
  String get labelPassword => '비밀번호';

  @override
  String get labelTimezone => '개인화 타임존';

  @override
  String get language => '언어';

  @override
  String get lastSnapshot => '마지막 스냅샷';

  @override
  String get lineageGuideFlow =>
      '파이프라인 흐름: 소스 시스템(파랑) → 변경 이력(주황) → Golden Master Record(보라) → 외부 연계(초록)';

  @override
  String get lineageGuideTitle => '데이터 계보(Data Lineage) 시각화 안내';

  @override
  String get lineageGuideZoom =>
      '그래프 노드 줌/드래그: 마우스 휠로 확대/축소 및 노드 클릭 시 변경이력 상세 비교 가능';

  @override
  String get lineageTimelineGuideTitle => '데이터 계보(Data Lineage) 노드 구별 안내';

  @override
  String get loadingData => '데이터를 불러오는 중입니다...';

  @override
  String get manageMembers => '구성원 관리';

  @override
  String get manageSectorsGroups => '섹터 및 그룹 관리';

  @override
  String get mappedPayloadTitle => 'mapped_payload.json (매핑 데이터)';

  @override
  String get maxValue => '최대값 (Max)';

  @override
  String get menuAccessStatistics => '메뉴 접근 통계';

  @override
  String get menuIcon => '메뉴 아이콘';

  @override
  String get menuManagement => '메뉴 관리';

  @override
  String get menuManagementDesc => '시스템 전체 트리 메뉴 구조 및 권한별 접근 노드를 관리합니다.';

  @override
  String get mergeAutoSurvivorship => '자동 서바이버십';

  @override
  String get autoSurvivorship => '자동 서바이버십';

  @override
  String get mergeCancel => '취소';

  @override
  String get mergeExecuteMerge => '병합 실행';

  @override
  String get executeMerge => '병합 실행';

  @override
  String get mergeFieldComparison => '필드 비교';

  @override
  String get fieldComparison => '필드 비교';

  @override
  String get mergeFieldName => '필드명';

  @override
  String get fieldName => '속성 / 필드명';

  @override
  String get mergeManualSelect => '수동 선택';

  @override
  String get manualSelect => '수동 선택';

  @override
  String get mergeMergeFail => '병합에 실패했습니다';

  @override
  String get mergeFail => '병합에 실패했습니다';

  @override
  String get mergeMergeSuccess => '레코드 병합이 완료되었습니다';

  @override
  String get mergeSuccess => '레코드 병합이 완료되었습니다';

  @override
  String get mergeMergedRecords => '병합 대상 레코드';

  @override
  String get mergedRecords => '병합 대상 레코드';

  @override
  String get mergePreview => '병합 결과 미리보기';

  @override
  String get preview => '병합 결과 미리보기';

  @override
  String get mergeSelectedValue => '선택된 값';

  @override
  String get selectedValue => '선택된 값';

  @override
  String get mergeSource => '소스';

  @override
  String get source => '소스';

  @override
  String get mergeSurvivorRecord => '서바이버 레코드';

  @override
  String get survivorRecord => '서바이버 레코드';

  @override
  String get mergeTitle => '레코드 병합';

  @override
  String get mergeUnmerge => '언머지 (복구)';

  @override
  String get unmerge => '언머지 (복구)';

  @override
  String get mergeUnmergeConfirmMsg => '정말로 레코드 병합을 해제하시겠습니까?';

  @override
  String get unmergeConfirmMsg => '정말로 레코드 병합을 해제하시겠습니까?';

  @override
  String get mergeUnmergeConfirmTitle => '병합 해제 확인';

  @override
  String get unmergeConfirmTitle => '병합 해제 확인';

  @override
  String get mergeUnmergeFail => '언머지 처리 중 오류가 발생했습니다';

  @override
  String get unmergeFail => '언머지 처리 중 오류가 발생했습니다';

  @override
  String get mergeUnmergeSuccess => '레코드가 성공적으로 언머지(복구) 되었습니다';

  @override
  String get unmergeSuccess => '레코드가 성공적으로 언머지(복구) 되었습니다';

  @override
  String get messengerAttachFileTooltip => '파일 첨부하기';

  @override
  String get attachfiletooltip => '파일 첨부하기';

  @override
  String get messengerCalendarTitle => '날짜 이동';

  @override
  String get calendartitle => '날짜 이동';

  @override
  String get messengerCancelBtn => '취소';

  @override
  String get cancelbtn => '취소';

  @override
  String get messengerCloseBtn => '닫기';

  @override
  String get closebtn => '닫기';

  @override
  String get messengerConfirmBtn => '확인';

  @override
  String get confirmbtn => '확인';

  @override
  String get messengerContentLabel => '내용';

  @override
  String get contentlabel => '내용';

  @override
  String get messengerContextCopy => '복사';

  @override
  String get contextcopy => '복사';

  @override
  String get messengerContextDelete => '삭제';

  @override
  String get contextdelete => '삭제';

  @override
  String get messengerContextForward => '전달';

  @override
  String get contextforward => '전달';

  @override
  String get messengerCopiedToClipboard => '📋 클립보드에 복사되었습니다!';

  @override
  String get copiedtoclipboard => '📋 클립보드에 복사되었습니다!';

  @override
  String get messengerCreateBtn => '생성';

  @override
  String get createbtn => '생성';

  @override
  String get messengerCreateGroupRoomBtn => '+ 그룹 대화방 생성';

  @override
  String get creategrouproombtn => '+ 그룹 대화방 생성';

  @override
  String get messengerCreateGroupRoomTitle => '👥 그룹 대화방 생성';

  @override
  String get creategrouproomtitle => '👥 그룹 대화방 생성';

  @override
  String get messengerCreateRoomTooltip => '그룹 대화방 만들기';

  @override
  String get createroomtooltip => '그룹 대화방 만들기';

  @override
  String get messengerCreatorBadge => '방장';

  @override
  String get creatorbadge => '방장';

  @override
  String get messengerDeptCol => '부서';

  @override
  String get deptcol => '부서';

  @override
  String get messengerDay => '일';

  @override
  String get day => '일';

  @override
  String get messengerDayFri => '금요일';

  @override
  String get dayfri => '금요일';

  @override
  String get messengerDayMon => '월요일';

  @override
  String get daymon => '월요일';

  @override
  String get messengerDaySat => '토요일';

  @override
  String get daysat => '토요일';

  @override
  String get messengerDaySun => '일요일';

  @override
  String get daysun => '일요일';

  @override
  String get messengerDayThu => '목요일';

  @override
  String get daythu => '목요일';

  @override
  String get messengerDayTue => '화요일';

  @override
  String get daytue => '화요일';

  @override
  String get messengerDayWed => '수요일';

  @override
  String get daywed => '수요일';

  @override
  String get messengerDownloadFile => '다운로드';

  @override
  String get downloadfile => '다운로드';

  @override
  String get messengerForwardTitle => '↗️ 메시지 전달하기';

  @override
  String get forwardtitle => '↗️ 메시지 전달하기';

  @override
  String get messengerForwardedFilePrefix => '[전달된 파일]';

  @override
  String get forwardedfileprefix => '[전달된 파일]';

  @override
  String get messengerForwardedImgPrefix => '[전달된 이미지]';

  @override
  String get forwardedimgprefix => '[전달된 이미지]';

  @override
  String get messengerInviteUserBtn => '+ 사용자 초대';

  @override
  String get inviteuserbtn => '+ 사용자 초대';

  @override
  String get messengerInviteModalTitle => '🤝 대화방 사용자 초대';

  @override
  String get invitemodaltitle => '🤝 대화방 사용자 초대';

  @override
  String get messengerKickUserBtn => '내보내기';

  @override
  String get kickuserbtn => '내보내기';

  @override
  String get messengerKickConfirmTitle => '🚪 참여자 강퇴';

  @override
  String get kickconfirmtitle => '🚪 참여자 강퇴';

  @override
  String get messengerKickConfirmDesc => '정말로 이 참여자를 대화방에서 강퇴하시겠습니까?';

  @override
  String get kickconfirmdesc => '정말로 이 참여자를 대화방에서 강퇴하시겠습니까?';

  @override
  String get messengerOnlineStatus => '온라인';

  @override
  String get onlinestatus => '온라인';

  @override
  String get messengerOrgCol => '조직';

  @override
  String get orgcol => '조직';

  @override
  String get messengerPastMessageOptionTitle => '기존 대화 내용 공개 설정';

  @override
  String get pastmessageoptiontitle => '기존 대화 내용 공개 설정';

  @override
  String get messengerPastMessageNone => '안 보임 (기본)';

  @override
  String get pastmessagenone => '안 보임 (기본)';

  @override
  String get messengerPastMessage1h => '최근 1시간';

  @override
  String get pastmessage1h => '최근 1시간';

  @override
  String get messengerPastMessage24h => '최근 24시간';

  @override
  String get pastmessage24h => '최근 24시간';

  @override
  String get messengerPastMessage48h => '최근 48시간';

  @override
  String get pastmessage48h => '최근 48시간';

  @override
  String get messengerForwardedPrefix => '[전달된 메시지]';

  @override
  String get forwardedprefix => '[전달된 메시지]';

  @override
  String get messengerHideTranslation => '번역 숨기기';

  @override
  String get hidetranslation => '번역 숨기기';

  @override
  String get messengerMeBadge => '나';

  @override
  String get mebadge => '나';

  @override
  String get messengerMonth => '월';

  @override
  String get month => '월';

  @override
  String get messengerNoDialogue => '대화가 없습니다.';

  @override
  String get nodialogue => '대화가 없습니다.';

  @override
  String get messengerNoRooms => '참여 중인 대화방이 없습니다.';

  @override
  String get norooms => '참여 중인 대화방이 없습니다.';

  @override
  String get messengerNoUserFound => '검색된 사용자가 없습니다.';

  @override
  String get nouserfound => '검색된 사용자가 없습니다.';

  @override
  String get messengerRoomSettings => '대화방 설정';

  @override
  String get roomsettings => '대화방 설정';

  @override
  String get messengerLeaveRoom => '대화방 나가기';

  @override
  String get leaveroom => '대화방 나가기';

  @override
  String get messengerDeleteRoom => '대화방 삭제';

  @override
  String get deleteroom => '대화방 삭제';

  @override
  String get messengerDelegateCreator => '방장 위임';

  @override
  String get delegatecreator => '방장 위임';

  @override
  String get messengerConfirmLeaveTitle => '대화방 나가기';

  @override
  String get confirmleavetitle => '대화방 나가기';

  @override
  String get messengerConfirmLeaveDesc =>
      '정말 이 대화방을 나가시겠습니까? 대화 내용은 더 이상 볼 수 없습니다.';

  @override
  String get confirmleavedesc => '정말 이 대화방을 나가시겠습니까? 대화 내용은 더 이상 볼 수 없습니다.';

  @override
  String get messengerConfirmDeleteTitle => '대화방 삭제';

  @override
  String get confirmdeletetitle => '대화방 삭제';

  @override
  String get messengerConfirmDeleteDesc =>
      '정말 이 대화방을 삭제하시겠습니까? 모든 참여자의 대화 내용이 삭제되며 복구할 수 없습니다.';

  @override
  String get confirmdeletedesc =>
      '정말 이 대화방을 삭제하시겠습니까? 모든 참여자의 대화 내용이 삭제되며 복구할 수 없습니다.';

  @override
  String get messengerDelegateCreatorTitle => '방장 권한 위임';

  @override
  String get delegatecreatortitle => '방장 권한 위임';

  @override
  String get messengerDelegateCreatorDesc => '방장 권한을 넘겨줄 사용자를 선택하세요.';

  @override
  String get delegatecreatordesc => '방장 권한을 넘겨줄 사용자를 선택하세요.';

  @override
  String messengerConfirmDelegateCreatorDesc(Object username) {
    return '$username 님에게 방장 권한을 위임하시겠습니까? 이 작업은 되돌릴 수 없습니다.';
  }

  @override
  String confirmdelegatecreatordesc(Object username) {
    return '$username 님에게 방장 권한을 위임하시겠습니까? 이 작업은 되돌릴 수 없습니다.';
  }

  @override
  String messengerSystemLeave(Object name) {
    return '$name 님이 대화방을 나갔습니다.';
  }

  @override
  String systemLeave(Object name) {
    return '$name 님이 대화방을 나갔습니다.';
  }

  @override
  String messengerSystemJoin(Object name) {
    return '$name 님이 초대되었습니다.';
  }

  @override
  String systemJoin(Object name) {
    return '$name 님이 초대되었습니다.';
  }

  @override
  String get messengerOfflineStatus => '오프라인';

  @override
  String get offlinestatus => '오프라인';

  @override
  String get messengerPlaceholderMsg => '메시지 입력 또는 이미지 Ctrl+V...';

  @override
  String get placeholdermsg => '메시지 입력 또는 이미지 Ctrl+V...';

  @override
  String get messengerRadioBroadcastTab => '📢 라이브 방송 송출';

  @override
  String get radiobroadcasttab => '📢 라이브 방송 송출';

  @override
  String get messengerRadioChannelUrl => '내 유튜브 채널/뮤직 URL';

  @override
  String get radiochannelurl => '내 유튜브 채널/뮤직 URL';

  @override
  String get messengerRadioChannelUrlPlaceholder =>
      'https://www.youtube.com/@mychannel 또는 내 유튜브 뮤직 프로필...';

  @override
  String get radiochannelurlplaceholder =>
      'https://www.youtube.com/@mychannel 또는 내 유튜브 뮤직 프로필...';

  @override
  String get messengerRadioConfigDesc =>
      '관리자 개인의 YouTube Music / 유튜브 채널 및 플레이리스트를 연동하여 손쉽게 원클릭으로 방송할 수 있습니다.';

  @override
  String get radioconfigdesc =>
      '관리자 개인의 YouTube Music / 유튜브 채널 및 플레이리스트를 연동하여 손쉽게 원클릭으로 방송할 수 있습니다.';

  @override
  String get messengerRadioConnectTab => '🔗 내 YouTube 계정 / 플레이리스트 연동';

  @override
  String get radioconnecttab => '🔗 내 YouTube 계정 / 플레이리스트 연동';

  @override
  String get messengerRadioCustomTitleLabel => '곡 제목 / 방송 타이틀 (선택)';

  @override
  String get radiocustomtitlelabel => '곡 제목 / 방송 타이틀 (선택)';

  @override
  String get messengerRadioCustomTitlePlaceholder => '예: ☕ 오후 업무용 힐링 Lofi BGM';

  @override
  String get radiocustomtitleplaceholder => '예: ☕ 오후 업무용 힐링 Lofi BGM';

  @override
  String get messengerRadioDjBadge => 'DJ';

  @override
  String get radiodjbadge => 'DJ';

  @override
  String get messengerRadioDjTitle => '🎵 DJ 방송 제어판';

  @override
  String get radiodjtitle => '🎵 DJ 방송 제어판';

  @override
  String get messengerRadioListenBtn => '라디오 듣기';

  @override
  String get radiolistenbtn => '라디오 듣기';

  @override
  String get messengerRadioMyPlaylistDefault => '내 연동 플레이리스트';

  @override
  String get radiomyplaylistdefault => '내 연동 플레이리스트';

  @override
  String get messengerRadioPlayThisPlaylist => '이 플레이리스트 재생하기';

  @override
  String get radioplaythisplaylist => '이 플레이리스트 재생하기';

  @override
  String get messengerRadioPlayingNow => '현재 라이브 방송 중';

  @override
  String get radioplayingnow => '현재 라이브 방송 중';

  @override
  String get messengerRadioPlaylistTitle => '재생목록 제목';

  @override
  String get radioplaylisttitle => '재생목록 제목';

  @override
  String get messengerRadioPlaylistTitlePlaceholder => '예: 🎧 내 유튜브 뮤직 힐링 리스트';

  @override
  String get radioplaylisttitleplaceholder => '예: 🎧 내 유튜브 뮤직 힐링 리스트';

  @override
  String get messengerRadioPlaylistUrl => '재생목록(Playlist) URL / ID';

  @override
  String get radioplaylisturl => '재생목록(Playlist) URL / ID';

  @override
  String get messengerRadioPlaylistUrlPlaceholder =>
      'https://www.youtube.com/playlist?list=PL12345... 또는 PL12345';

  @override
  String get radioplaylisturlplaceholder =>
      'https://www.youtube.com/playlist?list=PL12345... 또는 PL12345';

  @override
  String get messengerRadioSaveConfig => '계정/플레이리스트 연동 저장';

  @override
  String get radiosaveconfig => '계정/플레이리스트 연동 저장';

  @override
  String get messengerRadioStartBroadcast => '전 사용자 방송 송출 시작';

  @override
  String get radiostartbroadcast => '전 사용자 방송 송출 시작';

  @override
  String get messengerRadioStopBroadcast => '방송 중단';

  @override
  String get radiostopbroadcast => '방송 중단';

  @override
  String get messengerRadioTrackTitle => '트랙 제목';

  @override
  String get radiotracktitle => '트랙 제목';

  @override
  String get messengerRadioUrlPlaceholder => '오디오 스트림 URL 입력...';

  @override
  String get radiourlplaceholder => '오디오 스트림 URL 입력...';

  @override
  String get messengerRoleCol => '역할';

  @override
  String get rolecol => '역할';

  @override
  String get messengerRoomMembersTitle => '대화방 참여자';

  @override
  String get roommemberstitle => '대화방 참여자';

  @override
  String get messengerRoomNameLabel => '대화방 이름';

  @override
  String get roomnamelabel => '대화방 이름';

  @override
  String get messengerSearchUserPlaceholder => '사용자 검색 (이름, 아이디)...';

  @override
  String get searchuserplaceholder => '사용자 검색 (이름, 아이디)...';

  @override
  String get messengerSelectUsersLabel => '참여 사용자 선택:';

  @override
  String get selectuserslabel => '참여 사용자 선택:';

  @override
  String get messengerSendBtn => '전송';

  @override
  String get sendbtn => '전송';

  @override
  String get messengerTitle => '💬 메신저';

  @override
  String get messengerTranslateMessage => '메시지 번역';

  @override
  String get translatemessage => '메시지 번역';

  @override
  String get messengerTranslating => '번역 중...';

  @override
  String get translating => '번역 중...';

  @override
  String get messengerTranslationError => '번역 처리 중 에러가 발생했습니다.';

  @override
  String get translationerror => '번역 처리 중 에러가 발생했습니다.';

  @override
  String get messengerUnblockUser => '차단 해제';

  @override
  String get unblockuser => '차단 해제';

  @override
  String get messengerUnreadMessagesDesc => 'null개의 안 읽은 메시지가 있습니다.';

  @override
  String get unreadmessagesdesc => 'null개의 안 읽은 메시지가 있습니다.';

  @override
  String get messengerUsernameCol => '사용자명';

  @override
  String get usernamecol => '사용자명';

  @override
  String get messengerViewMembersTooltip => '참여자 목록 보기';

  @override
  String get viewmemberstooltip => '참여자 목록 보기';

  @override
  String get messengerWriterLabel => '작성자';

  @override
  String get writerlabel => '작성자';

  @override
  String get messengerYear => '년';

  @override
  String get year => '년';

  @override
  String get minValue => '최소값';

  @override
  String get modified => '수정됨';

  @override
  String get multiValue => '다중 값';

  @override
  String get multilingual => '다국어';

  @override
  String get myToDoList => '나의 할 일 목록';

  @override
  String get newData => '신규 데이터';

  @override
  String get nextSnapshot => '이후 스냅샷';

  @override
  String get nocomment => '의견 없음';

  @override
  String get noActiveWorkflow => '적용된 결재 워크플로우가 없어 레코드를 작성/수정할 수 없습니다.';

  @override
  String get noAffectedChannels => '연결된 활성 연동 채널 없음';

  @override
  String get noAssignedMembers =>
      '현재 이 부서에 할당된 구성원이 없습니다. 아래에서 구성원을 선택하여 등록하세요.';

  @override
  String get noChangeContent => '변경내용 없음';

  @override
  String get noChangesFound => '변경된 데이터가 없습니다.';

  @override
  String get noDiffOrInitialVersion => '변경 사항이 없거나 최초 생성 버전입니다.';

  @override
  String get noDifferencesFound => '선택된 레코드 간 차이점이 없습니다.';

  @override
  String get noHistoryData => '이력 데이터가 없습니다.';

  @override
  String get noPendingTasksYou => '대기 중인 작업이 없습니다.';

  @override
  String get noPermission => '해당 기능에 대한 권한이 없습니다.';

  @override
  String get noTableData => '테이블 데이터가 없습니다.';

  @override
  String get none => '없음';

  @override
  String get notice => '알림';

  @override
  String get notification => '알림';

  @override
  String get notificationsApprovalFinalized => '결재 최종 완료';

  @override
  String get approvalFinalized => '결재 최종 완료';

  @override
  String get notificationsApprovalPending => '결재 요청 대기';

  @override
  String get approvalPending => '결재 요청 대기';

  @override
  String get notificationsApprovalRejected => '결재 요청 반려';

  @override
  String get approvalRejected => '결재 요청 반려';

  @override
  String get notificationsApprovalStepApproved => '결재 단계 승인 완료';

  @override
  String get approvalStepApproved => '결재 단계 승인 완료';

  @override
  String get notificationsDeleteAll => '모두 삭제';

  @override
  String get deleteAll => '모두 삭제';

  @override
  String get notificationsMarkAllRead => '전체 읽음 처리';

  @override
  String get markAllRead => '모두 읽음';

  @override
  String get notificationsNoNotifications => '새로운 알림이 없습니다';

  @override
  String get noNotifications => '새로운 알림이 없습니다.';

  @override
  String get notificationsTitle => '시스템 알림 센터';

  @override
  String get notificationsTypeApproval => '결재';

  @override
  String get typeApproval => '결재';

  @override
  String get notificationsTypeDq => '데이터 품질';

  @override
  String get typeDq => '데이터 품질';

  @override
  String get notificationsTypeInfo => '안내';

  @override
  String get typeInfo => '안내';

  @override
  String get notificationsTypeWarning => '경고';

  @override
  String get typeWarning => '경고';

  @override
  String get notifiedPersons => '통보자(참조)';

  @override
  String get number => '숫자';

  @override
  String get onlyDifferences => '차이점만 보기';

  @override
  String get opBelow => '이하';

  @override
  String get below => '이하';

  @override
  String get opContains => '포함';

  @override
  String get contains => '포함';

  @override
  String get opEndsWith => '끝';

  @override
  String get endsWith => '끝';

  @override
  String get opEnterKeyword => '검색어 입력';

  @override
  String get enterKeyword => '검색어 입력';

  @override
  String get opEnterNumber => '숫자 입력';

  @override
  String get enterNumber => '숫자 입력';

  @override
  String get opEq => '일치';

  @override
  String get eq => '일치';

  @override
  String get opMaxValue => '최대값 (Max)';

  @override
  String get opRange => '범위';

  @override
  String get range => '범위';

  @override
  String get opSelectOption => '선택해주세요';

  @override
  String get selectOption => '선택해주세요';

  @override
  String get opStartsWith => '시작';

  @override
  String get startsWith => '시작';

  @override
  String get openTableModal => '전용 모달로 크게 보기';

  @override
  String get openTableModalBtn => '크게 보기';

  @override
  String get openTableModalBtnTitle => '큰 모달창 뷰어로 확대 보기';

  @override
  String get otherRequest => '기타 요청';

  @override
  String get outgoingPayloadTitle => 'outgoing_payload.json (송신 데이터)';

  @override
  String get password => '비밀번호';

  @override
  String get pasteOptionDesc =>
      '클립보드 데이터(엑셀 표/텍스트/이미지)를 어떤 형식으로 메시지에 전송하시겠습니까?';

  @override
  String get pasteOptionTitle => '붙여넣기 데이터 전송 방식 선택';

  @override
  String get path => '경로 (Path)';

  @override
  String get pendingFieldApprovalWarning => '현재 변경 결재가 진행 중인 필드가 포함되어 있습니다.';

  @override
  String get permActionLabel => '권한 행위/식별자 (예: export, execute)';

  @override
  String get permAll => '전체';

  @override
  String get permLabelEnLabel => '권한 영문 표시명 (예: Export, Execute)';

  @override
  String get permLabelLabel => '권한 한국어 표시명 (예: 내보내기, 실행)';

  @override
  String get permRead => '조회';

  @override
  String get permWrite => '작성';

  @override
  String get permissions => '권한';

  @override
  String get permissionsMatrixTitle => '부여할 세부 권한 그룹 목록 (Permissions Matrix)';

  @override
  String get placeholderPassword => '비밀번호를 입력하세요';

  @override
  String get placeholderTimezone => '타임존을 선택하세요';

  @override
  String get prevSnapshot => '이전 스냅샷';

  @override
  String get proceedAnyway => '위험 감수 후 변경 적용';

  @override
  String get processDate => '처리 일시';

  @override
  String get processedBy => '처리자';

  @override
  String get processor => '처리자';

  @override
  String get propertyName => '속성 항목';

  @override
  String get proxyapprove => '대결 승인';

  @override
  String get proxyapproveconfirm => '대결 승인 처리하시겠습니까?';

  @override
  String get proxyapprovefail => '대결 승인 처리에 실패했습니다.';

  @override
  String get proxyreject => '대결 반려';

  @override
  String get proxyrejectconfirm => '대결 반려 처리하시겠습니까?';

  @override
  String get proxyrejectfail => '대결 반려 처리에 실패했습니다.';

  @override
  String get rawData => '원본 데이터';

  @override
  String get readOnly => '읽기 전용';

  @override
  String get readingPreviousData => '이전 데이터를 읽는 중입니다. (읽기 전용)';

  @override
  String get readonlySnapshotMsg => '이전 데이터 스냅샷을 조회 중입니다. (읽기 전용)';

  @override
  String get reflectDate => '반영 일시';

  @override
  String get remove => '제거';

  @override
  String get removeSelected => '선택 삭제';

  @override
  String get reqId => '요청 ID';

  @override
  String get requesttype => '요청 유형';

  @override
  String get required => '필수';

  @override
  String get reset => '초기화';

  @override
  String get responseResultTitle => 'response_result.txt (수신 서버 응답 결과)';

  @override
  String get retryIntegration => '연동 재시도';

  @override
  String get riskLevel => '위험도 등급';

  @override
  String get saveChanges => '변경사항 저장';

  @override
  String get saveChangesHint =>
      '* 셀 수정 후 상단 또는 하단의 \'저장\' 버튼을 클릭하여 변경사항을 반영하세요.';

  @override
  String get schemaChange => '스키마 변경';

  @override
  String get scopeCol => '적용 스코프';

  @override
  String get scopeLevel => '적용 스코프 수준 *';

  @override
  String get search => '검색';

  @override
  String get searchCondition => '검색 조건';

  @override
  String get searchFilters => '검색 필터';

  @override
  String get searchInTable => '표 내 데이터 검색...';

  @override
  String get searchKeyword => '키워드 검색';

  @override
  String get searchWorkflowPlaceholder => '서식명 또는 설명 검색...';

  @override
  String get searchable => '검색가능';

  @override
  String get sector => '섹터';

  @override
  String get select => '선택';

  @override
  String get selectIconDesc => '부서 노드 및 헤더에 표시할 커스텀 아이콘을 선택하세요:';

  @override
  String get selectIconTitle => '부서 아이콘 선택';

  @override
  String get selectMenuPrompt => '좌측 트리에서 수정할 메뉴를 선택하세요.';

  @override
  String get selectedCount => '선택됨';

  @override
  String get sendAsImage => '🖼️ 이미지로 전송';

  @override
  String get sendAsTextData => '📋 텍스트/표로 전송';

  @override
  String get setAsBaseline => '기준으로 설정';

  @override
  String get setDefaultWorkflowDesc => '⭐ 기본 서식 지정 (선택 시 해당 액션의 기본 서식으로 설정됨)';

  @override
  String get showRawData => '원본 RAW 데이터 표기';

  @override
  String get snapshotViewingNotice => '이전 데이터 스냅샷을 조회 중입니다. (읽기 전용)';

  @override
  String get startDate => '시작일자';

  @override
  String get statusActive => '🟢 사용중';

  @override
  String get statusCol => '상태';

  @override
  String get statusFilter => '상태';

  @override
  String get statusIgnored => '별도 유지';

  @override
  String get statusInactive => '🔴 중지';

  @override
  String get statusMerged => '병합 완료';

  @override
  String get stepdraft => '상신완료';

  @override
  String get steppending => '대기중';

  @override
  String get stepConsensus => '합의';

  @override
  String get stepNameEnPlaceholder => '스텝명 (English)';

  @override
  String get stepNameKoPlaceholder => '스텝명 (한국어)';

  @override
  String get stepNamePlaceholder => '스텝명 (예: 1차 데이터 검토)';

  @override
  String get stepPrefix => '단계';

  @override
  String get stepTypeConsultation => '합의';

  @override
  String stepsCount(Object count) {
    return '$count단계 승인';
  }

  @override
  String get submissionCommentNotice => '(선택사항) 결재권자에게 남길 상신 사유(의견)를 작성해 주세요.';

  @override
  String get submissionCommentPlaceholder => '상신 사유(의견)를 작성해 주세요...';

  @override
  String get submissionCommentTitle => '상신 의견 작성';

  @override
  String get submissionDate => '상신일';

  @override
  String get submissionReason => '상신 사유';

  @override
  String get successDelete => '성공적으로 삭제되었습니다.';

  @override
  String get successSave => '성공적으로 저장되었습니다.';

  @override
  String get survivorshipAddFirstRule => '첫 번째 규칙 추가하기';

  @override
  String get survivorshipAddRule => '규칙 추가';

  @override
  String get survivorshipDescription =>
      '중복 레코드 병합 시 골든 레코드를 생성하기 위한 필드별 생존 우선순위 및 충돌 해결 전략을 설정합니다.';

  @override
  String get survivorshipEmptyNoDomain => '선택된 도메인이 없습니다.';

  @override
  String get emptyNoDomain => '선택된 도메인이 없습니다.';

  @override
  String get survivorshipEmptyNoRules => '등록된 서바이버십 규칙이 없습니다.';

  @override
  String get survivorshipEmptySubDomain =>
      '상단 드롭다운에서 도메인을 선택하면 서바이버십 규칙 목록이 표시됩니다.';

  @override
  String get emptySubDomain => '상단 드롭다운에서 도메인을 선택하면 서바이버십 규칙 목록이 표시됩니다.';

  @override
  String get survivorshipEmptySubRules =>
      '우측 상단의 \'+ 규칙 추가\' 버튼을 클릭하여 새로운 필드 병합 규칙을 구성할 수 있습니다.';

  @override
  String get emptySubRules =>
      '우측 상단의 \'+ 규칙 추가\' 버튼을 클릭하여 새로운 필드 병합 규칙을 구성할 수 있습니다.';

  @override
  String get survivorshipFieldKey => '도메인 필드 (Field Key)';

  @override
  String get fieldKey => '필드 식별 키 (Key)';

  @override
  String get survivorshipGuideText =>
      'SOURCE_PRIORITY(원천 소스 시스템 우선), MOST_RECENT(최신 수정 일시 기준), MOST_COMPLETE(가장 긴 완전 데이터 기준) 중 도메인별 최적의 병합 규칙을 구성하세요.';

  @override
  String get guideText =>
      'SOURCE_PRIORITY(원천 소스 시스템 우선), MOST_RECENT(최신 수정 일시 기준), MOST_COMPLETE(가장 긴 완전 데이터 기준) 중 도메인별 최적의 병합 규칙을 구성하세요.';

  @override
  String get survivorshipGuideTitle => '서바이버십(Survivorship) 전략 가이드';

  @override
  String get guideTitle => '서바이버십(Survivorship) 전략 가이드';

  @override
  String get survivorshipItemsCount => '개 항목';

  @override
  String get survivorshipKpiDomain => '선택 도메인:';

  @override
  String get kpiDomain => '선택 도메인:';

  @override
  String get survivorshipKpiFields => '도메인 필드:';

  @override
  String get kpiFields => '도메인 필드:';

  @override
  String get survivorshipKpiRules => '등록 규칙:';

  @override
  String get kpiRules => '등록 규칙:';

  @override
  String get survivorshipLoadDomainsFail => '도메인 목록을 불러오지 못했습니다.';

  @override
  String get loadDomainsFail => '도메인 목록을 불러오지 못했습니다.';

  @override
  String get survivorshipMostComplete => 'MOST_COMPLETE (최고 완전성 / 최장 길이)';

  @override
  String get mostComplete => 'MOST_COMPLETE (최고 완전성 / 최장 길이)';

  @override
  String get survivorshipMostRecent => 'MOST_RECENT (최신 수정 시각 기준)';

  @override
  String get mostRecent => 'MOST_RECENT (최신 수정 시각 기준)';

  @override
  String get survivorshipPriority => '우선순위';

  @override
  String get priority => '우선순위';

  @override
  String get survivorshipRefresh => '새로고침';

  @override
  String get survivorshipRuleList => '규칙 목록';

  @override
  String get survivorshipSaveFail => '생존 규칙 저장 중 오류가 발생했습니다.';

  @override
  String get saveFail => '생존 규칙 저장 중 오류가 발생했습니다.';

  @override
  String get survivorshipSaveSettings => '설정 저장';

  @override
  String get saveSettings => '설정 저장';

  @override
  String get survivorshipSaveSuccess => '병합 생존 규칙이 저장되었습니다.';

  @override
  String get survivorshipSelectDomainPlaceholder => '도메인을 선택하세요';

  @override
  String get survivorshipSourcePriority => 'SOURCE_PRIORITY (원천 소스 시스템 우선)';

  @override
  String get sourcePriority => 'SOURCE_PRIORITY (원천 소스 시스템 우선)';

  @override
  String get survivorshipStatAvailableFields => '사용 가능한 도메인 필드';

  @override
  String get statAvailableFields => '사용 가능한 도메인 필드';

  @override
  String get survivorshipStatCurrentDomain => '현재 선택된 도메인';

  @override
  String get statCurrentDomain => '현재 선택된 도메인';

  @override
  String get survivorshipStrategy => '생존 우선순위 전략 (Strategy)';

  @override
  String get strategy => '생존 우선순위 전략 (Strategy)';

  @override
  String get survivorshipStrategyDescSourcePriority =>
      '지정된 원천 소스 시스템(Legacy ERP, CRM 등)의 데이터 필드값을 최우선으로 채택합니다.';

  @override
  String get strategyDescSourcePriority =>
      '지정된 원천 소스 시스템(Legacy ERP, CRM 등)의 데이터 필드값을 최우선으로 채택합니다.';

  @override
  String get survivorshipStrategyDescMostRecent =>
      '가장 최근 시점에 생성되거나 수정 업데이트된 레코드의 필드값을 채택합니다.';

  @override
  String get strategyDescMostRecent =>
      '가장 최근 시점에 생성되거나 수정 업데이트된 레코드의 필드값을 채택합니다.';

  @override
  String get survivorshipStrategyDescMostComplete =>
      'Null이 아니며 가장 많은 정보와 긴 데이터 길이를 보유한 유효 필드값을 채택합니다.';

  @override
  String get strategyDescMostComplete =>
      'Null이 아니며 가장 많은 정보와 긴 데이터 길이를 보유한 유효 필드값을 채택합니다.';

  @override
  String get survivorshipTitle => '생존 규칙 관리';

  @override
  String get syncMenuSeed => '시드 파일로부터 동기화';

  @override
  String get syntaxErrorFormula => '수식의 문법 오류가 있습니다';

  @override
  String syntaxErrorInFormulaEMessage(Object message) {
    return '수식에 문법 오류가 있습니다: $message';
  }

  @override
  String get tabWorkflows => '결재 워크플로우 설정';

  @override
  String get tableViewerTitle => '메신저 데이터 표 상세 뷰어';

  @override
  String get targetbulkupload => '대량 업로드';

  @override
  String get targetrecordcreate => '레코드 등록';

  @override
  String get targetrecorddelete => '레코드 삭제';

  @override
  String get targetrecordupdate => '레코드 수정';

  @override
  String get targetDomainRefNotLoaded => '참조 도메인 정보를 불러오지 못했습니다.';

  @override
  String get text => '텍스트';

  @override
  String get time => '일시';

  @override
  String get timelineList => '타임라인 리스트';

  @override
  String get timezone => '타임존 설정';

  @override
  String get timezoneSelect => '타임존을 선택하세요';

  @override
  String get toValue => '~ 까지';

  @override
  String get today => '오늘';

  @override
  String get treeEmptyMessage =>
      '분류체계 트리가 없습니다. 하단의 Domain 버튼을 눌러 새 도메인을 생성해주세요.';

  @override
  String get typeconsensus => '합의';

  @override
  String get typedraft => '기안';

  @override
  String get unassign => '할당 해제';

  @override
  String get unassigned => '미할당';

  @override
  String get unclassified => '분류 미지정';

  @override
  String get unit => '단위';

  @override
  String get unknown => '알 수 없음';

  @override
  String get unmaskReasonDesc =>
      '민감한 개인정보 원본을 열람하기 위해 접근 사유를 입력해 주세요. 입력된 사유는 감사 로그에 안전하게 기록됩니다.';

  @override
  String get unmaskReasonTitle => '원본 보기 (마스킹 해제) 사유 입력';

  @override
  String get unmergeBtn => '병합 해제';

  @override
  String get update => '수정';

  @override
  String get updateSuccess => '수정 완료';

  @override
  String get updatedat => '변경일';

  @override
  String get notificationCenter => '알림 센터';

  @override
  String get downloadTemplate => '엑셀 템플릿 다운로드';

  @override
  String get exportExcelCsv => '레코드 데이터 내보내기 (CSV/Excel)';

  @override
  String get editorBold => '굵게';

  @override
  String get editorItalic => '기울임';

  @override
  String get editorUnderline => '밑줄';

  @override
  String get editorStrike => '취소선';

  @override
  String get editorHeading1 => '제목 1 (H1)';

  @override
  String get editorHeading2 => '제목 2 (H2)';

  @override
  String get editorHeading3 => '제목 3 (H3)';

  @override
  String get editorParagraph => '본문';

  @override
  String get editorBulletList => '글머리 기호 목록';

  @override
  String get editorOrderedList => '번호 매기기 목록';

  @override
  String get editorBlockquote => '인용구';

  @override
  String get editorCodeBlock => '코드 블록';

  @override
  String get editorAlignLeft => '왼쪽 정렬';

  @override
  String get editorAlignCenter => '가운데 정렬';

  @override
  String get editorAlignRight => '오른쪽 정렬';

  @override
  String get editorAlignJustify => '양쪽 맞춤';

  @override
  String get editorHorizontalRule => '구분선';

  @override
  String get editorUndo => '실행 취소';

  @override
  String get editorRedo => '다시 실행';

  @override
  String get editorPlaceholder => '내용을 입력하세요...';

  @override
  String get editorImage => '이미지 삽입';

  @override
  String get uploadingImage => '이미지 업로드 중...';

  @override
  String get failedUploadImage => '이미지 업로드에 실패했습니다.';

  @override
  String get uploadImage => '이미지 업로드';

  @override
  String get dragDropImageHint => '이미지를 드래그하여 놓거나 클릭하여 업로드하세요 (Ctrl+V 붙여넣기 지원)';

  @override
  String get deleteImage => '이미지 삭제';

  @override
  String get downloadImage => '이미지 다운로드';

  @override
  String get zoomIn => '확대';

  @override
  String get zoomOut => '축소';

  @override
  String get zoomReset => '원래 크기';

  @override
  String get noImage => '등록된 이미지가 없습니다.';

  @override
  String get imageCarouselPrev => '이전 이미지';

  @override
  String get imageCarouselNext => '다음 이미지';

  @override
  String get editorFontFamily => '글꼴';

  @override
  String get editorFontSize => '글자 크기';

  @override
  String get editorTextColor => '글자 색상';

  @override
  String get editorHighlight => '형광펜 배경색';

  @override
  String get editorTable => '표';

  @override
  String get editorInsertTable => '표 삽입 (3x3)';

  @override
  String get editorAddRowBefore => '위에 행 추가';

  @override
  String get editorAddRowAfter => '아래에 행 추가';

  @override
  String get editorDeleteRow => '행 삭제';

  @override
  String get editorAddColBefore => '왼쪽에 열 추가';

  @override
  String get editorAddColAfter => '오른쪽에 열 추가';

  @override
  String get editorDeleteCol => '열 삭제';

  @override
  String get editorMergeCells => '셀 병합';

  @override
  String get editorSplitCell => '셀 분할';

  @override
  String get editorToggleHeaderRow => '헤더 행 토글';

  @override
  String get editorDeleteTable => '표 삭제';

  @override
  String get editorTaskList => '체크리스트 (To-Do)';

  @override
  String get editorClearFormatting => '서식 지우기';

  @override
  String get editorFullscreen => '전체화면';

  @override
  String get editorExitFullscreen => '전체화면 종료';

  @override
  String get editorLink => '링크 삽입';

  @override
  String get editorUnlink => '링크 제거';

  @override
  String editorCharacterCount(Object count) {
    return '$count자';
  }

  @override
  String get editorCopyCode => '코드 복사';

  @override
  String get editorCodeCopied => '코드가 복사되었습니다.';

  @override
  String get modalMaximize => '전체 화면으로 펼치기';

  @override
  String get modalRestore => '원래 크기로 복귀';

  @override
  String get updatedAt => '변경일';

  @override
  String get version => '버전';

  @override
  String get viewdatachanges => '변경 데이터 보기';

  @override
  String get viewAfterSnapshot => '이후 스냅샷';

  @override
  String get viewBeforeSnapshot => '이전 스냅샷';

  @override
  String get viewChanges => '변경 사항 보기';

  @override
  String get viewSnapshot => '스냅샷 보기';

  @override
  String get visualGraph => '비주얼 그래프';

  @override
  String get vuesticCancel => '취소';

  @override
  String get vuesticClose => '닫기';

  @override
  String get vuesticConfirm => '확인';

  @override
  String get vuesticDelete => '삭제';

  @override
  String get vuesticGoFirstPage => '첫 페이지로 이동';

  @override
  String get gofirstpage => '첫 페이지로 이동';

  @override
  String get vuesticGoLastPage => '마지막 페이지로 이동';

  @override
  String get golastpage => '마지막 페이지로 이동';

  @override
  String get vuesticGoNextPage => '다음 페이지로 이동';

  @override
  String get gonextpage => '다음 페이지로 이동';

  @override
  String get vuesticGoPreviousPage => '이전 페이지로 이동';

  @override
  String get gopreviouspage => '이전 페이지로 이동';

  @override
  String vuesticGoToSpecificPage(Object page) {
    return '해당 페이지로 이동 $page';
  }

  @override
  String gotospecificpage(Object page) {
    return '해당 페이지로 이동 $page';
  }

  @override
  String get vuesticNoOptions => '선택 가능한 항목이 없습니다';

  @override
  String get nooptions => '선택 가능한 항목이 없습니다';

  @override
  String get vuesticOk => '확인';

  @override
  String get ok => '확인';

  @override
  String get vuesticOptionsFilter => '옵션 검색';

  @override
  String get optionsfilter => '옵션 검색';

  @override
  String get vuesticPagination => '페이지 이동';

  @override
  String get pagination => '페이지 이동';

  @override
  String get vuesticProgressState => '진행 상태';

  @override
  String get progressstate => '진행 상태';

  @override
  String get vuesticReset => '초기화';

  @override
  String get vuesticSave => '저장';

  @override
  String get vuesticSearch => '검색';

  @override
  String get vuesticSelect => '선택';

  @override
  String get vuesticSelectedOptions => '선택된 옵션';

  @override
  String get selectedoptions => '선택된 옵션';

  @override
  String get vuesticSortColumnBy => '컬럼 정렬';

  @override
  String get sortcolumnby => '컬럼 정렬';

  @override
  String get vuesticToggleDropdown => '드롭다운 토글';

  @override
  String get toggledropdown => '드롭다운 토글';

  @override
  String get vuesticUploadFile => '파일 업로드';

  @override
  String get uploadfile => '파일 업로드';

  @override
  String get vuesticFileTypeIncorrect => '지원하지 않는 파일 형식입니다';

  @override
  String get filetypeincorrect => '지원하지 않는 파일 형식입니다';

  @override
  String get vuesticDropFiles => '파일을 여기에 끌어다 놓으세요';

  @override
  String get dropfiles => '파일을 여기에 끌어다 놓으세요';

  @override
  String get vuesticFilesUploaded => '업로드된 파일';

  @override
  String get filesuploaded => '업로드된 파일';

  @override
  String get vuesticFileSizeIncorrect => '파일 크기가 초과되었습니다';

  @override
  String get filesizeincorrect => '파일 크기가 초과되었습니다';

  @override
  String get waitingFor => '대기중';

  @override
  String warningChannels(Object count) {
    return '활성 상태인 $count개 연동 채널의 매핑 설정을 확인하세요.';
  }

  @override
  String get welcome => '환영합니다';

  @override
  String get workflowdetails => '워크플로우 상세';

  @override
  String get workflowCenterTitle => '워크플로우 & 권한 관리 센터';

  @override
  String get workflowManagement => '워크플로우 관리';

  @override
  String get workflowManagementDesc =>
      '워크플로우 서식을 AG-Grid 테이블로 빠르게 탐색하고, 전용 모달 대화상자에서 손쉽게 생성 및 편집합니다.';

  @override
  String get workflowNameCol => '서식 명칭';

  @override
  String get dataLineage => '데이터 계보';

  @override
  String get dataLineageDesc =>
      '도메인, 분류 노드, 외부 연계 채널 간의 상호 참조 및 데이터 흐름 계보를 시각화합니다.';

  @override
  String get lineageNodes => '계보 구성 노드';

  @override
  String get lineageRelationships => '데이터 연계 흐름';

  @override
  String warningDqRules(Object count) {
    return '해당 필드에 매핑된 $count건의 데이터 품질(DQ) 검칙이 영향받습니다.';
  }

  @override
  String get noAffectedDqRules => '연결된 DQ 검칙 없음';

  @override
  String get expectedDqViolations => '예상 DQ 위반 건수';

  @override
  String get affectedDqRules => '영향받는 DQ 검칙';

  @override
  String get addDqRule => '규칙 추가';

  @override
  String get dqDashboardTitle => '데이터 품질 진단 대시보드';

  @override
  String get dqDashboardSubtitle => '실시간 마스터 데이터 거버넌스 및 데이터 정합성 모니터링';

  @override
  String get dqDashboardSelectDomainPlaceholder => '도메인을 선택하세요';

  @override
  String get dqDashboardTotalRecords => '전체 레코드 수';

  @override
  String get totalRecords => '전체 레코드 수';

  @override
  String get dqDashboardTotalRecordsSub => '도메인 내 모니터링 엔티티';

  @override
  String get totalRecordsSub => '도메인 내 모니터링 엔티티';

  @override
  String get dqDashboardTotalViolations => '총 품질 위반 건수';

  @override
  String get totalViolations => '총 품질 위반 건수';

  @override
  String get dqDashboardActionRequired => '⚠️ 즉시 조치 필요';

  @override
  String get dqDashboardAllPassed => '✅ 전체 검증 통과';

  @override
  String get allPassed => '✅ 전체 검증 통과';

  @override
  String get dqDashboardActiveDqRules => '활성 품질 검칙';

  @override
  String get activeDqRules => '활성 품질 검칙';

  @override
  String get dqDashboardActiveRulesSub => '자동 실행 검증 규칙';

  @override
  String get activeRulesSub => '자동 실행 검증 규칙';

  @override
  String get dqDashboardScoreTrendTitle => 'DQ 품질 점수 트렌드 히스토리';

  @override
  String get scoreTrendTitle => 'DQ 품질 점수 트렌드 히스토리';

  @override
  String dqDashboardSnapshotCount(Object count) {
    return '$count개 스냅샷';
  }

  @override
  String snapshotCount(Object count) {
    return '$count개 스냅샷';
  }

  @override
  String get dqDashboardRecent7Days => '최근 7일';

  @override
  String get recent7Days => '최근 7일';

  @override
  String get dqDashboardRecent30Days => '최근 30일';

  @override
  String get recent30Days => '최근 30일';

  @override
  String get dqDashboardRecent90Days => '최근 90일';

  @override
  String get recent90Days => '최근 90일';

  @override
  String get dqDashboardRecentAll => '전체';

  @override
  String get recentAll => '전체';

  @override
  String get dqDashboardRunScan => '⚡ DQ 스캔 실행';

  @override
  String get runScan => '프로파일링 스캔 실행';

  @override
  String get dqDashboardNoSnapshots => '기록된 DQ 스냅샷 이력이 없습니다';

  @override
  String get noSnapshots => '생성된 도메인 스냅샷이 없습니다.';

  @override
  String get dqDashboardNoSnapshotsDesc =>
      '우측 상단의 \'⚡ DQ 스캔 실행\' 버튼을 클릭하면 도메인 내 마스터 데이터 실시간 정합성 스캔이 수행되고 스냅샷 트렌드가 자동 누적됩니다.';

  @override
  String get noSnapshotsDesc =>
      '우측 상단의 \'⚡ DQ 스캔 실행\' 버튼을 클릭하면 도메인 내 마스터 데이터 실시간 정합성 스캔이 수행되고 스냅샷 트렌드가 자동 누적됩니다.';

  @override
  String get dqDashboardStartScanNow => '⚡ 지금 DQ 스캔 시작하기';

  @override
  String get startScanNow => '⚡ 지금 DQ 스캔 시작하기';

  @override
  String get dqDashboardAvgScore => '평균 품질 점수:';

  @override
  String get avgScore => '평균 품질 점수:';

  @override
  String get dqDashboardMaxScore => '최고 점수:';

  @override
  String get maxScore => '최고 점수:';

  @override
  String get dqDashboardLatestSnapshot => '최근 스냅샷:';

  @override
  String get latestSnapshot => '최근 스냅샷:';

  @override
  String dqDashboardTooltipInfo(Object violations, Object total) {
    return '위반 $violations건 / 전체 $total행';
  }

  @override
  String tooltipInfo(Object violations, Object total) {
    return '위반 $violations건 / 전체 $total행';
  }

  @override
  String get dqDashboardViolationsBySeverity => '심각도별 위반 현황';

  @override
  String get violationsBySeverity => '심각도별 위반 현황';

  @override
  String get dqDashboardNoViolationsDetected =>
      '감지된 품질 위반이 없습니다! 최상의 데이터 정합성입니다.';

  @override
  String get noViolationsDetected => '감지된 품질 위반이 없습니다! 최상의 데이터 정합성입니다.';

  @override
  String get dqDashboardViolationsByField => '필드별 위반 현황';

  @override
  String get violationsByField => '필드별 위반 현황';

  @override
  String get dqDashboardNoFieldViolations => '필드별 위반 내역이 없습니다.';

  @override
  String get noFieldViolations => '필드별 위반 내역이 없습니다.';

  @override
  String get dqDashboardViolationTableTitle => 'DQ 위반 상세 레코드 목록';

  @override
  String get violationTableTitle => 'DQ 위반 상세 레코드 목록';

  @override
  String get dqDashboardViolationTableSub => '실시간 검증 실패 상세 레코드 모니터링';

  @override
  String get violationTableSub => '실시간 검증 실패 상세 레코드 모니터링';

  @override
  String get dqDashboardSeverity => '심각도';

  @override
  String get severity => '심각도';

  @override
  String get dqDashboardField => '필드';

  @override
  String get field => '필드';

  @override
  String get dqDashboardLoadingViolations => '위반 레코드 조회 중...';

  @override
  String get loadingViolations => '위반 레코드 조회 중...';

  @override
  String get dqDashboardNoViolationsFound => '선택한 조건의 품질 위반 레코드가 없습니다.';

  @override
  String get noViolationsFound => '선택한 조건의 품질 위반 레코드가 없습니다.';

  @override
  String get dqDashboardRecordId => '레코드 식별자';

  @override
  String get recordId => '레코드 식별자';

  @override
  String get dqDashboardViolatedField => '위반 필드';

  @override
  String get violatedField => '위반 필드';

  @override
  String get dqDashboardRuleName => '검증 규칙';

  @override
  String get dqDashboardViolationMessage => '위반 내용';

  @override
  String get violationMessage => '위반 내용';

  @override
  String get dqDashboardActualValue => '실제 입력값';

  @override
  String get actualValue => '실제 입력값';

  @override
  String get dqDashboardEmptyValue => '(빈 값)';

  @override
  String get emptyValue => '(빈 값)';

  @override
  String get dqDashboardDetails => '상세보기';

  @override
  String dqDashboardPaginationSummary(Object start, Object end, Object total) {
    return '총 $total건 중 $start - $end건 표시';
  }

  @override
  String paginationSummary(Object start, Object end, Object total) {
    return '총 $total건 중 $start - $end건 표시';
  }

  @override
  String get dqDashboardDesc =>
      '도메인별 데이터 품질 검칙 이행률, 오류 건수 및 필드별 품질 진단 상태를 실시간 모니터링합니다.';

  @override
  String get dqErrorMessage => '오류 메시지';

  @override
  String get dqParams => '설정 파라미터(Params)';

  @override
  String get dqPermGroupTitle => '데이터 품질 권한';

  @override
  String get dqRuleType => '규칙 유형';

  @override
  String get dqRulesDesc => '도메인 필드별 데이터 품질 검증 규칙 및 검사 파라미터를 설정합니다.';

  @override
  String get dqRulesManagement => '데이터 품질 검칙 관리';

  @override
  String get dqScoreTitle => '데이터 품질 종합 점수';

  @override
  String get dqSeverity => '중요도';

  @override
  String get dqSortOrder => '적용 순서';

  @override
  String get editDqRule => '규칙 수정';

  @override
  String errorDqFailed(Object details) {
    return '데이터 품질 검사 실패: $details';
  }

  @override
  String get goToDqDashboard => '품질 관리 이동';

  @override
  String get initiatorRulesTitle => '신청 자격 제어 규칙';

  @override
  String get loadingDqMetrics => '데이터 품질 지표를 불러오는 중입니다...';

  @override
  String get noRulesDefault =>
      '규칙이 등록되지 않았습니다. 기본적으로 모든 사용자가 신청 가능하며 전체 필드 접근 권한이 적용됩니다.';

  @override
  String get openDqViolations => '미조치 품질 위반';

  @override
  String get permissionsRulesTitle => '신청 자격 및 필드 권한 제어 규칙';

  @override
  String get runDqScan => 'DQ 검증 스캔 실행';

  @override
  String get autoRemediation => '지능형 DQ 데이터 자동 정제 & 원클릭 보정';

  @override
  String get autoRemediationDesc =>
      '전화번호/사업자번호 하이픈 누락, 앞뒤 공백 등 품질 오류가 발생한 레코드를 룰 기반으로 자동 보정합니다.';

  @override
  String get scanRemediation => '보정 대상 데이터 자동 탐색';

  @override
  String get applyAllRemediation => '보정 일괄 반영';

  @override
  String get currentVal => '현재 값 (오류)';

  @override
  String get proposedVal => '보정 제안 값 (정상)';

  @override
  String get remediationReason => '보정 사유';

  @override
  String get noRemediationsNeeded => '보정이 필요한 품질 오류 데이터가 없습니다.';

  @override
  String get referenceIntegrity => '도메인 간 참조 무결성 자동 검증';

  @override
  String get referenceIntegrityDesc =>
      '다른 도메인 또는 상위 레코드를 참조하는 외래키 관계에서 고아(Orphan) 데이터 발생 여부를 실시간 진단합니다.';

  @override
  String get integrityScore => '참조 무결성 지수';

  @override
  String get orphanCount => '고아 참조 위반 건수';

  @override
  String get scannedRecords => '검사 완료 레코드';

  @override
  String get orphanDetails => '고아 참조 위반 내역';

  @override
  String get noOrphanRecords => '모든 외래 참조 관계가 유효하며 고아 레코드가 없습니다.';

  @override
  String get dqSeverityDistribution => '품질 오류 심각도별 분포';

  @override
  String get dqViolationTrend => '최근 7일간 품질 오류 추이';

  @override
  String get inboxTitle => '사내 편지함';

  @override
  String get inboxSubtitle => '내부 메시지 및 이메일 통합 문서함';

  @override
  String get inboxFolderInbox => '받은편지함';

  @override
  String get folderInbox => '받은편지함';

  @override
  String get inboxFolderSent => '보낸편지함';

  @override
  String get folderSent => '보낸편지함';

  @override
  String get inboxFolderDraft => '임시보관함';

  @override
  String get folderDraft => '임시보관함';

  @override
  String get inboxFolderArchive => '보관함';

  @override
  String get folderArchive => '보관함';

  @override
  String get inboxFolderTrash => '휴지통';

  @override
  String get folderTrash => '휴지통';

  @override
  String get inboxFolderStarred => '별표 편지';

  @override
  String get folderStarred => '별표 편지';

  @override
  String get inboxCompose => '새 메시지 작성';

  @override
  String get compose => '새 메시지 작성';

  @override
  String get inboxComposeTitle => '메시지 작성';

  @override
  String get composeTitle => '메시지 작성';

  @override
  String get inboxReply => '답장';

  @override
  String get reply => '답장';

  @override
  String get inboxReplyAll => '전체 답장';

  @override
  String get replyAll => '전체 답장';

  @override
  String get inboxForward => '전달';

  @override
  String get forward => '전달';

  @override
  String get inboxSend => '보내기';

  @override
  String get send => '보내기';

  @override
  String get inboxSendFailed => '메시지 전송에 실패하였습니다.';

  @override
  String get sendFailed => '메시지 전송에 실패하였습니다.';

  @override
  String get inboxSaveDraft => '임시저장';

  @override
  String get saveDraft => '임시저장';

  @override
  String get inboxDraftSaved => '임시저장되었습니다.';

  @override
  String get draftSaved => '임시저장되었습니다.';

  @override
  String get inboxDraftFailed => '임시저장에 실패하였습니다.';

  @override
  String get draftFailed => '임시저장에 실패하였습니다.';

  @override
  String get inboxMessageSent => '메시지가 발송되었습니다.';

  @override
  String get messageSent => '메시지가 발송되었습니다.';

  @override
  String get inboxMessageDeleted => '메시지가 삭제되었습니다.';

  @override
  String get messageDeleted => '메시지가 삭제되었습니다.';

  @override
  String get inboxMessageMoved => '메시지가 이동되었습니다.';

  @override
  String get messageMoved => '메시지가 이동되었습니다.';

  @override
  String get inboxMessageStarred => '별표가 설정되었습니다.';

  @override
  String get messageStarred => '별표가 설정되었습니다.';

  @override
  String get inboxMessageUnstarred => '별표가 해제되었습니다.';

  @override
  String get messageUnstarred => '별표가 해제되었습니다.';

  @override
  String get inboxMarkRead => '읽음으로 표시';

  @override
  String get markRead => '읽음으로 표시';

  @override
  String get inboxMarkUnread => '안읽음으로 표시';

  @override
  String get markUnread => '안읽음으로 표시';

  @override
  String get inboxMoveToArchive => '보관함으로 이동';

  @override
  String get moveToArchive => '보관함으로 이동';

  @override
  String get inboxMoveToTrash => '휴지통으로 이동';

  @override
  String get moveToTrash => '휴지통으로 이동';

  @override
  String get inboxDelete => '삭제';

  @override
  String get inboxPermanentDelete => '영구 삭제';

  @override
  String get permanentDelete => '영구 삭제';

  @override
  String get inboxPermanentDeleteConfirm =>
      '이 메시지를 영구적으로 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.';

  @override
  String get permanentDeleteConfirm =>
      '이 메시지를 영구적으로 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.';

  @override
  String get inboxRestore => '복원';

  @override
  String get restore => '복원';

  @override
  String get inboxEmptyTrash => '휴지통 비우기';

  @override
  String get emptyTrash => '휴지통 비우기';

  @override
  String get inboxRecipientTo => '수신';

  @override
  String get recipientTo => '수신';

  @override
  String get inboxRecipientCc => '참조';

  @override
  String get recipientCc => '참조';

  @override
  String get inboxRecipientBcc => '통보';

  @override
  String get recipientBcc => '통보';

  @override
  String get inboxAddRecipient => '수신자 추가';

  @override
  String get addRecipient => '수신자 추가';

  @override
  String get inboxAddExternalEmail => '외부 이메일 추가';

  @override
  String get addExternalEmail => '외부 이메일 추가';

  @override
  String get inboxSearchUsers => '사용자 검색...';

  @override
  String get searchUsers => '사용자 검색...';

  @override
  String get inboxSearchUsersBtn => '사용자 검색';

  @override
  String get searchUsersBtn => '사용자 검색';

  @override
  String get inboxAddressBook => '사용자 검색 / 주소록';

  @override
  String get addressBook => '사용자 검색 / 주소록';

  @override
  String get inboxNoRecipients => '수신자를 선택해주세요.';

  @override
  String get noRecipients => '수신자를 선택해주세요.';

  @override
  String get inboxRecipientRequired => '수신자를 1명 이상 입력하거나 선택해주세요.';

  @override
  String get recipientRequired => '수신자를 1명 이상 입력하거나 선택해주세요.';

  @override
  String get inboxSubjectRequired => '메시지 제목을 입력해주세요.';

  @override
  String get subjectRequired => '메시지 제목을 입력해주세요.';

  @override
  String get inboxNoSubject => '(제목 없음)';

  @override
  String get noSubject => '(제목 없음)';

  @override
  String get inboxSender => '보낸 사람';

  @override
  String get sender => '보낸 사람';

  @override
  String get inboxDate => '날짜';

  @override
  String get inboxSubject => '제목';

  @override
  String get subject => '제목';

  @override
  String get inboxSubjectPlaceholder => '제목을 입력하세요';

  @override
  String get subjectPlaceholder => '제목을 입력하세요';

  @override
  String get inboxBodyPlaceholder => '내용을 입력하세요...';

  @override
  String get bodyPlaceholder => '내용을 입력하세요...';

  @override
  String get inboxImportance => '중요도';

  @override
  String get importance => '중요도';

  @override
  String get inboxImportanceNormal => '보통';

  @override
  String get importanceNormal => '보통';

  @override
  String get inboxImportanceHigh => '중요';

  @override
  String get importanceHigh => '중요';

  @override
  String get inboxImportanceUrgent => '긴급';

  @override
  String get importanceUrgent => '긴급';

  @override
  String get inboxTypeInternal => 'Internal Message';

  @override
  String get typeInternal => 'Internal Message';

  @override
  String get inboxTypeExternalInbound => 'External Inbound Email';

  @override
  String get typeExternalInbound => 'External Inbound Email';

  @override
  String get inboxTypeExternalOutbound => 'External Outbound Email';

  @override
  String get typeExternalOutbound => 'External Outbound Email';

  @override
  String get inboxTypeApprovalNotice => 'Approval Notice';

  @override
  String get typeApprovalNotice => 'Approval Notice';

  @override
  String get inboxTypeSystemNotice => 'System Notice';

  @override
  String get typeSystemNotice => 'System Notice';

  @override
  String get inboxAttachment => 'Attachment';

  @override
  String get inboxAttachments => '첨부파일';

  @override
  String get attachments => '첨부파일';

  @override
  String get inboxAddAttachment => '파일 첨부';

  @override
  String get addAttachment => '파일 첨부';

  @override
  String get inboxDownloadAttachment => 'Download Attachment';

  @override
  String get downloadAttachment => 'Download Attachment';

  @override
  String get inboxNoMessages => '메시지가 없습니다.';

  @override
  String get noMessages => '메시지가 없습니다.';

  @override
  String get inboxNoMessageSelected => '메시지를 선택해주세요.';

  @override
  String get noMessageSelected => '메시지를 선택해주세요.';

  @override
  String inboxUnreadCount(Object count) {
    return '$count unread';
  }

  @override
  String unreadCount(Object count) {
    return '$count unread';
  }

  @override
  String get inboxSearchPlaceholder => 'Search messages...';

  @override
  String get searchPlaceholder => '아이디 또는 이름 속성으로 검색...';

  @override
  String get inboxThread => 'Conversation History';

  @override
  String get thread => 'Conversation History';

  @override
  String inboxThreadCount(Object count) {
    return '$count conversations';
  }

  @override
  String threadCount(Object count) {
    return '$count conversations';
  }

  @override
  String get inboxCancel => '취소';

  @override
  String get inboxClose => '닫기';

  @override
  String get inboxRecallMessage => '발송 취소';

  @override
  String get recallMessage => '발송 취소';

  @override
  String get inboxRecallConfirm =>
      '수신자가 읽지 않은 메시지에 대해 발송을 취소하시겠습니까? (이미 읽은 사용자의 메시지는 회수되지 않습니다)';

  @override
  String get recallConfirm =>
      '수신자가 읽지 않은 메시지에 대해 발송을 취소하시겠습니까? (이미 읽은 사용자의 메시지는 회수되지 않습니다)';

  @override
  String get inboxRecallSuccess => '발송 취소 처리가 완료되었습니다.';

  @override
  String get recallSuccess => '발송 취소 처리가 완료되었습니다.';

  @override
  String inboxRecallResultTotal(Object count) {
    return '전체 수신자: $count명';
  }

  @override
  String recallResultTotal(Object count) {
    return '전체 수신자: $count명';
  }

  @override
  String inboxRecallResultBeforeRead(Object count) {
    return '읽기 전 즉시 회수: $count명';
  }

  @override
  String recallResultBeforeRead(Object count) {
    return '읽기 전 즉시 회수: $count명';
  }

  @override
  String inboxRecallResultAfterRead(Object count) {
    return '이미 읽음 (회수 불가): $count명';
  }

  @override
  String recallResultAfterRead(Object count) {
    return '이미 읽음 (회수 불가): $count명';
  }

  @override
  String inboxRecallResultExternal(Object count) {
    return '외부 이메일 (발송 취소 불가): $count명';
  }

  @override
  String recallResultExternal(Object count) {
    return '외부 이메일 (발송 취소 불가): $count명';
  }

  @override
  String get inboxRead => '읽음';

  @override
  String get read => '읽음';

  @override
  String get inboxUnread => '안읽음';

  @override
  String get unread => '안읽음';

  @override
  String get inboxRecalled => '발송취소됨';

  @override
  String get recalled => '발송취소됨';

  @override
  String get inboxRecipientRecallStatusBefore => '회수 완료 (읽기 전 삭제)';

  @override
  String get recipientRecallStatusBefore => '회수 완료 (읽기 전 삭제)';

  @override
  String inboxRecipientRecallStatusAfter(Object time) {
    return '회수 불가 (읽음: $time)';
  }

  @override
  String recipientRecallStatusAfter(Object time) {
    return '회수 불가 (읽음: $time)';
  }

  @override
  String get inboxRecipientRecallStatusExternal => '외부 이메일 (회수 불가)';

  @override
  String get recipientRecallStatusExternal => '외부 이메일 (회수 불가)';

  @override
  String get inboxDragDropHint => '파일을 여기로 드래그하거나 클릭하여 첨부하세요';

  @override
  String get dragDropHint => '파일을 여기로 드래그하거나 클릭하여 첨부하세요';

  @override
  String get inboxDropOrClickFiles => '파일을 여기로 드래그하거나 클릭하여 첨부하세요';

  @override
  String get dropOrClickFiles => '파일을 여기로 드래그하거나 클릭하여 첨부하세요';

  @override
  String get inboxFileSizeLimit => '최대 50MB까지 첨부 가능 (발송 시 자동 업로드)';

  @override
  String get fileSizeLimit => '최대 50MB까지 첨부 가능 (발송 시 자동 업로드)';

  @override
  String get inboxUploadReady => '발송 시 업로드';

  @override
  String get uploadReady => '발송 시 업로드';

  @override
  String get inboxUploadInProgress => '업로드 중...';

  @override
  String get uploadInProgress => '업로드 중...';

  @override
  String get inboxUploadSuccess => '업로드 완료';

  @override
  String get uploadSuccess => '업로드 완료';

  @override
  String get inboxUploadFailed => '업로드 실패';

  @override
  String get uploadFailed => '업로드 실패';

  @override
  String get inboxUploadRetry => '재시도';

  @override
  String get uploadRetry => '재시도';

  @override
  String inboxTotalFilesSummary(Object count, Object size) {
    return '총 $count개 파일 ($size)';
  }

  @override
  String totalFilesSummary(Object count, Object size) {
    return '총 $count개 파일 ($size)';
  }

  @override
  String get inboxAttachFilesBtn => '파일 첨부';

  @override
  String get attachFilesBtn => '파일 첨부';

  @override
  String get inboxClearAllAttachments => '전체 삭제';

  @override
  String get clearAllAttachments => '전체 삭제';

  @override
  String get inboxNoAttachments => '첨부된 파일이 없습니다.';

  @override
  String get noAttachments => '첨부된 파일이 없습니다.';

  @override
  String get inboxLoading => '불러오는 중...';

  @override
  String get loading => '불러오는 중...';

  @override
  String get inboxEmptyFolder => '이 폴더는 비어 있습니다.';

  @override
  String get emptyFolder => '이 폴더는 비어 있습니다.';

  @override
  String inboxUnreadBadge(Object count) {
    return '$count개 안읽음';
  }

  @override
  String unreadBadge(Object count) {
    return '$count개 안읽음';
  }

  @override
  String get inboxAllRead => '모두 읽음';

  @override
  String get allRead => '모두 읽음';

  @override
  String get inboxDetails => '상세 정보';

  @override
  String get inboxRecipientsList => '수신자 목록';

  @override
  String get recipientsList => '수신자 목록';

  @override
  String get inboxViewAllRecipients => '수신자 전체 보기';

  @override
  String get viewAllRecipients => '수신자 전체 보기';

  @override
  String get inboxTo => '받는 사람';

  @override
  String get to => '받는 사람';

  @override
  String get inboxCc => '참조';

  @override
  String get cc => '참조';

  @override
  String get inboxBcc => '숨은 참조';

  @override
  String get bcc => '숨은 참조';

  @override
  String get inboxFrom => '보낸 사람';

  @override
  String get from => '보낸 사람';

  @override
  String get inboxAt => '일시';

  @override
  String get at => '일시';

  @override
  String get inboxAttachmentDownload => '다운로드';

  @override
  String get attachmentDownload => '다운로드';

  @override
  String get inboxAttachmentDownloadAll => '전체 다운로드';

  @override
  String get attachmentDownloadAll => '전체 다운로드';

  @override
  String get inboxViewModeSplit => '좌우 분할 보기';

  @override
  String get viewModeSplit => '좌우 분할 보기';

  @override
  String get inboxViewModeList => '목록 전용 보기 (클릭 시 팝업)';

  @override
  String get viewModeList => '목록 전용 보기 (클릭 시 팝업)';

  @override
  String get inboxViewMode => '보기 방식';

  @override
  String get viewMode => '보기 방식';

  @override
  String get inboxMessageDetailModal => '메시지 상세 조회';

  @override
  String get messageDetailModal => '메시지 상세 조회';

  @override
  String get inboxDragToResize => '드래그하여 크기 조절 (더블클릭 시 초기화)';

  @override
  String get dragToResize => '드래그하여 크기 조절 (더블클릭 시 초기화)';

  @override
  String get inboxNewMessageReceived => '새 쪽지/메일이 도착했습니다.';

  @override
  String get newMessageReceived => '새 쪽지/메일이 도착했습니다.';

  @override
  String get inboxOriginalMessage => '원본 메시지';

  @override
  String get originalMessage => '원본 메시지';

  @override
  String get inboxComposeMemoApproval => '메모 결재 상신';

  @override
  String get composeMemoApproval => '메모 결재 상신';

  @override
  String get inboxMemoApprovalTitle => '메모 결재 상신';

  @override
  String get memoApprovalTitle => '메모 결재 상신';

  @override
  String get inboxMemoApprovalDesc => '웹 에디터로 기안을 작성하고 결재선을 설정하여 결재를 상신합니다.';

  @override
  String get memoApprovalDesc => '웹 에디터로 기안을 작성하고 결재선을 설정하여 결재를 상신합니다.';

  @override
  String get inboxApprovalLine => '결재선';

  @override
  String get inboxApprovalRouteSetting => '결재선 설정';

  @override
  String get approvalRouteSetting => '결재선 설정';

  @override
  String get inboxDrafter => '기안';

  @override
  String get drafter => '기안';

  @override
  String get inboxApprovalType => '결재 구분';

  @override
  String get approvalType => '결재 구분';

  @override
  String get inboxTypeApproval => '결재';

  @override
  String get inboxTypeConsensus => '합의';

  @override
  String get typeConsensus => '합의';

  @override
  String get inboxTypeNotification => '통보';

  @override
  String get typeNotification => '통보';

  @override
  String get inboxParallelApproval => '병렬 결재';

  @override
  String get parallelApproval => '병렬 결재';

  @override
  String get inboxParallelConsensus => '병렬 합의';

  @override
  String get parallelConsensus => '병렬 합의';

  @override
  String get inboxStepOrder => '차수';

  @override
  String get stepOrder => '차수';

  @override
  String inboxStepOrderLabel(Object order) {
    return '$order차';
  }

  @override
  String stepOrderLabel(Object order) {
    return '$order차';
  }

  @override
  String get inboxAddStep => '결재 단계 추가';

  @override
  String get addStep => '결재 단계 추가';

  @override
  String get inboxAddParallelStep => '병렬 추가';

  @override
  String get addParallelStep => '병렬 추가';

  @override
  String get inboxMoveUp => '위로 이동';

  @override
  String get moveUp => '위로 이동';

  @override
  String get inboxMoveDown => '아래로 이동';

  @override
  String get moveDown => '아래로 이동';

  @override
  String get inboxDeleteStep => '삭제';

  @override
  String get deleteStep => '삭제';

  @override
  String get inboxObservers => '통보 대상자';

  @override
  String get inboxAddObserver => '통보자 추가';

  @override
  String get addObserver => '통보자 추가';

  @override
  String get inboxObserversDesc => '결재 완료 시점에 완료 결과가 통보됩니다.';

  @override
  String get observersDesc => '결재 완료 시점에 완료 결과가 통보됩니다.';

  @override
  String get inboxSubmitApproval => '결재 상신';

  @override
  String get submitApproval => '결재 상신';

  @override
  String get inboxSubmitApprovalSuccess => '메모 결재가 상신되었습니다.';

  @override
  String get submitApprovalSuccess => '메모 결재가 상신되었습니다.';

  @override
  String get inboxSubmitApprovalFailed => '메모 결재 상신에 실패하였습니다.';

  @override
  String get submitApprovalFailed => '메모 결재 상신에 실패하였습니다.';

  @override
  String get inboxApprovalStatus => '결재 상태';

  @override
  String get approvalStatus => '결재 상태';

  @override
  String get inboxApprovalLineEmpty => '결재선을 1명 이상 설정해주세요.';

  @override
  String get approvalLineEmpty => '결재선을 1명 이상 설정해주세요.';

  @override
  String get inboxApprove => '승인';

  @override
  String get inboxReject => '반려';

  @override
  String get inboxConsensusAgree => '합의';

  @override
  String get consensusAgree => '합의';

  @override
  String get inboxApprovalComment => '결재 의견';

  @override
  String get approvalComment => '결재 의견';

  @override
  String get inboxRejectReason => '반려 사유';

  @override
  String get rejectReason => '반려 사유';

  @override
  String get inboxRejectReasonRequired => '반려 사유를 입력해주세요.';

  @override
  String get rejectReasonRequired => '반려 사유를 입력해주세요.';

  @override
  String get inboxApproveSuccess => '결재가 승인되었습니다.';

  @override
  String get approveSuccess => '결재가 승인되었습니다.';

  @override
  String get inboxRejectSuccess => '결재가 반려되었습니다.';

  @override
  String get rejectSuccess => '거절 처리되었습니다.';

  @override
  String get inboxApprovalActionFailed => '결재 처리에 실패하였습니다.';

  @override
  String get approvalActionFailed => '결재 처리에 실패하였습니다.';

  @override
  String get inboxMemoContent => '기안 본문';

  @override
  String get memoContent => '기안 본문';

  @override
  String get inboxCancelApproval => '상신 취소';

  @override
  String get cancelApproval => '상신 취소';

  @override
  String get inboxCancelApprovalConfirm =>
      '이 결재 건의 상신을 취소하시겠습니까? 진행 중인 결재선이 모두 취소됩니다.';

  @override
  String get cancelApprovalConfirm =>
      '이 결재 건의 상신을 취소하시겠습니까? 진행 중인 결재선이 모두 취소됩니다.';

  @override
  String get inboxCancelApprovalReason => '취소 사유';

  @override
  String get cancelApprovalReason => '취소 사유';

  @override
  String get inboxCancelApprovalReasonPlaceholder => '상신 취소 사유를 입력하세요 (선택)';

  @override
  String get cancelApprovalReasonPlaceholder => '상신 취소 사유를 입력하세요 (선택)';

  @override
  String get inboxCancelApprovalSuccess => '결재 상신이 취소되었습니다.';

  @override
  String get cancelApprovalSuccess => '결재 상신이 취소되었습니다.';

  @override
  String get inboxCancelApprovalFailed => '상신 취소 처리에 실패하였습니다.';

  @override
  String get cancelApprovalFailed => '상신 취소 처리에 실패하였습니다.';

  @override
  String get inboxStatusCancelled => '상신 취소';

  @override
  String get selectNodeToViewRecords =>
      '좌측 트리에서 분류 노드를 선택하면 마스터 레코드 목록이 조회됩니다.';

  @override
  String get diffDetails => '변경 내역 상세';

  @override
  String get advancedSearch => '상세 검색';

  @override
  String get advancedSearchCondition => '상세 검색 조건 (Advanced Search)';

  @override
  String get noFieldsToExport => '내보낼 필드 정의가 존재하지 않습니다.';

  @override
  String get downloadTemplateSuccess => '템플릿 다운로드가 완료되었습니다.';

  @override
  String get downloadTemplateFailed => '템플릿 다운로드에 실패했습니다.';

  @override
  String get deleteRow => '행 삭제';

  @override
  String get emptyTableData => '등록된 데이터 행이 없습니다. \'+ 행 추가\' 버튼을 눌러 데이터를 입력하세요.';

  @override
  String get clearAllRows => '전체 행 삭제';

  @override
  String totalRowsCount(Object count) {
    return '총 $count건';
  }

  @override
  String get masterDataRecordList => '마스터 데이터 레코드 목록';

  @override
  String get resetAll => '전체 초기화';

  @override
  String get createRequest => '신규 등록 요청';

  @override
  String get createRecord => '레코드 생성';

  @override
  String get bulkUpload => '대량 업로드';

  @override
  String appliedFiltersCount(Object count) {
    return '적용 필터 $count개';
  }

  @override
  String get targetTypeRecord => '마스터 레코드';

  @override
  String get rollbackRecord => '과거 버전 복원 (Rollback)';

  @override
  String get rollbackBtn => '이 버전으로 복원';

  @override
  String get rollbackConfirmTitle => '레코드 버전 복원 확인';

  @override
  String rollbackConfirmDesc(Object version) {
    return 'Version $version 시점의 데이터로 복원 결재 요청을 생성합니다. 복원 사유를 입력하세요.';
  }

  @override
  String get rollbackReason => '복원 사유';

  @override
  String get rollbackReasonPlaceholder =>
      '데이터 롤백 사유를 입력해 주세요 (예: 오입력 수정, 이전 버전 원복 등)';

  @override
  String rollbackSuccess(Object version) {
    return '버전 $version 롤백 요청이 성공적으로 접수되었습니다.';
  }

  @override
  String get rollbackDiffPreview => '복원 전후 데이터 변경점 비교';

  @override
  String get currentData => '현재 데이터 (Current)';

  @override
  String targetVersionData(Object version) {
    return '복원 대상 데이터 (Version $version)';
  }

  @override
  String get asyncExport => '대용량 Export';

  @override
  String get dataLineageTitle => '마스터 데이터 생애주기 & 계보 (Data Lineage)';

  @override
  String warningDeleteRecords(Object count) {
    return '필드 삭제 시 $count개 활성 레코드의 해당 속성 데이터가 영구 파기됩니다.';
  }

  @override
  String get warningDeleteRecordsZero =>
      '현재 등록된 레코드 중 해당 필드 값을 보유한 활성 데이터가 0건입니다.';

  @override
  String get warningModifyRecords => '변경하려는 구조/타입에 따라 기존 데이터 형변환 유효성을 확인하세요.';

  @override
  String get warningModifyRecordsZero =>
      '현재 영향 받는 실데이터 레코드가 0건으로 구조 변경에 따른 데이터 손실 위험이 없습니다.';

  @override
  String get targetTypeRECORD => '마스터 레코드 신규 등록';

  @override
  String get targetTypeRECORDUPDATE => '마스터 레코드 수정';

  @override
  String get targetTypeRECORDDELETE => '마스터 레코드 삭제';

  @override
  String get targetTypeRECORDMERGE => '마스터 레코드 병합';

  @override
  String get recordIdAttr => 'ID 속성';

  @override
  String get recordNameAttr => '이름 속성';

  @override
  String get affectedRecordsBreakdown => '영향 받는 실데이터 샘플 목록 (Breakdown)';

  @override
  String get asyncExportTitle => '대용량 마스터 데이터 비동기 내보내기';

  @override
  String get asyncExportDesc =>
      '선택한 도메인의 전체 마스터 데이터를 백그라운드 비동기 작업으로 Excel 파일로 내보냅니다.';

  @override
  String get excelViewerTitle => '메신저 엑셀 전용 뷰어';

  @override
  String get excelViewerBtn => '뷰어';

  @override
  String get excelViewerOpen => '엑셀 전용 뷰어 열기';

  @override
  String get searchInExcel => '시트 내 검색...';

  @override
  String get excelLoading => '엑셀 워크시트 데이터를 로딩 및 파싱 중입니다...';

  @override
  String get noExcelData => '표시할 엑셀 셀 데이터가 없습니다.';

  @override
  String get excelCopyTip =>
      '셀 클릭/더블클릭 복사 또는 우측 버튼으로 [엑셀 데이터 표] 형태 그대로 클립보드에 복사하여 활용할 수 있습니다.';

  @override
  String get copyAsExcelText => '📋 엑셀 텍스트(표)로 복사';

  @override
  String get tableRecordCount => '총 데이터 건수';

  @override
  String get copyTableExcel => '📋 엑셀 표 데이터 전체 복사';

  @override
  String get excelSpreadsheetViewerTitle => 'MS Excel 스프레드시트 데이터 뷰어';

  @override
  String get copyRawTableExcel => '🔑 원본(RAW) 표 전체 복사';

  @override
  String get excelModalTip =>
      '마우스 드래그, Shift, Ctrl 키를 이용해 여러 셀을 멀티 선택할 수 있으며 Ctrl+C 로 엑셀에 바로 붙여넣기 할 수 있습니다.';

  @override
  String excelCellsSelected(Object count) {
    return '$count개 셀 선택됨';
  }

  @override
  String excelCellCopied(Object address, Object value) {
    return '📋 [$address] 셀 데이터 \"$value\" 복사 완료!';
  }

  @override
  String excelRangeCopied(Object count) {
    return '📋 $count개 선택 영역 데이터 복사 완료! (엑셀에 Ctrl+V 붙여넣기 가능)';
  }

  @override
  String excelTableCopied(Object rows) {
    return '📋 전체 $rows행 엑셀 표 데이터 복사 완료! (엑셀에 Ctrl+V 붙여넣기 가능)';
  }

  @override
  String get startExport => '내보내기 시작';

  @override
  String get exportProgress => '진행 상태';

  @override
  String get affectedRecords => '영향받는 데이터';

  @override
  String get activeRecords => '활성 레코드';

  @override
  String get baselineRecord => '기준 레코드:';

  @override
  String get btnCheckDuplicate => '중복 확인';

  @override
  String get compareRecords => '레코드 비교';

  @override
  String get compareRecordsTitle => '선택 레코드 비교';

  @override
  String get unmergePreviewTitle => '골든 레코드 분리(Unmerge) 사전 확인';

  @override
  String get unmergeWarningDesc =>
      '골든 레코드 통합을 해제하면 통합 전 각 소스 시스템의 원래 레코드 상태로 복원됩니다.';

  @override
  String get currentGoldenRecord => '현재 골든 레코드';

  @override
  String restoringRecordsCount(Object count) {
    return '복원 예정인 소스 레코드 ($count건)';
  }

  @override
  String get unmergeConfirmBtn => '분리 확정';

  @override
  String get sourceRecord => '소스 레코드';

  @override
  String get unnamedRecord => '이름 없는 레코드';

  @override
  String get duplicateRequestWarning => '이미 신청중인 도메인은 중복 신청이 제외되었습니다.';

  @override
  String get errorFieldDomainMismatch => '필드가 지정된 도메인에 속하지 않습니다.';

  @override
  String get errorFieldNodeMismatch => '필드가 지정된 노드에 속하지 않습니다.';

  @override
  String get errorNodeDomainMismatch => '노드가 지정된 도메인에 속하지 않습니다.';

  @override
  String get errorSectorDomainMismatch => '섹터가 지정된 도메인에 속하지 않습니다.';

  @override
  String get exportExcel => '엑셀 내보내기';

  @override
  String get goToMatchReview => '매칭 검토 이동';

  @override
  String get recordVersionNode => '변경 이력';

  @override
  String get masterRecordNode => '마스터 레코드';

  @override
  String get recordsCountSuffix => '건';

  @override
  String get secondaryNodesTab => '다축/보조 노드';

  @override
  String get masterData => '마스터 레코드';

  @override
  String get lineageNodeMasterDesc =>
      '마스터 레코드: 여러 이력을 거쳐 최종 통합 관리되고 있는 최신 Golden Record';

  @override
  String get installRequirePwdMatch => '비밀번호가 일치하지 않습니다.';

  @override
  String get managedMasterRecords => '관리 중인 마스터 레코드';

  @override
  String get matchReviewTitle => '매칭 검토';

  @override
  String get matchReviewDesc =>
      '유사도가 높은 매칭 후보를 검토하여 마스터 데이터 병합 승인 또는 거절 처리를 진행합니다.';

  @override
  String get matchReviewDomainSelect => '도메인 선택';

  @override
  String get domainSelect => '도메인 선택';

  @override
  String get matchReviewRefresh => '새로고침';

  @override
  String get matchReviewBatchConfirm => '일괄 승인';

  @override
  String get batchConfirm => '일괄 승인';

  @override
  String get matchReviewBatchReject => '일괄 거절';

  @override
  String get batchReject => '일괄 거절';

  @override
  String get matchReviewExistingRecord => '기존 마스터 레코드';

  @override
  String get existingRecord => '기존 마스터 레코드';

  @override
  String get matchReviewIncomingData => '신규 유입 레코드';

  @override
  String get incomingData => '신규 유입 레코드';

  @override
  String get matchReviewRejectNew => '거절';

  @override
  String get rejectNew => '거절';

  @override
  String get matchReviewConfirmMerge => '병합 검토';

  @override
  String get matchReviewStatusPending => '검토 대기';

  @override
  String get matchReviewStatusConfirmed => '병합 완료';

  @override
  String get statusConfirmed => '병합 완료';

  @override
  String get matchReviewStatusRejected => '거절됨';

  @override
  String get matchReviewSimilarityScore => '유사도 점수';

  @override
  String get similarityScore => '유사도 점수';

  @override
  String get matchReviewStatusFilter => '상태';

  @override
  String get matchReviewRejectSuccess => '거절 처리되었습니다.';

  @override
  String get matchReviewRejectFail => '거절 처리에 실패했습니다.';

  @override
  String get rejectFail => '거절 처리에 실패했습니다.';

  @override
  String get matchReviewConfirmSuccess => '승인 처리되었습니다.';

  @override
  String get confirmSuccess => '승인 처리되었습니다.';

  @override
  String get matchReviewConfirmFail => '승인 처리에 실패했습니다.';

  @override
  String get confirmFail => '승인 처리에 실패했습니다.';

  @override
  String get matchingRulesTitle => '매칭 규칙 관리';

  @override
  String get matchingRulesDesc =>
      '도메인별 중복 레코드 판별을 위한 EXACT / FUZZY 매칭 규칙 및 유사도 임계값을 설정합니다.';

  @override
  String get msgPasswordMismatch => '비밀번호가 일치하지 않습니다.';

  @override
  String get noDomainRecords => '등록된 마스터 레코드가 없습니다.';

  @override
  String get pendingMatchCandidates => '매칭 검토 대기';

  @override
  String get permMasterManagement => '세부 권한 마스터 관리';

  @override
  String get permMasterTitle => '세부 권한 마스터 그룹 목록';

  @override
  String get potentialDuplicates => '중복 가능성 추정 건';

  @override
  String get recordCountUnit => '개 레코드';

  @override
  String get recordCreationTrends => '레코드 생성 추이';

  @override
  String get recordSaveFailed => '입력 데이터가 없습니다.';

  @override
  String get records => '데이터 레코드';

  @override
  String get recordsManagement => '마스터 데이터 레코드 관리';

  @override
  String get recordsManagementDesc =>
      '도메인별 마스터 데이터 레코드를 조회, 신규 생성, 일괄 변경 및 서바이버십 병합을 진행합니다.';

  @override
  String get roleRecordManager => '레코드 관리자';

  @override
  String get selectRecordDoubleclick => '원하시는 레코드를 목록에서 더블 클릭하여 선택해주세요';

  @override
  String get exportRoles => '역할 템플릿 내보내기';

  @override
  String get importRoles => '역할 템플릿 불러오기';

  @override
  String get exportRolesSuccess => '역할 템플릿이 성공적으로 다운로드되었습니다.';

  @override
  String get exportRolesFail => '역할 템플릿 다운로드에 실패했습니다.';

  @override
  String get importRolesSuccess => '역할 템플릿이 성공적으로 적용되었습니다.';

  @override
  String get importRolesFail => '역할 템플릿 적용에 실패했습니다.';

  @override
  String get importRolesConfirm =>
      '업로드한 백업 파일의 역할 및 권한 설정으로 기존 조직의 권한을덮어쓰시겠습니까?';

  @override
  String get removeFilter => '필터 삭제';

  @override
  String get excelUploaderTitle => 'Excel 데이터 일괄 업로드';

  @override
  String get excelUploaderStep1 => '1. 파일 업로드';

  @override
  String get step1 => '1. 파일 업로드';

  @override
  String get excelUploaderStep2 => '2. 컬럼 매핑';

  @override
  String get step2 => '2. 컬럼 매핑';

  @override
  String get excelUploaderStep3 => '3. 사전 검증 리포트';

  @override
  String get step3 => '3. 사전 검증 리포트';

  @override
  String get excelUploaderStep4 => '4. 데이터 처리';

  @override
  String get step4 => '4. 데이터 처리';

  @override
  String get excelUploaderDownloadTemplate => '엑셀 템플릿 다운로드';

  @override
  String get excelUploaderDragDropFile => 'Excel 파일을 이곳으로 드래그하거나 클릭하여 선택하세요';

  @override
  String get dragDropFile => 'Excel 파일을 이곳으로 드래그하거나 클릭하여 선택하세요';

  @override
  String get excelUploaderSupportedFormats => '지원 포맷: .xlsx, .xls';

  @override
  String get supportedFormats => '지원 포맷: .xlsx, .xls';

  @override
  String get excelUploaderSelectedFile => '선택된 파일';

  @override
  String get selectedFile => '선택된 파일';

  @override
  String get excelUploaderReselectFile => '파일 다시 선택';

  @override
  String get reselectFile => '파일 다시 선택';

  @override
  String get excelUploaderParsingExcel => '엑셀 파일 분석 중...';

  @override
  String get parsingExcel => '엑셀 파일 분석 중...';

  @override
  String get excelUploaderSourceColumn => '엑셀 컬럼명';

  @override
  String get sourceColumn => '엑셀 컬럼명';

  @override
  String get excelUploaderSampleData => '샘플 데이터';

  @override
  String get sampleData => '샘플 데이터';

  @override
  String get excelUploaderTargetField => '매핑할 시스템 필드';

  @override
  String get excelUploaderAutoMapped => '자동 매핑 완료';

  @override
  String get autoMapped => '자동 매핑 완료';

  @override
  String get excelUploaderManualMapping => '수동 매핑 필요';

  @override
  String get manualMapping => '수동 매핑 필요';

  @override
  String get excelUploaderIgnoreColumn => '-- 매핑 제외 --';

  @override
  String get ignoreColumn => '-- 매핑 제외 --';

  @override
  String get excelUploaderRowValidating => '행 단위 DQ 검증 중...';

  @override
  String get rowValidating => '행 단위 DQ 검증 중...';

  @override
  String get excelUploaderAllRowsValid => '모든 행이 DQ 검증을 통과했습니다!';

  @override
  String get allRowsValid => '모든 행이 DQ 검증을 통과했습니다!';

  @override
  String excelUploaderViolationsFound(Object count) {
    return '$count건의 행에서 DQ 위반이 발견되었습니다.';
  }

  @override
  String violationsFound(Object count) {
    return '$count건의 행에서 DQ 위반이 발견되었습니다.';
  }

  @override
  String excelUploaderValidationSummary(
    Object total,
    Object valid,
    Object invalid,
  ) {
    return '전체 $total행 중 통과 $valid행 · 실패 $invalid행';
  }

  @override
  String validationSummary(Object total, Object valid, Object invalid) {
    return '전체 $total행 중 통과 $valid행 · 실패 $invalid행';
  }

  @override
  String get excelUploaderShowOnlyErrors => '위반 행만 보기';

  @override
  String get showOnlyErrors => '위반 행만 보기';

  @override
  String get excelUploaderColRow => '행';

  @override
  String get colRow => '행';

  @override
  String get excelUploaderColResult => '결과';

  @override
  String get colResult => '결과';

  @override
  String get excelUploaderColViolatedField => '위반 필드';

  @override
  String get colViolatedField => '위반 필드';

  @override
  String get excelUploaderColSeverity => '심각도';

  @override
  String get colSeverity => '심각도';

  @override
  String get excelUploaderColViolationReason => '위반 사유';

  @override
  String get colViolationReason => '위반 사유';

  @override
  String get excelUploaderColInputValue => '입력값';

  @override
  String get colInputValue => '입력값';

  @override
  String excelUploaderProcessing(Object percent) {
    return '데이터 처리 중... $percent%';
  }

  @override
  String get excelUploaderBtnCancel => '취소';

  @override
  String get excelUploaderBtnValidateUpload => '검증 후 업로드';

  @override
  String get btnValidateUpload => '검증 후 업로드';

  @override
  String get excelUploaderBtnEditMapping => '← 매핑 수정';

  @override
  String get btnEditMapping => '← 매핑 수정';

  @override
  String get excelUploaderBtnStartUpload => '업로드 시작';

  @override
  String get btnStartUpload => '업로드 시작';

  @override
  String excelUploaderBtnUploadValidOnly(Object count) {
    return '유효한 $count행만 업로드';
  }

  @override
  String btnUploadValidOnly(Object count) {
    return '유효한 $count행만 업로드';
  }

  @override
  String get excelUploaderBtnDone => '완료';

  @override
  String get btnDone => '완료';

  @override
  String get excelUploaderTooltipValidOnly => '위반 행은 제외하고 유효한 행만 업로드합니다';

  @override
  String get tooltipValidOnly => '위반 행은 제외하고 유효한 행만 업로드합니다';

  @override
  String reclassifySuccess(Object count) {
    return '총 $count건의 레코드가 성공적으로 재분류되었습니다.';
  }

  @override
  String reclassifyPartialFailed(Object success, Object failure) {
    return '$success건 성공, $failure건 실패하였습니다.';
  }

  @override
  String get selectTargetNodePlaceholder => '이동할 분류 노드를 선택하세요';

  @override
  String get bulkImport => '대량 레코드 일괄 업로드 (Bulk Import)';

  @override
  String get bulkImportDesc => 'CSV 또는 JSON 형식의 대용량 레코드 데이터를 업로드하여 일괄 등록합니다.';

  @override
  String bulkImportSuccess(Object success, Object errors) {
    return '일괄 업로드가 완료되었습니다. (성공: $success건, 실패: $errors건)';
  }

  @override
  String get selectFile => '파일 선택 (.csv / .json)';

  @override
  String get startUpload => '일괄 업로드 시작';

  @override
  String get errorDetails => '실패 행 및 오류 사유';

  @override
  String get rowNumber => '행 번호';

  @override
  String get errorReason => '오류 사유';

  @override
  String get complianceReport => '컴플라이언스 종합 감사 보고서';

  @override
  String get complianceReportDesc =>
      '마스터 레코드의 생성부터 수정, 결재 승인, 민감 데이터 열람, 롤백까지 5대 감사 로그를 시간순으로 통합 추적합니다.';

  @override
  String get auditTimeline => '생애주기 감사 타임라인';

  @override
  String get eventType => '이벤트 유형';

  @override
  String get actor => '수행자';

  @override
  String get eventDetail => '상세 내용';

  @override
  String get timeMachine => '레코드 타임머신 & 버전 Diff 비교';

  @override
  String get timeMachineDesc =>
      '과거 버전부터 현재 버전까지의 변경 이력을 타임라인으로 탐색하고, 두 시점 간의 필드별 변경 내용을 시각적으로 비교합니다.';

  @override
  String get compareVersions => '버전 비교';

  @override
  String get baseVersion => '기준 버전 (Before)';

  @override
  String get targetVersion => '대상 버전 (After)';

  @override
  String get diffAdded => '신규 추가';

  @override
  String get diffModified => '값 변경';

  @override
  String get diffRemoved => '삭제됨';

  @override
  String get diffUnchanged => '동일함';

  @override
  String get dataMasking => '동적 데이터 마스킹 & PII 개인정보 보호';

  @override
  String get dataMaskingDesc =>
      '권한에 따라 주민등록번호, 연락처, 이메일 등의 민감 개인정보(PII)를 실시간으로 자동 마스킹합니다.';

  @override
  String get maskedPreview => '마스킹 적용 상태';

  @override
  String get unmaskedPreview => '마스킹 해제 원본 (권한 보유자)';

  @override
  String get maskedFieldCount => '마스킹 적용 필드 수';

  @override
  String get goldenRecord => '중복 레코드 골든 레코드(Golden Record) 빌더';

  @override
  String get goldenRecordDesc =>
      '중복 의심 레코드들로부터 필드별 최신/신뢰 데이터를 자동 채택하여 최적의 단일 기준 레코드를 조립합니다.';

  @override
  String get candidateRecords => '병합 대상 후보 레코드';

  @override
  String get assembledGoldenData => '조립된 골든 레코드';

  @override
  String get chosenSource => '채택 출처';

  @override
  String get buildPreview => '골든 레코드 생성 시뮬레이션';

  @override
  String get hashChainLedger => '불변 해시체인 감사 원장';

  @override
  String get hashChainDesc =>
      '마스터 데이터 변경 이력에 SHA-256 해시체인을 연결하여 위변조가 불가능한 블록체인 감사 추적을 제공합니다.';

  @override
  String get verifyIntegrity => '원장 무결성 검증';

  @override
  String get chainStatusIntact => '무결성 정상 (위변조 없음)';

  @override
  String get chainStatusCorrupted => '위변조 탐지됨';

  @override
  String get blockIndex => '블록 번호';

  @override
  String get blockHash => '블록 해시 (SHA-256)';

  @override
  String get prevHash => '이전 블록 해시';

  @override
  String get smartQuery => '자연어 기반 스마트 데이터 질의 어시스턴트';

  @override
  String get smartQueryDesc =>
      '복잡한 필터 조건 없이 자연어 문장을 입력하면 도메인 스키마에 맞는 조건식으로 자동 해석하여 레코드를 검색합니다.';

  @override
  String get queryPlaceholder => '예: VIP 등급이면서 서울에 거주하는 고객 데이터 검색';

  @override
  String get parsedFilters => '해석된 검색 조건';

  @override
  String get matchedResults => '조건 부합 검색 결과';

  @override
  String get executeQuery => '스마트 질의 실행';

  @override
  String get businessRules => '복합 조건 비즈니스 룰 DQ 빌더';

  @override
  String get businessRulesDesc =>
      '단순 정규식을 넘어 조건부 IF-THEN 복합 검증 규칙을 정의하고 실시간 위반 데이터를 탐색합니다.';

  @override
  String get conditionExpr => '조건식 (IF)';

  @override
  String get validationExpr => '유효성 검증식 (THEN)';

  @override
  String get evaluateRules => '전체 룰 유효성 평가';

  @override
  String get violationFound => '위반 발견';

  @override
  String get allRulesPassed => '모든 비즈니스 룰을 완벽하게 만족합니다.';

  @override
  String get cdcStream => '실시간 변경 캡처';

  @override
  String get cdcStreamDesc =>
      '마스터 레코드의 생성/수정/삭제 이벤트를 실시간으로 캡처하고 변경 전/후 속성을 비교합니다.';

  @override
  String get cdcOp => '변경 유형';

  @override
  String get activeOffset => '활성 오프셋';

  @override
  String get eventsPerSec => '초당 이벤트 처리량';

  @override
  String get beforePayload => '변경 전';

  @override
  String get afterPayload => '변경 후';

  @override
  String get simulateChange => '변경 이벤트 시뮬레이션';

  @override
  String get aiStructurizer => 'AI 비정형 데이터 정형화 & 레코드 자동 추출기';

  @override
  String get aiStructurizerDesc =>
      '계약서 본문, 영수증 텍스트 등 비정형 텍스트를 입력하면 도메인 스키마에 맞는 필드와 값을 AI로 자동 추출합니다.';

  @override
  String get rawTextPlaceholder =>
      '계약서 내용, 고객 상담 내역, 이메일 본문 등의 비정형 텍스트를 입력하세요.';

  @override
  String get extractFields => 'AI 필드 추출 실행';

  @override
  String get extractedFieldsCount => '추출된 필드';

  @override
  String get overallConfidence => 'AI 종합 신뢰도';

  @override
  String get createRecordFromAi => '추출된 필드로 신규 레코드 생성';

  @override
  String get autonomousCleansing => 'AI 기반 이상치 자율 정제 추천 엔진';

  @override
  String get autonomousCleansingDesc =>
      '통계적 중앙값, 최빈값, 표준 사전 매핑을 분석하여 이상치 데이터를 최적의 값으로 자율 보정 추천합니다.';

  @override
  String get anomalyValue => '이상치 원본';

  @override
  String get recommendedValue => 'AI 추천 교정값';

  @override
  String get cleansingStrategy => '보정 전략';

  @override
  String get applyCleansing => '추천 교정값 일괄 적용';

  @override
  String get cleansingSuccess => '이상치 자율 정제가 성공적으로 완료되었습니다.';

  @override
  String get btnSave => '저장';

  @override
  String get btnClose => '닫기';

  @override
  String get btnEdit => '수정';

  @override
  String get btnDelete => '삭제';

  @override
  String get domainRefModalTitle => '참조 레코드 선택';

  @override
  String get domainRefModalGuide => '원하시는 레코드를 목록에서 더블 클릭하여 선택해 주세요.';

  @override
  String get guide => '원하시는 레코드를 목록에서 더블 클릭하여 선택해 주세요.';

  @override
  String get domainRefModalSearchPlaceholder => '아이디 또는 이름 속성으로 검색...';

  @override
  String get domainRefModalSearchBtn => '검색';

  @override
  String get searchBtn => '검색';

  @override
  String get domainRefModalResetBtn => '초기화';

  @override
  String get resetBtn => '초기화';

  @override
  String domainRefModalTotalCount(Object count) {
    return '총 $count건';
  }

  @override
  String totalCount(Object count) {
    return '총 $count건';
  }

  @override
  String get domainRefModalNoResults => '조회된 레코드가 없습니다.';

  @override
  String get noResults => '조회된 레코드가 없습니다.';

  @override
  String get classificationAxes => 'CLASSIFICATION AXES (다중 축)';

  @override
  String get domainName => '도메인명';

  @override
  String get classificationName => '분류명';

  @override
  String get tableSchemaSettings => '테이블 컬럼 정의 (JSON 서브 스키마)';

  @override
  String get tableSchemaGuide =>
      '💡 복합 테이블 형태로 데이터를 입력받을 서브 컬럼들을 정의하세요. (예: 학력 이력의 학교명, 전공, 졸업일 등)';

  @override
  String get addColumn => '+ 컬럼 추가';

  @override
  String get removeColumn => '컬럼 삭제';

  @override
  String get columnKey => '컬럼 키 (Key)';

  @override
  String get columnNameKo => '컬럼명 (KO)';

  @override
  String get columnNameEn => '컬럼명 (EN)';

  @override
  String get columnType => '컬럼 타입';

  @override
  String get columnOptions =>
      '선택지 (KEY:국문라벨:영문라벨 또는 쉼표 구분: 예: BACHELOR:학사:Bachelor, MASTER:석사:Master)';

  @override
  String get columnOptionsPlaceholder =>
      '예: HIGH:고졸:High School, BACHELOR:학사:Bachelor, MASTER:석사:Master, DOCTOR:박사:Doctor';

  @override
  String get columnWidth => '너비(px)';

  @override
  String get noTableColumnsDefined =>
      '정의된 테이블 컬럼이 없습니다. \'+ 컬럼 추가\' 버튼을 눌러 컬럼을 추가하세요.';

  @override
  String confirmDeleteNode(Object name) {
    return '\'$name\' 노드를 정말 삭제하시겠습니까?';
  }

  @override
  String get nodeDeletedSuccess => '노드가 성공적으로 삭제되었습니다.';

  @override
  String get nodeDeleteFailed => '노드 삭제 중 오류가 발생했습니다.';

  @override
  String addNodeTo(Object name) {
    return '$name에 하위 노드 추가';
  }

  @override
  String get nodeOrder => '정렬 순서';

  @override
  String get idAttribute => 'ID속성';

  @override
  String get nameAttribute => '이름속성';

  @override
  String get schemaImpactTitle => '스키마 변경 영향도 사전 분석 보고서';

  @override
  String get schemaImpactSummary => '스키마 변경 영향 요약';

  @override
  String get fieldDeleteApprovalSubmitted => '필드 삭제 요청이 결재함에 정상적으로 상신되었습니다.';

  @override
  String get fieldDeleteFailed => '필드 삭제 중 오류가 발생했습니다.';

  @override
  String get targetTypeSCHEMAFIELDDELETE => '스키마 필드 삭제';

  @override
  String get targetTypeSCHEMAFIELDADD => '스키마 필드 추가';

  @override
  String get targetTypeSCHEMAFIELDUPDATE => '스키마 필드 변경';

  @override
  String get targetTypeSCHEMANODECREATE => '분류 노드 생성';

  @override
  String get targetTypeSCHEMANODEUPDATE => '분류 노드 변경';

  @override
  String get targetTypeSCHEMANODEMOVE => '분류 노드 이동';

  @override
  String get affectedTargetField => '변경 대상 속성 필드';

  @override
  String get schemaImpactConfirmedTitle => '위험 감수 변경 적용 완료';

  @override
  String get schemaImpactConfirmedMsg =>
      '스키마 변경 영향도를 확인하였으며 위험 감수 변경 사항이 승인 및 적용되었습니다.';

  @override
  String get totalNodes => '총 계보 노드';

  @override
  String get actionTypeSchemaChange => '스키마 변경 (SCHEMA_CHANGE)';

  @override
  String get actionTypeSchemaChangeShort => '스키마 변경';

  @override
  String get addEditableFieldPlaceholder => '+ 작성 가능 필드 추가 선택 (미선택 시 전체)';

  @override
  String addFieldToNode(Object name) {
    return '$name 필드 추가';
  }

  @override
  String get fieldNameKo => '필드명 (국문)';

  @override
  String get fieldNameEn => '필드명 (영문)';

  @override
  String get fieldHintKo => '툴팁 / 힌트 (국문)';

  @override
  String get fieldHintEn => '툴팁 / 힌트 (영문)';

  @override
  String get groupSectorMapped => '속성 그룹 (섹터 자동 매핑)';

  @override
  String get fieldType => '데이터 타입';

  @override
  String get targetDomain => '참조 대상 도메인';

  @override
  String get addHiddenFieldPlaceholder => '+ 화면에서 숨길 필드 추가 선택';

  @override
  String get addNewField => '+ 신규 필드 추가';

  @override
  String get addSchema => '스키마 추가';

  @override
  String get allDomainsSelected => '선택 가능한 모든 도메인이 추가되었습니다.';

  @override
  String auditFieldChanged(Object field) {
    return '$field 변경됨';
  }

  @override
  String get basicFields => '기본 필드';

  @override
  String get classification => '분류';

  @override
  String get classificationTree => '분류체계 트리';

  @override
  String get deleteFieldProp => '🗑️ 필드 삭제';

  @override
  String get deleteSchema => '스키마 삭제';

  @override
  String get deletedFieldProperties => '삭제되는 필드 속성';

  @override
  String get domain => '도메인';

  @override
  String get domainDistributionTitle => '도메인별 마스터 레코드 분포';

  @override
  String get domainPermGroupTitle => '도메인 권한';

  @override
  String get domainSchemaDesc =>
      '분류체계 트리 구조 기반으로 도메인 모델, 속성(Attribute) 및 데이터 타입을 정의합니다.';

  @override
  String get domainSchemaTitle => '도메인 스키마 관리';

  @override
  String get editSchema => '스키마 편집';

  @override
  String get editableFieldsTitle =>
      '🟢 작성 및 DQ 검증 대상 지정 필드 (Editable & DQ Scoped)';

  @override
  String get errorDomainMissingId =>
      '도메인 설정 오류: 식별자(ID) 또는 표시명 필드 매핑이 누락되었습니다.';

  @override
  String get errorSavingDomain => '도메인 저장 중 오류가 발생했습니다.';

  @override
  String fieldKeyAlreadyExistsNewfieldValueKey(Object key) {
    return '이미 존재하는 Field Key 입니다: $key';
  }

  @override
  String get fieldKeyExists => '이미 존재하는 Field Key 입니다';

  @override
  String get fieldPermGroupTitle => '속성 필드 권한';

  @override
  String get grantedDomains => '보유 권한';

  @override
  String get hiddenFieldsTitle => '🔴 숨김 필드 (Hidden Fields)';

  @override
  String get nodeCountSuffix => '개';

  @override
  String get outboundNode => '외부 연계';

  @override
  String get lineageNodeSourceDesc => '소스 시스템: 최초 데이터가 유입/생성된 원출처 시스템';

  @override
  String get lineageNodeHistoryDesc =>
      '변경 이력: 최초 생성(Version 1)부터 정보 변경(Version 2...)까지의 시간순 이력 현황';

  @override
  String get lineageNodeOutboundDesc =>
      '외부 연계: 해당 마스터 레코드가 외부 타 시스템으로 전파된 연계 이력 현황';

  @override
  String get myGrantedDomains => '나의 보유 권한';

  @override
  String get newFieldProperties => '추가되는 신규 필드 속성';

  @override
  String get noDomainData => '도메인 데이터 없음';

  @override
  String get noGrantedDomains => '현재 보유한 권한이 없습니다.';

  @override
  String get noNewDomainsAvailable => '신청 가능한 신규 도메인이 없습니다.';

  @override
  String get noRulesSchemaChange =>
      '규칙이 등록되지 않았습니다. 기본적으로 모든 사용자가 스키마 변경 요청을 신청할 수 있습니다.';

  @override
  String get noSpecificDomainPermissions =>
      '특정 도메인 권한이 없습니다. (ADMIN은 모든 도메인을 볼 수 있습니다)';

  @override
  String get node => '노드';

  @override
  String get deptIcon => '부서 아이콘';

  @override
  String get nodePermGroupTitle => '분류 노드 권한';

  @override
  String get pendingDomainAccessRequests => '도메인 접근 권한 신청 대기 목록';

  @override
  String get pendingSchemaApprovalExists =>
      '현재 진행 중인 스키마 결재 건이 있습니다. 결재 완료 전까지 수정할 수 없습니다.';

  @override
  String get pleaseSelectATargetDomain => '대상 도메인을 선택해주세요.';

  @override
  String get requestDomainAccess => '도메인 권한 신청';

  @override
  String get requestNewDomain => '신규 도메인 권한 신청';

  @override
  String get requestedDomains => '신청 도메인';

  @override
  String get schema => '도메인 스키마';

  @override
  String get schemaApprovalInProgress => '결재 진행 중';

  @override
  String get schemaChangeComparison => '필드 속성 변경 비교 (Before ➔ After)';

  @override
  String get schemaHistoryTitle => '스키마 변경 이력';

  @override
  String get schemaHistoryTargetType => '대상 유형';

  @override
  String get schemaHistoryAction => '변경 유형';

  @override
  String get schemaHistoryChangedBy => '변경자';

  @override
  String get schemaHistoryChangedAt => '변경 시각';

  @override
  String get changedAt => '변경 시각';

  @override
  String get schemaHistoryBefore => '변경 전';

  @override
  String get before => '변경 전';

  @override
  String get schemaHistoryAfter => '변경 후';

  @override
  String get after => '변경 후';

  @override
  String get schemaHistoryField => '필드';

  @override
  String get schemaHistoryNode => '노드';

  @override
  String get schemaHistoryDomainEntity => '도메인';

  @override
  String get domainEntity => '도메인';

  @override
  String get schemaHistoryGroup => '그룹';

  @override
  String get schemaHistoryCreate => '생성';

  @override
  String get schemaHistoryUpdate => '수정';

  @override
  String get schemaHistoryDelete => '삭제';

  @override
  String get schemaHistoryNoHistory => '변경 이력이 없습니다';

  @override
  String get noHistory => '변경 이력이 없습니다';

  @override
  String get schemaHistoryViewChanges => '변경 사항 보기';

  @override
  String get schemaReason => '상신 사유 (의견)';

  @override
  String get schemaReasonPlaceholder => '스키마 변경 사유를 상세히 작성해 주세요.';

  @override
  String get scopeDomain => '도메인 공통';

  @override
  String get scopeNode => '분류 노드 개별';

  @override
  String get selectADomain => '도메인을 선택하세요';

  @override
  String get selectDomainFirst => '도메인을 먼저 선택해주세요.';

  @override
  String get selectNodePlaceholder => '분류 노드 선택';

  @override
  String get selectTargetDomain => '참조할 도메인을 선택해주세요.';

  @override
  String get selectTargetDomainAlert => '적용 도메인을 선택해주세요.';

  @override
  String get tabFields => '필드 관리';

  @override
  String get applyTargetDomain => '적용 도메인 *';

  @override
  String get targetNode => '적용 분류 노드 *';

  @override
  String get totalDomains => '총 도메인 수';

  @override
  String get updateFieldProps => '✏️ 필드 속성 변경';

  @override
  String get waitingForFieldData => '필드 데이터를 기다리는 중...';

  @override
  String get domainBracket => '[도메인]';

  @override
  String get domainLevel => '도메인 공통';

  @override
  String get belongingNodeDomain => '소속 노드 / 도메인 변경 (Belonging Node / Domain)';

  @override
  String get domainCommonField => '도메인 공통 필드';

  @override
  String get highlight => 'Highlight (강조)';

  @override
  String get conditionalFieldControl => 'Conditional Field Control (조건부 연동 설정)';

  @override
  String get enableCondition => '연동 사용 (Enable)';

  @override
  String get conditionMode => '설정 방식:';

  @override
  String get guiMode => '드롭다운 선택 (GUI)';

  @override
  String get expressionMode => '표현식 직접 입력 (Expression)';

  @override
  String get controlAction => '제어 동작:';

  @override
  String get actionShow => '👁️ 조건 충족 시만 노출 (SHOW)';

  @override
  String get actionHighlight => '✨ 조건 충족 시 하이라이트 (HIGHLIGHT)';

  @override
  String get actionRequire => '🔒 조건 충족 시 필수값 지정 (REQUIRE)';

  @override
  String get actionReadOnly => '📖 조건 충족 시 읽기 전용 (READ_ONLY)';

  @override
  String get actionDisable => '🚫 조건 충족 시 수정 금지 (DISABLE)';

  @override
  String get dependsOn => '기준 필드 (Depends On)';

  @override
  String get operator => '연산자';

  @override
  String get dateFormat => 'Date Format (형식 지정)';

  @override
  String get canInputDirectly => '직접 입력 가능';

  @override
  String get schemaPropName => '필드명';

  @override
  String get schemaPropKey => '필드 키';

  @override
  String get schemaPropType => '데이터 타입';

  @override
  String get schemaPropRequired => '필수 여부';

  @override
  String get schemaPropIsSearchable => '검색 가능 여부';

  @override
  String get schemaPropIsMultiValue => '다중값 허용';

  @override
  String get schemaPropIsEncrypted => '암호화 여부';

  @override
  String get schemaPropIsReadOnly => '읽기 전용';

  @override
  String get schemaPropIsHidden => '숨김 여부';

  @override
  String get schemaPropIsImmutable => '수정 불가';

  @override
  String get schemaPropOrder => '정렬 순서';

  @override
  String get schemaPropGroup => '필드 그룹';

  @override
  String get schemaPropUnit => '단위';

  @override
  String get schemaPropId => '식별 ID';

  @override
  String get schemaPropApprovalStatus => '결재 상태';

  @override
  String get schemaPropIsPendingApproval => '결재 대기 여부';

  @override
  String get schemaPropMarkingPattern => '마스킹 패턴';

  @override
  String get schemaPropHint => '입력 힌트';

  @override
  String get schemaPropFieldGroupId => '필드 그룹 ID';

  @override
  String get schemaPropDependsOnFieldKey => '참조 필드 키';

  @override
  String get schemaPropConditionOperator => '연동 연산자';

  @override
  String get schemaPropConditionValue => '연동 기준값';

  @override
  String get schemaPropConditionAction => '연동 제어 동작';

  @override
  String get schemaPropConditionMode => '연동 설정 방식';

  @override
  String get schemaPropConditionEnabled => '조건부 연동 사용 여부';

  @override
  String get schemaPropTrue => '예';

  @override
  String get schemaPropFalse => '아니오';

  @override
  String get governance => '거버넌스';

  @override
  String get dataProfiling => '데이터 프로파일링';

  @override
  String get selectNodePrompt => '좌측 트리에서 분류 노드를 선택하여 필드를 조회하거나 추가하세요.';

  @override
  String get schemaPackage => '도메인 스키마 패키지 (Export / Import)';

  @override
  String get exportPackage => '스키마 패키지 내보내기 (Export)';

  @override
  String get importPackage => '스키마 패키지 가져오기 (Import)';

  @override
  String get packageExportDesc =>
      '현재 도메인의 분류 체계, 필드 정의, DQ 룰, 매칭 룰, 결재 서식을 단일 JSON 파일로 내보냅니다.';

  @override
  String get packageImportDesc =>
      '외부에서 생성된 도메인 스키마 패키지 JSON을 업로드하여 도메인 구조를 일괄 복원 및 생성합니다.';

  @override
  String get exportDownloadJson => '패키지 다운로드 (.json)';

  @override
  String get importUploadJson => '패키지 가져오기 실행';

  @override
  String get overwriteExisting => '기존 동일 도메인 덮어쓰기 (Overwrite)';

  @override
  String get packageExportSubtext =>
      '분류 노드, 필드 정의, DQ 검칙, 중복 매칭 룰, 결재 서식 전체가 패키징됩니다.';

  @override
  String get packagePreviewLabel => '패키지 JSON 미리보기:';

  @override
  String get packageFileSelectLabel => 'JSON 패키지 파일 선택 (.json)';

  @override
  String get packagePreviewInfoLabel => '불러온 패키지 정보:';

  @override
  String get domainLabel => '도메인명';

  @override
  String packageSummaryCounts(Object nodes, Object fields, Object rules) {
    return '분류 노드 $nodes개 · 필드 $fields개 · DQ 룰 $rules개';
  }

  @override
  String get packageDownloadSuccess => '도메인 패키지 다운로드가 완료되었습니다.';

  @override
  String get invalidJsonPackageFile => '올바른 JSON 패키지 파일이 아닙니다.';

  @override
  String get profilingTitle => '도메인 데이터 프로파일링 & 이상치 분석';

  @override
  String get profilingDesc =>
      '도메인 전체 레코드의 필드별 결측률, 유일성 및 IQR 기반 통계적 이상치 레코드를 탐지합니다.';

  @override
  String get nullRate => '결측률';

  @override
  String get uniqueness => '유일성';

  @override
  String get distinctCount => '고유값 수';

  @override
  String outliersFound(Object count) {
    return '이상치 발견: $count건';
  }

  @override
  String get noOutliers => '발견된 이상치 데이터가 없습니다.';

  @override
  String get schemaSimulation => '스키마 변경 영향도 시뮬레이션';

  @override
  String get schemaSimulationDesc =>
      '필드 삭제 및 수정 시 기존 데이터, 연계 채널, DQ 검칙에 미치는 위험도를 사전 진단합니다.';

  @override
  String get safetyScore => '안전 점수';

  @override
  String get runSimulation => '영향도 시뮬레이션 실행';

  @override
  String get simulationRecommendations => '사전 안전 조치 권장사항';

  @override
  String get businessGlossary => '전사 표준 비즈니스 용어 사전';

  @override
  String get businessGlossaryDesc =>
      '전사 표준 용어, 표준 약어 및 개인정보 보호 등급을 정의하고 필드 정의 시 자동 추천합니다.';

  @override
  String get termName => '표준 용어명';

  @override
  String get termCode => '표준 용어 코드';

  @override
  String get abbreviation => '표준 약어';

  @override
  String get synonyms => '동의어/유의어';

  @override
  String get sensitivityLevel => '보안/민감도 등급';

  @override
  String get addTerm => '표준 용어 등록';

  @override
  String get recommendedTerms => '추천 표준 용어';

  @override
  String get domainSnapshot => '도메인 데이터 스냅샷 & 시점 복구';

  @override
  String get domainSnapshotDesc =>
      '도메인 전체 레코드 및 스키마 상태를 특정 시점으로 스냅샷 저장하고 언제든지 원클릭으로 롤백 복구합니다.';

  @override
  String get createSnapshot => '스냅샷 생성';

  @override
  String get snapshotName => '스냅샷 명칭';

  @override
  String get versionTag => '버전 태그';

  @override
  String get restoreSnapshot => '시점 복구';

  @override
  String confirmRestore(Object name, Object tag) {
    return '\'$name\' ($tag) 스냅샷 시점으로 도메인 데이터를 복원하시겠습니까? 현재 데이터가 해당 시점으로 대체됩니다.';
  }

  @override
  String get multilingualSync => '다국어 메타데이터 일괄 번역 & 사전 동기화기';

  @override
  String get multilingualSyncDesc =>
      '도메인 필드 중 다국어(한국어, 영어 등) 정의가 누락된 항목을 전사 비즈니스 용어사전과 매핑하여 일괄 동기화합니다.';

  @override
  String get missingLocalesCount => '다국어 누락 필드';

  @override
  String get missingLangs => '누락 언어';

  @override
  String get suggestedTranslation => '용어사전 추천값';

  @override
  String get allLocalesComplete => '모든 필드의 다국어 정의가 완벽하게 등록되어 있습니다.';

  @override
  String get dataAssetValuation => '전사 데이터 카탈로그 & 자산 가치 평가기';

  @override
  String get dataAssetValuationDesc =>
      '도메인별 데이터 규모, 연계 채널, DQ 품질 지수를 분석하여 데이터 자산 가치 등급 및 원화 환산 가치를 산출합니다.';

  @override
  String get totalAssetValue => '전사 데이터 자산 총액';

  @override
  String get averageQualityScore => '평균 DQ 품질 지수';

  @override
  String get assetRating => '자산 등급';

  @override
  String get estimatedValue => '추정 자산 가치';

  @override
  String get schemaCompatibility => '스키마 하위 호환성 및 브레이킹 체인지 분석기';

  @override
  String get schemaCompatibilityDesc =>
      '필드 타입 변경, 필수값 전환, 필드 삭제 시 기존 외부 API 연계 채널과의 하위 호환성 파괴 위험도를 사전 분석합니다.';

  @override
  String get compatibilityStatus => '하위 호환성 상태';

  @override
  String get riskScore => '호환성 위험 점수';

  @override
  String get checkCompatibility => '호환성 정적 분석 실행';

  @override
  String get breakingChangeDetected => '브레이킹 체인지 감지됨';

  @override
  String get compatibleStatus => '하위 호환 정상';

  @override
  String get semanticOntology => '도메인 간 시맨틱 온톨로지 지식 그래프';

  @override
  String get semanticOntologyDesc =>
      '전사 도메인 간의 시맨틱 관계(구매, 포함, 공급, 관리)를 온톨로지 지식 그래프로 탐색합니다.';

  @override
  String get ontologyNodes => '온톨로지 노드';

  @override
  String get ontologyEdges => '시맨틱 관계 (Edge)';

  @override
  String get searchOntology => '지식 그래프 검색';

  @override
  String get relationType => '관계 유형';

  @override
  String get navTabRecords => '레코드';

  @override
  String get navTabHome => '홈';

  @override
  String get navTabApprovals => '승인';

  @override
  String get navTabNotifications => '알림';

  @override
  String get navTabChat => '채팅';

  @override
  String get homeWelcomeTitle => '거버넌스 포털 대시보드';

  @override
  String get homeTodoTitle => '나의 처리 대기 현황';

  @override
  String get homeRecentActivity => '최근 변경 및 승인 활동';

  @override
  String get homeUnreadMessages => '미안독 채팅 메시지';

  @override
  String get homeNoActivity => '최근 활동 이력이 없습니다.';

  @override
  String get notificationsEmpty => '수신된 새로운 알림이 없습니다.';

  @override
  String get chatCreateRoom => '대화방 만들기';

  @override
  String get chatRoomTitlePlaceholder => '대화방 제목을 입력하세요';

  @override
  String get chatTitle => '실시간 협업 메신저';

  @override
  String get chatEmptyRooms => '참여 중인 대화방이 없습니다.';

  @override
  String get chatSelectMembers => '대화 상대 선택 (1명 이상 필수)';

  @override
  String get chatSearchSelectUser => '사용자 검색/선택';

  @override
  String get chatNoUserSelected => '선택된 사용자가 없습니다.';

  @override
  String chatUserMe(String username) {
    return '$username (나)';
  }

  @override
  String get chatCreateRoomFailed => '대화방 생성에 실패했습니다.';

  @override
  String get chatSearchUserHint => '사용자명, 역할, 부서 검색...';

  @override
  String get chatConfirmBtn => '확인';

  @override
  String get allCategories => '전체 (All)';

  @override
  String get recordData => '레코드 데이터';

  @override
  String get viewReasonTitle => '열람 사유 입력';

  @override
  String get viewReasonHint => '사유를 입력하세요 (예: 본인확인용)';

  @override
  String get viewReasonEmpty => '사유를 입력해야 합니다.';

  @override
  String get decryptSuccessNotice => '복호화가 완료되었습니다. (30초 후 다시 마스킹됩니다)';

  @override
  String get decryptFailedNotice => '복호화 실패:';

  @override
  String get keyInfo => '키 정보';

  @override
  String get generalInfo => '일반정보';

  @override
  String get viewHistory => '이력 보기';

  @override
  String get loginWithKeycloak => 'Keycloak SSO로 로그인';

  @override
  String get loginDividerOr => '또는 일반 계정으로 로그인';

  @override
  String get loginStandard => '일반 계정 로그인';

  @override
  String get loginSsoError => 'SSO 로그인 중 오류가 발생했습니다.';
}
