<template>
  <div>
    <!-- Data Changes Display (Collapsible Accordion with Silky Smooth CSS Grid Animation) -->
    <div class="accordion-card" style="background-color: var(--va-background-secondary); border-left: 4px solid var(--va-primary); border-radius: 6px; padding: 0.85rem 1rem; margin-bottom: 1.5rem; transition: all 0.25s ease;">
      <div 
        class="accordion-header"
        @click="isRequestedDataExpanded = !isRequestedDataExpanded" 
        style="display: flex; justify-content: space-between; align-items: center; cursor: pointer; user-select: none; border-radius: 4px; padding: 0.25rem 0.4rem; margin: -0.25rem -0.4rem; transition: background-color 0.2s ease;"
      >
        <h4 style="margin: 0; font-size: 0.95rem; color: var(--va-text-primary); font-weight: bold; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon 
            name="chevron_right" 
            size="small" 
            color="primary" 
            :style="{ transform: isRequestedDataExpanded ? 'rotate(90deg)' : 'rotate(0deg)', transition: 'transform 0.35s cubic-bezier(0.16, 1, 0.3, 1)' }" 
          />
          {{ t('requestedData') }}
        </h4>
        <va-chip size="small" color="primary" preset="outline" style="font-weight: 600; transition: transform 0.2s ease;">
          {{ isRequestedDataExpanded ? (t('collapse') || '접기') : (t('expand') || '펼치기') }}
        </va-chip>
      </div>

      <div 
        class="accordion-wrapper" 
        :class="{ 'is-expanded': isRequestedDataExpanded }"
      >
        <div class="accordion-inner">
          <div style="padding-top: 1rem;">
            <template v-if="getParsedChanges(request?.changes)">
          <div class="custom-scrollbar">
            <div v-for="sector in getGroupedChangesList(request.changes, request.targetType)" :key="sector.key" style="margin-bottom: 1rem;">
              <div style="font-weight: bold; padding: 0.5rem; background: var(--va-background-secondary); border-radius: 4px; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.5rem;">
                <va-icon name="folder" size="small" /> {{ sector.label }}
              </div>
              
              <div style="width: 100%; margin-top: 0.5rem; display: flex; flex-direction: column; gap: 0.5rem;">
                <div v-for="group in sector.groups" :key="group.key" style="border: 1px solid var(--va-background-border); border-radius: 4px; overflow: hidden; background: var(--va-background-element);">
                  <div style="background: var(--va-background-secondary); padding: 0.75rem 1rem; font-weight: bold; font-size: 0.95rem; color: var(--va-text-primary); border-bottom: 1px solid var(--va-background-border);">
                    {{ group.label }}
                  </div>
                  <div style="display: grid; grid-template-columns: repeat(12, 1fr); gap: 1rem; padding: 0.75rem;">
                    <template v-for="f in group.fields" :key="f.key">
                      <div v-if="request.targetType !== 'RECORD_UPDATE' || (f.val.isChanged || (f.val.before !== f.val.after))" :style="{ gridColumn: 'span ' + (f.gridWidth || 12), border: '1px solid var(--va-background-border)', borderRadius: '8px', overflow: 'hidden', background: 'var(--va-background-element)', boxShadow: 'var(--va-box-shadow)' }">
                        <div style="background: var(--va-background-secondary); padding: 0.75rem 1rem; border-bottom: 1px solid var(--va-background-border); font-weight: 600; font-size: 0.85rem; color: var(--va-text-primary); display: flex; justify-content: space-between; align-items: center;">
                          {{ f.label }}
                          <va-badge v-if="request.targetType === 'RECORD_UPDATE' && f.val.isChanged" color="warning" size="small">{{ t('modified') }}</va-badge>
                        </div>
                        <div style="padding: 0;">
                          <template v-if="request.targetType === 'RECORD_UPDATE'">
                            <div v-if="f.val.isChanged" style="display: flex; flex-direction: column;">
                              <div style="background-color: rgba(229, 57, 53, 0.1); border-bottom: 1px solid rgba(229, 57, 53, 0.2); padding: 0.75rem 1rem; font-size: 0.85rem; display: flex; align-items: flex-start; gap: 0.5rem;">
                                <va-icon name="remove_circle_outline" color="danger" size="small" style="margin-top: 2px;" />
                                <div style="color: var(--va-danger); word-break: break-all; width: 100%;">
                                  <template v-if="f.type === 'FILE' && getFilesList(f.val.before).length > 0">
                                    <div v-for="(fileUrl, idx) in getFilesList(f.val.before)" :key="idx" style="margin-bottom: 4px;">
                                      <a href="#" @click.prevent="downloadFileWithAuth(fileUrl, getFileName(fileUrl))" style="color: var(--va-danger); text-decoration: underline; display: inline-flex; align-items: center; gap: 4px;">
                                        <va-icon name="attach_file" size="small" />{{ getFileName(fileUrl) }}
                                      </a>
                                    </div>
                                  </template>
                                   <template v-else>{{ formatValue(f.val.before) }}</template>
                                </div>
                              </div>
                              <div style="background-color: rgba(67, 160, 71, 0.1); padding: 0.75rem 1rem; font-size: 0.85rem; display: flex; align-items: flex-start; gap: 0.5rem;">
                                <va-icon name="add_circle_outline" color="success" size="small" style="margin-top: 2px;" />
                                <div style="color: var(--va-success); font-weight: 500; word-break: break-all; width: 100%;">
                                  <template v-if="f.type === 'FILE' && getFilesList(f.val.after).length > 0">
                                    <div v-for="(fileUrl, idx) in getFilesList(f.val.after)" :key="idx" style="margin-bottom: 4px;">
                                      <a href="#" @click.prevent="downloadFileWithAuth(fileUrl, getFileName(fileUrl))" style="color: var(--va-success); text-decoration: underline; display: inline-flex; align-items: center; gap: 4px;">
                                        <va-icon name="attach_file" size="small" />{{ getFileName(fileUrl) }}
                                      </a>
                                    </div>
                                  </template>
                                  <template v-else>{{ formatValue(f.val.after) }}</template>
                                </div>
                              </div>
                            </div>
                            <div v-else style="padding: 0.75rem 1rem; font-size: 0.85rem; color: var(--va-text-secondary); background: var(--va-background-primary);">
                              <template v-if="f.type === 'FILE' && getFilesList(f.val.before).length > 0">
                                <div v-for="(fileUrl, idx) in getFilesList(f.val.before)" :key="idx" style="margin-bottom: 4px;">
                                  <a href="#" @click.prevent="downloadFileWithAuth(fileUrl, getFileName(fileUrl))" style="color: var(--va-primary); text-decoration: underline; display: inline-flex; align-items: center; gap: 4px;">
                                    <va-icon name="attach_file" size="small" />{{ getFileName(fileUrl) }}
                                  </a>
                                </div>
                              </template>
                              <template v-else>{{ formatValue(f.val.before) }}</template>
                            </div>
                          </template>
                          <template v-else>
                            <div style="padding: 0.75rem 1rem; font-size: 0.85rem; color: var(--va-text-primary);">
                              <template v-if="f.type === 'FILE' && getFilesList(f.val).length > 0">
                                <div v-for="(fileUrl, idx) in getFilesList(f.val)" :key="idx" style="margin-bottom: 4px;">
                                  <a href="#" @click.prevent="downloadFileWithAuth(fileUrl, getFileName(fileUrl))" style="color: var(--va-primary); text-decoration: underline; display: inline-flex; align-items: center; gap: 4px;">
                                    <va-icon name="attach_file" size="small" />{{ getFileName(fileUrl) }}
                                  </a>
                                </div>
                              </template>
                              <template v-else>{{ formatValue(f.val) }}</template>
                            </div>
                          </template>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <div v-else style="color: var(--va-text-secondary); font-style: italic; font-size: 0.9rem;">
          {{ t('noParsable') }}
        </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Integration Log Details (Inbound 연계 이력) -->
    <div v-if="request?.isIntegration || (request?.sourceSystem && (!request?.steps || request?.steps.length === 0))" 
         style="margin-bottom: 1.5rem; padding: 1rem; background-color: var(--va-background-secondary); border-radius: 6px; border-left: 4px solid var(--va-info);">
      <div style="font-weight: bold; font-size: 0.95rem; color: var(--va-text-primary); margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.5rem;">
        <va-icon name="sync" color="info" size="small" />
        <span>{{ $t('integration_log_info') || '연계 상세 정보 (Integration Log)' }}</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.75rem; margin-bottom: 1rem; font-size: 0.85rem;">
        <div>
          <span style="color: var(--va-text-secondary); margin-right: 0.5rem;">{{ $t('integration_channel_system') || '연계 채널/시스템' }}:</span>
          <span style="font-weight: bold; color: var(--va-text-primary);">{{ request.sourceSystem || 'Inbound Webhook' }}</span>
        </div>
        <div>
          <span style="color: var(--va-text-secondary); margin-right: 0.5rem;">{{ $t('integration_direction') || '연계 방향' }}:</span>
          <va-badge color="warning" text="INBOUND (수신)" size="small" />
        </div>
        <div>
          <span style="color: var(--va-text-secondary); margin-right: 0.5rem;">{{ $t('integration_received_at') || '수신 처리시각' }}:</span>
          <span style="color: var(--va-text-primary);">{{ formatDate(request.createdAt || request.integrationLog?.createdAt) }}</span>
        </div>
        <div>
          <span style="color: var(--va-text-secondary); margin-right: 0.5rem;">{{ $t('integration_status') || '처리 상태' }}:</span>
          <va-badge :color="request.integrationLog?.status === 'FAIL' ? 'danger' : 'success'" :text="request.integrationLog?.status || 'SUCCESS'" size="small" />
        </div>
      </div>

      <template v-if="request.integrationLog">
        <div style="margin-top: 0.75rem;">
          <div style="font-size: 0.82rem; font-weight: bold; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('integration_original_payload') || '외부 수신 원본 Payload (Original Payload)' }}:</div>
          <pre style="background: #1e1e1e; color: #d4d4d4; padding: 0.5rem 0.75rem; border-radius: 4px; font-size: 0.8rem; overflow-x: auto; max-height: 150px; margin: 0;">{{ request.integrationLog.originalPayload }}</pre>
        </div>

        <div style="margin-top: 0.75rem;">
          <div style="font-size: 0.82rem; font-weight: bold; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('integration_mapped_payload') || '매핑 변환 후 Payload (Mapped Payload)' }}:</div>
          <pre style="background: #1e1e1e; color: #ce9178; padding: 0.5rem 0.75rem; border-radius: 4px; font-size: 0.8rem; overflow-x: auto; max-height: 150px; margin: 0;">{{ request.integrationLog.mappedPayload }}</pre>
        </div>
      </template>
    </div>

    <!-- Simple Approval Line Summary & Status (일반 결재 건) -->
    <template v-else>
      <!-- Simple Approval Line Summary -->
      <div style="margin-bottom: 1.5rem; padding: 0.75rem; background-color: var(--va-background-secondary); border-radius: 4px; border-left: 4px solid var(--va-primary); overflow-x: auto;">
        <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 0.75rem;">{{ t('approvalLineSummary') }}</div>
        
        <div v-if="request?.steps && request.steps.length > 0" 
             style="display: flex; align-items: center; width: 100%; overflow-x: auto; padding: 0.25rem 0;">
          <template v-for="(s, idx) in getStepperSteps(request)" :key="idx">
            <!-- Step Node -->
            <div style="display: flex; flex-direction: row; align-items: center; gap: 0.5rem; flex-shrink: 0;">
              <div 
                :class="{'step-flash': s.isPending}"
                :style="{
                  width: '32px', height: '32px', borderRadius: '50%', 
                  backgroundColor: s.hasError ? 'var(--va-danger)' : (s.isPending ? 'var(--va-warning)' : (idx < getCurrentStepIndex(request) ? 'var(--va-primary)' : 'var(--va-background-element)')),
                  border: idx <= getCurrentStepIndex(request) ? 'none' : '2px solid var(--va-background-border)',
                  color: s.isPending ? '#262824' : (idx < getCurrentStepIndex(request) ? 'white' : 'var(--va-text-secondary)'),
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '0.9rem',
                  boxShadow: s.isPending ? '0 0 0 rgba(255, 212, 58, 0.4)' : 'none'
                }"
              >
                {{ s.stepOrder }}
              </div>
              <div style="font-size: 0.85rem; color: var(--va-text-primary); white-space: nowrap; display: flex; flex-direction: column; justify-content: center;">
                <div>{{ s.name }} <span style="color: var(--va-text-secondary); font-size: 0.8rem;">({{ s.statusText }})</span></div>
                <div v-if="s.processedDate" style="font-size: 0.72rem; color: var(--va-text-secondary); opacity: 0.8; margin-top: 2px;">
                  {{ s.processedDate }}
                </div>
              </div>
            </div>
            <!-- Line -->
            <div v-if="idx < getStepperSteps(request).length - 1" 
                 style="flex-grow: 1; min-width: 40px; height: 2px; margin: 0 1rem;"
                 :style="{ backgroundColor: idx < getCurrentStepIndex(request) - 0.5 ? 'var(--va-primary)' : 'var(--va-background-border)' }">
            </div>
          </template>
        </div>
        <div v-else style="font-weight: bold; color: var(--va-primary);">{{ t('noApprovalLine') }}</div>
      </div>

      <!-- Approval Line Status -->
      <div v-if="request?.steps && request.steps.length > 0" style="margin-bottom: 1.5rem; padding-top: 0.5rem; border-top: 1px solid var(--va-background-border);">
        <div style="font-weight: 600; font-size: 0.95rem; margin-bottom: 0.5rem; color: var(--va-text-primary);">{{ t('approvalLineStatus') }}</div>
        <div v-for="group in getGroupedSteps(request)" :key="group.order" style="margin-bottom: 0.25rem;">
          <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
            <div v-for="s in group.steps" :key="s.id" style="flex: 1; min-width: 200px; background: var(--va-background-element); padding: 0.5rem; border-radius: 4px; font-size: 0.85rem; border: 1px solid var(--va-background-border);">
              <div style="display: flex; justify-content: space-between; margin-bottom: 4px; align-items: center;">
                <span style="font-weight: bold; color: var(--va-primary); display: flex; align-items: center;">
                  <span style="display:inline-flex; align-items:center; justify-content:center; width:20px; height:20px; background-color:var(--va-primary); color:white; border-radius:50%; font-size:0.75rem; margin-right:6px; font-weight:bold;">{{ s.stepOrder }}</span>
                  {{ getStepTypeLabel(s) }} - {{ s.assigneeName || getUserName(s.assigneeId) }}
                </span>
                <va-badge :color="s.stepType === 'DRAFT' ? 'info' : (s.status === 'APPROVED' ? 'success' : (s.status === 'REJECTED' ? 'danger' : 'warning'))" size="small">{{ getStepStatusLabel(s) }}</va-badge>
              </div>
              <div v-if="s.status === 'APPROVED' || s.status === 'REJECTED' || s.stepType === 'DRAFT'" style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 4px; text-align: right;">
                {{ formatDate(s.updatedAt) }} {{ t('processed') }}
              </div>
              <div v-if="s.comment" style="color: var(--va-text-primary); background: var(--va-background-secondary); padding: 4px 8px; border-radius: 4px; border-left: 3px solid var(--va-primary); font-style: italic;">
                "{{ s.comment }}"
              </div>
              <div v-else style="color: var(--va-text-secondary); font-style: italic;">
                {{ t('noComment') }}
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="(request?.observerNames && request.observerNames.length > 0) || getObserversList(request?.observerIds).length > 0" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px dashed var(--va-background-border);">
          <div style="font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem; color: var(--va-text-secondary);">{{ t('observers') }}</div>
          <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
            <template v-if="request?.observerNames && request.observerNames.length > 0">
              <va-badge v-for="(obsName, idx) in request.observerNames" :key="idx" color="info" preset="secondary">{{ obsName }}</va-badge>
            </template>
            <template v-else>
              <va-badge v-for="obsId in getObserversList(request?.observerIds)" :key="obsId" color="info" preset="secondary">{{ getUserName(obsId) }}</va-badge>
            </template>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const isRequestedDataExpanded = ref(true)

