import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_ko.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'generated/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('ko'),
  ];

  /// No description provided for @emptyNotification.
  ///
  /// In en, this message translates to:
  /// **'No modified data.'**
  String get emptyNotification;

  /// No description provided for @beforeValue.
  ///
  /// In en, this message translates to:
  /// **'Previous Value'**
  String get beforeValue;

  /// No description provided for @afterValue.
  ///
  /// In en, this message translates to:
  /// **'New Value'**
  String get afterValue;

  /// No description provided for @approvalDraft.
  ///
  /// In en, this message translates to:
  /// **'Draft'**
  String get approvalDraft;

  /// No description provided for @processedStatus.
  ///
  /// In en, this message translates to:
  /// **'Processed'**
  String get processedStatus;

  /// No description provided for @systemAutoApprovedNotice.
  ///
  /// In en, this message translates to:
  /// **'System Auto-Approved (No approval line)'**
  String get systemAutoApprovedNotice;

  /// No description provided for @targetTypeRecordUpdate.
  ///
  /// In en, this message translates to:
  /// **'Master Record Update'**
  String get targetTypeRecordUpdate;

  /// No description provided for @targetTypeRecordCreate.
  ///
  /// In en, this message translates to:
  /// **'Master Record Create'**
  String get targetTypeRecordCreate;

  /// No description provided for @targetTypeRecordDelete.
  ///
  /// In en, this message translates to:
  /// **'Master Record Delete'**
  String get targetTypeRecordDelete;

  /// No description provided for @targetTypeSchemaChange.
  ///
  /// In en, this message translates to:
  /// **'Schema Change'**
  String get targetTypeSchemaChange;

  /// No description provided for @targetTypeSandbox.
  ///
  /// In en, this message translates to:
  /// **'Sandbox'**
  String get targetTypeSandbox;

  /// No description provided for @viewOriginal.
  ///
  /// In en, this message translates to:
  /// **'View Original'**
  String get viewOriginal;

  /// No description provided for @hideOriginal.
  ///
  /// In en, this message translates to:
  /// **'Hide Original'**
  String get hideOriginal;

  /// No description provided for @fontSizeSetting.
  ///
  /// In en, this message translates to:
  /// **'Font Size'**
  String get fontSizeSetting;

  /// No description provided for @fontSizeSmall.
  ///
  /// In en, this message translates to:
  /// **'Small'**
  String get fontSizeSmall;

  /// No description provided for @fontSizeMedium.
  ///
  /// In en, this message translates to:
  /// **'Medium'**
  String get fontSizeMedium;

  /// No description provided for @fontSizeLarge.
  ///
  /// In en, this message translates to:
  /// **'Large'**
  String get fontSizeLarge;

  /// No description provided for @fontSizeXlarge.
  ///
  /// In en, this message translates to:
  /// **'X-Large'**
  String get fontSizeXlarge;

  /// No description provided for @maskingPattern.
  ///
  /// In en, this message translates to:
  /// **'Masking Pattern'**
  String get maskingPattern;

  /// No description provided for @encryptedField.
  ///
  /// In en, this message translates to:
  /// **'Encrypted Field'**
  String get encryptedField;

  /// No description provided for @sensitiveAccessLogs.
  ///
  /// In en, this message translates to:
  /// **'Decryption Logs'**
  String get sensitiveAccessLogs;

  /// No description provided for @topUsers.
  ///
  /// In en, this message translates to:
  /// **'Top Users'**
  String get topUsers;

  /// No description provided for @accessLogViewer.
  ///
  /// In en, this message translates to:
  /// **'Viewer'**
  String get accessLogViewer;

  /// No description provided for @accessLogTargetType.
  ///
  /// In en, this message translates to:
  /// **'Target Type'**
  String get accessLogTargetType;

  /// No description provided for @accessLogTargetId.
  ///
  /// In en, this message translates to:
  /// **'Target ID'**
  String get accessLogTargetId;

  /// No description provided for @accessLogFields.
  ///
  /// In en, this message translates to:
  /// **'Accessed Fields'**
  String get accessLogFields;

  /// No description provided for @accessLogReason.
  ///
  /// In en, this message translates to:
  /// **'Access Reason'**
  String get accessLogReason;

  /// No description provided for @approvalREQUEST.
  ///
  /// In en, this message translates to:
  /// **'Approval Request'**
  String get approvalREQUEST;

  /// No description provided for @record.
  ///
  /// In en, this message translates to:
  /// **'Record Access'**
  String get record;

  /// No description provided for @recordHISTORY.
  ///
  /// In en, this message translates to:
  /// **'Record History'**
  String get recordHISTORY;

  /// No description provided for @accessLogIp.
  ///
  /// In en, this message translates to:
  /// **'IP Address'**
  String get accessLogIp;

  /// No description provided for @accessLogTime.
  ///
  /// In en, this message translates to:
  /// **'Access Time'**
  String get accessLogTime;

  /// No description provided for @maskingPatternGeneric.
  ///
  /// In en, this message translates to:
  /// **'Generic Masking (GENERIC)'**
  String get maskingPatternGeneric;

  /// No description provided for @maskingPatternCard.
  ///
  /// In en, this message translates to:
  /// **'Card Number (1234-****-****-5678)'**
  String get maskingPatternCard;

  /// No description provided for @maskingPatternRrn.
  ///
  /// In en, this message translates to:
  /// **'RRN/SSN (900101-1******)'**
  String get maskingPatternRrn;

  /// No description provided for @maskingPatternPhone.
  ///
  /// In en, this message translates to:
  /// **'Phone Number (010-****-5678)'**
  String get maskingPatternPhone;

  /// No description provided for @maskingPatternEmail.
  ///
  /// In en, this message translates to:
  /// **'Email Address (u***@example.com)'**
  String get maskingPatternEmail;

  /// No description provided for @addNewDept.
  ///
  /// In en, this message translates to:
  /// **'Add New Department'**
  String get addNewDept;

  /// No description provided for @addNewTeam.
  ///
  /// In en, this message translates to:
  /// **'Add New Team'**
  String get addNewTeam;

  /// No description provided for @addRole.
  ///
  /// In en, this message translates to:
  /// **'Add Role'**
  String get addRole;

  /// No description provided for @addRootDept.
  ///
  /// In en, this message translates to:
  /// **'Add Root Department'**
  String get addRootDept;

  /// No description provided for @addSubdept.
  ///
  /// In en, this message translates to:
  /// **'Add Sub-department'**
  String get addSubdept;

  /// No description provided for @addTeam.
  ///
  /// In en, this message translates to:
  /// **'Add Team'**
  String get addTeam;

  /// No description provided for @admin.
  ///
  /// In en, this message translates to:
  /// **'Admin'**
  String get admin;

  /// No description provided for @adminMonitor.
  ///
  /// In en, this message translates to:
  /// **'Admin Monitor'**
  String get adminMonitor;

  /// No description provided for @applicantRole.
  ///
  /// In en, this message translates to:
  /// **'Applicant Role'**
  String get applicantRole;

  /// No description provided for @applicantUser.
  ///
  /// In en, this message translates to:
  /// **'Applicant User'**
  String get applicantUser;

  /// No description provided for @assignDept.
  ///
  /// In en, this message translates to:
  /// **'Register to Dept'**
  String get assignDept;

  /// No description provided for @assigneeRole.
  ///
  /// In en, this message translates to:
  /// **'Role'**
  String get assigneeRole;

  /// No description provided for @assigneeUser.
  ///
  /// In en, this message translates to:
  /// **'User'**
  String get assigneeUser;

  /// No description provided for @auditSourceSystem.
  ///
  /// In en, this message translates to:
  /// **'Source System'**
  String get auditSourceSystem;

  /// No description provided for @belongsToDept.
  ///
  /// In en, this message translates to:
  /// **'Belongs to Department'**
  String get belongsToDept;

  /// No description provided for @belongsToOrg.
  ///
  /// In en, this message translates to:
  /// **'Belongs to Organization'**
  String get belongsToOrg;

  /// No description provided for @companyOrg.
  ///
  /// In en, this message translates to:
  /// **'Company / Organization'**
  String get companyOrg;

  /// No description provided for @checkDuplicate.
  ///
  /// In en, this message translates to:
  /// **'Check Duplicate'**
  String get checkDuplicate;

  /// No description provided for @createNewOrg.
  ///
  /// In en, this message translates to:
  /// **'Create New Organization'**
  String get createNewOrg;

  /// No description provided for @createOrganization.
  ///
  /// In en, this message translates to:
  /// **'Create Organization'**
  String get createOrganization;

  /// No description provided for @createRoleTitle.
  ///
  /// In en, this message translates to:
  /// **'Create Organization RBAC Role'**
  String get createRoleTitle;

  /// No description provided for @currentDept.
  ///
  /// In en, this message translates to:
  /// **'Current Dept'**
  String get currentDept;

  /// No description provided for @deleteDept.
  ///
  /// In en, this message translates to:
  /// **'Delete Department'**
  String get deleteDept;

  /// No description provided for @deleteOrganization.
  ///
  /// In en, this message translates to:
  /// **'Delete Organization'**
  String get deleteOrganization;

  /// No description provided for @dept.
  ///
  /// In en, this message translates to:
  /// **'Department'**
  String get dept;

  /// No description provided for @deptAssignCol.
  ///
  /// In en, this message translates to:
  /// **'Action'**
  String get deptAssignCol;

  /// No description provided for @deptMembers.
  ///
  /// In en, this message translates to:
  /// **'Manage Department Members'**
  String get deptMembers;

  /// No description provided for @deptMembersDesc.
  ///
  /// In en, this message translates to:
  /// **'Add new members to the selected department or unassign existing members.'**
  String get deptMembersDesc;

  /// No description provided for @deptName.
  ///
  /// In en, this message translates to:
  /// **'Department Name'**
  String get deptName;

  /// No description provided for @deptRoles.
  ///
  /// In en, this message translates to:
  /// **'Department Roles (Multi-selectable)'**
  String get deptRoles;

  /// No description provided for @deptStatusCol.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get deptStatusCol;

  /// No description provided for @deptStructure.
  ///
  /// In en, this message translates to:
  /// **'Department Hierarchy Structure (Tree View)'**
  String get deptStructure;

  /// No description provided for @deptStructureDesc.
  ///
  /// In en, this message translates to:
  /// **'Organization Hierarchy'**
  String get deptStructureDesc;

  /// No description provided for @deptTeamManagement.
  ///
  /// In en, this message translates to:
  /// **'Departments & Teams'**
  String get deptTeamManagement;

  /// No description provided for @editDept.
  ///
  /// In en, this message translates to:
  /// **'Edit Department'**
  String get editDept;

  /// No description provided for @editRoleTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Organization RBAC Role'**
  String get editRoleTitle;

  /// No description provided for @effectiveRoles.
  ///
  /// In en, this message translates to:
  /// **'Effective Roles'**
  String get effectiveRoles;

  /// No description provided for @encrypted.
  ///
  /// In en, this message translates to:
  /// **'Encrypted'**
  String get encrypted;

  /// No description provided for @errorUsernameExists.
  ///
  /// In en, this message translates to:
  /// **'Username already exists.'**
  String get errorUsernameExists;

  /// No description provided for @formulaSettings.
  ///
  /// In en, this message translates to:
  /// **'Formula Settings'**
  String get formulaSettings;

  /// No description provided for @installAdminName.
  ///
  /// In en, this message translates to:
  /// **'Admin Display Name'**
  String get installAdminName;

  /// No description provided for @installAdminPwd.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get installAdminPwd;

  /// No description provided for @installAdminPwdConfirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm Password'**
  String get installAdminPwdConfirm;

  /// No description provided for @installAdminUsername.
  ///
  /// In en, this message translates to:
  /// **'Admin Username'**
  String get installAdminUsername;

  /// No description provided for @installAdminEmail.
  ///
  /// In en, this message translates to:
  /// **'Email Address'**
  String get installAdminEmail;

  /// No description provided for @installRequireEmail.
  ///
  /// In en, this message translates to:
  /// **'Please enter an email address.'**
  String get installRequireEmail;

  /// No description provided for @installRequireEmailValid.
  ///
  /// In en, this message translates to:
  /// **'Invalid email format.'**
  String get installRequireEmailValid;

  /// No description provided for @sourceSystemNode.
  ///
  /// In en, this message translates to:
  /// **'Source System'**
  String get sourceSystemNode;

  /// No description provided for @installOrgEn.
  ///
  /// In en, this message translates to:
  /// **'Primary Master Organization (EN)'**
  String get installOrgEn;

  /// No description provided for @installOrgEnPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g. Enterprise HQ'**
  String get installOrgEnPlaceholder;

  /// No description provided for @installOrgKo.
  ///
  /// In en, this message translates to:
  /// **'Primary Master Organization (KO)'**
  String get installOrgKo;

  /// No description provided for @installOrgKoPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g. (주)엔터프라이즈 본사'**
  String get installOrgKoPlaceholder;

  /// No description provided for @installOrgEmailDomain.
  ///
  /// In en, this message translates to:
  /// **'Primary Email Domain'**
  String get installOrgEmailDomain;

  /// No description provided for @installOrgEmailDomainTip.
  ///
  /// In en, this message translates to:
  /// **'Configure the default email domain for organization members. (e.g. company.com, optional)'**
  String get installOrgEmailDomainTip;

  /// No description provided for @installOrgTip.
  ///
  /// In en, this message translates to:
  /// **'Standard system roles and wildcard(*) permissions will be automatically assigned to the created organization.'**
  String get installOrgTip;

  /// No description provided for @installRequireOrgEn.
  ///
  /// In en, this message translates to:
  /// **'Please enter organization name in English.'**
  String get installRequireOrgEn;

  /// No description provided for @installRequireOrgKo.
  ///
  /// In en, this message translates to:
  /// **'Please enter organization name in Korean.'**
  String get installRequireOrgKo;

  /// No description provided for @installRequireUsername.
  ///
  /// In en, this message translates to:
  /// **'Please enter username.'**
  String get installRequireUsername;

  /// No description provided for @integrationChannelSystem.
  ///
  /// In en, this message translates to:
  /// **'Channel / System'**
  String get integrationChannelSystem;

  /// No description provided for @labelRole.
  ///
  /// In en, this message translates to:
  /// **'Role'**
  String get labelRole;

  /// No description provided for @labelUsername.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get labelUsername;

  /// No description provided for @menuAccessLogs.
  ///
  /// In en, this message translates to:
  /// **'Menu Access Logs'**
  String get menuAccessLogs;

  /// No description provided for @msgUsernameAvailable.
  ///
  /// In en, this message translates to:
  /// **'Username is available.'**
  String get msgUsernameAvailable;

  /// No description provided for @msgUsernameCheckRequired.
  ///
  /// In en, this message translates to:
  /// **'Please check username availability.'**
  String get msgUsernameCheckRequired;

  /// No description provided for @msgUsernameExists.
  ///
  /// In en, this message translates to:
  /// **'Username already exists.'**
  String get msgUsernameExists;

  /// No description provided for @noDeptAssignedTip.
  ///
  /// In en, this message translates to:
  /// **'(Department Unassigned - Assign in [Organization Management])'**
  String get noDeptAssignedTip;

  /// No description provided for @noDeptsAdded.
  ///
  /// In en, this message translates to:
  /// **'No departments added yet. Click [+ Add Department] button.'**
  String get noDeptsAdded;

  /// No description provided for @noOrgHistory.
  ///
  /// In en, this message translates to:
  /// **'No organization change history recorded.'**
  String get noOrgHistory;

  /// No description provided for @optionsSettings.
  ///
  /// In en, this message translates to:
  /// **'Options Settings'**
  String get optionsSettings;

  /// No description provided for @orgCodePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'System Code (Unique, e.g. acme_corp)'**
  String get orgCodePlaceholder;

  /// No description provided for @orgCreatedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Organization created successfully.'**
  String get orgCreatedSuccess;

  /// No description provided for @orgDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete organization.'**
  String get orgDeleteFailed;

  /// No description provided for @orgDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Organization deleted successfully.'**
  String get orgDeleteSuccess;

  /// No description provided for @orgDescription.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get orgDescription;

  /// No description provided for @orgDisplayName.
  ///
  /// In en, this message translates to:
  /// **'Display Name'**
  String get orgDisplayName;

  /// No description provided for @orgDisplayNamePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Display Name (e.g. Acme Corporation)'**
  String get orgDisplayNamePlaceholder;

  /// No description provided for @orgHistoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Organization History'**
  String get orgHistoryTitle;

  /// No description provided for @orgIcon.
  ///
  /// In en, this message translates to:
  /// **'Organization Icon'**
  String get orgIcon;

  /// No description provided for @orgInfoTitle.
  ///
  /// In en, this message translates to:
  /// **'Organization Information'**
  String get orgInfoTitle;

  /// No description provided for @orgList.
  ///
  /// In en, this message translates to:
  /// **'Organizations'**
  String get orgList;

  /// No description provided for @orgManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage multi-tenant organizations, hierarchical departments/teams, and RBAC role/permission structures.'**
  String get orgManagementDesc;

  /// No description provided for @orgSysCode.
  ///
  /// In en, this message translates to:
  /// **'System Code (Unique)'**
  String get orgSysCode;

  /// No description provided for @orgTenantManagement.
  ///
  /// In en, this message translates to:
  /// **'Organizations & Departments'**
  String get orgTenantManagement;

  /// No description provided for @orgUpdatedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Organization info updated successfully.'**
  String get orgUpdatedSuccess;

  /// No description provided for @organization.
  ///
  /// In en, this message translates to:
  /// **'Organization'**
  String get organization;

  /// No description provided for @organizationManagement.
  ///
  /// In en, this message translates to:
  /// **'Organization Management'**
  String get organizationManagement;

  /// No description provided for @otherDept.
  ///
  /// In en, this message translates to:
  /// **'Other Dept'**
  String get otherDept;

  /// No description provided for @parentDept.
  ///
  /// In en, this message translates to:
  /// **'Parent Department (Default: Root)'**
  String get parentDept;

  /// No description provided for @personalSettings.
  ///
  /// In en, this message translates to:
  /// **'Personal Settings'**
  String get personalSettings;

  /// No description provided for @placeholderUsername.
  ///
  /// In en, this message translates to:
  /// **'Enter your username'**
  String get placeholderUsername;

  /// No description provided for @rbacRoleManagement.
  ///
  /// In en, this message translates to:
  /// **'RBAC Roles'**
  String get rbacRoleManagement;

  /// No description provided for @requiredRoles.
  ///
  /// In en, this message translates to:
  /// **'Required Roles (Multiple)'**
  String get requiredRoles;

  /// No description provided for @roleAdmin.
  ///
  /// In en, this message translates to:
  /// **'System Administrator'**
  String get roleAdmin;

  /// No description provided for @roleAssignCol.
  ///
  /// In en, this message translates to:
  /// **'Assign Role'**
  String get roleAssignCol;

  /// No description provided for @roleCodeLabel.
  ///
  /// In en, this message translates to:
  /// **'Role Code (e.g. CUSTOM_MANAGER)'**
  String get roleCodeLabel;

  /// No description provided for @roleCreationPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Role creation functionality is under preparation.'**
  String get roleCreationPlaceholder;

  /// No description provided for @roleDescriptionLabel.
  ///
  /// In en, this message translates to:
  /// **'Role Description'**
  String get roleDescriptionLabel;

  /// No description provided for @roleDisplayNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Role Display Name'**
  String get roleDisplayNameLabel;

  /// No description provided for @roleDomainEditor.
  ///
  /// In en, this message translates to:
  /// **'Domain Editor'**
  String get roleDomainEditor;

  /// No description provided for @roleDomainViewer.
  ///
  /// In en, this message translates to:
  /// **'Domain Viewer'**
  String get roleDomainViewer;

  /// No description provided for @roleUser.
  ///
  /// In en, this message translates to:
  /// **'Standard User'**
  String get roleUser;

  /// No description provided for @saveRole.
  ///
  /// In en, this message translates to:
  /// **'Save Role'**
  String get saveRole;

  /// No description provided for @searchUserBtn.
  ///
  /// In en, this message translates to:
  /// **'Search & Register User'**
  String get searchUserBtn;

  /// No description provided for @searchUserModalTitle.
  ///
  /// In en, this message translates to:
  /// **'Search & Select Department Member'**
  String get searchUserModalTitle;

  /// No description provided for @searchUserPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search username or role...'**
  String get searchUserPlaceholder;

  /// No description provided for @selectApproverRole.
  ///
  /// In en, this message translates to:
  /// **'Select Approval Role'**
  String get selectApproverRole;

  /// No description provided for @selectApproverUser.
  ///
  /// In en, this message translates to:
  /// **'Select Approver'**
  String get selectApproverUser;

  /// No description provided for @selectRole.
  ///
  /// In en, this message translates to:
  /// **'Select Role'**
  String get selectRole;

  /// No description provided for @selectRoleToAdd.
  ///
  /// In en, this message translates to:
  /// **'Select a role to add'**
  String get selectRoleToAdd;

  /// No description provided for @selectUser.
  ///
  /// In en, this message translates to:
  /// **'Select User'**
  String get selectUser;

  /// No description provided for @syncDefaultRoles.
  ///
  /// In en, this message translates to:
  /// **'Sync Default Roles & Permissions'**
  String get syncDefaultRoles;

  /// No description provided for @syncDefaultRolesConfirmAll.
  ///
  /// In en, this message translates to:
  /// **'Do you want to sync the 8 default system roles and missing permissions for all organizations?'**
  String get syncDefaultRolesConfirmAll;

  /// Translated from sync_default_roles_confirm_org
  ///
  /// In en, this message translates to:
  /// **'Do you want to sync the 8 default system roles and missing permissions for org \'{name}\'?'**
  String syncDefaultRolesConfirmOrg(Object name);

  /// No description provided for @syncDefaultRolesError.
  ///
  /// In en, this message translates to:
  /// **'An error occurred during synchronization.'**
  String get syncDefaultRolesError;

  /// No description provided for @syncDefaultRolesFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to synchronize default roles.'**
  String get syncDefaultRolesFail;

  /// No description provided for @syncDefaultRolesSuccess.
  ///
  /// In en, this message translates to:
  /// **'Default roles and permissions synchronization completed successfully.'**
  String get syncDefaultRolesSuccess;

  /// No description provided for @systemapplied.
  ///
  /// In en, this message translates to:
  /// **'System Reflect'**
  String get systemapplied;

  /// No description provided for @systemcancelled.
  ///
  /// In en, this message translates to:
  /// **'Cancelled'**
  String get systemcancelled;

  /// No description provided for @systemcomplete.
  ///
  /// In en, this message translates to:
  /// **'Complete'**
  String get systemcomplete;

  /// No description provided for @systemCustomRoles.
  ///
  /// In en, this message translates to:
  /// **'System & Custom Roles'**
  String get systemCustomRoles;

  /// No description provided for @systemLogs.
  ///
  /// In en, this message translates to:
  /// **'System Logs'**
  String get systemLogs;

  /// No description provided for @systemLogsDesc.
  ///
  /// In en, this message translates to:
  /// **'Real-time monitoring of system operation history, user logins, exception errors, and integration channel logs.'**
  String get systemLogsDesc;

  /// No description provided for @systemLogsTitle.
  ///
  /// In en, this message translates to:
  /// **'System Audit & Integration Logs'**
  String get systemLogsTitle;

  /// No description provided for @systemNotification.
  ///
  /// In en, this message translates to:
  /// **'System Notification'**
  String get systemNotification;

  /// No description provided for @systemOrgInfo.
  ///
  /// In en, this message translates to:
  /// **'System Organization Info'**
  String get systemOrgInfo;

  /// No description provided for @team.
  ///
  /// In en, this message translates to:
  /// **'Team'**
  String get team;

  /// No description provided for @teamName.
  ///
  /// In en, this message translates to:
  /// **'Team Name'**
  String get teamName;

  /// No description provided for @tempPassword.
  ///
  /// In en, this message translates to:
  /// **'Temporary Password'**
  String get tempPassword;

  /// No description provided for @tempPasswordCheck.
  ///
  /// In en, this message translates to:
  /// **'Temporary Password Check'**
  String get tempPasswordCheck;

  /// No description provided for @viewTempPassword.
  ///
  /// In en, this message translates to:
  /// **'View Temporary Password'**
  String get viewTempPassword;

  /// No description provided for @tempPasswordWarning.
  ///
  /// In en, this message translates to:
  /// **'This password will not be displayed again. Please make sure to copy and deliver it to the user.'**
  String get tempPasswordWarning;

  /// No description provided for @updateRole.
  ///
  /// In en, this message translates to:
  /// **'Update Role'**
  String get updateRole;

  /// No description provided for @userManagement.
  ///
  /// In en, this message translates to:
  /// **'User Management'**
  String get userManagement;

  /// No description provided for @userManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage user account information, organization/department assignments, system roles, and domain access permissions.'**
  String get userManagementDesc;

  /// No description provided for @userProfileTitle.
  ///
  /// In en, this message translates to:
  /// **'User Profile'**
  String get userProfileTitle;

  /// No description provided for @userInfoAndRole.
  ///
  /// In en, this message translates to:
  /// **'User Information & System Roles'**
  String get userInfoAndRole;

  /// No description provided for @userEmail.
  ///
  /// In en, this message translates to:
  /// **'Email Address'**
  String get userEmail;

  /// No description provided for @saveUserInfo.
  ///
  /// In en, this message translates to:
  /// **'Save User Info'**
  String get saveUserInfo;

  /// No description provided for @userInfoUpdatedSuccess.
  ///
  /// In en, this message translates to:
  /// **'User information (email and roles) has been successfully saved.'**
  String get userInfoUpdatedSuccess;

  /// No description provided for @userInfoUpdateFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save user information: '**
  String get userInfoUpdateFailed;

  /// No description provided for @invalidEmailFormat.
  ///
  /// In en, this message translates to:
  /// **'Please enter a valid email address.'**
  String get invalidEmailFormat;

  /// No description provided for @userRole.
  ///
  /// In en, this message translates to:
  /// **'User Role'**
  String get userRole;

  /// No description provided for @userRoles.
  ///
  /// In en, this message translates to:
  /// **'User System Roles (Multi-selectable)'**
  String get userRoles;

  /// No description provided for @username.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get username;

  /// No description provided for @usernameCol.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get usernameCol;

  /// No description provided for @viewUserProfile.
  ///
  /// In en, this message translates to:
  /// **'View user profile'**
  String get viewUserProfile;

  /// No description provided for @backupSeedFiles.
  ///
  /// In en, this message translates to:
  /// **'Backup State to Seed Files'**
  String get backupSeedFiles;

  /// No description provided for @codeManagementTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Dictionary'**
  String get codeManagementTitle;

  /// No description provided for @title.
  ///
  /// In en, this message translates to:
  /// **'Schema Change History'**
  String get title;

  /// No description provided for @codeManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage common system codes, dictionary groups, and hierarchical data code details globally.'**
  String get codeManagementDesc;

  /// No description provided for @desc.
  ///
  /// In en, this message translates to:
  /// **'Configure EXACT / FUZZY matching rules and similarity thresholds for duplicate record identification.'**
  String get desc;

  /// No description provided for @codeManagementExportJson.
  ///
  /// In en, this message translates to:
  /// **'Export JSON'**
  String get codeManagementExportJson;

  /// No description provided for @exportJson.
  ///
  /// In en, this message translates to:
  /// **'Export JSON'**
  String get exportJson;

  /// No description provided for @codeManagementImportJson.
  ///
  /// In en, this message translates to:
  /// **'Import JSON'**
  String get codeManagementImportJson;

  /// No description provided for @importJson.
  ///
  /// In en, this message translates to:
  /// **'Import JSON'**
  String get importJson;

  /// No description provided for @codeManagementCodeGroups.
  ///
  /// In en, this message translates to:
  /// **'Code Groups'**
  String get codeManagementCodeGroups;

  /// No description provided for @codeGroups.
  ///
  /// In en, this message translates to:
  /// **'Code Groups'**
  String get codeGroups;

  /// No description provided for @codeManagementCodeDetails.
  ///
  /// In en, this message translates to:
  /// **'Code Details'**
  String get codeManagementCodeDetails;

  /// No description provided for @codeDetails.
  ///
  /// In en, this message translates to:
  /// **'Code Details'**
  String get codeDetails;

  /// No description provided for @codeManagementAdd.
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get codeManagementAdd;

  /// No description provided for @add.
  ///
  /// In en, this message translates to:
  /// **'Add Channel'**
  String get add;

  /// No description provided for @codeManagementGroupCode.
  ///
  /// In en, this message translates to:
  /// **'Group Code'**
  String get codeManagementGroupCode;

  /// No description provided for @groupCode.
  ///
  /// In en, this message translates to:
  /// **'Group Code'**
  String get groupCode;

  /// No description provided for @codeManagementName.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get codeManagementName;

  /// No description provided for @name.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get name;

  /// No description provided for @codeManagementStatus.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get codeManagementStatus;

  /// No description provided for @status.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get status;

  /// No description provided for @codeManagementManage.
  ///
  /// In en, this message translates to:
  /// **'Manage'**
  String get codeManagementManage;

  /// No description provided for @manage.
  ///
  /// In en, this message translates to:
  /// **'Manage'**
  String get manage;

  /// No description provided for @codeManagementDetailCode.
  ///
  /// In en, this message translates to:
  /// **'Detail Code'**
  String get codeManagementDetailCode;

  /// No description provided for @detailCode.
  ///
  /// In en, this message translates to:
  /// **'Detail Code'**
  String get detailCode;

  /// No description provided for @codeManagementSortOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get codeManagementSortOrder;

  /// No description provided for @sortOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get sortOrder;

  /// No description provided for @codeManagementEditGroup.
  ///
  /// In en, this message translates to:
  /// **'Edit Group'**
  String get codeManagementEditGroup;

  /// No description provided for @editGroup.
  ///
  /// In en, this message translates to:
  /// **'Edit Group'**
  String get editGroup;

  /// No description provided for @codeManagementAddGroup.
  ///
  /// In en, this message translates to:
  /// **'Add Group'**
  String get codeManagementAddGroup;

  /// No description provided for @addGroup.
  ///
  /// In en, this message translates to:
  /// **'Add Group'**
  String get addGroup;

  /// No description provided for @codeManagementNameKo.
  ///
  /// In en, this message translates to:
  /// **'Name (Korean)'**
  String get codeManagementNameKo;

  /// No description provided for @nameKo.
  ///
  /// In en, this message translates to:
  /// **'Name (Korean)'**
  String get nameKo;

  /// No description provided for @codeManagementNameEn.
  ///
  /// In en, this message translates to:
  /// **'Name (English)'**
  String get codeManagementNameEn;

  /// No description provided for @nameEn.
  ///
  /// In en, this message translates to:
  /// **'Name (English)'**
  String get nameEn;

  /// No description provided for @codeManagementDescKo.
  ///
  /// In en, this message translates to:
  /// **'Description (Korean)'**
  String get codeManagementDescKo;

  /// No description provided for @descKo.
  ///
  /// In en, this message translates to:
  /// **'Description (Korean)'**
  String get descKo;

  /// No description provided for @codeManagementDescEn.
  ///
  /// In en, this message translates to:
  /// **'Description (English)'**
  String get codeManagementDescEn;

  /// No description provided for @descEn.
  ///
  /// In en, this message translates to:
  /// **'Description (English)'**
  String get descEn;

  /// No description provided for @codeManagementActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get codeManagementActive;

  /// No description provided for @active.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get active;

  /// No description provided for @codeManagementCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get codeManagementCancel;

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// No description provided for @codeManagementSave.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get codeManagementSave;

  /// No description provided for @save.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get save;

  /// No description provided for @codeManagementEditDetail.
  ///
  /// In en, this message translates to:
  /// **'Edit Detail Code'**
  String get codeManagementEditDetail;

  /// No description provided for @editDetail.
  ///
  /// In en, this message translates to:
  /// **'Edit Detail Code'**
  String get editDetail;

  /// No description provided for @codeManagementAddDetail.
  ///
  /// In en, this message translates to:
  /// **'Add Detail Code'**
  String get codeManagementAddDetail;

  /// No description provided for @addDetail.
  ///
  /// In en, this message translates to:
  /// **'Add Detail Code'**
  String get addDetail;

  /// No description provided for @codeManagementSelectGroupMsg.
  ///
  /// In en, this message translates to:
  /// **'Please select a code group from the left panel.'**
  String get codeManagementSelectGroupMsg;

  /// No description provided for @selectGroupMsg.
  ///
  /// In en, this message translates to:
  /// **'Please select a code group from the left panel.'**
  String get selectGroupMsg;

  /// Translated from code_management_confirm_delete_group
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete group {code}?'**
  String codeManagementConfirmDeleteGroup(Object code);

  /// Translated from confirm_delete_group
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete group {code}?'**
  String confirmDeleteGroup(Object code);

  /// Translated from code_management_confirm_delete_detail
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete detail {code}?'**
  String codeManagementConfirmDeleteDetail(Object code);

  /// Translated from confirm_delete_detail
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete detail {code}?'**
  String confirmDeleteDetail(Object code);

  /// No description provided for @codeManagementExportFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to export JSON.'**
  String get codeManagementExportFailed;

  /// No description provided for @exportFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to export JSON.'**
  String get exportFailed;

  /// No description provided for @codeManagementImportSuccess.
  ///
  /// In en, this message translates to:
  /// **'Codes imported successfully.'**
  String get codeManagementImportSuccess;

  /// Translated from import_success
  ///
  /// In en, this message translates to:
  /// **'Domain package imported successfully ({nodes} nodes, {fields} fields created).'**
  String importSuccess(Object nodes, Object fields);

  /// No description provided for @codeManagementImportFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to import JSON. Invalid format or server error.'**
  String get codeManagementImportFailed;

  /// No description provided for @importFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to import domain package.'**
  String get importFailed;

  /// No description provided for @codeManagementSaveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Saved successfully.'**
  String get codeManagementSaveSuccess;

  /// No description provided for @saveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Survivorship rules saved successfully.'**
  String get saveSuccess;

  /// No description provided for @codeManagementSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save.'**
  String get codeManagementSaveFailed;

  /// No description provided for @saveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save.'**
  String get saveFailed;

  /// No description provided for @codeManagementDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Deleted successfully.'**
  String get codeManagementDeleteSuccess;

  /// No description provided for @deleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Delete Completed'**
  String get deleteSuccess;

  /// No description provided for @codeManagementDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete.'**
  String get codeManagementDeleteFailed;

  /// No description provided for @deleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete.'**
  String get deleteFailed;

  /// No description provided for @codeManagementLoadFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to load data.'**
  String get codeManagementLoadFailed;

  /// No description provided for @loadFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to load data.'**
  String get loadFailed;

  /// No description provided for @codeManagementSyncCodes.
  ///
  /// In en, this message translates to:
  /// **'Sync Default Codes'**
  String get codeManagementSyncCodes;

  /// No description provided for @syncCodes.
  ///
  /// In en, this message translates to:
  /// **'Sync Default Codes'**
  String get syncCodes;

  /// No description provided for @codeManagementDumpCodes.
  ///
  /// In en, this message translates to:
  /// **'Backup Codes (Dump)'**
  String get codeManagementDumpCodes;

  /// No description provided for @dumpCodes.
  ///
  /// In en, this message translates to:
  /// **'Backup Codes (Dump)'**
  String get dumpCodes;

  /// No description provided for @globalSearchPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Global Search (Search any data...)'**
  String get globalSearchPlaceholder;

  /// No description provided for @searchMinLength.
  ///
  /// In en, this message translates to:
  /// **'Please enter at least 2 characters.'**
  String get searchMinLength;

  /// No description provided for @searchNoResults.
  ///
  /// In en, this message translates to:
  /// **'No results found.'**
  String get searchNoResults;

  /// No description provided for @searchNoData.
  ///
  /// In en, this message translates to:
  /// **'No Data'**
  String get searchNoData;

  /// No description provided for @matchingrulesTitle.
  ///
  /// In en, this message translates to:
  /// **'Matching Rules Management'**
  String get matchingrulesTitle;

  /// No description provided for @matchingrulesSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Configure EXACT / FUZZY matching rules and similarity thresholds for deduplication.'**
  String get matchingrulesSubtitle;

  /// No description provided for @subtitle.
  ///
  /// In en, this message translates to:
  /// **'Internal Messages & Email Management'**
  String get subtitle;

  /// No description provided for @matchingrulesSelectDomainPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get matchingrulesSelectDomainPlaceholder;

  /// No description provided for @selectDomainPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get selectDomainPlaceholder;

  /// No description provided for @matchingrulesAddRule.
  ///
  /// In en, this message translates to:
  /// **'Add Rule'**
  String get matchingrulesAddRule;

  /// No description provided for @addRule.
  ///
  /// In en, this message translates to:
  /// **'Add Business Rule'**
  String get addRule;

  /// No description provided for @matchingrulesRefresh.
  ///
  /// In en, this message translates to:
  /// **'Refresh'**
  String get matchingrulesRefresh;

  /// No description provided for @refresh.
  ///
  /// In en, this message translates to:
  /// **'Refresh'**
  String get refresh;

  /// No description provided for @matchingrulesTotalReviewed.
  ///
  /// In en, this message translates to:
  /// **'Total Reviewed'**
  String get matchingrulesTotalReviewed;

  /// No description provided for @totalReviewed.
  ///
  /// In en, this message translates to:
  /// **'Total Reviewed'**
  String get totalReviewed;

  /// No description provided for @matchingrulesPrecision.
  ///
  /// In en, this message translates to:
  /// **'Precision'**
  String get matchingrulesPrecision;

  /// No description provided for @precision.
  ///
  /// In en, this message translates to:
  /// **'Precision'**
  String get precision;

  /// No description provided for @matchingrulesConfirmed.
  ///
  /// In en, this message translates to:
  /// **'Confirmed'**
  String get matchingrulesConfirmed;

  /// No description provided for @confirmed.
  ///
  /// In en, this message translates to:
  /// **'Confirmed'**
  String get confirmed;

  /// No description provided for @matchingrulesRejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get matchingrulesRejected;

  /// No description provided for @rejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get rejected;

  /// No description provided for @matchingrulesCurrentThreshold.
  ///
  /// In en, this message translates to:
  /// **'Current Threshold'**
  String get matchingrulesCurrentThreshold;

  /// No description provided for @currentThreshold.
  ///
  /// In en, this message translates to:
  /// **'Current Threshold'**
  String get currentThreshold;

  /// No description provided for @matchingrulesRecommendedThreshold.
  ///
  /// In en, this message translates to:
  /// **'Recommended Threshold'**
  String get matchingrulesRecommendedThreshold;

  /// No description provided for @recommendedThreshold.
  ///
  /// In en, this message translates to:
  /// **'Recommended Threshold'**
  String get recommendedThreshold;

  /// No description provided for @matchingrulesRuleList.
  ///
  /// In en, this message translates to:
  /// **'Matching Rules List'**
  String get matchingrulesRuleList;

  /// No description provided for @ruleList.
  ///
  /// In en, this message translates to:
  /// **'Rule List'**
  String get ruleList;

  /// Translated from matchingRules_items_count
  ///
  /// In en, this message translates to:
  /// **'{count} items'**
  String matchingrulesItemsCount(Object count);

  /// Translated from items_count
  ///
  /// In en, this message translates to:
  /// **' items'**
  String itemsCount(Object count);

  /// No description provided for @matchingrulesEmptyNoRules.
  ///
  /// In en, this message translates to:
  /// **'No matching rules registered.'**
  String get matchingrulesEmptyNoRules;

  /// No description provided for @emptyNoRules.
  ///
  /// In en, this message translates to:
  /// **'No survivorship rules configured.'**
  String get emptyNoRules;

  /// No description provided for @matchingrulesEmptySelectDomain.
  ///
  /// In en, this message translates to:
  /// **'Please select a domain first from the dropdown above.'**
  String get matchingrulesEmptySelectDomain;

  /// No description provided for @emptySelectDomain.
  ///
  /// In en, this message translates to:
  /// **'Please select a domain first from the dropdown above.'**
  String get emptySelectDomain;

  /// No description provided for @matchingrulesEmptyNoRulesDesc.
  ///
  /// In en, this message translates to:
  /// **'Click \'+ Add Rule\' at the top right to create a new rule for deduplication.'**
  String get matchingrulesEmptyNoRulesDesc;

  /// No description provided for @emptyNoRulesDesc.
  ///
  /// In en, this message translates to:
  /// **'Click \'+ Add Rule\' at the top right to create a new rule for deduplication.'**
  String get emptyNoRulesDesc;

  /// No description provided for @matchingrulesEmptySelectDomainDesc.
  ///
  /// In en, this message translates to:
  /// **'Selecting a domain will display its deduplication rule list in AG-Grid.'**
  String get matchingrulesEmptySelectDomainDesc;

  /// No description provided for @emptySelectDomainDesc.
  ///
  /// In en, this message translates to:
  /// **'Selecting a domain will display its deduplication rule list in AG-Grid.'**
  String get emptySelectDomainDesc;

  /// No description provided for @matchingrulesAddFirstRule.
  ///
  /// In en, this message translates to:
  /// **'Add First Matching Rule'**
  String get matchingrulesAddFirstRule;

  /// No description provided for @addFirstRule.
  ///
  /// In en, this message translates to:
  /// **'Add First Rule'**
  String get addFirstRule;

  /// No description provided for @matchingrulesCreateTitle.
  ///
  /// In en, this message translates to:
  /// **'Add New Matching Rule'**
  String get matchingrulesCreateTitle;

  /// No description provided for @createTitle.
  ///
  /// In en, this message translates to:
  /// **'Add New Matching Rule'**
  String get createTitle;

  /// No description provided for @matchingrulesEditTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Matching Rule'**
  String get matchingrulesEditTitle;

  /// No description provided for @editTitle.
  ///
  /// In en, this message translates to:
  /// **'Edit Matching Rule'**
  String get editTitle;

  /// No description provided for @matchingrulesRuleName.
  ///
  /// In en, this message translates to:
  /// **'Rule Name'**
  String get matchingrulesRuleName;

  /// No description provided for @ruleName.
  ///
  /// In en, this message translates to:
  /// **'Rule Name'**
  String get ruleName;

  /// No description provided for @matchingrulesRuleNamePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g., Name and Contact Match Rule'**
  String get matchingrulesRuleNamePlaceholder;

  /// No description provided for @ruleNamePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g., Name and Contact Match Rule'**
  String get ruleNamePlaceholder;

  /// No description provided for @matchingrulesMatchType.
  ///
  /// In en, this message translates to:
  /// **'Match Type'**
  String get matchingrulesMatchType;

  /// No description provided for @matchType.
  ///
  /// In en, this message translates to:
  /// **'Match Type'**
  String get matchType;

  /// No description provided for @matchingrulesTargetFields.
  ///
  /// In en, this message translates to:
  /// **'Target Fields (Multi-select)'**
  String get matchingrulesTargetFields;

  /// No description provided for @targetFields.
  ///
  /// In en, this message translates to:
  /// **'Target Fields (Multi-select)'**
  String get targetFields;

  /// No description provided for @matchingrulesTargetFieldsCsv.
  ///
  /// In en, this message translates to:
  /// **'Target Field Keys (comma separated)'**
  String get matchingrulesTargetFieldsCsv;

  /// No description provided for @targetFieldsCsv.
  ///
  /// In en, this message translates to:
  /// **'Target Field Keys (comma separated)'**
  String get targetFieldsCsv;

  /// No description provided for @matchingrulesSimilarityThreshold.
  ///
  /// In en, this message translates to:
  /// **'Similarity Threshold (0.5 ~ 1.0)'**
  String get matchingrulesSimilarityThreshold;

  /// No description provided for @similarityThreshold.
  ///
  /// In en, this message translates to:
  /// **'Similarity Threshold (0.5 ~ 1.0)'**
  String get similarityThreshold;

  /// No description provided for @matchingrulesIsActive.
  ///
  /// In en, this message translates to:
  /// **'Rule Is Active'**
  String get matchingrulesIsActive;

  /// No description provided for @isActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get isActive;

  /// No description provided for @matchingrulesCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get matchingrulesCancel;

  /// No description provided for @matchingrulesSave.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get matchingrulesSave;

  /// No description provided for @matchingrulesActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get matchingrulesActive;

  /// No description provided for @matchingrulesInactive.
  ///
  /// In en, this message translates to:
  /// **'Inactive'**
  String get matchingrulesInactive;

  /// No description provided for @inactive.
  ///
  /// In en, this message translates to:
  /// **'Inactive'**
  String get inactive;

  /// No description provided for @matchingrulesSaveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Matching rule saved successfully.'**
  String get matchingrulesSaveSuccess;

  /// No description provided for @matchingrulesSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save matching rule.'**
  String get matchingrulesSaveFailed;

  /// Translated from matchingRules_delete_confirm
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete matching rule \'{name}\'?'**
  String matchingrulesDeleteConfirm(Object name);

  /// Translated from delete_confirm
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete matching rule \'{name}\'?'**
  String deleteConfirm(Object name);

  /// No description provided for @matchingrulesDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Matching rule deleted.'**
  String get matchingrulesDeleteSuccess;

  /// No description provided for @matchingrulesDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete matching rule.'**
  String get matchingrulesDeleteFailed;

  /// No description provided for @labelEmail.
  ///
  /// In en, this message translates to:
  /// **'Email Address'**
  String get labelEmail;

  /// No description provided for @placeholderEmail.
  ///
  /// In en, this message translates to:
  /// **'e.g., user@company.com'**
  String get placeholderEmail;

  /// No description provided for @emailDomain.
  ///
  /// In en, this message translates to:
  /// **'Email Domain'**
  String get emailDomain;

  /// No description provided for @placeholderEmailDomain.
  ///
  /// In en, this message translates to:
  /// **'e.g. company.com'**
  String get placeholderEmailDomain;

  /// No description provided for @orgEmailDomainDesc.
  ///
  /// In en, this message translates to:
  /// **'Configure the default email domain for the organization.'**
  String get orgEmailDomainDesc;

  /// No description provided for @ruleTypeBusinessNoChecksum.
  ///
  /// In en, this message translates to:
  /// **'Korean Business Registration No Checksum (10 digits)'**
  String get ruleTypeBusinessNoChecksum;

  /// No description provided for @ruleTypeCorporateNoChecksum.
  ///
  /// In en, this message translates to:
  /// **'Korean Corporate Registration No Checksum (13 digits)'**
  String get ruleTypeCorporateNoChecksum;

  /// No description provided for @channelHealth.
  ///
  /// In en, this message translates to:
  /// **'Channel Health'**
  String get channelHealth;

  /// No description provided for @channelMetrics.
  ///
  /// In en, this message translates to:
  /// **'Realtime Throughput & DLQ Metrics'**
  String get channelMetrics;

  /// No description provided for @healthHealthy.
  ///
  /// In en, this message translates to:
  /// **'HEALTHY'**
  String get healthHealthy;

  /// No description provided for @healthDegraded.
  ///
  /// In en, this message translates to:
  /// **'DEGRADED'**
  String get healthDegraded;

  /// No description provided for @healthUnhealthy.
  ///
  /// In en, this message translates to:
  /// **'UNHEALTHY'**
  String get healthUnhealthy;

  /// No description provided for @pingTest.
  ///
  /// In en, this message translates to:
  /// **'Ping Test'**
  String get pingTest;

  /// No description provided for @pingTesting.
  ///
  /// In en, this message translates to:
  /// **'Testing Ping...'**
  String get pingTesting;

  /// No description provided for @avgLatency.
  ///
  /// In en, this message translates to:
  /// **'Avg Latency'**
  String get avgLatency;

  /// No description provided for @timeSlot.
  ///
  /// In en, this message translates to:
  /// **'Time Slot'**
  String get timeSlot;

  /// No description provided for @smartMapping.
  ///
  /// In en, this message translates to:
  /// **'Smart Auto-Mapping Recommendation'**
  String get smartMapping;

  /// No description provided for @smartMappingDesc.
  ///
  /// In en, this message translates to:
  /// **'Analyze sample payload keys and suggest best domain field mappings with fuzzy text matching.'**
  String get smartMappingDesc;

  /// No description provided for @samplePayload.
  ///
  /// In en, this message translates to:
  /// **'Sample Payload (JSON)'**
  String get samplePayload;

  /// No description provided for @samplePayloadPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter sample JSON payload.'**
  String get samplePayloadPlaceholder;

  /// No description provided for @recommendMapping.
  ///
  /// In en, this message translates to:
  /// **'Run Auto-Mapping Suggestion'**
  String get recommendMapping;

  /// No description provided for @applyRecommendations.
  ///
  /// In en, this message translates to:
  /// **'Apply Suggested Mappings'**
  String get applyRecommendations;

  /// No description provided for @sourceField.
  ///
  /// In en, this message translates to:
  /// **'Source Field'**
  String get sourceField;

  /// No description provided for @targetField.
  ///
  /// In en, this message translates to:
  /// **'Target System Field'**
  String get targetField;

  /// No description provided for @confidenceScore.
  ///
  /// In en, this message translates to:
  /// **'Confidence'**
  String get confidenceScore;

  /// No description provided for @matchReason.
  ///
  /// In en, this message translates to:
  /// **'Match Reason'**
  String get matchReason;

  /// No description provided for @dlqHub.
  ///
  /// In en, this message translates to:
  /// **'Dead Letter Queue (DLQ) & Retry Hub'**
  String get dlqHub;

  /// No description provided for @dlqHubDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage and batch retry failed/dead-letter logs from external integration channels.'**
  String get dlqHubDesc;

  /// No description provided for @retrySelected.
  ///
  /// In en, this message translates to:
  /// **'Retry Selected'**
  String get retrySelected;

  /// No description provided for @retryAll.
  ///
  /// In en, this message translates to:
  /// **'Retry All Failed'**
  String get retryAll;

  /// No description provided for @retryCount.
  ///
  /// In en, this message translates to:
  /// **'Retry Count'**
  String get retryCount;

  /// No description provided for @errorMessage.
  ///
  /// In en, this message translates to:
  /// **'Error Message'**
  String get errorMessage;

  /// No description provided for @noDlqItems.
  ///
  /// In en, this message translates to:
  /// **'No failed or dead-letter logs found.'**
  String get noDlqItems;

  /// No description provided for @webhookHub.
  ///
  /// In en, this message translates to:
  /// **'Real-time Event Webhook Dispatcher'**
  String get webhookHub;

  /// No description provided for @webhookHubDesc.
  ///
  /// In en, this message translates to:
  /// **'Dispatch master record create/update/approval events to external systems in real-time.'**
  String get webhookHubDesc;

  /// No description provided for @addWebhook.
  ///
  /// In en, this message translates to:
  /// **'Add Webhook'**
  String get addWebhook;

  /// No description provided for @targetUrl.
  ///
  /// In en, this message translates to:
  /// **'Target Webhook URL'**
  String get targetUrl;

  /// No description provided for @subscribedEvents.
  ///
  /// In en, this message translates to:
  /// **'Subscribed Events'**
  String get subscribedEvents;

  /// No description provided for @testWebhook.
  ///
  /// In en, this message translates to:
  /// **'Test Webhook'**
  String get testWebhook;

  /// No description provided for @noWebhooks.
  ///
  /// In en, this message translates to:
  /// **'No webhooks registered.'**
  String get noWebhooks;

  /// No description provided for @dataRetention.
  ///
  /// In en, this message translates to:
  /// **'Data Retention & GDPR Purge Hub'**
  String get dataRetention;

  /// No description provided for @dataRetentionDesc.
  ///
  /// In en, this message translates to:
  /// **'Safely anonymizes or hard purges master records that exceeded legal retention periods or received erasure requests.'**
  String get dataRetentionDesc;

  /// No description provided for @retentionYears.
  ///
  /// In en, this message translates to:
  /// **'Retention Years'**
  String get retentionYears;

  /// No description provided for @scanExpired.
  ///
  /// In en, this message translates to:
  /// **'Scan Expired Records'**
  String get scanExpired;

  /// No description provided for @purgeType.
  ///
  /// In en, this message translates to:
  /// **'Purge Type'**
  String get purgeType;

  /// No description provided for @purgeAnonymize.
  ///
  /// In en, this message translates to:
  /// **'Soft Anonymize'**
  String get purgeAnonymize;

  /// No description provided for @purgeHardDelete.
  ///
  /// In en, this message translates to:
  /// **'Hard Delete'**
  String get purgeHardDelete;

  /// No description provided for @executePurge.
  ///
  /// In en, this message translates to:
  /// **'Execute Safe Purge'**
  String get executePurge;

  /// No description provided for @expiredCount.
  ///
  /// In en, this message translates to:
  /// **'Expired Count'**
  String get expiredCount;

  /// No description provided for @anomalyDetection.
  ///
  /// In en, this message translates to:
  /// **'Zero-Trust Anomaly Access & Exfiltration Detector'**
  String get anomalyDetection;

  /// No description provided for @anomalyDetectionDesc.
  ///
  /// In en, this message translates to:
  /// **'Real-time zero-trust detection of massive data exfiltration and unauthorized access anomalies.'**
  String get anomalyDetectionDesc;

  /// No description provided for @threatScore.
  ///
  /// In en, this message translates to:
  /// **'Security Threat Score'**
  String get threatScore;

  /// No description provided for @activeThreats.
  ///
  /// In en, this message translates to:
  /// **'Active Threats'**
  String get activeThreats;

  /// No description provided for @threatLevel.
  ///
  /// In en, this message translates to:
  /// **'Threat Level'**
  String get threatLevel;

  /// No description provided for @blockActor.
  ///
  /// In en, this message translates to:
  /// **'Block Actor'**
  String get blockActor;

  /// No description provided for @blockedBadge.
  ///
  /// In en, this message translates to:
  /// **'Blocked'**
  String get blockedBadge;

  /// No description provided for @syncPipeline.
  ///
  /// In en, this message translates to:
  /// **'Cross-Domain Data Sync Pipeline Scheduler'**
  String get syncPipeline;

  /// No description provided for @syncPipelineDesc.
  ///
  /// In en, this message translates to:
  /// **'Configure periodic synchronization rules and cron schedules across multiple domains.'**
  String get syncPipelineDesc;

  /// No description provided for @pipelineName.
  ///
  /// In en, this message translates to:
  /// **'Pipeline Name'**
  String get pipelineName;

  /// No description provided for @cronSchedule.
  ///
  /// In en, this message translates to:
  /// **'Cron Schedule'**
  String get cronSchedule;

  /// No description provided for @lastSynced.
  ///
  /// In en, this message translates to:
  /// **'Last Synced'**
  String get lastSynced;

  /// No description provided for @triggerPipeline.
  ///
  /// In en, this message translates to:
  /// **'Trigger Now'**
  String get triggerPipeline;

  /// No description provided for @noPipelines.
  ///
  /// In en, this message translates to:
  /// **'No sync pipelines configured.'**
  String get noPipelines;

  /// No description provided for @apiKeyMgmt.
  ///
  /// In en, this message translates to:
  /// **'Integration API Key & Scoped Access Manager'**
  String get apiKeyMgmt;

  /// No description provided for @apiKeyMgmtDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage API keys, expiration, IP whitelists, and granular permission scopes for external integration channels.'**
  String get apiKeyMgmtDesc;

  /// No description provided for @issueApiKey.
  ///
  /// In en, this message translates to:
  /// **'Issue New API Key'**
  String get issueApiKey;

  /// No description provided for @keyName.
  ///
  /// In en, this message translates to:
  /// **'Key Name'**
  String get keyName;

  /// No description provided for @validDays.
  ///
  /// In en, this message translates to:
  /// **'Validity (Days)'**
  String get validDays;

  /// No description provided for @allowedIps.
  ///
  /// In en, this message translates to:
  /// **'Allowed IPs (CIDR)'**
  String get allowedIps;

  /// No description provided for @permissionScopes.
  ///
  /// In en, this message translates to:
  /// **'Permission Scopes'**
  String get permissionScopes;

  /// No description provided for @revokeKey.
  ///
  /// In en, this message translates to:
  /// **'Revoke Key'**
  String get revokeKey;

  /// No description provided for @confirmRevokeKey.
  ///
  /// In en, this message translates to:
  /// **'Revoke this API key permanently? Channel calls using this key will be blocked immediately.'**
  String get confirmRevokeKey;

  /// No description provided for @systemDiagnostics.
  ///
  /// In en, this message translates to:
  /// **'Global System Health Diagnostics & Dependency Monitor'**
  String get systemDiagnostics;

  /// No description provided for @systemDiagnosticsDesc.
  ///
  /// In en, this message translates to:
  /// **'Monitor real-time latency and status across DB, cache, message brokers, and storage backbones.'**
  String get systemDiagnosticsDesc;

  /// No description provided for @componentName.
  ///
  /// In en, this message translates to:
  /// **'Component Name'**
  String get componentName;

  /// No description provided for @latency.
  ///
  /// In en, this message translates to:
  /// **'Latency (ms)'**
  String get latency;

  /// No description provided for @averageLatency.
  ///
  /// In en, this message translates to:
  /// **'Average Latency'**
  String get averageLatency;

  /// No description provided for @overallStatus.
  ///
  /// In en, this message translates to:
  /// **'Overall Status'**
  String get overallStatus;

  /// No description provided for @runDiagnostics.
  ///
  /// In en, this message translates to:
  /// **'Refresh Diagnostics'**
  String get runDiagnostics;

  /// No description provided for @workspaceWidgets.
  ///
  /// In en, this message translates to:
  /// **'Governance Dashboard Widget Customizer'**
  String get workspaceWidgets;

  /// No description provided for @workspaceWidgetsDesc.
  ///
  /// In en, this message translates to:
  /// **'Personalize your workspace dashboard by customizing DQ, approval, security, and integration widgets.'**
  String get workspaceWidgetsDesc;

  /// No description provided for @widgetGallery.
  ///
  /// In en, this message translates to:
  /// **'Widget Gallery'**
  String get widgetGallery;

  /// No description provided for @widgetEnabled.
  ///
  /// In en, this message translates to:
  /// **'Enabled'**
  String get widgetEnabled;

  /// No description provided for @widgetDisabled.
  ///
  /// In en, this message translates to:
  /// **'Disabled'**
  String get widgetDisabled;

  /// No description provided for @saveLayout.
  ///
  /// In en, this message translates to:
  /// **'Save Layout'**
  String get saveLayout;

  /// No description provided for @coldStorage.
  ///
  /// In en, this message translates to:
  /// **'Cold-Storage Archiver'**
  String get coldStorage;

  /// No description provided for @coldStorageDesc.
  ///
  /// In en, this message translates to:
  /// **'Freeze full enterprise master data into encrypted cold-storage archive packages and simulate disaster recovery integrity.'**
  String get coldStorageDesc;

  /// No description provided for @createArchive.
  ///
  /// In en, this message translates to:
  /// **'Create Frozen Archive'**
  String get createArchive;

  /// No description provided for @archiveName.
  ///
  /// In en, this message translates to:
  /// **'Archive Name'**
  String get archiveName;

  /// No description provided for @checksumSha256.
  ///
  /// In en, this message translates to:
  /// **'Checksum (SHA-256)'**
  String get checksumSha256;

  /// No description provided for @compressionRatio.
  ///
  /// In en, this message translates to:
  /// **'Compression & Encryption'**
  String get compressionRatio;

  /// No description provided for @simulateDr.
  ///
  /// In en, this message translates to:
  /// **'Simulate DR Restore'**
  String get simulateDr;

  /// No description provided for @regulatoryCompliance.
  ///
  /// In en, this message translates to:
  /// **'Regulatory Compliance'**
  String get regulatoryCompliance;

  /// No description provided for @regulatoryComplianceDesc.
  ///
  /// In en, this message translates to:
  /// **'Audits system encryption, audit ledgers, and purge engines against ISMS-P, PIPA, and GDPR regulations.'**
  String get regulatoryComplianceDesc;

  /// No description provided for @complianceScore.
  ///
  /// In en, this message translates to:
  /// **'Compliance Score'**
  String get complianceScore;

  /// No description provided for @certificationReadiness.
  ///
  /// In en, this message translates to:
  /// **'Certification Readiness'**
  String get certificationReadiness;

  /// No description provided for @controlCode.
  ///
  /// In en, this message translates to:
  /// **'Control Code'**
  String get controlCode;

  /// No description provided for @evidence.
  ///
  /// In en, this message translates to:
  /// **'Audit Evidence'**
  String get evidence;

  /// No description provided for @runAudit.
  ///
  /// In en, this message translates to:
  /// **'Run Audit'**
  String get runAudit;

  /// No description provided for @volumeRadar.
  ///
  /// In en, this message translates to:
  /// **'Volume Radar'**
  String get volumeRadar;

  /// No description provided for @volumeRadarDesc.
  ///
  /// In en, this message translates to:
  /// **'Monitors real-time record changes and API traffic with Z-score anomaly models to detect traffic spikes.'**
  String get volumeRadarDesc;

  /// No description provided for @currentThroughput.
  ///
  /// In en, this message translates to:
  /// **'Current Throughput'**
  String get currentThroughput;

  /// No description provided for @baselineThroughput.
  ///
  /// In en, this message translates to:
  /// **'Baseline Throughput'**
  String get baselineThroughput;

  /// No description provided for @volumeHistory.
  ///
  /// In en, this message translates to:
  /// **'Volume History'**
  String get volumeHistory;

  /// No description provided for @spikeAlert.
  ///
  /// In en, this message translates to:
  /// **'Spike Alert'**
  String get spikeAlert;

  /// No description provided for @normalTraffic.
  ///
  /// In en, this message translates to:
  /// **'Normal Traffic'**
  String get normalTraffic;

  /// No description provided for @governanceMaturity.
  ///
  /// In en, this message translates to:
  /// **'Governance Maturity'**
  String get governanceMaturity;

  /// No description provided for @governanceMaturityDesc.
  ///
  /// In en, this message translates to:
  /// **'Evaluates enterprise data quality KPIs and governance maturity levels (Level 1~5) based on DMM/CMMI models.'**
  String get governanceMaturityDesc;

  /// No description provided for @overallMaturityLevel.
  ///
  /// In en, this message translates to:
  /// **'Overall Governance Maturity Level'**
  String get overallMaturityLevel;

  /// No description provided for @maturityDimensions.
  ///
  /// In en, this message translates to:
  /// **'5 Maturity Dimensions'**
  String get maturityDimensions;

  /// No description provided for @completenessKpi.
  ///
  /// In en, this message translates to:
  /// **'Completeness'**
  String get completenessKpi;

  /// No description provided for @timelinessKpi.
  ///
  /// In en, this message translates to:
  /// **'Timeliness'**
  String get timelinessKpi;

  /// No description provided for @consistencyKpi.
  ///
  /// In en, this message translates to:
  /// **'Consistency'**
  String get consistencyKpi;

  /// No description provided for @validityKpi.
  ///
  /// In en, this message translates to:
  /// **'Validity'**
  String get validityKpi;

  /// No description provided for @multiTenant.
  ///
  /// In en, this message translates to:
  /// **'Multi-Tenant Router'**
  String get multiTenant;

  /// No description provided for @multiTenantDesc.
  ///
  /// In en, this message translates to:
  /// **'Manages row/column level data isolation and virtual routing policies across HQ, branches, and subsidiaries.'**
  String get multiTenantDesc;

  /// No description provided for @tenantName.
  ///
  /// In en, this message translates to:
  /// **'Tenant Name'**
  String get tenantName;

  /// No description provided for @partitionType.
  ///
  /// In en, this message translates to:
  /// **'Partition Type'**
  String get partitionType;

  /// No description provided for @routingExpression.
  ///
  /// In en, this message translates to:
  /// **'Routing Expression'**
  String get routingExpression;

  /// No description provided for @targetDomains.
  ///
  /// In en, this message translates to:
  /// **'Target Domains'**
  String get targetDomains;

  /// No description provided for @dataSla.
  ///
  /// In en, this message translates to:
  /// **'Data SLA Tracker'**
  String get dataSla;

  /// No description provided for @dataSlaDesc.
  ///
  /// In en, this message translates to:
  /// **'Tracks real-time latency, availability, and DQ compliance contracts across domains and integration channels.'**
  String get dataSlaDesc;

  /// No description provided for @slaContractName.
  ///
  /// In en, this message translates to:
  /// **'SLA Contract / Target'**
  String get slaContractName;

  /// No description provided for @latencySla.
  ///
  /// In en, this message translates to:
  /// **'Latency'**
  String get latencySla;

  /// No description provided for @availabilitySla.
  ///
  /// In en, this message translates to:
  /// **'Availability'**
  String get availabilitySla;

  /// No description provided for @qualitySla.
  ///
  /// In en, this message translates to:
  /// **'DQ Compliance'**
  String get qualitySla;

  /// No description provided for @meetingSla.
  ///
  /// In en, this message translates to:
  /// **'Meeting SLA'**
  String get meetingSla;

  /// No description provided for @masterOrchestrator.
  ///
  /// In en, this message translates to:
  /// **'Master Orchestrator'**
  String get masterOrchestrator;

  /// No description provided for @masterOrchestratorDesc.
  ///
  /// In en, this message translates to:
  /// **'Orchestrates and monitors the health status and lifecycle of all 50 enterprise master data governance features.'**
  String get masterOrchestratorDesc;

  /// No description provided for @totalFeaturesCount.
  ///
  /// In en, this message translates to:
  /// **'Total Features'**
  String get totalFeaturesCount;

  /// No description provided for @healthyFeaturesCount.
  ///
  /// In en, this message translates to:
  /// **'Healthy Features'**
  String get healthyFeaturesCount;

  /// No description provided for @featureNo.
  ///
  /// In en, this message translates to:
  /// **'No.'**
  String get featureNo;

  /// No description provided for @featureName.
  ///
  /// In en, this message translates to:
  /// **'Feature Name'**
  String get featureName;

  /// No description provided for @featureCategory.
  ///
  /// In en, this message translates to:
  /// **'Category'**
  String get featureCategory;

  /// No description provided for @pipelineSelfHealing.
  ///
  /// In en, this message translates to:
  /// **'Pipeline Self-Healing'**
  String get pipelineSelfHealing;

  /// No description provided for @pipelineSelfHealingDesc.
  ///
  /// In en, this message translates to:
  /// **'AI agent diagnoses schema mismatches, network delays, and format corruptions to autonomously heal and reroute pipelines.'**
  String get pipelineSelfHealingDesc;

  /// No description provided for @healingActionId.
  ///
  /// In en, this message translates to:
  /// **'Action ID'**
  String get healingActionId;

  /// No description provided for @diagnosedCause.
  ///
  /// In en, this message translates to:
  /// **'Diagnosed Cause'**
  String get diagnosedCause;

  /// No description provided for @healingStrategy.
  ///
  /// In en, this message translates to:
  /// **'Healing Strategy'**
  String get healingStrategy;

  /// No description provided for @recoveredRecords.
  ///
  /// In en, this message translates to:
  /// **'Recovered Records'**
  String get recoveredRecords;

  /// No description provided for @triggerHealing.
  ///
  /// In en, this message translates to:
  /// **'Trigger Self-Healing'**
  String get triggerHealing;

  /// No description provided for @healingTriggered.
  ///
  /// In en, this message translates to:
  /// **'Autonomous healing triggered successfully.'**
  String get healingTriggered;

  /// No description provided for @freshnessHeatmap.
  ///
  /// In en, this message translates to:
  /// **'Freshness Heatmap'**
  String get freshnessHeatmap;

  /// No description provided for @freshnessHeatmapDesc.
  ///
  /// In en, this message translates to:
  /// **'Monitors last update timestamps and real-time latency across domains to prevent stale data.'**
  String get freshnessHeatmapDesc;

  /// No description provided for @freshnessScore.
  ///
  /// In en, this message translates to:
  /// **'Freshness Score'**
  String get freshnessScore;

  /// No description provided for @lastUpdatedTime.
  ///
  /// In en, this message translates to:
  /// **'Last Updated'**
  String get lastUpdatedTime;

  /// No description provided for @delayMinutes.
  ///
  /// In en, this message translates to:
  /// **'Delay Minutes'**
  String get delayMinutes;

  /// No description provided for @freshStatus.
  ///
  /// In en, this message translates to:
  /// **'Fresh'**
  String get freshStatus;

  /// No description provided for @multiRegionConflict.
  ///
  /// In en, this message translates to:
  /// **'Conflict Auto-Resolver'**
  String get multiRegionConflict;

  /// No description provided for @multiRegionConflictDesc.
  ///
  /// In en, this message translates to:
  /// **'Autonomously resolves concurrent update conflicts across global regions using vector clocks and priority rules.'**
  String get multiRegionConflictDesc;

  /// No description provided for @regionPair.
  ///
  /// In en, this message translates to:
  /// **'Region Pair'**
  String get regionPair;

  /// No description provided for @resolutionStrategy.
  ///
  /// In en, this message translates to:
  /// **'Strategy'**
  String get resolutionStrategy;

  /// No description provided for @resolvedValue.
  ///
  /// In en, this message translates to:
  /// **'Resolved Value'**
  String get resolvedValue;

  /// No description provided for @autoResolved.
  ///
  /// In en, this message translates to:
  /// **'Auto-Resolved'**
  String get autoResolved;

  /// No description provided for @governanceCopilot.
  ///
  /// In en, this message translates to:
  /// **'Governance AI Copilot'**
  String get governanceCopilot;

  /// No description provided for @governanceCopilotDesc.
  ///
  /// In en, this message translates to:
  /// **'Interactive AI copilot for querying quality metrics, SLA risks, multi-region sync, and autonomous healing.'**
  String get governanceCopilotDesc;

  /// No description provided for @copilotPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Ask anything about data governance (e.g. Summarize DQ, Check SLA status...)'**
  String get copilotPlaceholder;

  /// No description provided for @copilotSend.
  ///
  /// In en, this message translates to:
  /// **'Send'**
  String get copilotSend;

  /// No description provided for @quickQuestions.
  ///
  /// In en, this message translates to:
  /// **'Quick Prompts'**
  String get quickQuestions;

  /// No description provided for @alldone.
  ///
  /// In en, this message translates to:
  /// **'All approvals/consensus are completed.'**
  String get alldone;

  /// No description provided for @noparsable.
  ///
  /// In en, this message translates to:
  /// **'No parsable data provided.'**
  String get noparsable;

  /// No description provided for @addcomment.
  ///
  /// In en, this message translates to:
  /// **'Add Comment'**
  String get addcomment;

  /// No description provided for @actionTitle.
  ///
  /// In en, this message translates to:
  /// **'Process Request'**
  String get actionTitle;

  /// Translated from bulk_approve
  ///
  /// In en, this message translates to:
  /// **'Bulk Approve'**
  String bulkApprove(Object count);

  /// Translated from bulk_reject
  ///
  /// In en, this message translates to:
  /// **'Bulk Reject'**
  String bulkReject(Object count);

  /// Translated from bulk_approve_confirm
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to approve the selected {count} requests?'**
  String bulkApproveConfirm(Object count);

  /// Translated from bulk_reject_confirm
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to reject the selected {count} requests?'**
  String bulkRejectConfirm(Object count);

  /// No description provided for @bulkApproveLoading.
  ///
  /// In en, this message translates to:
  /// **'Bulk approving...'**
  String get bulkApproveLoading;

  /// No description provided for @bulkRejectLoading.
  ///
  /// In en, this message translates to:
  /// **'Bulk rejecting...'**
  String get bulkRejectLoading;

  /// Translated from processing
  ///
  /// In en, this message translates to:
  /// **'Processing Data... {percent}%'**
  String processing(Object percent);

  /// No description provided for @approvalLine.
  ///
  /// In en, this message translates to:
  /// **'Approval Route'**
  String get approvalLine;

  /// No description provided for @targetType.
  ///
  /// In en, this message translates to:
  /// **'Target Type'**
  String get targetType;

  /// No description provided for @stepType.
  ///
  /// In en, this message translates to:
  /// **'Step Type'**
  String get stepType;

  /// No description provided for @action.
  ///
  /// In en, this message translates to:
  /// **'Action'**
  String get action;

  /// No description provided for @statusDraft.
  ///
  /// In en, this message translates to:
  /// **'Draft'**
  String get statusDraft;

  /// No description provided for @statusPending.
  ///
  /// In en, this message translates to:
  /// **'Pending Review'**
  String get statusPending;

  /// No description provided for @statusWaiting.
  ///
  /// In en, this message translates to:
  /// **'Waiting'**
  String get statusWaiting;

  /// No description provided for @recordCreate.
  ///
  /// In en, this message translates to:
  /// **'New Record Create'**
  String get recordCreate;

  /// No description provided for @recordUpdate.
  ///
  /// In en, this message translates to:
  /// **'Record Update'**
  String get recordUpdate;

  /// No description provided for @recordDelete.
  ///
  /// In en, this message translates to:
  /// **'Record Delete'**
  String get recordDelete;

  /// No description provided for @domainRecordCreate.
  ///
  /// In en, this message translates to:
  /// **'Domain Record Create'**
  String get domainRecordCreate;

  /// No description provided for @targetTypeMEMO.
  ///
  /// In en, this message translates to:
  /// **'Memo Approval'**
  String get targetTypeMEMO;

  /// No description provided for @targetTypeMemo.
  ///
  /// In en, this message translates to:
  /// **'Memo Approval'**
  String get targetTypeMemo;

  /// No description provided for @memoApproval.
  ///
  /// In en, this message translates to:
  /// **'Memo Approval'**
  String get memoApproval;

  /// No description provided for @noComment.
  ///
  /// In en, this message translates to:
  /// **'No Comment'**
  String get noComment;

  /// No description provided for @cancelReason.
  ///
  /// In en, this message translates to:
  /// **'Cancellation Reason'**
  String get cancelReason;

  /// No description provided for @rejectionReason.
  ///
  /// In en, this message translates to:
  /// **'Rejection Reason'**
  String get rejectionReason;

  /// No description provided for @cancellationNotice.
  ///
  /// In en, this message translates to:
  /// **'This approval request has been cancelled by the drafter.'**
  String get cancellationNotice;

  /// No description provided for @statusCancelled.
  ///
  /// In en, this message translates to:
  /// **'Cancelled'**
  String get statusCancelled;

  /// No description provided for @noReasonSpecified.
  ///
  /// In en, this message translates to:
  /// **'No reason specified.'**
  String get noReasonSpecified;

  /// No description provided for @observers.
  ///
  /// In en, this message translates to:
  /// **'Observers (CC)'**
  String get observers;

  /// No description provided for @stepscheduled.
  ///
  /// In en, this message translates to:
  /// **'Scheduled'**
  String get stepscheduled;

  /// No description provided for @coldomain.
  ///
  /// In en, this message translates to:
  /// **'Domain'**
  String get coldomain;

  /// No description provided for @colclassification.
  ///
  /// In en, this message translates to:
  /// **'Classification'**
  String get colclassification;

  /// No description provided for @colidattr.
  ///
  /// In en, this message translates to:
  /// **'ID Attribute'**
  String get colidattr;

  /// No description provided for @colnameattr.
  ///
  /// In en, this message translates to:
  /// **'Name Attribute'**
  String get colnameattr;

  /// No description provided for @colsummary.
  ///
  /// In en, this message translates to:
  /// **'Summary'**
  String get colsummary;

  /// No description provided for @actionApprove.
  ///
  /// In en, this message translates to:
  /// **'Approved'**
  String get actionApprove;

  /// No description provided for @actionReject.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get actionReject;

  /// Translated from action_processing
  ///
  /// In en, this message translates to:
  /// **'Processing {action}...'**
  String actionProcessing(Object action);

  /// Translated from action_success
  ///
  /// In en, this message translates to:
  /// **'Request has been successfully {action}.'**
  String actionSuccess(Object action);

  /// No description provided for @review.
  ///
  /// In en, this message translates to:
  /// **'Review'**
  String get review;

  /// No description provided for @created.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get created;

  /// No description provided for @close.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get close;

  /// No description provided for @general.
  ///
  /// In en, this message translates to:
  /// **'General'**
  String get general;

  /// No description provided for @fields.
  ///
  /// In en, this message translates to:
  /// **'Fields'**
  String get fields;

  /// No description provided for @summary.
  ///
  /// In en, this message translates to:
  /// **'Summary'**
  String get summary;

  /// No description provided for @details.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get details;

  /// No description provided for @id.
  ///
  /// In en, this message translates to:
  /// **'ID'**
  String get id;

  /// No description provided for @targetTypeApprovalRequest.
  ///
  /// In en, this message translates to:
  /// **'Approval Request'**
  String get targetTypeApprovalRequest;

  /// No description provided for @confirmAndSubmit.
  ///
  /// In en, this message translates to:
  /// **'Confirm Impact & Submit Request'**
  String get confirmAndSubmit;

  /// No description provided for @confirmSafetyApply.
  ///
  /// In en, this message translates to:
  /// **'Confirm Safety & Apply Changes'**
  String get confirmSafetyApply;

  /// No description provided for @confirmSafetySubmit.
  ///
  /// In en, this message translates to:
  /// **'Confirm Safety & Submit Request'**
  String get confirmSafetySubmit;

  /// No description provided for @approvalSubmittedTitle.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Submitted'**
  String get approvalSubmittedTitle;

  /// No description provided for @confirmRiskApply.
  ///
  /// In en, this message translates to:
  /// **'Confirm Risk & Apply Changes'**
  String get confirmRiskApply;

  /// No description provided for @confirmRiskDesc.
  ///
  /// In en, this message translates to:
  /// **'Confirmed risk levels and warnings. Approve and apply schema changes.'**
  String get confirmRiskDesc;

  /// No description provided for @addApprovalStep.
  ///
  /// In en, this message translates to:
  /// **'+ Add Approval Step'**
  String get addApprovalStep;

  /// No description provided for @approval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get approval;

  /// No description provided for @approvallinestatus.
  ///
  /// In en, this message translates to:
  /// **'Approval Line Status'**
  String get approvallinestatus;

  /// No description provided for @approvallinesummary.
  ///
  /// In en, this message translates to:
  /// **'Approval Line (Summary):'**
  String get approvallinesummary;

  /// No description provided for @approvalHistory.
  ///
  /// In en, this message translates to:
  /// **'Approval History'**
  String get approvalHistory;

  /// No description provided for @approvalHistoryBtn.
  ///
  /// In en, this message translates to:
  /// **'Approval History'**
  String get approvalHistoryBtn;

  /// No description provided for @approvalHistoryDetail.
  ///
  /// In en, this message translates to:
  /// **'Approval History Detail'**
  String get approvalHistoryDetail;

  /// No description provided for @approvalInProgress.
  ///
  /// In en, this message translates to:
  /// **'Approval in Progress'**
  String get approvalInProgress;

  /// No description provided for @approvalLineStatus.
  ///
  /// In en, this message translates to:
  /// **'Approval Line Status'**
  String get approvalLineStatus;

  /// No description provided for @approvalLineSummary.
  ///
  /// In en, this message translates to:
  /// **'Approval Line (Summary)'**
  String get approvalLineSummary;

  /// No description provided for @approvalLineTitle.
  ///
  /// In en, this message translates to:
  /// **'Multi-Step Approval Line'**
  String get approvalLineTitle;

  /// No description provided for @approvalMonitor.
  ///
  /// In en, this message translates to:
  /// **'Approval Monitor'**
  String get approvalMonitor;

  /// No description provided for @approvalMonitoring.
  ///
  /// In en, this message translates to:
  /// **'Approval Monitoring'**
  String get approvalMonitoring;

  /// No description provided for @approvalProgressStep.
  ///
  /// In en, this message translates to:
  /// **'Approval Progress Step'**
  String get approvalProgressStep;

  /// No description provided for @approvalReview.
  ///
  /// In en, this message translates to:
  /// **'Approval Review'**
  String get approvalReview;

  /// Translated from approval_stats_summary
  ///
  /// In en, this message translates to:
  /// **'Approved {approved} / Rejected {rejected}'**
  String approvalStatsSummary(Object approved, Object rejected);

  /// No description provided for @approvalStepsCol.
  ///
  /// In en, this message translates to:
  /// **'Approval Steps'**
  String get approvalStepsCol;

  /// No description provided for @approvalSuccessRate.
  ///
  /// In en, this message translates to:
  /// **'Approval Success Rate'**
  String get approvalSuccessRate;

  /// No description provided for @approvalTrendTitle.
  ///
  /// In en, this message translates to:
  /// **'7-Day Approval Requests Trend'**
  String get approvalTrendTitle;

  /// No description provided for @approvals.
  ///
  /// In en, this message translates to:
  /// **'Approvals'**
  String get approvals;

  /// No description provided for @approvalsTitle.
  ///
  /// In en, this message translates to:
  /// **'Approvals & Inbox'**
  String get approvalsTitle;

  /// No description provided for @approve.
  ///
  /// In en, this message translates to:
  /// **'Approve'**
  String get approve;

  /// No description provided for @approver.
  ///
  /// In en, this message translates to:
  /// **'Approver'**
  String get approver;

  /// No description provided for @btnSubmit.
  ///
  /// In en, this message translates to:
  /// **'Submit'**
  String get btnSubmit;

  /// No description provided for @cancelRequest.
  ///
  /// In en, this message translates to:
  /// **'Cancel Request'**
  String get cancelRequest;

  /// Translated from confirm_batch_approve
  ///
  /// In en, this message translates to:
  /// **'Batch approve selected {count} items?'**
  String confirmBatchApprove(Object count);

  /// Translated from confirm_batch_reject
  ///
  /// In en, this message translates to:
  /// **'Batch reject selected {count} items?'**
  String confirmBatchReject(Object count);

  /// No description provided for @confirmDelete.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete?'**
  String get confirmDelete;

  /// Translated from delete_workflow_confirm
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete workflow \'{name}\'?'**
  String deleteWorkflowConfirm(Object name);

  /// No description provided for @editDisabledApproval.
  ///
  /// In en, this message translates to:
  /// **'⚠️ This record cannot be edited while an approval is pending.'**
  String get editDisabledApproval;

  /// No description provided for @finalapproval.
  ///
  /// In en, this message translates to:
  /// **'Final Approval'**
  String get finalapproval;

  /// No description provided for @installBtnSubmit.
  ///
  /// In en, this message translates to:
  /// **'Complete Installation & Register Admin'**
  String get installBtnSubmit;

  /// No description provided for @labelConfirmPassword.
  ///
  /// In en, this message translates to:
  /// **'Confirm Password'**
  String get labelConfirmPassword;

  /// No description provided for @mysubmitted.
  ///
  /// In en, this message translates to:
  /// **'My Submitted Requests'**
  String get mysubmitted;

  /// No description provided for @mySubmittedRequests.
  ///
  /// In en, this message translates to:
  /// **'My Submitted Requests'**
  String get mySubmittedRequests;

  /// No description provided for @noapprovalline.
  ///
  /// In en, this message translates to:
  /// **'No approval line.'**
  String get noapprovalline;

  /// No description provided for @norequests.
  ///
  /// In en, this message translates to:
  /// **'There are no pending requests.'**
  String get norequests;

  /// No description provided for @nosubmitted.
  ///
  /// In en, this message translates to:
  /// **'No submitted requests.'**
  String get nosubmitted;

  /// No description provided for @noApprovalLine.
  ///
  /// In en, this message translates to:
  /// **'No Approval Line'**
  String get noApprovalLine;

  /// No description provided for @noApprovalSteps.
  ///
  /// In en, this message translates to:
  /// **'No approval steps defined (Auto-approved).'**
  String get noApprovalSteps;

  /// No description provided for @noPendingRequests.
  ///
  /// In en, this message translates to:
  /// **'No pending requests.'**
  String get noPendingRequests;

  /// No description provided for @noRequestsSubmittedYet.
  ///
  /// In en, this message translates to:
  /// **'No requests submitted yet.'**
  String get noRequestsSubmittedYet;

  /// No description provided for @pendingapprovals.
  ///
  /// In en, this message translates to:
  /// **'Pending Approvals'**
  String get pendingapprovals;

  /// No description provided for @pendingApproval.
  ///
  /// In en, this message translates to:
  /// **'Pending Approval'**
  String get pendingApproval;

  /// No description provided for @pendingApprovalAssignee.
  ///
  /// In en, this message translates to:
  /// **'Pending Approver:'**
  String get pendingApprovalAssignee;

  /// No description provided for @pendingApprovalNotice.
  ///
  /// In en, this message translates to:
  /// **'⚠️ This record is currently under approval and cannot be modified.'**
  String get pendingApprovalNotice;

  /// No description provided for @pendingApprovals.
  ///
  /// In en, this message translates to:
  /// **'Pending Approvals'**
  String get pendingApprovals;

  /// No description provided for @pendingRequests.
  ///
  /// In en, this message translates to:
  /// **'Pending Requests'**
  String get pendingRequests;

  /// No description provided for @placeholderConfirmPassword.
  ///
  /// In en, this message translates to:
  /// **'Re-enter your password'**
  String get placeholderConfirmPassword;

  /// No description provided for @reject.
  ///
  /// In en, this message translates to:
  /// **'Reject'**
  String get reject;

  /// No description provided for @requestDate.
  ///
  /// In en, this message translates to:
  /// **'Request Date'**
  String get requestDate;

  /// No description provided for @requestInfo.
  ///
  /// In en, this message translates to:
  /// **'Request Info'**
  String get requestInfo;

  /// No description provided for @requesteddata.
  ///
  /// In en, this message translates to:
  /// **'Requested Data'**
  String get requesteddata;

  /// No description provided for @requestedAccessTo.
  ///
  /// In en, this message translates to:
  /// **'requested access to'**
  String get requestedAccessTo;

  /// No description provided for @requester.
  ///
  /// In en, this message translates to:
  /// **'Requester'**
  String get requester;

  /// No description provided for @selectApproval.
  ///
  /// In en, this message translates to:
  /// **'Select Approval'**
  String get selectApproval;

  /// No description provided for @selectRejection.
  ///
  /// In en, this message translates to:
  /// **'Select Rejection'**
  String get selectRejection;

  /// No description provided for @statusApproved.
  ///
  /// In en, this message translates to:
  /// **'Approved'**
  String get statusApproved;

  /// No description provided for @statusRejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get statusRejected;

  /// No description provided for @stepapproved.
  ///
  /// In en, this message translates to:
  /// **'Approved'**
  String get stepapproved;

  /// No description provided for @steprejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get steprejected;

  /// No description provided for @stepApproval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get stepApproval;

  /// No description provided for @stepTypeApproval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get stepTypeApproval;

  /// No description provided for @submitRequest.
  ///
  /// In en, this message translates to:
  /// **'Submit Request'**
  String get submitRequest;

  /// No description provided for @typeapproval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get typeapproval;

  /// No description provided for @viewApprovalHistory.
  ///
  /// In en, this message translates to:
  /// **'View Approval History'**
  String get viewApprovalHistory;

  /// No description provided for @approvalInbox.
  ///
  /// In en, this message translates to:
  /// **'Approval Inbox'**
  String get approvalInbox;

  /// Translated from pending_count
  ///
  /// In en, this message translates to:
  /// **'Pending: {count}'**
  String pendingCount(Object count);

  /// Translated from item_count
  ///
  /// In en, this message translates to:
  /// **'{count} items'**
  String itemCount(Object count);

  /// No description provided for @consensus.
  ///
  /// In en, this message translates to:
  /// **'Consensus'**
  String get consensus;

  /// No description provided for @draft.
  ///
  /// In en, this message translates to:
  /// **'Draft'**
  String get draft;

  /// No description provided for @draftCompleted.
  ///
  /// In en, this message translates to:
  /// **'Draft Completed'**
  String get draftCompleted;

  /// No description provided for @processed.
  ///
  /// In en, this message translates to:
  /// **'Processed'**
  String get processed;

  /// No description provided for @observersList.
  ///
  /// In en, this message translates to:
  /// **'Observers List'**
  String get observersList;

  /// No description provided for @approvalDelegation.
  ///
  /// In en, this message translates to:
  /// **'Approval Delegation'**
  String get approvalDelegation;

  /// No description provided for @approvalDelegationDesc.
  ///
  /// In en, this message translates to:
  /// **'Designate a proxy approver during your absence (vacation, business trip, etc.).'**
  String get approvalDelegationDesc;

  /// No description provided for @delegatedByMe.
  ///
  /// In en, this message translates to:
  /// **'Delegated by Me'**
  String get delegatedByMe;

  /// No description provided for @delegatedToMe.
  ///
  /// In en, this message translates to:
  /// **'Delegated to Me'**
  String get delegatedToMe;

  /// No description provided for @delegatee.
  ///
  /// In en, this message translates to:
  /// **'Proxy Approver'**
  String get delegatee;

  /// No description provided for @delegator.
  ///
  /// In en, this message translates to:
  /// **'Delegator'**
  String get delegator;

  /// No description provided for @delegationPeriod.
  ///
  /// In en, this message translates to:
  /// **'Delegation Period'**
  String get delegationPeriod;

  /// No description provided for @delegationReason.
  ///
  /// In en, this message translates to:
  /// **'Delegation Reason'**
  String get delegationReason;

  /// No description provided for @addDelegation.
  ///
  /// In en, this message translates to:
  /// **'Add Delegation'**
  String get addDelegation;

  /// No description provided for @revokeDelegation.
  ///
  /// In en, this message translates to:
  /// **'Revoke Delegation'**
  String get revokeDelegation;

  /// No description provided for @delegationActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get delegationActive;

  /// No description provided for @delegationExpired.
  ///
  /// In en, this message translates to:
  /// **'Expired'**
  String get delegationExpired;

  /// Translated from proxy_badge
  ///
  /// In en, this message translates to:
  /// **'Proxy (for {name})'**
  String proxyBadge(Object name);

  /// No description provided for @delegationSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval delegation registered successfully.'**
  String get delegationSuccess;

  /// No description provided for @revokeSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval delegation revoked.'**
  String get revokeSuccess;

  /// Translated from sla_due
  ///
  /// In en, this message translates to:
  /// **'SLA Due: {time}'**
  String slaDue(Object time);

  /// No description provided for @slaExpired.
  ///
  /// In en, this message translates to:
  /// **'SLA Expired'**
  String get slaExpired;

  /// Translated from sla_escalated_badge
  ///
  /// In en, this message translates to:
  /// **'Escalated (Original: {name})'**
  String slaEscalatedBadge(Object name);

  /// No description provided for @scanEscalation.
  ///
  /// In en, this message translates to:
  /// **'Run SLA Escalation Scan'**
  String get scanEscalation;

  /// Translated from escalation_success
  ///
  /// In en, this message translates to:
  /// **'Successfully escalated {count} overdue approval steps to admins.'**
  String escalationSuccess(Object count);

  /// No description provided for @approvalSandbox.
  ///
  /// In en, this message translates to:
  /// **'Pre-Approval Data Simulation Sandbox'**
  String get approvalSandbox;

  /// No description provided for @approvalSandboxDesc.
  ///
  /// In en, this message translates to:
  /// **'Simulate and preview Before/After master data changes before final approval decision.'**
  String get approvalSandboxDesc;

  /// No description provided for @previewDiff.
  ///
  /// In en, this message translates to:
  /// **'Pre-Approval Simulation'**
  String get previewDiff;

  /// No description provided for @simulatedResult.
  ///
  /// In en, this message translates to:
  /// **'Simulated Post-Approval Data'**
  String get simulatedResult;

  /// No description provided for @dynamicRouting.
  ///
  /// In en, this message translates to:
  /// **'Dynamic Approval Routing & Workflow Templates'**
  String get dynamicRouting;

  /// No description provided for @dynamicRoutingDesc.
  ///
  /// In en, this message translates to:
  /// **'Automatically branch and assign approval stages based on field conditions and data sensitivity.'**
  String get dynamicRoutingDesc;

  /// No description provided for @templateName.
  ///
  /// In en, this message translates to:
  /// **'Template Name'**
  String get templateName;

  /// No description provided for @conditionField.
  ///
  /// In en, this message translates to:
  /// **'Condition Field'**
  String get conditionField;

  /// No description provided for @conditionOperator.
  ///
  /// In en, this message translates to:
  /// **'Operator'**
  String get conditionOperator;

  /// No description provided for @conditionValue.
  ///
  /// In en, this message translates to:
  /// **'Value'**
  String get conditionValue;

  /// No description provided for @approvalSteps.
  ///
  /// In en, this message translates to:
  /// **'Approval Steps'**
  String get approvalSteps;

  /// No description provided for @addTemplate.
  ///
  /// In en, this message translates to:
  /// **'Add Routing Template'**
  String get addTemplate;

  /// No description provided for @rejectionAnalytics.
  ///
  /// In en, this message translates to:
  /// **'Approval Rejection Reason Analytics & Resubmit Guide'**
  String get rejectionAnalytics;

  /// No description provided for @rejectionAnalyticsDesc.
  ///
  /// In en, this message translates to:
  /// **'Analyzes past rejection reasons to provide cause distribution statistics and resubmission checklists.'**
  String get rejectionAnalyticsDesc;

  /// No description provided for @rejectionCauseDistribution.
  ///
  /// In en, this message translates to:
  /// **'Rejection Cause Distribution'**
  String get rejectionCauseDistribution;

  /// No description provided for @resubmitChecklist.
  ///
  /// In en, this message translates to:
  /// **'Resubmission Pre-Checklist'**
  String get resubmitChecklist;

  /// No description provided for @actionGuide.
  ///
  /// In en, this message translates to:
  /// **'Action Guide'**
  String get actionGuide;

  /// No description provided for @loginCount.
  ///
  /// In en, this message translates to:
  /// **'Login Count'**
  String get loginCount;

  /// No description provided for @btnLogin.
  ///
  /// In en, this message translates to:
  /// **'Sign In'**
  String get btnLogin;

  /// No description provided for @btnRegister.
  ///
  /// In en, this message translates to:
  /// **'Create Account'**
  String get btnRegister;

  /// No description provided for @login.
  ///
  /// In en, this message translates to:
  /// **'Login'**
  String get login;

  /// No description provided for @loginFailed.
  ///
  /// In en, this message translates to:
  /// **'Login failed. Please check your credentials.'**
  String get loginFailed;

  /// No description provided for @loginTitleSub.
  ///
  /// In en, this message translates to:
  /// **'Secure Data Classification Platform'**
  String get loginTitleSub;

  /// No description provided for @logout.
  ///
  /// In en, this message translates to:
  /// **'Logout'**
  String get logout;

  /// No description provided for @newWorkflowRegister.
  ///
  /// In en, this message translates to:
  /// **'+ New Workflow'**
  String get newWorkflowRegister;

  /// No description provided for @noOrgsRegistered.
  ///
  /// In en, this message translates to:
  /// **'No organizations registered.'**
  String get noOrgsRegistered;

  /// No description provided for @registerRoleBtn.
  ///
  /// In en, this message translates to:
  /// **'Register Role'**
  String get registerRoleBtn;

  /// No description provided for @registeredDomains.
  ///
  /// In en, this message translates to:
  /// **'Registered Master Domains'**
  String get registeredDomains;

  /// No description provided for @tabLogin.
  ///
  /// In en, this message translates to:
  /// **'Login'**
  String get tabLogin;

  /// No description provided for @tabRegister.
  ///
  /// In en, this message translates to:
  /// **'Register'**
  String get tabRegister;

  /// No description provided for @userLoginLogs.
  ///
  /// In en, this message translates to:
  /// **'User Login Logs'**
  String get userLoginLogs;

  /// No description provided for @forcePasswordChange.
  ///
  /// In en, this message translates to:
  /// **'Force Password Change'**
  String get forcePasswordChange;

  /// No description provided for @forcePasswordChangeDesc.
  ///
  /// In en, this message translates to:
  /// **'For security reasons, you must change your initial password. Please set a new password.'**
  String get forcePasswordChangeDesc;

  /// No description provided for @createUser.
  ///
  /// In en, this message translates to:
  /// **'Create User'**
  String get createUser;

  /// No description provided for @userCreated.
  ///
  /// In en, this message translates to:
  /// **'User Created Successfully'**
  String get userCreated;

  /// No description provided for @oldPassword.
  ///
  /// In en, this message translates to:
  /// **'Current Password'**
  String get oldPassword;

  /// No description provided for @newPassword.
  ///
  /// In en, this message translates to:
  /// **'New Password'**
  String get newPassword;

  /// No description provided for @confirmNewPassword.
  ///
  /// In en, this message translates to:
  /// **'Confirm New Password'**
  String get confirmNewPassword;

  /// No description provided for @changePassword.
  ///
  /// In en, this message translates to:
  /// **'Change Password'**
  String get changePassword;

  /// No description provided for @tempPasswordIssued.
  ///
  /// In en, this message translates to:
  /// **'Temporary Password Issued'**
  String get tempPasswordIssued;

  /// No description provided for @authLoginErrorMessage.
  ///
  /// In en, this message translates to:
  /// **'Authentication failed. Please check your credentials.'**
  String get authLoginErrorMessage;

  /// No description provided for @requiredField.
  ///
  /// In en, this message translates to:
  /// **'This field is required.'**
  String get requiredField;

  /// No description provided for @min8Chars.
  ///
  /// In en, this message translates to:
  /// **'Please enter at least 8 characters.'**
  String get min8Chars;

  /// No description provided for @passwordsDoNotMatch.
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match.'**
  String get passwordsDoNotMatch;

  /// No description provided for @fillAllFields.
  ///
  /// In en, this message translates to:
  /// **'Please fill in all fields.'**
  String get fillAllFields;

  /// No description provided for @passwordChangeFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to change password.'**
  String get passwordChangeFailed;

  /// No description provided for @accessCount.
  ///
  /// In en, this message translates to:
  /// **'Access Count'**
  String get accessCount;

  /// No description provided for @dispatchedTO.
  ///
  /// In en, this message translates to:
  /// **'Dispatched To'**
  String get dispatchedTO;

  /// No description provided for @decryptionTrendLast7Days.
  ///
  /// In en, this message translates to:
  /// **'Decryption Trend (Last 7 Days)'**
  String get decryptionTrendLast7Days;

  /// No description provided for @evolvedTO.
  ///
  /// In en, this message translates to:
  /// **'Evolved To'**
  String get evolvedTO;

  /// No description provided for @modifiedTO.
  ///
  /// In en, this message translates to:
  /// **'Modified To'**
  String get modifiedTO;

  /// No description provided for @typeRatios.
  ///
  /// In en, this message translates to:
  /// **'Type Ratios'**
  String get typeRatios;

  /// No description provided for @recordItem.
  ///
  /// In en, this message translates to:
  /// **'Data Record'**
  String get recordItem;

  /// No description provided for @imageFile.
  ///
  /// In en, this message translates to:
  /// **'Image File'**
  String get imageFile;

  /// No description provided for @attachment.
  ///
  /// In en, this message translates to:
  /// **'Attachment'**
  String get attachment;

  /// No description provided for @richText.
  ///
  /// In en, this message translates to:
  /// **'Rich Text'**
  String get richText;

  /// No description provided for @previewImage.
  ///
  /// In en, this message translates to:
  /// **'Image Preview'**
  String get previewImage;

  /// No description provided for @downloadFile.
  ///
  /// In en, this message translates to:
  /// **'Download Completed File'**
  String get downloadFile;

  /// Translated from image_count
  ///
  /// In en, this message translates to:
  /// **'{count} images'**
  String imageCount(Object count);

  /// Translated from file_count
  ///
  /// In en, this message translates to:
  /// **'File ({count})'**
  String fileCount(Object count);

  /// No description provided for @accessReason.
  ///
  /// In en, this message translates to:
  /// **'Access Reason'**
  String get accessReason;

  /// No description provided for @accessReasonPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g., Business process, Customer request'**
  String get accessReasonPlaceholder;

  /// No description provided for @accessReasonRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter an access reason.'**
  String get accessReasonRequired;

  /// No description provided for @actionRequired.
  ///
  /// In en, this message translates to:
  /// **'⚠️ Action Required'**
  String get actionRequired;

  /// No description provided for @actionTypeAll.
  ///
  /// In en, this message translates to:
  /// **'All'**
  String get actionTypeAll;

  /// No description provided for @actionTypeCol.
  ///
  /// In en, this message translates to:
  /// **'Action Type'**
  String get actionTypeCol;

  /// No description provided for @actionTypeCreate.
  ///
  /// In en, this message translates to:
  /// **'Creation (CREATE)'**
  String get actionTypeCreate;

  /// No description provided for @actionTypeCreateShort.
  ///
  /// In en, this message translates to:
  /// **'Creation'**
  String get actionTypeCreateShort;

  /// No description provided for @actionTypeDelete.
  ///
  /// In en, this message translates to:
  /// **'Deletion (DELETE)'**
  String get actionTypeDelete;

  /// No description provided for @actionTypeDeleteShort.
  ///
  /// In en, this message translates to:
  /// **'Deletion'**
  String get actionTypeDeleteShort;

  /// No description provided for @actionTypeMerge.
  ///
  /// In en, this message translates to:
  /// **'Record Merge (MERGE)'**
  String get actionTypeMerge;

  /// No description provided for @actionTypeMergeShort.
  ///
  /// In en, this message translates to:
  /// **'Record Merge'**
  String get actionTypeMergeShort;

  /// No description provided for @actionTypeUpdate.
  ///
  /// In en, this message translates to:
  /// **'Modification (UPDATE)'**
  String get actionTypeUpdate;

  /// No description provided for @actionTypeUpdateShort.
  ///
  /// In en, this message translates to:
  /// **'Modification'**
  String get actionTypeUpdateShort;

  /// No description provided for @actions.
  ///
  /// In en, this message translates to:
  /// **'Tree / Actions'**
  String get actions;

  /// No description provided for @actionsCol.
  ///
  /// In en, this message translates to:
  /// **'Actions'**
  String get actionsCol;

  /// No description provided for @activeStatus.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get activeStatus;

  /// No description provided for @approvalprogress.
  ///
  /// In en, this message translates to:
  /// **'Approval Progress'**
  String get approvalprogress;

  /// No description provided for @propertyFieldName.
  ///
  /// In en, this message translates to:
  /// **'Property / Field Name'**
  String get propertyFieldName;

  /// No description provided for @previousValue.
  ///
  /// In en, this message translates to:
  /// **'Previous Value'**
  String get previousValue;

  /// No description provided for @newValue.
  ///
  /// In en, this message translates to:
  /// **'New Value'**
  String get newValue;

  /// No description provided for @addDepartment.
  ///
  /// In en, this message translates to:
  /// **'Add Department'**
  String get addDepartment;

  /// No description provided for @addFilter.
  ///
  /// In en, this message translates to:
  /// **'Add Filter'**
  String get addFilter;

  /// No description provided for @addMenu.
  ///
  /// In en, this message translates to:
  /// **'Add Menu'**
  String get addMenu;

  /// No description provided for @addNewGroupBtn.
  ///
  /// In en, this message translates to:
  /// **'Add New Group'**
  String get addNewGroupBtn;

  /// No description provided for @addNewMember.
  ///
  /// In en, this message translates to:
  /// **'Add New Member'**
  String get addNewMember;

  /// No description provided for @addNewPermGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Create New Permission Group'**
  String get addNewPermGroupTitle;

  /// No description provided for @addOption.
  ///
  /// In en, this message translates to:
  /// **'Add Option'**
  String get addOption;

  /// No description provided for @addPermBtn.
  ///
  /// In en, this message translates to:
  /// **'Add Permission'**
  String get addPermBtn;

  /// No description provided for @addPermToGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Add New Permission to Group'**
  String get addPermToGroupTitle;

  /// No description provided for @addRootMenu.
  ///
  /// In en, this message translates to:
  /// **'+ Add Root Menu'**
  String get addRootMenu;

  /// No description provided for @addRow.
  ///
  /// In en, this message translates to:
  /// **'+ Add Row'**
  String get addRow;

  /// No description provided for @affectedChannels.
  ///
  /// In en, this message translates to:
  /// **'Affected Integration Channels'**
  String get affectedChannels;

  /// No description provided for @afterChange.
  ///
  /// In en, this message translates to:
  /// **'After Change (New Value)'**
  String get afterChange;

  /// No description provided for @agGridUnifiedList.
  ///
  /// In en, this message translates to:
  /// **'AG-Grid Unified List'**
  String get agGridUnifiedList;

  /// No description provided for @allTasksCleared.
  ///
  /// In en, this message translates to:
  /// **'✅ All Tasks Cleared'**
  String get allTasksCleared;

  /// No description provided for @approvalDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Approval Details'**
  String get approvalDetailTitle;

  /// No description provided for @approvalMonitorTitle.
  ///
  /// In en, this message translates to:
  /// **'Approval Monitoring'**
  String get approvalMonitorTitle;

  /// No description provided for @assigned.
  ///
  /// In en, this message translates to:
  /// **'Assigned'**
  String get assigned;

  /// No description provided for @assignedAt.
  ///
  /// In en, this message translates to:
  /// **'Assignment Date'**
  String get assignedAt;

  /// No description provided for @assignedMembersList.
  ///
  /// In en, this message translates to:
  /// **'Assigned Members List'**
  String get assignedMembersList;

  /// No description provided for @auditChangeType.
  ///
  /// In en, this message translates to:
  /// **'Change Type'**
  String get auditChangeType;

  /// No description provided for @auditChangedBy.
  ///
  /// In en, this message translates to:
  /// **'Changed By'**
  String get auditChangedBy;

  /// No description provided for @auditNewData.
  ///
  /// In en, this message translates to:
  /// **'New Data'**
  String get auditNewData;

  /// No description provided for @auditNoHistory.
  ///
  /// In en, this message translates to:
  /// **'No history found.'**
  String get auditNoHistory;

  /// No description provided for @auditPreviousData.
  ///
  /// In en, this message translates to:
  /// **'Previous Data'**
  String get auditPreviousData;

  /// No description provided for @auditTrail.
  ///
  /// In en, this message translates to:
  /// **'Audit Trail'**
  String get auditTrail;

  /// No description provided for @axisActions.
  ///
  /// In en, this message translates to:
  /// **'Tree / Actions'**
  String get axisActions;

  /// No description provided for @axisAddAxis.
  ///
  /// In en, this message translates to:
  /// **'Add Axis'**
  String get axisAddAxis;

  /// No description provided for @addAxis.
  ///
  /// In en, this message translates to:
  /// **'Add Axis'**
  String get addAxis;

  /// No description provided for @axisAddChildNode.
  ///
  /// In en, this message translates to:
  /// **'+ Child Node'**
  String get axisAddChildNode;

  /// No description provided for @addChildNode.
  ///
  /// In en, this message translates to:
  /// **'+ Child Node'**
  String get addChildNode;

  /// No description provided for @axisAddRootNode.
  ///
  /// In en, this message translates to:
  /// **'Add Root Node'**
  String get axisAddRootNode;

  /// No description provided for @addRootNode.
  ///
  /// In en, this message translates to:
  /// **'Add Root Node'**
  String get addRootNode;

  /// No description provided for @axisAssignModalTitle.
  ///
  /// In en, this message translates to:
  /// **'Assign Secondary Classification Nodes'**
  String get axisAssignModalTitle;

  /// No description provided for @assignModalTitle.
  ///
  /// In en, this message translates to:
  /// **'Assign Secondary Classification Nodes'**
  String get assignModalTitle;

  /// No description provided for @axisAssignSecondaryNodes.
  ///
  /// In en, this message translates to:
  /// **'Assign / Edit Secondary Nodes'**
  String get axisAssignSecondaryNodes;

  /// No description provided for @assignSecondaryNodes.
  ///
  /// In en, this message translates to:
  /// **'Assign / Edit Secondary Nodes'**
  String get assignSecondaryNodes;

  /// No description provided for @axisAxisAdded.
  ///
  /// In en, this message translates to:
  /// **'New classification axis added.'**
  String get axisAxisAdded;

  /// No description provided for @axisAdded.
  ///
  /// In en, this message translates to:
  /// **'New classification axis added.'**
  String get axisAdded;

  /// No description provided for @axisAxisCodeLabel.
  ///
  /// In en, this message translates to:
  /// **'Axis Code'**
  String get axisAxisCodeLabel;

  /// No description provided for @axisCodeLabel.
  ///
  /// In en, this message translates to:
  /// **'Axis Code'**
  String get axisCodeLabel;

  /// No description provided for @axisAxisDeleted.
  ///
  /// In en, this message translates to:
  /// **'Classification axis deleted.'**
  String get axisAxisDeleted;

  /// No description provided for @axisDeleted.
  ///
  /// In en, this message translates to:
  /// **'Classification axis deleted.'**
  String get axisDeleted;

  /// No description provided for @axisAxisLabel.
  ///
  /// In en, this message translates to:
  /// **'Axis'**
  String get axisAxisLabel;

  /// No description provided for @axisLabel.
  ///
  /// In en, this message translates to:
  /// **'Axis'**
  String get axisLabel;

  /// No description provided for @axisAxisNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Axis Name'**
  String get axisAxisNameLabel;

  /// No description provided for @axisNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Axis Name'**
  String get axisNameLabel;

  /// No description provided for @axisAxisUpdated.
  ///
  /// In en, this message translates to:
  /// **'Classification axis updated.'**
  String get axisAxisUpdated;

  /// No description provided for @axisUpdated.
  ///
  /// In en, this message translates to:
  /// **'Classification axis updated.'**
  String get axisUpdated;

  /// No description provided for @axisCode.
  ///
  /// In en, this message translates to:
  /// **'Axis Code'**
  String get axisCode;

  /// No description provided for @code.
  ///
  /// In en, this message translates to:
  /// **'Axis Code'**
  String get code;

  /// No description provided for @axisCodeBadge.
  ///
  /// In en, this message translates to:
  /// **'Code'**
  String get axisCodeBadge;

  /// No description provided for @codeBadge.
  ///
  /// In en, this message translates to:
  /// **'Code'**
  String get codeBadge;

  /// No description provided for @axisDeleteAxisConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this axis? All child nodes will also be deleted.'**
  String get axisDeleteAxisConfirm;

  /// No description provided for @deleteAxisConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this axis? All child nodes will also be deleted.'**
  String get deleteAxisConfirm;

  /// No description provided for @axisDeleteNode.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get axisDeleteNode;

  /// No description provided for @deleteNode.
  ///
  /// In en, this message translates to:
  /// **'Delete Node'**
  String get deleteNode;

  /// No description provided for @axisDeleteNodeConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this node?'**
  String get axisDeleteNodeConfirm;

  /// No description provided for @deleteNodeConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this node?'**
  String get deleteNodeConfirm;

  /// No description provided for @axisDescription.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get axisDescription;

  /// No description provided for @description.
  ///
  /// In en, this message translates to:
  /// **'Configure field-level survival priority and conflict resolution rules for golden record creation during merge.'**
  String get description;

  /// No description provided for @axisEditAxis.
  ///
  /// In en, this message translates to:
  /// **'Edit Axis'**
  String get axisEditAxis;

  /// No description provided for @editAxis.
  ///
  /// In en, this message translates to:
  /// **'Edit Axis'**
  String get editAxis;

  /// No description provided for @axisEditNode.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
  String get axisEditNode;

  /// No description provided for @editNode.
  ///
  /// In en, this message translates to:
  /// **'Edit Node'**
  String get editNode;

  /// No description provided for @axisEnterCodeName.
  ///
  /// In en, this message translates to:
  /// **'Please enter code and axis name.'**
  String get axisEnterCodeName;

  /// No description provided for @enterCodeName.
  ///
  /// In en, this message translates to:
  /// **'Please enter code and axis name.'**
  String get enterCodeName;

  /// No description provided for @axisInvalidDomain.
  ///
  /// In en, this message translates to:
  /// **'Invalid domain information.'**
  String get axisInvalidDomain;

  /// No description provided for @invalidDomain.
  ///
  /// In en, this message translates to:
  /// **'Invalid domain information.'**
  String get invalidDomain;

  /// No description provided for @axisLoadingTree.
  ///
  /// In en, this message translates to:
  /// **'Loading tree nodes...'**
  String get axisLoadingTree;

  /// No description provided for @loadingTree.
  ///
  /// In en, this message translates to:
  /// **'Loading tree nodes...'**
  String get loadingTree;

  /// No description provided for @axisManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Register independent multi-dimensional classification axes such as Organization, Region, or Industry.'**
  String get axisManagementDesc;

  /// No description provided for @managementDesc.
  ///
  /// In en, this message translates to:
  /// **'Register independent multi-dimensional classification axes such as Organization, Region, or Industry.'**
  String get managementDesc;

  /// No description provided for @axisManagementTitle.
  ///
  /// In en, this message translates to:
  /// **'Multi-Axis Classification Management'**
  String get axisManagementTitle;

  /// No description provided for @managementTitle.
  ///
  /// In en, this message translates to:
  /// **'Multi-Axis Classification Management'**
  String get managementTitle;

  /// No description provided for @axisName.
  ///
  /// In en, this message translates to:
  /// **'Axis Name'**
  String get axisName;

  /// No description provided for @axisNoAxes.
  ///
  /// In en, this message translates to:
  /// **'No secondary classification axes registered.'**
  String get axisNoAxes;

  /// No description provided for @noAxes.
  ///
  /// In en, this message translates to:
  /// **'No secondary classification axes registered.'**
  String get noAxes;

  /// No description provided for @axisNoNodesDesc.
  ///
  /// In en, this message translates to:
  /// **'No classification nodes registered for this axis. Click [Add Root Node] to build an independent tree.'**
  String get axisNoNodesDesc;

  /// No description provided for @noNodesDesc.
  ///
  /// In en, this message translates to:
  /// **'No classification nodes registered for this axis. Click [Add Root Node] to build an independent tree.'**
  String get noNodesDesc;

  /// No description provided for @axisNoNodesRegistered.
  ///
  /// In en, this message translates to:
  /// **'No nodes registered'**
  String get axisNoNodesRegistered;

  /// No description provided for @noNodesRegistered.
  ///
  /// In en, this message translates to:
  /// **'No nodes registered'**
  String get noNodesRegistered;

  /// No description provided for @axisNoSecondaryNodes.
  ///
  /// In en, this message translates to:
  /// **'No secondary classification nodes assigned.'**
  String get axisNoSecondaryNodes;

  /// No description provided for @noSecondaryNodes.
  ///
  /// In en, this message translates to:
  /// **'No secondary classification nodes assigned.'**
  String get noSecondaryNodes;

  /// No description provided for @axisNodeAdded.
  ///
  /// In en, this message translates to:
  /// **'New classification node added.'**
  String get axisNodeAdded;

  /// No description provided for @nodeAdded.
  ///
  /// In en, this message translates to:
  /// **'New classification node added.'**
  String get nodeAdded;

  /// No description provided for @axisNodeDeleted.
  ///
  /// In en, this message translates to:
  /// **'Classification node deleted.'**
  String get axisNodeDeleted;

  /// No description provided for @nodeDeleted.
  ///
  /// In en, this message translates to:
  /// **'Classification node deleted.'**
  String get nodeDeleted;

  /// No description provided for @axisNodeIcon.
  ///
  /// In en, this message translates to:
  /// **'Node Icon'**
  String get axisNodeIcon;

  /// No description provided for @nodeIcon.
  ///
  /// In en, this message translates to:
  /// **'Node Icon'**
  String get nodeIcon;

  /// No description provided for @axisNodeManagementTitle.
  ///
  /// In en, this message translates to:
  /// **'Axis Specific Tree Node Management'**
  String get axisNodeManagementTitle;

  /// No description provided for @nodeManagementTitle.
  ///
  /// In en, this message translates to:
  /// **'Axis Specific Tree Node Management'**
  String get nodeManagementTitle;

  /// No description provided for @axisNodeNameEn.
  ///
  /// In en, this message translates to:
  /// **'Node Name (EN)'**
  String get axisNodeNameEn;

  /// No description provided for @nodeNameEn.
  ///
  /// In en, this message translates to:
  /// **'Node Name (EN)'**
  String get nodeNameEn;

  /// No description provided for @axisNodeNameKo.
  ///
  /// In en, this message translates to:
  /// **'Node Name (KO)'**
  String get axisNodeNameKo;

  /// No description provided for @nodeNameKo.
  ///
  /// In en, this message translates to:
  /// **'Node Name (KO)'**
  String get nodeNameKo;

  /// No description provided for @axisNodeUpdated.
  ///
  /// In en, this message translates to:
  /// **'Classification node updated.'**
  String get axisNodeUpdated;

  /// No description provided for @nodeUpdated.
  ///
  /// In en, this message translates to:
  /// **'Classification node updated.'**
  String get nodeUpdated;

  /// No description provided for @axisPrimaryTree.
  ///
  /// In en, this message translates to:
  /// **'Primary Schema'**
  String get axisPrimaryTree;

  /// No description provided for @primaryTree.
  ///
  /// In en, this message translates to:
  /// **'Primary Schema'**
  String get primaryTree;

  /// No description provided for @axisRefresh.
  ///
  /// In en, this message translates to:
  /// **'Refresh'**
  String get axisRefresh;

  /// No description provided for @axisSaveSecondaryNodes.
  ///
  /// In en, this message translates to:
  /// **'Save Secondary Nodes'**
  String get axisSaveSecondaryNodes;

  /// No description provided for @saveSecondaryNodes.
  ///
  /// In en, this message translates to:
  /// **'Save Secondary Nodes'**
  String get saveSecondaryNodes;

  /// No description provided for @axisSecondaryMappingDesc.
  ///
  /// In en, this message translates to:
  /// **'View and edit secondary classification node assignments for this record.'**
  String get axisSecondaryMappingDesc;

  /// No description provided for @secondaryMappingDesc.
  ///
  /// In en, this message translates to:
  /// **'View and edit secondary classification node assignments for this record.'**
  String get secondaryMappingDesc;

  /// No description provided for @axisSecondaryMappingTitle.
  ///
  /// In en, this message translates to:
  /// **'Record Secondary Classification Node Mapping'**
  String get axisSecondaryMappingTitle;

  /// No description provided for @secondaryMappingTitle.
  ///
  /// In en, this message translates to:
  /// **'Record Secondary Classification Node Mapping'**
  String get secondaryMappingTitle;

  /// No description provided for @axisSecondaryNodesSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save secondary classification nodes'**
  String get axisSecondaryNodesSaveFailed;

  /// No description provided for @secondaryNodesSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save secondary classification nodes'**
  String get secondaryNodesSaveFailed;

  /// No description provided for @axisSecondaryNodesSaved.
  ///
  /// In en, this message translates to:
  /// **'Secondary classification nodes assigned successfully.'**
  String get axisSecondaryNodesSaved;

  /// No description provided for @secondaryNodesSaved.
  ///
  /// In en, this message translates to:
  /// **'Secondary classification nodes assigned successfully.'**
  String get secondaryNodesSaved;

  /// No description provided for @axisSelectAxis.
  ///
  /// In en, this message translates to:
  /// **'Select Axis'**
  String get axisSelectAxis;

  /// No description provided for @selectAxis.
  ///
  /// In en, this message translates to:
  /// **'Select Axis'**
  String get selectAxis;

  /// No description provided for @axisSelectIcon.
  ///
  /// In en, this message translates to:
  /// **'Select Icon'**
  String get axisSelectIcon;

  /// No description provided for @selectIcon.
  ///
  /// In en, this message translates to:
  /// **'Select Icon'**
  String get selectIcon;

  /// No description provided for @axisSelectNodesForAxis.
  ///
  /// In en, this message translates to:
  /// **'Select Nodes per Axis'**
  String get axisSelectNodesForAxis;

  /// No description provided for @selectNodesForAxis.
  ///
  /// In en, this message translates to:
  /// **'Select Nodes per Axis'**
  String get selectNodesForAxis;

  /// No description provided for @axisSelectNodesPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select nodes'**
  String get axisSelectNodesPlaceholder;

  /// No description provided for @selectNodesPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select nodes'**
  String get selectNodesPlaceholder;

  /// No description provided for @axisSortOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get axisSortOrder;

  /// No description provided for @axisTreeManage.
  ///
  /// In en, this message translates to:
  /// **'Tree Manage'**
  String get axisTreeManage;

  /// No description provided for @treeManage.
  ///
  /// In en, this message translates to:
  /// **'Tree Manage'**
  String get treeManage;

  /// No description provided for @bulkReclassify.
  ///
  /// In en, this message translates to:
  /// **'Bulk Reclassify'**
  String get bulkReclassify;

  /// Translated from bulk_reclassify_desc
  ///
  /// In en, this message translates to:
  /// **'Batch change classification node for {count} selected records.'**
  String bulkReclassifyDesc(Object count);

  /// No description provided for @bulkReclassifySuccess.
  ///
  /// In en, this message translates to:
  /// **'Bulk reclassification completed successfully.'**
  String get bulkReclassifySuccess;

  /// No description provided for @bulkReclassifyFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to bulk reclassify records.'**
  String get bulkReclassifyFail;

  /// No description provided for @bulkReclassifyTargetNode.
  ///
  /// In en, this message translates to:
  /// **'Select Target Classification Node'**
  String get bulkReclassifyTargetNode;

  /// No description provided for @businessRuleBuilder.
  ///
  /// In en, this message translates to:
  /// **'Business Rule Builder'**
  String get businessRuleBuilder;

  /// Translated from selected_record_target
  ///
  /// In en, this message translates to:
  /// **'Target Record: {code}'**
  String selectedRecordTarget(Object code);

  /// No description provided for @tableView.
  ///
  /// In en, this message translates to:
  /// **'Diff Table'**
  String get tableView;

  /// No description provided for @jsonView.
  ///
  /// In en, this message translates to:
  /// **'Raw JSON'**
  String get jsonView;

  /// No description provided for @diffFieldName.
  ///
  /// In en, this message translates to:
  /// **'Field Name'**
  String get diffFieldName;

  /// No description provided for @diffBefore.
  ///
  /// In en, this message translates to:
  /// **'Before'**
  String get diffBefore;

  /// No description provided for @diffAfter.
  ///
  /// In en, this message translates to:
  /// **'After'**
  String get diffAfter;

  /// No description provided for @diffStatus.
  ///
  /// In en, this message translates to:
  /// **'Diff Status'**
  String get diffStatus;

  /// No description provided for @noDiffData.
  ///
  /// In en, this message translates to:
  /// **'No attribute data to display.'**
  String get noDiffData;

  /// No description provided for @selectCdcEventGuide.
  ///
  /// In en, this message translates to:
  /// **'Select a CDC event from the left list to inspect details.'**
  String get selectCdcEventGuide;

  /// No description provided for @noCdcEventsInDomain.
  ///
  /// In en, this message translates to:
  /// **'No real-time change data capture events found for this domain.'**
  String get noCdcEventsInDomain;

  /// No description provided for @exportDownloadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Export file downloaded successfully.'**
  String get exportDownloadSuccess;

  /// No description provided for @exportDownloadFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to download export file.'**
  String get exportDownloadFailed;

  /// Translated from email_domain_auto_hint
  ///
  /// In en, this message translates to:
  /// **'Auto-completed with organization domain ({domain}).'**
  String emailDomainAutoHint(Object domain);

  /// No description provided for @emailDefaultFallbackHint.
  ///
  /// In en, this message translates to:
  /// **'Enter your email address.'**
  String get emailDefaultFallbackHint;

  /// No description provided for @backupMenuSeed.
  ///
  /// In en, this message translates to:
  /// **'Backup Menu State'**
  String get backupMenuSeed;

  /// No description provided for @menuChildrenRoleUnionNotice.
  ///
  /// In en, this message translates to:
  /// **'Child menus exist, so required roles are automatically merged (union) and cannot be manually modified.'**
  String get menuChildrenRoleUnionNotice;

  /// No description provided for @menuDumpSeedConfirm.
  ///
  /// In en, this message translates to:
  /// **'Do you want to backup current menu settings (order, active status, required roles, etc.) to the default seed file? (Admin only)'**
  String get menuDumpSeedConfirm;

  /// No description provided for @menuDumpSeedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Menu seed file backup completed successfully.'**
  String get menuDumpSeedSuccess;

  /// No description provided for @menuDumpSeedFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to backup menu seed file.'**
  String get menuDumpSeedFailed;

  /// No description provided for @menuSyncSeedConfirm.
  ///
  /// In en, this message translates to:
  /// **'Do you want to sync the current menu structure with the seed file (default_menus.json)? (Missing menus will be added and existing menus updated)'**
  String get menuSyncSeedConfirm;

  /// No description provided for @menuSyncSeedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Menu synchronization completed successfully.'**
  String get menuSyncSeedSuccess;

  /// No description provided for @menuSyncSeedFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to synchronize menus.'**
  String get menuSyncSeedFailed;

  /// No description provided for @dqTargetField.
  ///
  /// In en, this message translates to:
  /// **'Target Field'**
  String get dqTargetField;

  /// No description provided for @dqSelectFieldPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select a node from the tree and then choose a field.'**
  String get dqSelectFieldPlaceholder;

  /// No description provided for @dqSelectNodeFieldGuide.
  ///
  /// In en, this message translates to:
  /// **'Select a node and field to manage data quality rules.'**
  String get dqSelectNodeFieldGuide;

  /// No description provided for @dqNoRulesFound.
  ///
  /// In en, this message translates to:
  /// **'No DQ rules found for this field.'**
  String get dqNoRulesFound;

  /// No description provided for @dqRuleSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save DQ rule.'**
  String get dqRuleSaveFailed;

  /// No description provided for @dqRuleDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this rule?'**
  String get dqRuleDeleteConfirm;

  /// No description provided for @dqRuleDeletedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Rule deleted successfully.'**
  String get dqRuleDeletedSuccess;

  /// No description provided for @dqRuleDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete rule.'**
  String get dqRuleDeleteFailed;

  /// Translated from user_delete_confirm
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete user \'{username}\'? This action cannot be undone.'**
  String userDeleteConfirm(Object username);

  /// No description provided for @userDeleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'User deleted successfully.'**
  String get userDeleteSuccess;

  /// No description provided for @userDeleteConflictError.
  ///
  /// In en, this message translates to:
  /// **'Cannot delete user because associated data (records or approval requests) exists.'**
  String get userDeleteConflictError;

  /// Translated from user_delete_failed
  ///
  /// In en, this message translates to:
  /// **'Error occurred while deleting user: {error}'**
  String userDeleteFailed(Object error);

  /// No description provided for @userTempPasswordNotFound.
  ///
  /// In en, this message translates to:
  /// **'Temporary password not found.'**
  String get userTempPasswordNotFound;

  /// No description provided for @userTempPasswordQueryFailed.
  ///
  /// In en, this message translates to:
  /// **'Query failed: Temporary password does not exist or unauthorized.'**
  String get userTempPasswordQueryFailed;

  /// Translated from dump_seed_files_confirm_org
  ///
  /// In en, this message translates to:
  /// **'Do you want to backup/overwrite all role states set in the \'{name}\' organization to the system default (Seed) JSON file?\n(This will directly modify files in the source code directory)'**
  String dumpSeedFilesConfirmOrg(Object name);

  /// No description provided for @dumpSeedFilesConfirmAll.
  ///
  /// In en, this message translates to:
  /// **'Do you want to backup/overwrite all role states stored in the entire DB to the system default (Seed) JSON file?\n(This will directly modify files in the source code directory)'**
  String get dumpSeedFilesConfirmAll;

  /// No description provided for @dumpSeedFilesSuccess.
  ///
  /// In en, this message translates to:
  /// **'Default seed files successfully updated.'**
  String get dumpSeedFilesSuccess;

  /// No description provided for @dumpSeedFilesFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to update default seed files.'**
  String get dumpSeedFilesFail;

  /// No description provided for @dumpSeedFilesError.
  ///
  /// In en, this message translates to:
  /// **'Error occurred while updating default seed files.'**
  String get dumpSeedFilesError;

  /// No description provided for @baselineBadge.
  ///
  /// In en, this message translates to:
  /// **'Baseline'**
  String get baselineBadge;

  /// No description provided for @basicInfo.
  ///
  /// In en, this message translates to:
  /// **'Basic Info'**
  String get basicInfo;

  /// No description provided for @beforeChange.
  ///
  /// In en, this message translates to:
  /// **'Before Change (Previous Value)'**
  String get beforeChange;

  /// No description provided for @boolean.
  ///
  /// In en, this message translates to:
  /// **'Boolean'**
  String get boolean;

  /// No description provided for @btndetails.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get btndetails;

  /// No description provided for @btnCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get btnCancel;

  /// No description provided for @calculated.
  ///
  /// In en, this message translates to:
  /// **'Calculated'**
  String get calculated;

  /// No description provided for @calculatedSuffix.
  ///
  /// In en, this message translates to:
  /// **'(Calculated)'**
  String get calculatedSuffix;

  /// No description provided for @changeContent.
  ///
  /// In en, this message translates to:
  /// **'Changes'**
  String get changeContent;

  /// No description provided for @changeDetails.
  ///
  /// In en, this message translates to:
  /// **'Changes'**
  String get changeDetails;

  /// No description provided for @changeHistory.
  ///
  /// In en, this message translates to:
  /// **'Change History'**
  String get changeHistory;

  /// No description provided for @changeHistoryTab.
  ///
  /// In en, this message translates to:
  /// **'Change History'**
  String get changeHistoryTab;

  /// No description provided for @changeType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get changeType;

  /// No description provided for @changedBy.
  ///
  /// In en, this message translates to:
  /// **'Changed By'**
  String get changedBy;

  /// No description provided for @chartRatio.
  ///
  /// In en, this message translates to:
  /// **'Ratio'**
  String get chartRatio;

  /// No description provided for @chatTableTitle.
  ///
  /// In en, this message translates to:
  /// **'Excel Data Table'**
  String get chatTableTitle;

  /// No description provided for @classificationNode.
  ///
  /// In en, this message translates to:
  /// **'Classification Node'**
  String get classificationNode;

  /// No description provided for @clickImageToExpandTip.
  ///
  /// In en, this message translates to:
  /// **'Click to expand image in large modal viewer'**
  String get clickImageToExpandTip;

  /// No description provided for @clickTableToExpandTip.
  ///
  /// In en, this message translates to:
  /// **'Click table to expand in large modal viewer'**
  String get clickTableToExpandTip;

  /// No description provided for @colaction.
  ///
  /// In en, this message translates to:
  /// **'Action'**
  String get colaction;

  /// No description provided for @colcreatedat.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get colcreatedat;

  /// No description provided for @colrequester.
  ///
  /// In en, this message translates to:
  /// **'Requester'**
  String get colrequester;

  /// No description provided for @colstatus.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get colstatus;

  /// No description provided for @coltargettype.
  ///
  /// In en, this message translates to:
  /// **'Target Type'**
  String get coltargettype;

  /// No description provided for @collapse.
  ///
  /// In en, this message translates to:
  /// **'Collapse'**
  String get collapse;

  /// No description provided for @comment.
  ///
  /// In en, this message translates to:
  /// **'Comment'**
  String get comment;

  /// No description provided for @commonLoading.
  ///
  /// In en, this message translates to:
  /// **'Processing data...'**
  String get commonLoading;

  /// No description provided for @compareChanges.
  ///
  /// In en, this message translates to:
  /// **'Compare Changes'**
  String get compareChanges;

  /// Translated from comparing_count
  ///
  /// In en, this message translates to:
  /// **'Comparing {count} records'**
  String comparingCount(Object count);

  /// No description provided for @confirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get confirm;

  /// No description provided for @copyAsMarkdown.
  ///
  /// In en, this message translates to:
  /// **'📝 Copy as Markdown Table'**
  String get copyAsMarkdown;

  /// No description provided for @copyCell.
  ///
  /// In en, this message translates to:
  /// **'Copy Cell'**
  String get copyCell;

  /// No description provided for @copyTableBtn.
  ///
  /// In en, this message translates to:
  /// **'Copy Table Data'**
  String get copyTableBtn;

  /// No description provided for @copyTableBtnTitle.
  ///
  /// In en, this message translates to:
  /// **'Copy Excel table data to clipboard'**
  String get copyTableBtnTitle;

  /// No description provided for @create.
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get create;

  /// No description provided for @createGroupBtn.
  ///
  /// In en, this message translates to:
  /// **'Create Group'**
  String get createGroupBtn;

  /// No description provided for @createWorkflowTitle.
  ///
  /// In en, this message translates to:
  /// **'🆕 Register New Workflow Template'**
  String get createWorkflowTitle;

  /// No description provided for @createdat.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get createdat;

  /// No description provided for @creationSuccess.
  ///
  /// In en, this message translates to:
  /// **'Creation Completed'**
  String get creationSuccess;

  /// No description provided for @currentAffiliation.
  ///
  /// In en, this message translates to:
  /// **'Current Affiliation:'**
  String get currentAffiliation;

  /// No description provided for @dashboard.
  ///
  /// In en, this message translates to:
  /// **'Dashboard'**
  String get dashboard;

  /// No description provided for @dashboardSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Monitor master data governance overview, quality diagnostic metrics, and key integration statuses.'**
  String get dashboardSubtitle;

  /// No description provided for @date.
  ///
  /// In en, this message translates to:
  /// **'Date'**
  String get date;

  /// No description provided for @dateTime.
  ///
  /// In en, this message translates to:
  /// **'Date & Time'**
  String get dateTime;

  /// No description provided for @decryptFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to decrypt value.'**
  String get decryptFailed;

  /// No description provided for @deduplicationCandidateRecord.
  ///
  /// In en, this message translates to:
  /// **'Candidate Record'**
  String get deduplicationCandidateRecord;

  /// No description provided for @candidateRecord.
  ///
  /// In en, this message translates to:
  /// **'Candidate Record'**
  String get candidateRecord;

  /// No description provided for @deduplicationCompareAndAction.
  ///
  /// In en, this message translates to:
  /// **'Compare & Act'**
  String get deduplicationCompareAndAction;

  /// No description provided for @compareAndAction.
  ///
  /// In en, this message translates to:
  /// **'Compare & Act'**
  String get compareAndAction;

  /// No description provided for @deduplicationConfirmMerge.
  ///
  /// In en, this message translates to:
  /// **'Approve Merge'**
  String get deduplicationConfirmMerge;

  /// No description provided for @confirmMerge.
  ///
  /// In en, this message translates to:
  /// **'Confirm Merge'**
  String get confirmMerge;

  /// No description provided for @deduplicationDuplicateCandidate.
  ///
  /// In en, this message translates to:
  /// **'Candidate Record (To Merge)'**
  String get deduplicationDuplicateCandidate;

  /// No description provided for @duplicateCandidate.
  ///
  /// In en, this message translates to:
  /// **'Candidate Record (To Merge)'**
  String get duplicateCandidate;

  /// No description provided for @deduplicationKeepSeparate.
  ///
  /// In en, this message translates to:
  /// **'Keep Separate'**
  String get deduplicationKeepSeparate;

  /// No description provided for @keepSeparate.
  ///
  /// In en, this message translates to:
  /// **'Keep Separate'**
  String get keepSeparate;

  /// No description provided for @deduplicationMasterRecord.
  ///
  /// In en, this message translates to:
  /// **'Master Record (Survivor)'**
  String get deduplicationMasterRecord;

  /// No description provided for @masterRecord.
  ///
  /// In en, this message translates to:
  /// **'Master Record (Survivor)'**
  String get masterRecord;

  /// No description provided for @deduplicationModalTitle.
  ///
  /// In en, this message translates to:
  /// **'Side-by-Side Field Comparison'**
  String get deduplicationModalTitle;

  /// No description provided for @modalTitle.
  ///
  /// In en, this message translates to:
  /// **'Side-by-Side Field Comparison'**
  String get modalTitle;

  /// No description provided for @deduplicationNoCandidates.
  ///
  /// In en, this message translates to:
  /// **'No candidate records to review.'**
  String get deduplicationNoCandidates;

  /// No description provided for @noCandidates.
  ///
  /// In en, this message translates to:
  /// **'No candidate records to review.'**
  String get noCandidates;

  /// Translated from deduplication_pending_count
  ///
  /// In en, this message translates to:
  /// **'Pending: {count}'**
  String deduplicationPendingCount(Object count);

  /// No description provided for @deduplicationRuleDefault.
  ///
  /// In en, this message translates to:
  /// **'Fuzzy Match Rule'**
  String get deduplicationRuleDefault;

  /// No description provided for @ruleDefault.
  ///
  /// In en, this message translates to:
  /// **'Fuzzy Match Rule'**
  String get ruleDefault;

  /// No description provided for @deduplicationSimilarity.
  ///
  /// In en, this message translates to:
  /// **'Similarity'**
  String get deduplicationSimilarity;

  /// No description provided for @similarity.
  ///
  /// In en, this message translates to:
  /// **'Similarity'**
  String get similarity;

  /// No description provided for @deduplicationSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Review potential duplicate records found by fuzzy matching and approve merge or separation.'**
  String get deduplicationSubtitle;

  /// No description provided for @deduplicationTargetRecord.
  ///
  /// In en, this message translates to:
  /// **'Target Master Record'**
  String get deduplicationTargetRecord;

  /// No description provided for @targetRecord.
  ///
  /// In en, this message translates to:
  /// **'Target Master Record'**
  String get targetRecord;

  /// No description provided for @deduplicationTitle.
  ///
  /// In en, this message translates to:
  /// **'Match Candidates Queue'**
  String get deduplicationTitle;

  /// No description provided for @defaultBadge.
  ///
  /// In en, this message translates to:
  /// **'⭐ Default'**
  String get defaultBadge;

  /// No description provided for @delete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get delete;

  /// No description provided for @deleteErrorTitle.
  ///
  /// In en, this message translates to:
  /// **'Deletion Error'**
  String get deleteErrorTitle;

  /// No description provided for @deletedStatus.
  ///
  /// In en, this message translates to:
  /// **'Deleted'**
  String get deletedStatus;

  /// No description provided for @department.
  ///
  /// In en, this message translates to:
  /// **'Department'**
  String get department;

  /// No description provided for @descriptionCol.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get descriptionCol;

  /// No description provided for @detailsInfo.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get detailsInfo;

  /// No description provided for @diffCountSuffix.
  ///
  /// In en, this message translates to:
  /// **'Diffs'**
  String get diffCountSuffix;

  /// No description provided for @doReview.
  ///
  /// In en, this message translates to:
  /// **'Review'**
  String get doReview;

  /// No description provided for @draftCommentOptional.
  ///
  /// In en, this message translates to:
  /// **'(Optional) Please write a comment for the approver'**
  String get draftCommentOptional;

  /// No description provided for @draftCommentPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter a comment...'**
  String get draftCommentPlaceholder;

  /// No description provided for @draftCommentTitle.
  ///
  /// In en, this message translates to:
  /// **'Draft Comment'**
  String get draftCommentTitle;

  /// Translated from e_g_abs_key_a_key_b_2_100
  ///
  /// In en, this message translates to:
  /// **'e.g. ABS({KEY_A} + {KEY_B} / 2) * 100'**
  String eGAbsKeyAKeyB2100(Object KEY_A, Object KEY_B);

  /// No description provided for @edit.
  ///
  /// In en, this message translates to:
  /// **'Edit Channel'**
  String get edit;

  /// No description provided for @editMenu.
  ///
  /// In en, this message translates to:
  /// **'Edit Menu'**
  String get editMenu;

  /// No description provided for @editWorkflowTitle.
  ///
  /// In en, this message translates to:
  /// **'✏️ Edit Workflow Template'**
  String get editWorkflowTitle;

  /// No description provided for @empNoPrefix.
  ///
  /// In en, this message translates to:
  /// **'Employee ID'**
  String get empNoPrefix;

  /// No description provided for @endDate.
  ///
  /// In en, this message translates to:
  /// **'End Date'**
  String get endDate;

  /// No description provided for @enterFormula.
  ///
  /// In en, this message translates to:
  /// **'Please enter a formula.'**
  String get enterFormula;

  /// No description provided for @enterKeyAllOptions.
  ///
  /// In en, this message translates to:
  /// **'Please enter a Key for all options.'**
  String get enterKeyAllOptions;

  /// No description provided for @error.
  ///
  /// In en, this message translates to:
  /// **'Error'**
  String get error;

  /// Translated from error_dedup_failed
  ///
  /// In en, this message translates to:
  /// **'Duplicate record found based on Identifier field \'{field}\'.'**
  String errorDedupFailed(Object field);

  /// No description provided for @errorDeletePendingCreation.
  ///
  /// In en, this message translates to:
  /// **'Cannot delete a record that is pending creation approval.'**
  String get errorDeletePendingCreation;

  /// No description provided for @errorFetching.
  ///
  /// In en, this message translates to:
  /// **'Error fetching data.'**
  String get errorFetching;

  /// No description provided for @errorInvalidCredentials.
  ///
  /// In en, this message translates to:
  /// **'Invalid credentials.'**
  String get errorInvalidCredentials;

  /// No description provided for @errorNotAssignee.
  ///
  /// In en, this message translates to:
  /// **'You are not the assignee for this step.'**
  String get errorNotAssignee;

  /// No description provided for @errorSaving.
  ///
  /// In en, this message translates to:
  /// **'Error saving.'**
  String get errorSaving;

  /// No description provided for @errorStepNotPending.
  ///
  /// In en, this message translates to:
  /// **'Step is not pending.'**
  String get errorStepNotPending;

  /// No description provided for @errorUpdatePendingCreation.
  ///
  /// In en, this message translates to:
  /// **'Cannot update a record that is pending creation approval.'**
  String get errorUpdatePendingCreation;

  /// No description provided for @errorUpdatePendingUpdate.
  ///
  /// In en, this message translates to:
  /// **'This record is already under a pending update approval.'**
  String get errorUpdatePendingUpdate;

  /// No description provided for @errorUploadDirFail.
  ///
  /// In en, this message translates to:
  /// **'Could not create the directory where the uploaded files will be stored.'**
  String get errorUploadDirFail;

  /// No description provided for @errorUploadFileFail.
  ///
  /// In en, this message translates to:
  /// **'Could not store file. Please try again!'**
  String get errorUploadFileFail;

  /// No description provided for @expand.
  ///
  /// In en, this message translates to:
  /// **'Expand'**
  String get expand;

  /// No description provided for @failedLoadApprovalDetails.
  ///
  /// In en, this message translates to:
  /// **'Failed to load approval details.'**
  String get failedLoadApprovalDetails;

  /// No description provided for @failedLoadHistory.
  ///
  /// In en, this message translates to:
  /// **'Failed to load history data.'**
  String get failedLoadHistory;

  /// No description provided for @file.
  ///
  /// In en, this message translates to:
  /// **'File'**
  String get file;

  /// No description provided for @fileUploadButton.
  ///
  /// In en, this message translates to:
  /// **'Select from PC'**
  String get fileUploadButton;

  /// No description provided for @fileUploadDropzone.
  ///
  /// In en, this message translates to:
  /// **'Drag and drop files here or '**
  String get fileUploadDropzone;

  /// No description provided for @finalPermValue.
  ///
  /// In en, this message translates to:
  /// **'Final Permission Value'**
  String get finalPermValue;

  /// No description provided for @formDescription.
  ///
  /// In en, this message translates to:
  /// **'Form Description'**
  String get formDescription;

  /// No description provided for @formDescriptionPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Describe the purpose and approval procedure...'**
  String get formDescriptionPlaceholder;

  /// No description provided for @formNameEn.
  ///
  /// In en, this message translates to:
  /// **'Form Title (English)'**
  String get formNameEn;

  /// No description provided for @formNameEnPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g. Domestic Stock Creation Form'**
  String get formNameEnPlaceholder;

  /// No description provided for @formNameKo.
  ///
  /// In en, this message translates to:
  /// **'Form Title (Korean) *'**
  String get formNameKo;

  /// No description provided for @formNameKoPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g. Domestic Stock Creation Form'**
  String get formNameKoPlaceholder;

  /// No description provided for @formulaGuide.
  ///
  /// In en, this message translates to:
  /// **'Formula Guide'**
  String get formulaGuide;

  /// No description provided for @governanceHealthTitle.
  ///
  /// In en, this message translates to:
  /// **'Governance & Data Quality Health'**
  String get governanceHealthTitle;

  /// No description provided for @grant.
  ///
  /// In en, this message translates to:
  /// **'Grant'**
  String get grant;

  /// No description provided for @grantNewPermission.
  ///
  /// In en, this message translates to:
  /// **'Grant New Permission'**
  String get grantNewPermission;

  /// No description provided for @grantPermissionSuccess.
  ///
  /// In en, this message translates to:
  /// **'Selected domain permissions granted successfully.'**
  String get grantPermissionSuccess;

  /// No description provided for @group.
  ///
  /// In en, this message translates to:
  /// **'Group'**
  String get group;

  /// No description provided for @groupCodeLabel.
  ///
  /// In en, this message translates to:
  /// **'Group Code Name (e.g. report, api)'**
  String get groupCodeLabel;

  /// No description provided for @groupIconLabel.
  ///
  /// In en, this message translates to:
  /// **'Emoji Icon (e.g. 📊, 🔑, ⚙️)'**
  String get groupIconLabel;

  /// No description provided for @groupTitleEnLabel.
  ///
  /// In en, this message translates to:
  /// **'Group English Title (e.g. Report Permissions)'**
  String get groupTitleEnLabel;

  /// No description provided for @groupTitleLabel.
  ///
  /// In en, this message translates to:
  /// **'Group Korean Title (e.g. 리포트 권한)'**
  String get groupTitleLabel;

  /// No description provided for @hidden.
  ///
  /// In en, this message translates to:
  /// **'Hidden'**
  String get hidden;

  /// No description provided for @history.
  ///
  /// In en, this message translates to:
  /// **'History'**
  String get history;

  /// No description provided for @historyVersionDiffDetail.
  ///
  /// In en, this message translates to:
  /// **'Detailed Comparison of Version Changes'**
  String get historyVersionDiffDetail;

  /// No description provided for @immutable.
  ///
  /// In en, this message translates to:
  /// **'Immutable'**
  String get immutable;

  /// No description provided for @impactAnalysis.
  ///
  /// In en, this message translates to:
  /// **'Impact Analysis'**
  String get impactAnalysis;

  /// No description provided for @impactAnalysisPreview.
  ///
  /// In en, this message translates to:
  /// **'Preview Impact Simulation'**
  String get impactAnalysisPreview;

  /// No description provided for @impactCheckTitle.
  ///
  /// In en, this message translates to:
  /// **'Pre-change Impact Review'**
  String get impactCheckTitle;

  /// No description provided for @impactSafetyNotice.
  ///
  /// In en, this message translates to:
  /// **'Checklist'**
  String get impactSafetyNotice;

  /// Translated from impact_summary_delete
  ///
  /// In en, this message translates to:
  /// **'Deleting field \'{field}\' will permanently remove data in {count} active record(s).'**
  String impactSummaryDelete(Object field, Object count);

  /// Translated from impact_summary_delete_empty
  ///
  /// In en, this message translates to:
  /// **'Field \'{field}\' has 0 active records, making it safe to delete without data loss.'**
  String impactSummaryDeleteEmpty(Object field);

  /// Translated from impact_summary_modify
  ///
  /// In en, this message translates to:
  /// **'Modifying field \'{field}\' will affect {count} active record(s).'**
  String impactSummaryModify(Object field, Object count);

  /// Translated from impact_summary_modify_empty
  ///
  /// In en, this message translates to:
  /// **'Field \'{field}\' has 0 active records, making it safe to modify without data loss.'**
  String impactSummaryModifyEmpty(Object field);

  /// No description provided for @impactWarnings.
  ///
  /// In en, this message translates to:
  /// **'Warnings & Precautions'**
  String get impactWarnings;

  /// No description provided for @inactiveBadge.
  ///
  /// In en, this message translates to:
  /// **'Inactive'**
  String get inactiveBadge;

  /// No description provided for @inactiveStatus.
  ///
  /// In en, this message translates to:
  /// **'Inactive'**
  String get inactiveStatus;

  /// No description provided for @incomingPayloadTitle.
  ///
  /// In en, this message translates to:
  /// **'incoming_payload.json'**
  String get incomingPayloadTitle;

  /// No description provided for @info.
  ///
  /// In en, this message translates to:
  /// **'Information'**
  String get info;

  /// No description provided for @infoMsg.
  ///
  /// In en, this message translates to:
  /// **'Select a domain node from the left tree to view records.'**
  String get infoMsg;

  /// No description provided for @initialCreated.
  ///
  /// In en, this message translates to:
  /// **'Initially Created'**
  String get initialCreated;

  /// No description provided for @initialCreation.
  ///
  /// In en, this message translates to:
  /// **'Initial Creation'**
  String get initialCreation;

  /// No description provided for @installBtnNext.
  ///
  /// In en, this message translates to:
  /// **'Next (Admin Account Setup)'**
  String get installBtnNext;

  /// No description provided for @installBtnPrev.
  ///
  /// In en, this message translates to:
  /// **'Back'**
  String get installBtnPrev;

  /// No description provided for @installRequirePwdLen.
  ///
  /// In en, this message translates to:
  /// **'Password must be at least 6 characters long.'**
  String get installRequirePwdLen;

  /// No description provided for @installStep1Label.
  ///
  /// In en, this message translates to:
  /// **'Primary Organization Setup'**
  String get installStep1Label;

  /// No description provided for @installStep2Label.
  ///
  /// In en, this message translates to:
  /// **'Super Admin Account Creation'**
  String get installStep2Label;

  /// No description provided for @installSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Initialize system setup and register the Super Admin account.'**
  String get installSubtitle;

  /// No description provided for @installTitle.
  ///
  /// In en, this message translates to:
  /// **'System Setup Wizard'**
  String get installTitle;

  /// No description provided for @integrationChannelsAdd.
  ///
  /// In en, this message translates to:
  /// **'Add Channel'**
  String get integrationChannelsAdd;

  /// No description provided for @integrationChannelsAddField.
  ///
  /// In en, this message translates to:
  /// **'Add Field'**
  String get integrationChannelsAddField;

  /// No description provided for @addField.
  ///
  /// In en, this message translates to:
  /// **'Add Field'**
  String get addField;

  /// No description provided for @integrationChannelsAddHeader.
  ///
  /// In en, this message translates to:
  /// **'Add Header'**
  String get integrationChannelsAddHeader;

  /// No description provided for @addHeader.
  ///
  /// In en, this message translates to:
  /// **'Add Header'**
  String get addHeader;

  /// No description provided for @integrationChannelsApprovalDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Approval Details'**
  String get integrationChannelsApprovalDetailTitle;

  /// No description provided for @integrationChannelsAuthApiKey.
  ///
  /// In en, this message translates to:
  /// **'API Key Header'**
  String get integrationChannelsAuthApiKey;

  /// No description provided for @authApiKey.
  ///
  /// In en, this message translates to:
  /// **'API Key Header'**
  String get authApiKey;

  /// No description provided for @integrationChannelsAuthBearer.
  ///
  /// In en, this message translates to:
  /// **'Bearer Token (Recommended)'**
  String get integrationChannelsAuthBearer;

  /// No description provided for @authBearer.
  ///
  /// In en, this message translates to:
  /// **'Bearer Token (Recommended)'**
  String get authBearer;

  /// No description provided for @integrationChannelsAuthHeaderExample.
  ///
  /// In en, this message translates to:
  /// **'Auth Header'**
  String get integrationChannelsAuthHeaderExample;

  /// No description provided for @authHeaderExample.
  ///
  /// In en, this message translates to:
  /// **'Auth Header'**
  String get authHeaderExample;

  /// No description provided for @integrationChannelsAuthNone.
  ///
  /// In en, this message translates to:
  /// **'None (No Auth)'**
  String get integrationChannelsAuthNone;

  /// No description provided for @authNone.
  ///
  /// In en, this message translates to:
  /// **'None (No Auth)'**
  String get authNone;

  /// No description provided for @integrationChannelsAuthType.
  ///
  /// In en, this message translates to:
  /// **'Inbound Auth Type'**
  String get integrationChannelsAuthType;

  /// No description provided for @authType.
  ///
  /// In en, this message translates to:
  /// **'Inbound Auth Type'**
  String get authType;

  /// No description provided for @integrationChannelsAutoMapFields.
  ///
  /// In en, this message translates to:
  /// **'Auto Map Domain Fields (Incl. Multilingual)'**
  String get integrationChannelsAutoMapFields;

  /// No description provided for @autoMapFields.
  ///
  /// In en, this message translates to:
  /// **'Auto Map Domain Fields (Incl. Multilingual)'**
  String get autoMapFields;

  /// No description provided for @integrationChannelsBasicConfig.
  ///
  /// In en, this message translates to:
  /// **'Basic & Auth Config'**
  String get integrationChannelsBasicConfig;

  /// No description provided for @basicConfig.
  ///
  /// In en, this message translates to:
  /// **'Basic & Auth Config'**
  String get basicConfig;

  /// No description provided for @integrationChannelsChannelCode.
  ///
  /// In en, this message translates to:
  /// **'Channel Code'**
  String get integrationChannelsChannelCode;

  /// No description provided for @channelCode.
  ///
  /// In en, this message translates to:
  /// **'Channel Code'**
  String get channelCode;

  /// No description provided for @integrationChannelsChannelName.
  ///
  /// In en, this message translates to:
  /// **'Channel Name'**
  String get integrationChannelsChannelName;

  /// No description provided for @channelName.
  ///
  /// In en, this message translates to:
  /// **'Channel Name'**
  String get channelName;

  /// No description provided for @integrationChannelsConfirmDeleteChannel.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this channel?'**
  String get integrationChannelsConfirmDeleteChannel;

  /// No description provided for @confirmDeleteChannel.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this channel?'**
  String get confirmDeleteChannel;

  /// No description provided for @integrationChannelsCopied.
  ///
  /// In en, this message translates to:
  /// **'Copied to clipboard.'**
  String get integrationChannelsCopied;

  /// No description provided for @copied.
  ///
  /// In en, this message translates to:
  /// **'Copied to clipboard.'**
  String get copied;

  /// No description provided for @integrationChannelsCopyCurl.
  ///
  /// In en, this message translates to:
  /// **'Copy cURL'**
  String get integrationChannelsCopyCurl;

  /// No description provided for @copyCurl.
  ///
  /// In en, this message translates to:
  /// **'Copy cURL'**
  String get copyCurl;

  /// No description provided for @integrationChannelsCopyHeader.
  ///
  /// In en, this message translates to:
  /// **'Copy Header'**
  String get integrationChannelsCopyHeader;

  /// No description provided for @copyHeader.
  ///
  /// In en, this message translates to:
  /// **'Copy Header'**
  String get copyHeader;

  /// No description provided for @integrationChannelsCopyJson.
  ///
  /// In en, this message translates to:
  /// **'Copy JSON'**
  String get integrationChannelsCopyJson;

  /// No description provided for @copyJson.
  ///
  /// In en, this message translates to:
  /// **'Copy JSON'**
  String get copyJson;

  /// No description provided for @integrationChannelsCopyValue.
  ///
  /// In en, this message translates to:
  /// **'Copy Value'**
  String get integrationChannelsCopyValue;

  /// No description provided for @copyValue.
  ///
  /// In en, this message translates to:
  /// **'Copy Value'**
  String get copyValue;

  /// No description provided for @integrationChannelsCreatedAt.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get integrationChannelsCreatedAt;

  /// No description provided for @createdAt.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get createdAt;

  /// No description provided for @integrationChannelsCurlCopied.
  ///
  /// In en, this message translates to:
  /// **'Sample cURL command copied to clipboard.'**
  String get integrationChannelsCurlCopied;

  /// No description provided for @curlCopied.
  ///
  /// In en, this message translates to:
  /// **'Sample cURL command copied to clipboard.'**
  String get curlCopied;

  /// No description provided for @integrationChannelsDbPassword.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get integrationChannelsDbPassword;

  /// No description provided for @dbPassword.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get dbPassword;

  /// No description provided for @integrationChannelsDbTable.
  ///
  /// In en, this message translates to:
  /// **'Target Table'**
  String get integrationChannelsDbTable;

  /// No description provided for @dbTable.
  ///
  /// In en, this message translates to:
  /// **'Target Table'**
  String get dbTable;

  /// No description provided for @integrationChannelsDbUrl.
  ///
  /// In en, this message translates to:
  /// **'DB URL'**
  String get integrationChannelsDbUrl;

  /// No description provided for @dbUrl.
  ///
  /// In en, this message translates to:
  /// **'DB URL'**
  String get dbUrl;

  /// No description provided for @integrationChannelsDbUser.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get integrationChannelsDbUser;

  /// No description provided for @dbUser.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get dbUser;

  /// No description provided for @integrationChannelsDeptRoles.
  ///
  /// In en, this message translates to:
  /// **'Department Roles (Multi-selectable)'**
  String get integrationChannelsDeptRoles;

  /// No description provided for @integrationChannelsDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage data integration pipelines and interface channel settings with external systems.'**
  String get integrationChannelsDesc;

  /// No description provided for @integrationChannelsDescription.
  ///
  /// In en, this message translates to:
  /// **'Manage integration channels.'**
  String get integrationChannelsDescription;

  /// No description provided for @integrationChannelsDetailConfig.
  ///
  /// In en, this message translates to:
  /// **'Channel Configuration'**
  String get integrationChannelsDetailConfig;

  /// No description provided for @detailConfig.
  ///
  /// In en, this message translates to:
  /// **'Channel Configuration'**
  String get detailConfig;

  /// No description provided for @integrationChannelsDirection.
  ///
  /// In en, this message translates to:
  /// **'Direction'**
  String get integrationChannelsDirection;

  /// No description provided for @direction.
  ///
  /// In en, this message translates to:
  /// **'Direction'**
  String get direction;

  /// No description provided for @integrationChannelsDomainField.
  ///
  /// In en, this message translates to:
  /// **'Domain Field (Optional)'**
  String get integrationChannelsDomainField;

  /// No description provided for @domainField.
  ///
  /// In en, this message translates to:
  /// **'Domain Field (Optional)'**
  String get domainField;

  /// No description provided for @integrationChannelsDomainRequiredForInbound.
  ///
  /// In en, this message translates to:
  /// **'Domain selection is required for Inbound channel.'**
  String get integrationChannelsDomainRequiredForInbound;

  /// No description provided for @domainRequiredForInbound.
  ///
  /// In en, this message translates to:
  /// **'Domain selection is required for Inbound channel.'**
  String get domainRequiredForInbound;

  /// No description provided for @integrationChannelsEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit Channel'**
  String get integrationChannelsEdit;

  /// No description provided for @integrationChannelsErrDbUrlRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a DB connection URL.'**
  String get integrationChannelsErrDbUrlRequired;

  /// No description provided for @errDbUrlRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a DB connection URL.'**
  String get errDbUrlRequired;

  /// No description provided for @integrationChannelsErrMqBrokerRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a broker URL.'**
  String get integrationChannelsErrMqBrokerRequired;

  /// No description provided for @errMqBrokerRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a broker URL.'**
  String get errMqBrokerRequired;

  /// No description provided for @integrationChannelsErrTestConnection.
  ///
  /// In en, this message translates to:
  /// **'Network/Server error occurred during connection test.'**
  String get integrationChannelsErrTestConnection;

  /// No description provided for @errTestConnection.
  ///
  /// In en, this message translates to:
  /// **'Network/Server error occurred during connection test.'**
  String get errTestConnection;

  /// No description provided for @integrationChannelsErrWsUrlRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter an endpoint URL.'**
  String get integrationChannelsErrWsUrlRequired;

  /// No description provided for @errWsUrlRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter an endpoint URL.'**
  String get errWsUrlRequired;

  /// No description provided for @integrationChannelsFieldMapping.
  ///
  /// In en, this message translates to:
  /// **'Data Field Mapping'**
  String get integrationChannelsFieldMapping;

  /// No description provided for @fieldMapping.
  ///
  /// In en, this message translates to:
  /// **'Data Field Mapping'**
  String get fieldMapping;

  /// No description provided for @integrationChannelsGenerateToken.
  ///
  /// In en, this message translates to:
  /// **'Generate Token'**
  String get integrationChannelsGenerateToken;

  /// No description provided for @generateToken.
  ///
  /// In en, this message translates to:
  /// **'Generate Token'**
  String get generateToken;

  /// No description provided for @integrationChannelsHeaderCopied.
  ///
  /// In en, this message translates to:
  /// **'Auth header copied to clipboard.'**
  String get integrationChannelsHeaderCopied;

  /// No description provided for @headerCopied.
  ///
  /// In en, this message translates to:
  /// **'Auth header copied to clipboard.'**
  String get headerCopied;

  /// No description provided for @integrationChannelsHeaderValueCopied.
  ///
  /// In en, this message translates to:
  /// **'Header value copied to clipboard.'**
  String get integrationChannelsHeaderValueCopied;

  /// No description provided for @headerValueCopied.
  ///
  /// In en, this message translates to:
  /// **'Header value copied to clipboard.'**
  String get headerValueCopied;

  /// No description provided for @integrationChannelsInbound.
  ///
  /// In en, this message translates to:
  /// **'Inbound'**
  String get integrationChannelsInbound;

  /// No description provided for @inbound.
  ///
  /// In en, this message translates to:
  /// **'Inbound'**
  String get inbound;

  /// No description provided for @integrationChannelsInboundNotice.
  ///
  /// In en, this message translates to:
  /// **'When an external system sends a POST request with JSON Payload to the Webhook URL below, data will be processed based on the configured mapping rules.'**
  String get integrationChannelsInboundNotice;

  /// No description provided for @inboundNotice.
  ///
  /// In en, this message translates to:
  /// **'When an external system sends a POST request with JSON Payload to the Webhook URL below, data will be processed based on the configured mapping rules.'**
  String get inboundNotice;

  /// No description provided for @integrationChannelsIntegrationDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Integration Details'**
  String get integrationChannelsIntegrationDetailTitle;

  /// No description provided for @integrationDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Integration Details'**
  String get integrationDetailTitle;

  /// No description provided for @integrationChannelsIsActive.
  ///
  /// In en, this message translates to:
  /// **'Is Active'**
  String get integrationChannelsIsActive;

  /// No description provided for @integrationChannelsJsonCopied.
  ///
  /// In en, this message translates to:
  /// **'Sample JSON payload copied to clipboard.'**
  String get integrationChannelsJsonCopied;

  /// No description provided for @jsonCopied.
  ///
  /// In en, this message translates to:
  /// **'Sample JSON payload copied to clipboard.'**
  String get jsonCopied;

  /// No description provided for @integrationChannelsManagement.
  ///
  /// In en, this message translates to:
  /// **'Actions'**
  String get integrationChannelsManagement;

  /// No description provided for @management.
  ///
  /// In en, this message translates to:
  /// **'Actions'**
  String get management;

  /// No description provided for @integrationChannelsMappingDesc.
  ///
  /// In en, this message translates to:
  /// **'Freely edit the target field and source expression. Selecting a domain enables the domain field dropdown.'**
  String get integrationChannelsMappingDesc;

  /// No description provided for @mappingDesc.
  ///
  /// In en, this message translates to:
  /// **'Freely edit the target field and source expression. Selecting a domain enables the domain field dropdown.'**
  String get mappingDesc;

  /// No description provided for @integrationChannelsMappingDescInbound.
  ///
  /// In en, this message translates to:
  /// **'Enter Root Path for array processing (e.g. payload[\'data\']). Write expressions based on the single object (#this).'**
  String get integrationChannelsMappingDescInbound;

  /// No description provided for @mappingDescInbound.
  ///
  /// In en, this message translates to:
  /// **'Enter Root Path for array processing (e.g. payload[\'data\']). Write expressions based on the single object (#this).'**
  String get mappingDescInbound;

  /// No description provided for @integrationChannelsMappingRootPath.
  ///
  /// In en, this message translates to:
  /// **'Array Data Root Path'**
  String get integrationChannelsMappingRootPath;

  /// No description provided for @mappingRootPath.
  ///
  /// In en, this message translates to:
  /// **'Array Data Root Path'**
  String get mappingRootPath;

  /// No description provided for @integrationChannelsMappingRootPathPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g., payload[\'data\'] or payload.data'**
  String get integrationChannelsMappingRootPathPlaceholder;

  /// No description provided for @mappingRootPathPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g., payload[\'data\'] or payload.data'**
  String get mappingRootPathPlaceholder;

  /// No description provided for @integrationChannelsMqBroker.
  ///
  /// In en, this message translates to:
  /// **'Broker URL'**
  String get integrationChannelsMqBroker;

  /// No description provided for @mqBroker.
  ///
  /// In en, this message translates to:
  /// **'Broker URL'**
  String get mqBroker;

  /// No description provided for @integrationChannelsMqTopic.
  ///
  /// In en, this message translates to:
  /// **'Topic Name'**
  String get integrationChannelsMqTopic;

  /// No description provided for @mqTopic.
  ///
  /// In en, this message translates to:
  /// **'Topic Name'**
  String get mqTopic;

  /// No description provided for @integrationChannelsName.
  ///
  /// In en, this message translates to:
  /// **'Channel Name'**
  String get integrationChannelsName;

  /// No description provided for @integrationChannelsNoHeaders.
  ///
  /// In en, this message translates to:
  /// **'No headers configured.'**
  String get integrationChannelsNoHeaders;

  /// No description provided for @noHeaders.
  ///
  /// In en, this message translates to:
  /// **'No headers configured.'**
  String get noHeaders;

  /// No description provided for @integrationChannelsNodeRequiredForInbound.
  ///
  /// In en, this message translates to:
  /// **'Classification node selection is required for Inbound channel.'**
  String get integrationChannelsNodeRequiredForInbound;

  /// No description provided for @nodeRequiredForInbound.
  ///
  /// In en, this message translates to:
  /// **'Classification node selection is required for Inbound channel.'**
  String get nodeRequiredForInbound;

  /// No description provided for @integrationChannelsOutbound.
  ///
  /// In en, this message translates to:
  /// **'Outbound'**
  String get integrationChannelsOutbound;

  /// No description provided for @outbound.
  ///
  /// In en, this message translates to:
  /// **'Outbound'**
  String get outbound;

  /// No description provided for @integrationChannelsRequiresApproval.
  ///
  /// In en, this message translates to:
  /// **'Requires Approval'**
  String get integrationChannelsRequiresApproval;

  /// No description provided for @requiresApproval.
  ///
  /// In en, this message translates to:
  /// **'Requires Approval'**
  String get requiresApproval;

  /// No description provided for @integrationChannelsSamplePayloadNotice.
  ///
  /// In en, this message translates to:
  /// **'Real-time example request payload generated from configured source expressions & Root Path in the Mapping tab.'**
  String get integrationChannelsSamplePayloadNotice;

  /// No description provided for @samplePayloadNotice.
  ///
  /// In en, this message translates to:
  /// **'Real-time example request payload generated from configured source expressions & Root Path in the Mapping tab.'**
  String get samplePayloadNotice;

  /// No description provided for @integrationChannelsSamplePayloadTitle.
  ///
  /// In en, this message translates to:
  /// **'Request JSON Payload Sample (Real-time Mapping)'**
  String get integrationChannelsSamplePayloadTitle;

  /// No description provided for @samplePayloadTitle.
  ///
  /// In en, this message translates to:
  /// **'Request JSON Payload Sample (Real-time Mapping)'**
  String get samplePayloadTitle;

  /// No description provided for @integrationChannelsSecretToken.
  ///
  /// In en, this message translates to:
  /// **'Secret Token'**
  String get integrationChannelsSecretToken;

  /// No description provided for @secretToken.
  ///
  /// In en, this message translates to:
  /// **'Secret Token'**
  String get secretToken;

  /// No description provided for @integrationChannelsSelectDomain.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get integrationChannelsSelectDomain;

  /// No description provided for @selectDomain.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get selectDomain;

  /// No description provided for @integrationChannelsSelectDomainNode.
  ///
  /// In en, this message translates to:
  /// **'Select Target Domain (Node)'**
  String get integrationChannelsSelectDomainNode;

  /// No description provided for @selectDomainNode.
  ///
  /// In en, this message translates to:
  /// **'Select Target Domain (Node)'**
  String get selectDomainNode;

  /// No description provided for @integrationChannelsSelectNode.
  ///
  /// In en, this message translates to:
  /// **'Select Node (Links only the selected node)'**
  String get integrationChannelsSelectNode;

  /// No description provided for @selectNode.
  ///
  /// In en, this message translates to:
  /// **'Select Node (Links only the selected node)'**
  String get selectNode;

  /// No description provided for @integrationChannelsSourceExpr.
  ///
  /// In en, this message translates to:
  /// **'Source Expression'**
  String get integrationChannelsSourceExpr;

  /// No description provided for @sourceExpr.
  ///
  /// In en, this message translates to:
  /// **'Source Expression'**
  String get sourceExpr;

  /// No description provided for @integrationChannelsSourceFieldInbound.
  ///
  /// In en, this message translates to:
  /// **'External Source Field'**
  String get integrationChannelsSourceFieldInbound;

  /// No description provided for @sourceFieldInbound.
  ///
  /// In en, this message translates to:
  /// **'External Source Field'**
  String get sourceFieldInbound;

  /// No description provided for @integrationChannelsStatus.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get integrationChannelsStatus;

  /// No description provided for @integrationChannelsSystemNotification.
  ///
  /// In en, this message translates to:
  /// **'System Notification'**
  String get integrationChannelsSystemNotification;

  /// No description provided for @integrationChannelsTargetField.
  ///
  /// In en, this message translates to:
  /// **'Target Field'**
  String get integrationChannelsTargetField;

  /// No description provided for @integrationChannelsTargetFieldInbound.
  ///
  /// In en, this message translates to:
  /// **'Internal Domain Field (Target)'**
  String get integrationChannelsTargetFieldInbound;

  /// No description provided for @targetFieldInbound.
  ///
  /// In en, this message translates to:
  /// **'Internal Domain Field (Target)'**
  String get targetFieldInbound;

  /// No description provided for @integrationChannelsTestConnection.
  ///
  /// In en, this message translates to:
  /// **'Test Connection'**
  String get integrationChannelsTestConnection;

  /// No description provided for @testConnection.
  ///
  /// In en, this message translates to:
  /// **'Test Connection'**
  String get testConnection;

  /// No description provided for @integrationChannelsTitle.
  ///
  /// In en, this message translates to:
  /// **'Integration Channels'**
  String get integrationChannelsTitle;

  /// No description provided for @integrationChannelsType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get integrationChannelsType;

  /// No description provided for @type.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get type;

  /// No description provided for @integrationChannelsWebhookCopy.
  ///
  /// In en, this message translates to:
  /// **'Copy URL'**
  String get integrationChannelsWebhookCopy;

  /// No description provided for @webhookCopy.
  ///
  /// In en, this message translates to:
  /// **'Copy URL'**
  String get webhookCopy;

  /// No description provided for @integrationChannelsWebhookUrl.
  ///
  /// In en, this message translates to:
  /// **'Webhook URL'**
  String get integrationChannelsWebhookUrl;

  /// No description provided for @webhookUrl.
  ///
  /// In en, this message translates to:
  /// **'Webhook URL'**
  String get webhookUrl;

  /// No description provided for @integrationChannelsWsMethod.
  ///
  /// In en, this message translates to:
  /// **'HTTP Method'**
  String get integrationChannelsWsMethod;

  /// No description provided for @wsMethod.
  ///
  /// In en, this message translates to:
  /// **'HTTP Method'**
  String get wsMethod;

  /// No description provided for @integrationChannelsWsUrl.
  ///
  /// In en, this message translates to:
  /// **'Endpoint URL'**
  String get integrationChannelsWsUrl;

  /// No description provided for @wsUrl.
  ///
  /// In en, this message translates to:
  /// **'Endpoint URL'**
  String get wsUrl;

  /// No description provided for @channelsAdd.
  ///
  /// In en, this message translates to:
  /// **'Add Channel'**
  String get channelsAdd;

  /// No description provided for @channelsAddField.
  ///
  /// In en, this message translates to:
  /// **'Add Field'**
  String get channelsAddField;

  /// No description provided for @channelsAddHeader.
  ///
  /// In en, this message translates to:
  /// **'Add Header'**
  String get channelsAddHeader;

  /// No description provided for @channelsApprovalDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Approval Details'**
  String get channelsApprovalDetailTitle;

  /// No description provided for @channelsAuthApiKey.
  ///
  /// In en, this message translates to:
  /// **'API Key Header'**
  String get channelsAuthApiKey;

  /// No description provided for @channelsAuthBearer.
  ///
  /// In en, this message translates to:
  /// **'Bearer Token (Recommended)'**
  String get channelsAuthBearer;

  /// No description provided for @channelsAuthHeaderExample.
  ///
  /// In en, this message translates to:
  /// **'Auth Header'**
  String get channelsAuthHeaderExample;

  /// No description provided for @channelsAuthNone.
  ///
  /// In en, this message translates to:
  /// **'None (No Auth)'**
  String get channelsAuthNone;

  /// No description provided for @channelsAuthType.
  ///
  /// In en, this message translates to:
  /// **'Inbound Auth Type'**
  String get channelsAuthType;

  /// No description provided for @channelsAutoMapFields.
  ///
  /// In en, this message translates to:
  /// **'Auto Map Domain Fields (Incl. Multilingual)'**
  String get channelsAutoMapFields;

  /// No description provided for @channelsBasicConfig.
  ///
  /// In en, this message translates to:
  /// **'Basic & Auth Config'**
  String get channelsBasicConfig;

  /// No description provided for @channelsChannelCode.
  ///
  /// In en, this message translates to:
  /// **'Channel Code'**
  String get channelsChannelCode;

  /// No description provided for @channelsChannelName.
  ///
  /// In en, this message translates to:
  /// **'Channel Name'**
  String get channelsChannelName;

  /// No description provided for @channelsConfirmDeleteChannel.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this channel?'**
  String get channelsConfirmDeleteChannel;

  /// No description provided for @channelsCopied.
  ///
  /// In en, this message translates to:
  /// **'Copied to clipboard.'**
  String get channelsCopied;

  /// No description provided for @channelsCopyCurl.
  ///
  /// In en, this message translates to:
  /// **'Copy cURL'**
  String get channelsCopyCurl;

  /// No description provided for @channelsCopyHeader.
  ///
  /// In en, this message translates to:
  /// **'Copy Header'**
  String get channelsCopyHeader;

  /// No description provided for @channelsCopyJson.
  ///
  /// In en, this message translates to:
  /// **'Copy JSON'**
  String get channelsCopyJson;

  /// No description provided for @channelsCopyValue.
  ///
  /// In en, this message translates to:
  /// **'Copy Value'**
  String get channelsCopyValue;

  /// No description provided for @channelsCreatedAt.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get channelsCreatedAt;

  /// No description provided for @channelsCurlCopied.
  ///
  /// In en, this message translates to:
  /// **'Sample cURL command copied to clipboard.'**
  String get channelsCurlCopied;

  /// No description provided for @channelsDbPassword.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get channelsDbPassword;

  /// No description provided for @channelsDbTable.
  ///
  /// In en, this message translates to:
  /// **'Target Table'**
  String get channelsDbTable;

  /// No description provided for @channelsDbUrl.
  ///
  /// In en, this message translates to:
  /// **'DB URL'**
  String get channelsDbUrl;

  /// No description provided for @channelsDbUser.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get channelsDbUser;

  /// No description provided for @channelsDeptRoles.
  ///
  /// In en, this message translates to:
  /// **'Department Roles (Multi-selectable)'**
  String get channelsDeptRoles;

  /// No description provided for @channelsDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage data integration pipelines and interface channel settings with external systems.'**
  String get channelsDesc;

  /// No description provided for @channelsDescription.
  ///
  /// In en, this message translates to:
  /// **'Manage integration channels.'**
  String get channelsDescription;

  /// No description provided for @channelsDetailConfig.
  ///
  /// In en, this message translates to:
  /// **'Channel Configuration'**
  String get channelsDetailConfig;

  /// No description provided for @channelsDirection.
  ///
  /// In en, this message translates to:
  /// **'Direction'**
  String get channelsDirection;

  /// No description provided for @channelsDomainField.
  ///
  /// In en, this message translates to:
  /// **'Domain Field (Optional)'**
  String get channelsDomainField;

  /// No description provided for @channelsDomainRequiredForInbound.
  ///
  /// In en, this message translates to:
  /// **'Domain selection is required for Inbound channel.'**
  String get channelsDomainRequiredForInbound;

  /// No description provided for @channelsEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit Channel'**
  String get channelsEdit;

  /// No description provided for @channelsErrDbUrlRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a DB connection URL.'**
  String get channelsErrDbUrlRequired;

  /// No description provided for @channelsErrMqBrokerRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a broker URL.'**
  String get channelsErrMqBrokerRequired;

  /// No description provided for @channelsErrTestConnection.
  ///
  /// In en, this message translates to:
  /// **'Network/Server error occurred during connection test.'**
  String get channelsErrTestConnection;

  /// No description provided for @channelsErrWsUrlRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter an endpoint URL.'**
  String get channelsErrWsUrlRequired;

  /// No description provided for @channelsFieldMapping.
  ///
  /// In en, this message translates to:
  /// **'Data Field Mapping'**
  String get channelsFieldMapping;

  /// No description provided for @channelsGenerateToken.
  ///
  /// In en, this message translates to:
  /// **'Generate Token'**
  String get channelsGenerateToken;

  /// No description provided for @channelsHeaderCopied.
  ///
  /// In en, this message translates to:
  /// **'Auth header copied to clipboard.'**
  String get channelsHeaderCopied;

  /// No description provided for @channelsHeaderValueCopied.
  ///
  /// In en, this message translates to:
  /// **'Header value copied to clipboard.'**
  String get channelsHeaderValueCopied;

  /// No description provided for @channelsInbound.
  ///
  /// In en, this message translates to:
  /// **'Inbound'**
  String get channelsInbound;

  /// No description provided for @channelsInboundNotice.
  ///
  /// In en, this message translates to:
  /// **'When an external system sends a POST request with JSON Payload to the Webhook URL below, data will be processed based on the configured mapping rules.'**
  String get channelsInboundNotice;

  /// No description provided for @channelsIntegrationDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Integration Details'**
  String get channelsIntegrationDetailTitle;

  /// No description provided for @channelsIsActive.
  ///
  /// In en, this message translates to:
  /// **'Is Active'**
  String get channelsIsActive;

  /// No description provided for @channelsJsonCopied.
  ///
  /// In en, this message translates to:
  /// **'Sample JSON payload copied to clipboard.'**
  String get channelsJsonCopied;

  /// No description provided for @channelsManagement.
  ///
  /// In en, this message translates to:
  /// **'Actions'**
  String get channelsManagement;

  /// No description provided for @channelsMappingDesc.
  ///
  /// In en, this message translates to:
  /// **'Freely edit the target field and source expression. Selecting a domain enables the domain field dropdown.'**
  String get channelsMappingDesc;

  /// No description provided for @channelsMappingDescInbound.
  ///
  /// In en, this message translates to:
  /// **'Enter Root Path for array processing (e.g. payload[\'data\']). Write expressions based on the single object (#this).'**
  String get channelsMappingDescInbound;

  /// No description provided for @channelsMappingRootPath.
  ///
  /// In en, this message translates to:
  /// **'Array Data Root Path'**
  String get channelsMappingRootPath;

  /// No description provided for @channelsMappingRootPathPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g., payload[\'data\'] or payload.data'**
  String get channelsMappingRootPathPlaceholder;

  /// No description provided for @channelsMqBroker.
  ///
  /// In en, this message translates to:
  /// **'Broker URL'**
  String get channelsMqBroker;

  /// No description provided for @channelsMqTopic.
  ///
  /// In en, this message translates to:
  /// **'Topic Name'**
  String get channelsMqTopic;

  /// No description provided for @channelsName.
  ///
  /// In en, this message translates to:
  /// **'Channel Name'**
  String get channelsName;

  /// No description provided for @channelsNoHeaders.
  ///
  /// In en, this message translates to:
  /// **'No headers configured.'**
  String get channelsNoHeaders;

  /// No description provided for @channelsNodeRequiredForInbound.
  ///
  /// In en, this message translates to:
  /// **'Classification node selection is required for Inbound channel.'**
  String get channelsNodeRequiredForInbound;

  /// No description provided for @channelsOutbound.
  ///
  /// In en, this message translates to:
  /// **'Outbound'**
  String get channelsOutbound;

  /// No description provided for @channelsRequiresApproval.
  ///
  /// In en, this message translates to:
  /// **'Requires Approval'**
  String get channelsRequiresApproval;

  /// No description provided for @channelsSamplePayloadNotice.
  ///
  /// In en, this message translates to:
  /// **'Real-time example request payload generated from configured source expressions & Root Path in the Mapping tab.'**
  String get channelsSamplePayloadNotice;

  /// No description provided for @channelsSamplePayloadTitle.
  ///
  /// In en, this message translates to:
  /// **'Request JSON Payload Sample (Real-time Mapping)'**
  String get channelsSamplePayloadTitle;

  /// No description provided for @channelsSecretToken.
  ///
  /// In en, this message translates to:
  /// **'Secret Token'**
  String get channelsSecretToken;

  /// No description provided for @channelsSelectDomain.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get channelsSelectDomain;

  /// No description provided for @channelsSelectDomainNode.
  ///
  /// In en, this message translates to:
  /// **'Select Target Domain (Node)'**
  String get channelsSelectDomainNode;

  /// No description provided for @channelsSelectNode.
  ///
  /// In en, this message translates to:
  /// **'Select Node (Links only the selected node)'**
  String get channelsSelectNode;

  /// No description provided for @channelsSourceExpr.
  ///
  /// In en, this message translates to:
  /// **'Source Expression'**
  String get channelsSourceExpr;

  /// No description provided for @channelsSourceFieldInbound.
  ///
  /// In en, this message translates to:
  /// **'External Source Field'**
  String get channelsSourceFieldInbound;

  /// No description provided for @channelsStatus.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get channelsStatus;

  /// No description provided for @channelsSystemNotification.
  ///
  /// In en, this message translates to:
  /// **'System Notification'**
  String get channelsSystemNotification;

  /// No description provided for @channelsTargetField.
  ///
  /// In en, this message translates to:
  /// **'Target Field'**
  String get channelsTargetField;

  /// No description provided for @channelsTargetFieldInbound.
  ///
  /// In en, this message translates to:
  /// **'Internal Domain Field (Target)'**
  String get channelsTargetFieldInbound;

  /// No description provided for @channelsTestConnection.
  ///
  /// In en, this message translates to:
  /// **'Test Connection'**
  String get channelsTestConnection;

  /// No description provided for @channelsTitle.
  ///
  /// In en, this message translates to:
  /// **'Integration Channels'**
  String get channelsTitle;

  /// No description provided for @channelsType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get channelsType;

  /// No description provided for @channelsWebhookCopy.
  ///
  /// In en, this message translates to:
  /// **'Copy URL'**
  String get channelsWebhookCopy;

  /// No description provided for @channelsWebhookUrl.
  ///
  /// In en, this message translates to:
  /// **'Webhook URL'**
  String get channelsWebhookUrl;

  /// No description provided for @channelsWsMethod.
  ///
  /// In en, this message translates to:
  /// **'HTTP Method'**
  String get channelsWsMethod;

  /// No description provided for @channelsWsUrl.
  ///
  /// In en, this message translates to:
  /// **'Endpoint URL'**
  String get channelsWsUrl;

  /// No description provided for @integrationChannels.
  ///
  /// In en, this message translates to:
  /// **'Integration Channels'**
  String get integrationChannels;

  /// No description provided for @integrationDirection.
  ///
  /// In en, this message translates to:
  /// **'Direction'**
  String get integrationDirection;

  /// No description provided for @integrationHistoryBtn.
  ///
  /// In en, this message translates to:
  /// **'Integration Log'**
  String get integrationHistoryBtn;

  /// No description provided for @integrationLogDetail.
  ///
  /// In en, this message translates to:
  /// **'Integration Monitoring Log Detail'**
  String get integrationLogDetail;

  /// No description provided for @integrationLogInfo.
  ///
  /// In en, this message translates to:
  /// **'Integration Log Details'**
  String get integrationLogInfo;

  /// No description provided for @integrationMappedPayload.
  ///
  /// In en, this message translates to:
  /// **'Mapped Output Payload'**
  String get integrationMappedPayload;

  /// No description provided for @integrationOriginalPayload.
  ///
  /// In en, this message translates to:
  /// **'Original Received Payload'**
  String get integrationOriginalPayload;

  /// No description provided for @integrationReceivedAt.
  ///
  /// In en, this message translates to:
  /// **'Received At'**
  String get integrationReceivedAt;

  /// No description provided for @integrationStatus.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get integrationStatus;

  /// No description provided for @isActiveLabel.
  ///
  /// In en, this message translates to:
  /// **'Active Status'**
  String get isActiveLabel;

  /// No description provided for @isActiveStatus.
  ///
  /// In en, this message translates to:
  /// **'🟢 Active Status'**
  String get isActiveStatus;

  /// No description provided for @koLang.
  ///
  /// In en, this message translates to:
  /// **'Korean'**
  String get koLang;

  /// No description provided for @labelDrafter.
  ///
  /// In en, this message translates to:
  /// **'Drafter'**
  String get labelDrafter;

  /// No description provided for @labelPassword.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get labelPassword;

  /// No description provided for @labelTimezone.
  ///
  /// In en, this message translates to:
  /// **'Timezone'**
  String get labelTimezone;

  /// No description provided for @language.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get language;

  /// No description provided for @lastSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Last Snapshot'**
  String get lastSnapshot;

  /// No description provided for @lineageGuideFlow.
  ///
  /// In en, this message translates to:
  /// **'Pipeline Flow: Source System (Blue) → Version History (Orange) → Golden Master Record (Purple) → Outbound Integration (Green)'**
  String get lineageGuideFlow;

  /// No description provided for @lineageGuideTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Lineage Visualization Guide'**
  String get lineageGuideTitle;

  /// No description provided for @lineageGuideZoom.
  ///
  /// In en, this message translates to:
  /// **'Graph Zoom/Drag: Mouse wheel zoom & click node for detailed diff'**
  String get lineageGuideZoom;

  /// No description provided for @lineageTimelineGuideTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Lineage Node Classification Guide'**
  String get lineageTimelineGuideTitle;

  /// No description provided for @loadingData.
  ///
  /// In en, this message translates to:
  /// **'Loading data...'**
  String get loadingData;

  /// No description provided for @manageMembers.
  ///
  /// In en, this message translates to:
  /// **'Manage Members'**
  String get manageMembers;

  /// No description provided for @manageSectorsGroups.
  ///
  /// In en, this message translates to:
  /// **'Manage Sectors & Groups'**
  String get manageSectorsGroups;

  /// No description provided for @mappedPayloadTitle.
  ///
  /// In en, this message translates to:
  /// **'mapped_payload.json'**
  String get mappedPayloadTitle;

  /// No description provided for @maxValue.
  ///
  /// In en, this message translates to:
  /// **'Max value'**
  String get maxValue;

  /// No description provided for @menuAccessStatistics.
  ///
  /// In en, this message translates to:
  /// **'Menu Access Statistics'**
  String get menuAccessStatistics;

  /// No description provided for @menuIcon.
  ///
  /// In en, this message translates to:
  /// **'Menu Icon'**
  String get menuIcon;

  /// No description provided for @menuManagement.
  ///
  /// In en, this message translates to:
  /// **'Menu Management'**
  String get menuManagement;

  /// No description provided for @menuManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Manage system tree menu structures and role-based access nodes.'**
  String get menuManagementDesc;

  /// No description provided for @mergeAutoSurvivorship.
  ///
  /// In en, this message translates to:
  /// **'Auto Survivorship'**
  String get mergeAutoSurvivorship;

  /// No description provided for @autoSurvivorship.
  ///
  /// In en, this message translates to:
  /// **'Auto Survivorship'**
  String get autoSurvivorship;

  /// No description provided for @mergeCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get mergeCancel;

  /// No description provided for @mergeExecuteMerge.
  ///
  /// In en, this message translates to:
  /// **'Execute Merge'**
  String get mergeExecuteMerge;

  /// No description provided for @executeMerge.
  ///
  /// In en, this message translates to:
  /// **'Execute Merge'**
  String get executeMerge;

  /// No description provided for @mergeFieldComparison.
  ///
  /// In en, this message translates to:
  /// **'Field Comparison'**
  String get mergeFieldComparison;

  /// No description provided for @fieldComparison.
  ///
  /// In en, this message translates to:
  /// **'Field Comparison'**
  String get fieldComparison;

  /// No description provided for @mergeFieldName.
  ///
  /// In en, this message translates to:
  /// **'Field Name'**
  String get mergeFieldName;

  /// No description provided for @fieldName.
  ///
  /// In en, this message translates to:
  /// **'Field Name'**
  String get fieldName;

  /// No description provided for @mergeManualSelect.
  ///
  /// In en, this message translates to:
  /// **'Manual Selection'**
  String get mergeManualSelect;

  /// No description provided for @manualSelect.
  ///
  /// In en, this message translates to:
  /// **'Manual Selection'**
  String get manualSelect;

  /// No description provided for @mergeMergeFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to merge records'**
  String get mergeMergeFail;

  /// No description provided for @mergeFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to merge records'**
  String get mergeFail;

  /// No description provided for @mergeMergeSuccess.
  ///
  /// In en, this message translates to:
  /// **'Records merged successfully'**
  String get mergeMergeSuccess;

  /// No description provided for @mergeSuccess.
  ///
  /// In en, this message translates to:
  /// **'Records merged successfully'**
  String get mergeSuccess;

  /// No description provided for @mergeMergedRecords.
  ///
  /// In en, this message translates to:
  /// **'Merged Records'**
  String get mergeMergedRecords;

  /// No description provided for @mergedRecords.
  ///
  /// In en, this message translates to:
  /// **'Merged Records'**
  String get mergedRecords;

  /// No description provided for @mergePreview.
  ///
  /// In en, this message translates to:
  /// **'Merge Result Preview'**
  String get mergePreview;

  /// No description provided for @preview.
  ///
  /// In en, this message translates to:
  /// **'Merge Result Preview'**
  String get preview;

  /// No description provided for @mergeSelectedValue.
  ///
  /// In en, this message translates to:
  /// **'Selected Value'**
  String get mergeSelectedValue;

  /// No description provided for @selectedValue.
  ///
  /// In en, this message translates to:
  /// **'Selected Value'**
  String get selectedValue;

  /// No description provided for @mergeSource.
  ///
  /// In en, this message translates to:
  /// **'Source'**
  String get mergeSource;

  /// No description provided for @source.
  ///
  /// In en, this message translates to:
  /// **'Source'**
  String get source;

  /// No description provided for @mergeSurvivorRecord.
  ///
  /// In en, this message translates to:
  /// **'Survivor Record'**
  String get mergeSurvivorRecord;

  /// No description provided for @survivorRecord.
  ///
  /// In en, this message translates to:
  /// **'Survivor Record'**
  String get survivorRecord;

  /// No description provided for @mergeTitle.
  ///
  /// In en, this message translates to:
  /// **'Merge Records'**
  String get mergeTitle;

  /// No description provided for @mergeUnmerge.
  ///
  /// In en, this message translates to:
  /// **'Unmerge'**
  String get mergeUnmerge;

  /// No description provided for @unmerge.
  ///
  /// In en, this message translates to:
  /// **'Unmerge'**
  String get unmerge;

  /// No description provided for @mergeUnmergeConfirmMsg.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to unmerge this record?'**
  String get mergeUnmergeConfirmMsg;

  /// No description provided for @unmergeConfirmMsg.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to unmerge this record?'**
  String get unmergeConfirmMsg;

  /// No description provided for @mergeUnmergeConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Unmerge'**
  String get mergeUnmergeConfirmTitle;

  /// No description provided for @unmergeConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Unmerge'**
  String get unmergeConfirmTitle;

  /// No description provided for @mergeUnmergeFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to unmerge record'**
  String get mergeUnmergeFail;

  /// No description provided for @unmergeFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to unmerge record'**
  String get unmergeFail;

  /// No description provided for @mergeUnmergeSuccess.
  ///
  /// In en, this message translates to:
  /// **'Record unmerged and restored successfully'**
  String get mergeUnmergeSuccess;

  /// No description provided for @unmergeSuccess.
  ///
  /// In en, this message translates to:
  /// **'Record unmerged and restored successfully'**
  String get unmergeSuccess;

  /// No description provided for @messengerAttachFileTooltip.
  ///
  /// In en, this message translates to:
  /// **'Attach file'**
  String get messengerAttachFileTooltip;

  /// No description provided for @attachfiletooltip.
  ///
  /// In en, this message translates to:
  /// **'Attach file'**
  String get attachfiletooltip;

  /// No description provided for @messengerCalendarTitle.
  ///
  /// In en, this message translates to:
  /// **'Jump to Date'**
  String get messengerCalendarTitle;

  /// No description provided for @calendartitle.
  ///
  /// In en, this message translates to:
  /// **'Jump to Date'**
  String get calendartitle;

  /// No description provided for @messengerCancelBtn.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get messengerCancelBtn;

  /// No description provided for @cancelbtn.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancelbtn;

  /// No description provided for @messengerCloseBtn.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get messengerCloseBtn;

  /// No description provided for @closebtn.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get closebtn;

  /// No description provided for @messengerConfirmBtn.
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get messengerConfirmBtn;

  /// No description provided for @confirmbtn.
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get confirmbtn;

  /// No description provided for @messengerContentLabel.
  ///
  /// In en, this message translates to:
  /// **'Content'**
  String get messengerContentLabel;

  /// No description provided for @contentlabel.
  ///
  /// In en, this message translates to:
  /// **'Content'**
  String get contentlabel;

  /// No description provided for @messengerContextCopy.
  ///
  /// In en, this message translates to:
  /// **'Copy'**
  String get messengerContextCopy;

  /// No description provided for @contextcopy.
  ///
  /// In en, this message translates to:
  /// **'Copy'**
  String get contextcopy;

  /// No description provided for @messengerContextDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get messengerContextDelete;

  /// No description provided for @contextdelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get contextdelete;

  /// No description provided for @messengerContextForward.
  ///
  /// In en, this message translates to:
  /// **'Forward'**
  String get messengerContextForward;

  /// No description provided for @contextforward.
  ///
  /// In en, this message translates to:
  /// **'Forward'**
  String get contextforward;

  /// No description provided for @messengerCopiedToClipboard.
  ///
  /// In en, this message translates to:
  /// **'📋 Copied to clipboard!'**
  String get messengerCopiedToClipboard;

  /// No description provided for @copiedtoclipboard.
  ///
  /// In en, this message translates to:
  /// **'📋 Copied to clipboard!'**
  String get copiedtoclipboard;

  /// No description provided for @messengerCreateBtn.
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get messengerCreateBtn;

  /// No description provided for @createbtn.
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get createbtn;

  /// No description provided for @messengerCreateGroupRoomBtn.
  ///
  /// In en, this message translates to:
  /// **'+ Create Group Room'**
  String get messengerCreateGroupRoomBtn;

  /// No description provided for @creategrouproombtn.
  ///
  /// In en, this message translates to:
  /// **'+ Create Group Room'**
  String get creategrouproombtn;

  /// No description provided for @messengerCreateGroupRoomTitle.
  ///
  /// In en, this message translates to:
  /// **'👥 Create Group Room'**
  String get messengerCreateGroupRoomTitle;

  /// No description provided for @creategrouproomtitle.
  ///
  /// In en, this message translates to:
  /// **'👥 Create Group Room'**
  String get creategrouproomtitle;

  /// No description provided for @messengerCreateRoomTooltip.
  ///
  /// In en, this message translates to:
  /// **'Create Group Room'**
  String get messengerCreateRoomTooltip;

  /// No description provided for @createroomtooltip.
  ///
  /// In en, this message translates to:
  /// **'Create Group Room'**
  String get createroomtooltip;

  /// No description provided for @messengerCreatorBadge.
  ///
  /// In en, this message translates to:
  /// **'Host'**
  String get messengerCreatorBadge;

  /// No description provided for @creatorbadge.
  ///
  /// In en, this message translates to:
  /// **'Host'**
  String get creatorbadge;

  /// No description provided for @messengerDeptCol.
  ///
  /// In en, this message translates to:
  /// **'Department'**
  String get messengerDeptCol;

  /// No description provided for @deptcol.
  ///
  /// In en, this message translates to:
  /// **'Department'**
  String get deptcol;

  /// No description provided for @messengerDay.
  ///
  /// In en, this message translates to:
  /// **''**
  String get messengerDay;

  /// No description provided for @day.
  ///
  /// In en, this message translates to:
  /// **''**
  String get day;

  /// No description provided for @messengerDayFri.
  ///
  /// In en, this message translates to:
  /// **'Friday'**
  String get messengerDayFri;

  /// No description provided for @dayfri.
  ///
  /// In en, this message translates to:
  /// **'Friday'**
  String get dayfri;

  /// No description provided for @messengerDayMon.
  ///
  /// In en, this message translates to:
  /// **'Monday'**
  String get messengerDayMon;

  /// No description provided for @daymon.
  ///
  /// In en, this message translates to:
  /// **'Monday'**
  String get daymon;

  /// No description provided for @messengerDaySat.
  ///
  /// In en, this message translates to:
  /// **'Saturday'**
  String get messengerDaySat;

  /// No description provided for @daysat.
  ///
  /// In en, this message translates to:
  /// **'Saturday'**
  String get daysat;

  /// No description provided for @messengerDaySun.
  ///
  /// In en, this message translates to:
  /// **'Sunday'**
  String get messengerDaySun;

  /// No description provided for @daysun.
  ///
  /// In en, this message translates to:
  /// **'Sunday'**
  String get daysun;

  /// No description provided for @messengerDayThu.
  ///
  /// In en, this message translates to:
  /// **'Thursday'**
  String get messengerDayThu;

  /// No description provided for @daythu.
  ///
  /// In en, this message translates to:
  /// **'Thursday'**
  String get daythu;

  /// No description provided for @messengerDayTue.
  ///
  /// In en, this message translates to:
  /// **'Tuesday'**
  String get messengerDayTue;

  /// No description provided for @daytue.
  ///
  /// In en, this message translates to:
  /// **'Tuesday'**
  String get daytue;

  /// No description provided for @messengerDayWed.
  ///
  /// In en, this message translates to:
  /// **'Wednesday'**
  String get messengerDayWed;

  /// No description provided for @daywed.
  ///
  /// In en, this message translates to:
  /// **'Wednesday'**
  String get daywed;

  /// No description provided for @messengerDownloadFile.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get messengerDownloadFile;

  /// No description provided for @downloadfile.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get downloadfile;

  /// No description provided for @messengerForwardTitle.
  ///
  /// In en, this message translates to:
  /// **'↗️ Forward Message'**
  String get messengerForwardTitle;

  /// No description provided for @forwardtitle.
  ///
  /// In en, this message translates to:
  /// **'↗️ Forward Message'**
  String get forwardtitle;

  /// No description provided for @messengerForwardedFilePrefix.
  ///
  /// In en, this message translates to:
  /// **'[Forwarded File]'**
  String get messengerForwardedFilePrefix;

  /// No description provided for @forwardedfileprefix.
  ///
  /// In en, this message translates to:
  /// **'[Forwarded File]'**
  String get forwardedfileprefix;

  /// No description provided for @messengerForwardedImgPrefix.
  ///
  /// In en, this message translates to:
  /// **'[Forwarded Image]'**
  String get messengerForwardedImgPrefix;

  /// No description provided for @forwardedimgprefix.
  ///
  /// In en, this message translates to:
  /// **'[Forwarded Image]'**
  String get forwardedimgprefix;

  /// No description provided for @messengerInviteUserBtn.
  ///
  /// In en, this message translates to:
  /// **'+ Invite User'**
  String get messengerInviteUserBtn;

  /// No description provided for @inviteuserbtn.
  ///
  /// In en, this message translates to:
  /// **'+ Invite User'**
  String get inviteuserbtn;

  /// No description provided for @messengerInviteModalTitle.
  ///
  /// In en, this message translates to:
  /// **'🤝 Invite Users to Room'**
  String get messengerInviteModalTitle;

  /// No description provided for @invitemodaltitle.
  ///
  /// In en, this message translates to:
  /// **'🤝 Invite Users to Room'**
  String get invitemodaltitle;

  /// No description provided for @messengerKickUserBtn.
  ///
  /// In en, this message translates to:
  /// **'Kick'**
  String get messengerKickUserBtn;

  /// No description provided for @kickuserbtn.
  ///
  /// In en, this message translates to:
  /// **'Kick'**
  String get kickuserbtn;

  /// No description provided for @messengerKickConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'🚪 Kick Participant'**
  String get messengerKickConfirmTitle;

  /// No description provided for @kickconfirmtitle.
  ///
  /// In en, this message translates to:
  /// **'🚪 Kick Participant'**
  String get kickconfirmtitle;

  /// No description provided for @messengerKickConfirmDesc.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to kick this participant from the room?'**
  String get messengerKickConfirmDesc;

  /// No description provided for @kickconfirmdesc.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to kick this participant from the room?'**
  String get kickconfirmdesc;

  /// No description provided for @messengerOnlineStatus.
  ///
  /// In en, this message translates to:
  /// **'Online'**
  String get messengerOnlineStatus;

  /// No description provided for @onlinestatus.
  ///
  /// In en, this message translates to:
  /// **'Online'**
  String get onlinestatus;

  /// No description provided for @messengerOrgCol.
  ///
  /// In en, this message translates to:
  /// **'Organization'**
  String get messengerOrgCol;

  /// No description provided for @orgcol.
  ///
  /// In en, this message translates to:
  /// **'Organization'**
  String get orgcol;

  /// No description provided for @messengerPastMessageOptionTitle.
  ///
  /// In en, this message translates to:
  /// **'Past Message Visibility'**
  String get messengerPastMessageOptionTitle;

  /// No description provided for @pastmessageoptiontitle.
  ///
  /// In en, this message translates to:
  /// **'Past Message Visibility'**
  String get pastmessageoptiontitle;

  /// No description provided for @messengerPastMessageNone.
  ///
  /// In en, this message translates to:
  /// **'Hidden (Default)'**
  String get messengerPastMessageNone;

  /// No description provided for @pastmessagenone.
  ///
  /// In en, this message translates to:
  /// **'Hidden (Default)'**
  String get pastmessagenone;

  /// No description provided for @messengerPastMessage1h.
  ///
  /// In en, this message translates to:
  /// **'Last 1 hour'**
  String get messengerPastMessage1h;

  /// No description provided for @pastmessage1h.
  ///
  /// In en, this message translates to:
  /// **'Last 1 hour'**
  String get pastmessage1h;

  /// No description provided for @messengerPastMessage24h.
  ///
  /// In en, this message translates to:
  /// **'Last 24 hours'**
  String get messengerPastMessage24h;

  /// No description provided for @pastmessage24h.
  ///
  /// In en, this message translates to:
  /// **'Last 24 hours'**
  String get pastmessage24h;

  /// No description provided for @messengerPastMessage48h.
  ///
  /// In en, this message translates to:
  /// **'Last 48 hours'**
  String get messengerPastMessage48h;

  /// No description provided for @pastmessage48h.
  ///
  /// In en, this message translates to:
  /// **'Last 48 hours'**
  String get pastmessage48h;

  /// No description provided for @messengerForwardedPrefix.
  ///
  /// In en, this message translates to:
  /// **'[Forwarded Message]'**
  String get messengerForwardedPrefix;

  /// No description provided for @forwardedprefix.
  ///
  /// In en, this message translates to:
  /// **'[Forwarded Message]'**
  String get forwardedprefix;

  /// No description provided for @messengerHideTranslation.
  ///
  /// In en, this message translates to:
  /// **'Hide Translation'**
  String get messengerHideTranslation;

  /// No description provided for @hidetranslation.
  ///
  /// In en, this message translates to:
  /// **'Hide Translation'**
  String get hidetranslation;

  /// No description provided for @messengerMeBadge.
  ///
  /// In en, this message translates to:
  /// **'Me'**
  String get messengerMeBadge;

  /// No description provided for @mebadge.
  ///
  /// In en, this message translates to:
  /// **'Me'**
  String get mebadge;

  /// No description provided for @messengerMonth.
  ///
  /// In en, this message translates to:
  /// **' '**
  String get messengerMonth;

  /// No description provided for @month.
  ///
  /// In en, this message translates to:
  /// **' '**
  String get month;

  /// No description provided for @messengerNoDialogue.
  ///
  /// In en, this message translates to:
  /// **'No messages yet.'**
  String get messengerNoDialogue;

  /// No description provided for @nodialogue.
  ///
  /// In en, this message translates to:
  /// **'No messages yet.'**
  String get nodialogue;

  /// No description provided for @messengerNoRooms.
  ///
  /// In en, this message translates to:
  /// **'No active chat rooms.'**
  String get messengerNoRooms;

  /// No description provided for @norooms.
  ///
  /// In en, this message translates to:
  /// **'No active chat rooms.'**
  String get norooms;

  /// No description provided for @messengerNoUserFound.
  ///
  /// In en, this message translates to:
  /// **'No users found.'**
  String get messengerNoUserFound;

  /// No description provided for @nouserfound.
  ///
  /// In en, this message translates to:
  /// **'No users found.'**
  String get nouserfound;

  /// No description provided for @messengerRoomSettings.
  ///
  /// In en, this message translates to:
  /// **'Room Settings'**
  String get messengerRoomSettings;

  /// No description provided for @roomsettings.
  ///
  /// In en, this message translates to:
  /// **'Room Settings'**
  String get roomsettings;

  /// No description provided for @messengerLeaveRoom.
  ///
  /// In en, this message translates to:
  /// **'Leave Room'**
  String get messengerLeaveRoom;

  /// No description provided for @leaveroom.
  ///
  /// In en, this message translates to:
  /// **'Leave Room'**
  String get leaveroom;

  /// No description provided for @messengerDeleteRoom.
  ///
  /// In en, this message translates to:
  /// **'Delete Room'**
  String get messengerDeleteRoom;

  /// No description provided for @deleteroom.
  ///
  /// In en, this message translates to:
  /// **'Delete Room'**
  String get deleteroom;

  /// No description provided for @messengerDelegateCreator.
  ///
  /// In en, this message translates to:
  /// **'Delegate Creator'**
  String get messengerDelegateCreator;

  /// No description provided for @delegatecreator.
  ///
  /// In en, this message translates to:
  /// **'Delegate Creator'**
  String get delegatecreator;

  /// No description provided for @messengerConfirmLeaveTitle.
  ///
  /// In en, this message translates to:
  /// **'Leave Room'**
  String get messengerConfirmLeaveTitle;

  /// No description provided for @confirmleavetitle.
  ///
  /// In en, this message translates to:
  /// **'Leave Room'**
  String get confirmleavetitle;

  /// No description provided for @messengerConfirmLeaveDesc.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to leave this room? You will no longer see the messages.'**
  String get messengerConfirmLeaveDesc;

  /// No description provided for @confirmleavedesc.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to leave this room? You will no longer see the messages.'**
  String get confirmleavedesc;

  /// No description provided for @messengerConfirmDeleteTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Room'**
  String get messengerConfirmDeleteTitle;

  /// No description provided for @confirmdeletetitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Room'**
  String get confirmdeletetitle;

  /// No description provided for @messengerConfirmDeleteDesc.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this room? All messages will be permanently deleted.'**
  String get messengerConfirmDeleteDesc;

  /// No description provided for @confirmdeletedesc.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete this room? All messages will be permanently deleted.'**
  String get confirmdeletedesc;

  /// No description provided for @messengerDelegateCreatorTitle.
  ///
  /// In en, this message translates to:
  /// **'Delegate Creator'**
  String get messengerDelegateCreatorTitle;

  /// No description provided for @delegatecreatortitle.
  ///
  /// In en, this message translates to:
  /// **'Delegate Creator'**
  String get delegatecreatortitle;

  /// No description provided for @messengerDelegateCreatorDesc.
  ///
  /// In en, this message translates to:
  /// **'Select a user to transfer the creator role to.'**
  String get messengerDelegateCreatorDesc;

  /// No description provided for @delegatecreatordesc.
  ///
  /// In en, this message translates to:
  /// **'Select a user to transfer the creator role to.'**
  String get delegatecreatordesc;

  /// Translated from messenger_confirmDelegateCreatorDesc
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delegate the creator role to {username}? This action cannot be undone.'**
  String messengerConfirmDelegateCreatorDesc(Object username);

  /// Translated from confirmDelegateCreatorDesc
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delegate the creator role to {username}? This action cannot be undone.'**
  String confirmdelegatecreatordesc(Object username);

  /// Translated from messenger_system_leave
  ///
  /// In en, this message translates to:
  /// **'{name} has left the room.'**
  String messengerSystemLeave(Object name);

  /// Translated from system_leave
  ///
  /// In en, this message translates to:
  /// **'{name} has left the room.'**
  String systemLeave(Object name);

  /// Translated from messenger_system_join
  ///
  /// In en, this message translates to:
  /// **'{name} was invited to the room.'**
  String messengerSystemJoin(Object name);

  /// Translated from system_join
  ///
  /// In en, this message translates to:
  /// **'{name} was invited to the room.'**
  String systemJoin(Object name);

  /// No description provided for @messengerOfflineStatus.
  ///
  /// In en, this message translates to:
  /// **'Offline'**
  String get messengerOfflineStatus;

  /// No description provided for @offlinestatus.
  ///
  /// In en, this message translates to:
  /// **'Offline'**
  String get offlinestatus;

  /// No description provided for @messengerPlaceholderMsg.
  ///
  /// In en, this message translates to:
  /// **'Type a message or Ctrl+V image...'**
  String get messengerPlaceholderMsg;

  /// No description provided for @placeholdermsg.
  ///
  /// In en, this message translates to:
  /// **'Type a message or Ctrl+V image...'**
  String get placeholdermsg;

  /// No description provided for @messengerRadioBroadcastTab.
  ///
  /// In en, this message translates to:
  /// **'📢 Live Broadcast'**
  String get messengerRadioBroadcastTab;

  /// No description provided for @radiobroadcasttab.
  ///
  /// In en, this message translates to:
  /// **'📢 Live Broadcast'**
  String get radiobroadcasttab;

  /// No description provided for @messengerRadioChannelUrl.
  ///
  /// In en, this message translates to:
  /// **'YouTube Channel / Music URL'**
  String get messengerRadioChannelUrl;

  /// No description provided for @radiochannelurl.
  ///
  /// In en, this message translates to:
  /// **'YouTube Channel / Music URL'**
  String get radiochannelurl;

  /// No description provided for @messengerRadioChannelUrlPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'https://www.youtube.com/@mychannel or YouTube Music profile...'**
  String get messengerRadioChannelUrlPlaceholder;

  /// No description provided for @radiochannelurlplaceholder.
  ///
  /// In en, this message translates to:
  /// **'https://www.youtube.com/@mychannel or YouTube Music profile...'**
  String get radiochannelurlplaceholder;

  /// No description provided for @messengerRadioConfigDesc.
  ///
  /// In en, this message translates to:
  /// **'Sync your personal YouTube Music / YouTube Channel and playlist to easily broadcast with a single click.'**
  String get messengerRadioConfigDesc;

  /// No description provided for @radioconfigdesc.
  ///
  /// In en, this message translates to:
  /// **'Sync your personal YouTube Music / YouTube Channel and playlist to easily broadcast with a single click.'**
  String get radioconfigdesc;

  /// No description provided for @messengerRadioConnectTab.
  ///
  /// In en, this message translates to:
  /// **'🔗 Sync YouTube / Playlist'**
  String get messengerRadioConnectTab;

  /// No description provided for @radioconnecttab.
  ///
  /// In en, this message translates to:
  /// **'🔗 Sync YouTube / Playlist'**
  String get radioconnecttab;

  /// No description provided for @messengerRadioCustomTitleLabel.
  ///
  /// In en, this message translates to:
  /// **'Track / Broadcast Title (Optional)'**
  String get messengerRadioCustomTitleLabel;

  /// No description provided for @radiocustomtitlelabel.
  ///
  /// In en, this message translates to:
  /// **'Track / Broadcast Title (Optional)'**
  String get radiocustomtitlelabel;

  /// No description provided for @messengerRadioCustomTitlePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g.: ☕ Afternoon Healing Lofi BGM'**
  String get messengerRadioCustomTitlePlaceholder;

  /// No description provided for @radiocustomtitleplaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g.: ☕ Afternoon Healing Lofi BGM'**
  String get radiocustomtitleplaceholder;

  /// No description provided for @messengerRadioDjBadge.
  ///
  /// In en, this message translates to:
  /// **'DJ'**
  String get messengerRadioDjBadge;

  /// No description provided for @radiodjbadge.
  ///
  /// In en, this message translates to:
  /// **'DJ'**
  String get radiodjbadge;

  /// No description provided for @messengerRadioDjTitle.
  ///
  /// In en, this message translates to:
  /// **'🎵 DJ Control Panel'**
  String get messengerRadioDjTitle;

  /// No description provided for @radiodjtitle.
  ///
  /// In en, this message translates to:
  /// **'🎵 DJ Control Panel'**
  String get radiodjtitle;

  /// No description provided for @messengerRadioListenBtn.
  ///
  /// In en, this message translates to:
  /// **'Listen to Radio'**
  String get messengerRadioListenBtn;

  /// No description provided for @radiolistenbtn.
  ///
  /// In en, this message translates to:
  /// **'Listen to Radio'**
  String get radiolistenbtn;

  /// No description provided for @messengerRadioMyPlaylistDefault.
  ///
  /// In en, this message translates to:
  /// **'My Synced Playlist'**
  String get messengerRadioMyPlaylistDefault;

  /// No description provided for @radiomyplaylistdefault.
  ///
  /// In en, this message translates to:
  /// **'My Synced Playlist'**
  String get radiomyplaylistdefault;

  /// No description provided for @messengerRadioPlayThisPlaylist.
  ///
  /// In en, this message translates to:
  /// **'Play This Playlist'**
  String get messengerRadioPlayThisPlaylist;

  /// No description provided for @radioplaythisplaylist.
  ///
  /// In en, this message translates to:
  /// **'Play This Playlist'**
  String get radioplaythisplaylist;

  /// No description provided for @messengerRadioPlayingNow.
  ///
  /// In en, this message translates to:
  /// **'Currently Live Broadcasting'**
  String get messengerRadioPlayingNow;

  /// No description provided for @radioplayingnow.
  ///
  /// In en, this message translates to:
  /// **'Currently Live Broadcasting'**
  String get radioplayingnow;

  /// No description provided for @messengerRadioPlaylistTitle.
  ///
  /// In en, this message translates to:
  /// **'Playlist Title'**
  String get messengerRadioPlaylistTitle;

  /// No description provided for @radioplaylisttitle.
  ///
  /// In en, this message translates to:
  /// **'Playlist Title'**
  String get radioplaylisttitle;

  /// No description provided for @messengerRadioPlaylistTitlePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g.: 🎧 My YouTube Music Playlist'**
  String get messengerRadioPlaylistTitlePlaceholder;

  /// No description provided for @radioplaylisttitleplaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g.: 🎧 My YouTube Music Playlist'**
  String get radioplaylisttitleplaceholder;

  /// No description provided for @messengerRadioPlaylistUrl.
  ///
  /// In en, this message translates to:
  /// **'Playlist URL / ID'**
  String get messengerRadioPlaylistUrl;

  /// No description provided for @radioplaylisturl.
  ///
  /// In en, this message translates to:
  /// **'Playlist URL / ID'**
  String get radioplaylisturl;

  /// No description provided for @messengerRadioPlaylistUrlPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'https://www.youtube.com/playlist?list=PL12345... or PL12345'**
  String get messengerRadioPlaylistUrlPlaceholder;

  /// No description provided for @radioplaylisturlplaceholder.
  ///
  /// In en, this message translates to:
  /// **'https://www.youtube.com/playlist?list=PL12345... or PL12345'**
  String get radioplaylisturlplaceholder;

  /// No description provided for @messengerRadioSaveConfig.
  ///
  /// In en, this message translates to:
  /// **'Save YouTube Sync Config'**
  String get messengerRadioSaveConfig;

  /// No description provided for @radiosaveconfig.
  ///
  /// In en, this message translates to:
  /// **'Save YouTube Sync Config'**
  String get radiosaveconfig;

  /// No description provided for @messengerRadioStartBroadcast.
  ///
  /// In en, this message translates to:
  /// **'Start Live Broadcast to All Users'**
  String get messengerRadioStartBroadcast;

  /// No description provided for @radiostartbroadcast.
  ///
  /// In en, this message translates to:
  /// **'Start Live Broadcast to All Users'**
  String get radiostartbroadcast;

  /// No description provided for @messengerRadioStopBroadcast.
  ///
  /// In en, this message translates to:
  /// **'Stop Broadcast'**
  String get messengerRadioStopBroadcast;

  /// No description provided for @radiostopbroadcast.
  ///
  /// In en, this message translates to:
  /// **'Stop Broadcast'**
  String get radiostopbroadcast;

  /// No description provided for @messengerRadioTrackTitle.
  ///
  /// In en, this message translates to:
  /// **'Track Title'**
  String get messengerRadioTrackTitle;

  /// No description provided for @radiotracktitle.
  ///
  /// In en, this message translates to:
  /// **'Track Title'**
  String get radiotracktitle;

  /// No description provided for @messengerRadioUrlPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter audio stream URL...'**
  String get messengerRadioUrlPlaceholder;

  /// No description provided for @radiourlplaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter audio stream URL...'**
  String get radiourlplaceholder;

  /// No description provided for @messengerRoleCol.
  ///
  /// In en, this message translates to:
  /// **'Role'**
  String get messengerRoleCol;

  /// No description provided for @rolecol.
  ///
  /// In en, this message translates to:
  /// **'Role'**
  String get rolecol;

  /// No description provided for @messengerRoomMembersTitle.
  ///
  /// In en, this message translates to:
  /// **'Room Members'**
  String get messengerRoomMembersTitle;

  /// No description provided for @roommemberstitle.
  ///
  /// In en, this message translates to:
  /// **'Room Members'**
  String get roommemberstitle;

  /// No description provided for @messengerRoomNameLabel.
  ///
  /// In en, this message translates to:
  /// **'Room Name'**
  String get messengerRoomNameLabel;

  /// No description provided for @roomnamelabel.
  ///
  /// In en, this message translates to:
  /// **'Room Name'**
  String get roomnamelabel;

  /// No description provided for @messengerSearchUserPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search user (Name, ID)...'**
  String get messengerSearchUserPlaceholder;

  /// No description provided for @searchuserplaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search user (Name, ID)...'**
  String get searchuserplaceholder;

  /// No description provided for @messengerSelectUsersLabel.
  ///
  /// In en, this message translates to:
  /// **'Select Participants:'**
  String get messengerSelectUsersLabel;

  /// No description provided for @selectuserslabel.
  ///
  /// In en, this message translates to:
  /// **'Select Participants:'**
  String get selectuserslabel;

  /// No description provided for @messengerSendBtn.
  ///
  /// In en, this message translates to:
  /// **'Send'**
  String get messengerSendBtn;

  /// No description provided for @sendbtn.
  ///
  /// In en, this message translates to:
  /// **'Send'**
  String get sendbtn;

  /// No description provided for @messengerTitle.
  ///
  /// In en, this message translates to:
  /// **'💬 Messenger'**
  String get messengerTitle;

  /// No description provided for @messengerTranslateMessage.
  ///
  /// In en, this message translates to:
  /// **'Translate Message'**
  String get messengerTranslateMessage;

  /// No description provided for @translatemessage.
  ///
  /// In en, this message translates to:
  /// **'Translate Message'**
  String get translatemessage;

  /// No description provided for @messengerTranslating.
  ///
  /// In en, this message translates to:
  /// **'Translating...'**
  String get messengerTranslating;

  /// No description provided for @translating.
  ///
  /// In en, this message translates to:
  /// **'Translating...'**
  String get translating;

  /// No description provided for @messengerTranslationError.
  ///
  /// In en, this message translates to:
  /// **'Failed to translate message.'**
  String get messengerTranslationError;

  /// No description provided for @translationerror.
  ///
  /// In en, this message translates to:
  /// **'Failed to translate message.'**
  String get translationerror;

  /// No description provided for @messengerUnblockUser.
  ///
  /// In en, this message translates to:
  /// **'Unblock'**
  String get messengerUnblockUser;

  /// No description provided for @unblockuser.
  ///
  /// In en, this message translates to:
  /// **'Unblock'**
  String get unblockuser;

  /// No description provided for @messengerUnreadMessagesDesc.
  ///
  /// In en, this message translates to:
  /// **'unread messages.'**
  String get messengerUnreadMessagesDesc;

  /// No description provided for @unreadmessagesdesc.
  ///
  /// In en, this message translates to:
  /// **'unread messages.'**
  String get unreadmessagesdesc;

  /// No description provided for @messengerUsernameCol.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get messengerUsernameCol;

  /// No description provided for @usernamecol.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get usernamecol;

  /// No description provided for @messengerViewMembersTooltip.
  ///
  /// In en, this message translates to:
  /// **'View Members'**
  String get messengerViewMembersTooltip;

  /// No description provided for @viewmemberstooltip.
  ///
  /// In en, this message translates to:
  /// **'View Members'**
  String get viewmemberstooltip;

  /// No description provided for @messengerWriterLabel.
  ///
  /// In en, this message translates to:
  /// **'Author'**
  String get messengerWriterLabel;

  /// No description provided for @writerlabel.
  ///
  /// In en, this message translates to:
  /// **'Author'**
  String get writerlabel;

  /// No description provided for @messengerYear.
  ///
  /// In en, this message translates to:
  /// **', '**
  String get messengerYear;

  /// No description provided for @year.
  ///
  /// In en, this message translates to:
  /// **', '**
  String get year;

  /// No description provided for @minValue.
  ///
  /// In en, this message translates to:
  /// **'Min Value'**
  String get minValue;

  /// No description provided for @modified.
  ///
  /// In en, this message translates to:
  /// **'Modified'**
  String get modified;

  /// No description provided for @multiValue.
  ///
  /// In en, this message translates to:
  /// **'Multi-Value'**
  String get multiValue;

  /// No description provided for @multilingual.
  ///
  /// In en, this message translates to:
  /// **'Multilingual'**
  String get multilingual;

  /// No description provided for @myToDoList.
  ///
  /// In en, this message translates to:
  /// **'My To-Do List'**
  String get myToDoList;

  /// No description provided for @newData.
  ///
  /// In en, this message translates to:
  /// **'New Data'**
  String get newData;

  /// No description provided for @nextSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Next Snapshot'**
  String get nextSnapshot;

  /// No description provided for @nocomment.
  ///
  /// In en, this message translates to:
  /// **'No comment'**
  String get nocomment;

  /// No description provided for @noActiveWorkflow.
  ///
  /// In en, this message translates to:
  /// **'No active approval workflow configured for this domain/node.'**
  String get noActiveWorkflow;

  /// No description provided for @noAffectedChannels.
  ///
  /// In en, this message translates to:
  /// **'No active integration channels linked'**
  String get noAffectedChannels;

  /// No description provided for @noAssignedMembers.
  ///
  /// In en, this message translates to:
  /// **'No members currently assigned to this department. Search and register members below.'**
  String get noAssignedMembers;

  /// No description provided for @noChangeContent.
  ///
  /// In en, this message translates to:
  /// **'No Changes'**
  String get noChangeContent;

  /// No description provided for @noChangesFound.
  ///
  /// In en, this message translates to:
  /// **'No changes found.'**
  String get noChangesFound;

  /// No description provided for @noDiffOrInitialVersion.
  ///
  /// In en, this message translates to:
  /// **'No changes found or this is the initial version.'**
  String get noDiffOrInitialVersion;

  /// No description provided for @noDifferencesFound.
  ///
  /// In en, this message translates to:
  /// **'No differences found between selected records.'**
  String get noDifferencesFound;

  /// No description provided for @noHistoryData.
  ///
  /// In en, this message translates to:
  /// **'No history data available.'**
  String get noHistoryData;

  /// No description provided for @noPendingTasksYou.
  ///
  /// In en, this message translates to:
  /// **'No pending tasks. You\\'**
  String get noPendingTasksYou;

  /// No description provided for @noPermission.
  ///
  /// In en, this message translates to:
  /// **'You do not have permission for this action.'**
  String get noPermission;

  /// No description provided for @noTableData.
  ///
  /// In en, this message translates to:
  /// **'No table data available.'**
  String get noTableData;

  /// No description provided for @none.
  ///
  /// In en, this message translates to:
  /// **'None'**
  String get none;

  /// No description provided for @notice.
  ///
  /// In en, this message translates to:
  /// **'Notice'**
  String get notice;

  /// No description provided for @notification.
  ///
  /// In en, this message translates to:
  /// **'Notification'**
  String get notification;

  /// No description provided for @notificationsApprovalFinalized.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Finalized'**
  String get notificationsApprovalFinalized;

  /// No description provided for @approvalFinalized.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Finalized'**
  String get approvalFinalized;

  /// No description provided for @notificationsApprovalPending.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Pending'**
  String get notificationsApprovalPending;

  /// No description provided for @approvalPending.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Pending'**
  String get approvalPending;

  /// No description provided for @notificationsApprovalRejected.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Rejected'**
  String get notificationsApprovalRejected;

  /// No description provided for @approvalRejected.
  ///
  /// In en, this message translates to:
  /// **'Approval Request Rejected'**
  String get approvalRejected;

  /// No description provided for @notificationsApprovalStepApproved.
  ///
  /// In en, this message translates to:
  /// **'Approval Step Approved'**
  String get notificationsApprovalStepApproved;

  /// No description provided for @approvalStepApproved.
  ///
  /// In en, this message translates to:
  /// **'Approval Step Approved'**
  String get approvalStepApproved;

  /// No description provided for @notificationsDeleteAll.
  ///
  /// In en, this message translates to:
  /// **'Delete all'**
  String get notificationsDeleteAll;

  /// No description provided for @deleteAll.
  ///
  /// In en, this message translates to:
  /// **'Delete all'**
  String get deleteAll;

  /// No description provided for @notificationsMarkAllRead.
  ///
  /// In en, this message translates to:
  /// **'Mark all as read'**
  String get notificationsMarkAllRead;

  /// No description provided for @markAllRead.
  ///
  /// In en, this message translates to:
  /// **'Mark All as Read'**
  String get markAllRead;

  /// No description provided for @notificationsNoNotifications.
  ///
  /// In en, this message translates to:
  /// **'No notifications'**
  String get notificationsNoNotifications;

  /// No description provided for @noNotifications.
  ///
  /// In en, this message translates to:
  /// **'No new notifications.'**
  String get noNotifications;

  /// No description provided for @notificationsTitle.
  ///
  /// In en, this message translates to:
  /// **'Notification Center'**
  String get notificationsTitle;

  /// No description provided for @notificationsTypeApproval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get notificationsTypeApproval;

  /// No description provided for @typeApproval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get typeApproval;

  /// No description provided for @notificationsTypeDq.
  ///
  /// In en, this message translates to:
  /// **'Data Quality'**
  String get notificationsTypeDq;

  /// No description provided for @typeDq.
  ///
  /// In en, this message translates to:
  /// **'Data Quality'**
  String get typeDq;

  /// No description provided for @notificationsTypeInfo.
  ///
  /// In en, this message translates to:
  /// **'Info'**
  String get notificationsTypeInfo;

  /// No description provided for @typeInfo.
  ///
  /// In en, this message translates to:
  /// **'Info'**
  String get typeInfo;

  /// No description provided for @notificationsTypeWarning.
  ///
  /// In en, this message translates to:
  /// **'Warning'**
  String get notificationsTypeWarning;

  /// No description provided for @typeWarning.
  ///
  /// In en, this message translates to:
  /// **'Warning'**
  String get typeWarning;

  /// No description provided for @notifiedPersons.
  ///
  /// In en, this message translates to:
  /// **'Notified Persons (CC)'**
  String get notifiedPersons;

  /// No description provided for @number.
  ///
  /// In en, this message translates to:
  /// **'Number'**
  String get number;

  /// No description provided for @onlyDifferences.
  ///
  /// In en, this message translates to:
  /// **'Show Only Differences'**
  String get onlyDifferences;

  /// No description provided for @opBelow.
  ///
  /// In en, this message translates to:
  /// **'below'**
  String get opBelow;

  /// No description provided for @below.
  ///
  /// In en, this message translates to:
  /// **'below'**
  String get below;

  /// No description provided for @opContains.
  ///
  /// In en, this message translates to:
  /// **'Contains'**
  String get opContains;

  /// No description provided for @contains.
  ///
  /// In en, this message translates to:
  /// **'Contains'**
  String get contains;

  /// No description provided for @opEndsWith.
  ///
  /// In en, this message translates to:
  /// **'Ends'**
  String get opEndsWith;

  /// No description provided for @endsWith.
  ///
  /// In en, this message translates to:
  /// **'Ends'**
  String get endsWith;

  /// No description provided for @opEnterKeyword.
  ///
  /// In en, this message translates to:
  /// **'Enter keyword'**
  String get opEnterKeyword;

  /// No description provided for @enterKeyword.
  ///
  /// In en, this message translates to:
  /// **'Enter keyword'**
  String get enterKeyword;

  /// No description provided for @opEnterNumber.
  ///
  /// In en, this message translates to:
  /// **'Enter number'**
  String get opEnterNumber;

  /// No description provided for @enterNumber.
  ///
  /// In en, this message translates to:
  /// **'Enter number'**
  String get enterNumber;

  /// No description provided for @opEq.
  ///
  /// In en, this message translates to:
  /// **'Equals'**
  String get opEq;

  /// No description provided for @eq.
  ///
  /// In en, this message translates to:
  /// **'Equals'**
  String get eq;

  /// No description provided for @opMaxValue.
  ///
  /// In en, this message translates to:
  /// **'Max value'**
  String get opMaxValue;

  /// No description provided for @opRange.
  ///
  /// In en, this message translates to:
  /// **'Range'**
  String get opRange;

  /// No description provided for @range.
  ///
  /// In en, this message translates to:
  /// **'Range'**
  String get range;

  /// No description provided for @opSelectOption.
  ///
  /// In en, this message translates to:
  /// **'Select an option'**
  String get opSelectOption;

  /// No description provided for @selectOption.
  ///
  /// In en, this message translates to:
  /// **'Select an option'**
  String get selectOption;

  /// No description provided for @opStartsWith.
  ///
  /// In en, this message translates to:
  /// **'Starts'**
  String get opStartsWith;

  /// No description provided for @startsWith.
  ///
  /// In en, this message translates to:
  /// **'Starts'**
  String get startsWith;

  /// No description provided for @openTableModal.
  ///
  /// In en, this message translates to:
  /// **'Expand in Large Modal Viewer'**
  String get openTableModal;

  /// No description provided for @openTableModalBtn.
  ///
  /// In en, this message translates to:
  /// **'Expand'**
  String get openTableModalBtn;

  /// No description provided for @openTableModalBtnTitle.
  ///
  /// In en, this message translates to:
  /// **'Open in Large Modal Viewer'**
  String get openTableModalBtnTitle;

  /// No description provided for @otherRequest.
  ///
  /// In en, this message translates to:
  /// **'Other Request'**
  String get otherRequest;

  /// No description provided for @outgoingPayloadTitle.
  ///
  /// In en, this message translates to:
  /// **'outgoing_payload.json'**
  String get outgoingPayloadTitle;

  /// No description provided for @password.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get password;

  /// No description provided for @pasteOptionDesc.
  ///
  /// In en, this message translates to:
  /// **'How would you like to send the clipboard data (Excel table/Text/Image)?'**
  String get pasteOptionDesc;

  /// No description provided for @pasteOptionTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Paste Data Send Format'**
  String get pasteOptionTitle;

  /// No description provided for @path.
  ///
  /// In en, this message translates to:
  /// **'Path'**
  String get path;

  /// No description provided for @pendingFieldApprovalWarning.
  ///
  /// In en, this message translates to:
  /// **'Contains fields with pending change approvals.'**
  String get pendingFieldApprovalWarning;

  /// No description provided for @permActionLabel.
  ///
  /// In en, this message translates to:
  /// **'Action / Identifier (e.g. export, execute)'**
  String get permActionLabel;

  /// No description provided for @permAll.
  ///
  /// In en, this message translates to:
  /// **'All'**
  String get permAll;

  /// No description provided for @permLabelEnLabel.
  ///
  /// In en, this message translates to:
  /// **'Permission English Label (e.g. Export)'**
  String get permLabelEnLabel;

  /// No description provided for @permLabelLabel.
  ///
  /// In en, this message translates to:
  /// **'Permission Korean Label (e.g. 내보내기)'**
  String get permLabelLabel;

  /// No description provided for @permRead.
  ///
  /// In en, this message translates to:
  /// **'Read'**
  String get permRead;

  /// No description provided for @permWrite.
  ///
  /// In en, this message translates to:
  /// **'Write'**
  String get permWrite;

  /// No description provided for @permissions.
  ///
  /// In en, this message translates to:
  /// **'Permissions'**
  String get permissions;

  /// No description provided for @permissionsMatrixTitle.
  ///
  /// In en, this message translates to:
  /// **'Permissions Matrix'**
  String get permissionsMatrixTitle;

  /// No description provided for @placeholderPassword.
  ///
  /// In en, this message translates to:
  /// **'Enter your password'**
  String get placeholderPassword;

  /// No description provided for @placeholderTimezone.
  ///
  /// In en, this message translates to:
  /// **'Select your timezone'**
  String get placeholderTimezone;

  /// No description provided for @prevSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Prev Snapshot'**
  String get prevSnapshot;

  /// No description provided for @proceedAnyway.
  ///
  /// In en, this message translates to:
  /// **'Proceed with Risk'**
  String get proceedAnyway;

  /// No description provided for @processDate.
  ///
  /// In en, this message translates to:
  /// **'Process Date'**
  String get processDate;

  /// No description provided for @processedBy.
  ///
  /// In en, this message translates to:
  /// **'Processed By'**
  String get processedBy;

  /// No description provided for @processor.
  ///
  /// In en, this message translates to:
  /// **'Processor'**
  String get processor;

  /// No description provided for @propertyName.
  ///
  /// In en, this message translates to:
  /// **'Property'**
  String get propertyName;

  /// No description provided for @proxyapprove.
  ///
  /// In en, this message translates to:
  /// **'Proxy Approve'**
  String get proxyapprove;

  /// No description provided for @proxyapproveconfirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm proxy approve?'**
  String get proxyapproveconfirm;

  /// No description provided for @proxyapprovefail.
  ///
  /// In en, this message translates to:
  /// **'Failed to proxy approve.'**
  String get proxyapprovefail;

  /// No description provided for @proxyreject.
  ///
  /// In en, this message translates to:
  /// **'Proxy Reject'**
  String get proxyreject;

  /// No description provided for @proxyrejectconfirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm proxy reject?'**
  String get proxyrejectconfirm;

  /// No description provided for @proxyrejectfail.
  ///
  /// In en, this message translates to:
  /// **'Failed to proxy reject.'**
  String get proxyrejectfail;

  /// No description provided for @rawData.
  ///
  /// In en, this message translates to:
  /// **'Raw Data'**
  String get rawData;

  /// No description provided for @readOnly.
  ///
  /// In en, this message translates to:
  /// **'Read-Only'**
  String get readOnly;

  /// No description provided for @readingPreviousData.
  ///
  /// In en, this message translates to:
  /// **'Reading previous data. (Read-only)'**
  String get readingPreviousData;

  /// No description provided for @readonlySnapshotMsg.
  ///
  /// In en, this message translates to:
  /// **'Viewing previous data snapshot. (Read-only)'**
  String get readonlySnapshotMsg;

  /// No description provided for @reflectDate.
  ///
  /// In en, this message translates to:
  /// **'Reflect Date'**
  String get reflectDate;

  /// No description provided for @remove.
  ///
  /// In en, this message translates to:
  /// **'Remove'**
  String get remove;

  /// No description provided for @removeSelected.
  ///
  /// In en, this message translates to:
  /// **'Remove Selected'**
  String get removeSelected;

  /// No description provided for @reqId.
  ///
  /// In en, this message translates to:
  /// **'Req ID'**
  String get reqId;

  /// No description provided for @requesttype.
  ///
  /// In en, this message translates to:
  /// **'Request Type'**
  String get requesttype;

  /// No description provided for @required.
  ///
  /// In en, this message translates to:
  /// **'Required'**
  String get required;

  /// No description provided for @reset.
  ///
  /// In en, this message translates to:
  /// **'Reset'**
  String get reset;

  /// No description provided for @responseResultTitle.
  ///
  /// In en, this message translates to:
  /// **'response_result.txt'**
  String get responseResultTitle;

  /// No description provided for @retryIntegration.
  ///
  /// In en, this message translates to:
  /// **'Retry Integration'**
  String get retryIntegration;

  /// No description provided for @riskLevel.
  ///
  /// In en, this message translates to:
  /// **'Risk Level'**
  String get riskLevel;

  /// No description provided for @saveChanges.
  ///
  /// In en, this message translates to:
  /// **'Save Changes'**
  String get saveChanges;

  /// No description provided for @saveChangesHint.
  ///
  /// In en, this message translates to:
  /// **'* Click the \'Save\' button at the top or bottom after editing cells to apply changes.'**
  String get saveChangesHint;

  /// No description provided for @schemaChange.
  ///
  /// In en, this message translates to:
  /// **'Schema Change'**
  String get schemaChange;

  /// No description provided for @scopeCol.
  ///
  /// In en, this message translates to:
  /// **'Scope'**
  String get scopeCol;

  /// No description provided for @scopeLevel.
  ///
  /// In en, this message translates to:
  /// **'Scope Level *'**
  String get scopeLevel;

  /// No description provided for @search.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get search;

  /// No description provided for @searchCondition.
  ///
  /// In en, this message translates to:
  /// **'Search Condition'**
  String get searchCondition;

  /// No description provided for @searchFilters.
  ///
  /// In en, this message translates to:
  /// **'Search Filters'**
  String get searchFilters;

  /// No description provided for @searchInTable.
  ///
  /// In en, this message translates to:
  /// **'Search in Table Data...'**
  String get searchInTable;

  /// No description provided for @searchKeyword.
  ///
  /// In en, this message translates to:
  /// **'Search Keyword'**
  String get searchKeyword;

  /// No description provided for @searchWorkflowPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search title or description...'**
  String get searchWorkflowPlaceholder;

  /// No description provided for @searchable.
  ///
  /// In en, this message translates to:
  /// **'Searchable'**
  String get searchable;

  /// No description provided for @sector.
  ///
  /// In en, this message translates to:
  /// **'Sector'**
  String get sector;

  /// No description provided for @select.
  ///
  /// In en, this message translates to:
  /// **'Select'**
  String get select;

  /// No description provided for @selectIconDesc.
  ///
  /// In en, this message translates to:
  /// **'Select a custom icon to display on department nodes and headers:'**
  String get selectIconDesc;

  /// No description provided for @selectIconTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Icon'**
  String get selectIconTitle;

  /// No description provided for @selectMenuPrompt.
  ///
  /// In en, this message translates to:
  /// **'Select a menu from the tree to edit.'**
  String get selectMenuPrompt;

  /// No description provided for @selectedCount.
  ///
  /// In en, this message translates to:
  /// **'Selected'**
  String get selectedCount;

  /// No description provided for @sendAsImage.
  ///
  /// In en, this message translates to:
  /// **'🖼️ Send as Image'**
  String get sendAsImage;

  /// No description provided for @sendAsTextData.
  ///
  /// In en, this message translates to:
  /// **'📋 Send as Text/Table Data'**
  String get sendAsTextData;

  /// No description provided for @setAsBaseline.
  ///
  /// In en, this message translates to:
  /// **'Set as Baseline'**
  String get setAsBaseline;

  /// No description provided for @setDefaultWorkflowDesc.
  ///
  /// In en, this message translates to:
  /// **'⭐ Set as Default Form for this Action'**
  String get setDefaultWorkflowDesc;

  /// No description provided for @showRawData.
  ///
  /// In en, this message translates to:
  /// **'Show Raw Unmodified Data'**
  String get showRawData;

  /// No description provided for @snapshotViewingNotice.
  ///
  /// In en, this message translates to:
  /// **'Viewing historic data snapshot. (Read Only)'**
  String get snapshotViewingNotice;

  /// No description provided for @startDate.
  ///
  /// In en, this message translates to:
  /// **'Start Date'**
  String get startDate;

  /// No description provided for @statusActive.
  ///
  /// In en, this message translates to:
  /// **'🟢 Active'**
  String get statusActive;

  /// No description provided for @statusCol.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get statusCol;

  /// No description provided for @statusFilter.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get statusFilter;

  /// No description provided for @statusIgnored.
  ///
  /// In en, this message translates to:
  /// **'Keep Separate'**
  String get statusIgnored;

  /// No description provided for @statusInactive.
  ///
  /// In en, this message translates to:
  /// **'🔴 Inactive'**
  String get statusInactive;

  /// No description provided for @statusMerged.
  ///
  /// In en, this message translates to:
  /// **'Merged'**
  String get statusMerged;

  /// No description provided for @stepdraft.
  ///
  /// In en, this message translates to:
  /// **'Submitted'**
  String get stepdraft;

  /// No description provided for @steppending.
  ///
  /// In en, this message translates to:
  /// **'Pending'**
  String get steppending;

  /// No description provided for @stepConsensus.
  ///
  /// In en, this message translates to:
  /// **'Consensus'**
  String get stepConsensus;

  /// No description provided for @stepNameEnPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Step Title (EN)'**
  String get stepNameEnPlaceholder;

  /// No description provided for @stepNameKoPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Step Title (KO)'**
  String get stepNameKoPlaceholder;

  /// No description provided for @stepNamePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Step Name (e.g. 1st Data Review)'**
  String get stepNamePlaceholder;

  /// No description provided for @stepPrefix.
  ///
  /// In en, this message translates to:
  /// **'Step'**
  String get stepPrefix;

  /// No description provided for @stepTypeConsultation.
  ///
  /// In en, this message translates to:
  /// **'Consultation'**
  String get stepTypeConsultation;

  /// Translated from steps_count
  ///
  /// In en, this message translates to:
  /// **'{count} Step Approval'**
  String stepsCount(Object count);

  /// No description provided for @submissionCommentNotice.
  ///
  /// In en, this message translates to:
  /// **'(Optional) Enter a reason or comment for the approver.'**
  String get submissionCommentNotice;

  /// No description provided for @submissionCommentPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter your comment for the approver...'**
  String get submissionCommentPlaceholder;

  /// No description provided for @submissionCommentTitle.
  ///
  /// In en, this message translates to:
  /// **'Submission Comment'**
  String get submissionCommentTitle;

  /// No description provided for @submissionDate.
  ///
  /// In en, this message translates to:
  /// **'Submission Date'**
  String get submissionDate;

  /// No description provided for @submissionReason.
  ///
  /// In en, this message translates to:
  /// **'Submission Reason'**
  String get submissionReason;

  /// No description provided for @successDelete.
  ///
  /// In en, this message translates to:
  /// **'Successfully deleted.'**
  String get successDelete;

  /// No description provided for @successSave.
  ///
  /// In en, this message translates to:
  /// **'Successfully saved.'**
  String get successSave;

  /// No description provided for @survivorshipAddFirstRule.
  ///
  /// In en, this message translates to:
  /// **'Add First Rule'**
  String get survivorshipAddFirstRule;

  /// No description provided for @survivorshipAddRule.
  ///
  /// In en, this message translates to:
  /// **'Add Rule'**
  String get survivorshipAddRule;

  /// No description provided for @survivorshipDescription.
  ///
  /// In en, this message translates to:
  /// **'Configure field-level survival priority and conflict resolution rules for golden record creation during merge.'**
  String get survivorshipDescription;

  /// No description provided for @survivorshipEmptyNoDomain.
  ///
  /// In en, this message translates to:
  /// **'No Domain Selected'**
  String get survivorshipEmptyNoDomain;

  /// No description provided for @emptyNoDomain.
  ///
  /// In en, this message translates to:
  /// **'No Domain Selected'**
  String get emptyNoDomain;

  /// No description provided for @survivorshipEmptyNoRules.
  ///
  /// In en, this message translates to:
  /// **'No survivorship rules configured.'**
  String get survivorshipEmptyNoRules;

  /// No description provided for @survivorshipEmptySubDomain.
  ///
  /// In en, this message translates to:
  /// **'Select a domain from the dropdown above to view survivorship rules.'**
  String get survivorshipEmptySubDomain;

  /// No description provided for @emptySubDomain.
  ///
  /// In en, this message translates to:
  /// **'Select a domain from the dropdown above to view survivorship rules.'**
  String get emptySubDomain;

  /// No description provided for @survivorshipEmptySubRules.
  ///
  /// In en, this message translates to:
  /// **'Click \'+ Add Rule\' on the top right to configure field merging rules.'**
  String get survivorshipEmptySubRules;

  /// No description provided for @emptySubRules.
  ///
  /// In en, this message translates to:
  /// **'Click \'+ Add Rule\' on the top right to configure field merging rules.'**
  String get emptySubRules;

  /// No description provided for @survivorshipFieldKey.
  ///
  /// In en, this message translates to:
  /// **'Domain Field (Field Key)'**
  String get survivorshipFieldKey;

  /// No description provided for @fieldKey.
  ///
  /// In en, this message translates to:
  /// **'Field Key'**
  String get fieldKey;

  /// No description provided for @survivorshipGuideText.
  ///
  /// In en, this message translates to:
  /// **'Configure optimal merge rules for each domain among SOURCE_PRIORITY, MOST_RECENT, and MOST_COMPLETE.'**
  String get survivorshipGuideText;

  /// No description provided for @guideText.
  ///
  /// In en, this message translates to:
  /// **'Configure optimal merge rules for each domain among SOURCE_PRIORITY, MOST_RECENT, and MOST_COMPLETE.'**
  String get guideText;

  /// No description provided for @survivorshipGuideTitle.
  ///
  /// In en, this message translates to:
  /// **'Survivorship Strategy Guide'**
  String get survivorshipGuideTitle;

  /// No description provided for @guideTitle.
  ///
  /// In en, this message translates to:
  /// **'Survivorship Strategy Guide'**
  String get guideTitle;

  /// No description provided for @survivorshipItemsCount.
  ///
  /// In en, this message translates to:
  /// **' items'**
  String get survivorshipItemsCount;

  /// No description provided for @survivorshipKpiDomain.
  ///
  /// In en, this message translates to:
  /// **'Domain:'**
  String get survivorshipKpiDomain;

  /// No description provided for @kpiDomain.
  ///
  /// In en, this message translates to:
  /// **'Domain:'**
  String get kpiDomain;

  /// No description provided for @survivorshipKpiFields.
  ///
  /// In en, this message translates to:
  /// **'Domain Fields:'**
  String get survivorshipKpiFields;

  /// No description provided for @kpiFields.
  ///
  /// In en, this message translates to:
  /// **'Domain Fields:'**
  String get kpiFields;

  /// No description provided for @survivorshipKpiRules.
  ///
  /// In en, this message translates to:
  /// **'Rules:'**
  String get survivorshipKpiRules;

  /// No description provided for @kpiRules.
  ///
  /// In en, this message translates to:
  /// **'Rules:'**
  String get kpiRules;

  /// No description provided for @survivorshipLoadDomainsFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to load domain list.'**
  String get survivorshipLoadDomainsFail;

  /// No description provided for @loadDomainsFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to load domain list.'**
  String get loadDomainsFail;

  /// No description provided for @survivorshipMostComplete.
  ///
  /// In en, this message translates to:
  /// **'MOST_COMPLETE (Highest Completeness)'**
  String get survivorshipMostComplete;

  /// No description provided for @mostComplete.
  ///
  /// In en, this message translates to:
  /// **'MOST_COMPLETE (Highest Completeness)'**
  String get mostComplete;

  /// No description provided for @survivorshipMostRecent.
  ///
  /// In en, this message translates to:
  /// **'MOST_RECENT (Most Recent Timestamp)'**
  String get survivorshipMostRecent;

  /// No description provided for @mostRecent.
  ///
  /// In en, this message translates to:
  /// **'MOST_RECENT (Most Recent Timestamp)'**
  String get mostRecent;

  /// No description provided for @survivorshipPriority.
  ///
  /// In en, this message translates to:
  /// **'Priority'**
  String get survivorshipPriority;

  /// No description provided for @priority.
  ///
  /// In en, this message translates to:
  /// **'Priority'**
  String get priority;

  /// No description provided for @survivorshipRefresh.
  ///
  /// In en, this message translates to:
  /// **'Refresh'**
  String get survivorshipRefresh;

  /// No description provided for @survivorshipRuleList.
  ///
  /// In en, this message translates to:
  /// **'Rule List'**
  String get survivorshipRuleList;

  /// No description provided for @survivorshipSaveFail.
  ///
  /// In en, this message translates to:
  /// **'Error occurred while saving survivorship rules.'**
  String get survivorshipSaveFail;

  /// No description provided for @saveFail.
  ///
  /// In en, this message translates to:
  /// **'Error occurred while saving survivorship rules.'**
  String get saveFail;

  /// No description provided for @survivorshipSaveSettings.
  ///
  /// In en, this message translates to:
  /// **'Save Settings'**
  String get survivorshipSaveSettings;

  /// No description provided for @saveSettings.
  ///
  /// In en, this message translates to:
  /// **'Save Settings'**
  String get saveSettings;

  /// No description provided for @survivorshipSaveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Survivorship rules saved successfully.'**
  String get survivorshipSaveSuccess;

  /// No description provided for @survivorshipSelectDomainPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get survivorshipSelectDomainPlaceholder;

  /// No description provided for @survivorshipSourcePriority.
  ///
  /// In en, this message translates to:
  /// **'SOURCE_PRIORITY (Source System Priority)'**
  String get survivorshipSourcePriority;

  /// No description provided for @sourcePriority.
  ///
  /// In en, this message translates to:
  /// **'SOURCE_PRIORITY (Source System Priority)'**
  String get sourcePriority;

  /// No description provided for @survivorshipStatAvailableFields.
  ///
  /// In en, this message translates to:
  /// **'Available Fields'**
  String get survivorshipStatAvailableFields;

  /// No description provided for @statAvailableFields.
  ///
  /// In en, this message translates to:
  /// **'Available Fields'**
  String get statAvailableFields;

  /// No description provided for @survivorshipStatCurrentDomain.
  ///
  /// In en, this message translates to:
  /// **'Current Domain'**
  String get survivorshipStatCurrentDomain;

  /// No description provided for @statCurrentDomain.
  ///
  /// In en, this message translates to:
  /// **'Current Domain'**
  String get statCurrentDomain;

  /// No description provided for @survivorshipStrategy.
  ///
  /// In en, this message translates to:
  /// **'Survivorship Strategy'**
  String get survivorshipStrategy;

  /// No description provided for @strategy.
  ///
  /// In en, this message translates to:
  /// **'Survivorship Strategy'**
  String get strategy;

  /// No description provided for @survivorshipStrategyDescSourcePriority.
  ///
  /// In en, this message translates to:
  /// **'Prioritizes data field values from the designated source system (e.g., Legacy ERP, CRM).'**
  String get survivorshipStrategyDescSourcePriority;

  /// No description provided for @strategyDescSourcePriority.
  ///
  /// In en, this message translates to:
  /// **'Prioritizes data field values from the designated source system (e.g., Legacy ERP, CRM).'**
  String get strategyDescSourcePriority;

  /// No description provided for @survivorshipStrategyDescMostRecent.
  ///
  /// In en, this message translates to:
  /// **'Adopts the field value of the record that was most recently created or updated.'**
  String get survivorshipStrategyDescMostRecent;

  /// No description provided for @strategyDescMostRecent.
  ///
  /// In en, this message translates to:
  /// **'Adopts the field value of the record that was most recently created or updated.'**
  String get strategyDescMostRecent;

  /// No description provided for @survivorshipStrategyDescMostComplete.
  ///
  /// In en, this message translates to:
  /// **'Adopts the valid field value with the most information and longest data length, not Null.'**
  String get survivorshipStrategyDescMostComplete;

  /// No description provided for @strategyDescMostComplete.
  ///
  /// In en, this message translates to:
  /// **'Adopts the valid field value with the most information and longest data length, not Null.'**
  String get strategyDescMostComplete;

  /// No description provided for @survivorshipTitle.
  ///
  /// In en, this message translates to:
  /// **'Survivorship Rules'**
  String get survivorshipTitle;

  /// No description provided for @syncMenuSeed.
  ///
  /// In en, this message translates to:
  /// **'Sync from Seed File'**
  String get syncMenuSeed;

  /// No description provided for @syntaxErrorFormula.
  ///
  /// In en, this message translates to:
  /// **'Syntax error in formula'**
  String get syntaxErrorFormula;

  /// Translated from syntax_error_in_formula_e_message
  ///
  /// In en, this message translates to:
  /// **'Syntax error in formula: {message}'**
  String syntaxErrorInFormulaEMessage(Object message);

  /// No description provided for @tabWorkflows.
  ///
  /// In en, this message translates to:
  /// **'Workflows'**
  String get tabWorkflows;

  /// No description provided for @tableViewerTitle.
  ///
  /// In en, this message translates to:
  /// **'Messenger Data Table Viewer'**
  String get tableViewerTitle;

  /// No description provided for @targetbulkupload.
  ///
  /// In en, this message translates to:
  /// **'Bulk Upload'**
  String get targetbulkupload;

  /// No description provided for @targetrecordcreate.
  ///
  /// In en, this message translates to:
  /// **'Record Create'**
  String get targetrecordcreate;

  /// No description provided for @targetrecorddelete.
  ///
  /// In en, this message translates to:
  /// **'Record Delete'**
  String get targetrecorddelete;

  /// No description provided for @targetrecordupdate.
  ///
  /// In en, this message translates to:
  /// **'Record Update'**
  String get targetrecordupdate;

  /// No description provided for @targetDomainRefNotLoaded.
  ///
  /// In en, this message translates to:
  /// **'Target domain reference not loaded.'**
  String get targetDomainRefNotLoaded;

  /// No description provided for @text.
  ///
  /// In en, this message translates to:
  /// **'Text'**
  String get text;

  /// No description provided for @time.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get time;

  /// No description provided for @timelineList.
  ///
  /// In en, this message translates to:
  /// **'Timeline List'**
  String get timelineList;

  /// No description provided for @timezone.
  ///
  /// In en, this message translates to:
  /// **'Timezone Settings'**
  String get timezone;

  /// No description provided for @timezoneSelect.
  ///
  /// In en, this message translates to:
  /// **'Select Timezone'**
  String get timezoneSelect;

  /// No description provided for @toValue.
  ///
  /// In en, this message translates to:
  /// **'~ To'**
  String get toValue;

  /// No description provided for @today.
  ///
  /// In en, this message translates to:
  /// **'Today'**
  String get today;

  /// No description provided for @treeEmptyMessage.
  ///
  /// In en, this message translates to:
  /// **'No classification tree found. Please click the Domain button below to create a new domain.'**
  String get treeEmptyMessage;

  /// No description provided for @typeconsensus.
  ///
  /// In en, this message translates to:
  /// **'Consensus'**
  String get typeconsensus;

  /// No description provided for @typedraft.
  ///
  /// In en, this message translates to:
  /// **'Draft'**
  String get typedraft;

  /// No description provided for @unassign.
  ///
  /// In en, this message translates to:
  /// **'Unassign'**
  String get unassign;

  /// No description provided for @unassigned.
  ///
  /// In en, this message translates to:
  /// **'Unassigned'**
  String get unassigned;

  /// No description provided for @unclassified.
  ///
  /// In en, this message translates to:
  /// **'Unclassified'**
  String get unclassified;

  /// No description provided for @unit.
  ///
  /// In en, this message translates to:
  /// **'Unit'**
  String get unit;

  /// No description provided for @unknown.
  ///
  /// In en, this message translates to:
  /// **'Unknown'**
  String get unknown;

  /// No description provided for @unmaskReasonDesc.
  ///
  /// In en, this message translates to:
  /// **'Please enter a reason for accessing the original sensitive data. This reason will be securely recorded in the audit logs.'**
  String get unmaskReasonDesc;

  /// No description provided for @unmaskReasonTitle.
  ///
  /// In en, this message translates to:
  /// **'Enter reason for unmasking'**
  String get unmaskReasonTitle;

  /// No description provided for @unmergeBtn.
  ///
  /// In en, this message translates to:
  /// **'Unmerge'**
  String get unmergeBtn;

  /// No description provided for @update.
  ///
  /// In en, this message translates to:
  /// **'Update'**
  String get update;

  /// No description provided for @updateSuccess.
  ///
  /// In en, this message translates to:
  /// **'Update Completed'**
  String get updateSuccess;

  /// No description provided for @updatedat.
  ///
  /// In en, this message translates to:
  /// **'Updated At'**
  String get updatedat;

  /// No description provided for @notificationCenter.
  ///
  /// In en, this message translates to:
  /// **'Notification Center'**
  String get notificationCenter;

  /// No description provided for @downloadTemplate.
  ///
  /// In en, this message translates to:
  /// **'Download Excel Template'**
  String get downloadTemplate;

  /// No description provided for @exportExcelCsv.
  ///
  /// In en, this message translates to:
  /// **'Export Records (CSV/Excel)'**
  String get exportExcelCsv;

  /// No description provided for @editorBold.
  ///
  /// In en, this message translates to:
  /// **'Bold'**
  String get editorBold;

  /// No description provided for @editorItalic.
  ///
  /// In en, this message translates to:
  /// **'Italic'**
  String get editorItalic;

  /// No description provided for @editorUnderline.
  ///
  /// In en, this message translates to:
  /// **'Underline'**
  String get editorUnderline;

  /// No description provided for @editorStrike.
  ///
  /// In en, this message translates to:
  /// **'Strike'**
  String get editorStrike;

  /// No description provided for @editorHeading1.
  ///
  /// In en, this message translates to:
  /// **'Heading 1 (H1)'**
  String get editorHeading1;

  /// No description provided for @editorHeading2.
  ///
  /// In en, this message translates to:
  /// **'Heading 2 (H2)'**
  String get editorHeading2;

  /// No description provided for @editorHeading3.
  ///
  /// In en, this message translates to:
  /// **'Heading 3 (H3)'**
  String get editorHeading3;

  /// No description provided for @editorParagraph.
  ///
  /// In en, this message translates to:
  /// **'Paragraph'**
  String get editorParagraph;

  /// No description provided for @editorBulletList.
  ///
  /// In en, this message translates to:
  /// **'Bullet List'**
  String get editorBulletList;

  /// No description provided for @editorOrderedList.
  ///
  /// In en, this message translates to:
  /// **'Ordered List'**
  String get editorOrderedList;

  /// No description provided for @editorBlockquote.
  ///
  /// In en, this message translates to:
  /// **'Blockquote'**
  String get editorBlockquote;

  /// No description provided for @editorCodeBlock.
  ///
  /// In en, this message translates to:
  /// **'Code Block'**
  String get editorCodeBlock;

  /// No description provided for @editorAlignLeft.
  ///
  /// In en, this message translates to:
  /// **'Align Left'**
  String get editorAlignLeft;

  /// No description provided for @editorAlignCenter.
  ///
  /// In en, this message translates to:
  /// **'Align Center'**
  String get editorAlignCenter;

  /// No description provided for @editorAlignRight.
  ///
  /// In en, this message translates to:
  /// **'Align Right'**
  String get editorAlignRight;

  /// No description provided for @editorAlignJustify.
  ///
  /// In en, this message translates to:
  /// **'Justify'**
  String get editorAlignJustify;

  /// No description provided for @editorHorizontalRule.
  ///
  /// In en, this message translates to:
  /// **'Horizontal Line'**
  String get editorHorizontalRule;

  /// No description provided for @editorUndo.
  ///
  /// In en, this message translates to:
  /// **'Undo'**
  String get editorUndo;

  /// No description provided for @editorRedo.
  ///
  /// In en, this message translates to:
  /// **'Redo'**
  String get editorRedo;

  /// No description provided for @editorPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter content...'**
  String get editorPlaceholder;

  /// No description provided for @editorImage.
  ///
  /// In en, this message translates to:
  /// **'Insert Image'**
  String get editorImage;

  /// No description provided for @uploadingImage.
  ///
  /// In en, this message translates to:
  /// **'Uploading image...'**
  String get uploadingImage;

  /// No description provided for @failedUploadImage.
  ///
  /// In en, this message translates to:
  /// **'Failed to upload image.'**
  String get failedUploadImage;

  /// No description provided for @uploadImage.
  ///
  /// In en, this message translates to:
  /// **'Upload Image'**
  String get uploadImage;

  /// No description provided for @dragDropImageHint.
  ///
  /// In en, this message translates to:
  /// **'Drag & drop images here or click to browse (Ctrl+V supported)'**
  String get dragDropImageHint;

  /// No description provided for @deleteImage.
  ///
  /// In en, this message translates to:
  /// **'Delete Image'**
  String get deleteImage;

  /// No description provided for @downloadImage.
  ///
  /// In en, this message translates to:
  /// **'Download Image'**
  String get downloadImage;

  /// No description provided for @zoomIn.
  ///
  /// In en, this message translates to:
  /// **'Zoom In'**
  String get zoomIn;

  /// No description provided for @zoomOut.
  ///
  /// In en, this message translates to:
  /// **'Zoom Out'**
  String get zoomOut;

  /// No description provided for @zoomReset.
  ///
  /// In en, this message translates to:
  /// **'Reset Zoom'**
  String get zoomReset;

  /// No description provided for @noImage.
  ///
  /// In en, this message translates to:
  /// **'No image registered.'**
  String get noImage;

  /// No description provided for @imageCarouselPrev.
  ///
  /// In en, this message translates to:
  /// **'Previous Image'**
  String get imageCarouselPrev;

  /// No description provided for @imageCarouselNext.
  ///
  /// In en, this message translates to:
  /// **'Next Image'**
  String get imageCarouselNext;

  /// No description provided for @editorFontFamily.
  ///
  /// In en, this message translates to:
  /// **'Font Family'**
  String get editorFontFamily;

  /// No description provided for @editorFontSize.
  ///
  /// In en, this message translates to:
  /// **'Font Size'**
  String get editorFontSize;

  /// No description provided for @editorTextColor.
  ///
  /// In en, this message translates to:
  /// **'Text Color'**
  String get editorTextColor;

  /// No description provided for @editorHighlight.
  ///
  /// In en, this message translates to:
  /// **'Highlight Color'**
  String get editorHighlight;

  /// No description provided for @editorTable.
  ///
  /// In en, this message translates to:
  /// **'Table'**
  String get editorTable;

  /// No description provided for @editorInsertTable.
  ///
  /// In en, this message translates to:
  /// **'Insert Table (3x3)'**
  String get editorInsertTable;

  /// No description provided for @editorAddRowBefore.
  ///
  /// In en, this message translates to:
  /// **'Add Row Above'**
  String get editorAddRowBefore;

  /// No description provided for @editorAddRowAfter.
  ///
  /// In en, this message translates to:
  /// **'Add Row Below'**
  String get editorAddRowAfter;

  /// No description provided for @editorDeleteRow.
  ///
  /// In en, this message translates to:
  /// **'Delete Row'**
  String get editorDeleteRow;

  /// No description provided for @editorAddColBefore.
  ///
  /// In en, this message translates to:
  /// **'Add Column Left'**
  String get editorAddColBefore;

  /// No description provided for @editorAddColAfter.
  ///
  /// In en, this message translates to:
  /// **'Add Column Right'**
  String get editorAddColAfter;

  /// No description provided for @editorDeleteCol.
  ///
  /// In en, this message translates to:
  /// **'Delete Column'**
  String get editorDeleteCol;

  /// No description provided for @editorMergeCells.
  ///
  /// In en, this message translates to:
  /// **'Merge Cells'**
  String get editorMergeCells;

  /// No description provided for @editorSplitCell.
  ///
  /// In en, this message translates to:
  /// **'Split Cell'**
  String get editorSplitCell;

  /// No description provided for @editorToggleHeaderRow.
  ///
  /// In en, this message translates to:
  /// **'Toggle Header Row'**
  String get editorToggleHeaderRow;

  /// No description provided for @editorDeleteTable.
  ///
  /// In en, this message translates to:
  /// **'Delete Table'**
  String get editorDeleteTable;

  /// No description provided for @editorTaskList.
  ///
  /// In en, this message translates to:
  /// **'Task List (To-Do)'**
  String get editorTaskList;

  /// No description provided for @editorClearFormatting.
  ///
  /// In en, this message translates to:
  /// **'Clear Formatting'**
  String get editorClearFormatting;

  /// No description provided for @editorFullscreen.
  ///
  /// In en, this message translates to:
  /// **'Full Screen'**
  String get editorFullscreen;

  /// No description provided for @editorExitFullscreen.
  ///
  /// In en, this message translates to:
  /// **'Exit Full Screen'**
  String get editorExitFullscreen;

  /// No description provided for @editorLink.
  ///
  /// In en, this message translates to:
  /// **'Insert Link'**
  String get editorLink;

  /// No description provided for @editorUnlink.
  ///
  /// In en, this message translates to:
  /// **'Remove Link'**
  String get editorUnlink;

  /// Translated from editor_character_count
  ///
  /// In en, this message translates to:
  /// **'{count} chars'**
  String editorCharacterCount(Object count);

  /// No description provided for @editorCopyCode.
  ///
  /// In en, this message translates to:
  /// **'Copy Code'**
  String get editorCopyCode;

  /// No description provided for @editorCodeCopied.
  ///
  /// In en, this message translates to:
  /// **'Code copied.'**
  String get editorCodeCopied;

  /// No description provided for @modalMaximize.
  ///
  /// In en, this message translates to:
  /// **'Maximize Modal'**
  String get modalMaximize;

  /// No description provided for @modalRestore.
  ///
  /// In en, this message translates to:
  /// **'Restore Modal Size'**
  String get modalRestore;

  /// No description provided for @updatedAt.
  ///
  /// In en, this message translates to:
  /// **'Updated At'**
  String get updatedAt;

  /// No description provided for @version.
  ///
  /// In en, this message translates to:
  /// **'Version'**
  String get version;

  /// No description provided for @viewdatachanges.
  ///
  /// In en, this message translates to:
  /// **'View Data Changes'**
  String get viewdatachanges;

  /// No description provided for @viewAfterSnapshot.
  ///
  /// In en, this message translates to:
  /// **'After Snapshot'**
  String get viewAfterSnapshot;

  /// No description provided for @viewBeforeSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Previous Snapshot'**
  String get viewBeforeSnapshot;

  /// No description provided for @viewChanges.
  ///
  /// In en, this message translates to:
  /// **'View Changes'**
  String get viewChanges;

  /// No description provided for @viewSnapshot.
  ///
  /// In en, this message translates to:
  /// **'View Snapshot'**
  String get viewSnapshot;

  /// No description provided for @visualGraph.
  ///
  /// In en, this message translates to:
  /// **'Visual Graph'**
  String get visualGraph;

  /// No description provided for @vuesticCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get vuesticCancel;

  /// No description provided for @vuesticClose.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get vuesticClose;

  /// No description provided for @vuesticConfirm.
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get vuesticConfirm;

  /// No description provided for @vuesticDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get vuesticDelete;

  /// No description provided for @vuesticGoFirstPage.
  ///
  /// In en, this message translates to:
  /// **'Go to first page'**
  String get vuesticGoFirstPage;

  /// No description provided for @gofirstpage.
  ///
  /// In en, this message translates to:
  /// **'Go to first page'**
  String get gofirstpage;

  /// No description provided for @vuesticGoLastPage.
  ///
  /// In en, this message translates to:
  /// **'Go to last page'**
  String get vuesticGoLastPage;

  /// No description provided for @golastpage.
  ///
  /// In en, this message translates to:
  /// **'Go to last page'**
  String get golastpage;

  /// No description provided for @vuesticGoNextPage.
  ///
  /// In en, this message translates to:
  /// **'Go to next page'**
  String get vuesticGoNextPage;

  /// No description provided for @gonextpage.
  ///
  /// In en, this message translates to:
  /// **'Go to next page'**
  String get gonextpage;

  /// No description provided for @vuesticGoPreviousPage.
  ///
  /// In en, this message translates to:
  /// **'Go to previous page'**
  String get vuesticGoPreviousPage;

  /// No description provided for @gopreviouspage.
  ///
  /// In en, this message translates to:
  /// **'Go to previous page'**
  String get gopreviouspage;

  /// Translated from vuestic_goToSpecificPage
  ///
  /// In en, this message translates to:
  /// **'Go to page {page}'**
  String vuesticGoToSpecificPage(Object page);

  /// Translated from goToSpecificPage
  ///
  /// In en, this message translates to:
  /// **'Go to page {page}'**
  String gotospecificpage(Object page);

  /// No description provided for @vuesticNoOptions.
  ///
  /// In en, this message translates to:
  /// **'No options available'**
  String get vuesticNoOptions;

  /// No description provided for @nooptions.
  ///
  /// In en, this message translates to:
  /// **'No options available'**
  String get nooptions;

  /// No description provided for @vuesticOk.
  ///
  /// In en, this message translates to:
  /// **'OK'**
  String get vuesticOk;

  /// No description provided for @ok.
  ///
  /// In en, this message translates to:
  /// **'OK'**
  String get ok;

  /// No description provided for @vuesticOptionsFilter.
  ///
  /// In en, this message translates to:
  /// **'Filter options'**
  String get vuesticOptionsFilter;

  /// No description provided for @optionsfilter.
  ///
  /// In en, this message translates to:
  /// **'Filter options'**
  String get optionsfilter;

  /// No description provided for @vuesticPagination.
  ///
  /// In en, this message translates to:
  /// **'Pagination'**
  String get vuesticPagination;

  /// No description provided for @pagination.
  ///
  /// In en, this message translates to:
  /// **'Pagination'**
  String get pagination;

  /// No description provided for @vuesticProgressState.
  ///
  /// In en, this message translates to:
  /// **'Progress State'**
  String get vuesticProgressState;

  /// No description provided for @progressstate.
  ///
  /// In en, this message translates to:
  /// **'Progress State'**
  String get progressstate;

  /// No description provided for @vuesticReset.
  ///
  /// In en, this message translates to:
  /// **'Reset'**
  String get vuesticReset;

  /// No description provided for @vuesticSave.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get vuesticSave;

  /// No description provided for @vuesticSearch.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get vuesticSearch;

  /// No description provided for @vuesticSelect.
  ///
  /// In en, this message translates to:
  /// **'Select'**
  String get vuesticSelect;

  /// No description provided for @vuesticSelectedOptions.
  ///
  /// In en, this message translates to:
  /// **'Selected options'**
  String get vuesticSelectedOptions;

  /// No description provided for @selectedoptions.
  ///
  /// In en, this message translates to:
  /// **'Selected options'**
  String get selectedoptions;

  /// No description provided for @vuesticSortColumnBy.
  ///
  /// In en, this message translates to:
  /// **'Sort column by'**
  String get vuesticSortColumnBy;

  /// No description provided for @sortcolumnby.
  ///
  /// In en, this message translates to:
  /// **'Sort column by'**
  String get sortcolumnby;

  /// No description provided for @vuesticToggleDropdown.
  ///
  /// In en, this message translates to:
  /// **'Toggle dropdown'**
  String get vuesticToggleDropdown;

  /// No description provided for @toggledropdown.
  ///
  /// In en, this message translates to:
  /// **'Toggle dropdown'**
  String get toggledropdown;

  /// No description provided for @vuesticUploadFile.
  ///
  /// In en, this message translates to:
  /// **'Upload File'**
  String get vuesticUploadFile;

  /// No description provided for @uploadfile.
  ///
  /// In en, this message translates to:
  /// **'Upload File'**
  String get uploadfile;

  /// No description provided for @vuesticFileTypeIncorrect.
  ///
  /// In en, this message translates to:
  /// **'File type is incorrect'**
  String get vuesticFileTypeIncorrect;

  /// No description provided for @filetypeincorrect.
  ///
  /// In en, this message translates to:
  /// **'File type is incorrect'**
  String get filetypeincorrect;

  /// No description provided for @vuesticDropFiles.
  ///
  /// In en, this message translates to:
  /// **'Drop files here to upload'**
  String get vuesticDropFiles;

  /// No description provided for @dropfiles.
  ///
  /// In en, this message translates to:
  /// **'Drop files here to upload'**
  String get dropfiles;

  /// No description provided for @vuesticFilesUploaded.
  ///
  /// In en, this message translates to:
  /// **'Files uploaded'**
  String get vuesticFilesUploaded;

  /// No description provided for @filesuploaded.
  ///
  /// In en, this message translates to:
  /// **'Files uploaded'**
  String get filesuploaded;

  /// No description provided for @vuesticFileSizeIncorrect.
  ///
  /// In en, this message translates to:
  /// **'File size exceeds limit'**
  String get vuesticFileSizeIncorrect;

  /// No description provided for @filesizeincorrect.
  ///
  /// In en, this message translates to:
  /// **'File size exceeds limit'**
  String get filesizeincorrect;

  /// No description provided for @waitingFor.
  ///
  /// In en, this message translates to:
  /// **'Waiting'**
  String get waitingFor;

  /// Translated from warning_channels
  ///
  /// In en, this message translates to:
  /// **'Please review mapping settings for {count} active integration channel(s).'**
  String warningChannels(Object count);

  /// No description provided for @welcome.
  ///
  /// In en, this message translates to:
  /// **'Welcome'**
  String get welcome;

  /// No description provided for @workflowdetails.
  ///
  /// In en, this message translates to:
  /// **'Workflow Details'**
  String get workflowdetails;

  /// No description provided for @workflowCenterTitle.
  ///
  /// In en, this message translates to:
  /// **'Workflow & Permission Management Center'**
  String get workflowCenterTitle;

  /// No description provided for @workflowManagement.
  ///
  /// In en, this message translates to:
  /// **'Workflow Management'**
  String get workflowManagement;

  /// No description provided for @workflowManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Easily explore workflow templates with AG-Grid and manage or edit them using the dedicated modal dialog.'**
  String get workflowManagementDesc;

  /// No description provided for @workflowNameCol.
  ///
  /// In en, this message translates to:
  /// **'Form Name'**
  String get workflowNameCol;

  /// No description provided for @dataLineage.
  ///
  /// In en, this message translates to:
  /// **'Data Lineage'**
  String get dataLineage;

  /// No description provided for @dataLineageDesc.
  ///
  /// In en, this message translates to:
  /// **'Visualize reference and data flow lineage between domains, taxonomy nodes, and external channels.'**
  String get dataLineageDesc;

  /// No description provided for @lineageNodes.
  ///
  /// In en, this message translates to:
  /// **'Lineage Nodes'**
  String get lineageNodes;

  /// No description provided for @lineageRelationships.
  ///
  /// In en, this message translates to:
  /// **'Data Pipeline Flows'**
  String get lineageRelationships;

  /// Translated from warning_dq_rules
  ///
  /// In en, this message translates to:
  /// **'{count} mapped Data Quality (DQ) rule(s) will be affected.'**
  String warningDqRules(Object count);

  /// No description provided for @noAffectedDqRules.
  ///
  /// In en, this message translates to:
  /// **'No DQ rules linked'**
  String get noAffectedDqRules;

  /// No description provided for @expectedDqViolations.
  ///
  /// In en, this message translates to:
  /// **'Expected DQ Violations'**
  String get expectedDqViolations;

  /// No description provided for @affectedDqRules.
  ///
  /// In en, this message translates to:
  /// **'Affected DQ Rules'**
  String get affectedDqRules;

  /// No description provided for @addDqRule.
  ///
  /// In en, this message translates to:
  /// **'Add Rule'**
  String get addDqRule;

  /// No description provided for @dqDashboardTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Quality Dashboard'**
  String get dqDashboardTitle;

  /// No description provided for @dqDashboardSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Real-time Master Data Governance & Integrity Monitoring'**
  String get dqDashboardSubtitle;

  /// No description provided for @dqDashboardSelectDomainPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select a Domain'**
  String get dqDashboardSelectDomainPlaceholder;

  /// No description provided for @dqDashboardTotalRecords.
  ///
  /// In en, this message translates to:
  /// **'Total Records'**
  String get dqDashboardTotalRecords;

  /// No description provided for @totalRecords.
  ///
  /// In en, this message translates to:
  /// **'Total Records'**
  String get totalRecords;

  /// No description provided for @dqDashboardTotalRecordsSub.
  ///
  /// In en, this message translates to:
  /// **'Monitored Entities in Domain'**
  String get dqDashboardTotalRecordsSub;

  /// No description provided for @totalRecordsSub.
  ///
  /// In en, this message translates to:
  /// **'Monitored Entities in Domain'**
  String get totalRecordsSub;

  /// No description provided for @dqDashboardTotalViolations.
  ///
  /// In en, this message translates to:
  /// **'Total Violations'**
  String get dqDashboardTotalViolations;

  /// No description provided for @totalViolations.
  ///
  /// In en, this message translates to:
  /// **'Total Violations'**
  String get totalViolations;

  /// No description provided for @dqDashboardActionRequired.
  ///
  /// In en, this message translates to:
  /// **'⚠️ Action Required'**
  String get dqDashboardActionRequired;

  /// No description provided for @dqDashboardAllPassed.
  ///
  /// In en, this message translates to:
  /// **'✅ All Records Passed'**
  String get dqDashboardAllPassed;

  /// No description provided for @allPassed.
  ///
  /// In en, this message translates to:
  /// **'✅ All Records Passed'**
  String get allPassed;

  /// No description provided for @dqDashboardActiveDqRules.
  ///
  /// In en, this message translates to:
  /// **'Active DQ Rules'**
  String get dqDashboardActiveDqRules;

  /// No description provided for @activeDqRules.
  ///
  /// In en, this message translates to:
  /// **'Active DQ Rules'**
  String get activeDqRules;

  /// No description provided for @dqDashboardActiveRulesSub.
  ///
  /// In en, this message translates to:
  /// **'Automated Inspection Rules'**
  String get dqDashboardActiveRulesSub;

  /// No description provided for @activeRulesSub.
  ///
  /// In en, this message translates to:
  /// **'Automated Inspection Rules'**
  String get activeRulesSub;

  /// No description provided for @dqDashboardScoreTrendTitle.
  ///
  /// In en, this message translates to:
  /// **'DQ Score Trend History'**
  String get dqDashboardScoreTrendTitle;

  /// No description provided for @scoreTrendTitle.
  ///
  /// In en, this message translates to:
  /// **'DQ Score Trend History'**
  String get scoreTrendTitle;

  /// Translated from dq_dashboard_snapshot_count
  ///
  /// In en, this message translates to:
  /// **'{count} Snapshots'**
  String dqDashboardSnapshotCount(Object count);

  /// Translated from snapshot_count
  ///
  /// In en, this message translates to:
  /// **'{count} Snapshots'**
  String snapshotCount(Object count);

  /// No description provided for @dqDashboardRecent7Days.
  ///
  /// In en, this message translates to:
  /// **'Last 7 Days'**
  String get dqDashboardRecent7Days;

  /// No description provided for @recent7Days.
  ///
  /// In en, this message translates to:
  /// **'Last 7 Days'**
  String get recent7Days;

  /// No description provided for @dqDashboardRecent30Days.
  ///
  /// In en, this message translates to:
  /// **'Last 30 Days'**
  String get dqDashboardRecent30Days;

  /// No description provided for @recent30Days.
  ///
  /// In en, this message translates to:
  /// **'Last 30 Days'**
  String get recent30Days;

  /// No description provided for @dqDashboardRecent90Days.
  ///
  /// In en, this message translates to:
  /// **'Last 90 Days'**
  String get dqDashboardRecent90Days;

  /// No description provided for @recent90Days.
  ///
  /// In en, this message translates to:
  /// **'Last 90 Days'**
  String get recent90Days;

  /// No description provided for @dqDashboardRecentAll.
  ///
  /// In en, this message translates to:
  /// **'All Time'**
  String get dqDashboardRecentAll;

  /// No description provided for @recentAll.
  ///
  /// In en, this message translates to:
  /// **'All Time'**
  String get recentAll;

  /// No description provided for @dqDashboardRunScan.
  ///
  /// In en, this message translates to:
  /// **'⚡ Run DQ Scan'**
  String get dqDashboardRunScan;

  /// No description provided for @runScan.
  ///
  /// In en, this message translates to:
  /// **'Run Profiling Scan'**
  String get runScan;

  /// No description provided for @dqDashboardNoSnapshots.
  ///
  /// In en, this message translates to:
  /// **'No DQ Snapshot History Recorded'**
  String get dqDashboardNoSnapshots;

  /// No description provided for @noSnapshots.
  ///
  /// In en, this message translates to:
  /// **'No snapshots created yet.'**
  String get noSnapshots;

  /// No description provided for @dqDashboardNoSnapshotsDesc.
  ///
  /// In en, this message translates to:
  /// **'Click \'⚡ Run DQ Scan\' to execute real-time master data validation and record snapshot trends.'**
  String get dqDashboardNoSnapshotsDesc;

  /// No description provided for @noSnapshotsDesc.
  ///
  /// In en, this message translates to:
  /// **'Click \'⚡ Run DQ Scan\' to execute real-time master data validation and record snapshot trends.'**
  String get noSnapshotsDesc;

  /// No description provided for @dqDashboardStartScanNow.
  ///
  /// In en, this message translates to:
  /// **'⚡ Start DQ Scan Now'**
  String get dqDashboardStartScanNow;

  /// No description provided for @startScanNow.
  ///
  /// In en, this message translates to:
  /// **'⚡ Start DQ Scan Now'**
  String get startScanNow;

  /// No description provided for @dqDashboardAvgScore.
  ///
  /// In en, this message translates to:
  /// **'Average Score:'**
  String get dqDashboardAvgScore;

  /// No description provided for @avgScore.
  ///
  /// In en, this message translates to:
  /// **'Average Score:'**
  String get avgScore;

  /// No description provided for @dqDashboardMaxScore.
  ///
  /// In en, this message translates to:
  /// **'Max Score:'**
  String get dqDashboardMaxScore;

  /// No description provided for @maxScore.
  ///
  /// In en, this message translates to:
  /// **'Max Score:'**
  String get maxScore;

  /// No description provided for @dqDashboardLatestSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Latest Snapshot:'**
  String get dqDashboardLatestSnapshot;

  /// No description provided for @latestSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Latest Snapshot:'**
  String get latestSnapshot;

  /// Translated from dq_dashboard_tooltip_info
  ///
  /// In en, this message translates to:
  /// **'Violations: {violations} / Total: {total} rows'**
  String dqDashboardTooltipInfo(Object violations, Object total);

  /// Translated from tooltip_info
  ///
  /// In en, this message translates to:
  /// **'Violations: {violations} / Total: {total} rows'**
  String tooltipInfo(Object violations, Object total);

  /// No description provided for @dqDashboardViolationsBySeverity.
  ///
  /// In en, this message translates to:
  /// **'Violations by Severity'**
  String get dqDashboardViolationsBySeverity;

  /// No description provided for @violationsBySeverity.
  ///
  /// In en, this message translates to:
  /// **'Violations by Severity'**
  String get violationsBySeverity;

  /// No description provided for @dqDashboardNoViolationsDetected.
  ///
  /// In en, this message translates to:
  /// **'No violations detected! Perfect data quality.'**
  String get dqDashboardNoViolationsDetected;

  /// No description provided for @noViolationsDetected.
  ///
  /// In en, this message translates to:
  /// **'No violations detected! Perfect data quality.'**
  String get noViolationsDetected;

  /// No description provided for @dqDashboardViolationsByField.
  ///
  /// In en, this message translates to:
  /// **'Violations by Field'**
  String get dqDashboardViolationsByField;

  /// No description provided for @violationsByField.
  ///
  /// In en, this message translates to:
  /// **'Violations by Field'**
  String get violationsByField;

  /// No description provided for @dqDashboardNoFieldViolations.
  ///
  /// In en, this message translates to:
  /// **'No field violations detected.'**
  String get dqDashboardNoFieldViolations;

  /// No description provided for @noFieldViolations.
  ///
  /// In en, this message translates to:
  /// **'No field violations detected.'**
  String get noFieldViolations;

  /// No description provided for @dqDashboardViolationTableTitle.
  ///
  /// In en, this message translates to:
  /// **'Detailed Violation Records List'**
  String get dqDashboardViolationTableTitle;

  /// No description provided for @violationTableTitle.
  ///
  /// In en, this message translates to:
  /// **'Detailed Violation Records List'**
  String get violationTableTitle;

  /// No description provided for @dqDashboardViolationTableSub.
  ///
  /// In en, this message translates to:
  /// **'Real-time failed validation record monitoring'**
  String get dqDashboardViolationTableSub;

  /// No description provided for @violationTableSub.
  ///
  /// In en, this message translates to:
  /// **'Real-time failed validation record monitoring'**
  String get violationTableSub;

  /// No description provided for @dqDashboardSeverity.
  ///
  /// In en, this message translates to:
  /// **'Severity'**
  String get dqDashboardSeverity;

  /// No description provided for @severity.
  ///
  /// In en, this message translates to:
  /// **'Severity'**
  String get severity;

  /// No description provided for @dqDashboardField.
  ///
  /// In en, this message translates to:
  /// **'Field'**
  String get dqDashboardField;

  /// No description provided for @field.
  ///
  /// In en, this message translates to:
  /// **'Field'**
  String get field;

  /// No description provided for @dqDashboardLoadingViolations.
  ///
  /// In en, this message translates to:
  /// **'Loading Violation Records...'**
  String get dqDashboardLoadingViolations;

  /// No description provided for @loadingViolations.
  ///
  /// In en, this message translates to:
  /// **'Loading Violation Records...'**
  String get loadingViolations;

  /// No description provided for @dqDashboardNoViolationsFound.
  ///
  /// In en, this message translates to:
  /// **'No violation records found for the selected condition.'**
  String get dqDashboardNoViolationsFound;

  /// No description provided for @noViolationsFound.
  ///
  /// In en, this message translates to:
  /// **'No violation records found for the selected condition.'**
  String get noViolationsFound;

  /// No description provided for @dqDashboardRecordId.
  ///
  /// In en, this message translates to:
  /// **'Record Identifier'**
  String get dqDashboardRecordId;

  /// No description provided for @recordId.
  ///
  /// In en, this message translates to:
  /// **'Record Identifier'**
  String get recordId;

  /// No description provided for @dqDashboardViolatedField.
  ///
  /// In en, this message translates to:
  /// **'Violated Field'**
  String get dqDashboardViolatedField;

  /// No description provided for @violatedField.
  ///
  /// In en, this message translates to:
  /// **'Violated Field'**
  String get violatedField;

  /// No description provided for @dqDashboardRuleName.
  ///
  /// In en, this message translates to:
  /// **'Inspection Rule'**
  String get dqDashboardRuleName;

  /// No description provided for @dqDashboardViolationMessage.
  ///
  /// In en, this message translates to:
  /// **'Violation Message'**
  String get dqDashboardViolationMessage;

  /// No description provided for @violationMessage.
  ///
  /// In en, this message translates to:
  /// **'Violation Message'**
  String get violationMessage;

  /// No description provided for @dqDashboardActualValue.
  ///
  /// In en, this message translates to:
  /// **'Actual Value'**
  String get dqDashboardActualValue;

  /// No description provided for @actualValue.
  ///
  /// In en, this message translates to:
  /// **'Actual Value'**
  String get actualValue;

  /// No description provided for @dqDashboardEmptyValue.
  ///
  /// In en, this message translates to:
  /// **'(Empty)'**
  String get dqDashboardEmptyValue;

  /// No description provided for @emptyValue.
  ///
  /// In en, this message translates to:
  /// **'(Empty)'**
  String get emptyValue;

  /// No description provided for @dqDashboardDetails.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get dqDashboardDetails;

  /// Translated from dq_dashboard_pagination_summary
  ///
  /// In en, this message translates to:
  /// **'Showing {start} - {end} of {total} records'**
  String dqDashboardPaginationSummary(Object start, Object end, Object total);

  /// Translated from pagination_summary
  ///
  /// In en, this message translates to:
  /// **'Showing {start} - {end} of {total} records'**
  String paginationSummary(Object start, Object end, Object total);

  /// No description provided for @dqDashboardDesc.
  ///
  /// In en, this message translates to:
  /// **'Real-time monitoring of data quality rule compliance, error counts, and field diagnosis status by domain.'**
  String get dqDashboardDesc;

  /// No description provided for @dqErrorMessage.
  ///
  /// In en, this message translates to:
  /// **'Error Message'**
  String get dqErrorMessage;

  /// No description provided for @dqParams.
  ///
  /// In en, this message translates to:
  /// **'Parameters'**
  String get dqParams;

  /// No description provided for @dqPermGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Quality Permissions'**
  String get dqPermGroupTitle;

  /// No description provided for @dqRuleType.
  ///
  /// In en, this message translates to:
  /// **'Rule Type'**
  String get dqRuleType;

  /// No description provided for @dqRulesDesc.
  ///
  /// In en, this message translates to:
  /// **'Configure data quality validation rules and inspection parameters by domain field.'**
  String get dqRulesDesc;

  /// No description provided for @dqRulesManagement.
  ///
  /// In en, this message translates to:
  /// **'Data Quality Rule Management'**
  String get dqRulesManagement;

  /// No description provided for @dqScoreTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Quality Score'**
  String get dqScoreTitle;

  /// No description provided for @dqSeverity.
  ///
  /// In en, this message translates to:
  /// **'Severity'**
  String get dqSeverity;

  /// No description provided for @dqSortOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get dqSortOrder;

  /// No description provided for @editDqRule.
  ///
  /// In en, this message translates to:
  /// **'Edit Rule'**
  String get editDqRule;

  /// Translated from error_dq_failed
  ///
  /// In en, this message translates to:
  /// **'Data Quality Check Failed: {details}'**
  String errorDqFailed(Object details);

  /// No description provided for @goToDqDashboard.
  ///
  /// In en, this message translates to:
  /// **'Go to DQ Dashboard'**
  String get goToDqDashboard;

  /// No description provided for @initiatorRulesTitle.
  ///
  /// In en, this message translates to:
  /// **'Initiator Eligibility Rules'**
  String get initiatorRulesTitle;

  /// No description provided for @loadingDqMetrics.
  ///
  /// In en, this message translates to:
  /// **'Loading Quality Metrics...'**
  String get loadingDqMetrics;

  /// No description provided for @noRulesDefault.
  ///
  /// In en, this message translates to:
  /// **'No rules defined. By default, all users are eligible and have full field access.'**
  String get noRulesDefault;

  /// No description provided for @openDqViolations.
  ///
  /// In en, this message translates to:
  /// **'Open DQ Violations'**
  String get openDqViolations;

  /// No description provided for @permissionsRulesTitle.
  ///
  /// In en, this message translates to:
  /// **'Initiator Eligibility & Field Control Rules'**
  String get permissionsRulesTitle;

  /// No description provided for @runDqScan.
  ///
  /// In en, this message translates to:
  /// **'Run DQ Scan'**
  String get runDqScan;

  /// No description provided for @autoRemediation.
  ///
  /// In en, this message translates to:
  /// **'Intelligent DQ Auto Remediation'**
  String get autoRemediation;

  /// No description provided for @autoRemediationDesc.
  ///
  /// In en, this message translates to:
  /// **'Rule-based automatic correction and one-click fix for formatting errors (phone, biz no, whitespace, lowercase).'**
  String get autoRemediationDesc;

  /// No description provided for @scanRemediation.
  ///
  /// In en, this message translates to:
  /// **'Scan Remediation Candidates'**
  String get scanRemediation;

  /// No description provided for @applyAllRemediation.
  ///
  /// In en, this message translates to:
  /// **'Apply All Corrections'**
  String get applyAllRemediation;

  /// No description provided for @currentVal.
  ///
  /// In en, this message translates to:
  /// **'Current Value (Error)'**
  String get currentVal;

  /// No description provided for @proposedVal.
  ///
  /// In en, this message translates to:
  /// **'Proposed Value (Fixed)'**
  String get proposedVal;

  /// No description provided for @remediationReason.
  ///
  /// In en, this message translates to:
  /// **'Remediation Reason'**
  String get remediationReason;

  /// No description provided for @noRemediationsNeeded.
  ///
  /// In en, this message translates to:
  /// **'No records require formatting remediation.'**
  String get noRemediationsNeeded;

  /// No description provided for @referenceIntegrity.
  ///
  /// In en, this message translates to:
  /// **'Cross-Domain Reference Integrity'**
  String get referenceIntegrity;

  /// No description provided for @referenceIntegrityDesc.
  ///
  /// In en, this message translates to:
  /// **'Real-time diagnostic scan for orphan references across cross-domain foreign key relations.'**
  String get referenceIntegrityDesc;

  /// No description provided for @integrityScore.
  ///
  /// In en, this message translates to:
  /// **'Integrity Score'**
  String get integrityScore;

  /// No description provided for @orphanCount.
  ///
  /// In en, this message translates to:
  /// **'Orphan References'**
  String get orphanCount;

  /// No description provided for @scannedRecords.
  ///
  /// In en, this message translates to:
  /// **'Scanned Records'**
  String get scannedRecords;

  /// No description provided for @orphanDetails.
  ///
  /// In en, this message translates to:
  /// **'Orphan Reference Details'**
  String get orphanDetails;

  /// No description provided for @noOrphanRecords.
  ///
  /// In en, this message translates to:
  /// **'All foreign references are healthy with zero orphan records.'**
  String get noOrphanRecords;

  /// No description provided for @dqSeverityDistribution.
  ///
  /// In en, this message translates to:
  /// **'DQ Violations by Severity'**
  String get dqSeverityDistribution;

  /// No description provided for @dqViolationTrend.
  ///
  /// In en, this message translates to:
  /// **'DQ Violation Trend (Last 7 Days)'**
  String get dqViolationTrend;

  /// No description provided for @inboxTitle.
  ///
  /// In en, this message translates to:
  /// **'Inbox'**
  String get inboxTitle;

  /// No description provided for @inboxSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Internal Messages & Email Management'**
  String get inboxSubtitle;

  /// No description provided for @inboxFolderInbox.
  ///
  /// In en, this message translates to:
  /// **'Inbox'**
  String get inboxFolderInbox;

  /// No description provided for @folderInbox.
  ///
  /// In en, this message translates to:
  /// **'Inbox'**
  String get folderInbox;

  /// No description provided for @inboxFolderSent.
  ///
  /// In en, this message translates to:
  /// **'Sent'**
  String get inboxFolderSent;

  /// No description provided for @folderSent.
  ///
  /// In en, this message translates to:
  /// **'Sent'**
  String get folderSent;

  /// No description provided for @inboxFolderDraft.
  ///
  /// In en, this message translates to:
  /// **'Drafts'**
  String get inboxFolderDraft;

  /// No description provided for @folderDraft.
  ///
  /// In en, this message translates to:
  /// **'Drafts'**
  String get folderDraft;

  /// No description provided for @inboxFolderArchive.
  ///
  /// In en, this message translates to:
  /// **'Archive'**
  String get inboxFolderArchive;

  /// No description provided for @folderArchive.
  ///
  /// In en, this message translates to:
  /// **'Archive'**
  String get folderArchive;

  /// No description provided for @inboxFolderTrash.
  ///
  /// In en, this message translates to:
  /// **'Trash'**
  String get inboxFolderTrash;

  /// No description provided for @folderTrash.
  ///
  /// In en, this message translates to:
  /// **'Trash'**
  String get folderTrash;

  /// No description provided for @inboxFolderStarred.
  ///
  /// In en, this message translates to:
  /// **'Starred'**
  String get inboxFolderStarred;

  /// No description provided for @folderStarred.
  ///
  /// In en, this message translates to:
  /// **'Starred'**
  String get folderStarred;

  /// No description provided for @inboxCompose.
  ///
  /// In en, this message translates to:
  /// **'New Message'**
  String get inboxCompose;

  /// No description provided for @compose.
  ///
  /// In en, this message translates to:
  /// **'New Message'**
  String get compose;

  /// No description provided for @inboxComposeTitle.
  ///
  /// In en, this message translates to:
  /// **'Compose Message'**
  String get inboxComposeTitle;

  /// No description provided for @composeTitle.
  ///
  /// In en, this message translates to:
  /// **'Compose Message'**
  String get composeTitle;

  /// No description provided for @inboxReply.
  ///
  /// In en, this message translates to:
  /// **'Reply'**
  String get inboxReply;

  /// No description provided for @reply.
  ///
  /// In en, this message translates to:
  /// **'Reply'**
  String get reply;

  /// No description provided for @inboxReplyAll.
  ///
  /// In en, this message translates to:
  /// **'Reply All'**
  String get inboxReplyAll;

  /// No description provided for @replyAll.
  ///
  /// In en, this message translates to:
  /// **'Reply All'**
  String get replyAll;

  /// No description provided for @inboxForward.
  ///
  /// In en, this message translates to:
  /// **'Forward'**
  String get inboxForward;

  /// No description provided for @forward.
  ///
  /// In en, this message translates to:
  /// **'Forward'**
  String get forward;

  /// No description provided for @inboxSend.
  ///
  /// In en, this message translates to:
  /// **'Send'**
  String get inboxSend;

  /// No description provided for @send.
  ///
  /// In en, this message translates to:
  /// **'Send'**
  String get send;

  /// No description provided for @inboxSendFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to send message.'**
  String get inboxSendFailed;

  /// No description provided for @sendFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to send message.'**
  String get sendFailed;

  /// No description provided for @inboxSaveDraft.
  ///
  /// In en, this message translates to:
  /// **'Save Draft'**
  String get inboxSaveDraft;

  /// No description provided for @saveDraft.
  ///
  /// In en, this message translates to:
  /// **'Save Draft'**
  String get saveDraft;

  /// No description provided for @inboxDraftSaved.
  ///
  /// In en, this message translates to:
  /// **'Draft saved.'**
  String get inboxDraftSaved;

  /// No description provided for @draftSaved.
  ///
  /// In en, this message translates to:
  /// **'Draft saved.'**
  String get draftSaved;

  /// No description provided for @inboxDraftFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save draft.'**
  String get inboxDraftFailed;

  /// No description provided for @draftFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save draft.'**
  String get draftFailed;

  /// No description provided for @inboxMessageSent.
  ///
  /// In en, this message translates to:
  /// **'Message sent.'**
  String get inboxMessageSent;

  /// No description provided for @messageSent.
  ///
  /// In en, this message translates to:
  /// **'Message sent.'**
  String get messageSent;

  /// No description provided for @inboxMessageDeleted.
  ///
  /// In en, this message translates to:
  /// **'Message deleted.'**
  String get inboxMessageDeleted;

  /// No description provided for @messageDeleted.
  ///
  /// In en, this message translates to:
  /// **'Message deleted.'**
  String get messageDeleted;

  /// No description provided for @inboxMessageMoved.
  ///
  /// In en, this message translates to:
  /// **'Message moved.'**
  String get inboxMessageMoved;

  /// No description provided for @messageMoved.
  ///
  /// In en, this message translates to:
  /// **'Message moved.'**
  String get messageMoved;

  /// No description provided for @inboxMessageStarred.
  ///
  /// In en, this message translates to:
  /// **'Message starred.'**
  String get inboxMessageStarred;

  /// No description provided for @messageStarred.
  ///
  /// In en, this message translates to:
  /// **'Message starred.'**
  String get messageStarred;

  /// No description provided for @inboxMessageUnstarred.
  ///
  /// In en, this message translates to:
  /// **'Message unstarred.'**
  String get inboxMessageUnstarred;

  /// No description provided for @messageUnstarred.
  ///
  /// In en, this message translates to:
  /// **'Message unstarred.'**
  String get messageUnstarred;

  /// No description provided for @inboxMarkRead.
  ///
  /// In en, this message translates to:
  /// **'Mark as Read'**
  String get inboxMarkRead;

  /// No description provided for @markRead.
  ///
  /// In en, this message translates to:
  /// **'Mark as Read'**
  String get markRead;

  /// No description provided for @inboxMarkUnread.
  ///
  /// In en, this message translates to:
  /// **'Mark as Unread'**
  String get inboxMarkUnread;

  /// No description provided for @markUnread.
  ///
  /// In en, this message translates to:
  /// **'Mark as Unread'**
  String get markUnread;

  /// No description provided for @inboxMoveToArchive.
  ///
  /// In en, this message translates to:
  /// **'Move to Archive'**
  String get inboxMoveToArchive;

  /// No description provided for @moveToArchive.
  ///
  /// In en, this message translates to:
  /// **'Move to Archive'**
  String get moveToArchive;

  /// No description provided for @inboxMoveToTrash.
  ///
  /// In en, this message translates to:
  /// **'Move to Trash'**
  String get inboxMoveToTrash;

  /// No description provided for @moveToTrash.
  ///
  /// In en, this message translates to:
  /// **'Move to Trash'**
  String get moveToTrash;

  /// No description provided for @inboxDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get inboxDelete;

  /// No description provided for @inboxPermanentDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete Permanently'**
  String get inboxPermanentDelete;

  /// No description provided for @permanentDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete Permanently'**
  String get permanentDelete;

  /// No description provided for @inboxPermanentDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to permanently delete this message? This action cannot be undone.'**
  String get inboxPermanentDeleteConfirm;

  /// No description provided for @permanentDeleteConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to permanently delete this message? This action cannot be undone.'**
  String get permanentDeleteConfirm;

  /// No description provided for @inboxRestore.
  ///
  /// In en, this message translates to:
  /// **'Restore'**
  String get inboxRestore;

  /// No description provided for @restore.
  ///
  /// In en, this message translates to:
  /// **'Restore'**
  String get restore;

  /// No description provided for @inboxEmptyTrash.
  ///
  /// In en, this message translates to:
  /// **'Empty Trash'**
  String get inboxEmptyTrash;

  /// No description provided for @emptyTrash.
  ///
  /// In en, this message translates to:
  /// **'Empty Trash'**
  String get emptyTrash;

  /// No description provided for @inboxRecipientTo.
  ///
  /// In en, this message translates to:
  /// **'To'**
  String get inboxRecipientTo;

  /// No description provided for @recipientTo.
  ///
  /// In en, this message translates to:
  /// **'To'**
  String get recipientTo;

  /// No description provided for @inboxRecipientCc.
  ///
  /// In en, this message translates to:
  /// **'CC'**
  String get inboxRecipientCc;

  /// No description provided for @recipientCc.
  ///
  /// In en, this message translates to:
  /// **'CC'**
  String get recipientCc;

  /// No description provided for @inboxRecipientBcc.
  ///
  /// In en, this message translates to:
  /// **'BCC'**
  String get inboxRecipientBcc;

  /// No description provided for @recipientBcc.
  ///
  /// In en, this message translates to:
  /// **'BCC'**
  String get recipientBcc;

  /// No description provided for @inboxAddRecipient.
  ///
  /// In en, this message translates to:
  /// **'Add Recipient'**
  String get inboxAddRecipient;

  /// No description provided for @addRecipient.
  ///
  /// In en, this message translates to:
  /// **'Add Recipient'**
  String get addRecipient;

  /// No description provided for @inboxAddExternalEmail.
  ///
  /// In en, this message translates to:
  /// **'Add External Email'**
  String get inboxAddExternalEmail;

  /// No description provided for @addExternalEmail.
  ///
  /// In en, this message translates to:
  /// **'Add External Email'**
  String get addExternalEmail;

  /// No description provided for @inboxSearchUsers.
  ///
  /// In en, this message translates to:
  /// **'Search users...'**
  String get inboxSearchUsers;

  /// No description provided for @searchUsers.
  ///
  /// In en, this message translates to:
  /// **'Search users...'**
  String get searchUsers;

  /// No description provided for @inboxSearchUsersBtn.
  ///
  /// In en, this message translates to:
  /// **'Search Users'**
  String get inboxSearchUsersBtn;

  /// No description provided for @searchUsersBtn.
  ///
  /// In en, this message translates to:
  /// **'Search Users'**
  String get searchUsersBtn;

  /// No description provided for @inboxAddressBook.
  ///
  /// In en, this message translates to:
  /// **'User Search / Address Book'**
  String get inboxAddressBook;

  /// No description provided for @addressBook.
  ///
  /// In en, this message translates to:
  /// **'User Search / Address Book'**
  String get addressBook;

  /// No description provided for @inboxNoRecipients.
  ///
  /// In en, this message translates to:
  /// **'Please select a recipient.'**
  String get inboxNoRecipients;

  /// No description provided for @noRecipients.
  ///
  /// In en, this message translates to:
  /// **'Please select a recipient.'**
  String get noRecipients;

  /// No description provided for @inboxRecipientRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter or select at least one recipient.'**
  String get inboxRecipientRequired;

  /// No description provided for @recipientRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter or select at least one recipient.'**
  String get recipientRequired;

  /// No description provided for @inboxSubjectRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a subject.'**
  String get inboxSubjectRequired;

  /// No description provided for @subjectRequired.
  ///
  /// In en, this message translates to:
  /// **'Please enter a subject.'**
  String get subjectRequired;

  /// No description provided for @inboxNoSubject.
  ///
  /// In en, this message translates to:
  /// **'(No subject)'**
  String get inboxNoSubject;

  /// No description provided for @noSubject.
  ///
  /// In en, this message translates to:
  /// **'(No subject)'**
  String get noSubject;

  /// No description provided for @inboxSender.
  ///
  /// In en, this message translates to:
  /// **'Sender'**
  String get inboxSender;

  /// No description provided for @sender.
  ///
  /// In en, this message translates to:
  /// **'Sender'**
  String get sender;

  /// No description provided for @inboxDate.
  ///
  /// In en, this message translates to:
  /// **'Date'**
  String get inboxDate;

  /// No description provided for @inboxSubject.
  ///
  /// In en, this message translates to:
  /// **'Subject'**
  String get inboxSubject;

  /// No description provided for @subject.
  ///
  /// In en, this message translates to:
  /// **'Subject'**
  String get subject;

  /// No description provided for @inboxSubjectPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter subject'**
  String get inboxSubjectPlaceholder;

  /// No description provided for @subjectPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter subject'**
  String get subjectPlaceholder;

  /// No description provided for @inboxBodyPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Write your message here...'**
  String get inboxBodyPlaceholder;

  /// No description provided for @bodyPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Write your message here...'**
  String get bodyPlaceholder;

  /// No description provided for @inboxImportance.
  ///
  /// In en, this message translates to:
  /// **'Importance'**
  String get inboxImportance;

  /// No description provided for @importance.
  ///
  /// In en, this message translates to:
  /// **'Importance'**
  String get importance;

  /// No description provided for @inboxImportanceNormal.
  ///
  /// In en, this message translates to:
  /// **'Normal'**
  String get inboxImportanceNormal;

  /// No description provided for @importanceNormal.
  ///
  /// In en, this message translates to:
  /// **'Normal'**
  String get importanceNormal;

  /// No description provided for @inboxImportanceHigh.
  ///
  /// In en, this message translates to:
  /// **'High'**
  String get inboxImportanceHigh;

  /// No description provided for @importanceHigh.
  ///
  /// In en, this message translates to:
  /// **'High'**
  String get importanceHigh;

  /// No description provided for @inboxImportanceUrgent.
  ///
  /// In en, this message translates to:
  /// **'Urgent'**
  String get inboxImportanceUrgent;

  /// No description provided for @importanceUrgent.
  ///
  /// In en, this message translates to:
  /// **'Urgent'**
  String get importanceUrgent;

  /// No description provided for @inboxTypeInternal.
  ///
  /// In en, this message translates to:
  /// **'Internal Message'**
  String get inboxTypeInternal;

  /// No description provided for @typeInternal.
  ///
  /// In en, this message translates to:
  /// **'Internal Message'**
  String get typeInternal;

  /// No description provided for @inboxTypeExternalInbound.
  ///
  /// In en, this message translates to:
  /// **'External Inbound Email'**
  String get inboxTypeExternalInbound;

  /// No description provided for @typeExternalInbound.
  ///
  /// In en, this message translates to:
  /// **'External Inbound Email'**
  String get typeExternalInbound;

  /// No description provided for @inboxTypeExternalOutbound.
  ///
  /// In en, this message translates to:
  /// **'External Outbound Email'**
  String get inboxTypeExternalOutbound;

  /// No description provided for @typeExternalOutbound.
  ///
  /// In en, this message translates to:
  /// **'External Outbound Email'**
  String get typeExternalOutbound;

  /// No description provided for @inboxTypeApprovalNotice.
  ///
  /// In en, this message translates to:
  /// **'Approval Notice'**
  String get inboxTypeApprovalNotice;

  /// No description provided for @typeApprovalNotice.
  ///
  /// In en, this message translates to:
  /// **'Approval Notice'**
  String get typeApprovalNotice;

  /// No description provided for @inboxTypeSystemNotice.
  ///
  /// In en, this message translates to:
  /// **'System Notice'**
  String get inboxTypeSystemNotice;

  /// No description provided for @typeSystemNotice.
  ///
  /// In en, this message translates to:
  /// **'System Notice'**
  String get typeSystemNotice;

  /// No description provided for @inboxAttachment.
  ///
  /// In en, this message translates to:
  /// **'Attachment'**
  String get inboxAttachment;

  /// No description provided for @inboxAttachments.
  ///
  /// In en, this message translates to:
  /// **'Attachments'**
  String get inboxAttachments;

  /// No description provided for @attachments.
  ///
  /// In en, this message translates to:
  /// **'Attachments'**
  String get attachments;

  /// No description provided for @inboxAddAttachment.
  ///
  /// In en, this message translates to:
  /// **'Attach File'**
  String get inboxAddAttachment;

  /// No description provided for @addAttachment.
  ///
  /// In en, this message translates to:
  /// **'Attach File'**
  String get addAttachment;

  /// No description provided for @inboxDownloadAttachment.
  ///
  /// In en, this message translates to:
  /// **'Download Attachment'**
  String get inboxDownloadAttachment;

  /// No description provided for @downloadAttachment.
  ///
  /// In en, this message translates to:
  /// **'Download Attachment'**
  String get downloadAttachment;

  /// No description provided for @inboxNoMessages.
  ///
  /// In en, this message translates to:
  /// **'No messages.'**
  String get inboxNoMessages;

  /// No description provided for @noMessages.
  ///
  /// In en, this message translates to:
  /// **'No messages.'**
  String get noMessages;

  /// No description provided for @inboxNoMessageSelected.
  ///
  /// In en, this message translates to:
  /// **'Please select a message.'**
  String get inboxNoMessageSelected;

  /// No description provided for @noMessageSelected.
  ///
  /// In en, this message translates to:
  /// **'Please select a message.'**
  String get noMessageSelected;

  /// Translated from inbox_unread_count
  ///
  /// In en, this message translates to:
  /// **'{count} unread'**
  String inboxUnreadCount(Object count);

  /// Translated from unread_count
  ///
  /// In en, this message translates to:
  /// **'{count} unread'**
  String unreadCount(Object count);

  /// No description provided for @inboxSearchPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search messages...'**
  String get inboxSearchPlaceholder;

  /// No description provided for @searchPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search by ID or Name attribute...'**
  String get searchPlaceholder;

  /// No description provided for @inboxThread.
  ///
  /// In en, this message translates to:
  /// **'Conversation History'**
  String get inboxThread;

  /// No description provided for @thread.
  ///
  /// In en, this message translates to:
  /// **'Conversation History'**
  String get thread;

  /// Translated from inbox_thread_count
  ///
  /// In en, this message translates to:
  /// **'{count} conversations'**
  String inboxThreadCount(Object count);

  /// Translated from thread_count
  ///
  /// In en, this message translates to:
  /// **'{count} conversations'**
  String threadCount(Object count);

  /// No description provided for @inboxCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get inboxCancel;

  /// No description provided for @inboxClose.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get inboxClose;

  /// No description provided for @inboxRecallMessage.
  ///
  /// In en, this message translates to:
  /// **'Recall Message'**
  String get inboxRecallMessage;

  /// No description provided for @recallMessage.
  ///
  /// In en, this message translates to:
  /// **'Recall Message'**
  String get recallMessage;

  /// No description provided for @inboxRecallConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to recall this message for unread recipients? (Messages already read cannot be recalled)'**
  String get inboxRecallConfirm;

  /// No description provided for @recallConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to recall this message for unread recipients? (Messages already read cannot be recalled)'**
  String get recallConfirm;

  /// No description provided for @inboxRecallSuccess.
  ///
  /// In en, this message translates to:
  /// **'Message recall processed.'**
  String get inboxRecallSuccess;

  /// No description provided for @recallSuccess.
  ///
  /// In en, this message translates to:
  /// **'Message recall processed.'**
  String get recallSuccess;

  /// Translated from inbox_recall_result_total
  ///
  /// In en, this message translates to:
  /// **'Total Recipients: {count}'**
  String inboxRecallResultTotal(Object count);

  /// Translated from recall_result_total
  ///
  /// In en, this message translates to:
  /// **'Total Recipients: {count}'**
  String recallResultTotal(Object count);

  /// Translated from inbox_recall_result_before_read
  ///
  /// In en, this message translates to:
  /// **'Recalled Before Read: {count}'**
  String inboxRecallResultBeforeRead(Object count);

  /// Translated from recall_result_before_read
  ///
  /// In en, this message translates to:
  /// **'Recalled Before Read: {count}'**
  String recallResultBeforeRead(Object count);

  /// Translated from inbox_recall_result_after_read
  ///
  /// In en, this message translates to:
  /// **'Already Read (Cannot recall): {count}'**
  String inboxRecallResultAfterRead(Object count);

  /// Translated from recall_result_after_read
  ///
  /// In en, this message translates to:
  /// **'Already Read (Cannot recall): {count}'**
  String recallResultAfterRead(Object count);

  /// Translated from inbox_recall_result_external
  ///
  /// In en, this message translates to:
  /// **'External Email (Cannot recall): {count}'**
  String inboxRecallResultExternal(Object count);

  /// Translated from recall_result_external
  ///
  /// In en, this message translates to:
  /// **'External Email (Cannot recall): {count}'**
  String recallResultExternal(Object count);

  /// No description provided for @inboxRead.
  ///
  /// In en, this message translates to:
  /// **'Read'**
  String get inboxRead;

  /// No description provided for @read.
  ///
  /// In en, this message translates to:
  /// **'Read'**
  String get read;

  /// No description provided for @inboxUnread.
  ///
  /// In en, this message translates to:
  /// **'Unread'**
  String get inboxUnread;

  /// No description provided for @unread.
  ///
  /// In en, this message translates to:
  /// **'Unread'**
  String get unread;

  /// No description provided for @inboxRecalled.
  ///
  /// In en, this message translates to:
  /// **'Recalled'**
  String get inboxRecalled;

  /// No description provided for @recalled.
  ///
  /// In en, this message translates to:
  /// **'Recalled'**
  String get recalled;

  /// No description provided for @inboxRecipientRecallStatusBefore.
  ///
  /// In en, this message translates to:
  /// **'Recalled (Deleted before read)'**
  String get inboxRecipientRecallStatusBefore;

  /// No description provided for @recipientRecallStatusBefore.
  ///
  /// In en, this message translates to:
  /// **'Recalled (Deleted before read)'**
  String get recipientRecallStatusBefore;

  /// Translated from inbox_recipient_recall_status_after
  ///
  /// In en, this message translates to:
  /// **'Cannot recall (Read: {time})'**
  String inboxRecipientRecallStatusAfter(Object time);

  /// Translated from recipient_recall_status_after
  ///
  /// In en, this message translates to:
  /// **'Cannot recall (Read: {time})'**
  String recipientRecallStatusAfter(Object time);

  /// No description provided for @inboxRecipientRecallStatusExternal.
  ///
  /// In en, this message translates to:
  /// **'External Email (Cannot recall)'**
  String get inboxRecipientRecallStatusExternal;

  /// No description provided for @recipientRecallStatusExternal.
  ///
  /// In en, this message translates to:
  /// **'External Email (Cannot recall)'**
  String get recipientRecallStatusExternal;

  /// No description provided for @inboxDragDropHint.
  ///
  /// In en, this message translates to:
  /// **'Drag and drop files here or click to browse'**
  String get inboxDragDropHint;

  /// No description provided for @dragDropHint.
  ///
  /// In en, this message translates to:
  /// **'Drag and drop files here or click to browse'**
  String get dragDropHint;

  /// No description provided for @inboxDropOrClickFiles.
  ///
  /// In en, this message translates to:
  /// **'Drop files here or click to attach'**
  String get inboxDropOrClickFiles;

  /// No description provided for @dropOrClickFiles.
  ///
  /// In en, this message translates to:
  /// **'Drop files here or click to attach'**
  String get dropOrClickFiles;

  /// No description provided for @inboxFileSizeLimit.
  ///
  /// In en, this message translates to:
  /// **'Max 50MB per file (Uploaded on send)'**
  String get inboxFileSizeLimit;

  /// No description provided for @fileSizeLimit.
  ///
  /// In en, this message translates to:
  /// **'Max 50MB per file (Uploaded on send)'**
  String get fileSizeLimit;

  /// No description provided for @inboxUploadReady.
  ///
  /// In en, this message translates to:
  /// **'Ready to upload'**
  String get inboxUploadReady;

  /// No description provided for @uploadReady.
  ///
  /// In en, this message translates to:
  /// **'Ready to upload'**
  String get uploadReady;

  /// No description provided for @inboxUploadInProgress.
  ///
  /// In en, this message translates to:
  /// **'Uploading...'**
  String get inboxUploadInProgress;

  /// No description provided for @uploadInProgress.
  ///
  /// In en, this message translates to:
  /// **'Uploading...'**
  String get uploadInProgress;

  /// No description provided for @inboxUploadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Upload Complete'**
  String get inboxUploadSuccess;

  /// No description provided for @uploadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Upload Complete'**
  String get uploadSuccess;

  /// No description provided for @inboxUploadFailed.
  ///
  /// In en, this message translates to:
  /// **'Upload Failed'**
  String get inboxUploadFailed;

  /// No description provided for @uploadFailed.
  ///
  /// In en, this message translates to:
  /// **'Upload Failed'**
  String get uploadFailed;

  /// No description provided for @inboxUploadRetry.
  ///
  /// In en, this message translates to:
  /// **'Retry'**
  String get inboxUploadRetry;

  /// No description provided for @uploadRetry.
  ///
  /// In en, this message translates to:
  /// **'Retry'**
  String get uploadRetry;

  /// Translated from inbox_total_files_summary
  ///
  /// In en, this message translates to:
  /// **'Total {count} files ({size})'**
  String inboxTotalFilesSummary(Object count, Object size);

  /// Translated from total_files_summary
  ///
  /// In en, this message translates to:
  /// **'Total {count} files ({size})'**
  String totalFilesSummary(Object count, Object size);

  /// No description provided for @inboxAttachFilesBtn.
  ///
  /// In en, this message translates to:
  /// **'Attach Files'**
  String get inboxAttachFilesBtn;

  /// No description provided for @attachFilesBtn.
  ///
  /// In en, this message translates to:
  /// **'Attach Files'**
  String get attachFilesBtn;

  /// No description provided for @inboxClearAllAttachments.
  ///
  /// In en, this message translates to:
  /// **'Clear All'**
  String get inboxClearAllAttachments;

  /// No description provided for @clearAllAttachments.
  ///
  /// In en, this message translates to:
  /// **'Clear All'**
  String get clearAllAttachments;

  /// No description provided for @inboxNoAttachments.
  ///
  /// In en, this message translates to:
  /// **'No attachments.'**
  String get inboxNoAttachments;

  /// No description provided for @noAttachments.
  ///
  /// In en, this message translates to:
  /// **'No attachments.'**
  String get noAttachments;

  /// No description provided for @inboxLoading.
  ///
  /// In en, this message translates to:
  /// **'Loading...'**
  String get inboxLoading;

  /// No description provided for @loading.
  ///
  /// In en, this message translates to:
  /// **'Loading...'**
  String get loading;

  /// No description provided for @inboxEmptyFolder.
  ///
  /// In en, this message translates to:
  /// **'This folder is empty.'**
  String get inboxEmptyFolder;

  /// No description provided for @emptyFolder.
  ///
  /// In en, this message translates to:
  /// **'This folder is empty.'**
  String get emptyFolder;

  /// Translated from inbox_unread_badge
  ///
  /// In en, this message translates to:
  /// **'{count} unread'**
  String inboxUnreadBadge(Object count);

  /// Translated from unread_badge
  ///
  /// In en, this message translates to:
  /// **'{count} unread'**
  String unreadBadge(Object count);

  /// No description provided for @inboxAllRead.
  ///
  /// In en, this message translates to:
  /// **'Mark All as Read'**
  String get inboxAllRead;

  /// No description provided for @allRead.
  ///
  /// In en, this message translates to:
  /// **'Mark All as Read'**
  String get allRead;

  /// No description provided for @inboxDetails.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get inboxDetails;

  /// No description provided for @inboxRecipientsList.
  ///
  /// In en, this message translates to:
  /// **'Recipients List'**
  String get inboxRecipientsList;

  /// No description provided for @recipientsList.
  ///
  /// In en, this message translates to:
  /// **'Recipients List'**
  String get recipientsList;

  /// No description provided for @inboxViewAllRecipients.
  ///
  /// In en, this message translates to:
  /// **'View All Recipients'**
  String get inboxViewAllRecipients;

  /// No description provided for @viewAllRecipients.
  ///
  /// In en, this message translates to:
  /// **'View All Recipients'**
  String get viewAllRecipients;

  /// No description provided for @inboxTo.
  ///
  /// In en, this message translates to:
  /// **'To'**
  String get inboxTo;

  /// No description provided for @to.
  ///
  /// In en, this message translates to:
  /// **'To'**
  String get to;

  /// No description provided for @inboxCc.
  ///
  /// In en, this message translates to:
  /// **'CC'**
  String get inboxCc;

  /// No description provided for @cc.
  ///
  /// In en, this message translates to:
  /// **'CC'**
  String get cc;

  /// No description provided for @inboxBcc.
  ///
  /// In en, this message translates to:
  /// **'BCC'**
  String get inboxBcc;

  /// No description provided for @bcc.
  ///
  /// In en, this message translates to:
  /// **'BCC'**
  String get bcc;

  /// No description provided for @inboxFrom.
  ///
  /// In en, this message translates to:
  /// **'From'**
  String get inboxFrom;

  /// No description provided for @from.
  ///
  /// In en, this message translates to:
  /// **'From'**
  String get from;

  /// No description provided for @inboxAt.
  ///
  /// In en, this message translates to:
  /// **'At'**
  String get inboxAt;

  /// No description provided for @at.
  ///
  /// In en, this message translates to:
  /// **'At'**
  String get at;

  /// No description provided for @inboxAttachmentDownload.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get inboxAttachmentDownload;

  /// No description provided for @attachmentDownload.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get attachmentDownload;

  /// No description provided for @inboxAttachmentDownloadAll.
  ///
  /// In en, this message translates to:
  /// **'Download All'**
  String get inboxAttachmentDownloadAll;

  /// No description provided for @attachmentDownloadAll.
  ///
  /// In en, this message translates to:
  /// **'Download All'**
  String get attachmentDownloadAll;

  /// No description provided for @inboxViewModeSplit.
  ///
  /// In en, this message translates to:
  /// **'Split View'**
  String get inboxViewModeSplit;

  /// No description provided for @viewModeSplit.
  ///
  /// In en, this message translates to:
  /// **'Split View'**
  String get viewModeSplit;

  /// No description provided for @inboxViewModeList.
  ///
  /// In en, this message translates to:
  /// **'List View (Popup Detail)'**
  String get inboxViewModeList;

  /// No description provided for @viewModeList.
  ///
  /// In en, this message translates to:
  /// **'List View (Popup Detail)'**
  String get viewModeList;

  /// No description provided for @inboxViewMode.
  ///
  /// In en, this message translates to:
  /// **'View Mode'**
  String get inboxViewMode;

  /// No description provided for @viewMode.
  ///
  /// In en, this message translates to:
  /// **'View Mode'**
  String get viewMode;

  /// No description provided for @inboxMessageDetailModal.
  ///
  /// In en, this message translates to:
  /// **'Message Details'**
  String get inboxMessageDetailModal;

  /// No description provided for @messageDetailModal.
  ///
  /// In en, this message translates to:
  /// **'Message Details'**
  String get messageDetailModal;

  /// No description provided for @inboxDragToResize.
  ///
  /// In en, this message translates to:
  /// **'Drag to resize (Double-click to reset)'**
  String get inboxDragToResize;

  /// No description provided for @dragToResize.
  ///
  /// In en, this message translates to:
  /// **'Drag to resize (Double-click to reset)'**
  String get dragToResize;

  /// No description provided for @inboxNewMessageReceived.
  ///
  /// In en, this message translates to:
  /// **'New message/mail received.'**
  String get inboxNewMessageReceived;

  /// No description provided for @newMessageReceived.
  ///
  /// In en, this message translates to:
  /// **'New message/mail received.'**
  String get newMessageReceived;

  /// No description provided for @inboxOriginalMessage.
  ///
  /// In en, this message translates to:
  /// **'Original Message'**
  String get inboxOriginalMessage;

  /// No description provided for @originalMessage.
  ///
  /// In en, this message translates to:
  /// **'Original Message'**
  String get originalMessage;

  /// No description provided for @inboxComposeMemoApproval.
  ///
  /// In en, this message translates to:
  /// **'Draft Memo Approval'**
  String get inboxComposeMemoApproval;

  /// No description provided for @composeMemoApproval.
  ///
  /// In en, this message translates to:
  /// **'Draft Memo Approval'**
  String get composeMemoApproval;

  /// No description provided for @inboxMemoApprovalTitle.
  ///
  /// In en, this message translates to:
  /// **'Draft Memo Approval'**
  String get inboxMemoApprovalTitle;

  /// No description provided for @memoApprovalTitle.
  ///
  /// In en, this message translates to:
  /// **'Draft Memo Approval'**
  String get memoApprovalTitle;

  /// No description provided for @inboxMemoApprovalDesc.
  ///
  /// In en, this message translates to:
  /// **'Write a proposal using the web editor, configure the approval route, and submit for approval.'**
  String get inboxMemoApprovalDesc;

  /// No description provided for @memoApprovalDesc.
  ///
  /// In en, this message translates to:
  /// **'Write a proposal using the web editor, configure the approval route, and submit for approval.'**
  String get memoApprovalDesc;

  /// No description provided for @inboxApprovalLine.
  ///
  /// In en, this message translates to:
  /// **'Approval Route'**
  String get inboxApprovalLine;

  /// No description provided for @inboxApprovalRouteSetting.
  ///
  /// In en, this message translates to:
  /// **'Approval Route Settings'**
  String get inboxApprovalRouteSetting;

  /// No description provided for @approvalRouteSetting.
  ///
  /// In en, this message translates to:
  /// **'Approval Route Settings'**
  String get approvalRouteSetting;

  /// No description provided for @inboxDrafter.
  ///
  /// In en, this message translates to:
  /// **'Drafter'**
  String get inboxDrafter;

  /// No description provided for @drafter.
  ///
  /// In en, this message translates to:
  /// **'Drafter'**
  String get drafter;

  /// No description provided for @inboxApprovalType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get inboxApprovalType;

  /// No description provided for @approvalType.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get approvalType;

  /// No description provided for @inboxTypeApproval.
  ///
  /// In en, this message translates to:
  /// **'Approval'**
  String get inboxTypeApproval;

  /// No description provided for @inboxTypeConsensus.
  ///
  /// In en, this message translates to:
  /// **'Agreement'**
  String get inboxTypeConsensus;

  /// No description provided for @typeConsensus.
  ///
  /// In en, this message translates to:
  /// **'Agreement'**
  String get typeConsensus;

  /// No description provided for @inboxTypeNotification.
  ///
  /// In en, this message translates to:
  /// **'Notification'**
  String get inboxTypeNotification;

  /// No description provided for @typeNotification.
  ///
  /// In en, this message translates to:
  /// **'Notification'**
  String get typeNotification;

  /// No description provided for @inboxParallelApproval.
  ///
  /// In en, this message translates to:
  /// **'Parallel Approval'**
  String get inboxParallelApproval;

  /// No description provided for @parallelApproval.
  ///
  /// In en, this message translates to:
  /// **'Parallel Approval'**
  String get parallelApproval;

  /// No description provided for @inboxParallelConsensus.
  ///
  /// In en, this message translates to:
  /// **'Parallel Agreement'**
  String get inboxParallelConsensus;

  /// No description provided for @parallelConsensus.
  ///
  /// In en, this message translates to:
  /// **'Parallel Agreement'**
  String get parallelConsensus;

  /// No description provided for @inboxStepOrder.
  ///
  /// In en, this message translates to:
  /// **'Step Order'**
  String get inboxStepOrder;

  /// No description provided for @stepOrder.
  ///
  /// In en, this message translates to:
  /// **'Step Order'**
  String get stepOrder;

  /// Translated from inbox_step_order_label
  ///
  /// In en, this message translates to:
  /// **'Step {order}'**
  String inboxStepOrderLabel(Object order);

  /// Translated from step_order_label
  ///
  /// In en, this message translates to:
  /// **'Step {order}'**
  String stepOrderLabel(Object order);

  /// No description provided for @inboxAddStep.
  ///
  /// In en, this message translates to:
  /// **'Add Step'**
  String get inboxAddStep;

  /// No description provided for @addStep.
  ///
  /// In en, this message translates to:
  /// **'Add Step'**
  String get addStep;

  /// No description provided for @inboxAddParallelStep.
  ///
  /// In en, this message translates to:
  /// **'Add Parallel'**
  String get inboxAddParallelStep;

  /// No description provided for @addParallelStep.
  ///
  /// In en, this message translates to:
  /// **'Add Parallel'**
  String get addParallelStep;

  /// No description provided for @inboxMoveUp.
  ///
  /// In en, this message translates to:
  /// **'Move Up'**
  String get inboxMoveUp;

  /// No description provided for @moveUp.
  ///
  /// In en, this message translates to:
  /// **'Move Up'**
  String get moveUp;

  /// No description provided for @inboxMoveDown.
  ///
  /// In en, this message translates to:
  /// **'Move Down'**
  String get inboxMoveDown;

  /// No description provided for @moveDown.
  ///
  /// In en, this message translates to:
  /// **'Move Down'**
  String get moveDown;

  /// No description provided for @inboxDeleteStep.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get inboxDeleteStep;

  /// No description provided for @deleteStep.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get deleteStep;

  /// No description provided for @inboxObservers.
  ///
  /// In en, this message translates to:
  /// **'Observers (CC)'**
  String get inboxObservers;

  /// No description provided for @inboxAddObserver.
  ///
  /// In en, this message translates to:
  /// **'Add Observer'**
  String get inboxAddObserver;

  /// No description provided for @addObserver.
  ///
  /// In en, this message translates to:
  /// **'Add Observer'**
  String get addObserver;

  /// No description provided for @inboxObserversDesc.
  ///
  /// In en, this message translates to:
  /// **'Observers are notified when the approval process is completed.'**
  String get inboxObserversDesc;

  /// No description provided for @observersDesc.
  ///
  /// In en, this message translates to:
  /// **'Observers are notified when the approval process is completed.'**
  String get observersDesc;

  /// No description provided for @inboxSubmitApproval.
  ///
  /// In en, this message translates to:
  /// **'Submit Approval'**
  String get inboxSubmitApproval;

  /// No description provided for @submitApproval.
  ///
  /// In en, this message translates to:
  /// **'Submit Approval'**
  String get submitApproval;

  /// No description provided for @inboxSubmitApprovalSuccess.
  ///
  /// In en, this message translates to:
  /// **'Memo approval request submitted successfully.'**
  String get inboxSubmitApprovalSuccess;

  /// No description provided for @submitApprovalSuccess.
  ///
  /// In en, this message translates to:
  /// **'Memo approval request submitted successfully.'**
  String get submitApprovalSuccess;

  /// No description provided for @inboxSubmitApprovalFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to submit memo approval request.'**
  String get inboxSubmitApprovalFailed;

  /// No description provided for @submitApprovalFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to submit memo approval request.'**
  String get submitApprovalFailed;

  /// No description provided for @inboxApprovalStatus.
  ///
  /// In en, this message translates to:
  /// **'Approval Status'**
  String get inboxApprovalStatus;

  /// No description provided for @approvalStatus.
  ///
  /// In en, this message translates to:
  /// **'Approval Status'**
  String get approvalStatus;

  /// No description provided for @inboxApprovalLineEmpty.
  ///
  /// In en, this message translates to:
  /// **'Please configure at least one approver in the route.'**
  String get inboxApprovalLineEmpty;

  /// No description provided for @approvalLineEmpty.
  ///
  /// In en, this message translates to:
  /// **'Please configure at least one approver in the route.'**
  String get approvalLineEmpty;

  /// No description provided for @inboxApprove.
  ///
  /// In en, this message translates to:
  /// **'Approve'**
  String get inboxApprove;

  /// No description provided for @inboxReject.
  ///
  /// In en, this message translates to:
  /// **'Reject'**
  String get inboxReject;

  /// No description provided for @inboxConsensusAgree.
  ///
  /// In en, this message translates to:
  /// **'Agree'**
  String get inboxConsensusAgree;

  /// No description provided for @consensusAgree.
  ///
  /// In en, this message translates to:
  /// **'Agree'**
  String get consensusAgree;

  /// No description provided for @inboxApprovalComment.
  ///
  /// In en, this message translates to:
  /// **'Approval Comment'**
  String get inboxApprovalComment;

  /// No description provided for @approvalComment.
  ///
  /// In en, this message translates to:
  /// **'Approval Comment'**
  String get approvalComment;

  /// No description provided for @inboxRejectReason.
  ///
  /// In en, this message translates to:
  /// **'Rejection Reason'**
  String get inboxRejectReason;

  /// No description provided for @rejectReason.
  ///
  /// In en, this message translates to:
  /// **'Rejection Reason'**
  String get rejectReason;

  /// No description provided for @inboxRejectReasonRequired.
  ///
  /// In en, this message translates to:
  /// **'Please provide a rejection reason.'**
  String get inboxRejectReasonRequired;

  /// No description provided for @rejectReasonRequired.
  ///
  /// In en, this message translates to:
  /// **'Please provide a rejection reason.'**
  String get rejectReasonRequired;

  /// No description provided for @inboxApproveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval request approved.'**
  String get inboxApproveSuccess;

  /// No description provided for @approveSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval request approved.'**
  String get approveSuccess;

  /// No description provided for @inboxRejectSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval request rejected.'**
  String get inboxRejectSuccess;

  /// No description provided for @rejectSuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully rejected candidate.'**
  String get rejectSuccess;

  /// No description provided for @inboxApprovalActionFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to process approval action.'**
  String get inboxApprovalActionFailed;

  /// No description provided for @approvalActionFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to process approval action.'**
  String get approvalActionFailed;

  /// No description provided for @inboxMemoContent.
  ///
  /// In en, this message translates to:
  /// **'Draft Content'**
  String get inboxMemoContent;

  /// No description provided for @memoContent.
  ///
  /// In en, this message translates to:
  /// **'Draft Content'**
  String get memoContent;

  /// No description provided for @inboxCancelApproval.
  ///
  /// In en, this message translates to:
  /// **'Cancel Submission'**
  String get inboxCancelApproval;

  /// No description provided for @cancelApproval.
  ///
  /// In en, this message translates to:
  /// **'Cancel Submission'**
  String get cancelApproval;

  /// No description provided for @inboxCancelApprovalConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to cancel this approval submission? All pending steps will be cancelled.'**
  String get inboxCancelApprovalConfirm;

  /// No description provided for @cancelApprovalConfirm.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to cancel this approval submission? All pending steps will be cancelled.'**
  String get cancelApprovalConfirm;

  /// No description provided for @inboxCancelApprovalReason.
  ///
  /// In en, this message translates to:
  /// **'Cancellation Reason'**
  String get inboxCancelApprovalReason;

  /// No description provided for @cancelApprovalReason.
  ///
  /// In en, this message translates to:
  /// **'Cancellation Reason'**
  String get cancelApprovalReason;

  /// No description provided for @inboxCancelApprovalReasonPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter cancellation reason (optional)'**
  String get inboxCancelApprovalReasonPlaceholder;

  /// No description provided for @cancelApprovalReasonPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter cancellation reason (optional)'**
  String get cancelApprovalReasonPlaceholder;

  /// No description provided for @inboxCancelApprovalSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval request has been cancelled.'**
  String get inboxCancelApprovalSuccess;

  /// No description provided for @cancelApprovalSuccess.
  ///
  /// In en, this message translates to:
  /// **'Approval request has been cancelled.'**
  String get cancelApprovalSuccess;

  /// No description provided for @inboxCancelApprovalFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to cancel approval request.'**
  String get inboxCancelApprovalFailed;

  /// No description provided for @cancelApprovalFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to cancel approval request.'**
  String get cancelApprovalFailed;

  /// No description provided for @inboxStatusCancelled.
  ///
  /// In en, this message translates to:
  /// **'Cancelled'**
  String get inboxStatusCancelled;

  /// No description provided for @selectNodeToViewRecords.
  ///
  /// In en, this message translates to:
  /// **'Select a classification node from the tree to view master records.'**
  String get selectNodeToViewRecords;

  /// No description provided for @diffDetails.
  ///
  /// In en, this message translates to:
  /// **'Change History Details'**
  String get diffDetails;

  /// No description provided for @advancedSearch.
  ///
  /// In en, this message translates to:
  /// **'Advanced Search'**
  String get advancedSearch;

  /// No description provided for @advancedSearchCondition.
  ///
  /// In en, this message translates to:
  /// **'Advanced Search'**
  String get advancedSearchCondition;

  /// No description provided for @noFieldsToExport.
  ///
  /// In en, this message translates to:
  /// **'No field definitions found to export.'**
  String get noFieldsToExport;

  /// No description provided for @downloadTemplateSuccess.
  ///
  /// In en, this message translates to:
  /// **'Template downloaded successfully.'**
  String get downloadTemplateSuccess;

  /// No description provided for @downloadTemplateFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to download template.'**
  String get downloadTemplateFailed;

  /// No description provided for @deleteRow.
  ///
  /// In en, this message translates to:
  /// **'Delete Row'**
  String get deleteRow;

  /// No description provided for @emptyTableData.
  ///
  /// In en, this message translates to:
  /// **'No rows defined. Click \'+ Add Row\' to insert a record row.'**
  String get emptyTableData;

  /// No description provided for @clearAllRows.
  ///
  /// In en, this message translates to:
  /// **'Clear All Rows'**
  String get clearAllRows;

  /// Translated from total_rows_count
  ///
  /// In en, this message translates to:
  /// **'Total {count} rows'**
  String totalRowsCount(Object count);

  /// No description provided for @masterDataRecordList.
  ///
  /// In en, this message translates to:
  /// **'Master Data Record List'**
  String get masterDataRecordList;

  /// No description provided for @resetAll.
  ///
  /// In en, this message translates to:
  /// **'Reset All'**
  String get resetAll;

  /// No description provided for @createRequest.
  ///
  /// In en, this message translates to:
  /// **'Request Creation'**
  String get createRequest;

  /// No description provided for @createRecord.
  ///
  /// In en, this message translates to:
  /// **'Create Record'**
  String get createRecord;

  /// No description provided for @bulkUpload.
  ///
  /// In en, this message translates to:
  /// **'Bulk Upload'**
  String get bulkUpload;

  /// Translated from applied_filters_count
  ///
  /// In en, this message translates to:
  /// **'Applied Filters: {count}'**
  String appliedFiltersCount(Object count);

  /// No description provided for @targetTypeRecord.
  ///
  /// In en, this message translates to:
  /// **'Master Record'**
  String get targetTypeRecord;

  /// No description provided for @rollbackRecord.
  ///
  /// In en, this message translates to:
  /// **'Rollback to Past Version'**
  String get rollbackRecord;

  /// No description provided for @rollbackBtn.
  ///
  /// In en, this message translates to:
  /// **'Rollback to this version'**
  String get rollbackBtn;

  /// No description provided for @rollbackConfirmTitle.
  ///
  /// In en, this message translates to:
  /// **'Confirm Record Rollback'**
  String get rollbackConfirmTitle;

  /// Translated from rollback_confirm_desc
  ///
  /// In en, this message translates to:
  /// **'Create an approval request to rollback to Version {version} data. Please provide a reason.'**
  String rollbackConfirmDesc(Object version);

  /// No description provided for @rollbackReason.
  ///
  /// In en, this message translates to:
  /// **'Rollback Reason'**
  String get rollbackReason;

  /// No description provided for @rollbackReasonPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter reason for rolling back (e.g. erroneous edit reversion)'**
  String get rollbackReasonPlaceholder;

  /// Translated from rollback_success
  ///
  /// In en, this message translates to:
  /// **'Rollback request for Version {version} submitted successfully.'**
  String rollbackSuccess(Object version);

  /// No description provided for @rollbackDiffPreview.
  ///
  /// In en, this message translates to:
  /// **'Data Diff Preview for Rollback'**
  String get rollbackDiffPreview;

  /// No description provided for @currentData.
  ///
  /// In en, this message translates to:
  /// **'Current Data'**
  String get currentData;

  /// Translated from target_version_data
  ///
  /// In en, this message translates to:
  /// **'Target Version Data (Version {version})'**
  String targetVersionData(Object version);

  /// No description provided for @asyncExport.
  ///
  /// In en, this message translates to:
  /// **'Async Export'**
  String get asyncExport;

  /// No description provided for @dataLineageTitle.
  ///
  /// In en, this message translates to:
  /// **'Master Data Lifecycle & Lineage'**
  String get dataLineageTitle;

  /// Translated from warning_delete_records
  ///
  /// In en, this message translates to:
  /// **'Deleting this field permanently purges data in {count} active record(s).'**
  String warningDeleteRecords(Object count);

  /// No description provided for @warningDeleteRecordsZero.
  ///
  /// In en, this message translates to:
  /// **'There are 0 active records containing values for this field.'**
  String get warningDeleteRecordsZero;

  /// No description provided for @warningModifyRecords.
  ///
  /// In en, this message translates to:
  /// **'Please verify data type compatibility for existing records.'**
  String get warningModifyRecords;

  /// No description provided for @warningModifyRecordsZero.
  ///
  /// In en, this message translates to:
  /// **'0 active records affected; no risk of data loss.'**
  String get warningModifyRecordsZero;

  /// No description provided for @targetTypeRECORD.
  ///
  /// In en, this message translates to:
  /// **'Master Record Creation'**
  String get targetTypeRECORD;

  /// No description provided for @targetTypeRECORDUPDATE.
  ///
  /// In en, this message translates to:
  /// **'Master Record Update'**
  String get targetTypeRECORDUPDATE;

  /// No description provided for @targetTypeRECORDDELETE.
  ///
  /// In en, this message translates to:
  /// **'Master Record Deletion'**
  String get targetTypeRECORDDELETE;

  /// No description provided for @targetTypeRECORDMERGE.
  ///
  /// In en, this message translates to:
  /// **'Master Record Merge'**
  String get targetTypeRECORDMERGE;

  /// No description provided for @recordIdAttr.
  ///
  /// In en, this message translates to:
  /// **'ID Attribute'**
  String get recordIdAttr;

  /// No description provided for @recordNameAttr.
  ///
  /// In en, this message translates to:
  /// **'Name Attribute'**
  String get recordNameAttr;

  /// No description provided for @affectedRecordsBreakdown.
  ///
  /// In en, this message translates to:
  /// **'Affected Record Samples (Breakdown)'**
  String get affectedRecordsBreakdown;

  /// No description provided for @asyncExportTitle.
  ///
  /// In en, this message translates to:
  /// **'Bulk Master Data Async Export'**
  String get asyncExportTitle;

  /// No description provided for @asyncExportDesc.
  ///
  /// In en, this message translates to:
  /// **'Exports all master data of the selected domain to an Excel file via background async processing.'**
  String get asyncExportDesc;

  /// No description provided for @excelViewerTitle.
  ///
  /// In en, this message translates to:
  /// **'Messenger Excel Viewer'**
  String get excelViewerTitle;

  /// No description provided for @excelViewerBtn.
  ///
  /// In en, this message translates to:
  /// **'Viewer'**
  String get excelViewerBtn;

  /// No description provided for @excelViewerOpen.
  ///
  /// In en, this message translates to:
  /// **'Open Excel Dedicated Viewer'**
  String get excelViewerOpen;

  /// No description provided for @searchInExcel.
  ///
  /// In en, this message translates to:
  /// **'Search in Sheet...'**
  String get searchInExcel;

  /// No description provided for @excelLoading.
  ///
  /// In en, this message translates to:
  /// **'Loading and parsing excel worksheet data...'**
  String get excelLoading;

  /// No description provided for @noExcelData.
  ///
  /// In en, this message translates to:
  /// **'No excel cell data to display.'**
  String get noExcelData;

  /// No description provided for @excelCopyTip.
  ///
  /// In en, this message translates to:
  /// **'Click a cell to inspect address or use copy buttons to copy table data format directly.'**
  String get excelCopyTip;

  /// No description provided for @copyAsExcelText.
  ///
  /// In en, this message translates to:
  /// **'📋 Copy as Excel Text (Table)'**
  String get copyAsExcelText;

  /// No description provided for @tableRecordCount.
  ///
  /// In en, this message translates to:
  /// **'Total Records'**
  String get tableRecordCount;

  /// No description provided for @copyTableExcel.
  ///
  /// In en, this message translates to:
  /// **'📋 Copy Entire Table Data (Excel)'**
  String get copyTableExcel;

  /// No description provided for @excelSpreadsheetViewerTitle.
  ///
  /// In en, this message translates to:
  /// **'MS Excel Spreadsheet Data Viewer'**
  String get excelSpreadsheetViewerTitle;

  /// No description provided for @copyRawTableExcel.
  ///
  /// In en, this message translates to:
  /// **'🔑 Copy 100% Raw Table Data'**
  String get copyRawTableExcel;

  /// No description provided for @excelModalTip.
  ///
  /// In en, this message translates to:
  /// **'Drag or Shift/Ctrl click to select multiple cells. Press Ctrl+C to copy directly into Excel.'**
  String get excelModalTip;

  /// Translated from excel_cells_selected
  ///
  /// In en, this message translates to:
  /// **'{count} cells selected'**
  String excelCellsSelected(Object count);

  /// Translated from excel_cell_copied
  ///
  /// In en, this message translates to:
  /// **'📋 [{address}] Cell data \"{value}\" copied!'**
  String excelCellCopied(Object address, Object value);

  /// Translated from excel_range_copied
  ///
  /// In en, this message translates to:
  /// **'📋 {count} cells data copied! (Ready for Ctrl+V in Excel)'**
  String excelRangeCopied(Object count);

  /// Translated from excel_table_copied
  ///
  /// In en, this message translates to:
  /// **'📋 All {rows} rows table data copied! (Ready for Ctrl+V in Excel)'**
  String excelTableCopied(Object rows);

  /// No description provided for @startExport.
  ///
  /// In en, this message translates to:
  /// **'Start Export'**
  String get startExport;

  /// No description provided for @exportProgress.
  ///
  /// In en, this message translates to:
  /// **'Export Progress Status'**
  String get exportProgress;

  /// No description provided for @affectedRecords.
  ///
  /// In en, this message translates to:
  /// **'Affected Records'**
  String get affectedRecords;

  /// No description provided for @activeRecords.
  ///
  /// In en, this message translates to:
  /// **'Active Records'**
  String get activeRecords;

  /// No description provided for @baselineRecord.
  ///
  /// In en, this message translates to:
  /// **'Baseline Record:'**
  String get baselineRecord;

  /// No description provided for @btnCheckDuplicate.
  ///
  /// In en, this message translates to:
  /// **'Check Availability'**
  String get btnCheckDuplicate;

  /// No description provided for @compareRecords.
  ///
  /// In en, this message translates to:
  /// **'Compare Records'**
  String get compareRecords;

  /// No description provided for @compareRecordsTitle.
  ///
  /// In en, this message translates to:
  /// **'Compare Selected Records'**
  String get compareRecordsTitle;

  /// No description provided for @unmergePreviewTitle.
  ///
  /// In en, this message translates to:
  /// **'Golden Record Unmerge Preview'**
  String get unmergePreviewTitle;

  /// No description provided for @unmergeWarningDesc.
  ///
  /// In en, this message translates to:
  /// **'Unmerging the golden record will restore individual source records back to their original states prior to merging.'**
  String get unmergeWarningDesc;

  /// No description provided for @currentGoldenRecord.
  ///
  /// In en, this message translates to:
  /// **'Current Golden Record'**
  String get currentGoldenRecord;

  /// Translated from restoring_records_count
  ///
  /// In en, this message translates to:
  /// **'Source Records to be Restored ({count})'**
  String restoringRecordsCount(Object count);

  /// No description provided for @unmergeConfirmBtn.
  ///
  /// In en, this message translates to:
  /// **'Confirm Unmerge'**
  String get unmergeConfirmBtn;

  /// No description provided for @sourceRecord.
  ///
  /// In en, this message translates to:
  /// **'Source Record'**
  String get sourceRecord;

  /// No description provided for @unnamedRecord.
  ///
  /// In en, this message translates to:
  /// **'Unnamed Record'**
  String get unnamedRecord;

  /// No description provided for @duplicateRequestWarning.
  ///
  /// In en, this message translates to:
  /// **'Some domains are already pending approval and were skipped.'**
  String get duplicateRequestWarning;

  /// No description provided for @errorFieldDomainMismatch.
  ///
  /// In en, this message translates to:
  /// **'Field does not belong to the specified domain.'**
  String get errorFieldDomainMismatch;

  /// No description provided for @errorFieldNodeMismatch.
  ///
  /// In en, this message translates to:
  /// **'Field does not belong to the specified node.'**
  String get errorFieldNodeMismatch;

  /// No description provided for @errorNodeDomainMismatch.
  ///
  /// In en, this message translates to:
  /// **'Node does not belong to the specified domain.'**
  String get errorNodeDomainMismatch;

  /// No description provided for @errorSectorDomainMismatch.
  ///
  /// In en, this message translates to:
  /// **'Sector does not belong to the domain.'**
  String get errorSectorDomainMismatch;

  /// No description provided for @exportExcel.
  ///
  /// In en, this message translates to:
  /// **'Export to Excel'**
  String get exportExcel;

  /// No description provided for @goToMatchReview.
  ///
  /// In en, this message translates to:
  /// **'Go to Match Review'**
  String get goToMatchReview;

  /// No description provided for @recordVersionNode.
  ///
  /// In en, this message translates to:
  /// **'Version History'**
  String get recordVersionNode;

  /// No description provided for @masterRecordNode.
  ///
  /// In en, this message translates to:
  /// **'Master Record'**
  String get masterRecordNode;

  /// No description provided for @recordsCountSuffix.
  ///
  /// In en, this message translates to:
  /// **'records'**
  String get recordsCountSuffix;

  /// No description provided for @secondaryNodesTab.
  ///
  /// In en, this message translates to:
  /// **'Secondary Nodes'**
  String get secondaryNodesTab;

  /// No description provided for @masterData.
  ///
  /// In en, this message translates to:
  /// **'Master Data'**
  String get masterData;

  /// No description provided for @lineageNodeMasterDesc.
  ///
  /// In en, this message translates to:
  /// **'Master Record: Latest Golden Record under unified management'**
  String get lineageNodeMasterDesc;

  /// No description provided for @installRequirePwdMatch.
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match.'**
  String get installRequirePwdMatch;

  /// No description provided for @managedMasterRecords.
  ///
  /// In en, this message translates to:
  /// **'Managed Master Records'**
  String get managedMasterRecords;

  /// No description provided for @matchReviewTitle.
  ///
  /// In en, this message translates to:
  /// **'Match Review'**
  String get matchReviewTitle;

  /// No description provided for @matchReviewDesc.
  ///
  /// In en, this message translates to:
  /// **'Review potential match candidates with high similarity to approve or reject master data merging.'**
  String get matchReviewDesc;

  /// No description provided for @matchReviewDomainSelect.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get matchReviewDomainSelect;

  /// No description provided for @domainSelect.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get domainSelect;

  /// No description provided for @matchReviewRefresh.
  ///
  /// In en, this message translates to:
  /// **'Refresh'**
  String get matchReviewRefresh;

  /// No description provided for @matchReviewBatchConfirm.
  ///
  /// In en, this message translates to:
  /// **'Batch Confirm'**
  String get matchReviewBatchConfirm;

  /// No description provided for @batchConfirm.
  ///
  /// In en, this message translates to:
  /// **'Batch Confirm'**
  String get batchConfirm;

  /// No description provided for @matchReviewBatchReject.
  ///
  /// In en, this message translates to:
  /// **'Batch Reject'**
  String get matchReviewBatchReject;

  /// No description provided for @batchReject.
  ///
  /// In en, this message translates to:
  /// **'Batch Reject'**
  String get batchReject;

  /// No description provided for @matchReviewExistingRecord.
  ///
  /// In en, this message translates to:
  /// **'Existing Master Record'**
  String get matchReviewExistingRecord;

  /// No description provided for @existingRecord.
  ///
  /// In en, this message translates to:
  /// **'Existing Master Record'**
  String get existingRecord;

  /// No description provided for @matchReviewIncomingData.
  ///
  /// In en, this message translates to:
  /// **'Incoming New Record'**
  String get matchReviewIncomingData;

  /// No description provided for @incomingData.
  ///
  /// In en, this message translates to:
  /// **'Incoming New Record'**
  String get incomingData;

  /// No description provided for @matchReviewRejectNew.
  ///
  /// In en, this message translates to:
  /// **'Reject'**
  String get matchReviewRejectNew;

  /// No description provided for @rejectNew.
  ///
  /// In en, this message translates to:
  /// **'Reject'**
  String get rejectNew;

  /// No description provided for @matchReviewConfirmMerge.
  ///
  /// In en, this message translates to:
  /// **'Confirm Merge'**
  String get matchReviewConfirmMerge;

  /// No description provided for @matchReviewStatusPending.
  ///
  /// In en, this message translates to:
  /// **'Pending Review'**
  String get matchReviewStatusPending;

  /// No description provided for @matchReviewStatusConfirmed.
  ///
  /// In en, this message translates to:
  /// **'Confirmed Merge'**
  String get matchReviewStatusConfirmed;

  /// No description provided for @statusConfirmed.
  ///
  /// In en, this message translates to:
  /// **'Confirmed Merge'**
  String get statusConfirmed;

  /// No description provided for @matchReviewStatusRejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get matchReviewStatusRejected;

  /// No description provided for @matchReviewSimilarityScore.
  ///
  /// In en, this message translates to:
  /// **'Similarity Score'**
  String get matchReviewSimilarityScore;

  /// No description provided for @similarityScore.
  ///
  /// In en, this message translates to:
  /// **'Similarity Score'**
  String get similarityScore;

  /// No description provided for @matchReviewStatusFilter.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get matchReviewStatusFilter;

  /// No description provided for @matchReviewRejectSuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully rejected candidate.'**
  String get matchReviewRejectSuccess;

  /// No description provided for @matchReviewRejectFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to reject candidate.'**
  String get matchReviewRejectFail;

  /// No description provided for @rejectFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to reject candidate.'**
  String get rejectFail;

  /// No description provided for @matchReviewConfirmSuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully confirmed candidate merge.'**
  String get matchReviewConfirmSuccess;

  /// No description provided for @confirmSuccess.
  ///
  /// In en, this message translates to:
  /// **'Successfully confirmed candidate merge.'**
  String get confirmSuccess;

  /// No description provided for @matchReviewConfirmFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to confirm candidate merge.'**
  String get matchReviewConfirmFail;

  /// No description provided for @confirmFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to confirm candidate merge.'**
  String get confirmFail;

  /// No description provided for @matchingRulesTitle.
  ///
  /// In en, this message translates to:
  /// **'Matching Rules Management'**
  String get matchingRulesTitle;

  /// No description provided for @matchingRulesDesc.
  ///
  /// In en, this message translates to:
  /// **'Configure EXACT / FUZZY matching rules and similarity thresholds for duplicate record identification.'**
  String get matchingRulesDesc;

  /// No description provided for @msgPasswordMismatch.
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match.'**
  String get msgPasswordMismatch;

  /// No description provided for @noDomainRecords.
  ///
  /// In en, this message translates to:
  /// **'No master records found.'**
  String get noDomainRecords;

  /// No description provided for @pendingMatchCandidates.
  ///
  /// In en, this message translates to:
  /// **'Pending Match Candidates'**
  String get pendingMatchCandidates;

  /// No description provided for @permMasterManagement.
  ///
  /// In en, this message translates to:
  /// **'Permission Master Management'**
  String get permMasterManagement;

  /// No description provided for @permMasterTitle.
  ///
  /// In en, this message translates to:
  /// **'Permission Master Group List'**
  String get permMasterTitle;

  /// No description provided for @potentialDuplicates.
  ///
  /// In en, this message translates to:
  /// **'Potential Duplicates'**
  String get potentialDuplicates;

  /// No description provided for @recordCountUnit.
  ///
  /// In en, this message translates to:
  /// **'records'**
  String get recordCountUnit;

  /// No description provided for @recordCreationTrends.
  ///
  /// In en, this message translates to:
  /// **'Record Creation Trends'**
  String get recordCreationTrends;

  /// No description provided for @recordSaveFailed.
  ///
  /// In en, this message translates to:
  /// **'No input data.'**
  String get recordSaveFailed;

  /// No description provided for @records.
  ///
  /// In en, this message translates to:
  /// **'Data Records'**
  String get records;

  /// No description provided for @recordsManagement.
  ///
  /// In en, this message translates to:
  /// **'Master Data Records Management'**
  String get recordsManagement;

  /// No description provided for @recordsManagementDesc.
  ///
  /// In en, this message translates to:
  /// **'Query, create, bulk modify, and execute survivorship merge for master data records by domain.'**
  String get recordsManagementDesc;

  /// No description provided for @roleRecordManager.
  ///
  /// In en, this message translates to:
  /// **'Record Manager'**
  String get roleRecordManager;

  /// No description provided for @selectRecordDoubleclick.
  ///
  /// In en, this message translates to:
  /// **'Double click a record from the list to select'**
  String get selectRecordDoubleclick;

  /// No description provided for @exportRoles.
  ///
  /// In en, this message translates to:
  /// **'Export Role Template'**
  String get exportRoles;

  /// No description provided for @importRoles.
  ///
  /// In en, this message translates to:
  /// **'Import Role Template'**
  String get importRoles;

  /// No description provided for @exportRolesSuccess.
  ///
  /// In en, this message translates to:
  /// **'Role template exported successfully.'**
  String get exportRolesSuccess;

  /// No description provided for @exportRolesFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to export role template.'**
  String get exportRolesFail;

  /// No description provided for @importRolesSuccess.
  ///
  /// In en, this message translates to:
  /// **'Role template imported successfully.'**
  String get importRolesSuccess;

  /// No description provided for @importRolesFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to import role template.'**
  String get importRolesFail;

  /// No description provided for @importRolesConfirm.
  ///
  /// In en, this message translates to:
  /// **'Do you want to overwrite existing roles and permissions with the uploaded backup file?'**
  String get importRolesConfirm;

  /// No description provided for @removeFilter.
  ///
  /// In en, this message translates to:
  /// **'Remove Filter'**
  String get removeFilter;

  /// No description provided for @excelUploaderTitle.
  ///
  /// In en, this message translates to:
  /// **'Excel Data Batch Upload'**
  String get excelUploaderTitle;

  /// No description provided for @excelUploaderStep1.
  ///
  /// In en, this message translates to:
  /// **'1. Upload File'**
  String get excelUploaderStep1;

  /// No description provided for @step1.
  ///
  /// In en, this message translates to:
  /// **'1. Upload File'**
  String get step1;

  /// No description provided for @excelUploaderStep2.
  ///
  /// In en, this message translates to:
  /// **'2. Column Mapping'**
  String get excelUploaderStep2;

  /// No description provided for @step2.
  ///
  /// In en, this message translates to:
  /// **'2. Column Mapping'**
  String get step2;

  /// No description provided for @excelUploaderStep3.
  ///
  /// In en, this message translates to:
  /// **'3. Pre-validation Report'**
  String get excelUploaderStep3;

  /// No description provided for @step3.
  ///
  /// In en, this message translates to:
  /// **'3. Pre-validation Report'**
  String get step3;

  /// No description provided for @excelUploaderStep4.
  ///
  /// In en, this message translates to:
  /// **'4. Data Processing'**
  String get excelUploaderStep4;

  /// No description provided for @step4.
  ///
  /// In en, this message translates to:
  /// **'4. Data Processing'**
  String get step4;

  /// No description provided for @excelUploaderDownloadTemplate.
  ///
  /// In en, this message translates to:
  /// **'Download Excel Template'**
  String get excelUploaderDownloadTemplate;

  /// No description provided for @excelUploaderDragDropFile.
  ///
  /// In en, this message translates to:
  /// **'Drag and drop your Excel file here or click to browse'**
  String get excelUploaderDragDropFile;

  /// No description provided for @dragDropFile.
  ///
  /// In en, this message translates to:
  /// **'Drag and drop your Excel file here or click to browse'**
  String get dragDropFile;

  /// No description provided for @excelUploaderSupportedFormats.
  ///
  /// In en, this message translates to:
  /// **'Supported formats: .xlsx, .xls'**
  String get excelUploaderSupportedFormats;

  /// No description provided for @supportedFormats.
  ///
  /// In en, this message translates to:
  /// **'Supported formats: .xlsx, .xls'**
  String get supportedFormats;

  /// No description provided for @excelUploaderSelectedFile.
  ///
  /// In en, this message translates to:
  /// **'Selected File'**
  String get excelUploaderSelectedFile;

  /// No description provided for @selectedFile.
  ///
  /// In en, this message translates to:
  /// **'Selected File'**
  String get selectedFile;

  /// No description provided for @excelUploaderReselectFile.
  ///
  /// In en, this message translates to:
  /// **'Select Another File'**
  String get excelUploaderReselectFile;

  /// No description provided for @reselectFile.
  ///
  /// In en, this message translates to:
  /// **'Select Another File'**
  String get reselectFile;

  /// No description provided for @excelUploaderParsingExcel.
  ///
  /// In en, this message translates to:
  /// **'Analyzing Excel file...'**
  String get excelUploaderParsingExcel;

  /// No description provided for @parsingExcel.
  ///
  /// In en, this message translates to:
  /// **'Analyzing Excel file...'**
  String get parsingExcel;

  /// No description provided for @excelUploaderSourceColumn.
  ///
  /// In en, this message translates to:
  /// **'Excel Column Name'**
  String get excelUploaderSourceColumn;

  /// No description provided for @sourceColumn.
  ///
  /// In en, this message translates to:
  /// **'Excel Column Name'**
  String get sourceColumn;

  /// No description provided for @excelUploaderSampleData.
  ///
  /// In en, this message translates to:
  /// **'Sample Data'**
  String get excelUploaderSampleData;

  /// No description provided for @sampleData.
  ///
  /// In en, this message translates to:
  /// **'Sample Data'**
  String get sampleData;

  /// No description provided for @excelUploaderTargetField.
  ///
  /// In en, this message translates to:
  /// **'Target System Field'**
  String get excelUploaderTargetField;

  /// No description provided for @excelUploaderAutoMapped.
  ///
  /// In en, this message translates to:
  /// **'Auto Mapped'**
  String get excelUploaderAutoMapped;

  /// No description provided for @autoMapped.
  ///
  /// In en, this message translates to:
  /// **'Auto Mapped'**
  String get autoMapped;

  /// No description provided for @excelUploaderManualMapping.
  ///
  /// In en, this message translates to:
  /// **'Manual Mapping Required'**
  String get excelUploaderManualMapping;

  /// No description provided for @manualMapping.
  ///
  /// In en, this message translates to:
  /// **'Manual Mapping Required'**
  String get manualMapping;

  /// No description provided for @excelUploaderIgnoreColumn.
  ///
  /// In en, this message translates to:
  /// **'-- Ignore Column --'**
  String get excelUploaderIgnoreColumn;

  /// No description provided for @ignoreColumn.
  ///
  /// In en, this message translates to:
  /// **'-- Ignore Column --'**
  String get ignoreColumn;

  /// No description provided for @excelUploaderRowValidating.
  ///
  /// In en, this message translates to:
  /// **'Running row-level DQ validation...'**
  String get excelUploaderRowValidating;

  /// No description provided for @rowValidating.
  ///
  /// In en, this message translates to:
  /// **'Running row-level DQ validation...'**
  String get rowValidating;

  /// No description provided for @excelUploaderAllRowsValid.
  ///
  /// In en, this message translates to:
  /// **'All rows passed DQ validation!'**
  String get excelUploaderAllRowsValid;

  /// No description provided for @allRowsValid.
  ///
  /// In en, this message translates to:
  /// **'All rows passed DQ validation!'**
  String get allRowsValid;

  /// Translated from excel_uploader_violations_found
  ///
  /// In en, this message translates to:
  /// **'DQ violations found in {count} row(s).'**
  String excelUploaderViolationsFound(Object count);

  /// Translated from violations_found
  ///
  /// In en, this message translates to:
  /// **'DQ violations found in {count} row(s).'**
  String violationsFound(Object count);

  /// Translated from excel_uploader_validation_summary
  ///
  /// In en, this message translates to:
  /// **'Total {total} rows: {valid} valid · {invalid} invalid'**
  String excelUploaderValidationSummary(
    Object total,
    Object valid,
    Object invalid,
  );

  /// Translated from validation_summary
  ///
  /// In en, this message translates to:
  /// **'Total {total} rows: {valid} valid · {invalid} invalid'**
  String validationSummary(Object total, Object valid, Object invalid);

  /// No description provided for @excelUploaderShowOnlyErrors.
  ///
  /// In en, this message translates to:
  /// **'Show Only Violated Rows'**
  String get excelUploaderShowOnlyErrors;

  /// No description provided for @showOnlyErrors.
  ///
  /// In en, this message translates to:
  /// **'Show Only Violated Rows'**
  String get showOnlyErrors;

  /// No description provided for @excelUploaderColRow.
  ///
  /// In en, this message translates to:
  /// **'Row'**
  String get excelUploaderColRow;

  /// No description provided for @colRow.
  ///
  /// In en, this message translates to:
  /// **'Row'**
  String get colRow;

  /// No description provided for @excelUploaderColResult.
  ///
  /// In en, this message translates to:
  /// **'Result'**
  String get excelUploaderColResult;

  /// No description provided for @colResult.
  ///
  /// In en, this message translates to:
  /// **'Result'**
  String get colResult;

  /// No description provided for @excelUploaderColViolatedField.
  ///
  /// In en, this message translates to:
  /// **'Violated Field'**
  String get excelUploaderColViolatedField;

  /// No description provided for @colViolatedField.
  ///
  /// In en, this message translates to:
  /// **'Violated Field'**
  String get colViolatedField;

  /// No description provided for @excelUploaderColSeverity.
  ///
  /// In en, this message translates to:
  /// **'Severity'**
  String get excelUploaderColSeverity;

  /// No description provided for @colSeverity.
  ///
  /// In en, this message translates to:
  /// **'Severity'**
  String get colSeverity;

  /// No description provided for @excelUploaderColViolationReason.
  ///
  /// In en, this message translates to:
  /// **'Violation Reason'**
  String get excelUploaderColViolationReason;

  /// No description provided for @colViolationReason.
  ///
  /// In en, this message translates to:
  /// **'Violation Reason'**
  String get colViolationReason;

  /// No description provided for @excelUploaderColInputValue.
  ///
  /// In en, this message translates to:
  /// **'Input Value'**
  String get excelUploaderColInputValue;

  /// No description provided for @colInputValue.
  ///
  /// In en, this message translates to:
  /// **'Input Value'**
  String get colInputValue;

  /// Translated from excel_uploader_processing
  ///
  /// In en, this message translates to:
  /// **'Processing Data... {percent}%'**
  String excelUploaderProcessing(Object percent);

  /// No description provided for @excelUploaderBtnCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get excelUploaderBtnCancel;

  /// No description provided for @excelUploaderBtnValidateUpload.
  ///
  /// In en, this message translates to:
  /// **'Validate & Upload'**
  String get excelUploaderBtnValidateUpload;

  /// No description provided for @btnValidateUpload.
  ///
  /// In en, this message translates to:
  /// **'Validate & Upload'**
  String get btnValidateUpload;

  /// No description provided for @excelUploaderBtnEditMapping.
  ///
  /// In en, this message translates to:
  /// **'← Edit Mapping'**
  String get excelUploaderBtnEditMapping;

  /// No description provided for @btnEditMapping.
  ///
  /// In en, this message translates to:
  /// **'← Edit Mapping'**
  String get btnEditMapping;

  /// No description provided for @excelUploaderBtnStartUpload.
  ///
  /// In en, this message translates to:
  /// **'Start Upload'**
  String get excelUploaderBtnStartUpload;

  /// No description provided for @btnStartUpload.
  ///
  /// In en, this message translates to:
  /// **'Start Upload'**
  String get btnStartUpload;

  /// Translated from excel_uploader_btn_upload_valid_only
  ///
  /// In en, this message translates to:
  /// **'Upload {count} Valid Rows Only'**
  String excelUploaderBtnUploadValidOnly(Object count);

  /// Translated from btn_upload_valid_only
  ///
  /// In en, this message translates to:
  /// **'Upload {count} Valid Rows Only'**
  String btnUploadValidOnly(Object count);

  /// No description provided for @excelUploaderBtnDone.
  ///
  /// In en, this message translates to:
  /// **'Done'**
  String get excelUploaderBtnDone;

  /// No description provided for @btnDone.
  ///
  /// In en, this message translates to:
  /// **'Done'**
  String get btnDone;

  /// No description provided for @excelUploaderTooltipValidOnly.
  ///
  /// In en, this message translates to:
  /// **'Only valid rows will be uploaded, excluding rows with violations'**
  String get excelUploaderTooltipValidOnly;

  /// No description provided for @tooltipValidOnly.
  ///
  /// In en, this message translates to:
  /// **'Only valid rows will be uploaded, excluding rows with violations'**
  String get tooltipValidOnly;

  /// Translated from reclassify_success
  ///
  /// In en, this message translates to:
  /// **'Successfully reclassified {count} records.'**
  String reclassifySuccess(Object count);

  /// Translated from reclassify_partial_failed
  ///
  /// In en, this message translates to:
  /// **'{success} succeeded, {failure} failed.'**
  String reclassifyPartialFailed(Object success, Object failure);

  /// No description provided for @selectTargetNodePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select target classification node'**
  String get selectTargetNodePlaceholder;

  /// No description provided for @bulkImport.
  ///
  /// In en, this message translates to:
  /// **'Bulk Record Import'**
  String get bulkImport;

  /// No description provided for @bulkImportDesc.
  ///
  /// In en, this message translates to:
  /// **'Upload CSV or JSON files to bulk create master data records.'**
  String get bulkImportDesc;

  /// Translated from bulk_import_success
  ///
  /// In en, this message translates to:
  /// **'Bulk import completed. (Success: {success}, Errors: {errors})'**
  String bulkImportSuccess(Object success, Object errors);

  /// No description provided for @selectFile.
  ///
  /// In en, this message translates to:
  /// **'Select File (.csv / .json)'**
  String get selectFile;

  /// No description provided for @startUpload.
  ///
  /// In en, this message translates to:
  /// **'Start Bulk Import'**
  String get startUpload;

  /// No description provided for @errorDetails.
  ///
  /// In en, this message translates to:
  /// **'Failed Rows & Error Details'**
  String get errorDetails;

  /// No description provided for @rowNumber.
  ///
  /// In en, this message translates to:
  /// **'Row No'**
  String get rowNumber;

  /// No description provided for @errorReason.
  ///
  /// In en, this message translates to:
  /// **'Error Reason'**
  String get errorReason;

  /// No description provided for @complianceReport.
  ///
  /// In en, this message translates to:
  /// **'Compliance Audit Lifecycle Report'**
  String get complianceReport;

  /// No description provided for @complianceReportDesc.
  ///
  /// In en, this message translates to:
  /// **'Track comprehensive lifecycle audit logs from creation to updates, approvals, sensitive data views, and rollbacks.'**
  String get complianceReportDesc;

  /// No description provided for @auditTimeline.
  ///
  /// In en, this message translates to:
  /// **'Lifecycle Audit Timeline'**
  String get auditTimeline;

  /// No description provided for @eventType.
  ///
  /// In en, this message translates to:
  /// **'Event Type'**
  String get eventType;

  /// No description provided for @actor.
  ///
  /// In en, this message translates to:
  /// **'Actor'**
  String get actor;

  /// No description provided for @eventDetail.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get eventDetail;

  /// No description provided for @timeMachine.
  ///
  /// In en, this message translates to:
  /// **'Record Time-Machine & Version Diff'**
  String get timeMachine;

  /// No description provided for @timeMachineDesc.
  ///
  /// In en, this message translates to:
  /// **'Explore version history across the timeline and visually compare field differences between two version snapshots.'**
  String get timeMachineDesc;

  /// No description provided for @compareVersions.
  ///
  /// In en, this message translates to:
  /// **'Compare Versions'**
  String get compareVersions;

  /// No description provided for @baseVersion.
  ///
  /// In en, this message translates to:
  /// **'Base Version (Before)'**
  String get baseVersion;

  /// No description provided for @targetVersion.
  ///
  /// In en, this message translates to:
  /// **'Target Version (After)'**
  String get targetVersion;

  /// No description provided for @diffAdded.
  ///
  /// In en, this message translates to:
  /// **'Added'**
  String get diffAdded;

  /// No description provided for @diffModified.
  ///
  /// In en, this message translates to:
  /// **'Modified'**
  String get diffModified;

  /// No description provided for @diffRemoved.
  ///
  /// In en, this message translates to:
  /// **'Removed'**
  String get diffRemoved;

  /// No description provided for @diffUnchanged.
  ///
  /// In en, this message translates to:
  /// **'Unchanged'**
  String get diffUnchanged;

  /// No description provided for @dataMasking.
  ///
  /// In en, this message translates to:
  /// **'Dynamic Data Masking & PII Protection'**
  String get dataMasking;

  /// No description provided for @dataMaskingDesc.
  ///
  /// In en, this message translates to:
  /// **'Automatically masks sensitive personal identifiable information (PII) such as phone, email, and resident numbers based on user permissions.'**
  String get dataMaskingDesc;

  /// No description provided for @maskedPreview.
  ///
  /// In en, this message translates to:
  /// **'Masked State'**
  String get maskedPreview;

  /// No description provided for @unmaskedPreview.
  ///
  /// In en, this message translates to:
  /// **'Unmasked Original (Authorized)'**
  String get unmaskedPreview;

  /// No description provided for @maskedFieldCount.
  ///
  /// In en, this message translates to:
  /// **'Masked Fields Count'**
  String get maskedFieldCount;

  /// No description provided for @goldenRecord.
  ///
  /// In en, this message translates to:
  /// **'Golden Record Builder & Merge Simulator'**
  String get goldenRecord;

  /// No description provided for @goldenRecordDesc.
  ///
  /// In en, this message translates to:
  /// **'Assembles optimal master data by selecting the most trustworthy and recent field values across duplicate candidate records.'**
  String get goldenRecordDesc;

  /// No description provided for @candidateRecords.
  ///
  /// In en, this message translates to:
  /// **'Candidate Records'**
  String get candidateRecords;

  /// No description provided for @assembledGoldenData.
  ///
  /// In en, this message translates to:
  /// **'Assembled Golden Record'**
  String get assembledGoldenData;

  /// No description provided for @chosenSource.
  ///
  /// In en, this message translates to:
  /// **'Chosen Source'**
  String get chosenSource;

  /// No description provided for @buildPreview.
  ///
  /// In en, this message translates to:
  /// **'Simulate Golden Record Build'**
  String get buildPreview;

  /// No description provided for @hashChainLedger.
  ///
  /// In en, this message translates to:
  /// **'Immutable Hash-Chain Audit Ledger'**
  String get hashChainLedger;

  /// No description provided for @hashChainDesc.
  ///
  /// In en, this message translates to:
  /// **'Provides tamper-proof blockchain audit tracking by linking master data changes with SHA-256 hash chains.'**
  String get hashChainDesc;

  /// No description provided for @verifyIntegrity.
  ///
  /// In en, this message translates to:
  /// **'Verify Ledger Integrity'**
  String get verifyIntegrity;

  /// No description provided for @chainStatusIntact.
  ///
  /// In en, this message translates to:
  /// **'Chain Intact (No Tampering)'**
  String get chainStatusIntact;

  /// No description provided for @chainStatusCorrupted.
  ///
  /// In en, this message translates to:
  /// **'Tampering Detected'**
  String get chainStatusCorrupted;

  /// No description provided for @blockIndex.
  ///
  /// In en, this message translates to:
  /// **'Block Index'**
  String get blockIndex;

  /// No description provided for @blockHash.
  ///
  /// In en, this message translates to:
  /// **'Block Hash (SHA-256)'**
  String get blockHash;

  /// No description provided for @prevHash.
  ///
  /// In en, this message translates to:
  /// **'Previous Block Hash'**
  String get prevHash;

  /// No description provided for @smartQuery.
  ///
  /// In en, this message translates to:
  /// **'Natural Language Smart Query Assistant'**
  String get smartQuery;

  /// No description provided for @smartQueryDesc.
  ///
  /// In en, this message translates to:
  /// **'Automatically interprets natural language queries into schema filter expressions to retrieve matching records.'**
  String get smartQueryDesc;

  /// No description provided for @queryPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g. Find VIP customers living in Seoul'**
  String get queryPlaceholder;

  /// No description provided for @parsedFilters.
  ///
  /// In en, this message translates to:
  /// **'Parsed Filter Expressions'**
  String get parsedFilters;

  /// No description provided for @matchedResults.
  ///
  /// In en, this message translates to:
  /// **'Matched Results'**
  String get matchedResults;

  /// No description provided for @executeQuery.
  ///
  /// In en, this message translates to:
  /// **'Run Smart Query'**
  String get executeQuery;

  /// No description provided for @businessRules.
  ///
  /// In en, this message translates to:
  /// **'Complex Business Rule DQ Expression Builder'**
  String get businessRules;

  /// No description provided for @businessRulesDesc.
  ///
  /// In en, this message translates to:
  /// **'Define conditional IF-THEN business validation logic and evaluate real-time data violations.'**
  String get businessRulesDesc;

  /// No description provided for @conditionExpr.
  ///
  /// In en, this message translates to:
  /// **'Condition Expression (IF)'**
  String get conditionExpr;

  /// No description provided for @validationExpr.
  ///
  /// In en, this message translates to:
  /// **'Validation Expression (THEN)'**
  String get validationExpr;

  /// No description provided for @evaluateRules.
  ///
  /// In en, this message translates to:
  /// **'Evaluate All Rules'**
  String get evaluateRules;

  /// No description provided for @violationFound.
  ///
  /// In en, this message translates to:
  /// **'Violations Found'**
  String get violationFound;

  /// No description provided for @allRulesPassed.
  ///
  /// In en, this message translates to:
  /// **'All business validation rules passed successfully.'**
  String get allRulesPassed;

  /// No description provided for @cdcStream.
  ///
  /// In en, this message translates to:
  /// **'CDC Stream'**
  String get cdcStream;

  /// No description provided for @cdcStreamDesc.
  ///
  /// In en, this message translates to:
  /// **'Capture real-time master record change events and inspect before/after attribute diffs.'**
  String get cdcStreamDesc;

  /// No description provided for @cdcOp.
  ///
  /// In en, this message translates to:
  /// **'Operation'**
  String get cdcOp;

  /// No description provided for @activeOffset.
  ///
  /// In en, this message translates to:
  /// **'Active Offset'**
  String get activeOffset;

  /// No description provided for @eventsPerSec.
  ///
  /// In en, this message translates to:
  /// **'Throughput'**
  String get eventsPerSec;

  /// No description provided for @beforePayload.
  ///
  /// In en, this message translates to:
  /// **'Before'**
  String get beforePayload;

  /// No description provided for @afterPayload.
  ///
  /// In en, this message translates to:
  /// **'After'**
  String get afterPayload;

  /// No description provided for @simulateChange.
  ///
  /// In en, this message translates to:
  /// **'Simulate Change Event'**
  String get simulateChange;

  /// No description provided for @aiStructurizer.
  ///
  /// In en, this message translates to:
  /// **'AI Unstructured Data Structurizer'**
  String get aiStructurizer;

  /// No description provided for @aiStructurizerDesc.
  ///
  /// In en, this message translates to:
  /// **'Automatically extracts schema fields and values from unstructured text like contracts, receipts, or emails.'**
  String get aiStructurizerDesc;

  /// No description provided for @rawTextPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter unstructured text such as contract summaries, emails, or notes.'**
  String get rawTextPlaceholder;

  /// No description provided for @extractFields.
  ///
  /// In en, this message translates to:
  /// **'Run AI Extraction'**
  String get extractFields;

  /// No description provided for @extractedFieldsCount.
  ///
  /// In en, this message translates to:
  /// **'Extracted Fields'**
  String get extractedFieldsCount;

  /// No description provided for @overallConfidence.
  ///
  /// In en, this message translates to:
  /// **'Overall Confidence'**
  String get overallConfidence;

  /// No description provided for @createRecordFromAi.
  ///
  /// In en, this message translates to:
  /// **'Create Record from AI Fields'**
  String get createRecordFromAi;

  /// No description provided for @autonomousCleansing.
  ///
  /// In en, this message translates to:
  /// **'Autonomous Anomaly Cleansing Recommender'**
  String get autonomousCleansing;

  /// No description provided for @autonomousCleansingDesc.
  ///
  /// In en, this message translates to:
  /// **'Analyzes statistical medians and standard dictionaries to autonomously recommend optimal values for anomalies.'**
  String get autonomousCleansingDesc;

  /// No description provided for @anomalyValue.
  ///
  /// In en, this message translates to:
  /// **'Anomaly Value'**
  String get anomalyValue;

  /// No description provided for @recommendedValue.
  ///
  /// In en, this message translates to:
  /// **'Recommended Value'**
  String get recommendedValue;

  /// No description provided for @cleansingStrategy.
  ///
  /// In en, this message translates to:
  /// **'Strategy'**
  String get cleansingStrategy;

  /// No description provided for @applyCleansing.
  ///
  /// In en, this message translates to:
  /// **'Apply Cleansing'**
  String get applyCleansing;

  /// No description provided for @cleansingSuccess.
  ///
  /// In en, this message translates to:
  /// **'Autonomous anomaly cleansing applied successfully.'**
  String get cleansingSuccess;

  /// No description provided for @btnSave.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get btnSave;

  /// No description provided for @btnClose.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get btnClose;

  /// No description provided for @btnEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
  String get btnEdit;

  /// No description provided for @btnDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get btnDelete;

  /// No description provided for @domainRefModalTitle.
  ///
  /// In en, this message translates to:
  /// **'Select Reference Record'**
  String get domainRefModalTitle;

  /// No description provided for @domainRefModalGuide.
  ///
  /// In en, this message translates to:
  /// **'Please double click the desired record from the list to select it.'**
  String get domainRefModalGuide;

  /// No description provided for @guide.
  ///
  /// In en, this message translates to:
  /// **'Please double click the desired record from the list to select it.'**
  String get guide;

  /// No description provided for @domainRefModalSearchPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search by ID or Name attribute...'**
  String get domainRefModalSearchPlaceholder;

  /// No description provided for @domainRefModalSearchBtn.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get domainRefModalSearchBtn;

  /// No description provided for @searchBtn.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get searchBtn;

  /// No description provided for @domainRefModalResetBtn.
  ///
  /// In en, this message translates to:
  /// **'Reset'**
  String get domainRefModalResetBtn;

  /// No description provided for @resetBtn.
  ///
  /// In en, this message translates to:
  /// **'Reset'**
  String get resetBtn;

  /// Translated from domain_ref_modal_total_count
  ///
  /// In en, this message translates to:
  /// **'Total {count} items'**
  String domainRefModalTotalCount(Object count);

  /// Translated from total_count
  ///
  /// In en, this message translates to:
  /// **'Total {count} items'**
  String totalCount(Object count);

  /// No description provided for @domainRefModalNoResults.
  ///
  /// In en, this message translates to:
  /// **'No records found.'**
  String get domainRefModalNoResults;

  /// No description provided for @noResults.
  ///
  /// In en, this message translates to:
  /// **'No records found.'**
  String get noResults;

  /// No description provided for @classificationAxes.
  ///
  /// In en, this message translates to:
  /// **'Classification Axes'**
  String get classificationAxes;

  /// No description provided for @domainName.
  ///
  /// In en, this message translates to:
  /// **'Domain Name'**
  String get domainName;

  /// No description provided for @classificationName.
  ///
  /// In en, this message translates to:
  /// **'Classification Name'**
  String get classificationName;

  /// No description provided for @tableSchemaSettings.
  ///
  /// In en, this message translates to:
  /// **'Table Column Definitions (JSON Sub-Schema)'**
  String get tableSchemaSettings;

  /// No description provided for @tableSchemaGuide.
  ///
  /// In en, this message translates to:
  /// **'💡 Define sub-columns to receive data in a complex table format (e.g. Major History with School, Major, Graduation Date).'**
  String get tableSchemaGuide;

  /// No description provided for @addColumn.
  ///
  /// In en, this message translates to:
  /// **'+ Add Column'**
  String get addColumn;

  /// No description provided for @removeColumn.
  ///
  /// In en, this message translates to:
  /// **'Remove Column'**
  String get removeColumn;

  /// No description provided for @columnKey.
  ///
  /// In en, this message translates to:
  /// **'Column Key'**
  String get columnKey;

  /// No description provided for @columnNameKo.
  ///
  /// In en, this message translates to:
  /// **'Column Name (KO)'**
  String get columnNameKo;

  /// No description provided for @columnNameEn.
  ///
  /// In en, this message translates to:
  /// **'Column Name (EN)'**
  String get columnNameEn;

  /// No description provided for @columnType.
  ///
  /// In en, this message translates to:
  /// **'Column Type'**
  String get columnType;

  /// No description provided for @columnOptions.
  ///
  /// In en, this message translates to:
  /// **'Options (KEY:KO_Label:EN_Label or comma separated: e.g. BACHELOR:학사:Bachelor, MASTER:석사:Master)'**
  String get columnOptions;

  /// No description provided for @columnOptionsPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'e.g. HIGH:고졸:High School, BACHELOR:학사:Bachelor, MASTER:석사:Master, DOCTOR:박사:Doctor'**
  String get columnOptionsPlaceholder;

  /// No description provided for @columnWidth.
  ///
  /// In en, this message translates to:
  /// **'Width(px)'**
  String get columnWidth;

  /// No description provided for @noTableColumnsDefined.
  ///
  /// In en, this message translates to:
  /// **'No table columns defined. Click \'+ Add Column\' to define columns.'**
  String get noTableColumnsDefined;

  /// Translated from confirm_delete_node
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to delete node \'{name}\'?'**
  String confirmDeleteNode(Object name);

  /// No description provided for @nodeDeletedSuccess.
  ///
  /// In en, this message translates to:
  /// **'Node deleted successfully.'**
  String get nodeDeletedSuccess;

  /// No description provided for @nodeDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete node.'**
  String get nodeDeleteFailed;

  /// Translated from add_node_to
  ///
  /// In en, this message translates to:
  /// **'Add Child Node to {name}'**
  String addNodeTo(Object name);

  /// No description provided for @nodeOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get nodeOrder;

  /// No description provided for @idAttribute.
  ///
  /// In en, this message translates to:
  /// **'ID Attribute'**
  String get idAttribute;

  /// No description provided for @nameAttribute.
  ///
  /// In en, this message translates to:
  /// **'Name Attribute'**
  String get nameAttribute;

  /// No description provided for @schemaImpactTitle.
  ///
  /// In en, this message translates to:
  /// **'Schema Change Impact Analysis Report'**
  String get schemaImpactTitle;

  /// No description provided for @schemaImpactSummary.
  ///
  /// In en, this message translates to:
  /// **'Schema Impact Summary'**
  String get schemaImpactSummary;

  /// No description provided for @fieldDeleteApprovalSubmitted.
  ///
  /// In en, this message translates to:
  /// **'Field deletion request has been submitted for approval.'**
  String get fieldDeleteApprovalSubmitted;

  /// No description provided for @fieldDeleteFailed.
  ///
  /// In en, this message translates to:
  /// **'An error occurred while deleting the field.'**
  String get fieldDeleteFailed;

  /// No description provided for @targetTypeSCHEMAFIELDDELETE.
  ///
  /// In en, this message translates to:
  /// **'Schema Field Deletion'**
  String get targetTypeSCHEMAFIELDDELETE;

  /// No description provided for @targetTypeSCHEMAFIELDADD.
  ///
  /// In en, this message translates to:
  /// **'Schema Field Addition'**
  String get targetTypeSCHEMAFIELDADD;

  /// No description provided for @targetTypeSCHEMAFIELDUPDATE.
  ///
  /// In en, this message translates to:
  /// **'Schema Field Modification'**
  String get targetTypeSCHEMAFIELDUPDATE;

  /// No description provided for @targetTypeSCHEMANODECREATE.
  ///
  /// In en, this message translates to:
  /// **'Classification Node Creation'**
  String get targetTypeSCHEMANODECREATE;

  /// No description provided for @targetTypeSCHEMANODEUPDATE.
  ///
  /// In en, this message translates to:
  /// **'Classification Node Modification'**
  String get targetTypeSCHEMANODEUPDATE;

  /// No description provided for @targetTypeSCHEMANODEMOVE.
  ///
  /// In en, this message translates to:
  /// **'Classification Node Move'**
  String get targetTypeSCHEMANODEMOVE;

  /// No description provided for @affectedTargetField.
  ///
  /// In en, this message translates to:
  /// **'Target Attribute Field'**
  String get affectedTargetField;

  /// No description provided for @schemaImpactConfirmedTitle.
  ///
  /// In en, this message translates to:
  /// **'Risk Change Applied'**
  String get schemaImpactConfirmedTitle;

  /// No description provided for @schemaImpactConfirmedMsg.
  ///
  /// In en, this message translates to:
  /// **'Schema change impact has been confirmed, and the risk-accepted changes have been approved and applied.'**
  String get schemaImpactConfirmedMsg;

  /// No description provided for @totalNodes.
  ///
  /// In en, this message translates to:
  /// **'Total Lineage Nodes'**
  String get totalNodes;

  /// No description provided for @actionTypeSchemaChange.
  ///
  /// In en, this message translates to:
  /// **'Schema Change (SCHEMA_CHANGE)'**
  String get actionTypeSchemaChange;

  /// No description provided for @actionTypeSchemaChangeShort.
  ///
  /// In en, this message translates to:
  /// **'Schema Change'**
  String get actionTypeSchemaChangeShort;

  /// No description provided for @addEditableFieldPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'+ Select Editable Fields (Default All)'**
  String get addEditableFieldPlaceholder;

  /// Translated from add_field_to_node
  ///
  /// In en, this message translates to:
  /// **'Add Field to {name}'**
  String addFieldToNode(Object name);

  /// No description provided for @fieldNameKo.
  ///
  /// In en, this message translates to:
  /// **'Field Name (KO)'**
  String get fieldNameKo;

  /// No description provided for @fieldNameEn.
  ///
  /// In en, this message translates to:
  /// **'Field Name (EN)'**
  String get fieldNameEn;

  /// No description provided for @fieldHintKo.
  ///
  /// In en, this message translates to:
  /// **'Field Tooltip (KO)'**
  String get fieldHintKo;

  /// No description provided for @fieldHintEn.
  ///
  /// In en, this message translates to:
  /// **'Field Tooltip (EN)'**
  String get fieldHintEn;

  /// No description provided for @groupSectorMapped.
  ///
  /// In en, this message translates to:
  /// **'Group (Sector mapped automatically)'**
  String get groupSectorMapped;

  /// No description provided for @fieldType.
  ///
  /// In en, this message translates to:
  /// **'Field Type'**
  String get fieldType;

  /// No description provided for @targetDomain.
  ///
  /// In en, this message translates to:
  /// **'Target Domain'**
  String get targetDomain;

  /// No description provided for @addHiddenFieldPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'+ Select Fields to Hide'**
  String get addHiddenFieldPlaceholder;

  /// No description provided for @addNewField.
  ///
  /// In en, this message translates to:
  /// **'+ Add New Field'**
  String get addNewField;

  /// No description provided for @addSchema.
  ///
  /// In en, this message translates to:
  /// **'Add Schema'**
  String get addSchema;

  /// No description provided for @allDomainsSelected.
  ///
  /// In en, this message translates to:
  /// **'All available domains are selected.'**
  String get allDomainsSelected;

  /// Translated from audit_field_changed
  ///
  /// In en, this message translates to:
  /// **'{field} changed'**
  String auditFieldChanged(Object field);

  /// No description provided for @basicFields.
  ///
  /// In en, this message translates to:
  /// **'Basic Fields'**
  String get basicFields;

  /// No description provided for @classification.
  ///
  /// In en, this message translates to:
  /// **'Classification'**
  String get classification;

  /// No description provided for @classificationTree.
  ///
  /// In en, this message translates to:
  /// **'Classification Tree'**
  String get classificationTree;

  /// No description provided for @deleteFieldProp.
  ///
  /// In en, this message translates to:
  /// **'🗑️ Delete Field'**
  String get deleteFieldProp;

  /// No description provided for @deleteSchema.
  ///
  /// In en, this message translates to:
  /// **'Delete Schema'**
  String get deleteSchema;

  /// No description provided for @deletedFieldProperties.
  ///
  /// In en, this message translates to:
  /// **'Deleted Field Properties'**
  String get deletedFieldProperties;

  /// No description provided for @domain.
  ///
  /// In en, this message translates to:
  /// **'Domain'**
  String get domain;

  /// No description provided for @domainDistributionTitle.
  ///
  /// In en, this message translates to:
  /// **'Master Record Distribution by Domain'**
  String get domainDistributionTitle;

  /// No description provided for @domainPermGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Domain Permissions'**
  String get domainPermGroupTitle;

  /// No description provided for @domainSchemaDesc.
  ///
  /// In en, this message translates to:
  /// **'Define domain models, attributes, and data types based on classification tree structures.'**
  String get domainSchemaDesc;

  /// No description provided for @domainSchemaTitle.
  ///
  /// In en, this message translates to:
  /// **'Domain Schema Management'**
  String get domainSchemaTitle;

  /// No description provided for @editSchema.
  ///
  /// In en, this message translates to:
  /// **'Edit Schema'**
  String get editSchema;

  /// No description provided for @editableFieldsTitle.
  ///
  /// In en, this message translates to:
  /// **'🟢 Editable & DQ Scoped Fields'**
  String get editableFieldsTitle;

  /// No description provided for @errorDomainMissingId.
  ///
  /// In en, this message translates to:
  /// **'Domain configuration error: Missing Identifier (ID) or Display Name mapping.'**
  String get errorDomainMissingId;

  /// No description provided for @errorSavingDomain.
  ///
  /// In en, this message translates to:
  /// **'Error saving domain'**
  String get errorSavingDomain;

  /// Translated from field_key_already_exists_newfield_value_key
  ///
  /// In en, this message translates to:
  /// **'Field Key already exists: {key}'**
  String fieldKeyAlreadyExistsNewfieldValueKey(Object key);

  /// No description provided for @fieldKeyExists.
  ///
  /// In en, this message translates to:
  /// **'Field Key already exists'**
  String get fieldKeyExists;

  /// No description provided for @fieldPermGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Attribute Field Permissions'**
  String get fieldPermGroupTitle;

  /// No description provided for @grantedDomains.
  ///
  /// In en, this message translates to:
  /// **'Granted Domains'**
  String get grantedDomains;

  /// No description provided for @hiddenFieldsTitle.
  ///
  /// In en, this message translates to:
  /// **'🔴 Hidden Fields'**
  String get hiddenFieldsTitle;

  /// No description provided for @nodeCountSuffix.
  ///
  /// In en, this message translates to:
  /// **''**
  String get nodeCountSuffix;

  /// No description provided for @outboundNode.
  ///
  /// In en, this message translates to:
  /// **'Outbound Integration'**
  String get outboundNode;

  /// No description provided for @lineageNodeSourceDesc.
  ///
  /// In en, this message translates to:
  /// **'Source System: Origin system where data was created/ingested'**
  String get lineageNodeSourceDesc;

  /// No description provided for @lineageNodeHistoryDesc.
  ///
  /// In en, this message translates to:
  /// **'Change History: Chronological version history from creation (v1) to updates (v2...)'**
  String get lineageNodeHistoryDesc;

  /// No description provided for @lineageNodeOutboundDesc.
  ///
  /// In en, this message translates to:
  /// **'Outbound Integration: Integration dispatch history to external systems'**
  String get lineageNodeOutboundDesc;

  /// No description provided for @myGrantedDomains.
  ///
  /// In en, this message translates to:
  /// **'My Granted Domains'**
  String get myGrantedDomains;

  /// No description provided for @newFieldProperties.
  ///
  /// In en, this message translates to:
  /// **'New Field Properties'**
  String get newFieldProperties;

  /// No description provided for @noDomainData.
  ///
  /// In en, this message translates to:
  /// **'No Domain Data'**
  String get noDomainData;

  /// No description provided for @noGrantedDomains.
  ///
  /// In en, this message translates to:
  /// **'No granted domains.'**
  String get noGrantedDomains;

  /// No description provided for @noNewDomainsAvailable.
  ///
  /// In en, this message translates to:
  /// **'No new domains available for access.'**
  String get noNewDomainsAvailable;

  /// No description provided for @noRulesSchemaChange.
  ///
  /// In en, this message translates to:
  /// **'No rules defined. By default, all users are eligible to submit schema change requests.'**
  String get noRulesSchemaChange;

  /// No description provided for @noSpecificDomainPermissions.
  ///
  /// In en, this message translates to:
  /// **'No specific domain permissions. (ADMIN sees all)'**
  String get noSpecificDomainPermissions;

  /// No description provided for @node.
  ///
  /// In en, this message translates to:
  /// **'Node'**
  String get node;

  /// No description provided for @deptIcon.
  ///
  /// In en, this message translates to:
  /// **'Department Icon'**
  String get deptIcon;

  /// No description provided for @nodePermGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Category Node Permissions'**
  String get nodePermGroupTitle;

  /// No description provided for @pendingDomainAccessRequests.
  ///
  /// In en, this message translates to:
  /// **'Pending Domain Access Requests'**
  String get pendingDomainAccessRequests;

  /// No description provided for @pendingSchemaApprovalExists.
  ///
  /// In en, this message translates to:
  /// **'A pending schema approval request already exists. Modifications are locked until completion.'**
  String get pendingSchemaApprovalExists;

  /// No description provided for @pleaseSelectATargetDomain.
  ///
  /// In en, this message translates to:
  /// **'Please select a target domain.'**
  String get pleaseSelectATargetDomain;

  /// No description provided for @requestDomainAccess.
  ///
  /// In en, this message translates to:
  /// **'Request Domain Access'**
  String get requestDomainAccess;

  /// No description provided for @requestNewDomain.
  ///
  /// In en, this message translates to:
  /// **'Request New Domain'**
  String get requestNewDomain;

  /// No description provided for @requestedDomains.
  ///
  /// In en, this message translates to:
  /// **'Requested Domains'**
  String get requestedDomains;

  /// No description provided for @schema.
  ///
  /// In en, this message translates to:
  /// **'Domain Schema'**
  String get schema;

  /// No description provided for @schemaApprovalInProgress.
  ///
  /// In en, this message translates to:
  /// **'Approval Pending'**
  String get schemaApprovalInProgress;

  /// No description provided for @schemaChangeComparison.
  ///
  /// In en, this message translates to:
  /// **'Field Property Changes (Before ➔ After)'**
  String get schemaChangeComparison;

  /// No description provided for @schemaHistoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Schema Change History'**
  String get schemaHistoryTitle;

  /// No description provided for @schemaHistoryTargetType.
  ///
  /// In en, this message translates to:
  /// **'Target Type'**
  String get schemaHistoryTargetType;

  /// No description provided for @schemaHistoryAction.
  ///
  /// In en, this message translates to:
  /// **'Action'**
  String get schemaHistoryAction;

  /// No description provided for @schemaHistoryChangedBy.
  ///
  /// In en, this message translates to:
  /// **'Changed By'**
  String get schemaHistoryChangedBy;

  /// No description provided for @schemaHistoryChangedAt.
  ///
  /// In en, this message translates to:
  /// **'Changed At'**
  String get schemaHistoryChangedAt;

  /// No description provided for @changedAt.
  ///
  /// In en, this message translates to:
  /// **'Changed At'**
  String get changedAt;

  /// No description provided for @schemaHistoryBefore.
  ///
  /// In en, this message translates to:
  /// **'Before'**
  String get schemaHistoryBefore;

  /// No description provided for @before.
  ///
  /// In en, this message translates to:
  /// **'Before'**
  String get before;

  /// No description provided for @schemaHistoryAfter.
  ///
  /// In en, this message translates to:
  /// **'After'**
  String get schemaHistoryAfter;

  /// No description provided for @after.
  ///
  /// In en, this message translates to:
  /// **'After'**
  String get after;

  /// No description provided for @schemaHistoryField.
  ///
  /// In en, this message translates to:
  /// **'Field'**
  String get schemaHistoryField;

  /// No description provided for @schemaHistoryNode.
  ///
  /// In en, this message translates to:
  /// **'Node'**
  String get schemaHistoryNode;

  /// No description provided for @schemaHistoryDomainEntity.
  ///
  /// In en, this message translates to:
  /// **'Domain'**
  String get schemaHistoryDomainEntity;

  /// No description provided for @domainEntity.
  ///
  /// In en, this message translates to:
  /// **'Domain'**
  String get domainEntity;

  /// No description provided for @schemaHistoryGroup.
  ///
  /// In en, this message translates to:
  /// **'Group'**
  String get schemaHistoryGroup;

  /// No description provided for @schemaHistoryCreate.
  ///
  /// In en, this message translates to:
  /// **'Create'**
  String get schemaHistoryCreate;

  /// No description provided for @schemaHistoryUpdate.
  ///
  /// In en, this message translates to:
  /// **'Update'**
  String get schemaHistoryUpdate;

  /// No description provided for @schemaHistoryDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get schemaHistoryDelete;

  /// No description provided for @schemaHistoryNoHistory.
  ///
  /// In en, this message translates to:
  /// **'No change history'**
  String get schemaHistoryNoHistory;

  /// No description provided for @noHistory.
  ///
  /// In en, this message translates to:
  /// **'No change history'**
  String get noHistory;

  /// No description provided for @schemaHistoryViewChanges.
  ///
  /// In en, this message translates to:
  /// **'View Changes'**
  String get schemaHistoryViewChanges;

  /// No description provided for @schemaReason.
  ///
  /// In en, this message translates to:
  /// **'Reason for Submission'**
  String get schemaReason;

  /// No description provided for @schemaReasonPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Please describe the reason for schema modification.'**
  String get schemaReasonPlaceholder;

  /// No description provided for @scopeDomain.
  ///
  /// In en, this message translates to:
  /// **'Domain Common'**
  String get scopeDomain;

  /// No description provided for @scopeNode.
  ///
  /// In en, this message translates to:
  /// **'Classification Node'**
  String get scopeNode;

  /// No description provided for @selectADomain.
  ///
  /// In en, this message translates to:
  /// **'Select a domain'**
  String get selectADomain;

  /// No description provided for @selectDomainFirst.
  ///
  /// In en, this message translates to:
  /// **'Please select a domain first.'**
  String get selectDomainFirst;

  /// No description provided for @selectNodePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select Classification Node'**
  String get selectNodePlaceholder;

  /// No description provided for @selectTargetDomain.
  ///
  /// In en, this message translates to:
  /// **'Please select a target domain.'**
  String get selectTargetDomain;

  /// No description provided for @selectTargetDomainAlert.
  ///
  /// In en, this message translates to:
  /// **'Please select a target domain.'**
  String get selectTargetDomainAlert;

  /// No description provided for @tabFields.
  ///
  /// In en, this message translates to:
  /// **'Fields'**
  String get tabFields;

  /// No description provided for @applyTargetDomain.
  ///
  /// In en, this message translates to:
  /// **'Target Domain *'**
  String get applyTargetDomain;

  /// No description provided for @targetNode.
  ///
  /// In en, this message translates to:
  /// **'Target Classification Node *'**
  String get targetNode;

  /// No description provided for @totalDomains.
  ///
  /// In en, this message translates to:
  /// **'Total Domains'**
  String get totalDomains;

  /// No description provided for @updateFieldProps.
  ///
  /// In en, this message translates to:
  /// **'✏️ Update Field'**
  String get updateFieldProps;

  /// No description provided for @waitingForFieldData.
  ///
  /// In en, this message translates to:
  /// **'Waiting for field data...'**
  String get waitingForFieldData;

  /// No description provided for @domainBracket.
  ///
  /// In en, this message translates to:
  /// **'[Domain]'**
  String get domainBracket;

  /// No description provided for @domainLevel.
  ///
  /// In en, this message translates to:
  /// **'Domain Level'**
  String get domainLevel;

  /// No description provided for @belongingNodeDomain.
  ///
  /// In en, this message translates to:
  /// **'Belonging Node / Domain'**
  String get belongingNodeDomain;

  /// No description provided for @domainCommonField.
  ///
  /// In en, this message translates to:
  /// **'Domain Common Field'**
  String get domainCommonField;

  /// No description provided for @highlight.
  ///
  /// In en, this message translates to:
  /// **'Highlight'**
  String get highlight;

  /// No description provided for @conditionalFieldControl.
  ///
  /// In en, this message translates to:
  /// **'Conditional Field Control'**
  String get conditionalFieldControl;

  /// No description provided for @enableCondition.
  ///
  /// In en, this message translates to:
  /// **'Enable'**
  String get enableCondition;

  /// No description provided for @conditionMode.
  ///
  /// In en, this message translates to:
  /// **'Mode:'**
  String get conditionMode;

  /// No description provided for @guiMode.
  ///
  /// In en, this message translates to:
  /// **'GUI (Dropdown)'**
  String get guiMode;

  /// No description provided for @expressionMode.
  ///
  /// In en, this message translates to:
  /// **'Expression'**
  String get expressionMode;

  /// No description provided for @controlAction.
  ///
  /// In en, this message translates to:
  /// **'Control Action:'**
  String get controlAction;

  /// No description provided for @actionShow.
  ///
  /// In en, this message translates to:
  /// **'👁️ Show on Match (SHOW)'**
  String get actionShow;

  /// No description provided for @actionHighlight.
  ///
  /// In en, this message translates to:
  /// **'✨ Highlight on Match (HIGHLIGHT)'**
  String get actionHighlight;

  /// No description provided for @actionRequire.
  ///
  /// In en, this message translates to:
  /// **'🔒 Require on Match (REQUIRE)'**
  String get actionRequire;

  /// No description provided for @actionReadOnly.
  ///
  /// In en, this message translates to:
  /// **'📖 Read Only on Match (READ_ONLY)'**
  String get actionReadOnly;

  /// No description provided for @actionDisable.
  ///
  /// In en, this message translates to:
  /// **'🚫 Disable on Match (DISABLE)'**
  String get actionDisable;

  /// No description provided for @dependsOn.
  ///
  /// In en, this message translates to:
  /// **'Depends On'**
  String get dependsOn;

  /// No description provided for @operator.
  ///
  /// In en, this message translates to:
  /// **'Operator'**
  String get operator;

  /// No description provided for @dateFormat.
  ///
  /// In en, this message translates to:
  /// **'Date Format'**
  String get dateFormat;

  /// No description provided for @canInputDirectly.
  ///
  /// In en, this message translates to:
  /// **'Can input directly'**
  String get canInputDirectly;

  /// No description provided for @schemaPropName.
  ///
  /// In en, this message translates to:
  /// **'Field Name'**
  String get schemaPropName;

  /// No description provided for @schemaPropKey.
  ///
  /// In en, this message translates to:
  /// **'Field Key'**
  String get schemaPropKey;

  /// No description provided for @schemaPropType.
  ///
  /// In en, this message translates to:
  /// **'Data Type'**
  String get schemaPropType;

  /// No description provided for @schemaPropRequired.
  ///
  /// In en, this message translates to:
  /// **'Required'**
  String get schemaPropRequired;

  /// No description provided for @schemaPropIsSearchable.
  ///
  /// In en, this message translates to:
  /// **'Searchable'**
  String get schemaPropIsSearchable;

  /// No description provided for @schemaPropIsMultiValue.
  ///
  /// In en, this message translates to:
  /// **'Multi-value'**
  String get schemaPropIsMultiValue;

  /// No description provided for @schemaPropIsEncrypted.
  ///
  /// In en, this message translates to:
  /// **'Encrypted'**
  String get schemaPropIsEncrypted;

  /// No description provided for @schemaPropIsReadOnly.
  ///
  /// In en, this message translates to:
  /// **'Read-Only'**
  String get schemaPropIsReadOnly;

  /// No description provided for @schemaPropIsHidden.
  ///
  /// In en, this message translates to:
  /// **'Hidden'**
  String get schemaPropIsHidden;

  /// No description provided for @schemaPropIsImmutable.
  ///
  /// In en, this message translates to:
  /// **'Immutable'**
  String get schemaPropIsImmutable;

  /// No description provided for @schemaPropOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get schemaPropOrder;

  /// No description provided for @schemaPropGroup.
  ///
  /// In en, this message translates to:
  /// **'Field Group'**
  String get schemaPropGroup;

  /// No description provided for @schemaPropUnit.
  ///
  /// In en, this message translates to:
  /// **'Unit'**
  String get schemaPropUnit;

  /// No description provided for @schemaPropId.
  ///
  /// In en, this message translates to:
  /// **'Identifier'**
  String get schemaPropId;

  /// No description provided for @schemaPropApprovalStatus.
  ///
  /// In en, this message translates to:
  /// **'Approval Status'**
  String get schemaPropApprovalStatus;

  /// No description provided for @schemaPropIsPendingApproval.
  ///
  /// In en, this message translates to:
  /// **'Is Pending Approval'**
  String get schemaPropIsPendingApproval;

  /// No description provided for @schemaPropMarkingPattern.
  ///
  /// In en, this message translates to:
  /// **'Masking Pattern'**
  String get schemaPropMarkingPattern;

  /// No description provided for @schemaPropHint.
  ///
  /// In en, this message translates to:
  /// **'Input Hint'**
  String get schemaPropHint;

  /// No description provided for @schemaPropFieldGroupId.
  ///
  /// In en, this message translates to:
  /// **'Field Group ID'**
  String get schemaPropFieldGroupId;

  /// No description provided for @schemaPropDependsOnFieldKey.
  ///
  /// In en, this message translates to:
  /// **'Depends On Field Key'**
  String get schemaPropDependsOnFieldKey;

  /// No description provided for @schemaPropConditionOperator.
  ///
  /// In en, this message translates to:
  /// **'Condition Operator'**
  String get schemaPropConditionOperator;

  /// No description provided for @schemaPropConditionValue.
  ///
  /// In en, this message translates to:
  /// **'Condition Value'**
  String get schemaPropConditionValue;

  /// No description provided for @schemaPropConditionAction.
  ///
  /// In en, this message translates to:
  /// **'Condition Action'**
  String get schemaPropConditionAction;

  /// No description provided for @schemaPropConditionMode.
  ///
  /// In en, this message translates to:
  /// **'Condition Mode'**
  String get schemaPropConditionMode;

  /// No description provided for @schemaPropConditionEnabled.
  ///
  /// In en, this message translates to:
  /// **'Condition Enabled'**
  String get schemaPropConditionEnabled;

  /// No description provided for @schemaPropTrue.
  ///
  /// In en, this message translates to:
  /// **'True'**
  String get schemaPropTrue;

  /// No description provided for @schemaPropFalse.
  ///
  /// In en, this message translates to:
  /// **'False'**
  String get schemaPropFalse;

  /// No description provided for @governance.
  ///
  /// In en, this message translates to:
  /// **'Governance'**
  String get governance;

  /// No description provided for @dataProfiling.
  ///
  /// In en, this message translates to:
  /// **'Data Profiling'**
  String get dataProfiling;

  /// No description provided for @selectNodePrompt.
  ///
  /// In en, this message translates to:
  /// **'Select a Classification Node from the tree to view or add fields.'**
  String get selectNodePrompt;

  /// No description provided for @schemaPackage.
  ///
  /// In en, this message translates to:
  /// **'Domain Schema Package (Export / Import)'**
  String get schemaPackage;

  /// No description provided for @exportPackage.
  ///
  /// In en, this message translates to:
  /// **'Export Schema Package'**
  String get exportPackage;

  /// No description provided for @importPackage.
  ///
  /// In en, this message translates to:
  /// **'Import Schema Package'**
  String get importPackage;

  /// No description provided for @packageExportDesc.
  ///
  /// In en, this message translates to:
  /// **'Export taxonomy, field definitions, DQ rules, matching rules, and workflows as a JSON package.'**
  String get packageExportDesc;

  /// No description provided for @packageImportDesc.
  ///
  /// In en, this message translates to:
  /// **'Import domain metadata JSON package to restore or create entire domain structure.'**
  String get packageImportDesc;

  /// No description provided for @exportDownloadJson.
  ///
  /// In en, this message translates to:
  /// **'Download Package (.json)'**
  String get exportDownloadJson;

  /// No description provided for @importUploadJson.
  ///
  /// In en, this message translates to:
  /// **'Execute Package Import'**
  String get importUploadJson;

  /// No description provided for @overwriteExisting.
  ///
  /// In en, this message translates to:
  /// **'Overwrite Existing Domain'**
  String get overwriteExisting;

  /// No description provided for @packageExportSubtext.
  ///
  /// In en, this message translates to:
  /// **'Taxonomy nodes, field definitions, DQ rules, matching rules, and workflow configs are all packaged.'**
  String get packageExportSubtext;

  /// No description provided for @packagePreviewLabel.
  ///
  /// In en, this message translates to:
  /// **'Package JSON Preview:'**
  String get packagePreviewLabel;

  /// No description provided for @packageFileSelectLabel.
  ///
  /// In en, this message translates to:
  /// **'Select JSON Package File (.json)'**
  String get packageFileSelectLabel;

  /// No description provided for @packagePreviewInfoLabel.
  ///
  /// In en, this message translates to:
  /// **'Loaded Package Summary:'**
  String get packagePreviewInfoLabel;

  /// No description provided for @domainLabel.
  ///
  /// In en, this message translates to:
  /// **'Domain Name'**
  String get domainLabel;

  /// Translated from package_summary_counts
  ///
  /// In en, this message translates to:
  /// **'Nodes {nodes} · Fields {fields} · DQ Rules {rules}'**
  String packageSummaryCounts(Object nodes, Object fields, Object rules);

  /// No description provided for @packageDownloadSuccess.
  ///
  /// In en, this message translates to:
  /// **'Domain package download completed.'**
  String get packageDownloadSuccess;

  /// No description provided for @invalidJsonPackageFile.
  ///
  /// In en, this message translates to:
  /// **'Invalid JSON package file.'**
  String get invalidJsonPackageFile;

  /// No description provided for @profilingTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Profiling & Anomaly Detection'**
  String get profilingTitle;

  /// No description provided for @profilingDesc.
  ///
  /// In en, this message translates to:
  /// **'Analyze field null rates, uniqueness, and statistical IQR anomalies across domain records.'**
  String get profilingDesc;

  /// No description provided for @nullRate.
  ///
  /// In en, this message translates to:
  /// **'Null Rate'**
  String get nullRate;

  /// No description provided for @uniqueness.
  ///
  /// In en, this message translates to:
  /// **'Uniqueness'**
  String get uniqueness;

  /// No description provided for @distinctCount.
  ///
  /// In en, this message translates to:
  /// **'Distinct Count'**
  String get distinctCount;

  /// Translated from outliers_found
  ///
  /// In en, this message translates to:
  /// **'Anomalies Found: {count}'**
  String outliersFound(Object count);

  /// No description provided for @noOutliers.
  ///
  /// In en, this message translates to:
  /// **'No anomalies detected.'**
  String get noOutliers;

  /// No description provided for @schemaSimulation.
  ///
  /// In en, this message translates to:
  /// **'Schema Change Impact Simulation'**
  String get schemaSimulation;

  /// No description provided for @schemaSimulationDesc.
  ///
  /// In en, this message translates to:
  /// **'Pre-simulate and diagnose risks to existing records, integration channels, and DQ rules before applying schema changes.'**
  String get schemaSimulationDesc;

  /// No description provided for @safetyScore.
  ///
  /// In en, this message translates to:
  /// **'Safety Score'**
  String get safetyScore;

  /// No description provided for @runSimulation.
  ///
  /// In en, this message translates to:
  /// **'Run Impact Simulation'**
  String get runSimulation;

  /// No description provided for @simulationRecommendations.
  ///
  /// In en, this message translates to:
  /// **'Safety Action Recommendations'**
  String get simulationRecommendations;

  /// No description provided for @businessGlossary.
  ///
  /// In en, this message translates to:
  /// **'Business Glossary & Data Dictionary'**
  String get businessGlossary;

  /// No description provided for @businessGlossaryDesc.
  ///
  /// In en, this message translates to:
  /// **'Define standard business terms, abbreviations, and data sensitivity levels with auto-recommendations.'**
  String get businessGlossaryDesc;

  /// No description provided for @termName.
  ///
  /// In en, this message translates to:
  /// **'Standard Term Name'**
  String get termName;

  /// No description provided for @termCode.
  ///
  /// In en, this message translates to:
  /// **'Standard Term Code'**
  String get termCode;

  /// No description provided for @abbreviation.
  ///
  /// In en, this message translates to:
  /// **'Abbreviation'**
  String get abbreviation;

  /// No description provided for @synonyms.
  ///
  /// In en, this message translates to:
  /// **'Synonyms'**
  String get synonyms;

  /// No description provided for @sensitivityLevel.
  ///
  /// In en, this message translates to:
  /// **'Sensitivity Level'**
  String get sensitivityLevel;

  /// No description provided for @addTerm.
  ///
  /// In en, this message translates to:
  /// **'Add Business Term'**
  String get addTerm;

  /// No description provided for @recommendedTerms.
  ///
  /// In en, this message translates to:
  /// **'Recommended Terms'**
  String get recommendedTerms;

  /// No description provided for @domainSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Domain Snapshot & Point-in-Time Restore'**
  String get domainSnapshot;

  /// No description provided for @domainSnapshotDesc.
  ///
  /// In en, this message translates to:
  /// **'Create full domain data snapshots and restore records to any specific point-in-time.'**
  String get domainSnapshotDesc;

  /// No description provided for @createSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Create Snapshot'**
  String get createSnapshot;

  /// No description provided for @snapshotName.
  ///
  /// In en, this message translates to:
  /// **'Snapshot Name'**
  String get snapshotName;

  /// No description provided for @versionTag.
  ///
  /// In en, this message translates to:
  /// **'Version Tag'**
  String get versionTag;

  /// No description provided for @restoreSnapshot.
  ///
  /// In en, this message translates to:
  /// **'Point-in-Time Restore'**
  String get restoreSnapshot;

  /// Translated from confirm_restore
  ///
  /// In en, this message translates to:
  /// **'Restore domain data to \'{name}\' ({tag}) snapshot state? Current data will be replaced.'**
  String confirmRestore(Object name, Object tag);

  /// No description provided for @multilingualSync.
  ///
  /// In en, this message translates to:
  /// **'Multilingual Metadata Auto-Translation & Sync'**
  String get multilingualSync;

  /// No description provided for @multilingualSyncDesc.
  ///
  /// In en, this message translates to:
  /// **'Scan domain fields missing multilingual names and batch synchronize them with the Business Glossary.'**
  String get multilingualSyncDesc;

  /// No description provided for @missingLocalesCount.
  ///
  /// In en, this message translates to:
  /// **'Missing Locales Count'**
  String get missingLocalesCount;

  /// No description provided for @missingLangs.
  ///
  /// In en, this message translates to:
  /// **'Missing Languages'**
  String get missingLangs;

  /// No description provided for @suggestedTranslation.
  ///
  /// In en, this message translates to:
  /// **'Glossary Suggestion'**
  String get suggestedTranslation;

  /// No description provided for @allLocalesComplete.
  ///
  /// In en, this message translates to:
  /// **'All fields have complete multilingual definitions.'**
  String get allLocalesComplete;

  /// No description provided for @dataAssetValuation.
  ///
  /// In en, this message translates to:
  /// **'Data Asset Valuation & Catalog Explorer'**
  String get dataAssetValuation;

  /// No description provided for @dataAssetValuationDesc.
  ///
  /// In en, this message translates to:
  /// **'Evaluates data asset ratings and monetary values across enterprise domains based on volume, connectivity, and DQ scores.'**
  String get dataAssetValuationDesc;

  /// No description provided for @totalAssetValue.
  ///
  /// In en, this message translates to:
  /// **'Total Data Asset Value'**
  String get totalAssetValue;

  /// No description provided for @averageQualityScore.
  ///
  /// In en, this message translates to:
  /// **'Average DQ Quality Score'**
  String get averageQualityScore;

  /// No description provided for @assetRating.
  ///
  /// In en, this message translates to:
  /// **'Asset Rating'**
  String get assetRating;

  /// No description provided for @estimatedValue.
  ///
  /// In en, this message translates to:
  /// **'Estimated Value'**
  String get estimatedValue;

  /// No description provided for @schemaCompatibility.
  ///
  /// In en, this message translates to:
  /// **'Schema Backward-Compatibility Analyzer'**
  String get schemaCompatibility;

  /// No description provided for @schemaCompatibilityDesc.
  ///
  /// In en, this message translates to:
  /// **'Statically analyzes breaking change risks across external APIs when altering field types or constraints.'**
  String get schemaCompatibilityDesc;

  /// No description provided for @compatibilityStatus.
  ///
  /// In en, this message translates to:
  /// **'Compatibility Status'**
  String get compatibilityStatus;

  /// No description provided for @riskScore.
  ///
  /// In en, this message translates to:
  /// **'Risk Score'**
  String get riskScore;

  /// No description provided for @checkCompatibility.
  ///
  /// In en, this message translates to:
  /// **'Run Compatibility Check'**
  String get checkCompatibility;

  /// No description provided for @breakingChangeDetected.
  ///
  /// In en, this message translates to:
  /// **'Breaking Changes Detected'**
  String get breakingChangeDetected;

  /// No description provided for @compatibleStatus.
  ///
  /// In en, this message translates to:
  /// **'Backward Compatible'**
  String get compatibleStatus;

  /// No description provided for @semanticOntology.
  ///
  /// In en, this message translates to:
  /// **'Cross-Domain Semantic Ontology Knowledge Graph'**
  String get semanticOntology;

  /// No description provided for @semanticOntologyDesc.
  ///
  /// In en, this message translates to:
  /// **'Explore semantic relationships (purchased-by, contains, supplied-by, managed-by) across enterprise domains.'**
  String get semanticOntologyDesc;

  /// No description provided for @ontologyNodes.
  ///
  /// In en, this message translates to:
  /// **'Ontology Nodes'**
  String get ontologyNodes;

  /// No description provided for @ontologyEdges.
  ///
  /// In en, this message translates to:
  /// **'Semantic Relations (Edges)'**
  String get ontologyEdges;

  /// No description provided for @searchOntology.
  ///
  /// In en, this message translates to:
  /// **'Search Knowledge Graph'**
  String get searchOntology;

  /// No description provided for @relationType.
  ///
  /// In en, this message translates to:
  /// **'Relation Type'**
  String get relationType;

  /// No description provided for @navTabRecords.
  ///
  /// In en, this message translates to:
  /// **'Records'**
  String get navTabRecords;

  /// No description provided for @navTabHome.
  ///
  /// In en, this message translates to:
  /// **'Home'**
  String get navTabHome;

  /// No description provided for @navTabApprovals.
  ///
  /// In en, this message translates to:
  /// **'Approvals'**
  String get navTabApprovals;

  /// No description provided for @navTabNotifications.
  ///
  /// In en, this message translates to:
  /// **'Notifications'**
  String get navTabNotifications;

  /// No description provided for @navTabChat.
  ///
  /// In en, this message translates to:
  /// **'Chat'**
  String get navTabChat;

  /// No description provided for @homeWelcomeTitle.
  ///
  /// In en, this message translates to:
  /// **'Governance Portal Dashboard'**
  String get homeWelcomeTitle;

  /// No description provided for @homeTodoTitle.
  ///
  /// In en, this message translates to:
  /// **'My Pending Tasks'**
  String get homeTodoTitle;

  /// No description provided for @homeRecentActivity.
  ///
  /// In en, this message translates to:
  /// **'Recent Activities & Approvals'**
  String get homeRecentActivity;

  /// No description provided for @homeUnreadMessages.
  ///
  /// In en, this message translates to:
  /// **'Unread Messages'**
  String get homeUnreadMessages;

  /// No description provided for @homeNoActivity.
  ///
  /// In en, this message translates to:
  /// **'No recent activities.'**
  String get homeNoActivity;

  /// No description provided for @notificationsEmpty.
  ///
  /// In en, this message translates to:
  /// **'No new notifications.'**
  String get notificationsEmpty;

  /// No description provided for @chatCreateRoom.
  ///
  /// In en, this message translates to:
  /// **'Create Room'**
  String get chatCreateRoom;

  /// No description provided for @chatRoomTitlePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Enter chat room title'**
  String get chatRoomTitlePlaceholder;

  /// No description provided for @chatTitle.
  ///
  /// In en, this message translates to:
  /// **'Real-time Messenger'**
  String get chatTitle;

  /// No description provided for @chatEmptyRooms.
  ///
  /// In en, this message translates to:
  /// **'No active chat rooms.'**
  String get chatEmptyRooms;

  /// No description provided for @chatSelectMembers.
  ///
  /// In en, this message translates to:
  /// **'Select Members (at least 1 required)'**
  String get chatSelectMembers;

  /// No description provided for @chatSearchSelectUser.
  ///
  /// In en, this message translates to:
  /// **'Search/Select User'**
  String get chatSearchSelectUser;

  /// No description provided for @chatNoUserSelected.
  ///
  /// In en, this message translates to:
  /// **'No user selected.'**
  String get chatNoUserSelected;

  /// No description provided for @chatUserMe.
  ///
  /// In en, this message translates to:
  /// **'{username} (Me)'**
  String chatUserMe(String username);

  /// No description provided for @chatCreateRoomFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to create chat room.'**
  String get chatCreateRoomFailed;

  /// No description provided for @chatSearchUserHint.
  ///
  /// In en, this message translates to:
  /// **'Search by username, role, department...'**
  String get chatSearchUserHint;

  /// No description provided for @chatConfirmBtn.
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get chatConfirmBtn;

  /// No description provided for @allCategories.
  ///
  /// In en, this message translates to:
  /// **'All Categories'**
  String get allCategories;

  /// No description provided for @recordData.
  ///
  /// In en, this message translates to:
  /// **'Record Data'**
  String get recordData;

  /// No description provided for @viewReasonTitle.
  ///
  /// In en, this message translates to:
  /// **'Enter View Reason'**
  String get viewReasonTitle;

  /// No description provided for @viewReasonHint.
  ///
  /// In en, this message translates to:
  /// **'Enter reason (e.g. Identity verification)'**
  String get viewReasonHint;

  /// No description provided for @viewReasonEmpty.
  ///
  /// In en, this message translates to:
  /// **'Reason is required.'**
  String get viewReasonEmpty;

  /// No description provided for @decryptSuccessNotice.
  ///
  /// In en, this message translates to:
  /// **'Decrypted successfully. (Will be masked again in 30 seconds)'**
  String get decryptSuccessNotice;

  /// No description provided for @decryptFailedNotice.
  ///
  /// In en, this message translates to:
  /// **'Decryption failed:'**
  String get decryptFailedNotice;

  /// No description provided for @keyInfo.
  ///
  /// In en, this message translates to:
  /// **'Key Information'**
  String get keyInfo;

  /// No description provided for @generalInfo.
  ///
  /// In en, this message translates to:
  /// **'General Information'**
  String get generalInfo;

  /// No description provided for @viewHistory.
  ///
  /// In en, this message translates to:
  /// **'View History'**
  String get viewHistory;

  /// No description provided for @loginWithKeycloak.
  ///
  /// In en, this message translates to:
  /// **'Login with Keycloak SSO'**
  String get loginWithKeycloak;

  /// No description provided for @loginDividerOr.
  ///
  /// In en, this message translates to:
  /// **'Or login with standard account'**
  String get loginDividerOr;

  /// No description provided for @loginStandard.
  ///
  /// In en, this message translates to:
  /// **'Standard Account Login'**
  String get loginStandard;

  /// No description provided for @loginSsoError.
  ///
  /// In en, this message translates to:
  /// **'An error occurred during SSO login.'**
  String get loginSsoError;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'ko'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'ko':
      return AppLocalizationsKo();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
