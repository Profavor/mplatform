// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Korean (`ko`).
class AppLocalizationsKo extends AppLocalizations {
  AppLocalizationsKo([String locale = 'ko']) : super(locale);

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
  String systemJoin(Object name) {
    return '$name 님이 초대되었습니다.';
  }

  @override
  String systemLeave(Object name) {
    return '$name 님이 대화방을 나갔습니다.';
  }

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
  String get statusRejected => '반려';

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
  String get accessReason => '접근 사유';

  @override
  String get accessReasonPlaceholder => '예: 업무 처리, 고객 요청 등';

  @override
  String get accessReasonRequired => '접근 사유를 입력해 주세요.';

  @override
  String get action => '동작';

  @override
  String get actionRequired => '⚠️ 승인 처리 필요';

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
  String get actions => '동작';

  @override
  String get actionsCol => '관리';

  @override
  String get activeStatus => '활성';

  @override
  String get add => '추가';

  @override
  String get addcomment => '코멘트 추가';

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
  String get affectedChannels => '영향 받는 연동 채널';

  @override
  String get afterChange => '변경 후 (New Value)';

  @override
  String get agGridUnifiedList => 'AG-Grid 통합 목록';

  @override
  String get alldone => '모든 결재/합의가 완료되었습니다.';

  @override
  String get allTasksCleared => '✅ 모든 처리 완료';

  @override
  String get approvalDetailTitle => '결재 상세 내역';

  @override
  String get approvalLine => '결재선';

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
  String get backupMenuSeed => '현재 상태 백업';

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
  String get bulkApprove => '일괄 승인';

  @override
  String get bulkApproveLoading => '일괄 승인 처리 중...';

  @override
  String get bulkReject => '일괄 반려';

  @override
  String get bulkRejectLoading => '일괄 반려 처리 중...';

  @override
  String get calculated => '수식';

  @override
  String get calculatedSuffix => '(계산됨)';

  @override
  String get cancel => '취소';

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
  String get changedBy => '작성자 / 변경자';

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
  String get close => '닫기';

  @override
  String get colaction => '작업';

  @override
  String get colclassification => '분류 체계';

  @override
  String get colcreatedat => '기안 일시';

  @override
  String get coldomain => '도메인';

  @override
  String get colidattr => '식별자';

  @override
  String get colnameattr => '명칭';

  @override
  String get colrequester => '기안자';

  @override
  String get colstatus => '상태';

  @override
  String get colsummary => '요약 정보';

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
  String get consensus => '합의';

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
  String get created => '생성 일시';

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
  String get date => '기안일';

  @override
  String get dateTime => '일시';

  @override
  String get decryptFailed => '복호화 실패 (권한을 확인하세요)';

  @override
  String get defaultBadge => '⭐ 기본';

  @override
  String get delete => '삭제';

  @override
  String get deleteErrorTitle => '삭제 오류';

  @override
  String get deleteFailed => '삭제에 실패했습니다.';

  @override
  String get deleteSuccess => '삭제 완료';

  @override
  String get deletedStatus => '삭제됨';

  @override
  String get department => '부서';

  @override
  String get description => '설명';

  @override
  String get descriptionCol => '상세 설명';

  @override
  String get details => '상세 내용';

  @override
  String get detailsInfo => '상세 정보';

  @override
  String get diffCountSuffix => '개 항목 다름';

  @override
  String get doReview => '심사하기';

  @override
  String get domainRecordCreate => '도메인 레코드 등록';

  @override
  String get downloadFile => '완료된 파일 다운로드';

  @override
  String get draft => '기안';

  @override
  String get draftCommentOptional => '(선택사항) 결재권자에게 남길 기안 의견을 작성해주세요';

  @override
  String get draftCommentPlaceholder => '의견을 입력하세요...';

  @override
  String get draftCommentTitle => '기안 의견 작성';

  @override
  String get draftCompleted => '상신완료';

  @override
  String eGAbsKeyAKeyB2100(Object KEY_A, Object KEY_B) {
    return '예: ABS($KEY_A + $KEY_B / 2) * 100';
  }

  @override
  String get edit => '수정';

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
  String get general => '일반';

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
  String get hideOriginal => '원본 숨기기';

  @override
  String get history => '변경이력';

  @override
  String get historyVersionDiffDetail => '이력 버전 변경 항목 상세 비교';

  @override
  String get id => '식별 ID';

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
  String get integrationChannels => '연계 채널 관리';

  @override
  String get integrationDetailTitle => '연동 상세 내역';

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
  String get isActive => '사용 여부';

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
  String get maxValue => '최대값';

  @override
  String get menuAccessStatistics => '메뉴 접근 통계';

  @override
  String get menuIcon => '메뉴 아이콘';

  @override
  String get menuManagement => '메뉴 관리';

  @override
  String get menuManagementDesc => '시스템 전체 트리 메뉴 구조 및 권한별 접근 노드를 관리합니다.';

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
  String get name => '이름';

  @override
  String get newData => '신규 데이터';

  @override
  String get nextSnapshot => '이후 스냅샷';

  @override
  String get nocomment => '의견 없음';

  @override
  String get noparsable => '파싱 가능한 데이터가 없습니다.';

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
  String get noComment => '의견 없음';

  @override
  String get noDiffOrInitialVersion => '변경 사항이 없거나 최초 생성 버전입니다.';

  @override
  String get noDifferencesFound => '선택한 레코드 간 차이점이 없습니다.';

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
  String get notifiedPersons => '통보자(참조)';

  @override
  String get number => '숫자';

  @override
  String get observers => '참조자(CC)';

  @override
  String get onlyDifferences => '차이점만 보기';

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
  String get processed => '처리됨';

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
  String get recordCreate => '마스터 레코드 신규 등록';