const props = defineProps({
  request: {
    type: Object,
    required: true
  }
})

const currentLocale = useCookie('locale', { default: () => 'ko' })
const token = useCookie('auth_token')
const { downloadFileWithAuth } = useFileDownloader()
const fieldNameMap = ref({})
const domainRefDisplayMap = ref({})

const getUserName = (uuid, nameFallback) => {
  return nameFallback || uuid || ''
}

const loadFieldNamesForRequest = async (req) => {
  if (!req) return;
  try {
    let nodeId = req.nodeId || props.nodeId;
    if (!nodeId && req.targetId) {
      try {
        const record = await $fetch(`/api/records/${req.targetId}`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => null)
        nodeId = record?.node?.id || record?.nodeId;
      } catch (e) {}
    }
    if (!nodeId && req.integrationLog?.channelId) {
      try {
        const ch = await $fetch(`/api/admin/integration/channels/${req.integrationLog.channelId}`, { headers: { Authorization: `Bearer ${token.value}` } })
        nodeId = ch?.nodeId;
      } catch (e) {}
    }
    if (nodeId) {
      const fields = await $fetch(`/api/nodes/${nodeId}/fields/effective`, { headers: { Authorization: `Bearer ${token.value}` } })
      if (fields && fields.length > 0) {
        const map = {}
        fields.forEach(f => {
          if (f.key) {
            map[f.key] = f
            map[f.key.toUpperCase()] = f
            map[f.key.toLowerCase()] = f
          }
        })
        fieldNameMap.value = map
      }
    }
  } catch (e) {
    console.error('Error loading field names for request:', e)
  }
}

