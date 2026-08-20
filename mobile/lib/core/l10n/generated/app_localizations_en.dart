// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get emptyNotification => 'No modified data.';

  @override
  String get beforeValue => 'Previous Value';

  @override
  String get afterValue => 'New Value';

  @override
  String get approvalDraft => 'Draft';

  @override
  String get processedStatus => 'Processed';

  @override
  String get systemAutoApprovedNotice =>
      'System Auto-Approved (No approval line)';

  @override
  String get targetTypeRecordUpdate => 'Master Record Update';

  @override
  String get targetTypeRecordCreate => 'Master Record Create';

  @override
  String get targetTypeRecordDelete => 'Master Record Delete';

  @override
  String get targetTypeSchemaChange => 'Schema Change';

  @override
  String get targetTypeSandbox => 'Sandbox';

  @override
  String get viewOriginal => 'View Original';

  @override
  String get hideOriginal => 'Hide Original';

  @override
  String get fontSizeSetting => 'Font Size';

  @override
  String get fontSizeSmall => 'Small';

  @override
  String get fontSizeMedium => 'Medium';

  @override
  String get fontSizeLarge => 'Large';

  @override
  String get fontSizeXlarge => 'X-Large';

  @override
  String get maskingPattern => 'Masking Pattern';

  @override
  String get encryptedField => 'Encrypted Field';

  @override
  String get sensitiveAccessLogs => 'Decryption Logs';

  @override
  String get topUsers => 'Top Users';

  @override
  String get accessLogViewer => 'Viewer';

  @override
  String get accessLogTargetType => 'Target Type';

  @override
  String get accessLogTargetId => 'Target ID';

  @override
  String get accessLogFields => 'Accessed Fields';

  @override
  String get accessLogReason => 'Access Reason';

  @override
  String get approvalREQUEST => 'Approval Request';

  @override
  String get record => 'Record Access';

  @override
  String get recordHISTORY => 'Record History';

  @override
  String get accessLogIp => 'IP Address';

  @override
  String get accessLogTime => 'Access Time';

  @override
  String get maskingPatternGeneric => 'Generic Masking (GENERIC)';

  @override
  String get maskingPatternCard => 'Card Number (1234-****-****-5678)';

  @override
  String get maskingPatternRrn => 'RRN/SSN (900101-1******)';

  @override
  String get maskingPatternPhone => 'Phone Number (010-****-5678)';

  @override
  String get maskingPatternEmail => 'Email Address (u***@example.com)';

  @override
  String get addNewDept => 'Add New Department';

  @override
  String get addNewTeam => 'Add New Team';

  @override
  String get addRole => 'Add Role';

  @override
  String get addRootDept => 'Add Root Department';

  @override
  String get addSubdept => 'Add Sub-department';

  @override
  String get addTeam => 'Add Team';

  @override
  String get admin => 'Admin';

  @override
  String get adminMonitor => 'Admin Monitor';

  @override
  String get applicantRole => 'Applicant Role';

  @override
  String get applicantUser => 'Applicant User';

  @override
  String get assignDept => 'Register to Dept';

  @override
  String get assigneeRole => 'Role';

  @override
  String get assigneeUser => 'User';

  @override
  String get auditSourceSystem => 'Source System';

  @override
  String get belongsToDept => 'Belongs to Department';

  @override
  String get belongsToOrg => 'Belongs to Organization';

  @override
  String get companyOrg => 'Company / Organization';

  @override
  String get checkDuplicate => 'Check Duplicate';

  @override
  String get createNewOrg => 'Create New Organization';

  @override
  String get createOrganization => 'Create Organization';

  @override
  String get createRoleTitle => 'Create Organization RBAC Role';

  @override
  String get currentDept => 'Current Dept';

  @override
  String get deleteDept => 'Delete Department';

  @override
  String get deleteOrganization => 'Delete Organization';

  @override
  String get dept => 'Department';

  @override
  String get deptAssignCol => 'Action';

  @override
  String get deptMembers => 'Manage Department Members';

  @override
  String get deptMembersDesc =>
      'Add new members to the selected department or unassign existing members.';

  @override
  String get deptName => 'Department Name';

  @override
  String get deptRoles => 'Department Roles (Multi-selectable)';

  @override
  String get deptStatusCol => 'Status';

  @override
  String get deptStructure => 'Department Hierarchy Structure (Tree View)';

  @override
  String get deptStructureDesc => 'Organization Hierarchy';

  @override
  String get deptTeamManagement => 'Departments & Teams';

  @override
  String get editDept => 'Edit Department';

  @override
  String get editRoleTitle => 'Edit Organization RBAC Role';

  @override
  String get effectiveRoles => 'Effective Roles';

  @override
  String get encrypted => 'Encrypted';

  @override
  String get errorUsernameExists => 'Username already exists.';

  @override
  String get formulaSettings => 'Formula Settings';

  @override
  String get installAdminName => 'Admin Display Name';

  @override
  String get installAdminPwd => 'Password';

  @override
  String get installAdminPwdConfirm => 'Confirm Password';

  @override
  String get installAdminUsername => 'Admin Username';

  @override
  String get installAdminEmail => 'Email Address';

  @override
  String get installRequireEmail => 'Please enter an email address.';

  @override
  String get installRequireEmailValid => 'Invalid email format.';

  @override
  String get sourceSystemNode => 'Source System';

  @override
  String get installOrgEn => 'Primary Master Organization (EN)';

  @override
  String get installOrgEnPlaceholder => 'e.g. Enterprise HQ';

  @override
  String get installOrgKo => 'Primary Master Organization (KO)';

  @override
  String get installOrgKoPlaceholder => 'e.g. (주)엔터프라이즈 본사';

  @override
  String get installOrgEmailDomain => 'Primary Email Domain';

  @override
  String get installOrgEmailDomainTip =>
      'Configure the default email domain for organization members. (e.g. company.com, optional)';

  @override
  String get installOrgTip =>
      'Standard system roles and wildcard(*) permissions will be automatically assigned to the created organization.';

  @override
  String get installRequireOrgEn =>
      'Please enter organization name in English.';

  @override
  String get installRequireOrgKo => 'Please enter organization name in Korean.';

  @override
  String get installRequireUsername => 'Please enter username.';

  @override
  String get integrationChannelSystem => 'Channel / System';

  @override
  String get labelRole => 'Role';

  @override
  String get labelUsername => 'Username';

  @override
  String get menuAccessLogs => 'Menu Access Logs';

  @override
  String get msgUsernameAvailable => 'Username is available.';

  @override
  String get msgUsernameCheckRequired => 'Please check username availability.';

  @override
  String get msgUsernameExists => 'Username already exists.';

  @override
  String get noDeptAssignedTip =>
      '(Department Unassigned - Assign in [Organization Management])';

  @override
  String get noDeptsAdded =>
      'No departments added yet. Click [+ Add Department] button.';

  @override
  String get noOrgHistory => 'No organization change history recorded.';

  @override
  String get optionsSettings => 'Options Settings';

  @override
  String get orgCodePlaceholder => 'System Code (Unique, e.g. acme_corp)';

  @override
  String get orgCreatedSuccess => 'Organization created successfully.';

  @override
  String get orgDeleteFailed => 'Failed to delete organization.';

  @override
  String get orgDeleteSuccess => 'Organization deleted successfully.';

  @override
  String get orgDescription => 'Description';

  @override
  String get orgDisplayName => 'Display Name';

  @override
  String get orgDisplayNamePlaceholder =>
      'Display Name (e.g. Acme Corporation)';

  @override
  String get orgHistoryTitle => 'Organization History';

  @override
  String get orgIcon => 'Organization Icon';

  @override
  String get orgInfoTitle => 'Organization Information';

  @override
  String get orgList => 'Organizations';

  @override
  String get orgManagementDesc =>
      'Manage multi-tenant organizations, hierarchical departments/teams, and RBAC role/permission structures.';

  @override
  String get orgSysCode => 'System Code (Unique)';

  @override
  String get orgTenantManagement => 'Organizations & Departments';

  @override
  String get orgUpdatedSuccess => 'Organization info updated successfully.';

  @override
  String get organization => 'Organization';

  @override
  String get organizationManagement => 'Organization Management';

  @override
  String get otherDept => 'Other Dept';

  @override
  String get parentDept => 'Parent Department (Default: Root)';

  @override
  String get personalSettings => 'Personal Settings';

  @override
  String get placeholderUsername => 'Enter your username';

  @override
  String get rbacRoleManagement => 'RBAC Roles';

  @override
  String get requiredRoles => 'Required Roles (Multiple)';

  @override
  String get roleAdmin => 'System Administrator';

  @override
  String get roleAssignCol => 'Assign Role';

  @override
  String get roleCodeLabel => 'Role Code (e.g. CUSTOM_MANAGER)';

  @override
  String get roleCreationPlaceholder =>
      'Role creation functionality is under preparation.';

  @override
  String get roleDescriptionLabel => 'Role Description';

  @override
  String get roleDisplayNameLabel => 'Role Display Name';

  @override
  String get roleDomainEditor => 'Domain Editor';

  @override
  String get roleDomainViewer => 'Domain Viewer';

  @override
  String get roleUser => 'Standard User';

  @override
  String get saveRole => 'Save Role';

  @override
  String get searchUserBtn => 'Search & Register User';

  @override
  String get searchUserModalTitle => 'Search & Select Department Member';

  @override
  String get searchUserPlaceholder => 'Search username or role...';

  @override
  String get selectApproverRole => 'Select Approval Role';

  @override
  String get selectApproverUser => 'Select Approver';

  @override
  String get selectRole => 'Select Role';

  @override
  String get selectRoleToAdd => 'Select a role to add';

  @override
  String get selectUser => 'Select User';

  @override
  String get syncDefaultRoles => 'Sync Default Roles & Permissions';

  @override
  String get syncDefaultRolesConfirmAll =>
      'Do you want to sync the 8 default system roles and missing permissions for all organizations?';

  @override
  String syncDefaultRolesConfirmOrg(Object name) {
    return 'Do you want to sync the 8 default system roles and missing permissions for org \'$name\'?';
  }

  @override
  String get syncDefaultRolesError =>
      'An error occurred during synchronization.';

  @override
  String get syncDefaultRolesFail => 'Failed to synchronize default roles.';

  @override
  String get syncDefaultRolesSuccess =>
      'Default roles and permissions synchronization completed successfully.';

  @override
  String get systemapplied => 'System Reflect';

  @override
  String get systemcancelled => 'Cancelled';

  @override
  String get systemcomplete => 'Complete';

  @override
  String get systemCustomRoles => 'System & Custom Roles';

  @override
  String get systemLogs => 'System Logs';

  @override
  String get systemLogsDesc =>
      'Real-time monitoring of system operation history, user logins, exception errors, and integration channel logs.';

  @override
  String get systemLogsTitle => 'System Audit & Integration Logs';

  @override
  String get systemNotification => 'System Notification';

  @override
  String get systemOrgInfo => 'System Organization Info';

  @override
  String get team => 'Team';

  @override
  String get teamName => 'Team Name';

  @override
  String get tempPassword => 'Temporary Password';

  @override
  String get tempPasswordCheck => 'Temporary Password Check';

  @override
  String get viewTempPassword => 'View Temporary Password';

  @override
  String get tempPasswordWarning =>
      'This password will not be displayed again. Please make sure to copy and deliver it to the user.';

  @override
  String get updateRole => 'Update Role';

  @override
  String get userManagement => 'User Management';

  @override
  String get userManagementDesc =>
      'Manage user account information, organization/department assignments, system roles, and domain access permissions.';

  @override
  String get userProfileTitle => 'User Profile';

  @override
  String get userInfoAndRole => 'User Information & System Roles';

  @override
  String get userEmail => 'Email Address';

  @override
  String get saveUserInfo => 'Save User Info';

  @override
  String get userInfoUpdatedSuccess =>
      'User information (email and roles) has been successfully saved.';

  @override
  String get userInfoUpdateFailed => 'Failed to save user information: ';

  @override
  String get invalidEmailFormat => 'Please enter a valid email address.';

  @override
  String get userRole => 'User Role';

  @override
  String get userRoles => 'User System Roles (Multi-selectable)';

  @override
  String get username => 'Username';

  @override
  String get usernameCol => 'Username';

  @override
  String get viewUserProfile => 'View user profile';

  @override
  String get backupSeedFiles => 'Backup State to Seed Files';

  @override
  String get codeManagementTitle => 'Data Dictionary';

  @override
  String get title => 'Schema Change History';

  @override
  String get codeManagementDesc =>
      'Manage common system codes, dictionary groups, and hierarchical data code details globally.';

  @override
  String get desc =>
      'Configure EXACT / FUZZY matching rules and similarity thresholds for duplicate record identification.';

  @override
  String get codeManagementExportJson => 'Export JSON';

  @override
  String get exportJson => 'Export JSON';

  @override
  String get codeManagementImportJson => 'Import JSON';

  @override
  String get importJson => 'Import JSON';

  @override
  String get codeManagementCodeGroups => 'Code Groups';

  @override
  String get codeGroups => 'Code Groups';

  @override
  String get codeManagementCodeDetails => 'Code Details';

  @override
  String get codeDetails => 'Code Details';

  @override
  String get codeManagementAdd => 'Add';

  @override
  String get add => 'Add Channel';

  @override
  String get codeManagementGroupCode => 'Group Code';

  @override
  String get groupCode => 'Group Code';

  @override
  String get codeManagementName => 'Name';

  @override
  String get name => 'Name';

  @override
  String get codeManagementStatus => 'Status';

  @override
  String get status => 'Status';

  @override
  String get codeManagementManage => 'Manage';

  @override
  String get manage => 'Manage';

  @override
  String get codeManagementDetailCode => 'Detail Code';

  @override
  String get detailCode => 'Detail Code';

  @override
  String get codeManagementSortOrder => 'Sort Order';

  @override
  String get sortOrder => 'Sort Order';

  @override
  String get codeManagementEditGroup => 'Edit Group';

  @override
  String get editGroup => 'Edit Group';

  @override
  String get codeManagementAddGroup => 'Add Group';

  @override
  String get addGroup => 'Add Group';

  @override
  String get codeManagementNameKo => 'Name (Korean)';

  @override
  String get nameKo => 'Name (Korean)';

  @override
  String get codeManagementNameEn => 'Name (English)';

  @override
  String get nameEn => 'Name (English)';

  @override
  String get codeManagementDescKo => 'Description (Korean)';

  @override
  String get descKo => 'Description (Korean)';

  @override
  String get codeManagementDescEn => 'Description (English)';

  @override
  String get descEn => 'Description (English)';

  @override
  String get codeManagementActive => 'Active';

  @override
  String get active => 'Active';

  @override
  String get codeManagementCancel => 'Cancel';

  @override
  String get cancel => 'Cancel';

  @override
  String get codeManagementSave => 'Save';

  @override
  String get save => 'Save';

  @override
  String get codeManagementEditDetail => 'Edit Detail Code';

  @override
  String get editDetail => 'Edit Detail Code';

  @override
  String get codeManagementAddDetail => 'Add Detail Code';

  @override
  String get addDetail => 'Add Detail Code';

  @override
  String get codeManagementSelectGroupMsg =>
      'Please select a code group from the left panel.';

  @override
  String get selectGroupMsg =>
      'Please select a code group from the left panel.';

  @override
  String codeManagementConfirmDeleteGroup(Object code) {
    return 'Are you sure you want to delete group $code?';
  }

  @override
  String confirmDeleteGroup(Object code) {
    return 'Are you sure you want to delete group $code?';
  }

  @override
  String codeManagementConfirmDeleteDetail(Object code) {
    return 'Are you sure you want to delete detail $code?';
  }

  @override
  String confirmDeleteDetail(Object code) {
    return 'Are you sure you want to delete detail $code?';
  }

  @override
  String get codeManagementExportFailed => 'Failed to export JSON.';

  @override
  String get exportFailed => 'Failed to export JSON.';

  @override
  String get codeManagementImportSuccess => 'Codes imported successfully.';

  @override
  String importSuccess(Object nodes, Object fields) {
    return 'Domain package imported successfully ($nodes nodes, $fields fields created).';
  }

  @override
  String get codeManagementImportFailed =>
      'Failed to import JSON. Invalid format or server error.';

  @override
  String get importFailed => 'Failed to import domain package.';

  @override
  String get codeManagementSaveSuccess => 'Saved successfully.';

  @override
  String get saveSuccess => 'Survivorship rules saved successfully.';

  @override
  String get codeManagementSaveFailed => 'Failed to save.';

  @override
  String get saveFailed => 'Failed to save.';

  @override
  String get codeManagementDeleteSuccess => 'Deleted successfully.';

  @override
  String get deleteSuccess => 'Delete Completed';

  @override
  String get codeManagementDeleteFailed => 'Failed to delete.';

  @override
  String get deleteFailed => 'Failed to delete.';

  @override
  String get codeManagementLoadFailed => 'Failed to load data.';

  @override
  String get loadFailed => 'Failed to load data.';

  @override
  String get codeManagementSyncCodes => 'Sync Default Codes';

  @override
  String get syncCodes => 'Sync Default Codes';

  @override
  String get codeManagementDumpCodes => 'Backup Codes (Dump)';

  @override
  String get dumpCodes => 'Backup Codes (Dump)';

  @override
  String get globalSearchPlaceholder => 'Global Search (Search any data...)';

  @override
  String get searchMinLength => 'Please enter at least 2 characters.';

  @override
  String get searchNoResults => 'No results found.';

  @override
  String get searchNoData => 'No Data';

  @override
  String get matchingrulesTitle => 'Matching Rules Management';

  @override
  String get matchingrulesSubtitle =>
      'Configure EXACT / FUZZY matching rules and similarity thresholds for deduplication.';

  @override
  String get subtitle => 'Internal Messages & Email Management';

  @override
  String get matchingrulesSelectDomainPlaceholder => 'Select Domain';

  @override
  String get selectDomainPlaceholder => 'Select Domain';

  @override
  String get matchingrulesAddRule => 'Add Rule';

  @override
  String get addRule => 'Add Business Rule';

  @override
  String get matchingrulesRefresh => 'Refresh';

  @override
  String get refresh => 'Refresh';

  @override
  String get matchingrulesTotalReviewed => 'Total Reviewed';

  @override
  String get totalReviewed => 'Total Reviewed';

  @override
  String get matchingrulesPrecision => 'Precision';

  @override
  String get precision => 'Precision';

  @override
  String get matchingrulesConfirmed => 'Confirmed';

  @override
  String get confirmed => 'Confirmed';

  @override
  String get matchingrulesRejected => 'Rejected';

  @override
  String get rejected => 'Rejected';

  @override
  String get matchingrulesCurrentThreshold => 'Current Threshold';

  @override
  String get currentThreshold => 'Current Threshold';

  @override
  String get matchingrulesRecommendedThreshold => 'Recommended Threshold';

  @override
  String get recommendedThreshold => 'Recommended Threshold';

  @override
  String get matchingrulesRuleList => 'Matching Rules List';

  @override
  String get ruleList => 'Rule List';

  @override
  String matchingrulesItemsCount(Object count) {
    return '$count items';
  }

  @override
  String itemsCount(Object count) {
    return ' items';
  }

  @override
  String get matchingrulesEmptyNoRules => 'No matching rules registered.';

  @override
  String get emptyNoRules => 'No survivorship rules configured.';

  @override
  String get matchingrulesEmptySelectDomain =>
      'Please select a domain first from the dropdown above.';

  @override
  String get emptySelectDomain =>
      'Please select a domain first from the dropdown above.';

  @override
  String get matchingrulesEmptyNoRulesDesc =>
      'Click \'+ Add Rule\' at the top right to create a new rule for deduplication.';

  @override
  String get emptyNoRulesDesc =>
      'Click \'+ Add Rule\' at the top right to create a new rule for deduplication.';

  @override
  String get matchingrulesEmptySelectDomainDesc =>
      'Selecting a domain will display its deduplication rule list in AG-Grid.';

  @override
  String get emptySelectDomainDesc =>
      'Selecting a domain will display its deduplication rule list in AG-Grid.';

  @override
  String get matchingrulesAddFirstRule => 'Add First Matching Rule';

  @override
  String get addFirstRule => 'Add First Rule';

  @override
  String get matchingrulesCreateTitle => 'Add New Matching Rule';

  @override
  String get createTitle => 'Add New Matching Rule';

  @override
  String get matchingrulesEditTitle => 'Edit Matching Rule';

  @override
  String get editTitle => 'Edit Matching Rule';

  @override
  String get matchingrulesRuleName => 'Rule Name';

  @override
  String get ruleName => 'Rule Name';

  @override
  String get matchingrulesRuleNamePlaceholder =>
      'e.g., Name and Contact Match Rule';

  @override
  String get ruleNamePlaceholder => 'e.g., Name and Contact Match Rule';

  @override
  String get matchingrulesMatchType => 'Match Type';

  @override
  String get matchType => 'Match Type';

  @override
  String get matchingrulesTargetFields => 'Target Fields (Multi-select)';

  @override
  String get targetFields => 'Target Fields (Multi-select)';

  @override
  String get matchingrulesTargetFieldsCsv =>
      'Target Field Keys (comma separated)';

  @override
  String get targetFieldsCsv => 'Target Field Keys (comma separated)';

  @override
  String get matchingrulesSimilarityThreshold =>
      'Similarity Threshold (0.5 ~ 1.0)';

  @override
  String get similarityThreshold => 'Similarity Threshold (0.5 ~ 1.0)';

  @override
  String get matchingrulesIsActive => 'Rule Is Active';

  @override
  String get isActive => 'Active';

  @override
  String get matchingrulesCancel => 'Cancel';

  @override
  String get matchingrulesSave => 'Save';

  @override
  String get matchingrulesActive => 'Active';

  @override
  String get matchingrulesInactive => 'Inactive';

  @override
  String get inactive => 'Inactive';

  @override
  String get matchingrulesSaveSuccess => 'Matching rule saved successfully.';

  @override
  String get matchingrulesSaveFailed => 'Failed to save matching rule.';

  @override
  String matchingrulesDeleteConfirm(Object name) {
    return 'Are you sure you want to delete matching rule \'$name\'?';
  }

  @override
  String deleteConfirm(Object name) {
    return 'Are you sure you want to delete matching rule \'$name\'?';
  }

  @override
  String get matchingrulesDeleteSuccess => 'Matching rule deleted.';

  @override
  String get matchingrulesDeleteFailed => 'Failed to delete matching rule.';

  @override
  String get labelEmail => 'Email Address';

  @override
  String get placeholderEmail => 'e.g., user@company.com';

  @override
  String get emailDomain => 'Email Domain';

  @override
  String get placeholderEmailDomain => 'e.g. company.com';

  @override
  String get orgEmailDomainDesc =>
      'Configure the default email domain for the organization.';

  @override
  String get ruleTypeBusinessNoChecksum =>
      'Korean Business Registration No Checksum (10 digits)';

  @override
  String get ruleTypeCorporateNoChecksum =>
      'Korean Corporate Registration No Checksum (13 digits)';

  @override
  String get channelHealth => 'Channel Health';

  @override
  String get channelMetrics => 'Realtime Throughput & DLQ Metrics';

  @override
  String get healthHealthy => 'HEALTHY';

  @override
  String get healthDegraded => 'DEGRADED';

  @override
  String get healthUnhealthy => 'UNHEALTHY';

  @override
  String get pingTest => 'Ping Test';

  @override
  String get pingTesting => 'Testing Ping...';

  @override
  String get avgLatency => 'Avg Latency';

  @override
  String get timeSlot => 'Time Slot';

  @override
  String get smartMapping => 'Smart Auto-Mapping Recommendation';

  @override
  String get smartMappingDesc =>
      'Analyze sample payload keys and suggest best domain field mappings with fuzzy text matching.';

  @override
  String get samplePayload => 'Sample Payload (JSON)';

  @override
  String get samplePayloadPlaceholder => 'Enter sample JSON payload.';

  @override
  String get recommendMapping => 'Run Auto-Mapping Suggestion';

  @override
  String get applyRecommendations => 'Apply Suggested Mappings';

  @override
  String get sourceField => 'Source Field';

  @override
  String get targetField => 'Target System Field';

  @override
  String get confidenceScore => 'Confidence';

  @override
  String get matchReason => 'Match Reason';

  @override
  String get dlqHub => 'Dead Letter Queue (DLQ) & Retry Hub';

  @override
  String get dlqHubDesc =>
      'Manage and batch retry failed/dead-letter logs from external integration channels.';

  @override
  String get retrySelected => 'Retry Selected';

  @override
  String get retryAll => 'Retry All Failed';

  @override
  String get retryCount => 'Retry Count';

  @override
  String get errorMessage => 'Error Message';

  @override
  String get noDlqItems => 'No failed or dead-letter logs found.';

  @override
  String get webhookHub => 'Real-time Event Webhook Dispatcher';

  @override
  String get webhookHubDesc =>
      'Dispatch master record create/update/approval events to external systems in real-time.';

  @override
  String get addWebhook => 'Add Webhook';

  @override
  String get targetUrl => 'Target Webhook URL';

  @override
  String get subscribedEvents => 'Subscribed Events';

  @override
  String get testWebhook => 'Test Webhook';

  @override
  String get noWebhooks => 'No webhooks registered.';

  @override
  String get dataRetention => 'Data Retention & GDPR Purge Hub';

  @override
  String get dataRetentionDesc =>
      'Safely anonymizes or hard purges master records that exceeded legal retention periods or received erasure requests.';

  @override
  String get retentionYears => 'Retention Years';

  @override
  String get scanExpired => 'Scan Expired Records';

  @override
  String get purgeType => 'Purge Type';

  @override
  String get purgeAnonymize => 'Soft Anonymize';

  @override
  String get purgeHardDelete => 'Hard Delete';

  @override
  String get executePurge => 'Execute Safe Purge';

  @override
  String get expiredCount => 'Expired Count';

  @override
  String get anomalyDetection =>
      'Zero-Trust Anomaly Access & Exfiltration Detector';

  @override
  String get anomalyDetectionDesc =>
      'Real-time zero-trust detection of massive data exfiltration and unauthorized access anomalies.';

  @override
  String get threatScore => 'Security Threat Score';

  @override
  String get activeThreats => 'Active Threats';

  @override
  String get threatLevel => 'Threat Level';

  @override
  String get blockActor => 'Block Actor';

  @override
  String get blockedBadge => 'Blocked';

  @override
  String get syncPipeline => 'Cross-Domain Data Sync Pipeline Scheduler';

  @override
  String get syncPipelineDesc =>
      'Configure periodic synchronization rules and cron schedules across multiple domains.';

  @override
  String get pipelineName => 'Pipeline Name';

  @override
  String get cronSchedule => 'Cron Schedule';

  @override
  String get lastSynced => 'Last Synced';

  @override
  String get triggerPipeline => 'Trigger Now';

  @override
  String get noPipelines => 'No sync pipelines configured.';

  @override
  String get apiKeyMgmt => 'Integration API Key & Scoped Access Manager';

  @override
  String get apiKeyMgmtDesc =>
      'Manage API keys, expiration, IP whitelists, and granular permission scopes for external integration channels.';

  @override
  String get issueApiKey => 'Issue New API Key';

  @override
  String get keyName => 'Key Name';

  @override
  String get validDays => 'Validity (Days)';

  @override
  String get allowedIps => 'Allowed IPs (CIDR)';

  @override
  String get permissionScopes => 'Permission Scopes';

  @override
  String get revokeKey => 'Revoke Key';

  @override
  String get confirmRevokeKey =>
      'Revoke this API key permanently? Channel calls using this key will be blocked immediately.';

  @override
  String get systemDiagnostics =>
      'Global System Health Diagnostics & Dependency Monitor';

  @override
  String get systemDiagnosticsDesc =>
      'Monitor real-time latency and status across DB, cache, message brokers, and storage backbones.';

  @override
  String get componentName => 'Component Name';

  @override
  String get latency => 'Latency (ms)';

  @override
  String get averageLatency => 'Average Latency';

  @override
  String get overallStatus => 'Overall Status';

  @override
  String get runDiagnostics => 'Refresh Diagnostics';

  @override
  String get workspaceWidgets => 'Governance Dashboard Widget Customizer';

  @override
  String get workspaceWidgetsDesc =>
      'Personalize your workspace dashboard by customizing DQ, approval, security, and integration widgets.';

  @override
  String get widgetGallery => 'Widget Gallery';

  @override
  String get widgetEnabled => 'Enabled';

  @override
  String get widgetDisabled => 'Disabled';

  @override
  String get saveLayout => 'Save Layout';

  @override
  String get coldStorage => 'Cold-Storage Archiver';

  @override
  String get coldStorageDesc =>
      'Freeze full enterprise master data into encrypted cold-storage archive packages and simulate disaster recovery integrity.';

  @override
  String get createArchive => 'Create Frozen Archive';

  @override
  String get archiveName => 'Archive Name';

  @override
  String get checksumSha256 => 'Checksum (SHA-256)';

  @override
  String get compressionRatio => 'Compression & Encryption';

  @override
  String get simulateDr => 'Simulate DR Restore';

  @override
  String get regulatoryCompliance => 'Regulatory Compliance';

  @override
  String get regulatoryComplianceDesc =>
      'Audits system encryption, audit ledgers, and purge engines against ISMS-P, PIPA, and GDPR regulations.';

  @override
  String get complianceScore => 'Compliance Score';

  @override
  String get certificationReadiness => 'Certification Readiness';

  @override
  String get controlCode => 'Control Code';

  @override
  String get evidence => 'Audit Evidence';

  @override
  String get runAudit => 'Run Audit';

  @override
  String get volumeRadar => 'Volume Radar';

  @override
  String get volumeRadarDesc =>
      'Monitors real-time record changes and API traffic with Z-score anomaly models to detect traffic spikes.';

  @override
  String get currentThroughput => 'Current Throughput';

  @override
  String get baselineThroughput => 'Baseline Throughput';

  @override
  String get volumeHistory => 'Volume History';

  @override
  String get spikeAlert => 'Spike Alert';

  @override
  String get normalTraffic => 'Normal Traffic';

  @override
  String get governanceMaturity => 'Governance Maturity';

  @override
  String get governanceMaturityDesc =>
      'Evaluates enterprise data quality KPIs and governance maturity levels (Level 1~5) based on DMM/CMMI models.';

  @override
  String get overallMaturityLevel => 'Overall Governance Maturity Level';

  @override
  String get maturityDimensions => '5 Maturity Dimensions';

  @override
  String get completenessKpi => 'Completeness';

  @override
  String get timelinessKpi => 'Timeliness';

  @override
  String get consistencyKpi => 'Consistency';

  @override
  String get validityKpi => 'Validity';

  @override
  String get multiTenant => 'Multi-Tenant Router';

  @override
  String get multiTenantDesc =>
      'Manages row/column level data isolation and virtual routing policies across HQ, branches, and subsidiaries.';

  @override
  String get tenantName => 'Tenant Name';

  @override
  String get partitionType => 'Partition Type';

  @override
  String get routingExpression => 'Routing Expression';

  @override
  String get targetDomains => 'Target Domains';

  @override
  String get dataSla => 'Data SLA Tracker';

  @override
  String get dataSlaDesc =>
      'Tracks real-time latency, availability, and DQ compliance contracts across domains and integration channels.';

  @override
  String get slaContractName => 'SLA Contract / Target';

  @override
  String get latencySla => 'Latency';

  @override
  String get availabilitySla => 'Availability';

  @override
  String get qualitySla => 'DQ Compliance';

  @override
  String get meetingSla => 'Meeting SLA';

  @override
  String get masterOrchestrator => 'Master Orchestrator';

  @override
  String get masterOrchestratorDesc =>
      'Orchestrates and monitors the health status and lifecycle of all 50 enterprise master data governance features.';

  @override
  String get totalFeaturesCount => 'Total Features';

  @override
  String get healthyFeaturesCount => 'Healthy Features';

  @override
  String get featureNo => 'No.';

  @override
  String get featureName => 'Feature Name';

  @override
  String get featureCategory => 'Category';

  @override
  String get pipelineSelfHealing => 'Pipeline Self-Healing';

  @override
  String get pipelineSelfHealingDesc =>
      'AI agent diagnoses schema mismatches, network delays, and format corruptions to autonomously heal and reroute pipelines.';

  @override
  String get healingActionId => 'Action ID';

  @override
  String get diagnosedCause => 'Diagnosed Cause';

  @override
  String get healingStrategy => 'Healing Strategy';

  @override
  String get recoveredRecords => 'Recovered Records';

  @override
  String get triggerHealing => 'Trigger Self-Healing';

  @override
  String get healingTriggered => 'Autonomous healing triggered successfully.';

  @override
  String get freshnessHeatmap => 'Freshness Heatmap';

  @override
  String get freshnessHeatmapDesc =>
      'Monitors last update timestamps and real-time latency across domains to prevent stale data.';

  @override
  String get freshnessScore => 'Freshness Score';

  @override
  String get lastUpdatedTime => 'Last Updated';

  @override
  String get delayMinutes => 'Delay Minutes';

  @override
  String get freshStatus => 'Fresh';

  @override
  String get multiRegionConflict => 'Conflict Auto-Resolver';

  @override
  String get multiRegionConflictDesc =>
      'Autonomously resolves concurrent update conflicts across global regions using vector clocks and priority rules.';

  @override
  String get regionPair => 'Region Pair';

  @override
  String get resolutionStrategy => 'Strategy';

  @override
  String get resolvedValue => 'Resolved Value';

  @override
  String get autoResolved => 'Auto-Resolved';

  @override
  String get governanceCopilot => 'Governance AI Copilot';

  @override
  String get governanceCopilotDesc =>
      'Interactive AI copilot for querying quality metrics, SLA risks, multi-region sync, and autonomous healing.';

  @override
  String get copilotPlaceholder =>
      'Ask anything about data governance (e.g. Summarize DQ, Check SLA status...)';

  @override
  String get copilotSend => 'Send';

  @override
  String get quickQuestions => 'Quick Prompts';

  @override
  String get alldone => 'All approvals/consensus are completed.';

  @override
  String get noparsable => 'No parsable data provided.';

  @override
  String get addcomment => 'Add Comment';

  @override
  String get actionTitle => 'Process Request';

  @override
  String bulkApprove(Object count) {
    return 'Bulk Approve';
  }

  @override
  String bulkReject(Object count) {
    return 'Bulk Reject';
  }

  @override
  String bulkApproveConfirm(Object count) {
    return 'Are you sure you want to approve the selected $count requests?';
  }

  @override
  String bulkRejectConfirm(Object count) {
    return 'Are you sure you want to reject the selected $count requests?';
  }

  @override
  String get bulkApproveLoading => 'Bulk approving...';

  @override
  String get bulkRejectLoading => 'Bulk rejecting...';

  @override
  String processing(Object percent) {
    return 'Processing Data... $percent%';
  }

  @override
  String get approvalLine => 'Approval Route';

  @override
  String get targetType => 'Target Type';

  @override
  String get stepType => 'Step Type';

  @override
  String get action => 'Action';

  @override
  String get statusDraft => 'Draft';

  @override
  String get statusPending => 'Pending Review';

  @override
  String get statusWaiting => 'Waiting';

  @override
  String get recordCreate => 'New Record Create';

  @override
  String get recordUpdate => 'Record Update';

  @override
  String get recordDelete => 'Record Delete';

  @override
  String get domainRecordCreate => 'Domain Record Create';

  @override
  String get targetTypeMEMO => 'Memo Approval';

  @override
  String get targetTypeMemo => 'Memo Approval';

  @override
  String get memoApproval => 'Memo Approval';

  @override
  String get noComment => 'No Comment';

  @override
  String get cancelReason => 'Cancellation Reason';

  @override
  String get rejectionReason => 'Rejection Reason';

  @override
  String get cancellationNotice =>
      'This approval request has been cancelled by the drafter.';

  @override
  String get statusCancelled => 'Cancelled';

  @override
  String get noReasonSpecified => 'No reason specified.';

  @override
  String get observers => 'Observers (CC)';

  @override
  String get stepscheduled => 'Scheduled';

  @override
  String get coldomain => 'Domain';

  @override
  String get colclassification => 'Classification';

  @override
  String get colidattr => 'ID Attribute';

  @override
  String get colnameattr => 'Name Attribute';

  @override
  String get colsummary => 'Summary';

  @override
  String get actionApprove => 'Approved';

  @override
  String get actionReject => 'Rejected';

  @override
  String actionProcessing(Object action) {
    return 'Processing $action...';
  }

  @override
  String actionSuccess(Object action) {
    return 'Request has been successfully $action.';
  }

  @override
  String get review => 'Review';

  @override
  String get created => 'Created At';

  @override
  String get close => 'Close';

  @override
  String get general => 'General';

  @override
  String get fields => 'Fields';

  @override
  String get summary => 'Summary';

  @override
  String get details => 'Details';

  @override
  String get id => 'ID';

  @override
  String get targetTypeApprovalRequest => 'Approval Request';

  @override
  String get confirmAndSubmit => 'Confirm Impact & Submit Request';

  @override
  String get confirmSafetyApply => 'Confirm Safety & Apply Changes';

  @override
  String get confirmSafetySubmit => 'Confirm Safety & Submit Request';

  @override
  String get approvalSubmittedTitle => 'Approval Request Submitted';

  @override
  String get confirmRiskApply => 'Confirm Risk & Apply Changes';

  @override
  String get confirmRiskDesc =>
      'Confirmed risk levels and warnings. Approve and apply schema changes.';

  @override
  String get addApprovalStep => '+ Add Approval Step';

  @override
  String get approval => 'Approval';

  @override
  String get approvallinestatus => 'Approval Line Status';

  @override
  String get approvallinesummary => 'Approval Line (Summary):';

  @override
  String get approvalHistory => 'Approval History';

  @override
  String get approvalHistoryBtn => 'Approval History';

  @override
  String get approvalHistoryDetail => 'Approval History Detail';

  @override
  String get approvalInProgress => 'Approval in Progress';

  @override
  String get approvalLineStatus => 'Approval Line Status';

  @override
  String get approvalLineSummary => 'Approval Line (Summary)';

  @override
  String get approvalLineTitle => 'Multi-Step Approval Line';

  @override
  String get approvalMonitor => 'Approval Monitor';

  @override
  String get approvalMonitoring => 'Approval Monitoring';

  @override
  String get approvalProgressStep => 'Approval Progress Step';

  @override
  String get approvalReview => 'Approval Review';

  @override
  String approvalStatsSummary(Object approved, Object rejected) {
    return 'Approved $approved / Rejected $rejected';
  }

  @override
  String get approvalStepsCol => 'Approval Steps';

  @override
  String get approvalSuccessRate => 'Approval Success Rate';

  @override
  String get approvalTrendTitle => '7-Day Approval Requests Trend';

  @override
  String get approvals => 'Approvals';

  @override
  String get approvalsTitle => 'Approvals & Inbox';

  @override
  String get approve => 'Approve';

  @override
  String get approver => 'Approver';

  @override
  String get btnSubmit => 'Submit';

  @override
  String get cancelRequest => 'Cancel Request';

  @override
  String confirmBatchApprove(Object count) {
    return 'Batch approve selected $count items?';
  }

  @override
  String confirmBatchReject(Object count) {
    return 'Batch reject selected $count items?';
  }

  @override
  String get confirmDelete => 'Are you sure you want to delete?';

  @override
  String deleteWorkflowConfirm(Object name) {
    return 'Are you sure you want to delete workflow \'$name\'?';
  }

  @override
  String get editDisabledApproval =>
      '⚠️ This record cannot be edited while an approval is pending.';

  @override
  String get finalapproval => 'Final Approval';

  @override
  String get installBtnSubmit => 'Complete Installation & Register Admin';

  @override
  String get labelConfirmPassword => 'Confirm Password';

  @override
  String get mysubmitted => 'My Submitted Requests';

  @override
  String get mySubmittedRequests => 'My Submitted Requests';

  @override
  String get noapprovalline => 'No approval line.';

  @override
  String get norequests => 'There are no pending requests.';

  @override
  String get nosubmitted => 'No submitted requests.';

  @override
  String get noApprovalLine => 'No Approval Line';

  @override
  String get noApprovalSteps => 'No approval steps defined (Auto-approved).';

  @override
  String get noPendingRequests => 'No pending requests.';

  @override
  String get noRequestsSubmittedYet => 'No requests submitted yet.';

  @override
  String get pendingapprovals => 'Pending Approvals';

  @override
  String get pendingApproval => 'Pending Approval';

  @override
  String get pendingApprovalAssignee => 'Pending Approver:';

  @override
  String get pendingApprovalNotice =>
      '⚠️ This record is currently under approval and cannot be modified.';

  @override
  String get pendingApprovals => 'Pending Approvals';

  @override
  String get pendingRequests => 'Pending Requests';

  @override
  String get placeholderConfirmPassword => 'Re-enter your password';

  @override
  String get reject => 'Reject';

  @override
  String get requestDate => 'Request Date';

  @override
  String get requestInfo => 'Request Info';

  @override
  String get requesteddata => 'Requested Data';

  @override
  String get requestedAccessTo => 'requested access to';

  @override
  String get requester => 'Requester';

  @override
  String get selectApproval => 'Select Approval';

  @override
  String get selectRejection => 'Select Rejection';

  @override
  String get statusApproved => 'Approved';

  @override
  String get statusRejected => 'Rejected';

  @override
  String get stepapproved => 'Approved';

  @override
  String get steprejected => 'Rejected';

  @override
  String get stepApproval => 'Approval';

  @override
  String get stepTypeApproval => 'Approval';

  @override
  String get submitRequest => 'Submit Request';

  @override
  String get typeapproval => 'Approval';

  @override
  String get viewApprovalHistory => 'View Approval History';

  @override
  String get approvalInbox => 'Approval Inbox';

  @override
  String pendingCount(Object count) {
    return 'Pending: $count';
  }

  @override
  String itemCount(Object count) {
    return '$count items';
  }

  @override
  String get consensus => 'Consensus';

  @override
  String get draft => 'Draft';

  @override
  String get draftCompleted => 'Draft Completed';

  @override
  String get processed => 'Processed';

  @override
  String get observersList => 'Observers List';

  @override
  String get approvalDelegation => 'Approval Delegation';

  @override
  String get approvalDelegationDesc =>
      'Designate a proxy approver during your absence (vacation, business trip, etc.).';

  @override
  String get delegatedByMe => 'Delegated by Me';

  @override
  String get delegatedToMe => 'Delegated to Me';

  @override
  String get delegatee => 'Proxy Approver';

  @override
  String get delegator => 'Delegator';

  @override
  String get delegationPeriod => 'Delegation Period';

  @override
  String get delegationReason => 'Delegation Reason';

  @override
  String get addDelegation => 'Add Delegation';

  @override
  String get revokeDelegation => 'Revoke Delegation';

  @override
  String get delegationActive => 'Active';

  @override
  String get delegationExpired => 'Expired';

  @override
  String proxyBadge(Object name) {
    return 'Proxy (for $name)';
  }

  @override
  String get delegationSuccess =>
      'Approval delegation registered successfully.';

  @override
  String get revokeSuccess => 'Approval delegation revoked.';

  @override
  String slaDue(Object time) {
    return 'SLA Due: $time';
  }

  @override
  String get slaExpired => 'SLA Expired';

  @override
  String slaEscalatedBadge(Object name) {
    return 'Escalated (Original: $name)';
  }

  @override
  String get scanEscalation => 'Run SLA Escalation Scan';

  @override
  String escalationSuccess(Object count) {
    return 'Successfully escalated $count overdue approval steps to admins.';
  }

  @override
  String get approvalSandbox => 'Pre-Approval Data Simulation Sandbox';

  @override
  String get approvalSandboxDesc =>
      'Simulate and preview Before/After master data changes before final approval decision.';

  @override
  String get previewDiff => 'Pre-Approval Simulation';

  @override
  String get simulatedResult => 'Simulated Post-Approval Data';

  @override
  String get dynamicRouting => 'Dynamic Approval Routing & Workflow Templates';

  @override
  String get dynamicRoutingDesc =>
      'Automatically branch and assign approval stages based on field conditions and data sensitivity.';

  @override
  String get templateName => 'Template Name';

  @override
  String get conditionField => 'Condition Field';

  @override
  String get conditionOperator => 'Operator';

  @override
  String get conditionValue => 'Value';

  @override
  String get approvalSteps => 'Approval Steps';

  @override
  String get addTemplate => 'Add Routing Template';

  @override
  String get rejectionAnalytics =>
      'Approval Rejection Reason Analytics & Resubmit Guide';

  @override
  String get rejectionAnalyticsDesc =>
      'Analyzes past rejection reasons to provide cause distribution statistics and resubmission checklists.';

  @override
  String get rejectionCauseDistribution => 'Rejection Cause Distribution';

  @override
  String get resubmitChecklist => 'Resubmission Pre-Checklist';

  @override
  String get actionGuide => 'Action Guide';

  @override
  String get loginCount => 'Login Count';

  @override
  String get btnLogin => 'Sign In';

  @override
  String get btnRegister => 'Create Account';

  @override
  String get login => 'Login';

  @override
  String get loginFailed => 'Login failed. Please check your credentials.';

  @override
  String get loginTitleSub => 'Secure Data Classification Platform';

  @override
  String get logout => 'Logout';

  @override
  String get newWorkflowRegister => '+ New Workflow';

  @override
  String get noOrgsRegistered => 'No organizations registered.';

  @override
  String get registerRoleBtn => 'Register Role';

  @override
  String get registeredDomains => 'Registered Master Domains';

  @override
  String get tabLogin => 'Login';

  @override
  String get tabRegister => 'Register';

  @override
  String get userLoginLogs => 'User Login Logs';

  @override
  String get forcePasswordChange => 'Force Password Change';

  @override
  String get forcePasswordChangeDesc =>
      'For security reasons, you must change your initial password. Please set a new password.';

  @override
  String get createUser => 'Create User';

  @override
  String get userCreated => 'User Created Successfully';

  @override
  String get oldPassword => 'Current Password';

  @override
  String get newPassword => 'New Password';

  @override
  String get confirmNewPassword => 'Confirm New Password';

  @override
  String get changePassword => 'Change Password';

  @override
  String get tempPasswordIssued => 'Temporary Password Issued';

  @override
  String get authLoginErrorMessage =>
      'Authentication failed. Please check your credentials.';

  @override
  String get requiredField => 'This field is required.';

  @override
  String get min8Chars => 'Please enter at least 8 characters.';

  @override
  String get passwordsDoNotMatch => 'Passwords do not match.';

  @override
  String get fillAllFields => 'Please fill in all fields.';

  @override
  String get passwordChangeFailed => 'Failed to change password.';

  @override
  String get accessCount => 'Access Count';

  @override
  String get dispatchedTO => 'Dispatched To';

  @override
  String get decryptionTrendLast7Days => 'Decryption Trend (Last 7 Days)';

  @override
  String get evolvedTO => 'Evolved To';

  @override
  String get modifiedTO => 'Modified To';

  @override
  String get typeRatios => 'Type Ratios';

  @override
  String get recordItem => 'Data Record';

  @override
  String get imageFile => 'Image File';

  @override
  String get attachment => 'Attachment';

  @override
  String get richText => 'Rich Text';

  @override
  String get previewImage => 'Image Preview';

  @override
  String get downloadFile => 'Download Completed File';

  @override
  String imageCount(Object count) {
    return '$count images';
  }

  @override
  String fileCount(Object count) {
    return 'File ($count)';
  }

  @override
  String get accessReason => 'Access Reason';

  @override
  String get accessReasonPlaceholder =>
      'e.g., Business process, Customer request';

  @override
  String get accessReasonRequired => 'Please enter an access reason.';

  @override
  String get actionRequired => '⚠️ Action Required';

  @override
  String get actionTypeAll => 'All';

  @override
  String get actionTypeCol => 'Action Type';

  @override
  String get actionTypeCreate => 'Creation (CREATE)';

  @override
  String get actionTypeCreateShort => 'Creation';

  @override
  String get actionTypeDelete => 'Deletion (DELETE)';

  @override
  String get actionTypeDeleteShort => 'Deletion';

  @override
  String get actionTypeMerge => 'Record Merge (MERGE)';

  @override
  String get actionTypeMergeShort => 'Record Merge';

  @override
  String get actionTypeUpdate => 'Modification (UPDATE)';

  @override
  String get actionTypeUpdateShort => 'Modification';

  @override
  String get actions => 'Tree / Actions';

  @override
  String get actionsCol => 'Actions';

  @override
  String get activeStatus => 'Active';

  @override
  String get approvalprogress => 'Approval Progress';

  @override
  String get propertyFieldName => 'Property / Field Name';

  @override
  String get previousValue => 'Previous Value';

  @override
  String get newValue => 'New Value';

  @override
  String get addDepartment => 'Add Department';

  @override
  String get addFilter => 'Add Filter';

  @override
  String get addMenu => 'Add Menu';

  @override
  String get addNewGroupBtn => 'Add New Group';

  @override
  String get addNewMember => 'Add New Member';

  @override
  String get addNewPermGroupTitle => 'Create New Permission Group';

  @override
  String get addOption => 'Add Option';

  @override
  String get addPermBtn => 'Add Permission';

  @override
  String get addPermToGroupTitle => 'Add New Permission to Group';

  @override
  String get addRootMenu => '+ Add Root Menu';

  @override
  String get addRow => '+ Add Row';

  @override
  String get affectedChannels => 'Affected Integration Channels';

  @override
  String get afterChange => 'After Change (New Value)';

  @override
  String get agGridUnifiedList => 'AG-Grid Unified List';

  @override
  String get allTasksCleared => '✅ All Tasks Cleared';

  @override
  String get approvalDetailTitle => 'Approval Details';

  @override
  String get approvalMonitorTitle => 'Approval Monitoring';

  @override
  String get assigned => 'Assigned';

  @override
  String get assignedAt => 'Assignment Date';

  @override
  String get assignedMembersList => 'Assigned Members List';

  @override
  String get auditChangeType => 'Change Type';

  @override
  String get auditChangedBy => 'Changed By';

  @override
  String get auditNewData => 'New Data';

  @override
  String get auditNoHistory => 'No history found.';

  @override
  String get auditPreviousData => 'Previous Data';

  @override
  String get auditTrail => 'Audit Trail';

  @override
  String get axisActions => 'Tree / Actions';

  @override
  String get axisAddAxis => 'Add Axis';

  @override
  String get addAxis => 'Add Axis';

  @override
  String get axisAddChildNode => '+ Child Node';

  @override
  String get addChildNode => '+ Child Node';

  @override
  String get axisAddRootNode => 'Add Root Node';

  @override
  String get addRootNode => 'Add Root Node';

  @override
  String get axisAssignModalTitle => 'Assign Secondary Classification Nodes';

  @override
  String get assignModalTitle => 'Assign Secondary Classification Nodes';

  @override
  String get axisAssignSecondaryNodes => 'Assign / Edit Secondary Nodes';

  @override
  String get assignSecondaryNodes => 'Assign / Edit Secondary Nodes';

  @override
  String get axisAxisAdded => 'New classification axis added.';

  @override
  String get axisAdded => 'New classification axis added.';

  @override
  String get axisAxisCodeLabel => 'Axis Code';

  @override
  String get axisCodeLabel => 'Axis Code';

  @override
  String get axisAxisDeleted => 'Classification axis deleted.';

  @override
  String get axisDeleted => 'Classification axis deleted.';

  @override
  String get axisAxisLabel => 'Axis';

  @override
  String get axisLabel => 'Axis';

  @override
  String get axisAxisNameLabel => 'Axis Name';

  @override
  String get axisNameLabel => 'Axis Name';

  @override
  String get axisAxisUpdated => 'Classification axis updated.';

  @override
  String get axisUpdated => 'Classification axis updated.';

  @override
  String get axisCode => 'Axis Code';

  @override
  String get code => 'Axis Code';

  @override
  String get axisCodeBadge => 'Code';

  @override
  String get codeBadge => 'Code';

  @override
  String get axisDeleteAxisConfirm =>
      'Are you sure you want to delete this axis? All child nodes will also be deleted.';

  @override
  String get deleteAxisConfirm =>
      'Are you sure you want to delete this axis? All child nodes will also be deleted.';

  @override
  String get axisDeleteNode => 'Delete';

  @override
  String get deleteNode => 'Delete Node';

  @override
  String get axisDeleteNodeConfirm =>
      'Are you sure you want to delete this node?';

  @override
  String get deleteNodeConfirm => 'Are you sure you want to delete this node?';

  @override
  String get axisDescription => 'Description';

  @override
  String get description =>
      'Configure field-level survival priority and conflict resolution rules for golden record creation during merge.';

  @override
  String get axisEditAxis => 'Edit Axis';

  @override
  String get editAxis => 'Edit Axis';

  @override
  String get axisEditNode => 'Edit';

  @override
  String get editNode => 'Edit Node';

  @override
  String get axisEnterCodeName => 'Please enter code and axis name.';

  @override
  String get enterCodeName => 'Please enter code and axis name.';

  @override
  String get axisInvalidDomain => 'Invalid domain information.';

  @override
  String get invalidDomain => 'Invalid domain information.';

  @override
  String get axisLoadingTree => 'Loading tree nodes...';

  @override
  String get loadingTree => 'Loading tree nodes...';

  @override
  String get axisManagementDesc =>
      'Register independent multi-dimensional classification axes such as Organization, Region, or Industry.';

  @override
  String get managementDesc =>
      'Register independent multi-dimensional classification axes such as Organization, Region, or Industry.';

  @override
  String get axisManagementTitle => 'Multi-Axis Classification Management';

  @override
  String get managementTitle => 'Multi-Axis Classification Management';

  @override
  String get axisName => 'Axis Name';

  @override
  String get axisNoAxes => 'No secondary classification axes registered.';

  @override
  String get noAxes => 'No secondary classification axes registered.';

  @override
  String get axisNoNodesDesc =>
      'No classification nodes registered for this axis. Click [Add Root Node] to build an independent tree.';

  @override
  String get noNodesDesc =>
      'No classification nodes registered for this axis. Click [Add Root Node] to build an independent tree.';

  @override
  String get axisNoNodesRegistered => 'No nodes registered';

  @override
  String get noNodesRegistered => 'No nodes registered';

  @override
  String get axisNoSecondaryNodes =>
      'No secondary classification nodes assigned.';

  @override
  String get noSecondaryNodes => 'No secondary classification nodes assigned.';

  @override
  String get axisNodeAdded => 'New classification node added.';

  @override
  String get nodeAdded => 'New classification node added.';

  @override
  String get axisNodeDeleted => 'Classification node deleted.';

  @override
  String get nodeDeleted => 'Classification node deleted.';

  @override
  String get axisNodeIcon => 'Node Icon';

  @override
  String get nodeIcon => 'Node Icon';

  @override
  String get axisNodeManagementTitle => 'Axis Specific Tree Node Management';

  @override
  String get nodeManagementTitle => 'Axis Specific Tree Node Management';

  @override
  String get axisNodeNameEn => 'Node Name (EN)';

  @override
  String get nodeNameEn => 'Node Name (EN)';

  @override
  String get axisNodeNameKo => 'Node Name (KO)';

  @override
  String get nodeNameKo => 'Node Name (KO)';

  @override
  String get axisNodeUpdated => 'Classification node updated.';

  @override
  String get nodeUpdated => 'Classification node updated.';

  @override
  String get axisPrimaryTree => 'Primary Schema';

  @override
  String get primaryTree => 'Primary Schema';

  @override
  String get axisRefresh => 'Refresh';

  @override
  String get axisSaveSecondaryNodes => 'Save Secondary Nodes';

  @override
  String get saveSecondaryNodes => 'Save Secondary Nodes';

  @override
  String get axisSecondaryMappingDesc =>
      'View and edit secondary classification node assignments for this record.';

  @override
  String get secondaryMappingDesc =>
      'View and edit secondary classification node assignments for this record.';

  @override
  String get axisSecondaryMappingTitle =>
      'Record Secondary Classification Node Mapping';

  @override
  String get secondaryMappingTitle =>
      'Record Secondary Classification Node Mapping';

  @override
  String get axisSecondaryNodesSaveFailed =>
      'Failed to save secondary classification nodes';

  @override
  String get secondaryNodesSaveFailed =>
      'Failed to save secondary classification nodes';

  @override
  String get axisSecondaryNodesSaved =>
      'Secondary classification nodes assigned successfully.';

  @override
  String get secondaryNodesSaved =>
      'Secondary classification nodes assigned successfully.';

  @override
  String get axisSelectAxis => 'Select Axis';

  @override
  String get selectAxis => 'Select Axis';

  @override
  String get axisSelectIcon => 'Select Icon';

  @override
  String get selectIcon => 'Select Icon';

  @override
  String get axisSelectNodesForAxis => 'Select Nodes per Axis';

  @override
  String get selectNodesForAxis => 'Select Nodes per Axis';

  @override
  String get axisSelectNodesPlaceholder => 'Select nodes';

  @override
  String get selectNodesPlaceholder => 'Select nodes';

  @override
  String get axisSortOrder => 'Sort Order';

  @override
  String get axisTreeManage => 'Tree Manage';

  @override
  String get treeManage => 'Tree Manage';

  @override
  String get bulkReclassify => 'Bulk Reclassify';

  @override
  String bulkReclassifyDesc(Object count) {
    return 'Batch change classification node for $count selected records.';
  }

  @override
  String get bulkReclassifySuccess =>
      'Bulk reclassification completed successfully.';

  @override
  String get bulkReclassifyFail => 'Failed to bulk reclassify records.';

  @override
  String get bulkReclassifyTargetNode => 'Select Target Classification Node';

  @override
  String get businessRuleBuilder => 'Business Rule Builder';

  @override
  String selectedRecordTarget(Object code) {
    return 'Target Record: $code';
  }

  @override
  String get tableView => 'Diff Table';

  @override
  String get jsonView => 'Raw JSON';

  @override
  String get diffFieldName => 'Field Name';

  @override
  String get diffBefore => 'Before';

  @override
  String get diffAfter => 'After';

  @override
  String get diffStatus => 'Diff Status';

  @override
  String get noDiffData => 'No attribute data to display.';

  @override
  String get selectCdcEventGuide =>
      'Select a CDC event from the left list to inspect details.';

  @override
  String get noCdcEventsInDomain =>
      'No real-time change data capture events found for this domain.';

  @override
  String get exportDownloadSuccess => 'Export file downloaded successfully.';

  @override
  String get exportDownloadFailed => 'Failed to download export file.';

  @override
  String emailDomainAutoHint(Object domain) {
    return 'Auto-completed with organization domain ($domain).';
  }

  @override
  String get emailDefaultFallbackHint => 'Enter your email address.';

  @override
  String get backupMenuSeed => 'Backup Menu State';

  @override
  String get menuChildrenRoleUnionNotice =>
      'Child menus exist, so required roles are automatically merged (union) and cannot be manually modified.';

  @override
  String get menuDumpSeedConfirm =>
      'Do you want to backup current menu settings (order, active status, required roles, etc.) to the default seed file? (Admin only)';

  @override
  String get menuDumpSeedSuccess =>
      'Menu seed file backup completed successfully.';

  @override
  String get menuDumpSeedFailed => 'Failed to backup menu seed file.';

  @override
  String get menuSyncSeedConfirm =>
      'Do you want to sync the current menu structure with the seed file (default_menus.json)? (Missing menus will be added and existing menus updated)';

  @override
  String get menuSyncSeedSuccess =>
      'Menu synchronization completed successfully.';

  @override
  String get menuSyncSeedFailed => 'Failed to synchronize menus.';

  @override
  String get dqTargetField => 'Target Field';

  @override
  String get dqSelectFieldPlaceholder =>
      'Select a node from the tree and then choose a field.';

  @override
  String get dqSelectNodeFieldGuide =>
      'Select a node and field to manage data quality rules.';

  @override
  String get dqNoRulesFound => 'No DQ rules found for this field.';

  @override
  String get dqRuleSaveFailed => 'Failed to save DQ rule.';

  @override
  String get dqRuleDeleteConfirm =>
      'Are you sure you want to delete this rule?';

  @override
  String get dqRuleDeletedSuccess => 'Rule deleted successfully.';

  @override
  String get dqRuleDeleteFailed => 'Failed to delete rule.';

  @override
  String userDeleteConfirm(Object username) {
    return 'Are you sure you want to delete user \'$username\'? This action cannot be undone.';
  }

  @override
  String get userDeleteSuccess => 'User deleted successfully.';

  @override
  String get userDeleteConflictError =>
      'Cannot delete user because associated data (records or approval requests) exists.';

  @override
  String userDeleteFailed(Object error) {
    return 'Error occurred while deleting user: $error';
  }

  @override
  String get userTempPasswordNotFound => 'Temporary password not found.';

  @override
  String get userTempPasswordQueryFailed =>
      'Query failed: Temporary password does not exist or unauthorized.';

  @override
  String dumpSeedFilesConfirmOrg(Object name) {
    return 'Do you want to backup/overwrite all role states set in the \'$name\' organization to the system default (Seed) JSON file?\n(This will directly modify files in the source code directory)';
  }

  @override
  String get dumpSeedFilesConfirmAll =>
      'Do you want to backup/overwrite all role states stored in the entire DB to the system default (Seed) JSON file?\n(This will directly modify files in the source code directory)';

  @override
  String get dumpSeedFilesSuccess => 'Default seed files successfully updated.';

  @override
  String get dumpSeedFilesFail => 'Failed to update default seed files.';

  @override
  String get dumpSeedFilesError =>
      'Error occurred while updating default seed files.';

  @override
  String get baselineBadge => 'Baseline';

  @override
  String get basicInfo => 'Basic Info';

  @override
  String get beforeChange => 'Before Change (Previous Value)';

  @override
  String get boolean => 'Boolean';

  @override
  String get btndetails => 'Details';

  @override
  String get btnCancel => 'Cancel';

  @override
  String get calculated => 'Calculated';

  @override
  String get calculatedSuffix => '(Calculated)';

  @override
  String get changeContent => 'Changes';

  @override
  String get changeDetails => 'Changes';

  @override
  String get changeHistory => 'Change History';

  @override
  String get changeHistoryTab => 'Change History';

  @override
  String get changeType => 'Type';

  @override
  String get changedBy => 'Changed By';

  @override
  String get chartRatio => 'Ratio';

  @override
  String get chatTableTitle => 'Excel Data Table';

  @override
  String get classificationNode => 'Classification Node';

  @override
  String get clickImageToExpandTip =>
      'Click to expand image in large modal viewer';

  @override
  String get clickTableToExpandTip =>
      'Click table to expand in large modal viewer';

  @override
  String get colaction => 'Action';

  @override
  String get colcreatedat => 'Created At';

  @override
  String get colrequester => 'Requester';

  @override
  String get colstatus => 'Status';

  @override
  String get coltargettype => 'Target Type';

  @override
  String get collapse => 'Collapse';

  @override
  String get comment => 'Comment';

  @override
  String get commonLoading => 'Processing data...';

  @override
  String get compareChanges => 'Compare Changes';

  @override
  String comparingCount(Object count) {
    return 'Comparing $count records';
  }

  @override
  String get confirm => 'Confirm';

  @override
  String get copyAsMarkdown => '📝 Copy as Markdown Table';

  @override
  String get copyCell => 'Copy Cell';

  @override
  String get copyTableBtn => 'Copy Table Data';

  @override
  String get copyTableBtnTitle => 'Copy Excel table data to clipboard';

  @override
  String get create => 'Create';

  @override
  String get createGroupBtn => 'Create Group';

  @override
  String get createWorkflowTitle => '🆕 Register New Workflow Template';

  @override
  String get createdat => 'Created At';

  @override
  String get creationSuccess => 'Creation Completed';

  @override
  String get currentAffiliation => 'Current Affiliation:';

  @override
  String get dashboard => 'Dashboard';

  @override
  String get dashboardSubtitle =>
      'Monitor master data governance overview, quality diagnostic metrics, and key integration statuses.';

  @override
  String get date => 'Date';

  @override
  String get dateTime => 'Date & Time';

  @override
  String get decryptFailed => 'Failed to decrypt value.';

  @override
  String get deduplicationCandidateRecord => 'Candidate Record';

  @override
  String get candidateRecord => 'Candidate Record';

  @override
  String get deduplicationCompareAndAction => 'Compare & Act';

  @override
  String get compareAndAction => 'Compare & Act';

  @override
  String get deduplicationConfirmMerge => 'Approve Merge';

  @override
  String get confirmMerge => 'Confirm Merge';

  @override
  String get deduplicationDuplicateCandidate => 'Candidate Record (To Merge)';

  @override
  String get duplicateCandidate => 'Candidate Record (To Merge)';

  @override
  String get deduplicationKeepSeparate => 'Keep Separate';

  @override
  String get keepSeparate => 'Keep Separate';

  @override
  String get deduplicationMasterRecord => 'Master Record (Survivor)';

  @override
  String get masterRecord => 'Master Record (Survivor)';

  @override
  String get deduplicationModalTitle => 'Side-by-Side Field Comparison';

  @override
  String get modalTitle => 'Side-by-Side Field Comparison';

  @override
  String get deduplicationNoCandidates => 'No candidate records to review.';

  @override
  String get noCandidates => 'No candidate records to review.';

  @override
  String deduplicationPendingCount(Object count) {
    return 'Pending: $count';
  }

  @override
  String get deduplicationRuleDefault => 'Fuzzy Match Rule';

  @override
  String get ruleDefault => 'Fuzzy Match Rule';

  @override
  String get deduplicationSimilarity => 'Similarity';

  @override
  String get similarity => 'Similarity';

  @override
  String get deduplicationSubtitle =>
      'Review potential duplicate records found by fuzzy matching and approve merge or separation.';

  @override
  String get deduplicationTargetRecord => 'Target Master Record';

  @override
  String get targetRecord => 'Target Master Record';

  @override
  String get deduplicationTitle => 'Match Candidates Queue';

  @override
  String get defaultBadge => '⭐ Default';

  @override
  String get delete => 'Delete';

  @override
  String get deleteErrorTitle => 'Deletion Error';

  @override
  String get deletedStatus => 'Deleted';

  @override
  String get department => 'Department';

  @override
  String get descriptionCol => 'Description';

  @override
  String get detailsInfo => 'Details';

  @override
  String get diffCountSuffix => 'Diffs';

  @override
  String get doReview => 'Review';

  @override
  String get draftCommentOptional =>
      '(Optional) Please write a comment for the approver';

  @override
  String get draftCommentPlaceholder => 'Enter a comment...';

  @override
  String get draftCommentTitle => 'Draft Comment';

  @override
  String eGAbsKeyAKeyB2100(Object KEY_A, Object KEY_B) {
    return 'e.g. ABS($KEY_A + $KEY_B / 2) * 100';
  }

  @override
  String get edit => 'Edit Channel';

  @override
  String get editMenu => 'Edit Menu';

  @override
  String get editWorkflowTitle => '✏️ Edit Workflow Template';

  @override
  String get empNoPrefix => 'Employee ID';

  @override
  String get endDate => 'End Date';

  @override
  String get enterFormula => 'Please enter a formula.';

  @override
  String get enterKeyAllOptions => 'Please enter a Key for all options.';

  @override
  String get error => 'Error';

  @override
  String errorDedupFailed(Object field) {
    return 'Duplicate record found based on Identifier field \'$field\'.';
  }

  @override
  String get errorDeletePendingCreation =>
      'Cannot delete a record that is pending creation approval.';

  @override
  String get errorFetching => 'Error fetching data.';

  @override
  String get errorInvalidCredentials => 'Invalid credentials.';

  @override
  String get errorNotAssignee => 'You are not the assignee for this step.';

  @override
  String get errorSaving => 'Error saving.';

  @override
  String get errorStepNotPending => 'Step is not pending.';

  @override
  String get errorUpdatePendingCreation =>
      'Cannot update a record that is pending creation approval.';

  @override
  String get errorUpdatePendingUpdate =>
      'This record is already under a pending update approval.';

  @override
  String get errorUploadDirFail =>
      'Could not create the directory where the uploaded files will be stored.';

  @override
  String get errorUploadFileFail => 'Could not store file. Please try again!';

  @override
  String get expand => 'Expand';

  @override
  String get failedLoadApprovalDetails => 'Failed to load approval details.';

  @override
  String get failedLoadHistory => 'Failed to load history data.';

  @override
  String get file => 'File';

  @override
  String get fileUploadButton => 'Select from PC';

  @override
  String get fileUploadDropzone => 'Drag and drop files here or ';

  @override
  String get finalPermValue => 'Final Permission Value';

  @override
  String get formDescription => 'Form Description';

  @override
  String get formDescriptionPlaceholder =>
      'Describe the purpose and approval procedure...';

  @override
  String get formNameEn => 'Form Title (English)';

  @override
  String get formNameEnPlaceholder => 'e.g. Domestic Stock Creation Form';

  @override
  String get formNameKo => 'Form Title (Korean) *';

  @override
  String get formNameKoPlaceholder => 'e.g. Domestic Stock Creation Form';

  @override
  String get formulaGuide => 'Formula Guide';

  @override
  String get governanceHealthTitle => 'Governance & Data Quality Health';

  @override
  String get grant => 'Grant';

  @override
  String get grantNewPermission => 'Grant New Permission';

  @override
  String get grantPermissionSuccess =>
      'Selected domain permissions granted successfully.';

  @override
  String get group => 'Group';

  @override
  String get groupCodeLabel => 'Group Code Name (e.g. report, api)';

  @override
  String get groupIconLabel => 'Emoji Icon (e.g. 📊, 🔑, ⚙️)';

  @override
  String get groupTitleEnLabel =>
      'Group English Title (e.g. Report Permissions)';

  @override
  String get groupTitleLabel => 'Group Korean Title (e.g. 리포트 권한)';

  @override
  String get hidden => 'Hidden';

  @override
  String get history => 'History';

  @override
  String get historyVersionDiffDetail =>
      'Detailed Comparison of Version Changes';

  @override
  String get immutable => 'Immutable';

  @override
  String get impactAnalysis => 'Impact Analysis';

  @override
  String get impactAnalysisPreview => 'Preview Impact Simulation';

  @override
  String get impactCheckTitle => 'Pre-change Impact Review';

  @override
  String get impactSafetyNotice => 'Checklist';

  @override
  String impactSummaryDelete(Object field, Object count) {
    return 'Deleting field \'$field\' will permanently remove data in $count active record(s).';
  }

  @override
  String impactSummaryDeleteEmpty(Object field) {
    return 'Field \'$field\' has 0 active records, making it safe to delete without data loss.';
  }

  @override
  String impactSummaryModify(Object field, Object count) {
    return 'Modifying field \'$field\' will affect $count active record(s).';
  }

  @override
  String impactSummaryModifyEmpty(Object field) {
    return 'Field \'$field\' has 0 active records, making it safe to modify without data loss.';
  }

  @override
  String get impactWarnings => 'Warnings & Precautions';

  @override
  String get inactiveBadge => 'Inactive';

  @override
  String get inactiveStatus => 'Inactive';

  @override
  String get incomingPayloadTitle => 'incoming_payload.json';

  @override
  String get info => 'Information';

  @override
  String get infoMsg =>
      'Select a domain node from the left tree to view records.';

  @override
  String get initialCreated => 'Initially Created';

  @override
  String get initialCreation => 'Initial Creation';

  @override
  String get installBtnNext => 'Next (Admin Account Setup)';

  @override
  String get installBtnPrev => 'Back';

  @override
  String get installRequirePwdLen =>
      'Password must be at least 6 characters long.';

  @override
  String get installStep1Label => 'Primary Organization Setup';

  @override
  String get installStep2Label => 'Super Admin Account Creation';

  @override
  String get installSubtitle =>
      'Initialize system setup and register the Super Admin account.';

  @override
  String get installTitle => 'System Setup Wizard';

  @override
  String get integrationChannelsAdd => 'Add Channel';

  @override
  String get integrationChannelsAddField => 'Add Field';

  @override
  String get addField => 'Add Field';

  @override
  String get integrationChannelsAddHeader => 'Add Header';

  @override
  String get addHeader => 'Add Header';

  @override
  String get integrationChannelsApprovalDetailTitle => 'Approval Details';

  @override
  String get integrationChannelsAuthApiKey => 'API Key Header';

  @override
  String get authApiKey => 'API Key Header';

  @override
  String get integrationChannelsAuthBearer => 'Bearer Token (Recommended)';

  @override
  String get authBearer => 'Bearer Token (Recommended)';

  @override
  String get integrationChannelsAuthHeaderExample => 'Auth Header';

  @override
  String get authHeaderExample => 'Auth Header';

  @override
  String get integrationChannelsAuthNone => 'None (No Auth)';

  @override
  String get authNone => 'None (No Auth)';

  @override
  String get integrationChannelsAuthType => 'Inbound Auth Type';

  @override
  String get authType => 'Inbound Auth Type';

  @override
  String get integrationChannelsAutoMapFields =>
      'Auto Map Domain Fields (Incl. Multilingual)';

  @override
  String get autoMapFields => 'Auto Map Domain Fields (Incl. Multilingual)';

  @override
  String get integrationChannelsBasicConfig => 'Basic & Auth Config';

  @override
  String get basicConfig => 'Basic & Auth Config';

  @override
  String get integrationChannelsChannelCode => 'Channel Code';

  @override
  String get channelCode => 'Channel Code';

  @override
  String get integrationChannelsChannelName => 'Channel Name';

  @override
  String get channelName => 'Channel Name';

  @override
  String get integrationChannelsConfirmDeleteChannel =>
      'Are you sure you want to delete this channel?';

  @override
  String get confirmDeleteChannel =>
      'Are you sure you want to delete this channel?';

  @override
  String get integrationChannelsCopied => 'Copied to clipboard.';

  @override
  String get copied => 'Copied to clipboard.';

  @override
  String get integrationChannelsCopyCurl => 'Copy cURL';

  @override
  String get copyCurl => 'Copy cURL';

  @override
  String get integrationChannelsCopyHeader => 'Copy Header';

  @override
  String get copyHeader => 'Copy Header';

  @override
  String get integrationChannelsCopyJson => 'Copy JSON';

  @override
  String get copyJson => 'Copy JSON';

  @override
  String get integrationChannelsCopyValue => 'Copy Value';

  @override
  String get copyValue => 'Copy Value';

  @override
  String get integrationChannelsCreatedAt => 'Created At';

  @override
  String get createdAt => 'Created At';

  @override
  String get integrationChannelsCurlCopied =>
      'Sample cURL command copied to clipboard.';

  @override
  String get curlCopied => 'Sample cURL command copied to clipboard.';

  @override
  String get integrationChannelsDbPassword => 'Password';

  @override
  String get dbPassword => 'Password';

  @override
  String get integrationChannelsDbTable => 'Target Table';

  @override
  String get dbTable => 'Target Table';

  @override
  String get integrationChannelsDbUrl => 'DB URL';

  @override
  String get dbUrl => 'DB URL';

  @override
  String get integrationChannelsDbUser => 'Username';

  @override
  String get dbUser => 'Username';

  @override
  String get integrationChannelsDeptRoles =>
      'Department Roles (Multi-selectable)';

  @override
  String get integrationChannelsDesc =>
      'Manage data integration pipelines and interface channel settings with external systems.';

  @override
  String get integrationChannelsDescription => 'Manage integration channels.';

  @override
  String get integrationChannelsDetailConfig => 'Channel Configuration';

  @override
  String get detailConfig => 'Channel Configuration';

  @override
  String get integrationChannelsDirection => 'Direction';

  @override
  String get direction => 'Direction';

  @override
  String get integrationChannelsDomainField => 'Domain Field (Optional)';

  @override
  String get domainField => 'Domain Field (Optional)';

  @override
  String get integrationChannelsDomainRequiredForInbound =>
      'Domain selection is required for Inbound channel.';

  @override
  String get domainRequiredForInbound =>
      'Domain selection is required for Inbound channel.';

  @override
  String get integrationChannelsEdit => 'Edit Channel';

  @override
  String get integrationChannelsErrDbUrlRequired =>
      'Please enter a DB connection URL.';

  @override
  String get errDbUrlRequired => 'Please enter a DB connection URL.';

  @override
  String get integrationChannelsErrMqBrokerRequired =>
      'Please enter a broker URL.';

  @override
  String get errMqBrokerRequired => 'Please enter a broker URL.';

  @override
  String get integrationChannelsErrTestConnection =>
      'Network/Server error occurred during connection test.';

  @override
  String get errTestConnection =>
      'Network/Server error occurred during connection test.';

  @override
  String get integrationChannelsErrWsUrlRequired =>
      'Please enter an endpoint URL.';

  @override
  String get errWsUrlRequired => 'Please enter an endpoint URL.';

  @override
  String get integrationChannelsFieldMapping => 'Data Field Mapping';

  @override
  String get fieldMapping => 'Data Field Mapping';

  @override
  String get integrationChannelsGenerateToken => 'Generate Token';

  @override
  String get generateToken => 'Generate Token';

  @override
  String get integrationChannelsHeaderCopied =>
      'Auth header copied to clipboard.';

  @override
  String get headerCopied => 'Auth header copied to clipboard.';

  @override
  String get integrationChannelsHeaderValueCopied =>
      'Header value copied to clipboard.';

  @override
  String get headerValueCopied => 'Header value copied to clipboard.';

  @override
  String get integrationChannelsInbound => 'Inbound';

  @override
  String get inbound => 'Inbound';

  @override
  String get integrationChannelsInboundNotice =>
      'When an external system sends a POST request with JSON Payload to the Webhook URL below, data will be processed based on the configured mapping rules.';

  @override
  String get inboundNotice =>
      'When an external system sends a POST request with JSON Payload to the Webhook URL below, data will be processed based on the configured mapping rules.';

  @override
  String get integrationChannelsIntegrationDetailTitle => 'Integration Details';

  @override
  String get integrationDetailTitle => 'Integration Details';

  @override
  String get integrationChannelsIsActive => 'Is Active';

  @override
  String get integrationChannelsJsonCopied =>
      'Sample JSON payload copied to clipboard.';

  @override
  String get jsonCopied => 'Sample JSON payload copied to clipboard.';

  @override
  String get integrationChannelsManagement => 'Actions';

  @override
  String get management => 'Actions';

  @override
  String get integrationChannelsMappingDesc =>
      'Freely edit the target field and source expression. Selecting a domain enables the domain field dropdown.';

  @override
  String get mappingDesc =>
      'Freely edit the target field and source expression. Selecting a domain enables the domain field dropdown.';

  @override
  String get integrationChannelsMappingDescInbound =>
      'Enter Root Path for array processing (e.g. payload[\'data\']). Write expressions based on the single object (#this).';

  @override
  String get mappingDescInbound =>
      'Enter Root Path for array processing (e.g. payload[\'data\']). Write expressions based on the single object (#this).';

  @override
  String get integrationChannelsMappingRootPath => 'Array Data Root Path';

  @override
  String get mappingRootPath => 'Array Data Root Path';

  @override
  String get integrationChannelsMappingRootPathPlaceholder =>
      'e.g., payload[\'data\'] or payload.data';

  @override
  String get mappingRootPathPlaceholder =>
      'e.g., payload[\'data\'] or payload.data';

  @override
  String get integrationChannelsMqBroker => 'Broker URL';

  @override
  String get mqBroker => 'Broker URL';

  @override
  String get integrationChannelsMqTopic => 'Topic Name';

  @override
  String get mqTopic => 'Topic Name';

  @override
  String get integrationChannelsName => 'Channel Name';

  @override
  String get integrationChannelsNoHeaders => 'No headers configured.';

  @override
  String get noHeaders => 'No headers configured.';

  @override
  String get integrationChannelsNodeRequiredForInbound =>
      'Classification node selection is required for Inbound channel.';

  @override
  String get nodeRequiredForInbound =>
      'Classification node selection is required for Inbound channel.';

  @override
  String get integrationChannelsOutbound => 'Outbound';

  @override
  String get outbound => 'Outbound';

  @override
  String get integrationChannelsRequiresApproval => 'Requires Approval';

  @override
  String get requiresApproval => 'Requires Approval';

  @override
  String get integrationChannelsSamplePayloadNotice =>
      'Real-time example request payload generated from configured source expressions & Root Path in the Mapping tab.';

  @override
  String get samplePayloadNotice =>
      'Real-time example request payload generated from configured source expressions & Root Path in the Mapping tab.';

  @override
  String get integrationChannelsSamplePayloadTitle =>
      'Request JSON Payload Sample (Real-time Mapping)';

  @override
  String get samplePayloadTitle =>
      'Request JSON Payload Sample (Real-time Mapping)';

  @override
  String get integrationChannelsSecretToken => 'Secret Token';

  @override
  String get secretToken => 'Secret Token';

  @override
  String get integrationChannelsSelectDomain => 'Select Domain';

  @override
  String get selectDomain => 'Select Domain';

  @override
  String get integrationChannelsSelectDomainNode =>
      'Select Target Domain (Node)';

  @override
  String get selectDomainNode => 'Select Target Domain (Node)';

  @override
  String get integrationChannelsSelectNode =>
      'Select Node (Links only the selected node)';

  @override
  String get selectNode => 'Select Node (Links only the selected node)';

  @override
  String get integrationChannelsSourceExpr => 'Source Expression';

  @override
  String get sourceExpr => 'Source Expression';

  @override
  String get integrationChannelsSourceFieldInbound => 'External Source Field';

  @override
  String get sourceFieldInbound => 'External Source Field';

  @override
  String get integrationChannelsStatus => 'Status';

  @override
  String get integrationChannelsSystemNotification => 'System Notification';

  @override
  String get integrationChannelsTargetField => 'Target Field';

  @override
  String get integrationChannelsTargetFieldInbound =>
      'Internal Domain Field (Target)';

  @override
  String get targetFieldInbound => 'Internal Domain Field (Target)';

  @override
  String get integrationChannelsTestConnection => 'Test Connection';

  @override
  String get testConnection => 'Test Connection';

  @override
  String get integrationChannelsTitle => 'Integration Channels';

  @override
  String get integrationChannelsType => 'Type';

  @override
  String get type => 'Type';

  @override
  String get integrationChannelsWebhookCopy => 'Copy URL';

  @override
  String get webhookCopy => 'Copy URL';

  @override
  String get integrationChannelsWebhookUrl => 'Webhook URL';

  @override
  String get webhookUrl => 'Webhook URL';

  @override
  String get integrationChannelsWsMethod => 'HTTP Method';

  @override
  String get wsMethod => 'HTTP Method';

  @override
  String get integrationChannelsWsUrl => 'Endpoint URL';

  @override
  String get wsUrl => 'Endpoint URL';

  @override
  String get channelsAdd => 'Add Channel';

  @override
  String get channelsAddField => 'Add Field';

  @override
  String get channelsAddHeader => 'Add Header';

  @override
  String get channelsApprovalDetailTitle => 'Approval Details';

  @override
  String get channelsAuthApiKey => 'API Key Header';

  @override
  String get channelsAuthBearer => 'Bearer Token (Recommended)';

  @override
  String get channelsAuthHeaderExample => 'Auth Header';

  @override
  String get channelsAuthNone => 'None (No Auth)';

  @override
  String get channelsAuthType => 'Inbound Auth Type';

  @override
  String get channelsAutoMapFields =>
      'Auto Map Domain Fields (Incl. Multilingual)';

  @override
  String get channelsBasicConfig => 'Basic & Auth Config';

  @override
  String get channelsChannelCode => 'Channel Code';

  @override
  String get channelsChannelName => 'Channel Name';

  @override
  String get channelsConfirmDeleteChannel =>
      'Are you sure you want to delete this channel?';

  @override
  String get channelsCopied => 'Copied to clipboard.';

  @override
  String get channelsCopyCurl => 'Copy cURL';

  @override
  String get channelsCopyHeader => 'Copy Header';

  @override
  String get channelsCopyJson => 'Copy JSON';

  @override
  String get channelsCopyValue => 'Copy Value';

  @override
  String get channelsCreatedAt => 'Created At';

  @override
  String get channelsCurlCopied => 'Sample cURL command copied to clipboard.';

  @override
  String get channelsDbPassword => 'Password';

  @override
  String get channelsDbTable => 'Target Table';

  @override
  String get channelsDbUrl => 'DB URL';

  @override
  String get channelsDbUser => 'Username';

  @override
  String get channelsDeptRoles => 'Department Roles (Multi-selectable)';

  @override
  String get channelsDesc =>
      'Manage data integration pipelines and interface channel settings with external systems.';

  @override
  String get channelsDescription => 'Manage integration channels.';

  @override
  String get channelsDetailConfig => 'Channel Configuration';

  @override
  String get channelsDirection => 'Direction';

  @override
  String get channelsDomainField => 'Domain Field (Optional)';

  @override
  String get channelsDomainRequiredForInbound =>
      'Domain selection is required for Inbound channel.';

  @override
  String get channelsEdit => 'Edit Channel';

  @override
  String get channelsErrDbUrlRequired => 'Please enter a DB connection URL.';

  @override
  String get channelsErrMqBrokerRequired => 'Please enter a broker URL.';

  @override
  String get channelsErrTestConnection =>
      'Network/Server error occurred during connection test.';

  @override
  String get channelsErrWsUrlRequired => 'Please enter an endpoint URL.';

  @override
  String get channelsFieldMapping => 'Data Field Mapping';

  @override
  String get channelsGenerateToken => 'Generate Token';

  @override
  String get channelsHeaderCopied => 'Auth header copied to clipboard.';

  @override
  String get channelsHeaderValueCopied => 'Header value copied to clipboard.';

  @override
  String get channelsInbound => 'Inbound';

  @override
  String get channelsInboundNotice =>
      'When an external system sends a POST request with JSON Payload to the Webhook URL below, data will be processed based on the configured mapping rules.';

  @override
  String get channelsIntegrationDetailTitle => 'Integration Details';

  @override
  String get channelsIsActive => 'Is Active';

  @override
  String get channelsJsonCopied => 'Sample JSON payload copied to clipboard.';

  @override
  String get channelsManagement => 'Actions';

  @override
  String get channelsMappingDesc =>
      'Freely edit the target field and source expression. Selecting a domain enables the domain field dropdown.';

  @override
  String get channelsMappingDescInbound =>
      'Enter Root Path for array processing (e.g. payload[\'data\']). Write expressions based on the single object (#this).';

  @override
  String get channelsMappingRootPath => 'Array Data Root Path';

  @override
  String get channelsMappingRootPathPlaceholder =>
      'e.g., payload[\'data\'] or payload.data';

  @override
  String get channelsMqBroker => 'Broker URL';

  @override
  String get channelsMqTopic => 'Topic Name';

  @override
  String get channelsName => 'Channel Name';

  @override
  String get channelsNoHeaders => 'No headers configured.';

  @override
  String get channelsNodeRequiredForInbound =>
      'Classification node selection is required for Inbound channel.';

  @override
  String get channelsOutbound => 'Outbound';

  @override
  String get channelsRequiresApproval => 'Requires Approval';

  @override
  String get channelsSamplePayloadNotice =>
      'Real-time example request payload generated from configured source expressions & Root Path in the Mapping tab.';

  @override
  String get channelsSamplePayloadTitle =>
      'Request JSON Payload Sample (Real-time Mapping)';

  @override
  String get channelsSecretToken => 'Secret Token';

  @override
  String get channelsSelectDomain => 'Select Domain';

  @override
  String get channelsSelectDomainNode => 'Select Target Domain (Node)';

  @override
  String get channelsSelectNode => 'Select Node (Links only the selected node)';

  @override
  String get channelsSourceExpr => 'Source Expression';

  @override
  String get channelsSourceFieldInbound => 'External Source Field';

  @override
  String get channelsStatus => 'Status';

  @override
  String get channelsSystemNotification => 'System Notification';

  @override
  String get channelsTargetField => 'Target Field';

  @override
  String get channelsTargetFieldInbound => 'Internal Domain Field (Target)';

  @override
  String get channelsTestConnection => 'Test Connection';

  @override
  String get channelsTitle => 'Integration Channels';

  @override
  String get channelsType => 'Type';

  @override
  String get channelsWebhookCopy => 'Copy URL';

  @override
  String get channelsWebhookUrl => 'Webhook URL';

  @override
  String get channelsWsMethod => 'HTTP Method';

  @override
  String get channelsWsUrl => 'Endpoint URL';

  @override
  String get integrationChannels => 'Integration Channels';

  @override
  String get integrationDirection => 'Direction';

  @override
  String get integrationHistoryBtn => 'Integration Log';

  @override
  String get integrationLogDetail => 'Integration Monitoring Log Detail';

  @override
  String get integrationLogInfo => 'Integration Log Details';

  @override
  String get integrationMappedPayload => 'Mapped Output Payload';

  @override
  String get integrationOriginalPayload => 'Original Received Payload';

  @override
  String get integrationReceivedAt => 'Received At';

  @override
  String get integrationStatus => 'Status';

  @override
  String get isActiveLabel => 'Active Status';

  @override
  String get isActiveStatus => '🟢 Active Status';

  @override
  String get koLang => 'Korean';

  @override
  String get labelDrafter => 'Drafter';

  @override
  String get labelPassword => 'Password';

  @override
  String get labelTimezone => 'Timezone';

  @override
  String get language => 'Language';

  @override
  String get lastSnapshot => 'Last Snapshot';

  @override
  String get lineageGuideFlow =>
      'Pipeline Flow: Source System (Blue) → Version History (Orange) → Golden Master Record (Purple) → Outbound Integration (Green)';

  @override
  String get lineageGuideTitle => 'Data Lineage Visualization Guide';

  @override
  String get lineageGuideZoom =>
      'Graph Zoom/Drag: Mouse wheel zoom & click node for detailed diff';

  @override
  String get lineageTimelineGuideTitle =>
      'Data Lineage Node Classification Guide';

  @override
  String get loadingData => 'Loading data...';

  @override
  String get manageMembers => 'Manage Members';

  @override
  String get manageSectorsGroups => 'Manage Sectors & Groups';

  @override
  String get mappedPayloadTitle => 'mapped_payload.json';

  @override
  String get maxValue => 'Max value';

  @override
  String get menuAccessStatistics => 'Menu Access Statistics';

  @override
  String get menuIcon => 'Menu Icon';

  @override
  String get menuManagement => 'Menu Management';

  @override
  String get menuManagementDesc =>
      'Manage system tree menu structures and role-based access nodes.';

  @override
  String get mergeAutoSurvivorship => 'Auto Survivorship';

  @override
  String get autoSurvivorship => 'Auto Survivorship';

  @override
  String get mergeCancel => 'Cancel';

  @override
  String get mergeExecuteMerge => 'Execute Merge';

  @override
  String get executeMerge => 'Execute Merge';

  @override
  String get mergeFieldComparison => 'Field Comparison';

  @override
  String get fieldComparison => 'Field Comparison';

  @override
  String get mergeFieldName => 'Field Name';

  @override
  String get fieldName => 'Field Name';

  @override
  String get mergeManualSelect => 'Manual Selection';

  @override
  String get manualSelect => 'Manual Selection';

  @override
  String get mergeMergeFail => 'Failed to merge records';

  @override
  String get mergeFail => 'Failed to merge records';

  @override
  String get mergeMergeSuccess => 'Records merged successfully';

  @override
  String get mergeSuccess => 'Records merged successfully';

  @override
  String get mergeMergedRecords => 'Merged Records';

  @override
  String get mergedRecords => 'Merged Records';

  @override
  String get mergePreview => 'Merge Result Preview';

  @override
  String get preview => 'Merge Result Preview';

  @override
  String get mergeSelectedValue => 'Selected Value';

  @override
  String get selectedValue => 'Selected Value';

  @override
  String get mergeSource => 'Source';

  @override
  String get source => 'Source';

  @override
  String get mergeSurvivorRecord => 'Survivor Record';

  @override
  String get survivorRecord => 'Survivor Record';

  @override
  String get mergeTitle => 'Merge Records';

  @override
  String get mergeUnmerge => 'Unmerge';

  @override
  String get unmerge => 'Unmerge';

  @override
  String get mergeUnmergeConfirmMsg =>
      'Are you sure you want to unmerge this record?';

  @override
  String get unmergeConfirmMsg =>
      'Are you sure you want to unmerge this record?';

  @override
  String get mergeUnmergeConfirmTitle => 'Confirm Unmerge';

  @override
  String get unmergeConfirmTitle => 'Confirm Unmerge';

  @override
  String get mergeUnmergeFail => 'Failed to unmerge record';

  @override
  String get unmergeFail => 'Failed to unmerge record';

  @override
  String get mergeUnmergeSuccess => 'Record unmerged and restored successfully';

  @override
  String get unmergeSuccess => 'Record unmerged and restored successfully';

  @override
  String get messengerAttachFileTooltip => 'Attach file';

  @override
  String get attachfiletooltip => 'Attach file';

  @override
  String get messengerCalendarTitle => 'Jump to Date';

  @override
  String get calendartitle => 'Jump to Date';

  @override
  String get messengerCancelBtn => 'Cancel';

  @override
  String get cancelbtn => 'Cancel';

  @override
  String get messengerCloseBtn => 'Close';

  @override
  String get closebtn => 'Close';

  @override
  String get messengerConfirmBtn => 'Confirm';

  @override
  String get confirmbtn => 'Confirm';

  @override
  String get messengerContentLabel => 'Content';

  @override
  String get contentlabel => 'Content';

  @override
  String get messengerContextCopy => 'Copy';

  @override
  String get contextcopy => 'Copy';

  @override
  String get messengerContextDelete => 'Delete';

  @override
  String get contextdelete => 'Delete';

  @override
  String get messengerContextForward => 'Forward';

  @override
  String get contextforward => 'Forward';

  @override
  String get messengerCopiedToClipboard => '📋 Copied to clipboard!';

  @override
  String get copiedtoclipboard => '📋 Copied to clipboard!';

  @override
  String get messengerCreateBtn => 'Create';

  @override
  String get createbtn => 'Create';

  @override
  String get messengerCreateGroupRoomBtn => '+ Create Group Room';

  @override
  String get creategrouproombtn => '+ Create Group Room';

  @override
  String get messengerCreateGroupRoomTitle => '👥 Create Group Room';

  @override
  String get creategrouproomtitle => '👥 Create Group Room';

  @override
  String get messengerCreateRoomTooltip => 'Create Group Room';

  @override
  String get createroomtooltip => 'Create Group Room';

  @override
  String get messengerCreatorBadge => 'Host';

  @override
  String get creatorbadge => 'Host';

  @override
  String get messengerDeptCol => 'Department';

  @override
  String get deptcol => 'Department';

  @override
  String get messengerDay => '';

  @override
  String get day => '';

  @override
  String get messengerDayFri => 'Friday';

  @override
  String get dayfri => 'Friday';

  @override
  String get messengerDayMon => 'Monday';

  @override
  String get daymon => 'Monday';

  @override
  String get messengerDaySat => 'Saturday';

  @override
  String get daysat => 'Saturday';

  @override
  String get messengerDaySun => 'Sunday';

  @override
  String get daysun => 'Sunday';

  @override
  String get messengerDayThu => 'Thursday';

  @override
  String get daythu => 'Thursday';

  @override
  String get messengerDayTue => 'Tuesday';

  @override
  String get daytue => 'Tuesday';

  @override
  String get messengerDayWed => 'Wednesday';

  @override
  String get daywed => 'Wednesday';

  @override
  String get messengerDownloadFile => 'Download';

  @override
  String get downloadfile => 'Download';

  @override
  String get messengerForwardTitle => '↗️ Forward Message';

  @override
  String get forwardtitle => '↗️ Forward Message';

  @override
  String get messengerForwardedFilePrefix => '[Forwarded File]';

  @override
  String get forwardedfileprefix => '[Forwarded File]';

  @override
  String get messengerForwardedImgPrefix => '[Forwarded Image]';

  @override
  String get forwardedimgprefix => '[Forwarded Image]';

  @override
  String get messengerInviteUserBtn => '+ Invite User';

  @override
  String get inviteuserbtn => '+ Invite User';

  @override
  String get messengerInviteModalTitle => '🤝 Invite Users to Room';

  @override
  String get invitemodaltitle => '🤝 Invite Users to Room';

  @override
  String get messengerKickUserBtn => 'Kick';

  @override
  String get kickuserbtn => 'Kick';

  @override
  String get messengerKickConfirmTitle => '🚪 Kick Participant';

  @override
  String get kickconfirmtitle => '🚪 Kick Participant';

  @override
  String get messengerKickConfirmDesc =>
      'Are you sure you want to kick this participant from the room?';

  @override
  String get kickconfirmdesc =>
      'Are you sure you want to kick this participant from the room?';

  @override
  String get messengerOnlineStatus => 'Online';

  @override
  String get onlinestatus => 'Online';

  @override
  String get messengerOrgCol => 'Organization';

  @override
  String get orgcol => 'Organization';

  @override
  String get messengerPastMessageOptionTitle => 'Past Message Visibility';

  @override
  String get pastmessageoptiontitle => 'Past Message Visibility';

  @override
  String get messengerPastMessageNone => 'Hidden (Default)';

  @override
  String get pastmessagenone => 'Hidden (Default)';

  @override
  String get messengerPastMessage1h => 'Last 1 hour';

  @override
  String get pastmessage1h => 'Last 1 hour';

  @override
  String get messengerPastMessage24h => 'Last 24 hours';

  @override
  String get pastmessage24h => 'Last 24 hours';

  @override
  String get messengerPastMessage48h => 'Last 48 hours';

  @override
  String get pastmessage48h => 'Last 48 hours';

  @override
  String get messengerForwardedPrefix => '[Forwarded Message]';

  @override
  String get forwardedprefix => '[Forwarded Message]';

  @override
  String get messengerHideTranslation => 'Hide Translation';

  @override
  String get hidetranslation => 'Hide Translation';

  @override
  String get messengerMeBadge => 'Me';

  @override
  String get mebadge => 'Me';

  @override
  String get messengerMonth => ' ';

  @override
  String get month => ' ';

  @override
  String get messengerNoDialogue => 'No messages yet.';

  @override
  String get nodialogue => 'No messages yet.';

  @override
  String get messengerNoRooms => 'No active chat rooms.';

  @override
  String get norooms => 'No active chat rooms.';

  @override
  String get messengerNoUserFound => 'No users found.';

  @override
  String get nouserfound => 'No users found.';

  @override
  String get messengerRoomSettings => 'Room Settings';

  @override
  String get roomsettings => 'Room Settings';

  @override
  String get messengerLeaveRoom => 'Leave Room';

  @override
  String get leaveroom => 'Leave Room';

  @override
  String get messengerDeleteRoom => 'Delete Room';

  @override
  String get deleteroom => 'Delete Room';

  @override
  String get messengerDelegateCreator => 'Delegate Creator';

  @override
  String get delegatecreator => 'Delegate Creator';

  @override
  String get messengerConfirmLeaveTitle => 'Leave Room';

  @override
  String get confirmleavetitle => 'Leave Room';

  @override
  String get messengerConfirmLeaveDesc =>
      'Are you sure you want to leave this room? You will no longer see the messages.';

  @override
  String get confirmleavedesc =>
      'Are you sure you want to leave this room? You will no longer see the messages.';

  @override
  String get messengerConfirmDeleteTitle => 'Delete Room';

  @override
  String get confirmdeletetitle => 'Delete Room';

  @override
  String get messengerConfirmDeleteDesc =>
      'Are you sure you want to delete this room? All messages will be permanently deleted.';

  @override
  String get confirmdeletedesc =>
      'Are you sure you want to delete this room? All messages will be permanently deleted.';

  @override
  String get messengerDelegateCreatorTitle => 'Delegate Creator';

  @override
  String get delegatecreatortitle => 'Delegate Creator';

  @override
  String get messengerDelegateCreatorDesc =>
      'Select a user to transfer the creator role to.';

  @override
  String get delegatecreatordesc =>
      'Select a user to transfer the creator role to.';

  @override
  String messengerConfirmDelegateCreatorDesc(Object username) {
    return 'Are you sure you want to delegate the creator role to $username? This action cannot be undone.';
  }

  @override
  String confirmdelegatecreatordesc(Object username) {
    return 'Are you sure you want to delegate the creator role to $username? This action cannot be undone.';
  }

  @override
  String messengerSystemLeave(Object name) {
    return '$name has left the room.';
  }

  @override
  String systemLeave(Object name) {
    return '$name has left the room.';
  }

  @override
  String messengerSystemJoin(Object name) {
    return '$name was invited to the room.';
  }

  @override
  String systemJoin(Object name) {
    return '$name was invited to the room.';
  }

  @override
  String get messengerOfflineStatus => 'Offline';

  @override
  String get offlinestatus => 'Offline';

  @override
  String get messengerPlaceholderMsg => 'Type a message or Ctrl+V image...';

  @override
  String get placeholdermsg => 'Type a message or Ctrl+V image...';

  @override
  String get messengerRadioBroadcastTab => '📢 Live Broadcast';

  @override
  String get radiobroadcasttab => '📢 Live Broadcast';

  @override
  String get messengerRadioChannelUrl => 'YouTube Channel / Music URL';

  @override
  String get radiochannelurl => 'YouTube Channel / Music URL';

  @override
  String get messengerRadioChannelUrlPlaceholder =>
      'https://www.youtube.com/@mychannel or YouTube Music profile...';

  @override
  String get radiochannelurlplaceholder =>
      'https://www.youtube.com/@mychannel or YouTube Music profile...';

  @override
  String get messengerRadioConfigDesc =>
      'Sync your personal YouTube Music / YouTube Channel and playlist to easily broadcast with a single click.';

  @override
  String get radioconfigdesc =>
      'Sync your personal YouTube Music / YouTube Channel and playlist to easily broadcast with a single click.';

  @override
  String get messengerRadioConnectTab => '🔗 Sync YouTube / Playlist';

  @override
  String get radioconnecttab => '🔗 Sync YouTube / Playlist';

  @override
  String get messengerRadioCustomTitleLabel =>
      'Track / Broadcast Title (Optional)';

  @override
  String get radiocustomtitlelabel => 'Track / Broadcast Title (Optional)';

  @override
  String get messengerRadioCustomTitlePlaceholder =>
      'e.g.: ☕ Afternoon Healing Lofi BGM';

  @override
  String get radiocustomtitleplaceholder =>
      'e.g.: ☕ Afternoon Healing Lofi BGM';

  @override
  String get messengerRadioDjBadge => 'DJ';

  @override
  String get radiodjbadge => 'DJ';

  @override
  String get messengerRadioDjTitle => '🎵 DJ Control Panel';

  @override
  String get radiodjtitle => '🎵 DJ Control Panel';

  @override
  String get messengerRadioListenBtn => 'Listen to Radio';

  @override
  String get radiolistenbtn => 'Listen to Radio';

  @override
  String get messengerRadioMyPlaylistDefault => 'My Synced Playlist';

  @override
  String get radiomyplaylistdefault => 'My Synced Playlist';

  @override
  String get messengerRadioPlayThisPlaylist => 'Play This Playlist';

  @override
  String get radioplaythisplaylist => 'Play This Playlist';

  @override
  String get messengerRadioPlayingNow => 'Currently Live Broadcasting';

  @override
  String get radioplayingnow => 'Currently Live Broadcasting';

  @override
  String get messengerRadioPlaylistTitle => 'Playlist Title';

  @override
  String get radioplaylisttitle => 'Playlist Title';

  @override
  String get messengerRadioPlaylistTitlePlaceholder =>
      'e.g.: 🎧 My YouTube Music Playlist';

  @override
  String get radioplaylisttitleplaceholder =>
      'e.g.: 🎧 My YouTube Music Playlist';

  @override
  String get messengerRadioPlaylistUrl => 'Playlist URL / ID';

  @override
  String get radioplaylisturl => 'Playlist URL / ID';

  @override
  String get messengerRadioPlaylistUrlPlaceholder =>
      'https://www.youtube.com/playlist?list=PL12345... or PL12345';

  @override
  String get radioplaylisturlplaceholder =>
      'https://www.youtube.com/playlist?list=PL12345... or PL12345';

  @override
  String get messengerRadioSaveConfig => 'Save YouTube Sync Config';

  @override
  String get radiosaveconfig => 'Save YouTube Sync Config';

  @override
  String get messengerRadioStartBroadcast =>
      'Start Live Broadcast to All Users';

  @override
  String get radiostartbroadcast => 'Start Live Broadcast to All Users';

  @override
  String get messengerRadioStopBroadcast => 'Stop Broadcast';

  @override
  String get radiostopbroadcast => 'Stop Broadcast';

  @override
  String get messengerRadioTrackTitle => 'Track Title';

  @override
  String get radiotracktitle => 'Track Title';

  @override
  String get messengerRadioUrlPlaceholder => 'Enter audio stream URL...';

  @override
  String get radiourlplaceholder => 'Enter audio stream URL...';

  @override
  String get messengerRoleCol => 'Role';

  @override
  String get rolecol => 'Role';

  @override
  String get messengerRoomMembersTitle => 'Room Members';

  @override
  String get roommemberstitle => 'Room Members';

  @override
  String get messengerRoomNameLabel => 'Room Name';

  @override
  String get roomnamelabel => 'Room Name';

  @override
  String get messengerSearchUserPlaceholder => 'Search user (Name, ID)...';

  @override
  String get searchuserplaceholder => 'Search user (Name, ID)...';

  @override
  String get messengerSelectUsersLabel => 'Select Participants:';

  @override
  String get selectuserslabel => 'Select Participants:';

  @override
  String get messengerSendBtn => 'Send';

  @override
  String get sendbtn => 'Send';

  @override
  String get messengerTitle => '💬 Messenger';

  @override
  String get messengerTranslateMessage => 'Translate Message';

  @override
  String get translatemessage => 'Translate Message';

  @override
  String get messengerTranslating => 'Translating...';

  @override
  String get translating => 'Translating...';

  @override
  String get messengerTranslationError => 'Failed to translate message.';

  @override
  String get translationerror => 'Failed to translate message.';

  @override
  String get messengerUnblockUser => 'Unblock';

  @override
  String get unblockuser => 'Unblock';

  @override
  String get messengerUnreadMessagesDesc => 'unread messages.';

  @override
  String get unreadmessagesdesc => 'unread messages.';

  @override
  String get messengerUsernameCol => 'Username';

  @override
  String get usernamecol => 'Username';

  @override
  String get messengerViewMembersTooltip => 'View Members';

  @override
  String get viewmemberstooltip => 'View Members';

  @override
  String get messengerWriterLabel => 'Author';

  @override
  String get writerlabel => 'Author';

  @override
  String get messengerYear => ', ';

  @override
  String get year => ', ';

  @override
  String get minValue => 'Min Value';

  @override
  String get modified => 'Modified';

  @override
  String get multiValue => 'Multi-Value';

  @override
  String get multilingual => 'Multilingual';

  @override
  String get myToDoList => 'My To-Do List';

  @override
  String get newData => 'New Data';

  @override
  String get nextSnapshot => 'Next Snapshot';

  @override
  String get nocomment => 'No comment';

  @override
  String get noActiveWorkflow =>
      'No active approval workflow configured for this domain/node.';

  @override
  String get noAffectedChannels => 'No active integration channels linked';

  @override
  String get noAssignedMembers =>
      'No members currently assigned to this department. Search and register members below.';

  @override
  String get noChangeContent => 'No Changes';

  @override
  String get noChangesFound => 'No changes found.';

  @override
  String get noDiffOrInitialVersion =>
      'No changes found or this is the initial version.';

  @override
  String get noDifferencesFound =>
      'No differences found between selected records.';

  @override
  String get noHistoryData => 'No history data available.';

  @override
  String get noPendingTasksYou => 'No pending tasks. You\\';

  @override
  String get noPermission => 'You do not have permission for this action.';

  @override
  String get noTableData => 'No table data available.';

  @override
  String get none => 'None';

  @override
  String get notice => 'Notice';

  @override
  String get notification => 'Notification';

  @override
  String get notificationsApprovalFinalized => 'Approval Request Finalized';

  @override
  String get approvalFinalized => 'Approval Request Finalized';

  @override
  String get notificationsApprovalPending => 'Approval Request Pending';

  @override
  String get approvalPending => 'Approval Request Pending';

  @override
  String get notificationsApprovalRejected => 'Approval Request Rejected';

  @override
  String get approvalRejected => 'Approval Request Rejected';

  @override
  String get notificationsApprovalStepApproved => 'Approval Step Approved';

  @override
  String get approvalStepApproved => 'Approval Step Approved';

  @override
  String get notificationsDeleteAll => 'Delete all';

  @override
  String get deleteAll => 'Delete all';

  @override
  String get notificationsMarkAllRead => 'Mark all as read';

  @override
  String get markAllRead => 'Mark All as Read';

  @override
  String get notificationsNoNotifications => 'No notifications';

  @override
  String get noNotifications => 'No new notifications.';

  @override
  String get notificationsTitle => 'Notification Center';

  @override
  String get notificationsTypeApproval => 'Approval';

  @override
  String get typeApproval => 'Approval';

  @override
  String get notificationsTypeDq => 'Data Quality';

  @override
  String get typeDq => 'Data Quality';

  @override
  String get notificationsTypeInfo => 'Info';

  @override
  String get typeInfo => 'Info';

  @override
  String get notificationsTypeWarning => 'Warning';

  @override
  String get typeWarning => 'Warning';

  @override
  String get notifiedPersons => 'Notified Persons (CC)';

  @override
  String get number => 'Number';

  @override
  String get onlyDifferences => 'Show Only Differences';

  @override
  String get opBelow => 'below';

  @override
  String get below => 'below';

  @override
  String get opContains => 'Contains';

  @override
  String get contains => 'Contains';

  @override
  String get opEndsWith => 'Ends';

  @override
  String get endsWith => 'Ends';

  @override
  String get opEnterKeyword => 'Enter keyword';

  @override
  String get enterKeyword => 'Enter keyword';

  @override
  String get opEnterNumber => 'Enter number';

  @override
  String get enterNumber => 'Enter number';

  @override
  String get opEq => 'Equals';

  @override
  String get eq => 'Equals';

  @override
  String get opMaxValue => 'Max value';

  @override
  String get opRange => 'Range';

  @override
  String get range => 'Range';

  @override
  String get opSelectOption => 'Select an option';

  @override
  String get selectOption => 'Select an option';

  @override
  String get opStartsWith => 'Starts';

  @override
  String get startsWith => 'Starts';

  @override
  String get openTableModal => 'Expand in Large Modal Viewer';

  @override
  String get openTableModalBtn => 'Expand';

  @override
  String get openTableModalBtnTitle => 'Open in Large Modal Viewer';

  @override
  String get otherRequest => 'Other Request';

  @override
  String get outgoingPayloadTitle => 'outgoing_payload.json';

  @override
  String get password => 'Password';

  @override
  String get pasteOptionDesc =>
      'How would you like to send the clipboard data (Excel table/Text/Image)?';

  @override
  String get pasteOptionTitle => 'Select Paste Data Send Format';

  @override
  String get path => 'Path';

  @override
  String get pendingFieldApprovalWarning =>
      'Contains fields with pending change approvals.';

  @override
  String get permActionLabel => 'Action / Identifier (e.g. export, execute)';

  @override
  String get permAll => 'All';

  @override
  String get permLabelEnLabel => 'Permission English Label (e.g. Export)';

  @override
  String get permLabelLabel => 'Permission Korean Label (e.g. 내보내기)';

  @override
  String get permRead => 'Read';

  @override
  String get permWrite => 'Write';

  @override
  String get permissions => 'Permissions';

  @override
  String get permissionsMatrixTitle => 'Permissions Matrix';

  @override
  String get placeholderPassword => 'Enter your password';

  @override
  String get placeholderTimezone => 'Select your timezone';

  @override
  String get prevSnapshot => 'Prev Snapshot';

  @override
  String get proceedAnyway => 'Proceed with Risk';

  @override
  String get processDate => 'Process Date';

  @override
  String get processedBy => 'Processed By';

  @override
  String get processor => 'Processor';

  @override
  String get propertyName => 'Property';

  @override
  String get proxyapprove => 'Proxy Approve';

  @override
  String get proxyapproveconfirm => 'Confirm proxy approve?';

  @override
  String get proxyapprovefail => 'Failed to proxy approve.';

  @override
  String get proxyreject => 'Proxy Reject';

  @override
  String get proxyrejectconfirm => 'Confirm proxy reject?';

  @override
  String get proxyrejectfail => 'Failed to proxy reject.';

  @override
  String get rawData => 'Raw Data';

  @override
  String get readOnly => 'Read-Only';

  @override
  String get readingPreviousData => 'Reading previous data. (Read-only)';

  @override
  String get readonlySnapshotMsg =>
      'Viewing previous data snapshot. (Read-only)';

  @override
  String get reflectDate => 'Reflect Date';

  @override
  String get remove => 'Remove';

  @override
  String get removeSelected => 'Remove Selected';

  @override
  String get reqId => 'Req ID';

  @override
  String get requesttype => 'Request Type';

  @override
  String get required => 'Required';

  @override
  String get reset => 'Reset';

  @override
  String get responseResultTitle => 'response_result.txt';

  @override
  String get retryIntegration => 'Retry Integration';

  @override
  String get riskLevel => 'Risk Level';

  @override
  String get saveChanges => 'Save Changes';

  @override
  String get saveChangesHint =>
      '* Click the \'Save\' button at the top or bottom after editing cells to apply changes.';

  @override
  String get schemaChange => 'Schema Change';

  @override
  String get scopeCol => 'Scope';

  @override
  String get scopeLevel => 'Scope Level *';

  @override
  String get search => 'Search';

  @override
  String get searchCondition => 'Search Condition';

  @override
  String get searchFilters => 'Search Filters';

  @override
  String get searchInTable => 'Search in Table Data...';

  @override
  String get searchKeyword => 'Search Keyword';

  @override
  String get searchWorkflowPlaceholder => 'Search title or description...';

  @override
  String get searchable => 'Searchable';

  @override
  String get sector => 'Sector';

  @override
  String get select => 'Select';

  @override
  String get selectIconDesc =>
      'Select a custom icon to display on department nodes and headers:';

  @override
  String get selectIconTitle => 'Select Icon';

  @override
  String get selectMenuPrompt => 'Select a menu from the tree to edit.';

  @override
  String get selectedCount => 'Selected';

  @override
  String get sendAsImage => '🖼️ Send as Image';

  @override
  String get sendAsTextData => '📋 Send as Text/Table Data';

  @override
  String get setAsBaseline => 'Set as Baseline';

  @override
  String get setDefaultWorkflowDesc => '⭐ Set as Default Form for this Action';

  @override
  String get showRawData => 'Show Raw Unmodified Data';

  @override
  String get snapshotViewingNotice =>
      'Viewing historic data snapshot. (Read Only)';

  @override
  String get startDate => 'Start Date';

  @override
  String get statusActive => '🟢 Active';

  @override
  String get statusCol => 'Status';

  @override
  String get statusFilter => 'Status';

  @override
  String get statusIgnored => 'Keep Separate';

  @override
  String get statusInactive => '🔴 Inactive';

  @override
  String get statusMerged => 'Merged';

  @override
  String get stepdraft => 'Submitted';

  @override
  String get steppending => 'Pending';

  @override
  String get stepConsensus => 'Consensus';

  @override
  String get stepNameEnPlaceholder => 'Step Title (EN)';

  @override
  String get stepNameKoPlaceholder => 'Step Title (KO)';

  @override
  String get stepNamePlaceholder => 'Step Name (e.g. 1st Data Review)';

  @override
  String get stepPrefix => 'Step';

  @override
  String get stepTypeConsultation => 'Consultation';

  @override
  String stepsCount(Object count) {
    return '$count Step Approval';
  }

  @override
  String get submissionCommentNotice =>
      '(Optional) Enter a reason or comment for the approver.';

  @override
  String get submissionCommentPlaceholder =>
      'Enter your comment for the approver...';

  @override
  String get submissionCommentTitle => 'Submission Comment';

  @override
  String get submissionDate => 'Submission Date';

  @override
  String get submissionReason => 'Submission Reason';

  @override
  String get successDelete => 'Successfully deleted.';

  @override
  String get successSave => 'Successfully saved.';

  @override
  String get survivorshipAddFirstRule => 'Add First Rule';

  @override
  String get survivorshipAddRule => 'Add Rule';

  @override
  String get survivorshipDescription =>
      'Configure field-level survival priority and conflict resolution rules for golden record creation during merge.';

  @override
  String get survivorshipEmptyNoDomain => 'No Domain Selected';

  @override
  String get emptyNoDomain => 'No Domain Selected';

  @override
  String get survivorshipEmptyNoRules => 'No survivorship rules configured.';

  @override
  String get survivorshipEmptySubDomain =>
      'Select a domain from the dropdown above to view survivorship rules.';

  @override
  String get emptySubDomain =>
      'Select a domain from the dropdown above to view survivorship rules.';

  @override
  String get survivorshipEmptySubRules =>
      'Click \'+ Add Rule\' on the top right to configure field merging rules.';

  @override
  String get emptySubRules =>
      'Click \'+ Add Rule\' on the top right to configure field merging rules.';

  @override
  String get survivorshipFieldKey => 'Domain Field (Field Key)';

  @override
  String get fieldKey => 'Field Key';

  @override
  String get survivorshipGuideText =>
      'Configure optimal merge rules for each domain among SOURCE_PRIORITY, MOST_RECENT, and MOST_COMPLETE.';

  @override
  String get guideText =>
      'Configure optimal merge rules for each domain among SOURCE_PRIORITY, MOST_RECENT, and MOST_COMPLETE.';

  @override
  String get survivorshipGuideTitle => 'Survivorship Strategy Guide';

  @override
  String get guideTitle => 'Survivorship Strategy Guide';

  @override
  String get survivorshipItemsCount => ' items';

  @override
  String get survivorshipKpiDomain => 'Domain:';

  @override
  String get kpiDomain => 'Domain:';

  @override
  String get survivorshipKpiFields => 'Domain Fields:';

  @override
  String get kpiFields => 'Domain Fields:';

  @override
  String get survivorshipKpiRules => 'Rules:';

  @override
  String get kpiRules => 'Rules:';

  @override
  String get survivorshipLoadDomainsFail => 'Failed to load domain list.';

  @override
  String get loadDomainsFail => 'Failed to load domain list.';

  @override
  String get survivorshipMostComplete => 'MOST_COMPLETE (Highest Completeness)';

  @override
  String get mostComplete => 'MOST_COMPLETE (Highest Completeness)';

  @override
  String get survivorshipMostRecent => 'MOST_RECENT (Most Recent Timestamp)';

  @override
  String get mostRecent => 'MOST_RECENT (Most Recent Timestamp)';

  @override
  String get survivorshipPriority => 'Priority';

  @override
  String get priority => 'Priority';

  @override
  String get survivorshipRefresh => 'Refresh';

  @override
  String get survivorshipRuleList => 'Rule List';

  @override
  String get survivorshipSaveFail =>
      'Error occurred while saving survivorship rules.';

  @override
  String get saveFail => 'Error occurred while saving survivorship rules.';

  @override
  String get survivorshipSaveSettings => 'Save Settings';

  @override
  String get saveSettings => 'Save Settings';

  @override
  String get survivorshipSaveSuccess =>
      'Survivorship rules saved successfully.';

  @override
  String get survivorshipSelectDomainPlaceholder => 'Select Domain';

  @override
  String get survivorshipSourcePriority =>
      'SOURCE_PRIORITY (Source System Priority)';

  @override
  String get sourcePriority => 'SOURCE_PRIORITY (Source System Priority)';

  @override
  String get survivorshipStatAvailableFields => 'Available Fields';

  @override
  String get statAvailableFields => 'Available Fields';

  @override
  String get survivorshipStatCurrentDomain => 'Current Domain';

  @override
  String get statCurrentDomain => 'Current Domain';

  @override
  String get survivorshipStrategy => 'Survivorship Strategy';

  @override
  String get strategy => 'Survivorship Strategy';

  @override
  String get survivorshipStrategyDescSourcePriority =>
      'Prioritizes data field values from the designated source system (e.g., Legacy ERP, CRM).';

  @override
  String get strategyDescSourcePriority =>
      'Prioritizes data field values from the designated source system (e.g., Legacy ERP, CRM).';

  @override
  String get survivorshipStrategyDescMostRecent =>
      'Adopts the field value of the record that was most recently created or updated.';

  @override
  String get strategyDescMostRecent =>
      'Adopts the field value of the record that was most recently created or updated.';

  @override
  String get survivorshipStrategyDescMostComplete =>
      'Adopts the valid field value with the most information and longest data length, not Null.';

  @override
  String get strategyDescMostComplete =>
      'Adopts the valid field value with the most information and longest data length, not Null.';

  @override
  String get survivorshipTitle => 'Survivorship Rules';

  @override
  String get syncMenuSeed => 'Sync from Seed File';

  @override
  String get syntaxErrorFormula => 'Syntax error in formula';

  @override
  String syntaxErrorInFormulaEMessage(Object message) {
    return 'Syntax error in formula: $message';
  }

  @override
  String get tabWorkflows => 'Workflows';

  @override
  String get tableViewerTitle => 'Messenger Data Table Viewer';

  @override
  String get targetbulkupload => 'Bulk Upload';

  @override
  String get targetrecordcreate => 'Record Create';

  @override
  String get targetrecorddelete => 'Record Delete';

  @override
  String get targetrecordupdate => 'Record Update';

  @override
  String get targetDomainRefNotLoaded => 'Target domain reference not loaded.';

  @override
  String get text => 'Text';

  @override
  String get time => 'Time';

  @override
  String get timelineList => 'Timeline List';

  @override
  String get timezone => 'Timezone Settings';

  @override
  String get timezoneSelect => 'Select Timezone';

  @override
  String get toValue => '~ To';

  @override
  String get today => 'Today';

  @override
  String get treeEmptyMessage =>
      'No classification tree found. Please click the Domain button below to create a new domain.';

  @override
  String get typeconsensus => 'Consensus';

  @override
  String get typedraft => 'Draft';

  @override
  String get unassign => 'Unassign';

  @override
  String get unassigned => 'Unassigned';

  @override
  String get unclassified => 'Unclassified';

  @override
  String get unit => 'Unit';

  @override
  String get unknown => 'Unknown';

  @override
  String get unmaskReasonDesc =>
      'Please enter a reason for accessing the original sensitive data. This reason will be securely recorded in the audit logs.';

  @override
  String get unmaskReasonTitle => 'Enter reason for unmasking';

  @override
  String get unmergeBtn => 'Unmerge';

  @override
  String get update => 'Update';

  @override
  String get updateSuccess => 'Update Completed';

  @override
  String get updatedat => 'Updated At';

  @override
  String get notificationCenter => 'Notification Center';

  @override
  String get downloadTemplate => 'Download Excel Template';

  @override
  String get exportExcelCsv => 'Export Records (CSV/Excel)';

  @override
  String get editorBold => 'Bold';

  @override
  String get editorItalic => 'Italic';

  @override
  String get editorUnderline => 'Underline';

  @override
  String get editorStrike => 'Strike';

  @override
  String get editorHeading1 => 'Heading 1 (H1)';

  @override
  String get editorHeading2 => 'Heading 2 (H2)';

  @override
  String get editorHeading3 => 'Heading 3 (H3)';

  @override
  String get editorParagraph => 'Paragraph';

  @override
  String get editorBulletList => 'Bullet List';

  @override
  String get editorOrderedList => 'Ordered List';

  @override
  String get editorBlockquote => 'Blockquote';

  @override
  String get editorCodeBlock => 'Code Block';

  @override
  String get editorAlignLeft => 'Align Left';

  @override
  String get editorAlignCenter => 'Align Center';

  @override
  String get editorAlignRight => 'Align Right';

  @override
  String get editorAlignJustify => 'Justify';

  @override
  String get editorHorizontalRule => 'Horizontal Line';

  @override
  String get editorUndo => 'Undo';

  @override
  String get editorRedo => 'Redo';

  @override
  String get editorPlaceholder => 'Enter content...';

  @override
  String get editorImage => 'Insert Image';

  @override
  String get uploadingImage => 'Uploading image...';

  @override
  String get failedUploadImage => 'Failed to upload image.';

  @override
  String get uploadImage => 'Upload Image';

  @override
  String get dragDropImageHint =>
      'Drag & drop images here or click to browse (Ctrl+V supported)';

  @override
  String get deleteImage => 'Delete Image';

  @override
  String get downloadImage => 'Download Image';

  @override
  String get zoomIn => 'Zoom In';

  @override
  String get zoomOut => 'Zoom Out';

  @override
  String get zoomReset => 'Reset Zoom';

  @override
  String get noImage => 'No image registered.';

  @override
  String get imageCarouselPrev => 'Previous Image';

  @override
  String get imageCarouselNext => 'Next Image';

  @override
  String get editorFontFamily => 'Font Family';

  @override
  String get editorFontSize => 'Font Size';

  @override
  String get editorTextColor => 'Text Color';

  @override
  String get editorHighlight => 'Highlight Color';

  @override
  String get editorTable => 'Table';

  @override
  String get editorInsertTable => 'Insert Table (3x3)';

  @override
  String get editorAddRowBefore => 'Add Row Above';

  @override
  String get editorAddRowAfter => 'Add Row Below';

  @override
  String get editorDeleteRow => 'Delete Row';

  @override
  String get editorAddColBefore => 'Add Column Left';

  @override
  String get editorAddColAfter => 'Add Column Right';

  @override
  String get editorDeleteCol => 'Delete Column';

  @override
  String get editorMergeCells => 'Merge Cells';

  @override
  String get editorSplitCell => 'Split Cell';

  @override
  String get editorToggleHeaderRow => 'Toggle Header Row';

  @override
  String get editorDeleteTable => 'Delete Table';

  @override
  String get editorTaskList => 'Task List (To-Do)';

  @override
  String get editorClearFormatting => 'Clear Formatting';

  @override
  String get editorFullscreen => 'Full Screen';

  @override
  String get editorExitFullscreen => 'Exit Full Screen';

  @override
  String get editorLink => 'Insert Link';

  @override
  String get editorUnlink => 'Remove Link';

  @override
  String editorCharacterCount(Object count) {
    return '$count chars';
  }

  @override
  String get editorCopyCode => 'Copy Code';

  @override
  String get editorCodeCopied => 'Code copied.';

  @override
  String get modalMaximize => 'Maximize Modal';

  @override
  String get modalRestore => 'Restore Modal Size';

  @override
  String get updatedAt => 'Updated At';

  @override
  String get version => 'Version';

  @override
  String get viewdatachanges => 'View Data Changes';

  @override
  String get viewAfterSnapshot => 'After Snapshot';

  @override
  String get viewBeforeSnapshot => 'Previous Snapshot';

  @override
  String get viewChanges => 'View Changes';

  @override
  String get viewSnapshot => 'View Snapshot';

  @override
  String get visualGraph => 'Visual Graph';

  @override
  String get vuesticCancel => 'Cancel';

  @override
  String get vuesticClose => 'Close';

  @override
  String get vuesticConfirm => 'Confirm';

  @override
  String get vuesticDelete => 'Delete';

  @override
  String get vuesticGoFirstPage => 'Go to first page';

  @override
  String get gofirstpage => 'Go to first page';

  @override
  String get vuesticGoLastPage => 'Go to last page';

  @override
  String get golastpage => 'Go to last page';

  @override
  String get vuesticGoNextPage => 'Go to next page';

  @override
  String get gonextpage => 'Go to next page';

  @override
  String get vuesticGoPreviousPage => 'Go to previous page';

  @override
  String get gopreviouspage => 'Go to previous page';

  @override
  String vuesticGoToSpecificPage(Object page) {
    return 'Go to page $page';
  }

  @override
  String gotospecificpage(Object page) {
    return 'Go to page $page';
  }

  @override
  String get vuesticNoOptions => 'No options available';

  @override
  String get nooptions => 'No options available';

  @override
  String get vuesticOk => 'OK';

  @override
  String get ok => 'OK';

  @override
  String get vuesticOptionsFilter => 'Filter options';

  @override
  String get optionsfilter => 'Filter options';

  @override
  String get vuesticPagination => 'Pagination';

  @override
  String get pagination => 'Pagination';

  @override
  String get vuesticProgressState => 'Progress State';

  @override
  String get progressstate => 'Progress State';

  @override
  String get vuesticReset => 'Reset';

  @override
  String get vuesticSave => 'Save';

  @override
  String get vuesticSearch => 'Search';

  @override
  String get vuesticSelect => 'Select';

  @override
  String get vuesticSelectedOptions => 'Selected options';

  @override
  String get selectedoptions => 'Selected options';

  @override
  String get vuesticSortColumnBy => 'Sort column by';

  @override
  String get sortcolumnby => 'Sort column by';

  @override
  String get vuesticToggleDropdown => 'Toggle dropdown';

  @override
  String get toggledropdown => 'Toggle dropdown';

  @override
  String get vuesticUploadFile => 'Upload File';

  @override
  String get uploadfile => 'Upload File';

  @override
  String get vuesticFileTypeIncorrect => 'File type is incorrect';

  @override
  String get filetypeincorrect => 'File type is incorrect';

  @override
  String get vuesticDropFiles => 'Drop files here to upload';

  @override
  String get dropfiles => 'Drop files here to upload';

  @override
  String get vuesticFilesUploaded => 'Files uploaded';

  @override
  String get filesuploaded => 'Files uploaded';

  @override
  String get vuesticFileSizeIncorrect => 'File size exceeds limit';

  @override
  String get filesizeincorrect => 'File size exceeds limit';

  @override
  String get waitingFor => 'Waiting';

  @override
  String warningChannels(Object count) {
    return 'Please review mapping settings for $count active integration channel(s).';
  }

  @override
  String get welcome => 'Welcome';

  @override
  String get workflowdetails => 'Workflow Details';

  @override
  String get workflowCenterTitle => 'Workflow & Permission Management Center';

  @override
  String get workflowManagement => 'Workflow Management';

  @override
  String get workflowManagementDesc =>
      'Easily explore workflow templates with AG-Grid and manage or edit them using the dedicated modal dialog.';

  @override
  String get workflowNameCol => 'Form Name';

  @override
  String get dataLineage => 'Data Lineage';

  @override
  String get dataLineageDesc =>
      'Visualize reference and data flow lineage between domains, taxonomy nodes, and external channels.';

  @override
  String get lineageNodes => 'Lineage Nodes';

  @override
  String get lineageRelationships => 'Data Pipeline Flows';

  @override
  String warningDqRules(Object count) {
    return '$count mapped Data Quality (DQ) rule(s) will be affected.';
  }

  @override
  String get noAffectedDqRules => 'No DQ rules linked';

  @override
  String get expectedDqViolations => 'Expected DQ Violations';

  @override
  String get affectedDqRules => 'Affected DQ Rules';

  @override
  String get addDqRule => 'Add Rule';

  @override
  String get dqDashboardTitle => 'Data Quality Dashboard';

  @override
  String get dqDashboardSubtitle =>
      'Real-time Master Data Governance & Integrity Monitoring';

  @override
  String get dqDashboardSelectDomainPlaceholder => 'Select a Domain';

  @override
  String get dqDashboardTotalRecords => 'Total Records';

  @override
  String get totalRecords => 'Total Records';

  @override
  String get dqDashboardTotalRecordsSub => 'Monitored Entities in Domain';

  @override
  String get totalRecordsSub => 'Monitored Entities in Domain';

  @override
  String get dqDashboardTotalViolations => 'Total Violations';

  @override
  String get totalViolations => 'Total Violations';

  @override
  String get dqDashboardActionRequired => '⚠️ Action Required';

  @override
  String get dqDashboardAllPassed => '✅ All Records Passed';

  @override
  String get allPassed => '✅ All Records Passed';

  @override
  String get dqDashboardActiveDqRules => 'Active DQ Rules';

  @override
  String get activeDqRules => 'Active DQ Rules';

  @override
  String get dqDashboardActiveRulesSub => 'Automated Inspection Rules';

  @override
  String get activeRulesSub => 'Automated Inspection Rules';

  @override
  String get dqDashboardScoreTrendTitle => 'DQ Score Trend History';

  @override
  String get scoreTrendTitle => 'DQ Score Trend History';

  @override
  String dqDashboardSnapshotCount(Object count) {
    return '$count Snapshots';
  }

  @override
  String snapshotCount(Object count) {
    return '$count Snapshots';
  }

  @override
  String get dqDashboardRecent7Days => 'Last 7 Days';

  @override
  String get recent7Days => 'Last 7 Days';

  @override
  String get dqDashboardRecent30Days => 'Last 30 Days';

  @override
  String get recent30Days => 'Last 30 Days';

  @override
  String get dqDashboardRecent90Days => 'Last 90 Days';

  @override
  String get recent90Days => 'Last 90 Days';

  @override
  String get dqDashboardRecentAll => 'All Time';

  @override
  String get recentAll => 'All Time';

  @override
  String get dqDashboardRunScan => '⚡ Run DQ Scan';

  @override
  String get runScan => 'Run Profiling Scan';

  @override
  String get dqDashboardNoSnapshots => 'No DQ Snapshot History Recorded';

  @override
  String get noSnapshots => 'No snapshots created yet.';

  @override
  String get dqDashboardNoSnapshotsDesc =>
      'Click \'⚡ Run DQ Scan\' to execute real-time master data validation and record snapshot trends.';

  @override
  String get noSnapshotsDesc =>
      'Click \'⚡ Run DQ Scan\' to execute real-time master data validation and record snapshot trends.';

  @override
  String get dqDashboardStartScanNow => '⚡ Start DQ Scan Now';

  @override
  String get startScanNow => '⚡ Start DQ Scan Now';

  @override
  String get dqDashboardAvgScore => 'Average Score:';

  @override
  String get avgScore => 'Average Score:';

  @override
  String get dqDashboardMaxScore => 'Max Score:';

  @override
  String get maxScore => 'Max Score:';

  @override
  String get dqDashboardLatestSnapshot => 'Latest Snapshot:';

  @override
  String get latestSnapshot => 'Latest Snapshot:';

  @override
  String dqDashboardTooltipInfo(Object violations, Object total) {
    return 'Violations: $violations / Total: $total rows';
  }

  @override
  String tooltipInfo(Object violations, Object total) {
    return 'Violations: $violations / Total: $total rows';
  }

  @override
  String get dqDashboardViolationsBySeverity => 'Violations by Severity';

  @override
  String get violationsBySeverity => 'Violations by Severity';

  @override
  String get dqDashboardNoViolationsDetected =>
      'No violations detected! Perfect data quality.';

  @override
  String get noViolationsDetected =>
      'No violations detected! Perfect data quality.';

  @override
  String get dqDashboardViolationsByField => 'Violations by Field';

  @override
  String get violationsByField => 'Violations by Field';

  @override
  String get dqDashboardNoFieldViolations => 'No field violations detected.';

  @override
  String get noFieldViolations => 'No field violations detected.';

  @override
  String get dqDashboardViolationTableTitle =>
      'Detailed Violation Records List';

  @override
  String get violationTableTitle => 'Detailed Violation Records List';

  @override
  String get dqDashboardViolationTableSub =>
      'Real-time failed validation record monitoring';

  @override
  String get violationTableSub =>
      'Real-time failed validation record monitoring';

  @override
  String get dqDashboardSeverity => 'Severity';

  @override
  String get severity => 'Severity';

  @override
  String get dqDashboardField => 'Field';

  @override
  String get field => 'Field';

  @override
  String get dqDashboardLoadingViolations => 'Loading Violation Records...';

  @override
  String get loadingViolations => 'Loading Violation Records...';

  @override
  String get dqDashboardNoViolationsFound =>
      'No violation records found for the selected condition.';

  @override
  String get noViolationsFound =>
      'No violation records found for the selected condition.';

  @override
  String get dqDashboardRecordId => 'Record Identifier';

  @override
  String get recordId => 'Record Identifier';

  @override
  String get dqDashboardViolatedField => 'Violated Field';

  @override
  String get violatedField => 'Violated Field';

  @override
  String get dqDashboardRuleName => 'Inspection Rule';

  @override
  String get dqDashboardViolationMessage => 'Violation Message';

  @override
  String get violationMessage => 'Violation Message';

  @override
  String get dqDashboardActualValue => 'Actual Value';

  @override
  String get actualValue => 'Actual Value';

  @override
  String get dqDashboardEmptyValue => '(Empty)';

  @override
  String get emptyValue => '(Empty)';

  @override
  String get dqDashboardDetails => 'Details';

  @override
  String dqDashboardPaginationSummary(Object start, Object end, Object total) {
    return 'Showing $start - $end of $total records';
  }

  @override
  String paginationSummary(Object start, Object end, Object total) {
    return 'Showing $start - $end of $total records';
  }

  @override
  String get dqDashboardDesc =>
      'Real-time monitoring of data quality rule compliance, error counts, and field diagnosis status by domain.';

  @override
  String get dqErrorMessage => 'Error Message';

  @override
  String get dqParams => 'Parameters';

  @override
  String get dqPermGroupTitle => 'Data Quality Permissions';

  @override
  String get dqRuleType => 'Rule Type';

  @override
  String get dqRulesDesc =>
      'Configure data quality validation rules and inspection parameters by domain field.';

  @override
  String get dqRulesManagement => 'Data Quality Rule Management';

  @override
  String get dqScoreTitle => 'Data Quality Score';

  @override
  String get dqSeverity => 'Severity';

  @override
  String get dqSortOrder => 'Sort Order';

  @override
  String get editDqRule => 'Edit Rule';

  @override
  String errorDqFailed(Object details) {
    return 'Data Quality Check Failed: $details';
  }

  @override
  String get goToDqDashboard => 'Go to DQ Dashboard';

  @override
  String get initiatorRulesTitle => 'Initiator Eligibility Rules';

  @override
  String get loadingDqMetrics => 'Loading Quality Metrics...';

  @override
  String get noRulesDefault =>
      'No rules defined. By default, all users are eligible and have full field access.';

  @override
  String get openDqViolations => 'Open DQ Violations';

  @override
  String get permissionsRulesTitle =>
      'Initiator Eligibility & Field Control Rules';

  @override
  String get runDqScan => 'Run DQ Scan';

  @override
  String get autoRemediation => 'Intelligent DQ Auto Remediation';

  @override
  String get autoRemediationDesc =>
      'Rule-based automatic correction and one-click fix for formatting errors (phone, biz no, whitespace, lowercase).';

  @override
  String get scanRemediation => 'Scan Remediation Candidates';

  @override
  String get applyAllRemediation => 'Apply All Corrections';

  @override
  String get currentVal => 'Current Value (Error)';

  @override
  String get proposedVal => 'Proposed Value (Fixed)';

  @override
  String get remediationReason => 'Remediation Reason';

  @override
  String get noRemediationsNeeded =>
      'No records require formatting remediation.';

  @override
  String get referenceIntegrity => 'Cross-Domain Reference Integrity';

  @override
  String get referenceIntegrityDesc =>
      'Real-time diagnostic scan for orphan references across cross-domain foreign key relations.';

  @override
  String get integrityScore => 'Integrity Score';

  @override
  String get orphanCount => 'Orphan References';

  @override
  String get scannedRecords => 'Scanned Records';

  @override
  String get orphanDetails => 'Orphan Reference Details';

  @override
  String get noOrphanRecords =>
      'All foreign references are healthy with zero orphan records.';

  @override
  String get dqSeverityDistribution => 'DQ Violations by Severity';

  @override
  String get dqViolationTrend => 'DQ Violation Trend (Last 7 Days)';

  @override
  String get inboxTitle => 'Inbox';

  @override
  String get inboxSubtitle => 'Internal Messages & Email Management';

  @override
  String get inboxFolderInbox => 'Inbox';

  @override
  String get folderInbox => 'Inbox';

  @override
  String get inboxFolderSent => 'Sent';

  @override
  String get folderSent => 'Sent';

  @override
  String get inboxFolderDraft => 'Drafts';

  @override
  String get folderDraft => 'Drafts';

  @override
  String get inboxFolderArchive => 'Archive';

  @override
  String get folderArchive => 'Archive';

  @override
  String get inboxFolderTrash => 'Trash';

  @override
  String get folderTrash => 'Trash';

  @override
  String get inboxFolderStarred => 'Starred';

  @override
  String get folderStarred => 'Starred';

  @override
  String get inboxCompose => 'New Message';

  @override
  String get compose => 'New Message';

  @override
  String get inboxComposeTitle => 'Compose Message';

  @override
  String get composeTitle => 'Compose Message';

  @override
  String get inboxReply => 'Reply';

  @override
  String get reply => 'Reply';

  @override
  String get inboxReplyAll => 'Reply All';

  @override
  String get replyAll => 'Reply All';

  @override
  String get inboxForward => 'Forward';

  @override
  String get forward => 'Forward';

  @override
  String get inboxSend => 'Send';

  @override
  String get send => 'Send';

  @override
  String get inboxSendFailed => 'Failed to send message.';

  @override
  String get sendFailed => 'Failed to send message.';

  @override
  String get inboxSaveDraft => 'Save Draft';

  @override
  String get saveDraft => 'Save Draft';

  @override
  String get inboxDraftSaved => 'Draft saved.';

  @override
  String get draftSaved => 'Draft saved.';

  @override
  String get inboxDraftFailed => 'Failed to save draft.';

  @override
  String get draftFailed => 'Failed to save draft.';

  @override
  String get inboxMessageSent => 'Message sent.';

  @override
  String get messageSent => 'Message sent.';

  @override
  String get inboxMessageDeleted => 'Message deleted.';

  @override
  String get messageDeleted => 'Message deleted.';

  @override
  String get inboxMessageMoved => 'Message moved.';

  @override
  String get messageMoved => 'Message moved.';

  @override
  String get inboxMessageStarred => 'Message starred.';

  @override
  String get messageStarred => 'Message starred.';

  @override
  String get inboxMessageUnstarred => 'Message unstarred.';

  @override
  String get messageUnstarred => 'Message unstarred.';

  @override
  String get inboxMarkRead => 'Mark as Read';

  @override
  String get markRead => 'Mark as Read';

  @override
  String get inboxMarkUnread => 'Mark as Unread';

  @override
  String get markUnread => 'Mark as Unread';

  @override
  String get inboxMoveToArchive => 'Move to Archive';

  @override
  String get moveToArchive => 'Move to Archive';

  @override
  String get inboxMoveToTrash => 'Move to Trash';

  @override
  String get moveToTrash => 'Move to Trash';

  @override
  String get inboxDelete => 'Delete';

  @override
  String get inboxPermanentDelete => 'Delete Permanently';

  @override
  String get permanentDelete => 'Delete Permanently';

  @override
  String get inboxPermanentDeleteConfirm =>
      'Are you sure you want to permanently delete this message? This action cannot be undone.';

  @override
  String get permanentDeleteConfirm =>
      'Are you sure you want to permanently delete this message? This action cannot be undone.';

  @override
  String get inboxRestore => 'Restore';

  @override
  String get restore => 'Restore';

  @override
  String get inboxEmptyTrash => 'Empty Trash';

  @override
  String get emptyTrash => 'Empty Trash';

  @override
  String get inboxRecipientTo => 'To';

  @override
  String get recipientTo => 'To';

  @override
  String get inboxRecipientCc => 'CC';

  @override
  String get recipientCc => 'CC';

  @override
  String get inboxRecipientBcc => 'BCC';

  @override
  String get recipientBcc => 'BCC';

  @override
  String get inboxAddRecipient => 'Add Recipient';

  @override
  String get addRecipient => 'Add Recipient';

  @override
  String get inboxAddExternalEmail => 'Add External Email';

  @override
  String get addExternalEmail => 'Add External Email';

  @override
  String get inboxSearchUsers => 'Search users...';

  @override
  String get searchUsers => 'Search users...';

  @override
  String get inboxSearchUsersBtn => 'Search Users';

  @override
  String get searchUsersBtn => 'Search Users';

  @override
  String get inboxAddressBook => 'User Search / Address Book';

  @override
  String get addressBook => 'User Search / Address Book';

  @override
  String get inboxNoRecipients => 'Please select a recipient.';

  @override
  String get noRecipients => 'Please select a recipient.';

  @override
  String get inboxRecipientRequired =>
      'Please enter or select at least one recipient.';

  @override
  String get recipientRequired =>
      'Please enter or select at least one recipient.';

  @override
  String get inboxSubjectRequired => 'Please enter a subject.';

  @override
  String get subjectRequired => 'Please enter a subject.';

  @override
  String get inboxNoSubject => '(No subject)';

  @override
  String get noSubject => '(No subject)';

  @override
  String get inboxSender => 'Sender';

  @override
  String get sender => 'Sender';

  @override
  String get inboxDate => 'Date';

  @override
  String get inboxSubject => 'Subject';

  @override
  String get subject => 'Subject';

  @override
  String get inboxSubjectPlaceholder => 'Enter subject';

  @override
  String get subjectPlaceholder => 'Enter subject';

  @override
  String get inboxBodyPlaceholder => 'Write your message here...';

  @override
  String get bodyPlaceholder => 'Write your message here...';

  @override
  String get inboxImportance => 'Importance';

  @override
  String get importance => 'Importance';

  @override
  String get inboxImportanceNormal => 'Normal';

  @override
  String get importanceNormal => 'Normal';

  @override
  String get inboxImportanceHigh => 'High';

  @override
  String get importanceHigh => 'High';

  @override
  String get inboxImportanceUrgent => 'Urgent';

  @override
  String get importanceUrgent => 'Urgent';

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
  String get inboxAttachments => 'Attachments';

  @override
  String get attachments => 'Attachments';

  @override
  String get inboxAddAttachment => 'Attach File';

  @override
  String get addAttachment => 'Attach File';

  @override
  String get inboxDownloadAttachment => 'Download Attachment';

  @override
  String get downloadAttachment => 'Download Attachment';

  @override
  String get inboxNoMessages => 'No messages.';

  @override
  String get noMessages => 'No messages.';

  @override
  String get inboxNoMessageSelected => 'Please select a message.';

  @override
  String get noMessageSelected => 'Please select a message.';

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
  String get searchPlaceholder => 'Search by ID or Name attribute...';

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
  String get inboxCancel => 'Cancel';

  @override
  String get inboxClose => 'Close';

  @override
  String get inboxRecallMessage => 'Recall Message';

  @override
  String get recallMessage => 'Recall Message';

  @override
  String get inboxRecallConfirm =>
      'Are you sure you want to recall this message for unread recipients? (Messages already read cannot be recalled)';

  @override
  String get recallConfirm =>
      'Are you sure you want to recall this message for unread recipients? (Messages already read cannot be recalled)';

  @override
  String get inboxRecallSuccess => 'Message recall processed.';

  @override
  String get recallSuccess => 'Message recall processed.';

  @override
  String inboxRecallResultTotal(Object count) {
    return 'Total Recipients: $count';
  }

  @override
  String recallResultTotal(Object count) {
    return 'Total Recipients: $count';
  }

  @override
  String inboxRecallResultBeforeRead(Object count) {
    return 'Recalled Before Read: $count';
  }

  @override
  String recallResultBeforeRead(Object count) {
    return 'Recalled Before Read: $count';
  }

  @override
  String inboxRecallResultAfterRead(Object count) {
    return 'Already Read (Cannot recall): $count';
  }

  @override
  String recallResultAfterRead(Object count) {
    return 'Already Read (Cannot recall): $count';
  }

  @override
  String inboxRecallResultExternal(Object count) {
    return 'External Email (Cannot recall): $count';
  }

  @override
  String recallResultExternal(Object count) {
    return 'External Email (Cannot recall): $count';
  }

  @override
  String get inboxRead => 'Read';

  @override
  String get read => 'Read';

  @override
  String get inboxUnread => 'Unread';

  @override
  String get unread => 'Unread';

  @override
  String get inboxRecalled => 'Recalled';

  @override
  String get recalled => 'Recalled';

  @override
  String get inboxRecipientRecallStatusBefore =>
      'Recalled (Deleted before read)';

  @override
  String get recipientRecallStatusBefore => 'Recalled (Deleted before read)';

  @override
  String inboxRecipientRecallStatusAfter(Object time) {
    return 'Cannot recall (Read: $time)';
  }

  @override
  String recipientRecallStatusAfter(Object time) {
    return 'Cannot recall (Read: $time)';
  }

  @override
  String get inboxRecipientRecallStatusExternal =>
      'External Email (Cannot recall)';

  @override
  String get recipientRecallStatusExternal => 'External Email (Cannot recall)';

  @override
  String get inboxDragDropHint => 'Drag and drop files here or click to browse';

  @override
  String get dragDropHint => 'Drag and drop files here or click to browse';

  @override
  String get inboxDropOrClickFiles => 'Drop files here or click to attach';

  @override
  String get dropOrClickFiles => 'Drop files here or click to attach';

  @override
  String get inboxFileSizeLimit => 'Max 50MB per file (Uploaded on send)';

  @override
  String get fileSizeLimit => 'Max 50MB per file (Uploaded on send)';

  @override
  String get inboxUploadReady => 'Ready to upload';

  @override
  String get uploadReady => 'Ready to upload';

  @override
  String get inboxUploadInProgress => 'Uploading...';

  @override
  String get uploadInProgress => 'Uploading...';

  @override
  String get inboxUploadSuccess => 'Upload Complete';

  @override
  String get uploadSuccess => 'Upload Complete';

  @override
  String get inboxUploadFailed => 'Upload Failed';

  @override
  String get uploadFailed => 'Upload Failed';

  @override
  String get inboxUploadRetry => 'Retry';

  @override
  String get uploadRetry => 'Retry';

  @override
  String inboxTotalFilesSummary(Object count, Object size) {
    return 'Total $count files ($size)';
  }

  @override
  String totalFilesSummary(Object count, Object size) {
    return 'Total $count files ($size)';
  }

  @override
  String get inboxAttachFilesBtn => 'Attach Files';

  @override
  String get attachFilesBtn => 'Attach Files';

  @override
  String get inboxClearAllAttachments => 'Clear All';

  @override
  String get clearAllAttachments => 'Clear All';

  @override
  String get inboxNoAttachments => 'No attachments.';

  @override
  String get noAttachments => 'No attachments.';

  @override
  String get inboxLoading => 'Loading...';

  @override
  String get loading => 'Loading...';

  @override
  String get inboxEmptyFolder => 'This folder is empty.';

  @override
  String get emptyFolder => 'This folder is empty.';

  @override
  String inboxUnreadBadge(Object count) {
    return '$count unread';
  }

  @override
  String unreadBadge(Object count) {
    return '$count unread';
  }

  @override
  String get inboxAllRead => 'Mark All as Read';

  @override
  String get allRead => 'Mark All as Read';

  @override
  String get inboxDetails => 'Details';

  @override
  String get inboxRecipientsList => 'Recipients List';

  @override
  String get recipientsList => 'Recipients List';

  @override
  String get inboxViewAllRecipients => 'View All Recipients';

  @override
  String get viewAllRecipients => 'View All Recipients';

  @override
  String get inboxTo => 'To';

  @override
  String get to => 'To';

  @override
  String get inboxCc => 'CC';

  @override
  String get cc => 'CC';

  @override
  String get inboxBcc => 'BCC';

  @override
  String get bcc => 'BCC';

  @override
  String get inboxFrom => 'From';

  @override
  String get from => 'From';

  @override
  String get inboxAt => 'At';

  @override
  String get at => 'At';

  @override
  String get inboxAttachmentDownload => 'Download';

  @override
  String get attachmentDownload => 'Download';

  @override
  String get inboxAttachmentDownloadAll => 'Download All';

  @override
  String get attachmentDownloadAll => 'Download All';

  @override
  String get inboxViewModeSplit => 'Split View';

  @override
  String get viewModeSplit => 'Split View';

  @override
  String get inboxViewModeList => 'List View (Popup Detail)';

  @override
  String get viewModeList => 'List View (Popup Detail)';

  @override
  String get inboxViewMode => 'View Mode';

  @override
  String get viewMode => 'View Mode';

  @override
  String get inboxMessageDetailModal => 'Message Details';

  @override
  String get messageDetailModal => 'Message Details';

  @override
  String get inboxDragToResize => 'Drag to resize (Double-click to reset)';

  @override
  String get dragToResize => 'Drag to resize (Double-click to reset)';

  @override
  String get inboxNewMessageReceived => 'New message/mail received.';

  @override
  String get newMessageReceived => 'New message/mail received.';

  @override
  String get inboxOriginalMessage => 'Original Message';

  @override
  String get originalMessage => 'Original Message';

  @override
  String get inboxComposeMemoApproval => 'Draft Memo Approval';

  @override
  String get composeMemoApproval => 'Draft Memo Approval';

  @override
  String get inboxMemoApprovalTitle => 'Draft Memo Approval';

  @override
  String get memoApprovalTitle => 'Draft Memo Approval';

  @override
  String get inboxMemoApprovalDesc =>
      'Write a proposal using the web editor, configure the approval route, and submit for approval.';

  @override
  String get memoApprovalDesc =>
      'Write a proposal using the web editor, configure the approval route, and submit for approval.';

  @override
  String get inboxApprovalLine => 'Approval Route';

  @override
  String get inboxApprovalRouteSetting => 'Approval Route Settings';

  @override
  String get approvalRouteSetting => 'Approval Route Settings';

  @override
  String get inboxDrafter => 'Drafter';

  @override
  String get drafter => 'Drafter';

  @override
  String get inboxApprovalType => 'Type';

  @override
  String get approvalType => 'Type';

  @override
  String get inboxTypeApproval => 'Approval';

  @override
  String get inboxTypeConsensus => 'Agreement';

  @override
  String get typeConsensus => 'Agreement';

  @override
  String get inboxTypeNotification => 'Notification';

  @override
  String get typeNotification => 'Notification';

  @override
  String get inboxParallelApproval => 'Parallel Approval';

  @override
  String get parallelApproval => 'Parallel Approval';

  @override
  String get inboxParallelConsensus => 'Parallel Agreement';

  @override
  String get parallelConsensus => 'Parallel Agreement';

  @override
  String get inboxStepOrder => 'Step Order';

  @override
  String get stepOrder => 'Step Order';

  @override
  String inboxStepOrderLabel(Object order) {
    return 'Step $order';
  }

  @override
  String stepOrderLabel(Object order) {
    return 'Step $order';
  }

  @override
  String get inboxAddStep => 'Add Step';

  @override
  String get addStep => 'Add Step';

  @override
  String get inboxAddParallelStep => 'Add Parallel';

  @override
  String get addParallelStep => 'Add Parallel';

  @override
  String get inboxMoveUp => 'Move Up';

  @override
  String get moveUp => 'Move Up';

  @override
  String get inboxMoveDown => 'Move Down';

  @override
  String get moveDown => 'Move Down';

  @override
  String get inboxDeleteStep => 'Delete';

  @override
  String get deleteStep => 'Delete';

  @override
  String get inboxObservers => 'Observers (CC)';

  @override
  String get inboxAddObserver => 'Add Observer';

  @override
  String get addObserver => 'Add Observer';

  @override
  String get inboxObserversDesc =>
      'Observers are notified when the approval process is completed.';

  @override
  String get observersDesc =>
      'Observers are notified when the approval process is completed.';

  @override
  String get inboxSubmitApproval => 'Submit Approval';

  @override
  String get submitApproval => 'Submit Approval';

  @override
  String get inboxSubmitApprovalSuccess =>
      'Memo approval request submitted successfully.';

  @override
  String get submitApprovalSuccess =>
      'Memo approval request submitted successfully.';

  @override
  String get inboxSubmitApprovalFailed =>
      'Failed to submit memo approval request.';

  @override
  String get submitApprovalFailed => 'Failed to submit memo approval request.';

  @override
  String get inboxApprovalStatus => 'Approval Status';

  @override
  String get approvalStatus => 'Approval Status';

  @override
  String get inboxApprovalLineEmpty =>
      'Please configure at least one approver in the route.';

  @override
  String get approvalLineEmpty =>
      'Please configure at least one approver in the route.';

  @override
  String get inboxApprove => 'Approve';

  @override
  String get inboxReject => 'Reject';

  @override
  String get inboxConsensusAgree => 'Agree';

  @override
  String get consensusAgree => 'Agree';

  @override
  String get inboxApprovalComment => 'Approval Comment';

  @override
  String get approvalComment => 'Approval Comment';

  @override
  String get inboxRejectReason => 'Rejection Reason';

  @override
  String get rejectReason => 'Rejection Reason';

  @override
  String get inboxRejectReasonRequired => 'Please provide a rejection reason.';

  @override
  String get rejectReasonRequired => 'Please provide a rejection reason.';

  @override
  String get inboxApproveSuccess => 'Approval request approved.';

  @override
  String get approveSuccess => 'Approval request approved.';

  @override
  String get inboxRejectSuccess => 'Approval request rejected.';

  @override
  String get rejectSuccess => 'Successfully rejected candidate.';

  @override
  String get inboxApprovalActionFailed => 'Failed to process approval action.';

  @override
  String get approvalActionFailed => 'Failed to process approval action.';

  @override
  String get inboxMemoContent => 'Draft Content';

  @override
  String get memoContent => 'Draft Content';

  @override
  String get inboxCancelApproval => 'Cancel Submission';

  @override
  String get cancelApproval => 'Cancel Submission';

  @override
  String get inboxCancelApprovalConfirm =>
      'Are you sure you want to cancel this approval submission? All pending steps will be cancelled.';

  @override
  String get cancelApprovalConfirm =>
      'Are you sure you want to cancel this approval submission? All pending steps will be cancelled.';

  @override
  String get inboxCancelApprovalReason => 'Cancellation Reason';

  @override
  String get cancelApprovalReason => 'Cancellation Reason';

  @override
  String get inboxCancelApprovalReasonPlaceholder =>
      'Enter cancellation reason (optional)';

  @override
  String get cancelApprovalReasonPlaceholder =>
      'Enter cancellation reason (optional)';

  @override
  String get inboxCancelApprovalSuccess =>
      'Approval request has been cancelled.';

  @override
  String get cancelApprovalSuccess => 'Approval request has been cancelled.';

  @override
  String get inboxCancelApprovalFailed => 'Failed to cancel approval request.';

  @override
  String get cancelApprovalFailed => 'Failed to cancel approval request.';

  @override
  String get inboxStatusCancelled => 'Cancelled';

  @override
  String get selectNodeToViewRecords =>
      'Select a classification node from the tree to view master records.';

  @override
  String get diffDetails => 'Change History Details';

  @override
  String get advancedSearch => 'Advanced Search';

  @override
  String get advancedSearchCondition => 'Advanced Search';

  @override
  String get noFieldsToExport => 'No field definitions found to export.';

  @override
  String get downloadTemplateSuccess => 'Template downloaded successfully.';

  @override
  String get downloadTemplateFailed => 'Failed to download template.';

  @override
  String get deleteRow => 'Delete Row';

  @override
  String get emptyTableData =>
      'No rows defined. Click \'+ Add Row\' to insert a record row.';

  @override
  String get clearAllRows => 'Clear All Rows';

  @override
  String totalRowsCount(Object count) {
    return 'Total $count rows';
  }

  @override
  String get masterDataRecordList => 'Master Data Record List';

  @override
  String get resetAll => 'Reset All';

  @override
  String get createRequest => 'Request Creation';

  @override
  String get createRecord => 'Create Record';

  @override
  String get bulkUpload => 'Bulk Upload';

  @override
  String appliedFiltersCount(Object count) {
    return 'Applied Filters: $count';
  }

  @override
  String get targetTypeRecord => 'Master Record';

  @override
  String get rollbackRecord => 'Rollback to Past Version';

  @override
  String get rollbackBtn => 'Rollback to this version';

  @override
  String get rollbackConfirmTitle => 'Confirm Record Rollback';

  @override
  String rollbackConfirmDesc(Object version) {
    return 'Create an approval request to rollback to Version $version data. Please provide a reason.';
  }

  @override
  String get rollbackReason => 'Rollback Reason';

  @override
  String get rollbackReasonPlaceholder =>
      'Enter reason for rolling back (e.g. erroneous edit reversion)';

  @override
  String rollbackSuccess(Object version) {
    return 'Rollback request for Version $version submitted successfully.';
  }

  @override
  String get rollbackDiffPreview => 'Data Diff Preview for Rollback';

  @override
  String get currentData => 'Current Data';

  @override
  String targetVersionData(Object version) {
    return 'Target Version Data (Version $version)';
  }

  @override
  String get asyncExport => 'Async Export';

  @override
  String get dataLineageTitle => 'Master Data Lifecycle & Lineage';

  @override
  String warningDeleteRecords(Object count) {
    return 'Deleting this field permanently purges data in $count active record(s).';
  }

  @override
  String get warningDeleteRecordsZero =>
      'There are 0 active records containing values for this field.';

  @override
  String get warningModifyRecords =>
      'Please verify data type compatibility for existing records.';

  @override
  String get warningModifyRecordsZero =>
      '0 active records affected; no risk of data loss.';

  @override
  String get targetTypeRECORD => 'Master Record Creation';

  @override
  String get targetTypeRECORDUPDATE => 'Master Record Update';

  @override
  String get targetTypeRECORDDELETE => 'Master Record Deletion';

  @override
  String get targetTypeRECORDMERGE => 'Master Record Merge';

  @override
  String get recordIdAttr => 'ID Attribute';

  @override
  String get recordNameAttr => 'Name Attribute';

  @override
  String get affectedRecordsBreakdown => 'Affected Record Samples (Breakdown)';

  @override
  String get asyncExportTitle => 'Bulk Master Data Async Export';

  @override
  String get asyncExportDesc =>
      'Exports all master data of the selected domain to an Excel file via background async processing.';

  @override
  String get excelViewerTitle => 'Messenger Excel Viewer';

  @override
  String get excelViewerBtn => 'Viewer';

  @override
  String get excelViewerOpen => 'Open Excel Dedicated Viewer';

  @override
  String get searchInExcel => 'Search in Sheet...';

  @override
  String get excelLoading => 'Loading and parsing excel worksheet data...';

  @override
  String get noExcelData => 'No excel cell data to display.';

  @override
  String get excelCopyTip =>
      'Click a cell to inspect address or use copy buttons to copy table data format directly.';

  @override
  String get copyAsExcelText => '📋 Copy as Excel Text (Table)';

  @override
  String get tableRecordCount => 'Total Records';

  @override
  String get copyTableExcel => '📋 Copy Entire Table Data (Excel)';

  @override
  String get excelSpreadsheetViewerTitle => 'MS Excel Spreadsheet Data Viewer';

  @override
  String get copyRawTableExcel => '🔑 Copy 100% Raw Table Data';

  @override
  String get excelModalTip =>
      'Drag or Shift/Ctrl click to select multiple cells. Press Ctrl+C to copy directly into Excel.';

  @override
  String excelCellsSelected(Object count) {
    return '$count cells selected';
  }

  @override
  String excelCellCopied(Object address, Object value) {
    return '📋 [$address] Cell data \"$value\" copied!';
  }

  @override
  String excelRangeCopied(Object count) {
    return '📋 $count cells data copied! (Ready for Ctrl+V in Excel)';
  }

  @override
  String excelTableCopied(Object rows) {
    return '📋 All $rows rows table data copied! (Ready for Ctrl+V in Excel)';
  }

  @override
  String get startExport => 'Start Export';

  @override
  String get exportProgress => 'Export Progress Status';

  @override
  String get affectedRecords => 'Affected Records';

  @override
  String get activeRecords => 'Active Records';

  @override
  String get baselineRecord => 'Baseline Record:';

  @override
  String get btnCheckDuplicate => 'Check Availability';

  @override
  String get compareRecords => 'Compare Records';

  @override
  String get compareRecordsTitle => 'Compare Selected Records';

  @override
  String get unmergePreviewTitle => 'Golden Record Unmerge Preview';

  @override
  String get unmergeWarningDesc =>
      'Unmerging the golden record will restore individual source records back to their original states prior to merging.';

  @override
  String get currentGoldenRecord => 'Current Golden Record';

  @override
  String restoringRecordsCount(Object count) {
    return 'Source Records to be Restored ($count)';
  }

  @override
  String get unmergeConfirmBtn => 'Confirm Unmerge';

  @override
  String get sourceRecord => 'Source Record';

  @override
  String get unnamedRecord => 'Unnamed Record';

  @override
  String get duplicateRequestWarning =>
      'Some domains are already pending approval and were skipped.';

  @override
  String get errorFieldDomainMismatch =>
      'Field does not belong to the specified domain.';

  @override
  String get errorFieldNodeMismatch =>
      'Field does not belong to the specified node.';

  @override
  String get errorNodeDomainMismatch =>
      'Node does not belong to the specified domain.';

  @override
  String get errorSectorDomainMismatch =>
      'Sector does not belong to the domain.';

  @override
  String get exportExcel => 'Export to Excel';

  @override
  String get goToMatchReview => 'Go to Match Review';

  @override
  String get recordVersionNode => 'Version History';

  @override
  String get masterRecordNode => 'Master Record';

  @override
  String get recordsCountSuffix => 'records';

  @override
  String get secondaryNodesTab => 'Secondary Nodes';

  @override
  String get masterData => 'Master Data';

  @override
  String get lineageNodeMasterDesc =>
      'Master Record: Latest Golden Record under unified management';

  @override
  String get installRequirePwdMatch => 'Passwords do not match.';

  @override
  String get managedMasterRecords => 'Managed Master Records';

  @override
  String get matchReviewTitle => 'Match Review';

  @override
  String get matchReviewDesc =>
      'Review potential match candidates with high similarity to approve or reject master data merging.';

  @override
  String get matchReviewDomainSelect => 'Select Domain';

  @override
  String get domainSelect => 'Select Domain';

  @override
  String get matchReviewRefresh => 'Refresh';

  @override
  String get matchReviewBatchConfirm => 'Batch Confirm';

  @override
  String get batchConfirm => 'Batch Confirm';

  @override
  String get matchReviewBatchReject => 'Batch Reject';

  @override
  String get batchReject => 'Batch Reject';

  @override
  String get matchReviewExistingRecord => 'Existing Master Record';

  @override
  String get existingRecord => 'Existing Master Record';

  @override
  String get matchReviewIncomingData => 'Incoming New Record';

  @override
  String get incomingData => 'Incoming New Record';

  @override
  String get matchReviewRejectNew => 'Reject';

  @override
  String get rejectNew => 'Reject';

  @override
  String get matchReviewConfirmMerge => 'Confirm Merge';

  @override
  String get matchReviewStatusPending => 'Pending Review';

  @override
  String get matchReviewStatusConfirmed => 'Confirmed Merge';

  @override
  String get statusConfirmed => 'Confirmed Merge';

  @override
  String get matchReviewStatusRejected => 'Rejected';

  @override
  String get matchReviewSimilarityScore => 'Similarity Score';

  @override
  String get similarityScore => 'Similarity Score';

  @override
  String get matchReviewStatusFilter => 'Status';

  @override
  String get matchReviewRejectSuccess => 'Successfully rejected candidate.';

  @override
  String get matchReviewRejectFail => 'Failed to reject candidate.';

  @override
  String get rejectFail => 'Failed to reject candidate.';

  @override
  String get matchReviewConfirmSuccess =>
      'Successfully confirmed candidate merge.';

  @override
  String get confirmSuccess => 'Successfully confirmed candidate merge.';

  @override
  String get matchReviewConfirmFail => 'Failed to confirm candidate merge.';

  @override
  String get confirmFail => 'Failed to confirm candidate merge.';

  @override
  String get matchingRulesTitle => 'Matching Rules Management';

  @override
  String get matchingRulesDesc =>
      'Configure EXACT / FUZZY matching rules and similarity thresholds for duplicate record identification.';

  @override
  String get msgPasswordMismatch => 'Passwords do not match.';

  @override
  String get noDomainRecords => 'No master records found.';

  @override
  String get pendingMatchCandidates => 'Pending Match Candidates';

  @override
  String get permMasterManagement => 'Permission Master Management';

  @override
  String get permMasterTitle => 'Permission Master Group List';

  @override
  String get potentialDuplicates => 'Potential Duplicates';

  @override
  String get recordCountUnit => 'records';

  @override
  String get recordCreationTrends => 'Record Creation Trends';

  @override
  String get recordSaveFailed => 'No input data.';

  @override
  String get records => 'Data Records';

  @override
  String get recordsManagement => 'Master Data Records Management';

  @override
  String get recordsManagementDesc =>
      'Query, create, bulk modify, and execute survivorship merge for master data records by domain.';

  @override
  String get roleRecordManager => 'Record Manager';

  @override
  String get selectRecordDoubleclick =>
      'Double click a record from the list to select';

  @override
  String get exportRoles => 'Export Role Template';

  @override
  String get importRoles => 'Import Role Template';

  @override
  String get exportRolesSuccess => 'Role template exported successfully.';

  @override
  String get exportRolesFail => 'Failed to export role template.';

  @override
  String get importRolesSuccess => 'Role template imported successfully.';

  @override
  String get importRolesFail => 'Failed to import role template.';

  @override
  String get importRolesConfirm =>
      'Do you want to overwrite existing roles and permissions with the uploaded backup file?';

  @override
  String get removeFilter => 'Remove Filter';

  @override
  String get excelUploaderTitle => 'Excel Data Batch Upload';

  @override
  String get excelUploaderStep1 => '1. Upload File';

  @override
  String get step1 => '1. Upload File';

  @override
  String get excelUploaderStep2 => '2. Column Mapping';

  @override
  String get step2 => '2. Column Mapping';

  @override
  String get excelUploaderStep3 => '3. Pre-validation Report';

  @override
  String get step3 => '3. Pre-validation Report';

  @override
  String get excelUploaderStep4 => '4. Data Processing';

  @override
  String get step4 => '4. Data Processing';

  @override
  String get excelUploaderDownloadTemplate => 'Download Excel Template';

  @override
  String get excelUploaderDragDropFile =>
      'Drag and drop your Excel file here or click to browse';

  @override
  String get dragDropFile =>
      'Drag and drop your Excel file here or click to browse';

  @override
  String get excelUploaderSupportedFormats => 'Supported formats: .xlsx, .xls';

  @override
  String get supportedFormats => 'Supported formats: .xlsx, .xls';

  @override
  String get excelUploaderSelectedFile => 'Selected File';

  @override
  String get selectedFile => 'Selected File';

  @override
  String get excelUploaderReselectFile => 'Select Another File';

  @override
  String get reselectFile => 'Select Another File';

  @override
  String get excelUploaderParsingExcel => 'Analyzing Excel file...';

  @override
  String get parsingExcel => 'Analyzing Excel file...';

  @override
  String get excelUploaderSourceColumn => 'Excel Column Name';

  @override
  String get sourceColumn => 'Excel Column Name';

  @override
  String get excelUploaderSampleData => 'Sample Data';

  @override
  String get sampleData => 'Sample Data';

  @override
  String get excelUploaderTargetField => 'Target System Field';

  @override
  String get excelUploaderAutoMapped => 'Auto Mapped';

  @override
  String get autoMapped => 'Auto Mapped';

  @override
  String get excelUploaderManualMapping => 'Manual Mapping Required';

  @override
  String get manualMapping => 'Manual Mapping Required';

  @override
  String get excelUploaderIgnoreColumn => '-- Ignore Column --';

  @override
  String get ignoreColumn => '-- Ignore Column --';

  @override
  String get excelUploaderRowValidating => 'Running row-level DQ validation...';

  @override
  String get rowValidating => 'Running row-level DQ validation...';

  @override
  String get excelUploaderAllRowsValid => 'All rows passed DQ validation!';

  @override
  String get allRowsValid => 'All rows passed DQ validation!';

  @override
  String excelUploaderViolationsFound(Object count) {
    return 'DQ violations found in $count row(s).';
  }

  @override
  String violationsFound(Object count) {
    return 'DQ violations found in $count row(s).';
  }

  @override
  String excelUploaderValidationSummary(
    Object total,
    Object valid,
    Object invalid,
  ) {
    return 'Total $total rows: $valid valid · $invalid invalid';
  }

  @override
  String validationSummary(Object total, Object valid, Object invalid) {
    return 'Total $total rows: $valid valid · $invalid invalid';
  }

  @override
  String get excelUploaderShowOnlyErrors => 'Show Only Violated Rows';

  @override
  String get showOnlyErrors => 'Show Only Violated Rows';

  @override
  String get excelUploaderColRow => 'Row';

  @override
  String get colRow => 'Row';

  @override
  String get excelUploaderColResult => 'Result';

  @override
  String get colResult => 'Result';

  @override
  String get excelUploaderColViolatedField => 'Violated Field';

  @override
  String get colViolatedField => 'Violated Field';

  @override
  String get excelUploaderColSeverity => 'Severity';

  @override
  String get colSeverity => 'Severity';

  @override
  String get excelUploaderColViolationReason => 'Violation Reason';

  @override
  String get colViolationReason => 'Violation Reason';

  @override
  String get excelUploaderColInputValue => 'Input Value';

  @override
  String get colInputValue => 'Input Value';

  @override
  String excelUploaderProcessing(Object percent) {
    return 'Processing Data... $percent%';
  }

  @override
  String get excelUploaderBtnCancel => 'Cancel';

  @override
  String get excelUploaderBtnValidateUpload => 'Validate & Upload';

  @override
  String get btnValidateUpload => 'Validate & Upload';

  @override
  String get excelUploaderBtnEditMapping => '← Edit Mapping';

  @override
  String get btnEditMapping => '← Edit Mapping';

  @override
  String get excelUploaderBtnStartUpload => 'Start Upload';

  @override
  String get btnStartUpload => 'Start Upload';

  @override
  String excelUploaderBtnUploadValidOnly(Object count) {
    return 'Upload $count Valid Rows Only';
  }

  @override
  String btnUploadValidOnly(Object count) {
    return 'Upload $count Valid Rows Only';
  }

  @override
  String get excelUploaderBtnDone => 'Done';

  @override
  String get btnDone => 'Done';

  @override
  String get excelUploaderTooltipValidOnly =>
      'Only valid rows will be uploaded, excluding rows with violations';

  @override
  String get tooltipValidOnly =>
      'Only valid rows will be uploaded, excluding rows with violations';

  @override
  String reclassifySuccess(Object count) {
    return 'Successfully reclassified $count records.';
  }

  @override
  String reclassifyPartialFailed(Object success, Object failure) {
    return '$success succeeded, $failure failed.';
  }

  @override
  String get selectTargetNodePlaceholder => 'Select target classification node';

  @override
  String get bulkImport => 'Bulk Record Import';

  @override
  String get bulkImportDesc =>
      'Upload CSV or JSON files to bulk create master data records.';

  @override
  String bulkImportSuccess(Object success, Object errors) {
    return 'Bulk import completed. (Success: $success, Errors: $errors)';
  }

  @override
  String get selectFile => 'Select File (.csv / .json)';

  @override
  String get startUpload => 'Start Bulk Import';

  @override
  String get errorDetails => 'Failed Rows & Error Details';

  @override
  String get rowNumber => 'Row No';

  @override
  String get errorReason => 'Error Reason';

  @override
  String get complianceReport => 'Compliance Audit Lifecycle Report';

  @override
  String get complianceReportDesc =>
      'Track comprehensive lifecycle audit logs from creation to updates, approvals, sensitive data views, and rollbacks.';

  @override
  String get auditTimeline => 'Lifecycle Audit Timeline';

  @override
  String get eventType => 'Event Type';

  @override
  String get actor => 'Actor';

  @override
  String get eventDetail => 'Details';

  @override
  String get timeMachine => 'Record Time-Machine & Version Diff';

  @override
  String get timeMachineDesc =>
      'Explore version history across the timeline and visually compare field differences between two version snapshots.';

  @override
  String get compareVersions => 'Compare Versions';

  @override
  String get baseVersion => 'Base Version (Before)';

  @override
  String get targetVersion => 'Target Version (After)';

  @override
  String get diffAdded => 'Added';

  @override
  String get diffModified => 'Modified';

  @override
  String get diffRemoved => 'Removed';

  @override
  String get diffUnchanged => 'Unchanged';

  @override
  String get dataMasking => 'Dynamic Data Masking & PII Protection';

  @override
  String get dataMaskingDesc =>
      'Automatically masks sensitive personal identifiable information (PII) such as phone, email, and resident numbers based on user permissions.';

  @override
  String get maskedPreview => 'Masked State';

  @override
  String get unmaskedPreview => 'Unmasked Original (Authorized)';

  @override
  String get maskedFieldCount => 'Masked Fields Count';

  @override
  String get goldenRecord => 'Golden Record Builder & Merge Simulator';

  @override
  String get goldenRecordDesc =>
      'Assembles optimal master data by selecting the most trustworthy and recent field values across duplicate candidate records.';

  @override
  String get candidateRecords => 'Candidate Records';

  @override
  String get assembledGoldenData => 'Assembled Golden Record';

  @override
  String get chosenSource => 'Chosen Source';

  @override
  String get buildPreview => 'Simulate Golden Record Build';

  @override
  String get hashChainLedger => 'Immutable Hash-Chain Audit Ledger';

  @override
  String get hashChainDesc =>
      'Provides tamper-proof blockchain audit tracking by linking master data changes with SHA-256 hash chains.';

  @override
  String get verifyIntegrity => 'Verify Ledger Integrity';

  @override
  String get chainStatusIntact => 'Chain Intact (No Tampering)';

  @override
  String get chainStatusCorrupted => 'Tampering Detected';

  @override
  String get blockIndex => 'Block Index';

  @override
  String get blockHash => 'Block Hash (SHA-256)';

  @override
  String get prevHash => 'Previous Block Hash';

  @override
  String get smartQuery => 'Natural Language Smart Query Assistant';

  @override
  String get smartQueryDesc =>
      'Automatically interprets natural language queries into schema filter expressions to retrieve matching records.';

  @override
  String get queryPlaceholder => 'e.g. Find VIP customers living in Seoul';

  @override
  String get parsedFilters => 'Parsed Filter Expressions';

  @override
  String get matchedResults => 'Matched Results';

  @override
  String get executeQuery => 'Run Smart Query';

  @override
  String get businessRules => 'Complex Business Rule DQ Expression Builder';

  @override
  String get businessRulesDesc =>
      'Define conditional IF-THEN business validation logic and evaluate real-time data violations.';

  @override
  String get conditionExpr => 'Condition Expression (IF)';

  @override
  String get validationExpr => 'Validation Expression (THEN)';

  @override
  String get evaluateRules => 'Evaluate All Rules';

  @override
  String get violationFound => 'Violations Found';

  @override
  String get allRulesPassed =>
      'All business validation rules passed successfully.';

  @override
  String get cdcStream => 'CDC Stream';

  @override
  String get cdcStreamDesc =>
      'Capture real-time master record change events and inspect before/after attribute diffs.';

  @override
  String get cdcOp => 'Operation';

  @override
  String get activeOffset => 'Active Offset';

  @override
  String get eventsPerSec => 'Throughput';

  @override
  String get beforePayload => 'Before';

  @override
  String get afterPayload => 'After';

  @override
  String get simulateChange => 'Simulate Change Event';

  @override
  String get aiStructurizer => 'AI Unstructured Data Structurizer';

  @override
  String get aiStructurizerDesc =>
      'Automatically extracts schema fields and values from unstructured text like contracts, receipts, or emails.';

  @override
  String get rawTextPlaceholder =>
      'Enter unstructured text such as contract summaries, emails, or notes.';

  @override
  String get extractFields => 'Run AI Extraction';

  @override
  String get extractedFieldsCount => 'Extracted Fields';

  @override
  String get overallConfidence => 'Overall Confidence';

  @override
  String get createRecordFromAi => 'Create Record from AI Fields';

  @override
  String get autonomousCleansing => 'Autonomous Anomaly Cleansing Recommender';

  @override
  String get autonomousCleansingDesc =>
      'Analyzes statistical medians and standard dictionaries to autonomously recommend optimal values for anomalies.';

  @override
  String get anomalyValue => 'Anomaly Value';

  @override
  String get recommendedValue => 'Recommended Value';

  @override
  String get cleansingStrategy => 'Strategy';

  @override
  String get applyCleansing => 'Apply Cleansing';

  @override
  String get cleansingSuccess =>
      'Autonomous anomaly cleansing applied successfully.';

  @override
  String get btnSave => 'Save';

  @override
  String get btnClose => 'Close';

  @override
  String get btnEdit => 'Edit';

  @override
  String get btnDelete => 'Delete';

  @override
  String get domainRefModalTitle => 'Select Reference Record';

  @override
  String get domainRefModalGuide =>
      'Please double click the desired record from the list to select it.';

  @override
  String get guide =>
      'Please double click the desired record from the list to select it.';

  @override
  String get domainRefModalSearchPlaceholder =>
      'Search by ID or Name attribute...';

  @override
  String get domainRefModalSearchBtn => 'Search';

  @override
  String get searchBtn => 'Search';

  @override
  String get domainRefModalResetBtn => 'Reset';

  @override
  String get resetBtn => 'Reset';

  @override
  String domainRefModalTotalCount(Object count) {
    return 'Total $count items';
  }

  @override
  String totalCount(Object count) {
    return 'Total $count items';
  }

  @override
  String get domainRefModalNoResults => 'No records found.';

  @override
  String get noResults => 'No records found.';

  @override
  String get classificationAxes => 'Classification Axes';

  @override
  String get domainName => 'Domain Name';

  @override
  String get classificationName => 'Classification Name';

  @override
  String get tableSchemaSettings =>
      'Table Column Definitions (JSON Sub-Schema)';

  @override
  String get tableSchemaGuide =>
      '💡 Define sub-columns to receive data in a complex table format (e.g. Major History with School, Major, Graduation Date).';

  @override
  String get addColumn => '+ Add Column';

  @override
  String get removeColumn => 'Remove Column';

  @override
  String get columnKey => 'Column Key';

  @override
  String get columnNameKo => 'Column Name (KO)';

  @override
  String get columnNameEn => 'Column Name (EN)';

  @override
  String get columnType => 'Column Type';

  @override
  String get columnOptions =>
      'Options (KEY:KO_Label:EN_Label or comma separated: e.g. BACHELOR:학사:Bachelor, MASTER:석사:Master)';

  @override
  String get columnOptionsPlaceholder =>
      'e.g. HIGH:고졸:High School, BACHELOR:학사:Bachelor, MASTER:석사:Master, DOCTOR:박사:Doctor';

  @override
  String get columnWidth => 'Width(px)';

  @override
  String get noTableColumnsDefined =>
      'No table columns defined. Click \'+ Add Column\' to define columns.';

  @override
  String confirmDeleteNode(Object name) {
    return 'Are you sure you want to delete node \'$name\'?';
  }

  @override
  String get nodeDeletedSuccess => 'Node deleted successfully.';

  @override
  String get nodeDeleteFailed => 'Failed to delete node.';

  @override
  String addNodeTo(Object name) {
    return 'Add Child Node to $name';
  }

  @override
  String get nodeOrder => 'Sort Order';

  @override
  String get idAttribute => 'ID Attribute';

  @override
  String get nameAttribute => 'Name Attribute';

  @override
  String get schemaImpactTitle => 'Schema Change Impact Analysis Report';

  @override
  String get schemaImpactSummary => 'Schema Impact Summary';

  @override
  String get fieldDeleteApprovalSubmitted =>
      'Field deletion request has been submitted for approval.';

  @override
  String get fieldDeleteFailed => 'An error occurred while deleting the field.';

  @override
  String get targetTypeSCHEMAFIELDDELETE => 'Schema Field Deletion';

  @override
  String get targetTypeSCHEMAFIELDADD => 'Schema Field Addition';

  @override
  String get targetTypeSCHEMAFIELDUPDATE => 'Schema Field Modification';

  @override
  String get targetTypeSCHEMANODECREATE => 'Classification Node Creation';

  @override
  String get targetTypeSCHEMANODEUPDATE => 'Classification Node Modification';

  @override
  String get targetTypeSCHEMANODEMOVE => 'Classification Node Move';

  @override
  String get affectedTargetField => 'Target Attribute Field';

  @override
  String get schemaImpactConfirmedTitle => 'Risk Change Applied';

  @override
  String get schemaImpactConfirmedMsg =>
      'Schema change impact has been confirmed, and the risk-accepted changes have been approved and applied.';

  @override
  String get totalNodes => 'Total Lineage Nodes';

  @override
  String get actionTypeSchemaChange => 'Schema Change (SCHEMA_CHANGE)';

  @override
  String get actionTypeSchemaChangeShort => 'Schema Change';

  @override
  String get addEditableFieldPlaceholder =>
      '+ Select Editable Fields (Default All)';

  @override
  String addFieldToNode(Object name) {
    return 'Add Field to $name';
  }

  @override
  String get fieldNameKo => 'Field Name (KO)';

  @override
  String get fieldNameEn => 'Field Name (EN)';

  @override
  String get fieldHintKo => 'Field Tooltip (KO)';

  @override
  String get fieldHintEn => 'Field Tooltip (EN)';

  @override
  String get groupSectorMapped => 'Group (Sector mapped automatically)';

  @override
  String get fieldType => 'Field Type';

  @override
  String get targetDomain => 'Target Domain';

  @override
  String get addHiddenFieldPlaceholder => '+ Select Fields to Hide';

  @override
  String get addNewField => '+ Add New Field';

  @override
  String get addSchema => 'Add Schema';

  @override
  String get allDomainsSelected => 'All available domains are selected.';

  @override
  String auditFieldChanged(Object field) {
    return '$field changed';
  }

  @override
  String get basicFields => 'Basic Fields';

  @override
  String get classification => 'Classification';

  @override
  String get classificationTree => 'Classification Tree';

  @override
  String get deleteFieldProp => '🗑️ Delete Field';

  @override
  String get deleteSchema => 'Delete Schema';

  @override
  String get deletedFieldProperties => 'Deleted Field Properties';

  @override
  String get domain => 'Domain';

  @override
  String get domainDistributionTitle => 'Master Record Distribution by Domain';

  @override
  String get domainPermGroupTitle => 'Domain Permissions';

  @override
  String get domainSchemaDesc =>
      'Define domain models, attributes, and data types based on classification tree structures.';

  @override
  String get domainSchemaTitle => 'Domain Schema Management';

  @override
  String get editSchema => 'Edit Schema';

  @override
  String get editableFieldsTitle => '🟢 Editable & DQ Scoped Fields';

  @override
  String get errorDomainMissingId =>
      'Domain configuration error: Missing Identifier (ID) or Display Name mapping.';

  @override
  String get errorSavingDomain => 'Error saving domain';

  @override
  String fieldKeyAlreadyExistsNewfieldValueKey(Object key) {
    return 'Field Key already exists: $key';
  }

  @override
  String get fieldKeyExists => 'Field Key already exists';

  @override
  String get fieldPermGroupTitle => 'Attribute Field Permissions';

  @override
  String get grantedDomains => 'Granted Domains';

  @override
  String get hiddenFieldsTitle => '🔴 Hidden Fields';

  @override
  String get nodeCountSuffix => '';

  @override
  String get outboundNode => 'Outbound Integration';

  @override
  String get lineageNodeSourceDesc =>
      'Source System: Origin system where data was created/ingested';

  @override
  String get lineageNodeHistoryDesc =>
      'Change History: Chronological version history from creation (v1) to updates (v2...)';

  @override
  String get lineageNodeOutboundDesc =>
      'Outbound Integration: Integration dispatch history to external systems';

  @override
  String get myGrantedDomains => 'My Granted Domains';

  @override
  String get newFieldProperties => 'New Field Properties';

  @override
  String get noDomainData => 'No Domain Data';

  @override
  String get noGrantedDomains => 'No granted domains.';

  @override
  String get noNewDomainsAvailable => 'No new domains available for access.';

  @override
  String get noRulesSchemaChange =>
      'No rules defined. By default, all users are eligible to submit schema change requests.';

  @override
  String get noSpecificDomainPermissions =>
      'No specific domain permissions. (ADMIN sees all)';

  @override
  String get node => 'Node';

  @override
  String get deptIcon => 'Department Icon';

  @override
  String get nodePermGroupTitle => 'Category Node Permissions';

  @override
  String get pendingDomainAccessRequests => 'Pending Domain Access Requests';

  @override
  String get pendingSchemaApprovalExists =>
      'A pending schema approval request already exists. Modifications are locked until completion.';

  @override
  String get pleaseSelectATargetDomain => 'Please select a target domain.';

  @override
  String get requestDomainAccess => 'Request Domain Access';

  @override
  String get requestNewDomain => 'Request New Domain';

  @override
  String get requestedDomains => 'Requested Domains';

  @override
  String get schema => 'Domain Schema';

  @override
  String get schemaApprovalInProgress => 'Approval Pending';

  @override
  String get schemaChangeComparison =>
      'Field Property Changes (Before ➔ After)';

  @override
  String get schemaHistoryTitle => 'Schema Change History';

  @override
  String get schemaHistoryTargetType => 'Target Type';

  @override
  String get schemaHistoryAction => 'Action';

  @override
  String get schemaHistoryChangedBy => 'Changed By';

  @override
  String get schemaHistoryChangedAt => 'Changed At';

  @override
  String get changedAt => 'Changed At';

  @override
  String get schemaHistoryBefore => 'Before';

  @override
  String get before => 'Before';

  @override
  String get schemaHistoryAfter => 'After';

  @override
  String get after => 'After';

  @override
  String get schemaHistoryField => 'Field';

  @override
  String get schemaHistoryNode => 'Node';

  @override
  String get schemaHistoryDomainEntity => 'Domain';

  @override
  String get domainEntity => 'Domain';

  @override
  String get schemaHistoryGroup => 'Group';

  @override
  String get schemaHistoryCreate => 'Create';

  @override
  String get schemaHistoryUpdate => 'Update';

  @override
  String get schemaHistoryDelete => 'Delete';

  @override
  String get schemaHistoryNoHistory => 'No change history';

  @override
  String get noHistory => 'No change history';

  @override
  String get schemaHistoryViewChanges => 'View Changes';

  @override
  String get schemaReason => 'Reason for Submission';

  @override
  String get schemaReasonPlaceholder =>
      'Please describe the reason for schema modification.';

  @override
  String get scopeDomain => 'Domain Common';

  @override
  String get scopeNode => 'Classification Node';

  @override
  String get selectADomain => 'Select a domain';

  @override
  String get selectDomainFirst => 'Please select a domain first.';

  @override
  String get selectNodePlaceholder => 'Select Classification Node';

  @override
  String get selectTargetDomain => 'Please select a target domain.';

  @override
  String get selectTargetDomainAlert => 'Please select a target domain.';

  @override
  String get tabFields => 'Fields';

  @override
  String get applyTargetDomain => 'Target Domain *';

  @override
  String get targetNode => 'Target Classification Node *';

  @override
  String get totalDomains => 'Total Domains';

  @override
  String get updateFieldProps => '✏️ Update Field';

  @override
  String get waitingForFieldData => 'Waiting for field data...';

  @override
  String get domainBracket => '[Domain]';

  @override
  String get domainLevel => 'Domain Level';

  @override
  String get belongingNodeDomain => 'Belonging Node / Domain';

  @override
  String get domainCommonField => 'Domain Common Field';

  @override
  String get highlight => 'Highlight';

  @override
  String get conditionalFieldControl => 'Conditional Field Control';

  @override
  String get enableCondition => 'Enable';

  @override
  String get conditionMode => 'Mode:';

  @override
  String get guiMode => 'GUI (Dropdown)';

  @override
  String get expressionMode => 'Expression';

  @override
  String get controlAction => 'Control Action:';

  @override
  String get actionShow => '👁️ Show on Match (SHOW)';

  @override
  String get actionHighlight => '✨ Highlight on Match (HIGHLIGHT)';

  @override
  String get actionRequire => '🔒 Require on Match (REQUIRE)';

  @override
  String get actionReadOnly => '📖 Read Only on Match (READ_ONLY)';

  @override
  String get actionDisable => '🚫 Disable on Match (DISABLE)';

  @override
  String get dependsOn => 'Depends On';

  @override
  String get operator => 'Operator';

  @override
  String get dateFormat => 'Date Format';

  @override
  String get canInputDirectly => 'Can input directly';

  @override
  String get schemaPropName => 'Field Name';

  @override
  String get schemaPropKey => 'Field Key';

  @override
  String get schemaPropType => 'Data Type';

  @override
  String get schemaPropRequired => 'Required';

  @override
  String get schemaPropIsSearchable => 'Searchable';

  @override
  String get schemaPropIsMultiValue => 'Multi-value';

  @override
  String get schemaPropIsEncrypted => 'Encrypted';

  @override
  String get schemaPropIsReadOnly => 'Read-Only';

  @override
  String get schemaPropIsHidden => 'Hidden';

  @override
  String get schemaPropIsImmutable => 'Immutable';

  @override
  String get schemaPropOrder => 'Sort Order';

  @override
  String get schemaPropGroup => 'Field Group';

  @override
  String get schemaPropUnit => 'Unit';

  @override
  String get schemaPropId => 'Identifier';

  @override
  String get schemaPropApprovalStatus => 'Approval Status';

  @override
  String get schemaPropIsPendingApproval => 'Is Pending Approval';

  @override
  String get schemaPropMarkingPattern => 'Masking Pattern';

  @override
  String get schemaPropHint => 'Input Hint';

  @override
  String get schemaPropFieldGroupId => 'Field Group ID';

  @override
  String get schemaPropDependsOnFieldKey => 'Depends On Field Key';

  @override
  String get schemaPropConditionOperator => 'Condition Operator';

  @override
  String get schemaPropConditionValue => 'Condition Value';

  @override
  String get schemaPropConditionAction => 'Condition Action';

  @override
  String get schemaPropConditionMode => 'Condition Mode';

  @override
  String get schemaPropConditionEnabled => 'Condition Enabled';

  @override
  String get schemaPropTrue => 'True';

  @override
  String get schemaPropFalse => 'False';

  @override
  String get governance => 'Governance';

  @override
  String get dataProfiling => 'Data Profiling';

  @override
  String get selectNodePrompt =>
      'Select a Classification Node from the tree to view or add fields.';

  @override
  String get schemaPackage => 'Domain Schema Package (Export / Import)';

  @override
  String get exportPackage => 'Export Schema Package';

  @override
  String get importPackage => 'Import Schema Package';

  @override
  String get packageExportDesc =>
      'Export taxonomy, field definitions, DQ rules, matching rules, and workflows as a JSON package.';

  @override
  String get packageImportDesc =>
      'Import domain metadata JSON package to restore or create entire domain structure.';

  @override
  String get exportDownloadJson => 'Download Package (.json)';

  @override
  String get importUploadJson => 'Execute Package Import';

  @override
  String get overwriteExisting => 'Overwrite Existing Domain';

  @override
  String get packageExportSubtext =>
      'Taxonomy nodes, field definitions, DQ rules, matching rules, and workflow configs are all packaged.';

  @override
  String get packagePreviewLabel => 'Package JSON Preview:';

  @override
  String get packageFileSelectLabel => 'Select JSON Package File (.json)';

  @override
  String get packagePreviewInfoLabel => 'Loaded Package Summary:';

  @override
  String get domainLabel => 'Domain Name';

  @override
  String packageSummaryCounts(Object nodes, Object fields, Object rules) {
    return 'Nodes $nodes · Fields $fields · DQ Rules $rules';
  }

  @override
  String get packageDownloadSuccess => 'Domain package download completed.';

  @override
  String get invalidJsonPackageFile => 'Invalid JSON package file.';

  @override
  String get profilingTitle => 'Data Profiling & Anomaly Detection';

  @override
  String get profilingDesc =>
      'Analyze field null rates, uniqueness, and statistical IQR anomalies across domain records.';

  @override
  String get nullRate => 'Null Rate';

  @override
  String get uniqueness => 'Uniqueness';

  @override
  String get distinctCount => 'Distinct Count';

  @override
  String outliersFound(Object count) {
    return 'Anomalies Found: $count';
  }

  @override
  String get noOutliers => 'No anomalies detected.';

  @override
  String get schemaSimulation => 'Schema Change Impact Simulation';

  @override
  String get schemaSimulationDesc =>
      'Pre-simulate and diagnose risks to existing records, integration channels, and DQ rules before applying schema changes.';

  @override
  String get safetyScore => 'Safety Score';

  @override
  String get runSimulation => 'Run Impact Simulation';

  @override
  String get simulationRecommendations => 'Safety Action Recommendations';

  @override
  String get businessGlossary => 'Business Glossary & Data Dictionary';

  @override
  String get businessGlossaryDesc =>
      'Define standard business terms, abbreviations, and data sensitivity levels with auto-recommendations.';

  @override
  String get termName => 'Standard Term Name';

  @override
  String get termCode => 'Standard Term Code';

  @override
  String get abbreviation => 'Abbreviation';

  @override
  String get synonyms => 'Synonyms';

  @override
  String get sensitivityLevel => 'Sensitivity Level';

  @override
  String get addTerm => 'Add Business Term';

  @override
  String get recommendedTerms => 'Recommended Terms';

  @override
  String get domainSnapshot => 'Domain Snapshot & Point-in-Time Restore';

  @override
  String get domainSnapshotDesc =>
      'Create full domain data snapshots and restore records to any specific point-in-time.';

  @override
  String get createSnapshot => 'Create Snapshot';

  @override
  String get snapshotName => 'Snapshot Name';

  @override
  String get versionTag => 'Version Tag';

  @override
  String get restoreSnapshot => 'Point-in-Time Restore';

  @override
  String confirmRestore(Object name, Object tag) {
    return 'Restore domain data to \'$name\' ($tag) snapshot state? Current data will be replaced.';
  }

  @override
  String get multilingualSync =>
      'Multilingual Metadata Auto-Translation & Sync';

  @override
  String get multilingualSyncDesc =>
      'Scan domain fields missing multilingual names and batch synchronize them with the Business Glossary.';

  @override
  String get missingLocalesCount => 'Missing Locales Count';

  @override
  String get missingLangs => 'Missing Languages';

  @override
  String get suggestedTranslation => 'Glossary Suggestion';

  @override
  String get allLocalesComplete =>
      'All fields have complete multilingual definitions.';

  @override
  String get dataAssetValuation => 'Data Asset Valuation & Catalog Explorer';

  @override
  String get dataAssetValuationDesc =>
      'Evaluates data asset ratings and monetary values across enterprise domains based on volume, connectivity, and DQ scores.';

  @override
  String get totalAssetValue => 'Total Data Asset Value';

  @override
  String get averageQualityScore => 'Average DQ Quality Score';

  @override
  String get assetRating => 'Asset Rating';

  @override
  String get estimatedValue => 'Estimated Value';

  @override
  String get schemaCompatibility => 'Schema Backward-Compatibility Analyzer';

  @override
  String get schemaCompatibilityDesc =>
      'Statically analyzes breaking change risks across external APIs when altering field types or constraints.';

  @override
  String get compatibilityStatus => 'Compatibility Status';

  @override
  String get riskScore => 'Risk Score';

  @override
  String get checkCompatibility => 'Run Compatibility Check';

  @override
  String get breakingChangeDetected => 'Breaking Changes Detected';

  @override
  String get compatibleStatus => 'Backward Compatible';

  @override
  String get semanticOntology =>
      'Cross-Domain Semantic Ontology Knowledge Graph';

  @override
  String get semanticOntologyDesc =>
      'Explore semantic relationships (purchased-by, contains, supplied-by, managed-by) across enterprise domains.';

  @override
  String get ontologyNodes => 'Ontology Nodes';

  @override
  String get ontologyEdges => 'Semantic Relations (Edges)';

  @override
  String get searchOntology => 'Search Knowledge Graph';

  @override
  String get relationType => 'Relation Type';

  @override
  String get navTabRecords => 'Records';

  @override
  String get navTabHome => 'Home';

  @override
  String get navTabApprovals => 'Approvals';

  @override
  String get navTabNotifications => 'Notifications';

  @override
  String get navTabChat => 'Chat';

  @override
  String get homeWelcomeTitle => 'Governance Portal Dashboard';

  @override
  String get homeTodoTitle => 'My Pending Tasks';

  @override
  String get homeRecentActivity => 'Recent Activities & Approvals';

  @override
  String get homeUnreadMessages => 'Unread Messages';

  @override
  String get homeNoActivity => 'No recent activities.';

  @override
  String get notificationsEmpty => 'No new notifications.';

  @override
  String get chatCreateRoom => 'Create Room';

  @override
  String get chatRoomTitlePlaceholder => 'Enter chat room title';

  @override
  String get chatTitle => 'Real-time Messenger';

  @override
  String get chatEmptyRooms => 'No active chat rooms.';

  @override
  String get chatSelectMembers => 'Select Members (at least 1 required)';

  @override
  String get chatSearchSelectUser => 'Search/Select User';

  @override
  String get chatNoUserSelected => 'No user selected.';

  @override
  String chatUserMe(String username) {
    return '$username (Me)';
  }

  @override
  String get chatCreateRoomFailed => 'Failed to create chat room.';

  @override
  String get chatSearchUserHint => 'Search by username, role, department...';

  @override
  String get chatConfirmBtn => 'Confirm';

  @override
  String get allCategories => 'All Categories';

  @override
  String get recordData => 'Record Data';

  @override
  String get viewReasonTitle => 'Enter View Reason';

  @override
  String get viewReasonHint => 'Enter reason (e.g. Identity verification)';

  @override
  String get viewReasonEmpty => 'Reason is required.';

  @override
  String get decryptSuccessNotice =>
      'Decrypted successfully. (Will be masked again in 30 seconds)';

  @override
  String get decryptFailedNotice => 'Decryption failed:';

  @override
  String get keyInfo => 'Key Information';

  @override
  String get generalInfo => 'General Information';

  @override
  String get viewHistory => 'View History';

  @override
  String get loginWithKeycloak => 'Login with Keycloak SSO';

  @override
  String get loginDividerOr => 'Or login with standard account';

  @override
  String get loginStandard => 'Standard Account Login';

  @override
  String get loginSsoError => 'An error occurred during SSO login.';
}