  @override
  String get recordDelete => '마스터 레코드 삭제';

  @override
  String get recordUpdate => '마스터 레코드 수정';

  @override
  String get reflectDate => '반영 일시';

  @override
  String get refresh => '새로고침';

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
  String get review => '심사하기';

  @override
  String get riskLevel => '위험도 등급';

  @override
  String get save => '저장';

  @override
  String get saveChanges => '변경사항 저장';

  @override
  String get saveChangesHint =>
      '* 셀 수정 후 상단 또는 하단의 \'저장\' 버튼을 클릭하여 변경사항을 반영하세요.';

  @override
  String get saveFailed => '저장에 실패했습니다.';

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
  String get selectIcon => '아이콘 선택';

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
  String get sortOrder => '정렬 순서';

  @override
  String get startDate => '시작일자';

  @override
  String get status => '상태';

  @override
  String get statusActive => '🟢 사용중';

  @override
  String get statusCol => '상태';

  @override
  String get statusDraft => '임시 저장 (Draft)';

  @override
  String get statusFilter => '상태 필터';

  @override
  String get statusIgnored => '별도 유지';

  @override
  String get statusInactive => '🔴 중지';

  @override
  String get statusMerged => '병합 완료';

  @override
  String get statusPending => '검토 대기';

  @override
  String get statusWaiting => '대기';

  @override
  String get stepdraft => '상신완료';

  @override
  String get steppending => '대기중';

  @override
  String get stepscheduled => '예정';

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
  String get stepType => '스텝 유형';

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
  String get subtitle => '시스템 모니터링 및 세부 관리';

  @override
  String get successDelete => '성공적으로 삭제되었습니다.';

  @override
  String get successSave => '성공적으로 저장되었습니다.';

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
  String get targetType => '대상 유형';

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
  String get title => '결재함';

  @override
  String get toValue => '~ 까지';

  @override
  String get today => '오늘';

  @override
  String get treeEmptyMessage =>
      '분류체계 트리가 없습니다. 하단의 Domain 버튼을 눌러 새 도메인을 생성해주세요.';

  @override
  String get type => '유형';

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
  String get update => '변경';

  @override
  String get updateSuccess => '수정 완료';

  @override
  String get updatedat => '변경일';

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
  String get viewChanges => '변경 내역 보기';

  @override
  String get viewOriginal => '원본 보기';

  @override
  String get viewSnapshot => '스냅샷 보기';

  @override
  String get visualGraph => '비주얼 그래프';

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
  String warningDqRules(Object count) {
    return '해당 필드에 매핑된 $count건의 데이터 품질(DQ) 검칙이 영향받습니다.';
  }

  @override
  String get noAffectedDqRules => '연결된 DQ 검칙 없음';

  @override
  String get expectedDqViolations => '예상 DQ 위반 건수';

  @override
  String get affectedDqRules => '연관 품질(DQ) 검칙';

  @override
  String get addDqRule => '규칙 추가';

  @override
  String get addRule => '+ 규칙 추가';

  @override
  String get dqDashboardDesc =>
      '도메인별 데이터 품질 검칙 이행률, 오류 건수 및 필드별 품질 진단 상태를 실시간 모니터링합니다.';

  @override
  String get dqDashboardSubtitle => '실시간 마스터 데이터 거버넌스 및 데이터 정합성 모니터링';

  @override
  String get dqDashboardTitle => '데이터 품질 진단 대시보드';

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
  String get advancedSearch => '상세 검색';

  @override
  String get advancedSearchCondition => '상세 검색 조건 (Advanced Search)';

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
  String get dataLineage => '데이터 계보';

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
  String get affectedRecords => '영향 받는 레코드';

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
      '업로드한 백업 파일의 역할 및 권한 설정으로 기존 조직의 권한을 덮어쓰시겠습니까?';

  @override
  String get classificationAxes => 'CLASSIFICATION AXES (다중 축)';

  @override
  String get domainName => '도메인명';

  @override
  String get classificationName => '분류명';

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
  String get addField => '필드 추가';

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
  String get field => '필드';

  @override
  String fieldKeyAlreadyExistsNewfieldValueKey(Object key) {
    return '이미 존재하는 Field Key 입니다: $key';
  }

  @override
  String get fieldKeyExists => '이미 존재하는 Field Key 입니다';

  @override
  String get fieldName => '속성 / 필드명';

  @override
  String get fieldPermGroupTitle => '속성 필드 권한';

  @override
  String get fields => '기본 필드';

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
  String get nodeIcon => '부서 아이콘';

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
  String get selectDomainPlaceholder => '도메인 선택';

  @override
  String get selectNodePlaceholder => '분류 노드 선택';

  @override
  String get selectTargetDomain => '참조할 도메인을 선택해주세요.';

  @override
  String get selectTargetDomainAlert => '적용 도메인을 선택해주세요.';

  @override
  String get tabFields => '필드 관리';

  @override
  String get targetDomain => '적용 도메인 *';

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
  String get itemsCount => '개 항목';

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
  String get notificationsTitle => '시스템 알림 센터';

  @override
  String get notificationsEmpty => '수신된 새로운 알림이 없습니다.';

  @override
  String get notificationsMarkAllRead => '전체 읽음 처리';

  @override
  String get chatCreateRoom => '대화방 만들기';

  @override
  String get chatRoomTitlePlaceholder => '대화방 제목을 입력하세요';

  @override
  String get chatTitle => '실시간 협업 메신저';

  @override
  String get chatEmptyRooms => '참여 중인 대화방이 없습니다.';

  @override
  String get homeNoActivity => '최근 활동 이력이 없습니다.';

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
}