watch(() => [props.request, props.nodeId], async ([newReq]) => {
  if (newReq) {
    await loadFieldNamesForRequest(newReq);
  }
}, { immediate: true })


const getStepTypeLabel = (s) => {
  if (!s) return ''
  if (s.stepType === 'CONSENSUS') return t('typeConsensus')
  if (s.stepType === 'DRAFT') return t('typeDraft')
  return t('typeApproval')
}

const getStepStatusLabel = (s) => {
  if (!s) return ''
  return s.stepType === 'DRAFT' ? t('stepDraft') : s.status
}

const getParsedChanges = (changesString) => {
  if (!changesString) return null
  if (typeof changesString === 'object') return changesString
  try {
    let parsed = JSON.parse(changesString)
    if (typeof parsed === 'string') parsed = JSON.parse(parsed)
    if (typeof parsed === 'string') parsed = JSON.parse(parsed)
    if (Object.keys(parsed || {}).length === 0) return null
    return parsed
  } catch (e) {
    console.error('Failed to parse changes:', e, changesString)
    return null
  }
}

const fetchDomainRefName = async (uuid, targetDomainId) => {
  if (!uuid || domainRefDisplayMap.value[uuid]) return;
  domainRefDisplayMap.value[uuid] = 'Loading...'; 
  try {
    const rec = await $fetch(`/api/records/${uuid}`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => null);
    
    if (!rec) {
      const uname = getUserName(uuid);
      domainRefDisplayMap.value[uuid] = (uname && uname !== uuid) ? uname : uuid;
      return;
    }
    
    let tDomainId = targetDomainId;
    if (!tDomainId && rec.node) {
      tDomainId = rec.node.domain?.id || rec.node.domainId;
    }
    if (!tDomainId) tDomainId = rec.domainId;
    
    if (!tDomainId) {
      domainRefDisplayMap.value[uuid] = uuid;
      return;
    }

    const domains = await $fetch('/api/domains', { headers: { Authorization: `Bearer ${token.value}` } })
    const tDomain = domains.find(d => d.id === tDomainId)
    if (!tDomain) {
      domainRefDisplayMap.value[uuid] = uuid;
      return;
    }
    const dFieldId = tDomain.displayNameFieldId || tDomain.identifierFieldId
    const tFields = await $fetch(`/api/domains/${tDomainId}/fields`, { headers: { Authorization: `Bearer ${token.value}` } })
    let f = tFields.find(x => x.id === dFieldId);
    if (!f) {
      f = tFields.find(x => {
        const n = JSON.stringify(x.name).toLowerCase();
        return n.includes('name') || n.includes('이름') || n.includes('사원명') || n.includes('title') || n.includes('제목');
      });
      if (!f) f = tFields.find(x => x.type === 'TEXT');
    }
    
    if (f && rec.data) {
      const dataObj = typeof rec.data === 'string' ? JSON.parse(rec.data) : rec.data;
      const rawVal = dataObj[f.key];
      if (rawVal) {
        let displayStr = rawVal;
        if (typeof rawVal === 'string') {
          try {
            const parsed = JSON.parse(rawVal);
            if (parsed && typeof parsed === 'object') {
              displayStr = parsed[currentLocale.value] || parsed.ko || parsed.en || rawVal;
            }
          } catch(e) {}
        } else if (typeof rawVal === 'object') {
          displayStr = rawVal[currentLocale.value] || rawVal.ko || rawVal.en || JSON.stringify(rawVal);
        }
        domainRefDisplayMap.value[uuid] = displayStr;
      } else {
        domainRefDisplayMap.value[uuid] = uuid;
      }
    } else {
      domainRefDisplayMap.value[uuid] = uuid;
    }
  } catch (e) {
    const uname = getUserName(uuid);
    domainRefDisplayMap.value[uuid] = (uname && uname !== uuid) ? uname : uuid;
  }
}

