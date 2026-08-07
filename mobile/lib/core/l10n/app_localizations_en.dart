// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

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
  String get deptRoles => 'Department Roles (Multiple Selectable)';

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
  String get systemcomplete => 'Completed';

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
      'This password will not be shown again. Please copy it and share it securely with the user.';

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
  String get accessReason => 'Access Reason';

  @override
  String get accessReasonPlaceholder =>
      'e.g., Business process, Customer request';

  @override
  String get accessReasonRequired => 'Please enter an access reason.';

  @override
  String get action => 'Action';

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
  String get actions => 'Actions';

  @override
  String get actionsCol => 'Actions';

  @override
  String get activeStatus => 'Active';

  @override
  String get add => 'Add';

  @override
  String get addcomment => 'Add Comment';

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
  String get alldone => 'All approvals/consensus are completed.';

  @override
  String get allTasksCleared => '✅ All Tasks Cleared';

  @override
  String get approvalDetailTitle => 'Approval Details';

  @override
  String get approvalLine => 'Approval Line';

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
  String get backupMenuSeed => 'Backup Current State';

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
  String get bulkApprove => 'Bulk Approve';

  @override
  String get bulkApproveLoading => 'Bulk approving...';

  @override
  String get bulkReject => 'Bulk Reject';

  @override
  String get bulkRejectLoading => 'Bulk rejecting...';

  @override
  String get calculated => 'Calculated';

  @override
  String get calculatedSuffix => '(Calculated)';

  @override
  String get cancel => 'Cancel';

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
  String get changedBy => 'Author / Modifier';

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
  String get close => 'Close';

  @override
  String get colaction => 'Action';

  @override
  String get colclassification => 'Classification';

  @override
  String get colcreatedat => 'Created At';

  @override
  String get coldomain => 'Domain';

  @override
  String get colidattr => 'ID Attribute';

  @override
  String get colnameattr => 'Name Attribute';

  @override
  String get colrequester => 'Requester';

  @override
  String get colstatus => 'Status';

  @override
  String get colsummary => 'Summary';

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
  String get consensus => 'Consensus';

  @override
  String get copyAsMarkdown => '📝 Copy as Markdown Table';

  @override
  String get copyCell => 'Copy Cell';

  @override
  String get copyTableBtn => 'Copy Table Data';

  @override
  String get copyTableBtnTitle => 'Copy Excel table data to clipboard';

  @override
  String get create => 'CREATE';

  @override
  String get createGroupBtn => 'Create Group';

  @override
  String get createWorkflowTitle => '🆕 Register New Workflow Template';

  @override
  String get created => 'Created At';

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
  String get decryptFailed => 'Failed to decrypt (Check permissions)';

  @override
  String get defaultBadge => '⭐ Default';

  @override
  String get delete => 'Delete';

  @override
  String get deleteErrorTitle => 'Deletion Error';

  @override
  String get deleteFailed => 'Failed to delete.';

  @override
  String get deleteSuccess => 'Delete Completed';

  @override
  String get deletedStatus => 'Deleted';

  @override
  String get department => 'Department';

  @override
  String get description => 'Description';

  @override
  String get descriptionCol => 'Description';

  @override
  String get details => 'Details';

  @override
  String get detailsInfo => 'Details';

  @override
  String get diffCountSuffix => 'Diffs';

  @override
  String get doReview => 'Review';

  @override
  String get domainRecordCreate => 'Domain Record Create';

  @override
  String get downloadFile => 'Download Completed File';

  @override
  String get draft => 'Draft';

  @override
  String get draftCommentOptional =>
      '(Optional) Please write a comment for the approver';

  @override
  String get draftCommentPlaceholder => 'Enter a comment...';

  @override
  String get draftCommentTitle => 'Draft Comment';

  @override
  String get draftCompleted => 'Draft Completed';

  @override
  String eGAbsKeyAKeyB2100(Object KEY_A, Object KEY_B) {
    return 'e.g. ABS($KEY_A + $KEY_B / 2) * 100';
  }

  @override
  String get edit => 'Edit';

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
  String get general => 'General';

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
  String get hideOriginal => 'Hide Original';

  @override
  String get history => 'History';

  @override
  String get historyVersionDiffDetail =>
      'Detailed Comparison of Version Changes';

  @override
  String get id => 'ID';

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
  String get integrationChannels => 'Integration Channels';

  @override
  String get integrationDetailTitle => 'Integration Details';

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
  String get isActive => 'Active';

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
  String get maxValue => 'Max Value';

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
  String get name => 'Name';

  @override
  String get newData => 'New Data';

  @override
  String get nextSnapshot => 'Next Snapshot';

  @override
  String get nocomment => 'No comment';

  @override
  String get noparsable => 'No parsable data provided.';

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
  String get noComment => 'No Comment';

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
  String get notifiedPersons => 'Notified Persons (CC)';

  @override
  String get number => 'Number';

  @override
  String get observers => 'Observers (CC)';

  @override
  String get onlyDifferences => 'Show Only Differences';

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
  String get processed => 'Processed';

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
  String get recordCreate => 'New Record Create';

  @override
  String get recordDelete => 'Record Delete';

  @override
  String get recordUpdate => 'Record Update';

  @override
  String get reflectDate => 'Reflect Date';

  @override
  String get refresh => 'Refresh';

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
  String get review => 'Review';

  @override
  String get riskLevel => 'Risk Level';

  @override
  String get save => 'Save';

  @override
  String get saveChanges => 'Save Changes';

  @override
  String get saveChangesHint =>
      '* Click the \'Save\' button at the top or bottom after editing cells to apply changes.';

  @override
  String get saveFailed => 'Failed to save.';

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
  String get selectIcon => 'Select Icon';

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
  String get sortOrder => 'Sort Order';

  @override
  String get startDate => 'Start Date';

  @override
  String get status => 'Status';

  @override
  String get statusActive => '🟢 Active';

  @override
  String get statusCol => 'Status';

  @override
  String get statusDraft => 'Draft';

  @override
  String get statusFilter => 'Status Filter';

  @override
  String get statusIgnored => 'Keep Separate';

  @override
  String get statusInactive => '🔴 Inactive';

  @override
  String get statusMerged => 'Merged';

  @override
  String get statusPending => 'Pending Review';

  @override
  String get statusWaiting => 'Waiting';

  @override
  String get stepdraft => 'Submitted';

  @override
  String get steppending => 'Pending';

  @override
  String get stepscheduled => 'Scheduled';

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
  String get stepType => 'Step Type';

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
  String get subtitle => 'System monitoring and management details';

  @override
  String get successDelete => 'Successfully deleted.';

  @override
  String get successSave => 'Successfully saved.';

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
  String get targetType => 'Target Type';

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
  String get title => 'Approvals';

  @override
  String get toValue => '~ To';

  @override
  String get today => 'Today';

  @override
  String get treeEmptyMessage =>
      'No classification tree found. Please click the Domain button below to create a new domain.';

  @override
  String get type => 'Type';

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
  String get update => 'UPDATE';

  @override
  String get updateSuccess => 'Update Completed';

  @override
  String get updatedat => 'Updated At';

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
  String get viewOriginal => 'View Original';

  @override
  String get viewSnapshot => 'View Snapshot';

  @override
  String get visualGraph => 'Visual Graph';

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
  String warningDqRules(Object count) {
    return '$count mapped Data Quality (DQ) rule(s) will be affected.';
  }

  @override
  String get noAffectedDqRules => 'No DQ rules linked';

  @override
  String get expectedDqViolations => 'Expected DQ Violations';

  @override
  String get affectedDqRules => 'Associated DQ Rules';

  @override
  String get addDqRule => 'Add Rule';

  @override
  String get addRule => '+ Add Rule';

  @override
  String get dqDashboardDesc =>
      'Real-time monitoring of data quality rule compliance, error counts, and field diagnosis status by domain.';

  @override
  String get dqDashboardSubtitle =>
      'Real-time Master Data Governance & Integrity Monitoring';

  @override
  String get dqDashboardTitle => 'Data Quality Dashboard';

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
  String get advancedSearch => 'Advanced Search';

  @override
  String get advancedSearchCondition => 'Advanced Search';

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
  String get dataLineage => 'Data Lineage';

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
  String get classificationAxes => 'Classification Axes';

  @override
  String get domainName => 'Domain Name';

  @override
  String get classificationName => 'Classification Name';

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
      '+ Add Editable Field (All if unselected)';

  @override
  String get addField => 'Add Field';

  @override
  String get addHiddenFieldPlaceholder => '+ Add Hidden Field';

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
  String get field => 'Field';

  @override
  String fieldKeyAlreadyExistsNewfieldValueKey(Object key) {
    return 'Field Key already exists: $key';
  }

  @override
  String get fieldKeyExists => 'Field Key already exists';

  @override
  String get fieldName => 'Field Name';

  @override
  String get fieldPermGroupTitle => 'Attribute Field Permissions';

  @override
  String get fields => 'Fields';

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
  String get nodeIcon => 'Department Icon';

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
  String get selectDomainPlaceholder => 'Select Domain';

  @override
  String get selectNodePlaceholder => 'Select Classification Node';

  @override
  String get selectTargetDomain => 'Please select a target domain.';

  @override
  String get selectTargetDomainAlert => 'Please select a target domain.';

  @override
  String get tabFields => 'Fields';

  @override
  String get targetDomain => 'Target Domain *';

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
  String get itemsCount => ' items';
}
