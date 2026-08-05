<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="manage_accounts" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="RBAC" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('user_management_desc') || '사용자 계정 정보, 소속 조직/부서 및 시스템 역할 및 도메인 접근 권한을 관리합니다.' }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button preset="solid" color="success" icon="person_add" size="small" @click="showCreateUserModal = true" style="font-weight: 700;">
          {{ $t('create_user') || '사용자 등록' }}
        </va-button>
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="fetchUsers">
          {{ $t('refresh') || '새로고침' }}
        </va-button>
      </div>
    </div>

    <div style="display: flex; gap: 1.5rem; align-items: flex-start; flex-wrap: wrap;">
      <!-- Left Column: User Directory List -->
      <va-card style="flex: 1; min-width: 340px; display: flex; flex-direction: column;">
        <va-card-title style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); padding-bottom: 0.85rem;">
          <div style="display: flex; align-items: center; gap: 0.5rem; color: var(--va-text-primary); font-weight: 700; font-size: 1.05rem;">
            <va-icon name="people" color="primary" />
            <span>{{ $t('user_management') }}</span>
            <va-badge :text="String(users.length)" color="primary" size="small" />
          </div>
          <va-input v-model="searchQuery" :placeholder="$t('search') || 'Search'" @keydown="onSearchKeydown" clearable @clear="fetchUsers" style="max-width: 140px;" dense />
        </va-card-title>
        <va-card-content style="flex: 1; display: flex; flex-direction: column; padding: 0.75rem;">
          <va-list style="flex: 1; display: flex; flex-direction: column; gap: 0.35rem;">
            <va-list-item
              v-for="user in users"
              :key="user.id"
              @click="selectUser(user)"
              style="cursor: pointer; padding: 0.85rem; border-radius: 10px; transition: all 0.2s ease; display: flex; align-items: center;"
              :style="{
                backgroundColor: selectedUser?.id === user.id ? 'rgba(37, 99, 235, 0.12)' : 'var(--va-background-element)',
                borderLeft: selectedUser?.id === user.id ? '4px solid var(--va-primary)' : '4px solid transparent',
                borderTop: '1px solid var(--va-background-border)',
                borderRight: '1px solid var(--va-background-border)',
                borderBottom: '1px solid var(--va-background-border)'
              }"
            >
              <va-list-item-section avatar style="min-width: 44px;">
                <va-avatar color="primary" size="medium" style="font-weight: 700;">
                  {{ user.username.charAt(0).toUpperCase() }}
                </va-avatar>
              </va-list-item-section>
              <va-list-item-section style="overflow: hidden;">
                <div style="font-weight: 800; font-size: 1rem; color: var(--va-text-primary); margin-bottom: 0.25rem;">
                  {{ user.username }}
                </div>
                <div style="display: flex; gap: 0.35rem; align-items: center; flex-wrap: wrap;">
                  <RoleBadge :value="user.role" />
                  <va-badge :text="getOrgName(user.organizationId)" color="info" outline size="small" />
                  <va-badge v-if="getDeptName(user.departmentId)" :text="getDeptName(user.departmentId)" color="success" outline size="small" />
                  <va-badge v-if="user.mustChangePassword" text="임시비밀번호" color="warning" size="small" />
                </div>
              </va-list-item-section>
            </va-list-item>
          </va-list>
          <div style="margin-top: 1rem; display: flex; justify-content: center;" v-if="totalPages > 1">
            <va-pagination v-model="currentPage" :pages="totalPages" @update:modelValue="fetchUsers" :visible-pages="5" size="small" />
          </div>
        </va-card-content>
      </va-card>

      <!-- Right Column: User Details, Roles, Permissions & History -->
      <va-card v-if="selectedUser" style="flex: 2; min-width: 450px;">
        <va-card-title style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); padding-bottom: 0.85rem;">
          <div style="display: flex; align-items: center; gap: 0.75rem; width: 100%;">
            <va-avatar color="primary" size="large" style="font-size: 1.25rem; font-weight: 800;">
              {{ selectedUser.username.charAt(0).toUpperCase() }}
            </va-avatar>
            <div style="width: 100%;">
              <div style="font-size: 1.2rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem; width: 100%;">
                {{ selectedUser.username }}
                <va-badge text="Active User" color="success" size="small" />
                <div style="margin-left: auto; display: flex; gap: 0.5rem;">
                  <va-button v-if="selectedUser.mustChangePassword" size="small" color="warning" outline icon="key" @click="viewTempPassword(selectedUser.id)">
                    {{ $t('view_temp_password') || '임시 비밀번호 확인' }}
                  </va-button>
                  <va-button size="small" color="danger" outline icon="delete" @click="confirmDeleteUser(selectedUser)">
                    {{ $t('delete') || '삭제' }}
                  </va-button>
                </div>
              </div>
              <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-top: 0.15rem;">
                {{ getOrgName(selectedUser.organizationId) }} <span v-if="getDeptName(selectedUser.departmentId)">• {{ getDeptName(selectedUser.departmentId) }}</span>
              </div>
            </div>
          </div>
        </va-card-title>

        <va-card-content style="display: flex; flex-direction: column; gap: 1.5rem; padding-top: 1.25rem;">
          
          <!-- User System Role Setting -->
          <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 12px; padding: 1.25rem; box-shadow: 0 2px 6px rgba(0,0,0,0.02);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.85rem;">
              <h3 style="font-weight: 800; margin: 0; color: var(--va-text-primary); font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
                <va-icon name="manage_accounts" color="primary" />
                <span>{{ $t('user_role') || '사용자 시스템 권한 역할' }}</span>
              </h3>

              <va-button
                v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')"
                color="primary"
                icon="save"
                size="small"
                @click="updateUserRoleOnly"
                style="font-weight: 700;"
              >
                {{ $t('save_role') || '역할 저장' }}
              </va-button>
            </div>
            
            <div style="display: flex; gap: 0.5rem; margin-bottom: 0.85rem; align-items: center; font-size: 0.88rem; color: var(--va-text-secondary); flex-wrap: wrap;">
              <span>{{ $t('current_affiliation') }}</span>
              <va-badge :text="getOrgName(selectedUser.organizationId)" color="info" outline size="small" />
              <va-badge v-if="getDeptName(selectedUser.departmentId)" :text="getDeptName(selectedUser.departmentId)" color="success" outline size="small" />
              <span v-else style="font-style: italic; color: var(--va-text-secondary);">{{ $t('no_dept_assigned_tip') }}</span>
            </div>

            <UserRoleSelect
              v-model="selectedUserRoles"
              multiple
              :org-id="selectedUser?.organizationId"
              :label="getLabel('user_roles', '사용자 시스템 역할 (다중 선택 가능)')"
              style="width: 100%;"
            />
          </div>

          <!-- Domain Permissions Section -->
          <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 12px; padding: 1.25rem; box-shadow: 0 2px 6px rgba(0,0,0,0.02);">
            <h3 style="font-weight: 800; margin: 0 0 0.75rem 0; color: var(--va-text-primary); font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="verified_user" color="primary" />
              <span>{{ $t('granted_domains') }}</span>
            </h3>
            <div v-if="userPermissions.length === 0" style="padding: 1.25rem; text-align: center; background: var(--va-background-primary); border: 1px dashed var(--va-background-border); border-radius: 8px; color: var(--va-text-secondary); font-size: 0.88rem;">
              <va-icon name="do_not_disturb_on" color="secondary" size="medium" style="margin-bottom: 0.35rem; display: block;" />
              <span>{{ $t('no_specific_domain_permissions') }}</span>
            </div>
            <div v-else style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
              <va-chip v-for="perm in userPermissions" :key="perm.id" color="primary" style="font-weight: 700; font-size: 0.85rem; padding: 6px 12px;">
                {{ getDomainName(perm.domain.name) }}
                <va-icon v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" name="close" size="small" style="margin-left: 0.5rem; cursor: pointer;" @click="revokePermission(perm.domain.id)" />
              </va-chip>
            </div>
          </div>
          
          <!-- Grant New Permission Section -->
          <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 12px; padding: 1.25rem; box-shadow: 0 2px 6px rgba(0,0,0,0.02);">
            <h3 style="font-weight: 800; margin: 0 0 0.75rem 0; color: var(--va-text-primary); font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="add_moderator" color="primary" />
              <span>{{ $t('grant_new_permission') }}</span>
            </h3>
            <div style="display: flex; gap: 0.75rem; align-items: flex-end;">
              <va-select
                v-model="selectedDomainsToGrant"
                multiple
                :options="availableDomains"
                value-by="id"
                text-by="label"
                :placeholder="$t('select_a_domain') || '도메인을 선택하세요 (다중 선택 가능)'"
                style="flex: 1;"
              />
              <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" color="primary" icon="add" @click="grantPermissions" :disabled="!selectedDomainsToGrant || selectedDomainsToGrant.length === 0" style="font-weight: 700;">
                {{ $t('grant') || '권한 부여' }}
              </va-button>
            </div>
          </div>

          <!-- Organization Change History -->
          <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 12px; padding: 1.25rem; box-shadow: 0 2px 6px rgba(0,0,0,0.02);">
            <h3 style="font-weight: 800; margin: 0 0 0.75rem 0; color: var(--va-text-primary); font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="history" color="primary" />
              <span>{{ $t('org_history_title') }}</span>
            </h3>
            <div v-if="!userOrgHistory || userOrgHistory.length === 0" style="padding: 1.25rem; text-align: center; background: var(--va-background-primary); border: 1px dashed var(--va-background-border); border-radius: 8px; color: var(--va-text-secondary); font-size: 0.88rem;">
              <va-icon name="history_toggle_off" color="secondary" size="medium" style="margin-bottom: 0.35rem; display: block;" />
              <span>{{ $t('no_org_history') }}</span>
            </div>
            <div v-else style="display: flex; flex-direction: column; gap: 0.65rem; max-height: 240px; overflow-y: auto;">
              <div
                v-for="h in userOrgHistory"
                :key="h.id"
                style="padding: 0.85rem 1rem; background: var(--va-background-primary); border-radius: 10px; border: 1px solid var(--va-background-border);"
              >
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem;">
                  <span style="font-weight: 700; color: var(--va-text-primary); font-size: 0.88rem; display: flex; align-items: center; gap: 0.35rem;">
                    <va-icon name="schedule" size="small" color="primary" />
                    {{ formatDate(h.changedAt) }}
                  </span>
                  <va-badge size="small" color="info" :text="h.changedBy || 'SYSTEM'" />
                </div>
                <div style="display: flex; align-items: center; gap: 0.5rem; font-weight: 800; font-size: 0.95rem;">
                  <span style="color: var(--va-text-secondary);">{{ getI18nText(h.prevDepartmentName || h.prevOrganizationName) || '-' }}</span>
                  <va-icon name="east" size="small" color="primary" />
                  <span style="color: var(--va-primary);">{{ getI18nText(h.newDepartmentName || h.newOrganizationName) || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- Pending Access Requests Bottom Card -->
    <va-card style="margin-top: 1rem;">
      <va-card-title style="border-bottom: 1px solid var(--va-background-border); padding-bottom: 0.85rem;">
        <div style="display: flex; align-items: center; gap: 0.5rem; color: var(--va-text-primary); font-weight: 800; font-size: 1.1rem;">
          <va-icon name="pending_actions" color="warning" />
          <span>{{ $t('pending_domain_access_requests') }}</span>
          <va-badge v-if="pendingRequests.length > 0" :text="String(pendingRequests.length)" color="warning" size="small" />
        </div>
      </va-card-title>
      <va-card-content style="padding-top: 1.25rem;">
        <div v-if="pendingRequests.length === 0" style="padding: 2rem; text-align: center; background: var(--va-background-element); border: 1px dashed var(--va-background-border); border-radius: 12px; color: var(--va-text-secondary); font-size: 0.9rem;">
          <va-icon name="mark_email_read" color="secondary" size="large" style="margin-bottom: 0.5rem; display: block;" />
          <span>{{ $t('no_pending_requests') }}</span>
        </div>
        <div v-else style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;">
          <va-card v-for="group in groupedPendingRequests" :key="group.userId" outlined style="background-color: var(--va-background-primary); border-radius: 12px; border: 1px solid var(--va-background-border);" class="hoverable-card">
            <va-card-content style="display: flex; flex-direction: column; height: 100%; padding: 1.25rem;">
              <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem;">
                <va-avatar size="large" color="primary" style="font-size: 1.25rem; font-weight: bold; width: 44px; height: 44px;">
                  {{ group.username.charAt(0).toUpperCase() }}
                </va-avatar>
                <div style="flex: 1; overflow: hidden;">
                  <div style="font-weight: 800; font-size: 1.1rem; color: var(--va-text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                    {{ group.username }}
                  </div>
                  <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-top: 0.2rem;">
                    {{ group.formattedDate }}
                  </div>
                </div>
              </div>

              <div style="flex: 1; margin-bottom: 1.25rem;">
                <div style="font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary); margin-bottom: 0.5rem;">
                  {{ $t('requested_domains') }} ({{ group.domains.length }})
                </div>
                <div style="display: flex; flex-wrap: wrap; gap: 0.4rem; max-height: 100px; overflow-y: auto; padding: 0.2rem;">
                  <va-chip v-for="(dom, idx) in group.domains" :key="idx" size="small" color="primary" outline style="font-weight: 700;">
                    {{ dom }}
                  </va-chip>
                </div>
              </div>

              <div style="display: flex; gap: 0.75rem; justify-content: flex-end; border-top: 1px solid var(--va-background-border); padding-top: 1rem;">
                <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" color="danger" preset="outline" size="small" @click="handleBatchReject(group.reqIds)" style="font-weight: 700;">
                  {{ $t('reject') }}
                </va-button>
                <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" color="success" size="small" @click="handleBatchApprove(group.reqIds)" style="font-weight: 700;">
                  {{ $t('approve') }}
                </va-button>
              </div>
            </va-card-content>
          </va-card>
        </div>
      </va-card-content>
    </va-card>



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

    <!-- Create User Modal -->
    <va-modal v-model="showCreateUserModal" :title="$t('create_user') || '사용자 등록'" hide-default-actions>
      <div style="padding: 1rem; min-width: 400px; display: flex; flex-direction: column; gap: 1.25rem;">
        <va-input v-model="newUser.username" :label="$t('label_username') || '아이디 (Username)'" outline @update:modelValue="isUsernameChecked = false" :success="isUsernameChecked && checkedUsername === newUser.username">
          <template #appendInner>
            <va-button size="small" color="primary" preset="secondary" @click="checkUsernameDuplicate" :loading="isCheckingUsername" :disabled="!newUser.username || (isUsernameChecked && checkedUsername === newUser.username)" style="white-space: nowrap;">
              {{ $t('check_duplicate') || '중복 확인' }}
            </va-button>
          </template>
        </va-input>
        <UserRoleSelect v-model="newUser.role" :label="$t('user_role') || '시스템 역할 (단일/다중)'" />
        <va-select v-model="newUser.organizationId" :options="organizations" value-by="id" :text-by="o => getI18nText(o.displayName) || o.name" :label="$t('organization') || '소속 조직'" clearable outline />
        <va-select v-model="newUser.departmentId" :options="departmentsForNewUser" value-by="id" text-by="name" :label="$t('department') || '소속 부서'" clearable outline :disabled="!newUser.organizationId" />
        
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="showCreateUserModal = false">{{ $t('cancel') || '취소' }}</va-button>
          <va-button color="success" @click="createUser" :loading="isCreatingUser">{{ $t('create') || '등록' }}</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Temp Password Alert Modal -->
    <va-modal v-model="showTempPasswordModal" :title="$t('user_created') || '사용자 등록 완료'" hide-default-actions :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="padding: 1.5rem; text-align: center;">
        <va-icon name="check_circle" color="success" size="3.5rem" style="margin-bottom: 1rem;" />
        <h3 style="margin-bottom: 1rem; font-weight: 800; font-size: 1.3rem;">{{ $t('temp_password_issued') || '임시 비밀번호가 발급되었습니다.' }}</h3>
        <p style="color: var(--va-text-secondary); margin-bottom: 1.5rem; font-size: 0.9rem;">
          {{ $t('temp_password_warning') || '이 비밀번호는 다시 표시되지 않으므로 반드시 사용자에게 안전하게 전달해 주세요.' }}
        </p>
        <div style="background: var(--va-background-secondary); padding: 1.25rem; border-radius: 12px; font-size: 1.8rem; font-weight: 800; letter-spacing: 4px; margin-bottom: 1.5rem; border: 1px dashed var(--va-background-border); color: var(--va-primary);">
          {{ createdTempPassword }}
        </div>
        <va-button color="primary" @click="showTempPasswordModal = false; createdTempPassword = '';" size="large">{{ $t('confirm') || '확인했습니다' }}</va-button>
      </div>
    </va-modal>

    <!-- View Temp Password Modal -->
    <va-modal v-model="showViewTempPasswordModal" :title="$t('temp_password') || '임시 비밀번호'" hide-default-actions>
      <div style="padding: 1.5rem; text-align: center;">
        <va-icon name="key" color="warning" size="3.5rem" style="margin-bottom: 1rem;" />
        <h3 style="margin-bottom: 1rem; font-weight: 800; font-size: 1.3rem;">{{ $t('temp_password_check') || '임시 비밀번호 확인' }}</h3>
        <p style="color: var(--va-text-secondary); margin-bottom: 1.5rem; font-size: 0.9rem;">
          {{ $t('temp_password_warning') || '이 비밀번호는 사용자가 로그인하여 변경하기 전까지만 유효합니다.' }}
        </p>
        <div style="background: var(--va-background-secondary); padding: 1.25rem; border-radius: 12px; font-size: 1.8rem; font-weight: 800; letter-spacing: 4px; margin-bottom: 1.5rem; border: 1px dashed var(--va-background-border); color: var(--va-primary);">
          {{ fetchedTempPassword }}
        </div>
        <va-button color="primary" @click="showViewTempPasswordModal = false; fetchedTempPassword = '';" size="large">{{ $t('close') || '닫기' }}</va-button>
      </div>
    </va-modal>
  </div>
</template>

<script setup>
import { usePermission } from '~/composables/usePermission'
import { usePageTitle } from '~/composables/usePageTitle'

const { pageTitle } = usePageTitle('user_management', '사용자 및 권한 관리')

const { t, locale } = useI18n()
const { hasPermission } = usePermission()

const getLabel = (key, fallback) => {
  const res = t(key)
  return (!res || res === key) ? fallback : res
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

const getDomainName = (nameObj) => {
  if (!nameObj) return 'Unknown'
  const lang = (locale && locale.value) ? locale.value : 'ko'
  if (typeof nameObj === 'string') {
    try {
      const parsed = JSON.parse(nameObj)
      return parsed[lang] || parsed.ko || parsed.en || 'Unknown'
    } catch {
      return nameObj
    }
  }
  return nameObj[lang] || nameObj.ko || nameObj.en || 'Unknown'
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
const users = ref([])
const organizations = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')

const fetchUsers = async () => {
  try {
    const res = await $fetch('/api/permissions/users', {
      headers: { Authorization: `Bearer ${token.value}` },
      query: {
        page: currentPage.value - 1,
        size: 100,
        search: searchQuery.value || ''
      }
    })
    users.value = res.content || []
    totalPages.value = res.totalPages || 1
  } catch (e) {
    console.error(e)
  }
}

const onSearchKeydown = (e) => {
  if (e && e.key === 'Enter') {
    fetchUsers()
  }
}
const selectedUser = ref(null)
const selectedUserRoles = ref([])
const selectedUserOrgId = ref(null)
const selectedUserDeptId = ref(null)
const departments = ref([])

const userPermissions = ref([])
const allDomains = ref([])
const selectedDomainsToGrant = ref([])
const pendingRequests = ref([])

const parseDate = (dateString) => {
  if (!dateString) return null
  let str = String(dateString).trim()
  if (str.includes(' ') && !str.includes('T')) {
    str = str.replace(' ', 'T')
  }
  if (!str.endsWith('Z') && !str.includes('+') && !str.includes('-')) {
    const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
    let serverOffset = '+09:00'
    if (tz === 'UTC' || tz === 'GMT') serverOffset = 'Z'
    else if (tz === 'America/New_York') serverOffset = '-05:00'
    else if (tz === 'Europe/London') serverOffset = '+00:00'
    str += serverOffset
  }
  const d = new Date(str)
  return isNaN(d.getTime()) ? new Date(dateString) : d
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const formatted = date.toLocaleString(undefined, { timeZone: tz })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const groupedPendingRequests = computed(() => {
  const groups = {}
  pendingRequests.value.forEach(req => {
    const key = req.user.id
    if (!groups[key]) {
      groups[key] = {
        userId: req.user.id,
        username: req.user.username,
        domains: [],
        reqIds: [],
        createdAt: req.createdAt
      }
    }
    groups[key].domains.push(getDomainName(req.domain.name))
    groups[key].reqIds.push(req.id)
    if (new Date(req.createdAt) < new Date(groups[key].createdAt)) {
      groups[key].createdAt = req.createdAt
    }
  })
  return Object.values(groups).map(g => ({
    ...g,
    formattedDate: formatDate(g.createdAt)
  }))
})

const allDepartmentsMap = ref({})

const fetchAllDepartments = async () => {
  try {
    const orgs = organizations.value
    for (const org of orgs) {
      const depts = await $fetch(`/api/organizations/${org.id}/departments`, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      if (depts) {
        depts.forEach(d => {
          allDepartmentsMap.value[d.id] = d.name
        })
      }
    }
  } catch (e) {
    console.error('Failed to fetch all departments:', e)
  }
}

const fetchOrganizations = async () => {
  try {
    const res = await $fetch('/api/organizations', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    organizations.value = res || []
    await fetchAllDepartments()
  } catch (e) {
    console.error('Failed to fetch orgs:', e)
  }
}

const getOrgName = (orgId) => {
  if (!orgId) return getLabel('unassigned', '미지정')
  if (!organizations.value || organizations.value.length === 0) return getLabel('unassigned', '미지정')
  const found = organizations.value.find(o => o.id === orgId)
  return found ? (getI18nText(found.displayName) || found.name) : getLabel('unassigned', '미지정')
}

const getDeptName = (deptId) => {
  if (!deptId) return null
  const raw = allDepartmentsMap.value[deptId]
  return raw ? getI18nText(raw) : null
}

const getUserRolesArray = (role) => {
  if (!role) return ['USER']
  if (Array.isArray(role)) return role
  return String(role).split(',').map(r => r.trim()).filter(Boolean)
}

const selectUser = async (user) => {
  if (user) {
    selectedUser.value = user
    selectedUserRoles.value = getUserRolesArray(user.role)
    selectedUserOrgId.value = user.organizationId
    selectedUserDeptId.value = user.departmentId
    await fetchAllDepartments()
    await loadUserPermissions(user.id)
    await loadUserOrgHistory(user.id)
  }
}

const userOrgHistory = ref([])

const loadUserOrgHistory = async (userId) => {
  try {
    userOrgHistory.value = await $fetch(`/api/users/${userId}/org-history`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
  } catch (e) {
    userOrgHistory.value = []
  }
}

const updateUserRoleOnly = async () => {
  if (!selectedUser.value) return
  const roleStr = Array.isArray(selectedUserRoles.value) ? selectedUserRoles.value.join(',') : (selectedUserRoles.value || 'USER')
  try {
    await $fetch(`/api/permissions/users/${selectedUser.value.id}/tenant-info`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        role: roleStr,
        organizationId: selectedUser.value.organizationId,
        departmentId: selectedUser.value.departmentId
      }
    })
    selectedUser.value.role = roleStr
    await fetchUsers()
    showCustomAlert(
      getLabel('role_updated_success', '사용자 역할 권한이 성공적으로 변경되었습니다.'),
      getLabel('update_success', '수정 완료'),
      getLabel('notification', '알림'),
      'success'
    )
  } catch (e) {
    showCustomAlert('Failed to update user role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const loadUserPermissions = async (userId) => {
  userPermissions.value = await $fetch(`/api/permissions/users/${userId}/domains`, {
    headers: { Authorization: `Bearer ${token.value}` }
  })
}



const availableDomains = computed(() => {
  const grantedIds = userPermissions.value.map(p => p.domain.id)
  return allDomains.value
    .filter(d => !grantedIds.includes(d.id))
    .map(d => ({ id: d.id, label: getDomainName(d.name) }))
})

const grantPermissions = async () => {
  if (!selectedUser.value || !selectedDomainsToGrant.value || selectedDomainsToGrant.value.length === 0) return
  try {
    await Promise.all(
      selectedDomainsToGrant.value.map(domainId =>
        $fetch(`/api/permissions/users/${selectedUser.value.id}/domains/${domainId}`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token.value}` }
        })
      )
    )
    selectedDomainsToGrant.value = []
    await loadUserPermissions(selectedUser.value.id)
    showCustomAlert(getLabel('grant_permission_success', '선택한 도메인 권한이 성공적으로 부여되었습니다.'), getLabel('update_success', '부여 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to grant permissions: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const revokePermission = async (domainId) => {
  if (!selectedUser.value) return
  await $fetch(`/api/permissions/users/${selectedUser.value.id}/domains/${domainId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token.value}` }
  })
  await loadUserPermissions(selectedUser.value.id)
}

const handleBatchApprove = async (reqIds) => {
  try {
    await Promise.all(reqIds.map(id => 
      $fetch(`/api/permissions/requests/${id}/approve`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` }
      })
    ))
    await fetchPendingRequests()
    if (selectedUser.value) {
      await loadUserPermissions(selectedUser.value.id)
    }
  } catch (e) {
    console.error(e)
  }
}

const handleBatchReject = async (reqIds) => {
  try {
    await Promise.all(reqIds.map(id => 
      $fetch(`/api/permissions/requests/${id}/reject`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
      })
    ))
    await fetchPendingRequests()
  } catch (e) {
    console.error(e)
  }
}

const fetchDomains = async () => {
  try {
    const res = await $fetch('/api/domains', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    allDomains.value = res || []
  } catch (e) {
    console.error('Failed to fetch domains:', e)
  }
}

const fetchPendingRequests = async () => {
  try {
    const res = await $fetch('/api/permissions/requests/pending', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    pendingRequests.value = res || []
  } catch (e) {
    console.error('Failed to fetch pending requests:', e)
  }
}

const showCreateUserModal = ref(false)
const showTempPasswordModal = ref(false)
const isCreatingUser = ref(false)
const createdTempPassword = ref('')
const newUser = ref({ username: '', role: '', organizationId: null, departmentId: null })
const isUsernameChecked = ref(false)
const isCheckingUsername = ref(false)
const checkedUsername = ref('')
const showViewTempPasswordModal = ref(false)
const fetchedTempPassword = ref('')

const viewTempPassword = async (userId) => {
  try {
    const res = await $fetch(`/api/users/${userId}/temp-password`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    if (res && res.tempPassword) {
      fetchedTempPassword.value = res.tempPassword
      showViewTempPasswordModal.value = true
    } else {
      showCustomAlert('임시 비밀번호를 조회할 수 없습니다.', '오류', '알림', 'error')
    }
  } catch (e) {
    showCustomAlert('조회 실패: 해당 사용자의 임시 비밀번호가 존재하지 않거나 권한이 없습니다.', '오류', '알림', 'error')
  }
}

const confirmDeleteUser = async (user) => {
  if (confirm(`정말로 사용자 '${user.username}'를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
    try {
      await $fetch(`/api/users/${user.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token.value}` }
      })
      showCustomAlert('사용자가 성공적으로 삭제되었습니다.', '삭제 완료', '알림', 'success')
      selectedUser.value = null
      fetchUsers()
    } catch (e) {
      if (e.response?.status === 409) {
        showCustomAlert('해당 사용자가 생성한 레코드나 결재 이력 등 연결된 데이터가 존재하여 삭제할 수 없습니다.', '삭제 불가', '알림', 'error')
      } else {
        showCustomAlert('사용자 삭제 중 오류가 발생했습니다: ' + (e.response?._data || e.message), '오류', '알림', 'error')
      }
    }
  }
}

const checkUsernameDuplicate = async () => {
  if (!newUser.value.username) return
  isCheckingUsername.value = true
  try {
    const res = await $fetch(`/api/auth/check-username?username=${encodeURIComponent(newUser.value.username)}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    if (res && res.available) {
      isUsernameChecked.value = true
      checkedUsername.value = newUser.value.username
      showCustomAlert('사용 가능한 아이디입니다.', '확인 완료', '알림', 'success')
    } else {
      isUsernameChecked.value = false
      checkedUsername.value = ''
      showCustomAlert('이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.', '중복 확인', '알림', 'warning')
    }
  } catch (e) {
    showCustomAlert('중복 확인 중 오류가 발생했습니다.', '오류', '알림', 'error')
  } finally {
    isCheckingUsername.value = false
  }
}

const departmentsForNewUser = computed(() => {
  if (!newUser.value.organizationId) return []
  const depts = []
  Object.keys(allDepartmentsMap.value).forEach(id => {
    // We don't have orgId in allDepartmentsMap easily accessible here. 
    // It's better to fetch dynamically or just show all if we don't have org link.
    // Wait, allDepartmentsMap is just a flat map.
    depts.push({ id, name: getI18nText(allDepartmentsMap.value[id]) })
  })
  return depts
})

const createUser = async () => {
  if (!newUser.value.username) {
    showCustomAlert('아이디를 입력해주세요.', '입력 오류', '알림', 'warning')
    return
  }
  if (!isUsernameChecked.value || checkedUsername.value !== newUser.value.username) {
    showCustomAlert('아이디 중복 확인을 먼저 해주세요.', '입력 오류', '알림', 'warning')
    return
  }
  isCreatingUser.value = true
  try {
    const roleStr = Array.isArray(newUser.value.role) ? newUser.value.role.join(',') : (newUser.value.role || 'ROLE_USER')
    const res = await $fetch('/api/users', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        username: newUser.value.username,
        role: roleStr,
        organizationId: newUser.value.organizationId,
        departmentId: newUser.value.departmentId
      }
    })
    createdTempPassword.value = res.tempPassword
    showCreateUserModal.value = false
    showTempPasswordModal.value = true
    newUser.value = { username: '', role: '', organizationId: null, departmentId: null }
    isUsernameChecked.value = false
    checkedUsername.value = ''
    await fetchUsers()
  } catch (e) {
    showCustomAlert('사용자 등록 실패: ' + (e.response?._data || e.message), '오류', '알림', 'error')
  } finally {
    isCreatingUser.value = false
  }
}

onMounted(() => {
  fetchOrganizations()
  fetchUsers()
  fetchDomains()
  fetchPendingRequests()
})
</script>
