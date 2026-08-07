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
/// import 'l10n/app_localizations.dart';
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
  /// **'Department Roles (Multiple Selectable)'**
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
  /// **'Completed'**
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
  /// **'This password will not be shown again. Please copy it and share it securely with the user.'**
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

  /// No description provided for @action.
  ///
  /// In en, this message translates to:
  /// **'Action'**
  String get action;

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
  /// **'Actions'**
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

  /// No description provided for @add.
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get add;

  /// No description provided for @addcomment.
  ///
  /// In en, this message translates to:
  /// **'Add Comment'**
  String get addcomment;

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

  /// No description provided for @alldone.
  ///
  /// In en, this message translates to:
  /// **'All approvals/consensus are completed.'**
  String get alldone;

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

  /// No description provided for @approvalLine.
  ///
  /// In en, this message translates to:
  /// **'Approval Line'**
  String get approvalLine;

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

  /// No description provided for @backupMenuSeed.
  ///
  /// In en, this message translates to:
  /// **'Backup Current State'**
  String get backupMenuSeed;

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

  /// No description provided for @bulkApprove.
  ///
  /// In en, this message translates to:
  /// **'Bulk Approve'**
  String get bulkApprove;

  /// No description provided for @bulkApproveLoading.
  ///
  /// In en, this message translates to:
  /// **'Bulk approving...'**
  String get bulkApproveLoading;

  /// No description provided for @bulkReject.
  ///
  /// In en, this message translates to:
  /// **'Bulk Reject'**
  String get bulkReject;

  /// No description provided for @bulkRejectLoading.
  ///
  /// In en, this message translates to:
  /// **'Bulk rejecting...'**
  String get bulkRejectLoading;

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

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

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
  /// **'Author / Modifier'**
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

  /// No description provided for @close.
  ///
  /// In en, this message translates to:
  /// **'Close'**
  String get close;

  /// No description provided for @colaction.
  ///
  /// In en, this message translates to:
  /// **'Action'**
  String get colaction;

  /// No description provided for @colclassification.
  ///
  /// In en, this message translates to:
  /// **'Classification'**
  String get colclassification;

  /// No description provided for @colcreatedat.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get colcreatedat;

  /// No description provided for @coldomain.
  ///
  /// In en, this message translates to:
  /// **'Domain'**
  String get coldomain;

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

  /// No description provided for @colsummary.
  ///
  /// In en, this message translates to:
  /// **'Summary'**
  String get colsummary;

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

  /// No description provided for @consensus.
  ///
  /// In en, this message translates to:
  /// **'Consensus'**
  String get consensus;

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
  /// **'CREATE'**
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

  /// No description provided for @created.
  ///
  /// In en, this message translates to:
  /// **'Created At'**
  String get created;

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
  /// **'Failed to decrypt (Check permissions)'**
  String get decryptFailed;

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

  /// No description provided for @deleteFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to delete.'**
  String get deleteFailed;

  /// No description provided for @deleteSuccess.
  ///
  /// In en, this message translates to:
  /// **'Delete Completed'**
  String get deleteSuccess;

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

  /// No description provided for @description.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get description;

  /// No description provided for @descriptionCol.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get descriptionCol;

  /// No description provided for @details.
  ///
  /// In en, this message translates to:
  /// **'Details'**
  String get details;

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

  /// No description provided for @domainRecordCreate.
  ///
  /// In en, this message translates to:
  /// **'Domain Record Create'**
  String get domainRecordCreate;

  /// No description provided for @downloadFile.
  ///
  /// In en, this message translates to:
  /// **'Download Completed File'**
  String get downloadFile;

  /// No description provided for @draft.
  ///
  /// In en, this message translates to:
  /// **'Draft'**
  String get draft;

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

  /// No description provided for @draftCompleted.
  ///
  /// In en, this message translates to:
  /// **'Draft Completed'**
  String get draftCompleted;

  /// Translated from e_g_abs_key_a_key_b_2_100
  ///
  /// In en, this message translates to:
  /// **'e.g. ABS({KEY_A} + {KEY_B} / 2) * 100'**
  String eGAbsKeyAKeyB2100(Object KEY_A, Object KEY_B);

  /// No description provided for @edit.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
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

  /// No description provided for @general.
  ///
  /// In en, this message translates to:
  /// **'General'**
  String get general;

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

  /// No description provided for @hideOriginal.
  ///
  /// In en, this message translates to:
  /// **'Hide Original'**
  String get hideOriginal;

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

  /// No description provided for @id.
  ///
  /// In en, this message translates to:
  /// **'ID'**
  String get id;

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

  /// No description provided for @integrationChannels.
  ///
  /// In en, this message translates to:
  /// **'Integration Channels'**
  String get integrationChannels;

  /// No description provided for @integrationDetailTitle.
  ///
  /// In en, this message translates to:
  /// **'Integration Details'**
  String get integrationDetailTitle;

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

  /// No description provided for @isActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get isActive;

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
  /// **'Max Value'**
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

  /// No description provided for @name.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get name;

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

  /// No description provided for @noparsable.
  ///
  /// In en, this message translates to:
  /// **'No parsable data provided.'**
  String get noparsable;

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

  /// No description provided for @noComment.
  ///
  /// In en, this message translates to:
  /// **'No Comment'**
  String get noComment;

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

  /// No description provided for @observers.
  ///
  /// In en, this message translates to:
  /// **'Observers (CC)'**
  String get observers;

  /// No description provided for @onlyDifferences.
  ///
  /// In en, this message translates to:
  /// **'Show Only Differences'**
  String get onlyDifferences;

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

  /// No description provided for @processed.
  ///
  /// In en, this message translates to:
  /// **'Processed'**
  String get processed;

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

  /// No description provided for @recordCreate.
  ///
  /// In en, this message translates to:
  /// **'New Record Create'**
  String get recordCreate;

  /// No description provided for @recordDelete.
  ///
  /// In en, this message translates to:
  /// **'Record Delete'**
  String get recordDelete;

  /// No description provided for @recordUpdate.
  ///
  /// In en, this message translates to:
  /// **'Record Update'**
  String get recordUpdate;

  /// No description provided for @reflectDate.
  ///
  /// In en, this message translates to:
  /// **'Reflect Date'**
  String get reflectDate;

  /// No description provided for @refresh.
  ///
  /// In en, this message translates to:
  /// **'Refresh'**
  String get refresh;

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

  /// No description provided for @review.
  ///
  /// In en, this message translates to:
  /// **'Review'**
  String get review;

  /// No description provided for @riskLevel.
  ///
  /// In en, this message translates to:
  /// **'Risk Level'**
  String get riskLevel;

  /// No description provided for @save.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get save;

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

  /// No description provided for @saveFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to save.'**
  String get saveFailed;

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

  /// No description provided for @selectIcon.
  ///
  /// In en, this message translates to:
  /// **'Select Icon'**
  String get selectIcon;

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

  /// No description provided for @sortOrder.
  ///
  /// In en, this message translates to:
  /// **'Sort Order'**
  String get sortOrder;

  /// No description provided for @startDate.
  ///
  /// In en, this message translates to:
  /// **'Start Date'**
  String get startDate;

  /// No description provided for @status.
  ///
  /// In en, this message translates to:
  /// **'Status'**
  String get status;

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

  /// No description provided for @statusDraft.
  ///
  /// In en, this message translates to:
  /// **'Draft'**
  String get statusDraft;

  /// No description provided for @statusFilter.
  ///
  /// In en, this message translates to:
  /// **'Status Filter'**
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

  /// No description provided for @stepscheduled.
  ///
  /// In en, this message translates to:
  /// **'Scheduled'**
  String get stepscheduled;

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

  /// No description provided for @stepType.
  ///
  /// In en, this message translates to:
  /// **'Step Type'**
  String get stepType;

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

  /// No description provided for @subtitle.
  ///
  /// In en, this message translates to:
  /// **'System monitoring and management details'**
  String get subtitle;

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

  /// No description provided for @targetType.
  ///
  /// In en, this message translates to:
  /// **'Target Type'**
  String get targetType;

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

  /// No description provided for @title.
  ///
  /// In en, this message translates to:
  /// **'Approvals'**
  String get title;

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

  /// No description provided for @type.
  ///
  /// In en, this message translates to:
  /// **'Type'**
  String get type;

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
  /// **'UPDATE'**
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

  /// No description provided for @viewOriginal.
  ///
  /// In en, this message translates to:
  /// **'View Original'**
  String get viewOriginal;

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
  /// **'Associated DQ Rules'**
  String get affectedDqRules;

  /// No description provided for @addDqRule.
  ///
  /// In en, this message translates to:
  /// **'Add Rule'**
  String get addDqRule;

  /// No description provided for @addRule.
  ///
  /// In en, this message translates to:
  /// **'+ Add Rule'**
  String get addRule;

  /// No description provided for @dqDashboardDesc.
  ///
  /// In en, this message translates to:
  /// **'Real-time monitoring of data quality rule compliance, error counts, and field diagnosis status by domain.'**
  String get dqDashboardDesc;

  /// No description provided for @dqDashboardSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Real-time Master Data Governance & Integrity Monitoring'**
  String get dqDashboardSubtitle;

  /// No description provided for @dqDashboardTitle.
  ///
  /// In en, this message translates to:
  /// **'Data Quality Dashboard'**
  String get dqDashboardTitle;

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

  /// No description provided for @dataLineage.
  ///
  /// In en, this message translates to:
  /// **'Data Lineage'**
  String get dataLineage;

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
  /// **'+ Add Editable Field (All if unselected)'**
  String get addEditableFieldPlaceholder;

  /// No description provided for @addField.
  ///
  /// In en, this message translates to:
  /// **'Add Field'**
  String get addField;

  /// No description provided for @addHiddenFieldPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'+ Add Hidden Field'**
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

  /// No description provided for @field.
  ///
  /// In en, this message translates to:
  /// **'Field'**
  String get field;

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

  /// No description provided for @fieldName.
  ///
  /// In en, this message translates to:
  /// **'Field Name'**
  String get fieldName;

  /// No description provided for @fieldPermGroupTitle.
  ///
  /// In en, this message translates to:
  /// **'Attribute Field Permissions'**
  String get fieldPermGroupTitle;

  /// No description provided for @fields.
  ///
  /// In en, this message translates to:
  /// **'Fields'**
  String get fields;

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

  /// No description provided for @nodeIcon.
  ///
  /// In en, this message translates to:
  /// **'Department Icon'**
  String get nodeIcon;

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

  /// No description provided for @selectDomainPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Select Domain'**
  String get selectDomainPlaceholder;

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

  /// No description provided for @targetDomain.
  ///
  /// In en, this message translates to:
  /// **'Target Domain *'**
  String get targetDomain;

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

  /// No description provided for @itemsCount.
  ///
  /// In en, this message translates to:
  /// **' items'**
  String get itemsCount;
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
