<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem; width: 100%;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="corporate_fare" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Tenant" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ t('org_management_desc') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.5rem; align-items: center;">
        <va-button v-if="hasPermission('admin:write') || hasPermission('org:write')" preset="outline" icon="published_with_changes" color="warning" size="small" :loading="isSyncingRoles" @click="handleSyncDefaultRoles">
          {{ $t('sync_default_roles') }}
        </va-button>
        <va-button v-if="hasPermission('admin:write')" preset="outline" icon="save_alt" color="info" size="small" :loading="isSyncingRoles" @click="handleDumpSeedFiles">
          기본 시드(Seed) 파일로 현재 상태 백업
        </va-button>
        <va-button v-if="hasPermission('admin:write') || hasPermission('org:write')" preset="outline" icon="admin_panel_settings" color="primary" size="small" @click="showPermMasterModal = true">
          {{ $t('perm_master_management') }}
        </va-button>
        <va-button v-if="hasPermission('admin:write') || hasPermission('org:write')" color="primary" icon="add" size="small" @click="openCreateOrgModal">
          {{ $t('create_organization') }}
        </va-button>
      </div>
    </div>

    <!-- Main Layout -->
    <div style="display: flex; gap: 1.5rem; align-items: flex-start; flex-wrap: wrap;">
      <!-- Modularized Organization List Sidebar -->
      <OrgTreeSidebar
        :organizations="organizations"
        :selected-id="selectedOrg?.id"
        :loading="loadingOrgs"
        @select-org="selectOrganization"
        @add-org="openCreateOrgModal"
        @delete-org="openDeleteOrgModal"
      />

      <!-- Modularized Organization Detail & Management Form -->
      <OrgDetailForm
        :organization="selectedOrg"
        :departments="departments"
        :root-departments="rootDepartments"
        :roles="roles"
        @save-org="saveOrgInfo"
        @delete-org="openDeleteOrgModal"
        @open-icon-picker="openIconPicker"
        @add-dept="openCreateDeptModal"
        @edit-dept="openEditDeptModal"
        @delete-dept="openDeleteDeptConfirmModal"
        @manage-members="openManageMembersModal"
        @add-role="openCreateRoleModal"
        @edit-role="openEditRoleModal"
        @delete-role="openDeleteRoleConfirmModal"
        @export-roles="exportRoles"
        @import-roles="openImportRolesModal"
      />
    </div>

    <!-- Create Organization Modal -->
    <va-modal v-model="showCreateOrgModalFlag" :title="t('create_new_org')" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <va-input v-model="newOrgForm.name" :label="t('org_code_placeholder')" required />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ t('org_display_name_placeholder') }}
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="newOrgForm.displayNameKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="newOrgForm.displayNameEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ t('org_description') }}
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-textarea v-model="newOrgForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
            </va-textarea>
            <va-textarea v-model="newOrgForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
            </va-textarea>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="showCreateOrgModalFlag = false">{{ t('cancel') }}</va-button>
          <va-button color="primary" @click="saveNewOrg">{{ t('create_organization') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Create Department Modal -->
    <va-modal v-model="showCreateDeptModalFlag" :title="getLabel('add_new_dept', '부서 신규 등록')" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <va-select
          v-model="newDeptForm.parentDepartmentId"
          :options="allDeptOptions"
          value-by="id"
          text-by="displayNameText"
          :label="getLabel('parent_dept', '상위 부서 (미선택 시 최상위 부서)')"
          clearable
        />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ t('dept_name') }} <span style="color: var(--va-danger);">*</span>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="newDeptForm.nameKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="newDeptForm.nameEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary); margin-bottom: 0.5rem;">
            {{ getLabel('node_icon', '부서 아이콘') }}
          </label>
          <div style="display: flex; align-items: center; gap: 1rem; background: var(--va-background-element); padding: 0.5rem 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
            <va-icon :name="newDeptForm.icon || 'folder'" color="primary" size="medium" />
            <va-button preset="primary" outline icon="palette" size="small" @click="openIconPicker('new')">
              {{ getLabel('select_icon', '아이콘 선택') }}
            </va-button>
          </div>
        </div>
        <UserRoleSelect
          v-model="newDeptForm.roles"
          multiple
          :org-id="selectedOrgId"
          :label="getLabel('dept_roles', '부서 역할 (다중 선택 가능)')"
          clearable
        />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('description', '설명') }}
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-textarea v-model="newDeptForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
            </va-textarea>
            <va-textarea v-model="newDeptForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
            </va-textarea>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="showCreateDeptModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" @click="saveNewDept">{{ getLabel('add_department', '부서 등록') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Edit Department Modal -->
    <va-modal v-model="showEditDeptModalFlag" :title="getLabel('edit_dept', '부서/조직 정보 수정')" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <va-select
          v-model="editDeptForm.parentDepartmentId"
          :options="availableParentDeptOptions"
          value-by="id"
          text-by="displayNameText"
          :label="getLabel('parent_dept', '상위 부서 (미선택 시 최상위 부서)')"
          clearable
        />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('dept_name', '부서/조직명') }} <span style="color: var(--va-danger);">*</span>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="editDeptForm.nameKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="editDeptForm.nameEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary); margin-bottom: 0.5rem;">
            {{ getLabel('node_icon', '부서 아이콘') }}
          </label>
          <div style="display: flex; align-items: center; gap: 1rem; background: var(--va-background-element); padding: 0.5rem 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
            <va-icon :name="editDeptForm.icon || 'folder'" color="primary" size="medium" />
            <va-button preset="primary" outline icon="palette" size="small" @click="openIconPicker('edit')">
              {{ getLabel('select_icon', '아이콘 선택') }}
            </va-button>
          </div>
        </div>
        <UserRoleSelect
          v-model="editDeptForm.roles"
          multiple
          :org-id="selectedOrgId"
          :label="getLabel('dept_roles', '부서 역할 (다중 선택 가능)')"
          clearable
        />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('description', '설명') }}
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-textarea v-model="editDeptForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
            </va-textarea>
            <va-textarea v-model="editDeptForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
            </va-textarea>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="showEditDeptModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" @click="saveEditDept">{{ getLabel('save', '저장') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Create Role Modal -->
    <va-modal v-model="showCreateRoleModalFlag" :title="getLabel('create_role_title', '조직 RBAC 역할 신규 등록')" hide-default-actions size="medium" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1.1rem;">
        <va-input v-model="newRoleForm.name" :label="getLabel('role_code_label', '역할 코드명 (예: CUSTOM_MANAGER, DQ_OPERATOR)')" placeholder="영문 대문자" required />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('role_display_name_label', 'ROLE DISPLAY NAME (역할 표시명)') }} <span style="color: var(--va-danger);">*</span>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="newRoleForm.displayNameKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="newRoleForm.displayNameEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('role_description_label', 'ROLE DESCRIPTION (역할 상세 설명)') }}
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-textarea v-model="newRoleForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
            </va-textarea>
            <va-textarea v-model="newRoleForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
            </va-textarea>
          </div>
        </div>
        <PermissionMatrix
          v-model="newRoleForm.permissions"
          :groups="customPermissionGroups"
          :editable="false"
        />
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showCreateRoleModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" @click="saveNewRole">{{ getLabel('register_role_btn', '역할 등록') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Edit Role Modal -->
    <va-modal v-model="showEditRoleModalFlag" :title="getLabel('edit_role_title', '조직 RBAC 역할 정보 수정')" hide-default-actions size="medium" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1.1rem;">
        <va-input v-model="editRoleForm.name" :label="getLabel('role_code_label', '역할 코드명')" readonly class="readonly-sys-code" />
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('role_display_name_label', 'ROLE DISPLAY NAME (역할 표시명)') }} <span style="color: var(--va-danger);">*</span>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="editRoleForm.displayNameKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="editRoleForm.displayNameEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('role_description_label', 'ROLE DESCRIPTION (역할 상세 설명)') }}
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-textarea v-model="editRoleForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
            </va-textarea>
            <va-textarea v-model="editRoleForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
            </va-textarea>
          </div>
        </div>
        <PermissionMatrix
          v-model="editRoleForm.permissions"
          :groups="customPermissionGroups"
          :editable="false"
        />
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showEditRoleModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" @click="saveEditRole">{{ getLabel('save', '저장') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Delete Role Confirm Modal -->
    <va-modal v-model="showDeleteRoleModalFlag" title="역할 삭제 확인" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1.25rem 0; text-align: center;">
        <div style="width: 50px; height: 50px; border-radius: 50%; background: rgba(229, 57, 53, 0.12); color: #b91c1c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem auto;">
          <va-icon name="warning" size="large" color="danger" />
        </div>
        <h4 style="margin: 0 0 0.5rem 0; font-weight: 700; color: var(--va-text-primary);">
          '{{ getI18nText(targetDeletingRole?.displayName) || targetDeletingRole?.name }}' 역할을 삭제하시겠습니까?
        </h4>
        <p style="margin: 0; font-size: 0.88rem; color: var(--va-text-secondary);">
          이 역할을 할당받은 부서나 사용자의 접근 권한에 영향을 줄 수 있습니다.
        </p>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="showDeleteRoleModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
        <va-button color="danger" @click="confirmDeleteRole">{{ getLabel('delete', '삭제') }}</va-button>
      </div>
    </va-modal>

    <!-- Icon Picker Modal -->
    <va-modal v-model="showIconPickerModalFlag" :title="getLabel('select_icon_title', '부서 아이콘 선택')" hide-default-actions size="small">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <div style="font-size: 0.88rem; color: var(--va-text-secondary); font-weight: 600;">
          {{ getLabel('select_icon_desc', '부서 노드 및 헤더에 표시할 커스텀 아이콘을 선택하세요:') }}
        </div>
        <div style="display: grid; grid-template-columns: repeat(5, 1fr); gap: 0.75rem; max-height: 280px; overflow-y: auto; padding: 0.5rem;">
          <div
            v-for="iconName in availableIconList"
            :key="iconName"
            @click="selectIconFromPicker(iconName)"
            style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border); cursor: pointer; transition: all 0.2s;"
            class="hoverable-icon-box"
          >
            <va-icon :name="iconName" color="primary" size="medium" />
            <span style="font-size: 0.7rem; margin-top: 0.35rem; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%;">{{ iconName }}</span>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showIconPickerModalFlag = false">{{ getLabel('close', '닫기') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Delete Department Confirm Modal -->
    <va-modal v-model="showDeleteDeptModalFlag" :title="getLabel('delete_dept', '부서/조직 삭제')" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1.25rem 0; text-align: center;">
        <div style="width: 50px; height: 50px; border-radius: 50%; background: rgba(229, 57, 53, 0.12); color: #b91c1c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem auto;">
          <va-icon name="warning" size="2rem" color="danger" />
        </div>
        <h4 style="margin: 0 0 0.5rem 0; font-weight: 700;">{{ targetDeletingDept?.name }}</h4>
        <p style="font-size: 0.9rem; color: var(--va-text-secondary); margin-bottom: 1.5rem;">
          정말 이 부서/조직을 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다.
        </p>
        <div style="display: flex; justify-content: center; gap: 0.75rem;">
          <va-button preset="secondary" @click="showDeleteDeptModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="danger" @click="confirmDeleteDept">{{ getLabel('delete', '삭제') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Manage Department Members Modal -->
    <va-modal v-model="showManageMembersModalFlag" :title="getLabel('dept_members', '부서 구성원 지정 및 관리')" hide-default-actions size="medium" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1.25rem;">
        <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem;">
          <h4 style="margin: 0 0 0.25rem 0; font-weight: 700; font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
            <va-icon name="folder" color="primary" />
            {{ getI18nText(targetManagingDept?.name) }}
          </h4>
          <p style="margin: 0; font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ getLabel('dept_members_desc', '선택된 부서에 구성원을 신규 추가하거나 할당 해제합니다.') }}
          </p>
        </div>

        <!-- Section 1: Assigned Members -->
        <div>
          <h5 style="font-weight: 700; margin-bottom: 0.75rem; color: var(--va-text-primary); font-size: 0.95rem; display: flex; align-items: center; justify-content: space-between;">
            <span>{{ getLabel('assigned_members_list', '소속 구성원 목록') }} ({{ currentDeptMembers.length }})</span>
          </h5>
          <div v-if="currentDeptMembers.length === 0" style="padding: 1.5rem; text-align: center; color: var(--va-text-secondary); background: var(--va-background-element); border-radius: 6px; font-size: 0.9rem;">
            {{ getLabel('no_assigned_members', '현재 이 부서에 할당된 구성원이 없습니다. 아래에서 구성원을 선택하여 등록하세요.') }}
          </div>
          <div v-else style="display: flex; flex-direction: column; gap: 0.5rem; max-height: 180px; overflow-y: auto;">
            <div
              v-for="user in currentDeptMembers"
              :key="user.id"
              style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.85rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 6px;"
            >
              <div style="display: flex; align-items: center; gap: 0.6rem;">
                <va-icon name="account_circle" color="primary" />
                <span style="font-weight: 700; font-size: 0.95rem;">{{ user.username }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <UserRoleSelect
                  :model-value="getUserRolesArray(user.role)"
                  multiple
                  :org-id="selectedOrgId"
                  @update:modelValue="changeMemberRolesInDept(user, $event)"
                  style="min-width: 280px;"
                  size="small"
                  clearable
                />
                <va-button size="small" color="danger" preset="secondary" icon="person_remove" @click="removeUserFromDept(user)">
                  {{ getLabel('unassign', '할당 해제') }}
                </va-button>
              </div>
            </div>
          </div>
        </div>

        <va-divider />

        <!-- Section 2: Add Unassigned or Other Users to Dept -->
        <div>
          <h5 style="font-weight: 700; margin-bottom: 0.75rem; color: var(--va-text-primary); font-size: 0.95rem; display: flex; justify-content: space-between; align-items: center;">
            <span>{{ getLabel('add_new_member', '신규 구성원 추가') }}</span>
            <va-button color="primary" icon="person_search" size="small" @click="openUserSearchSelectModal">
              + {{ getLabel('search_user_btn', '사용자 검색 및 등록') }}
            </va-button>
          </h5>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
          <va-button preset="primary" @click="showManageMembersModalFlag = false">{{ getLabel('close', '닫기') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Permission Master Management Modal -->
    <va-modal v-model="showPermMasterModal" :title="getLabel('perm_master_management', '세부 권한 마스터 관리')" hide-default-actions size="large" style="--va-modal-max-width: 1000px;" :prevent-click-outside="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 12px; padding: 1.25rem;">
          <PermissionMatrix
            :groups="customPermissionGroups"
            :editable="true"
            @add-group="openAddGroupModal('master')"
            @edit-group="openEditGroupModal"
            @delete-group="deleteGroup"
            @add-perm="($event) => openAddPermToGroupModal('master', $event)"
            @edit-perm="({ group, perm }) => openEditPermModal(group, perm)"
            @delete-perm="({ group, perm }) => deletePermFromGroup(group, perm)"
          />
        </div>
        <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showPermMasterModal = false">{{ getLabel('close', '닫기') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Modal 1: Add New Permission Group -->
    <va-modal v-model="showAddGroupModalFlag" :title="isEditingGroup ? getLabel('edit_perm_group_title', '권한 그룹 수정') : getLabel('add_new_perm_group_title', '신규 권한 그룹 생성')" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('group_name_label', 'GROUP NAME') }} <span style="color: var(--va-danger);">*</span>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="newGroupForm.titleKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="newGroupForm.titleEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <va-input v-model="newGroupForm.code" :label="getLabel('group_code_label', '그룹 코드명 (예: report, api)')" placeholder="영문 소문자" :disabled="isEditingGroup" required />
        <div>
          <label style="display: block; font-size: 0.75rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.35rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('group_icon_picker_label', '이모지 아이콘 선택') }}
          </label>
          <div style="display: flex; align-items: center; background: var(--va-background-secondary); border: 1px solid var(--va-background-border); padding: 0.5rem 0.75rem; border-radius: 8px; margin-bottom: 0.5rem;">
            <span style="font-size: 0.85rem; color: var(--va-text-primary); font-weight: 600;">
              선택된 아이콘: <b style="color: var(--va-primary); font-size: 1.2rem; margin-left: 0.3rem;">{{ newGroupForm.icon || '⚙️' }}</b>
            </span>
          </div>
          <div style="display: grid; grid-template-columns: repeat(8, 1fr); gap: 0.35rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); padding: 0.5rem; border-radius: 8px; max-height: 120px; overflow-y: auto;">
            <button
              v-for="emoji in presetEmojiList"
              :key="emoji"
              type="button"
              @click="newGroupForm.icon = emoji"
              style="font-size: 1.2rem; padding: 0.3rem 0; border: 1.5px solid transparent; border-radius: 6px; cursor: pointer; transition: all 0.15s; background: transparent; display: flex; align-items: center; justify-content: center;"
              :style="{
                borderColor: newGroupForm.icon === emoji ? 'var(--va-primary)' : 'transparent',
                background: newGroupForm.icon === emoji ? 'rgba(21, 101, 192, 0.14)' : 'transparent',
                transform: newGroupForm.icon === emoji ? 'scale(1.15)' : 'none'
              }"
            >
              {{ emoji }}
            </button>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showAddGroupModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" @click="saveNewGroup">{{ isEditingGroup ? getLabel('save', '저장') : getLabel('create_group_btn', '그룹 생성') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Modal 2: Add New Permission to Specific Group -->
    <va-modal v-model="showAddPermToGroupModalFlag" :title="isEditingPerm ? `[${targetGroupForPerm?.title}] ${getLabel('edit_perm_title', '그룹 내 권한 정보 수정')}` : `[${targetGroupForPerm?.title}] ${getLabel('add_perm_to_group_title', '그룹 내 신규 권한 추가')}`" hide-default-actions size="small" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
        <div>
          <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
            {{ getLabel('perm_name_label', 'NAME') }} <span style="color: var(--va-danger);">*</span>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
            <va-input v-model="newPermToGroupForm.labelKo" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
            </va-input>
            <va-input v-model="newPermToGroupForm.labelEn" style="flex: 1; min-width: 0;" required>
              <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
            </va-input>
          </div>
        </div>
        <va-input v-model="newPermToGroupForm.action" :label="getLabel('perm_action_label', '권한 행위/식별자 (예: export, execute)')" placeholder="영문 소문자" :disabled="isEditingPerm" required />
        <div style="font-size: 0.8rem; color: var(--va-text-secondary); background: var(--va-background-element); padding: 0.5rem; border-radius: 6px;">
          {{ getLabel('final_perm_value', '최종 권한 값') }}: <b style="color: var(--va-primary);">{{ targetGroupForPerm?.code }}:{{ newPermToGroupForm.action || 'action' }}</b>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showAddPermToGroupModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" @click="saveNewPermToGroup">{{ isEditingPerm ? getLabel('save', '저장') : getLabel('add_perm_btn', '권한 추가') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- User Search & Select Modal with AG Grid -->
    <va-modal v-model="showUserSearchSelectModalFlag" :title="getLabel('search_user_modal_title', '부서 구성원 검색 및 선택')" hide-default-actions size="large" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem; width: 100%;">
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <va-input
            v-model="userSearchKeyword"
            :placeholder="getLabel('search_user_placeholder', '사용자명 또는 역할 검색...')"
            clearable
            style="flex: 1;"
          >
            <template #prependInner>
              <va-icon name="search" color="primary" />
            </template>
          </va-input>
        </div>

        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; height: 380px; border: 1px solid var(--va-background-border); border-radius: 8px; overflow: hidden;">
          <client-only>
            <ag-grid-vue
              v-if="isMounted"
              style="width: 100%; height: 100%;"
              :theme="gridTheme"
              :columnDefs="userGridColumnDefs"
              :rowData="filteredUsersToSearch"
              :autoSizeStrategy="{ type: 'fitGridWidth' }"
              :pagination="true"
              :paginationPageSize="10"
              :paginationPageSizeSelector="[5, 10, 20, 50]"
            />
          </client-only>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showUserSearchSelectModalFlag = false">{{ getLabel('close', '닫기') }}</va-button>
          <va-button color="primary" icon="save" @click="saveAllSearchModalUserRoles">{{ getLabel('save', '저장') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Multi-Role Selection Modal -->
    <va-modal
      v-model="showMultiRoleEditModalFlag"
      :title="`[${editingRoleUser?.username || ''}] 구성원 역할 지정 (다중 선택)`"
      hide-default-actions
      size="medium"
      :prevent-click-outside="true"
      :no-outside-dismiss="true"
    >
      <div style="padding: 1rem 0; display: flex; flex-direction: column; gap: 1rem;">
        <p style="margin: 0; font-size: 0.9rem; color: var(--va-text-secondary);">
          해당 구성원에게 부여할 하나 이상의 부서 역할을 선택하세요.
        </p>

        <div style="display: flex; flex-direction: column; gap: 0.6rem; max-height: 280px; overflow-y: auto; padding: 0.5rem; border: 1px solid var(--va-background-border); border-radius: 8px; background: var(--va-background-element);">
          <div
            v-for="opt in availableOrgRoleOptions"
            :key="opt.value"
            style="display: flex; align-items: center; justify-content: space-between; padding: 0.5rem 0.75rem; border-radius: 6px; background: var(--va-background-secondary); cursor: pointer;"
            @click="toggleRoleCheckbox(opt.value)"
          >
            <div style="display: flex; align-items: center; gap: 0.6rem;">
              <va-checkbox
                :model-value="selectedRoleCheckboxes.includes(opt.value)"
                @update:model-value="toggleRoleCheckbox(opt.value)"
                @click.stop
              />
              <span style="font-weight: 700; font-size: 0.92rem; color: var(--va-text-primary);">
                {{ opt.label }}
              </span>
            </div>
            <span style="font-family: monospace; font-size: 0.8rem; color: var(--va-text-secondary); background: var(--va-background-border); padding: 2px 6px; border-radius: 4px;">
              {{ opt.cleanValue }}
            </span>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showMultiRoleEditModalFlag = false">{{ getLabel('cancel', '취소') }}</va-button>
          <va-button color="primary" icon="save" @click="saveMultiRoleForUser">{{ getLabel('save', '저장') }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- System Notification Modal -->
    <va-modal
      v-model="showErrorAlertModal"
      :title="errorAlertTitle || $t('system_notification')"
      hide-default-actions
      size="small"
      :prevent-click-outside="true"
      :no-outside-dismiss="true"
    >
      <div style="padding: 1.25rem 0; text-align: center;">
        <div
          v-if="errorAlertType === 'success'"
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(30, 203, 114, 0.12); color: #15803d; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="check_circle" size="2.5rem" color="success" />
        </div>
        <div
          v-else-if="errorAlertType === 'warning'"
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(232, 139, 36, 0.12); color: #c2410c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="warning" size="2.5rem" color="warning" />
        </div>
        <div
          v-else
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(229, 57, 53, 0.12); color: #b91c1c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="error" size="2.5rem" color="danger" />
        </div>

        <h3
          style="margin: 0 0 0.75rem 0; font-weight: 700; font-size: 1.25rem;"
          :style="{
            color: errorAlertType === 'success' ? '#15803d' : (errorAlertType === 'warning' ? '#c2410c' : '#b91c1c')
          }"
        >
          {{ errorAlertHeader || $t('system_notification') }}
        </h3>

        <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem 1.25rem; text-align: left; font-size: 0.92rem; color: var(--va-text-primary); max-height: 200px; overflow-y: auto; margin-bottom: 1.5rem; word-break: break-word; white-space: pre-wrap;">
          {{ errorAlertMessage }}
        </div>

        <div style="display: flex; justify-content: center;">
          <va-button
            :color="errorAlertType === 'success' ? 'success' : (errorAlertType === 'warning' ? 'warning' : 'primary')"
            preset="solid"
            style="min-width: 120px;"
            @click="showErrorAlertModal = false"
          >
            {{ $t('close') || '확인' }}
          </va-button>
        </div>
      </div>
    </va-modal>

    <!-- Organization Delete Confirm Modal -->
    <va-modal
      v-model="showDeleteOrgModalFlag"
      :title="getLabel('delete_organization', '조직 삭제')"
      :ok-text="getLabel('delete', '삭제')"
      ok-color="danger"
      @ok="confirmDeleteOrganization"
    >
      <div style="padding: 0.5rem 0;">
        <p style="margin-bottom: 0.5rem; font-weight: 700; color: var(--va-text-primary); font-size: 1rem;">
          정말로 [{{ getI18nText(targetDeletingOrg?.displayName) || targetDeletingOrg?.name }}] 조직을 삭제하시겠습니까?
        </p>
        <p style="font-size: 0.85rem; color: var(--va-danger); margin: 0; line-height: 1.4;">
          ⚠️ 조직 삭제 시 해당 조직에 속한 하위 부서, 팀 및 RBAC 역할/권한 정보가 함께 삭제됩니다.
        </p>
      </div>
    </va-modal>
    <input type="file" ref="fileInput" style="display: none" accept=".json" @change="handleImportFile" />
  </div>
</template>

<script setup>
import OrgTreeSidebar from '~/components/org/OrgTreeSidebar.vue'
import OrgDetailForm from '~/components/org/OrgDetailForm.vue'
import { usePageTitle } from '~/composables/usePageTitle'

const { pageTitle } = usePageTitle('org_tenant_management', '조직 및 부서 관리')
import { ref, onMounted, computed } from 'vue'
import { useCookie } from '#app'
import { AgGridVue } from 'ag-grid-vue3'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import PermissionMatrix from '~/components/PermissionMatrix.vue'
import UserRoleSelect from '~/components/UserRoleSelect.vue'
import { useRoleStore } from '~/stores/useRoleStore'
import { usePermission } from '~/composables/usePermission'

const { gridTheme, isDark } = useAgGridTheme()
const isMounted = ref(false)
const roleStore = useRoleStore()
const { hasPermission } = usePermission()
const isSyncingRoles = ref(false)

const { t, locale } = useI18n()
const { init: initToast } = useToast()

const getLabel = (key, fallback) => {
  const translated = t(key)
  return (translated && translated !== key) ? translated : (fallback || key)
}


const getI18nText = (textStr) => {
  if (!textStr) return ''
  try {
    const parsed = typeof textStr === 'object' ? textStr : JSON.parse(textStr)
    if (parsed && typeof parsed === 'object') {
      const loc = (locale?.value || 'ko').toLowerCase()
      return loc.startsWith('en') ? (parsed.en || parsed.ko || '') : (parsed.ko || parsed.en || '')
    }
    return String(textStr)
  } catch (e) {
    return textStr
  }
}

const handleSyncDefaultRoles = async () => {
  const targetOrgId = selectedOrg.value?.id
  const targetName = selectedOrg.value ? (getI18nText(selectedOrg.value.displayName) || selectedOrg.value.name) : ''
  
  const confirmMsg = targetOrgId 
    ? t('sync_default_roles_confirm_org', { name: targetName })
    : t('sync_default_roles_confirm_all')

  if (!confirm(confirmMsg)) return

  isSyncingRoles.value = true
  try {
    const success = await roleStore.syncDefaultRoles(targetOrgId)
    if (success) {
      if (typeof initToast === 'function') {
        initToast({ message: t('sync_default_roles_success'), color: 'success' })
      }
      if (targetOrgId && typeof loadRoles === 'function') {
        await loadRoles(targetOrgId)
      } else if (typeof loadOrganizations === 'function') {
        await loadOrganizations()
      }
    } else {
      if (typeof initToast === 'function') {
        initToast({ message: t('sync_default_roles_fail'), color: 'danger' })
      }
    }
  } catch (e) {
    console.error(e)
    if (typeof initToast === 'function') {
      initToast({ message: t('sync_default_roles_error'), color: 'danger' })
    }
  } finally {
    isSyncingRoles.value = false
  }
}

const handleDumpSeedFiles = async () => {
  const targetOrgId = selectedOrg.value?.id
  const targetName = selectedOrg.value ? (getI18nText(selectedOrg.value.displayName) || selectedOrg.value.name) : ''
  const msg = targetOrgId 
    ? `현재 '${targetName}' 조직에 셋팅된 모든 역할 상태를 시스템 기본값(Seed) JSON 파일로 백업/덮어쓰시겠습니까?\n(이 작업은 소스코드 디렉토리 내의 파일을 직접 수정합니다)`
    : '현재 전체 DB에 저장된 모든 역할 상태를 시스템 기본값(Seed) JSON 파일로 백업/덮어쓰시겠습니까?\n(이 작업은 소스코드 디렉토리 내의 파일을 직접 수정합니다)'

  if (!confirm(msg)) return

  isSyncingRoles.value = true
  try {
    const success = await roleStore.dumpSeedFiles(targetOrgId)
    if (success) {
      if (typeof initToast === 'function') {
        initToast({ message: '기본 시드(Seed) 파일 갱신 완료', color: 'success' })
      }
    } else {
      if (typeof initToast === 'function') {
        initToast({ message: '기본 시드 파일 갱신 실패', color: 'danger' })
      }
    }
  } catch (e) {
    console.error(e)
    if (typeof initToast === 'function') {
      initToast({ message: '기본 시드 파일 갱신 중 오류 발생', color: 'danger' })
    }
  } finally {
    isSyncingRoles.value = false
  }
}

const token = useCookie('auth_token')
const showErrorAlertModal = ref(false)
const errorAlertTitle = ref('')
const errorAlertHeader = ref('')
const errorAlertMessage = ref('')
const errorAlertType = ref('success')

const showCustomAlert = (msg, header = '', title = '', type = 'success') => {
  errorAlertMessage.value = msg
  errorAlertHeader.value = header
  errorAlertTitle.value = title
  errorAlertType.value = type
  showErrorAlertModal.value = true
}

const organizations = ref([])
const loadingOrgs = ref(false)
const selectedOrg = ref(null)
const selectedOrgId = computed(() => selectedOrg.value?.id)
const showPermMasterModal = ref(false)

const departments = ref([])
const teams = ref([])
const roles = ref([])

const showCreateOrgModalFlag = ref(false)
const newOrgForm = ref({ name: '', displayNameKo: '', displayNameEn: '', descriptionKo: '', descriptionEn: '', icon: 'corporate_fare' })

const showCreateDeptModalFlag = ref(false)
const newDeptForm = ref({ parentDepartmentId: null, nameKo: '', nameEn: '', descriptionKo: '', descriptionEn: '', roles: [], icon: 'folder' })

const showEditDeptModalFlag = ref(false)
const editDeptForm = ref({ id: null, parentDepartmentId: null, nameKo: '', nameEn: '', descriptionKo: '', descriptionEn: '', roles: [], icon: 'folder' })

const showDeleteDeptModalFlag = ref(false)
const targetDeletingDept = ref(null)

const availableIconList = ref([
  'folder', 'corporate_fare', 'domain', 'business', 'school', 'apartment',
  'account_tree', 'groups', 'hub', 'work', 'star', 'widgets',
  'dashboard', 'settings', 'analytics', 'security', 'shield', 'cloud',
  'verified', 'workspace_premium', 'device_hub', 'badge', 'store', 'local_offer',
  'emoji_events', 'science', 'biotech', 'memory', 'speed', 'auto_awesome'
])

const showIconPickerModalFlag = ref(false)
const iconPickerTarget = ref('edit')

const openIconPicker = (target) => {
  iconPickerTarget.value = target
  showIconPickerModalFlag.value = true
}

const selectIconFromPicker = (iconName) => {
  if (iconPickerTarget.value === 'new') {
    newDeptForm.value.icon = iconName
  } else if (iconPickerTarget.value === 'org') {
    if (selectedOrg.value) selectedOrg.value.icon = iconName
  } else {
    editDeptForm.value.icon = iconName
  }
  showIconPickerModalFlag.value = false
}

const availableParentDeptOptions = computed(() => {
  if (!departments.value) return []
  return departments.value
    .filter(d => !editDeptForm.value.id || d.id !== editDeptForm.value.id)
    .map(d => ({
      ...d,
      displayNameText: getI18nText(d.name)
    }))
})

const allDeptOptions = computed(() => {
  if (!departments.value) return []
  return departments.value.map(d => ({
    ...d,
    displayNameText: getI18nText(d.name)
  }))
})

const parseMultilingualField = (rawVal) => {
  if (!rawVal) return { ko: '', en: '' }
  if (typeof rawVal === 'object') {
    return { ko: rawVal.ko || '', en: rawVal.en || '' }
  }
  try {
    const parsed = JSON.parse(rawVal)
    if (parsed && typeof parsed === 'object' && ('ko' in parsed || 'en' in parsed)) {
      return { ko: parsed.ko || '', en: parsed.en || '' }
    }
  } catch (e) {}
  return { ko: String(rawVal), en: String(rawVal) }
}

const openEditDeptModal = (dept) => {
  const rolesArr = dept.role ? (Array.isArray(dept.role) ? dept.role : String(dept.role).split(',').map(r => r.trim()).filter(Boolean)) : []
  const parsedName = parseMultilingualField(dept.name)
  const parsedDesc = parseMultilingualField(dept.description)

  editDeptForm.value = {
    id: dept.id,
    parentDepartmentId: dept.parentDepartmentId || null,
    nameKo: parsedName.ko,
    nameEn: parsedName.en,
    descriptionKo: parsedDesc.ko,
    descriptionEn: parsedDesc.en,
    roles: rolesArr,
    icon: dept.icon || 'folder'
  }
  showEditDeptModalFlag.value = true
}

const saveEditDept = async () => {
  if (!selectedOrg.value || (!editDeptForm.value.nameKo && !editDeptForm.value.nameEn)) return
  const roleStr = Array.isArray(editDeptForm.value.roles) ? editDeptForm.value.roles.join(',') : (editDeptForm.value.roles || '')
  try {
    await $fetch(`/api/organizations/${selectedOrg.value.id}/departments/${editDeptForm.value.id}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        id: editDeptForm.value.id,
        parentDepartmentId: editDeptForm.value.parentDepartmentId,
        name: JSON.stringify({ ko: editDeptForm.value.nameKo || editDeptForm.value.nameEn, en: editDeptForm.value.nameEn || editDeptForm.value.nameKo }),
        description: JSON.stringify({ ko: editDeptForm.value.descriptionKo, en: editDeptForm.value.descriptionEn }),
        roles: Array.isArray(editDeptForm.value.roles) ? editDeptForm.value.roles : [],
        role: roleStr,
        icon: editDeptForm.value.icon
      }
    })
    showEditDeptModalFlag.value = false
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert(getLabel('dept_updated_success', '부서 정보가 성공적으로 수정되었습니다.'), getLabel('update_success', '수정 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to update department: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const openDeleteDeptConfirmModal = (dept) => {
  targetDeletingDept.value = dept
  showDeleteDeptModalFlag.value = true
}

const confirmDeleteDept = async () => {
  if (!selectedOrg.value || !targetDeletingDept.value) return
  try {
    await $fetch(`/api/organizations/${selectedOrg.value.id}/departments/${targetDeletingDept.value.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    showDeleteDeptModalFlag.value = false
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert('부서가 성공적으로 삭제되었습니다.', t('delete_success') || '삭제 완료', t('notification') || '알림', 'success')
  } catch (e) {
    showCustomAlert('Failed to delete department: ' + (e.message || String(e)), t('error') || 'Error', t('notification') || 'Notification', 'error')
  }
}

const showManageMembersModalFlag = ref(false)
const targetManagingDept = ref(null)
const allUsersList = ref([])
const showUserSearchSelectModalFlag = ref(false)
const userSearchKeyword = ref('')

const fetchAllUsersList = async () => {
  try {
    const res = await $fetch('/api/permissions/users', {
      headers: { Authorization: `Bearer ${token.value}` },
      query: { page: 0, size: 100 }
    })
    allUsersList.value = res.content || []
  } catch (e) {
    console.error('Failed to fetch users:', e)
  }
}

const currentDeptMembers = computed(() => {
  if (!targetManagingDept.value) return []
  return allUsersList.value.filter(u => u.departmentId === targetManagingDept.value.id)
})

const filteredUsersToSearch = computed(() => {
  if (!allUsersList.value) return []
  const kw = userSearchKeyword.value ? userSearchKeyword.value.toLowerCase().trim() : ''
  if (!kw) return allUsersList.value
  return allUsersList.value.filter(u =>
    (u.username && u.username.toLowerCase().includes(kw)) ||
    (u.role && u.role.toLowerCase().includes(kw))
  )
})

const showMultiRoleEditModalFlag = ref(false)
const editingRoleUser = ref(null)
const selectedRoleCheckboxes = ref([])

const availableOrgRoleOptions = computed(() => {
  const loadedRoles = roleStore.rolesList.value || []

  return loadedRoles.map(r => {
    const rawName = r.name || ''
    const cleanName = rawName.startsWith('ROLE_') ? rawName.replace('ROLE_', '') : rawName
    const dispName = getI18nText(r.displayName) || getI18nText(r.name) || cleanName
    return {
      value: rawName,
      cleanValue: cleanName,
      label: dispName
    }
  })
})

const openMultiRoleEditModal = (user) => {
  editingRoleUser.value = user
  const userRolesArr = getUserRolesArray(user.role)
  selectedRoleCheckboxes.value = availableOrgRoleOptions.value
    .filter(opt => userRolesArr.some(ur => ur === opt.value || ur === opt.cleanValue || `ROLE_${ur}` === opt.value))
    .map(opt => opt.value)
  showMultiRoleEditModalFlag.value = true
}

const toggleRoleCheckbox = (val) => {
  const idx = selectedRoleCheckboxes.value.indexOf(val)
  if (idx >= 0) {
    selectedRoleCheckboxes.value.splice(idx, 1)
  } else {
    selectedRoleCheckboxes.value.push(val)
  }
}

const saveMultiRoleForUser = async () => {
  if (!editingRoleUser.value || !targetManagingDept.value) return
  const user = editingRoleUser.value
  const chosenRolesStr = selectedRoleCheckboxes.value.join(',')
  try {
    await $fetch(`/api/permissions/users/${user.id}/tenant-info`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        organizationId: selectedOrg.value.id,
        departmentId: targetManagingDept.value.id,
        role: chosenRolesStr
      }
    })
    user.role = chosenRolesStr
    user.departmentId = targetManagingDept.value.id
    updateLoggedUserCookieIfSelf(user.id, selectedOrg.value.id, targetManagingDept.value.id, chosenRolesStr)
    await fetchAllUsersList()
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert(`'${user.username}' 구성원의 역할이 성공적으로 저장되었습니다.`, getLabel('update_success', '저장 완료'), getLabel('notification', '알림'), 'success')
    showMultiRoleEditModalFlag.value = false
  } catch (e) {
    showCustomAlert('Failed to update user roles: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const userGridColumnDefs = computed(() => [
  {
    headerName: getLabel('username_col', '사용자명 (Username)'),
    field: 'username',
    flex: 2,
    minWidth: 140,
    cellRenderer: (params) => {
      if (!params.data) return ''
      return `<div style="font-weight: 700; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
        <span>👤</span> <span>${params.value || ''}</span>
      </div>`
    }
  },
  {
    headerName: getLabel('role_assign_col', '부서 할당 역할 (다중 지정 가능)'),
    field: 'role',
    flex: 2.5,
    minWidth: 220,
    cellRenderer: (params) => {
      if (!params.data) return ''
      const userRolesArr = getUserRolesArray(params.value)
      const options = availableOrgRoleOptions.value

      const chipsHtml = userRolesArr.map(r => {
        const clean = r.startsWith('ROLE_') ? r.replace('ROLE_', '') : r
        const matched = options.find(opt => opt.value === r || opt.cleanValue === clean)
        const labelStr = matched ? matched.label : clean
        return `<span style="background: rgba(37, 99, 235, 0.1); color: #2563eb; padding: 2px 7px; border-radius: 4px; font-size: 0.75rem; font-weight: 700; border: 1px solid rgba(37, 99, 235, 0.2); margin-right: 3px; display: inline-block;">${labelStr}</span>`
      }).join('')

      return `<div style="display: flex; align-items: center; justify-content: space-between; gap: 0.4rem; width: 100%;">
        <div style="display: flex; flex-wrap: wrap; gap: 0.2rem; flex: 1; min-width: 0; overflow: hidden; align-items: center;">${chipsHtml || '<span style="color: #9ca3af; font-size: 0.75rem;">역할 없음</span>'}</div>
        <button style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); color: var(--va-text-primary); padding: 3px 8px; border-radius: 4px; font-size: 0.75rem; cursor: pointer; font-weight: 600; flex-shrink: 0;">✏️ 역할 변경</button>
      </div>`
    },
    onCellClicked: (params) => {
      if (params.data) {
        openMultiRoleEditModal(params.data)
      }
    }
  },
  {
    headerName: getLabel('dept_status_col', '소속 상태'),
    field: 'departmentId',
    flex: 1,
    minWidth: 110,
    cellRenderer: (params) => {
      if (!params.data) return ''
      const isCurrent = params.value === targetManagingDept.value?.id
      if (isCurrent) {
        return `<span style="color: #16a34a; font-weight: 700; font-size: 0.85rem;">📁 ${getLabel('current_dept', '현재 부서')}</span>`
      } else if (params.value) {
        return `<span style="color: #d97706; font-weight: 600; font-size: 0.85rem;">${getLabel('other_dept', '타부서 소속')}</span>`
      } else {
        return `<span style="color: #6b7280; font-style: italic; font-size: 0.85rem;">${getLabel('unassigned', '미할당')}</span>`
      }
    }
  },
  {
    headerName: getLabel('dept_assign_col', '부서 지정 / 역할 저장'),
    field: 'actions',
    flex: 1.3,
    minWidth: 140,
    cellRenderer: (params) => {
      if (!params.data) return ''
      const isCurrent = params.data.departmentId === targetManagingDept.value?.id
      if (isCurrent) {
        return `<button style="background: #16a34a; color: white; border: none; border-radius: 4px; padding: 4px 10px; font-size: 0.8rem; cursor: pointer; font-weight: 600;">💾 ${getLabel('save', '저장')}</button>`
      }
      return `<button style="background: #2563eb; color: white; border: none; border-radius: 4px; padding: 4px 10px; font-size: 0.8rem; cursor: pointer; font-weight: 600;">+ ${getLabel('assign_dept', '부서 등록')}</button>`
    },
    onCellClicked: (params) => {
      if (params.data) {
        assignUserFromSearchModal(params.data)
      }
    }
  }
])

const openManageMembersModal = async (dept) => {
  targetManagingDept.value = dept
  if (selectedOrg.value?.id) {
    await roleStore.fetchRolesForOrg(selectedOrg.value.id, true)
  }
  await fetchAllUsersList()
  showManageMembersModalFlag.value = true
}

const openUserSearchSelectModal = async () => {
  userSearchKeyword.value = ''
  if (selectedOrg.value?.id) {
    await roleStore.fetchRolesForOrg(selectedOrg.value.id, true)
  }
  showUserSearchSelectModalFlag.value = true
}

const userCookie = useCookie('user_data')

const updateLoggedUserCookieIfSelf = (userId, orgId, deptId, role = null) => {
  if (userCookie.value) {
    const usr = typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
    if (usr && (usr.id === userId || usr.uuid === userId)) {
      if (orgId !== undefined) usr.organizationId = orgId
      if (deptId !== undefined) usr.departmentId = deptId
      if (role) usr.role = role
      userCookie.value = JSON.stringify(usr)
    }
  }
}

const getUserRolesArray = (role) => {
  if (!role) return ['USER']
  if (Array.isArray(role)) return role
  return String(role).split(',').map(r => r.trim()).filter(Boolean)
}

const changeMemberRolesInDept = async (user, newRoles) => {
  if (!user) return
  const roleStr = Array.isArray(newRoles) ? newRoles.join(',') : (newRoles || 'USER')
  try {
    await $fetch(`/api/permissions/users/${user.id}/tenant-info`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        organizationId: selectedOrg.value.id,
        departmentId: user.departmentId,
        role: roleStr
      }
    })
    user.role = roleStr
    updateLoggedUserCookieIfSelf(user.id, selectedOrg.value.id, user.departmentId, roleStr)
    await fetchAllUsersList()
    showCustomAlert(`'${user.username}' 구성원의 역할이 '${roleStr}'(으)로 변경되었습니다.`, getLabel('update_success', '수정 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to update member role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const assignUserFromSearchModal = async (user) => {
  if (!targetManagingDept.value || !user) return
  const selectElem = document.getElementById(`role-sel-${user.id}`)
  const chosenRole = selectElem ? selectElem.value : (user.role || 'USER')
  const isCurrent = user.departmentId === targetManagingDept.value.id

  try {
    await $fetch(`/api/permissions/users/${user.id}/tenant-info`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        organizationId: selectedOrg.value.id,
        departmentId: targetManagingDept.value.id,
        role: chosenRole
      }
    })
    user.role = chosenRole
    user.departmentId = targetManagingDept.value.id
    updateLoggedUserCookieIfSelf(user.id, selectedOrg.value.id, targetManagingDept.value.id, chosenRole)
    await fetchAllUsersList()
    await loadOrgDetails(selectedOrg.value.id)
    const actionText = isCurrent ? '역할 변경 저장' : '부서 및 역할 등록'
    showCustomAlert(`'${user.username}' 구성원이 [${chosenRole}] 역할로 ${actionText} 되었습니다.`, getLabel('update_success', `${actionText} 완료`), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to update user dept/role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const saveAllSearchModalUserRoles = async () => {
  if (!targetManagingDept.value || !filteredUsersToSearch.value) return
  let updatedCount = 0
  for (const user of filteredUsersToSearch.value) {
    const selectElem = document.getElementById(`role-sel-${user.id}`)
    const chosenRole = selectElem ? selectElem.value : user.role
    const isCurrent = user.departmentId === targetManagingDept.value.id
    if (isCurrent && chosenRole && chosenRole !== user.role) {
      try {
        await $fetch(`/api/permissions/users/${user.id}/tenant-info`, {
          method: 'PUT',
          headers: { Authorization: `Bearer ${token.value}` },
          body: {
            organizationId: selectedOrg.value.id,
            departmentId: targetManagingDept.value.id,
            role: chosenRole
          }
        })
        user.role = chosenRole
        updateLoggedUserCookieIfSelf(user.id, selectedOrg.value.id, targetManagingDept.value.id, chosenRole)
        updatedCount++
      } catch (e) {
        console.error(`Failed to update role for user ${user.username}`, e)
      }
    }
  }
  await fetchAllUsersList()
  await loadOrgDetails(selectedOrg.value.id)
  showCustomAlert(updatedCount > 0 ? `${updatedCount}명 구성원의 역할 변경이 저장되었습니다.` : '모든 사용자의 부서 및 역할 정보가 최신 상태입니다.', getLabel('save_success', '저장 완료'), getLabel('notification', '알림'), 'success')
  showUserSearchSelectModalFlag.value = false
}

const removeUserFromDept = async (user) => {
  if (!targetManagingDept.value || !user) return
  try {
    await $fetch(`/api/permissions/users/${user.id}/tenant-info`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        organizationId: selectedOrg.value.id,
        departmentId: null
      }
    })
    updateLoggedUserCookieIfSelf(user.id, selectedOrg.value.id, null)
    await fetchAllUsersList()
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert('구성원의 부서 할당이 해제되었습니다.', getLabel('update_success', '해제 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to remove user from dept: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const rootDepartments = computed(() => {
  if (!departments.value) return []
  
  const memberCounts = {}
  allUsersList.value.forEach(u => {
    if (u.departmentId) {
      memberCounts[u.departmentId] = (memberCounts[u.departmentId] || 0) + 1
    }
  })

  const map = {}
  departments.value.forEach(d => {
    map[d.id] = {
      ...d,
      memberCount: memberCounts[d.id] || 0,
      subDepts: []
    }
  })

  const roots = []
  departments.value.forEach(d => {
    const node = map[d.id]
    if (d.parentDepartmentId && map[d.parentDepartmentId]) {
      map[d.parentDepartmentId].subDepts.push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
})

const openCreateDeptModal = (parentDeptId = null) => {
  newDeptForm.value = { parentDepartmentId: parentDeptId, nameKo: '', nameEn: '', descriptionKo: '', descriptionEn: '', roles: [], icon: 'folder' }
  showCreateDeptModalFlag.value = true
}

const fetchOrganizations = async () => {
  loadingOrgs.value = true
  try {
    const res = await $fetch('/api/organizations', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    organizations.value = res || []
    if (organizations.value.length > 0 && !selectedOrg.value) {
      selectOrganization(organizations.value[0])
    }
    await fetchAllUsersList()
  } catch (e) {
    console.error('Failed to fetch organizations:', e)
  } finally {
    loadingOrgs.value = false
  }
}

const selectOrganization = async (org) => {
  selectedOrg.value = org
  await loadOrgDetails(org.id)
}

const loadOrgDetails = async (orgId) => {
  try {
    const [deptsRes, teamsRes, rolesRes] = await Promise.all([
      $fetch(`/api/organizations/${orgId}/departments`, { headers: { Authorization: `Bearer ${token.value}` } }),
      $fetch(`/api/organizations/${orgId}/teams`, { headers: { Authorization: `Bearer ${token.value}` } }),
      $fetch(`/api/roles/org/${orgId}`, { headers: { Authorization: `Bearer ${token.value}` } })
    ])
    departments.value = deptsRes || []
    teams.value = teamsRes || []
    roles.value = rolesRes || []
  } catch (e) {
    console.error('Failed to load org details:', e)
  }
}

const showDeleteOrgModalFlag = ref(false)
const targetDeletingOrg = ref(null)

const openDeleteOrgModal = (org) => {
  if (!org) return
  if (org.id === '00000000-0000-0000-0000-000000000001') {
    showCustomAlert(
      '기본 시스템 조직은 삭제할 수 없습니다.',
      getLabel('warning', '경고'),
      getLabel('notification', '알림'),
      'warning'
    )
    return
  }
  targetDeletingOrg.value = org
  showDeleteOrgModalFlag.value = true
}

const confirmDeleteOrganization = async () => {
  if (!targetDeletingOrg.value) return
  try {
    await $fetch(`/api/organizations/${targetDeletingOrg.value.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    showDeleteOrgModalFlag.value = false
    const deletedId = targetDeletingOrg.value.id
    targetDeletingOrg.value = null
    if (selectedOrg.value?.id === deletedId) {
      selectedOrg.value = null
    }
    await fetchOrganizations()
    showCustomAlert(
      getLabel('org_delete_success', '조직이 성공적으로 삭제되었습니다.'),
      getLabel('delete_success', '삭제 완료'),
      getLabel('notification', '알림'),
      'success'
    )
  } catch (e) {
    console.error('Failed to delete organization:', e)
    showCustomAlert('Failed to delete org: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const openCreateOrgModal = () => {
  newOrgForm.value = { name: '', displayNameKo: '', displayNameEn: '', descriptionKo: '', descriptionEn: '', icon: 'corporate_fare' }
  showCreateOrgModalFlag.value = true
}

const saveNewOrg = async () => {
  if (!newOrgForm.value.name) return
  try {
    const created = await $fetch('/api/organizations', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        name: newOrgForm.value.name.trim(),
        displayName: JSON.stringify({ ko: newOrgForm.value.displayNameKo || newOrgForm.value.name, en: newOrgForm.value.displayNameEn || newOrgForm.value.displayNameKo || newOrgForm.value.name }),
        description: JSON.stringify({ ko: newOrgForm.value.descriptionKo, en: newOrgForm.value.descriptionEn }),
        icon: newOrgForm.value.icon || 'corporate_fare'
      }
    })
    showCreateOrgModalFlag.value = false
    await fetchOrganizations()
    if (created) selectOrganization(created)
    showCustomAlert(
      getLabel('org_created_success', '조직이 성공적으로 생성되었습니다.'),
      getLabel('creation_success', '생성 완료'),
      getLabel('notification', '알림'),
      'success'
    )
  } catch (e) {
    showCustomAlert('Failed to create organization: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const saveOrgInfo = async (editOrgFormData) => {
  if (!selectedOrg.value) return
  try {
    const updated = await $fetch(`/api/organizations/${selectedOrg.value.id}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        name: editOrgFormData.name,
        displayName: JSON.stringify({ ko: editOrgFormData.displayNameKo || editOrgFormData.name, en: editOrgFormData.displayNameEn || editOrgFormData.displayNameKo || editOrgFormData.name }),
        description: JSON.stringify({ ko: editOrgFormData.descriptionKo, en: editOrgFormData.descriptionEn }),
        icon: editOrgFormData.icon
      }
    })
    await fetchOrganizations()
    if (updated) {
      selectOrganization(updated)
    }
    showCustomAlert(
      getLabel('org_updated_success', '조직 정보가 성공적으로 수정되었습니다.'),
      getLabel('update_success', '수정 완료'),
      getLabel('notification', '알림'),
      'success'
    )
  } catch (e) {
    showCustomAlert('Failed to update organization: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const saveNewDept = async () => {
  if (!selectedOrg.value || (!newDeptForm.value.nameKo && !newDeptForm.value.nameEn)) return
  const roleStr = Array.isArray(newDeptForm.value.roles) ? newDeptForm.value.roles.join(',') : (newDeptForm.value.roles || '')
  try {
    await $fetch(`/api/organizations/${selectedOrg.value.id}/departments`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        parentDepartmentId: newDeptForm.value.parentDepartmentId,
        name: JSON.stringify({ ko: newDeptForm.value.nameKo || newDeptForm.value.nameEn, en: newDeptForm.value.nameEn || newDeptForm.value.nameKo }),
        description: JSON.stringify({ ko: newDeptForm.value.descriptionKo, en: newDeptForm.value.descriptionEn }),
        role: roleStr,
        icon: newDeptForm.value.icon || 'folder'
      }
    })
    showCreateDeptModalFlag.value = false
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert(
      getLabel('dept_created_success', '부서가 성공적으로 생성되었습니다.'),
      getLabel('creation_success', '생성 완료'),
      getLabel('notification', '알림'),
      'success'
    )
  } catch (e) {
    showCustomAlert('Failed to create department: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const customPermissionGroups = ref([
  {
    id: 'domain',
    title: '도메인 권한',
    titleEn: 'Domain Permissions',
    titleKey: 'domain_perm_group_title',
    code: 'domain',
    icon: '🌐',
    color: '#3b82f6',
    chipClass: '',
    permissions: [
      { label: '도메인 전체 (*)', labelEn: 'Domain All (*)', labelKey: 'perm_all', value: 'domain:*' },
      { label: '조회 (read)', labelEn: 'Read (read)', labelKey: 'perm_read', value: 'domain:read' },
      { label: '작성 (write)', labelEn: 'Write (write)', labelKey: 'perm_write', value: 'domain:write' }
    ]
  },
  {
    id: 'node',
    title: '분류 노드 권한',
    titleEn: 'Category Node Permissions',
    titleKey: 'node_perm_group_title',
    code: 'node',
    icon: '📁',
    color: '#10b981',
    chipClass: 'green',
    permissions: [
      { label: '노드 전체 (*)', labelEn: 'Category Node All (*)', labelKey: 'perm_all', value: 'node:*' },
      { label: '조회 (read)', labelEn: 'Read (read)', labelKey: 'perm_read', value: 'node:read' },
      { label: '작성 (write)', labelEn: 'Write (write)', labelKey: 'perm_write', value: 'node:write' }
    ]
  },
  {
    id: 'field',
    title: '속성 필드 권한',
    titleEn: 'Attribute Field Permissions',
    titleKey: 'field_perm_group_title',
    code: 'field',
    icon: '🏷️',
    color: '#8b5cf6',
    chipClass: 'purple',
    permissions: [
      { label: '필드 전체 (*)', labelEn: 'Attribute Field All (*)', labelKey: 'perm_all', value: 'field:*' },
      { label: '조회 (read)', labelEn: 'Read (read)', labelKey: 'perm_read', value: 'field:read' },
      { label: '작성 (write)', labelEn: 'Write (write)', labelKey: 'perm_write', value: 'field:write' }
    ]
  },
  {
    id: 'dq',
    title: '데이터 품질 권한',
    titleEn: 'Data Quality Permissions',
    titleKey: 'dq_perm_group_title',
    code: 'dq',
    icon: '⚡',
    color: '#f59e0b',
    chipClass: 'amber',
    permissions: [
      { label: 'DQ 전체 (*)', labelEn: 'Data Quality All (*)', labelKey: 'perm_all', value: 'dq:*' },
      { label: '조회 (read)', labelEn: 'Read (read)', labelKey: 'perm_read', value: 'dq:read' },
      { label: '작성 (write)', labelEn: 'Write (write)', labelKey: 'perm_write', value: 'dq:write' }
    ]
  }
])

const showAddGroupModalFlag = ref(false)
const modalTargetContext = ref('new')
const presetEmojiList = ['⚙️', '🌐', '📁', '📌', '⚡', '📊', '🔑', '🛡️', '🔒', '👤', '🏢', '📦', '📑', '🛠️', '🎯', '🚀', '⭐', '💡', '🏷️', '📋', '📈', '🔔', '💬', '🔍']
const newGroupForm = ref({ titleKo: '', titleEn: '', code: '', icon: '⚙️' })

const isEditingGroup = ref(false)
const targetEditingGroup = ref(null)

const openAddGroupModal = (targetContext) => {
  modalTargetContext.value = targetContext
  isEditingGroup.value = false
  targetEditingGroup.value = null
  newGroupForm.value = { titleKo: '', titleEn: '', code: '', icon: '⚙️' }
  showAddGroupModalFlag.value = true
}

const openEditGroupModal = (group) => {
  isEditingGroup.value = true
  targetEditingGroup.value = group
  newGroupForm.value = {
    titleKo: group.title || '',
    titleEn: group.titleEn || group.title || '',
    code: group.code || '',
    icon: group.icon || '⚙️'
  }
  showAddGroupModalFlag.value = true
}

const fetchPermissionMasterGroups = async () => {
  try {
    const list = await $fetch('/api/permissions/groups', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    if (Array.isArray(list) && list.length > 0) {
      customPermissionGroups.value = list.map(g => ({
        id: g.id,
        code: g.code,
        title: g.titleKo,
        titleEn: g.titleEn || g.titleKo,
        icon: g.icon || '⚙️',
        color: g.color || '#3b82f6',
        chipClass: g.chipClass || '',
        permissions: (g.items || []).map(i => ({
          id: i.id,
          label: i.labelKo,
          labelEn: i.labelEn || i.labelKo,
          value: i.permValue
        }))
      }))
    }
  } catch (e) {
    console.error('Failed to fetch DB permission groups:', e)
  }
}

const deleteGroup = async (group) => {
  try {
    await $fetch(`/api/permissions/groups/${group.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    await fetchPermissionMasterGroups()
  } catch (e) {
    showCustomAlert('Failed to delete group: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const saveNewGroup = async () => {
  if (!newGroupForm.value.titleKo || !newGroupForm.value.code) return
  const codeClean = newGroupForm.value.code.toLowerCase().trim()
  const titleKoStr = newGroupForm.value.titleKo
  const titleEnStr = newGroupForm.value.titleEn || titleKoStr

  try {
    if (isEditingGroup.value && targetEditingGroup.value) {
      await $fetch(`/api/permissions/groups/${targetEditingGroup.value.id}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token.value}` },
        body: {
          titleKo: titleKoStr,
          titleEn: titleEnStr,
          icon: newGroupForm.value.icon || '⚙️'
        }
      })
    } else {
      await $fetch('/api/permissions/groups', {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: {
          code: codeClean,
          titleKo: titleKoStr,
          titleEn: titleEnStr,
          icon: newGroupForm.value.icon || '⚙️',
          color: '#06b6d4',
          chipClass: 'cyan'
        }
      })
    }
    showAddGroupModalFlag.value = false
    await fetchPermissionMasterGroups()
  } catch (e) {
    showCustomAlert('Failed to save group: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const showAddPermToGroupModalFlag = ref(false)
const targetGroupForPerm = ref(null)
const newPermToGroupForm = ref({ labelKo: '', labelEn: '', action: '' })

const isEditingPerm = ref(false)
const targetEditingPerm = ref(null)

const openAddPermToGroupModal = (targetContext, group) => {
  modalTargetContext.value = targetContext
  isEditingPerm.value = false
  targetEditingPerm.value = null
  targetGroupForPerm.value = group
  newPermToGroupForm.value = { labelKo: '', labelEn: '', action: '' }
  showAddPermToGroupModalFlag.value = true
}

const openEditPermModal = (group, perm) => {
  isEditingPerm.value = true
  targetGroupForPerm.value = group
  targetEditingPerm.value = perm
  const parts = (perm.value || '').split(':')
  const action = parts[1] || ''
  let rawLabelKo = perm.label || ''
  let rawLabelEn = perm.labelEn || perm.label || ''
  newPermToGroupForm.value = {
    labelKo: rawLabelKo.replace(` (${action})`, ''),
    labelEn: rawLabelEn.replace(` (${action})`, ''),
    action: action
  }
  showAddPermToGroupModalFlag.value = true
}

const deletePermFromGroup = async (group, perm) => {
  if (!perm || !perm.id) {
    const grp = customPermissionGroups.value.find(g => g.id === group.id)
    if (grp) {
      const idx = grp.permissions.findIndex(p => p.value === perm.value)
      if (idx > -1) grp.permissions.splice(idx, 1)
    }
    return
  }
  try {
    await $fetch(`/api/permissions/groups/items/${perm.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    await fetchPermissionMasterGroups()
  } catch (e) {
    showCustomAlert('Failed to delete permission item: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const saveNewPermToGroup = async () => {
  if (!targetGroupForPerm.value || !newPermToGroupForm.value.labelKo || !newPermToGroupForm.value.action) return
  const actClean = newPermToGroupForm.value.action.toLowerCase().trim()
  const permValue = `${targetGroupForPerm.value.code}:${actClean}`
  const labelKoStr = newPermToGroupForm.value.labelKo
  const labelEnStr = newPermToGroupForm.value.labelEn || labelKoStr

  try {
    await $fetch(`/api/permissions/groups/${targetGroupForPerm.value.id}/items`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        labelKo: `${labelKoStr} (${actClean})`,
        labelEn: `${labelEnStr} (${actClean})`,
        permValue: permValue
      }
    })
    showAddPermToGroupModalFlag.value = false
    await fetchPermissionMasterGroups()
  } catch (e) {
    showCustomAlert('Failed to add permission item: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const showCreateRoleModalFlag = ref(false)
const showEditRoleModalFlag = ref(false)
const newRoleForm = ref({ name: '', displayNameKo: '', displayNameEn: '', descriptionKo: '', descriptionEn: '', permissions: [] })
const editRoleForm = ref({ id: null, name: '', displayNameKo: '', displayNameEn: '', descriptionKo: '', descriptionEn: '', permissions: [], isSystemRole: false })

const showDeleteRoleModalFlag = ref(false)
const targetDeletingRole = ref(null)

const openCreateRoleModal = () => {
  newRoleForm.value = { name: '', displayNameKo: '', displayNameEn: '', descriptionKo: '', descriptionEn: '', permissions: ['domain:read', 'node:read'] }
  showCreateRoleModalFlag.value = true
}

const saveNewRole = async () => {
  if (!selectedOrg.value || !newRoleForm.value.name) return
  try {
    const permsSet = Array.isArray(newRoleForm.value.permissions) ? newRoleForm.value.permissions : []
    await $fetch('/api/roles', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        organizationId: selectedOrg.value.id,
        name: newRoleForm.value.name.toUpperCase().trim(),
        displayName: JSON.stringify({ ko: newRoleForm.value.displayNameKo || newRoleForm.value.name, en: newRoleForm.value.displayNameEn || newRoleForm.value.displayNameKo || newRoleForm.value.name }),
        description: JSON.stringify({ ko: newRoleForm.value.descriptionKo, en: newRoleForm.value.descriptionEn }),
        permissions: permsSet,
        isSystemRole: false
      }
    })
    showCreateRoleModalFlag.value = false
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert('새 조직 역할이 성공적으로 생성되었습니다.', getLabel('creation_success', '생성 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to create role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const openEditRoleModal = (role) => {
  let dNameKo = role.displayName || role.name
  let dNameEn = role.displayName || role.name
  let descKo = role.description || ''
  let descEn = role.description || ''
  try {
    const parsedDn = JSON.parse(role.displayName || '{}')
    if (parsedDn.ko || parsedDn.en) { dNameKo = parsedDn.ko || ''; dNameEn = parsedDn.en || '' }
  } catch (e) {}
  try {
    const parsedDesc = JSON.parse(role.description || '{}')
    if (parsedDesc.ko || parsedDesc.en) { descKo = parsedDesc.ko || ''; descEn = parsedDesc.en || '' }
  } catch (e) {}
  editRoleForm.value = {
    id: role.id,
    name: role.name,
    displayNameKo: dNameKo,
    displayNameEn: dNameEn,
    descriptionKo: descKo,
    descriptionEn: descEn,
    permissions: Array.from(role.permissions || []),
    isSystemRole: role.isSystemRole || false
  }
  showEditRoleModalFlag.value = true
}

const saveEditRole = async () => {
  if (!editRoleForm.value.id) return
  try {
    await $fetch(`/api/roles/${editRoleForm.value.id}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        displayName: JSON.stringify({ ko: editRoleForm.value.displayNameKo || editRoleForm.value.name, en: editRoleForm.value.displayNameEn || editRoleForm.value.displayNameKo || editRoleForm.value.name }),
        description: JSON.stringify({ ko: editRoleForm.value.descriptionKo, en: editRoleForm.value.descriptionEn }),
        permissions: editRoleForm.value.permissions
      }
    })
    showEditRoleModalFlag.value = false
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert('역할 정보가 성공적으로 수정되었습니다.', getLabel('update_success', '수정 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to update role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const openDeleteRoleConfirmModal = (role) => {
  targetDeletingRole.value = role
  showDeleteRoleModalFlag.value = true
}

const confirmDeleteRole = async () => {
  if (!targetDeletingRole.value) return
  try {
    await $fetch(`/api/roles/${targetDeletingRole.value.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    showDeleteRoleModalFlag.value = false
    await loadOrgDetails(selectedOrg.value.id)
    showCustomAlert('역할이 성공적으로 삭제되었습니다.', getLabel('delete_success', '삭제 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to delete role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

// -- Role Template Export & Import --
const fileInput = ref(null)

const exportRoles = async () => {
  if (!selectedOrg.value) return
  try {
    const rolesData = await roleStore.exportRolesForOrg(selectedOrg.value.id)
    const jsonStr = JSON.stringify(rolesData, null, 2)
    const blob = new Blob([jsonStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `roles_backup_${selectedOrg.value.name}_${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    initToast({ message: t('export_roles_success'), color: 'success' })
  } catch (e) {
    initToast({ message: t('export_roles_fail'), color: 'danger' })
  }
}

const openImportRolesModal = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

const handleImportFile = async (e) => {
  const target = e.target
  if (!target.files || target.files.length === 0) return
  
  const file = target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = async (ev) => {
    try {
      const content = ev.target?.result
      const parsedData = JSON.parse(content)
      
      const confirmed = window.confirm(t('import_roles_confirm'))
      if (!confirmed) {
        if (fileInput.value) fileInput.value.value = ''
        return
      }

      const success = await roleStore.importRolesForOrg(selectedOrg.value.id, parsedData)
      if (success) {
        initToast({ message: t('import_roles_success'), color: 'success' })
        await loadOrgDetails(selectedOrg.value.id)
      } else {
        initToast({ message: t('import_roles_fail'), color: 'danger' })
      }
    } catch (err) {
      console.error(err)
      initToast({ message: t('import_roles_fail'), color: 'danger' })
    } finally {
      if (fileInput.value) fileInput.value.value = ''
    }
  }
  reader.readAsText(file)
}

onMounted(() => {
  isMounted.value = true
  fetchOrganizations()
  fetchPermissionMasterGroups()
})
</script>

<style scoped>
.hoverable-icon-box:hover {
  background: var(--va-primary) !important;
  color: white !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}
.hoverable-icon-box:hover .va-icon {
  color: white !important;
}

.readonly-sys-code :deep(.va-input-wrapper),
.readonly-sys-code :deep(input) {
  background-color: var(--va-background-element) !important;
  color: var(--va-text-primary) !important;
  opacity: 0.85;
}
</style>