const getFilesList = (v) => {
  if (!v) return []
  if (typeof v === 'string') {
    if (v.startsWith('[')) {
      try {
        const arr = JSON.parse(v)
        if (Array.isArray(arr)) {
          return arr
        }
      } catch (e) {}
    }
    return [v]
  }
  return []
}

const formatValue = (val) => {
  if (val === null || val === undefined || val === '') return '-';
  let obj = val;
  if (typeof val === 'string') {
    if (val.trim().startsWith('{')) {
      try { obj = JSON.parse(val); } catch (e) { return val; }
    } else {
      return val;
    }
  }
  if (typeof obj === 'object' && obj !== null) {
    if ('ko' in obj || 'en' in obj) {
      const loc = currentLocale.value === 'en' ? 'en' : 'ko';
      const primary = obj[loc] || obj.ko || obj.en;
      const secondary = loc === 'ko' ? obj.en : obj.ko;
      if (primary && secondary && primary !== secondary) {
        return `${primary} (${secondary})`;
      }
      return primary || secondary || '-';
    }
    return JSON.stringify(obj);
  }
  return String(val);
}

const getFileName = (url) => {
  if (!url) return ''
  if (typeof url !== 'string') return 'Unknown File'
  try {
    if (url.includes('?name=')) {
      const qs = url.split('?name=')[1]
      return decodeURIComponent(qs.split('&')[0])
    }
    const parts = url.split('/')
    let name = parts[parts.length - 1]
    if (name.includes('?')) {
      name = name.split('?')[0]
    }
    return decodeURIComponent(name)
  } catch (e) {
    return url
  }
}

const getGroupedChangesList = (changesString, targetType) => {
  let parsed = getParsedChanges(changesString)
  if (!parsed) return []
  
  const normalizeData = (dataObj) => {
    const normalized = {};
    Object.keys(dataObj).forEach(k => {
      normalized[k.toUpperCase()] = dataObj[k];
    });
    Object.values(fieldNameMap.value || {}).forEach(f => {
      const uKey = String(f.key).toUpperCase();
      if (f.type === 'MULTILINGUAL' && normalized[uKey] !== undefined && normalized[uKey] !== null) {
        if (typeof normalized[uKey] === 'string') {
          try { normalized[uKey] = JSON.parse(normalized[uKey]); }
          catch (e) { normalized[uKey] = { ko: normalized[uKey], en: '' }; }
        }
      }
    });
    return normalized;
  };

  if (targetType === 'RECORD_UPDATE') {
    parsed.before = normalizeData(parsed.before || {})
    parsed.after = normalizeData(parsed.after || {})
  } else {
    if (targetType === 'RECORD_CREATE' || targetType === 'RECORD' || targetType === 'CREATE') {
       if (parsed.after) parsed = parsed.after;
    }
    parsed = normalizeData(parsed || {})
  }

  const map = new Map()
  let keysToProcess = []
  if (targetType === 'RECORD_UPDATE') {
    const beforeKeys = Object.keys(parsed.before || {})
    const afterKeys = Object.keys(parsed.after || {})
    keysToProcess = [...new Set([...beforeKeys, ...afterKeys])]
  } else {
    keysToProcess = Object.keys(parsed)
  }
  
  keysToProcess.forEach(key => {
    let valBefore = null
    let valAfter = null
    if (targetType === 'RECORD_UPDATE') {
      valBefore = (parsed.before || {})[key]
      valAfter = (parsed.after || {})[key]
    } else {
      valAfter = parsed[key]
    }
    
    const foundField = Object.values(fieldNameMap.value || {}).find(field => field && field.key && (String(field.key).toUpperCase() === key || String(field.key).toLowerCase() === key.toLowerCase()))
    let inferredType = foundField ? foundField.type : undefined;
    const strValCheck = String(valAfter || valBefore || '');
    if (!inferredType && (strValCheck.includes('/api/files/download/') || strValCheck.includes('name='))) {
      inferredType = 'FILE';
    }
    const fieldLabelName = foundField ? foundField.name : (inferredType === 'FILE' ? '파일' : key);
    const f = foundField ? { ...foundField, type: foundField.type || inferredType } : { name: fieldLabelName, type: inferredType, fieldGroup: null };
    
    const parseName = (nameObj) => {
      if (!nameObj) return null;
      if (typeof nameObj === 'string') {
        try {
          const parsed = JSON.parse(nameObj);
          if (typeof parsed === 'object' && parsed !== null) return parsed;
          return nameObj;
        } catch(e){
          return nameObj;
        }
      }
      return nameObj;
    }
    const translate = (nameObj, defaultKo, defaultEn) => {
      const p = parseName(nameObj)
      if (!p) return currentLocale.value === 'ko' ? defaultKo : defaultEn
      if (typeof p === 'string') return p;
      return p[currentLocale.value] || p.ko || p.en || (currentLocale.value === 'ko' ? defaultKo : defaultEn)
    }
    
    const sObj = f.fieldGroup?.sector
    const gObj = f.fieldGroup

    const sName = translate(sObj?.name, '일반', 'General')
    const sKey = sObj?.id || 'default'
    const sOrder = sObj?.sortOrder || 0
    
    const gName = translate(gObj?.name, '기본 정보', 'Basic Info')
    const gKey = gObj?.id || 'default'
    const gOrder = gObj?.sortOrder || 0
    
    if (!map.has(sKey)) {
      map.set(sKey, { key: sKey, label: sName, order: sOrder, groups: new Map() })
    }
    const sectorObj = map.get(sKey)
    
    if (!sectorObj.groups.has(gKey)) {
      sectorObj.groups.set(gKey, { key: gKey, label: gName, order: gOrder, fields: [] })
    }
    
    let displayValBefore = valBefore;
    let displayValAfter = valAfter;
    
    const parseMultilingual = (val) => {
      if (!val) return val;
      let obj = val;
      if (typeof val === 'string') {
        try {
          obj = JSON.parse(val);
        } catch (e) {
          return val;
        }
      }
      if (typeof obj === 'object' && obj !== null) {
        const isEmpty = Object.values(obj).every(v => !v || String(v).trim() === '');
        if (isEmpty) return '-';
        const loc = currentLocale.value === 'en' ? 'en' : 'ko';
        const primary = obj[loc] || obj.ko || obj.en;
        const secondary = loc === 'ko' ? obj.en : obj.ko;
        if (primary && secondary && primary !== secondary) {
          return `${primary} (${secondary})`;
        }
        return primary || secondary || JSON.stringify(obj);
      }
      return val;
    };

    if (f.type === 'DOMAIN_REFERENCE') {
      let tDomainId = null
      try { tDomainId = JSON.parse(f.options || '{}').targetDomainId } catch(e){}
      if (targetType === 'RECORD_UPDATE') {
        if (valBefore && !domainRefDisplayMap.value[valBefore]) fetchDomainRefName(valBefore, tDomainId);
        if (valAfter && !domainRefDisplayMap.value[valAfter]) fetchDomainRefName(valAfter, tDomainId);
        displayValBefore = domainRefDisplayMap.value[valBefore] || valBefore;
        displayValAfter = domainRefDisplayMap.value[valAfter] || valAfter;
      } else {
        if (valAfter && !domainRefDisplayMap.value[valAfter]) fetchDomainRefName(valAfter, tDomainId);
        displayValAfter = domainRefDisplayMap.value[valAfter] || valAfter;
      }
    } else if (f.type === 'FILE') {
      displayValBefore = valBefore;
      displayValAfter = valAfter;
    } else if (f.type === 'MULTILINGUAL' || typeof valAfter === 'object' || typeof valBefore === 'object' || (typeof valAfter === 'string' && valAfter.trim().startsWith('{')) || (typeof valBefore === 'string' && valBefore.trim().startsWith('{'))) {
      try {
        if (targetType === 'RECORD_UPDATE') {
          displayValBefore = parseMultilingual(valBefore);
          displayValAfter = parseMultilingual(valAfter);
        } else {
          displayValAfter = parseMultilingual(valAfter);
        }
      } catch (e) {}
    } else if (['SELECT', 'MULTI_SELECT'].includes(f.type) && f.options) {
      try {
        const opts = JSON.parse(f.options);
        const mapVal = (v) => {
          if (!v) return v;
          const found = opts.find(o => o.key === v);
          if (found && found.label) {
            return found.label[currentLocale.value] || found.label.ko || found.label.en || v;
          }
          return v;
        };
        
        if (targetType === 'RECORD_UPDATE') {
          if (Array.isArray(valBefore)) displayValBefore = valBefore.map(mapVal).join(', ');
          else displayValBefore = mapVal(valBefore);
          
          if (Array.isArray(valAfter)) displayValAfter = valAfter.map(mapVal).join(', ');
          else displayValAfter = mapVal(valAfter);
        } else {
          if (Array.isArray(valAfter)) displayValAfter = valAfter.map(mapVal).join(', ');
          else displayValAfter = mapVal(valAfter);
        }
      } catch(e) {}
    }
    
    if (typeof displayValBefore === 'string') {
      const uName = getUserName(displayValBefore);
      if (uName && uName !== displayValBefore) displayValBefore = uName;
    }
    if (typeof displayValAfter === 'string') {
      const uName = getUserName(displayValAfter);
      if (uName && uName !== displayValAfter) displayValAfter = uName;
    }
    
    if (f && ['NUMBER', 'INTEGER', 'DECIMAL', 'CALCULATED'].includes(f.type)) {
      if (displayValBefore !== null && displayValBefore !== undefined && displayValBefore !== '' && displayValBefore !== '-') {
        const numBefore = Number(displayValBefore);
        if (!isNaN(numBefore)) {
          displayValBefore = numBefore.toLocaleString('ko-KR');
          if (f.unit) displayValBefore += ` ${f.unit}`;
        }
      }
      if (displayValAfter !== null && displayValAfter !== undefined && displayValAfter !== '' && displayValAfter !== '-') {
        const numAfter = Number(displayValAfter);
        if (!isNaN(numAfter)) {
          displayValAfter = numAfter.toLocaleString('ko-KR');
          if (f.unit) displayValAfter += ` ${f.unit}`;
        }
      }
    }

    let finalVal = null;
    if (targetType === 'RECORD_UPDATE') {
      const vBefore = displayValBefore || '-';
      const vAfter = displayValAfter || '-';

      let isActuallyChanged = false;
      const strBefore = (vBefore === '-' || vBefore === null || vBefore === undefined) ? '' : String(vBefore).trim();
      const strAfter = (vAfter === '-' || vAfter === null || vAfter === undefined) ? '' : String(vAfter).trim();

      if (strBefore !== strAfter) {
        if (f.type === 'FILE') {
          // 파일 타입 필드는 after에 유효한 파일 정보가 있으면 변경으로 판단
          if (strAfter !== '' && strAfter !== '-') {
            isActuallyChanged = true;
          }
        } else {
          // 일반 텍스트/다국어 필드는 before 객체에 키가 실제로 존재하고 strBefore가 유효할 때만 변경으로 판단
          const uKey = String(key).toUpperCase();
          const fKey = f && f.key ? String(f.key).toUpperCase() : uKey;
          const bObj = parsed.before || {};
          const bKeys = Object.keys(bObj);
          const hasKeyInBefore = bKeys.some(bk => {
            const uppercaseBk = String(bk).toUpperCase();
            return uppercaseBk === uKey || uppercaseBk === fKey;
          });

          if (hasKeyInBefore && strBefore !== '') {
            const multiBefore = parseMultilingual(valBefore);
            const multiAfter = parseMultilingual(valAfter);
            if (multiBefore && multiAfter && String(multiBefore).trim() === String(multiAfter).trim()) {
              isActuallyChanged = false;
            } else {
              isActuallyChanged = true;
            }
          } else {
            // before 객체에 없었던 텍스트 필드는 수정한 대상이 아님!
            isActuallyChanged = false;
          }
        }
      }

      finalVal = {
        before: vBefore,
        after: vAfter,
        isChanged: isActuallyChanged
      }
    } else {
      finalVal = displayValAfter || '-'
    }
    
    sectorObj.groups.get(gKey).fields.push({ key, label: translate(f.name, key, key), val: finalVal, gridWidth: f.gridWidth, type: f.type, order: f.order || 0 })
  })
  
  const sectorsArray = Array.from(map.values())
  let sectors = sectorsArray
  
  if (targetType === 'RECORD_UPDATE') {
    sectors.forEach(s => {
      s.groups.forEach(g => {
        g.fields = g.fields.filter(f => f.val && f.val.isChanged)
      })
      Array.from(s.groups.keys()).forEach(k => {
        if (s.groups.get(k).fields.length === 0) {
          s.groups.delete(k)
        }
      })
    })
    sectors = sectors.filter(s => s.groups.size > 0)
  }

  sectors.sort((a, b) => a.order - b.order)
  
  return sectors.map(s => {
    const groups = Array.from(s.groups.values())
    groups.sort((a, b) => a.order - b.order)
    groups.forEach(g => {
      g.fields.sort((a, b) => a.order - b.order)
    })
    return {
      key: s.key,
      label: s.label,
      groups: groups
    }
  })
}

const getGroupedSteps = (request) => {
  if (!request) return []
  const steps = request.steps || []
  const map = new Map()

  const hasDraftStep = steps.some(s => s.stepOrder === 0 || s.stepType === 'DRAFT')
  if (!hasDraftStep && request.requesterId) {
    map.set(0, {
      order: 0,
      steps: [{
        id: 'draft-step-' + request.id,
        stepType: 'DRAFT',
        stepOrder: 0,
        assigneeId: request.requesterId,
        status: 'SUBMITTED',
        updatedAt: request.createdAt,
        comment: '' 
      }]
    })
  }

  if (steps.length > 0) {
    steps.forEach(s => {
      if (!map.has(s.stepOrder)) {
        map.set(s.stepOrder, { order: s.stepOrder, steps: [] })
      }
      map.get(s.stepOrder).steps.push(s)
    })
  }
  
  const result = Array.from(map.values())
  result.sort((a, b) => a.order - b.order)
  return result
}

const { parseDate: commonParseDate, formatWithTimezone } = useTimezoneDate()

const formatDate = (dateString) => {
  if (!dateString) return ''
  return formatWithTimezone(dateString)
}

const parseDate = (dateString) => {
  return commonParseDate(dateString)
}

const formatStepDate = (dateString) => {
  if (!dateString) return '';
  const date = parseDate(dateString);
  if (!date) return '';
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value;
  try {
    const formatter = new Intl.DateTimeFormat('ko-KR', {
      timeZone: tz,
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
    const parts = formatter.formatToParts(date);
    const getPart = (type) => parts.find(p => p.type === type).value;

    const tzFormatter = new Intl.DateTimeFormat('en-US', {
      timeZone: tz,
      year: 'numeric', month: 'numeric', day: 'numeric',
      hour: 'numeric', minute: 'numeric', hour12: false
    });
    const tzParts = tzFormatter.formatToParts(date);
    const getTzVal = (type) => parseInt(tzParts.find(p => p.type === type).value, 10);

    const utcFormatter = new Intl.DateTimeFormat('en-US', {
      timeZone: 'UTC',
      year: 'numeric', month: 'numeric', day: 'numeric',
      hour: 'numeric', minute: 'numeric', hour12: false
    });
    const utcParts = utcFormatter.formatToParts(date);
    const getUtcVal = (type) => parseInt(utcParts.find(p => p.type === type).value, 10);

    const tzDate = new Date(Date.UTC(getTzVal('year'), getTzVal('month') - 1, getTzVal('day'), getTzVal('hour'), getTzVal('minute')));
    const utcDate = new Date(Date.UTC(getUtcVal('year'), getUtcVal('month') - 1, getUtcVal('day'), getUtcVal('hour'), getUtcVal('minute')));

    const diffMs = tzDate.getTime() - utcDate.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    const sign = diffHours >= 0 ? '+' : '-';
    const absHours = Math.abs(diffHours);
    const hours = Math.floor(absHours);
    const offsetStr = `GMT${sign}${hours}`;

    return `${getPart('month')}/${getPart('day')} ${getPart('hour')}:${getPart('minute')}:${getPart('second')}`;
  } catch (e) {
    const pad = (n) => n.toString().padStart(2, '0');
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    const seconds = pad(date.getSeconds());
    return `${month}/${day} ${hours}:${minutes}:${seconds}`;
  }
}

const getStepperSteps = (req) => {
  if (!req || !req.steps || req.steps.length === 0) return [];
  const sortedSteps = [...req.steps].sort((a, b) => a.stepOrder - b.stepOrder);
  const result = sortedSteps.map(s => {
    const name = s.assigneeName || getUserName(s.assigneeId);
    let statusText = '';
    if (s.stepType === 'DRAFT') statusText = t('stepDraft');
    else if (s.status === 'APPROVED') statusText = t('stepApproved');
    else if (s.status === 'REJECTED') statusText = t('stepRejected');
    else if (s.status === 'PENDING') statusText = t('stepPending');
    else statusText = t('stepScheduled');
    
    // DRAFT, APPROVED, REJECTED 완료 시점에 날짜 표시
    const isCompleted = s.stepType === 'DRAFT' || s.status === 'APPROVED' || s.status === 'REJECTED';
    const processedDate = isCompleted ? formatStepDate(s.updatedAt) : '';

    return {
      stepOrder: s.stepOrder,
      name: name,
      statusText: statusText,
      hasError: s.status === 'REJECTED',
      isPending: s.status === 'PENDING',
      processedDate: processedDate
    };
  });
  
  const isAllApproved = req.status === 'APPROVED';
  const isRejected = req.status === 'REJECTED';
  const isFinalized = isAllApproved || isRejected;
  
  let systemStatusText = '';
  if (isAllApproved) {
    systemStatusText = t('systemComplete');
  } else if (isRejected) {
    systemStatusText = t('systemCancelled');
  } else {
    systemStatusText = t('stepScheduled');
  }

  result.push({
    stepOrder: result.length > 0 ? result[result.length - 1].stepOrder + 1 : 1,
    name: t('systemApplied'),
    statusText: systemStatusText,
    hasError: false,
    isPending: false,
    processedDate: isFinalized ? formatStepDate(req.updatedAt) : ''
  });
  
  return result;
}

const getCurrentStepIndex = (req) => {
  if (!req || !req.steps || req.steps.length === 0) return 0;
  if (req.status === 'APPROVED') return req.steps.length + 1;
  const sortedSteps = [...req.steps].sort((a, b) => a.stepOrder - b.stepOrder);
  let currentIndex = sortedSteps.findIndex(s => s.status === 'PENDING');
  if (currentIndex === -1) {
    currentIndex = sortedSteps.findIndex(s => s.status === 'REJECTED');
    if (currentIndex === -1) {
      currentIndex = sortedSteps.length - 1; 
    }
  }
  return currentIndex;
}

const getObserversList = (obsString) => {
  if (!obsString) return []
  try {
    const parsed = JSON.parse(obsString)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    return []
  }
}

</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: var(--va-background-primary); 
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--va-background-border); 
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--va-secondary); 
}
.step-flash {
  animation: pulse-border 2s infinite;
}
@keyframes pulse-border {
  0% { box-shadow: 0 0 0 0 rgba(255, 212, 58, 0.7); }
  70% { box-shadow: 0 0 0 6px rgba(255, 212, 58, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 212, 58, 0); }
}
</style>



<style scoped>
.accordion-header:hover {
  background-color: rgba(67, 56, 202, 0.06) !important;
}

.accordion-header:hover .va-chip {
  transform: translateY(-1px);
}

.accordion-wrapper {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.38s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0;
}

.accordion-wrapper.is-expanded {
  grid-template-rows: 1fr;
  opacity: 1;
}

.accordion-inner {
  overflow: hidden;
}
</style>
