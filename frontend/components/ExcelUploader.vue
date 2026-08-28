<template>
  <AppModal
    :model-value="true"
    :title="t('excel_uploader.title') || '엑셀 / CSV 대량 업로드 (Bulk Import)'"
    icon="upload_file"
    size="large"
    hide-default-actions
    @update:model-value="$emit('close')"
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; min-height: 480px; max-height: 75vh;">
      <!-- Modern Multi-Step Stepper Header -->
      <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 1rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 10px; gap: 0.5rem; flex-wrap: wrap;">
        <div
          v-for="(st, idx) in stepsMeta"
          :key="st.step"
          style="display: flex; align-items: center; gap: 0.5rem; cursor: default;"
          :style="{ opacity: step >= st.step ? 1 : 0.45 }"
        >
          <div
            style="width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.82rem; font-weight: 700; transition: all 0.2s;"
            :style="{
              background: step === st.step ? 'var(--va-primary)' : (step > st.step ? 'var(--va-success)' : 'var(--va-background-border)'),
              color: '#ffffff',
              boxShadow: step === st.step ? '0 2px 8px rgba(37, 99, 235, 0.35)' : 'none'
            }"
          >
            <va-icon v-if="step > st.step" name="check" size="small" color="#fff" />
            <span v-else>{{ idx + 1 }}</span>
          </div>
          <div style="display: flex; flex-direction: column;">
            <span style="font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary);">{{ st.label }}</span>
            <span style="font-size: 0.72rem; color: var(--va-text-secondary);">{{ st.desc }}</span>
          </div>
          <va-icon v-if="idx < stepsMeta.length - 1" name="chevron_right" size="small" color="secondary" style="margin-left: 0.5rem;" />
        </div>
      </div>

      <!-- Global Error Alert -->
      <va-alert v-if="uploadErrorMsg" color="danger" outline closeable @update:model-value="uploadErrorMsg = ''" style="margin: 0;">
        ⚠️ {{ uploadErrorMsg }}
      </va-alert>

      <!-- Step 1: File Upload & Template Download -->
      <div v-if="step === 1" style="display: flex; flex-direction: column; gap: 1.25rem; flex: 1;">
        <!-- Template Download Banner -->
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.25rem; background: rgba(37, 99, 235, 0.04); border: 1px solid rgba(37, 99, 235, 0.18); border-radius: 10px;">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div style="width: 40px; height: 40px; border-radius: 8px; background: rgba(37, 99, 235, 0.1); display: flex; align-items: center; justify-content: center;">
              <va-icon name="description" color="primary" size="1.4rem" />
            </div>
            <div>
              <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">표준 엑셀 템플릿 제공</div>
              <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-top: 2px;">
                현재 스키마에 정의된 필드 구조(코드, 유효성 검칙 목록)가 포함된 표준 서식입니다.
              </div>
            </div>
          </div>
          <va-button color="primary" outline size="small" icon="download" @click="downloadTemplate">
            {{ t('excel_uploader.download_template') || '템플릿 다운로드 (.xlsx)' }}
          </va-button>
        </div>

        <!-- Drag & Drop Zone -->
        <div
          style="flex: 1; min-height: 240px; border: 2px dashed var(--va-primary); border-radius: 12px; background: var(--va-background-element); display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; text-align: center; cursor: pointer; transition: all 0.2s;"
          @click="$refs.fileInput?.click()"
          @dragover.prevent
          @drop.prevent="handleDrop"
        >
          <input ref="fileInput" type="file" accept=".xlsx, .xls, .csv" style="display: none;" @change="handleFileUpload" />
          
          <div style="width: 64px; height: 64px; border-radius: 50%; background: rgba(37, 99, 235, 0.08); display: flex; align-items: center; justify-content: center; margin-bottom: 1rem;">
            <va-icon name="cloud_upload" color="primary" size="2.5rem" />
          </div>
          
          <h4 style="font-size: 1.05rem; font-weight: 700; color: var(--va-text-primary); margin-bottom: 0.35rem;">
            {{ t('excel_uploader.drag_drop_file') || '파일을 드래그하여 놓거나 클릭하여 선택하세요' }}
          </h4>
          <p style="font-size: 0.82rem; color: var(--va-text-secondary); margin-bottom: 1rem;">
            {{ t('excel_uploader.supported_formats') || '지원 형식: .xlsx, .xls, .csv (최대 50MB, 10,000행)' }}
          </p>

          <va-button color="primary" size="small" icon="folder_open">
            {{ t('excel_uploader.selected_file') || '파일 찾아보기' }}
          </va-button>
        </div>
      </div>

      <!-- Step 2: Smart Column Mapping -->
      <div v-else-if="step === 2" style="display: flex; flex-direction: column; gap: 1rem; flex: 1; overflow: hidden;">
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1rem; background: var(--va-background-element); border-radius: 8px; border: 1px solid var(--va-background-border);">
          <div>
            <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
              총 <b>{{ parsedData.length.toLocaleString() }}</b>개의 데이터 행(Row)이 감지되었습니다.
            </span>
            <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-top: 2px;">
              업로드할 파일의 엑셀 열(헤더)과 시스템 마스터 필드를 매핑해주세요. (자동 매핑 완료)
            </div>
          </div>
          <va-badge :text="`${nodeFields.length}개 대상 필드`" color="info" />
        </div>

        <div style="flex: 1; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; background: var(--va-background-card);">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left;">
            <thead>
              <tr style="position: sticky; top: 0; z-index: 2; background: var(--va-background-secondary); border-bottom: 2px solid var(--va-background-border);">
                <th style="padding: 0.6rem 0.8rem; width: 45%; font-weight: 700; color: var(--va-text-primary);">{{ t('excel_uploader.target_field') || '시스템 마스터 필드' }}</th>
                <th style="padding: 0.6rem 0.8rem; width: 55%; font-weight: 700; color: var(--va-text-primary);">{{ t('excel_uploader.source_column') || '엑셀 파일 열 (Source Column)' }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="field in nodeFields"
                :key="field.id"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.65rem 0.8rem; vertical-align: middle;">
                  <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;">
                    <span style="font-weight: 600; color: var(--va-text-primary);">{{ getTranslatedName(field.name) }}</span>
                    <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-family: monospace;">({{ field.key }})</span>
                    <va-badge v-if="field.required" text="필수" color="danger" size="small" />
                    <va-badge v-if="field.type === 'MULTILINGUAL'" text="다국어" color="info" size="small" />
                    <va-badge v-if="field.isEncrypted" text="암호화" color="warning" size="small" />
                  </div>
                </td>
                <td style="padding: 0.5rem 0.8rem; vertical-align: middle;">
                  <!-- Multilingual (ko/en) -->
                  <div v-if="field.type === 'MULTILINGUAL'" style="display: flex; flex-direction: column; gap: 0.4rem;">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                      <va-badge text="KO" color="primary" size="small" />
                      <select v-model="mapping[field.key + '_ko']" class="mapping-select">
                        <option :value="null">{{ t('excel_uploader.ignore_column') || '(열 매핑 제외)' }}</option>
                        <option v-for="header in excelHeaders" :key="header" :value="header">{{ header }}</option>
                      </select>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                      <va-badge text="EN" color="secondary" size="small" />
                      <select v-model="mapping[field.key + '_en']" class="mapping-select">
                        <option :value="null">{{ t('excel_uploader.ignore_column') || '(열 매핑 제외)' }}</option>
                        <option v-for="header in excelHeaders" :key="header" :value="header">{{ header }}</option>
                      </select>
                    </div>
                  </div>
                  <!-- Normal Fields -->
                  <div v-else>
                    <select v-model="mapping[field.key]" class="mapping-select">
                      <option :value="null">{{ t('excel_uploader.ignore_column') || '(열 매핑 제외)' }}</option>
                      <option v-for="header in excelHeaders" :key="header" :value="header">{{ header }}</option>
                    </select>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Step 3: Data Quality (DQ) Validation Report -->
      <div v-else-if="step === 3" style="display: flex; flex-direction: column; gap: 1rem; flex: 1; overflow: hidden;">
        <!-- Loading State -->
        <div v-if="validating" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 3rem; gap: 1rem;">
          <va-progress-circle indeterminate color="primary" size="large" />
          <div style="font-weight: 700; font-size: 1rem; color: var(--va-text-primary);">
            데이터 품질 검칙(DQ) 및 무결성 검증 중...
          </div>
          <div style="font-size: 0.85rem; color: var(--va-text-secondary);">
            필수값, 정규식 포맷, 도메인 참조 무결성 등을 전수 검사하고 있습니다.
          </div>
        </div>

        <!-- Validation Result -->
        <template v-else-if="validationResult">
          <!-- Summary Metric Cards -->
          <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem;">
            <div style="padding: 0.75rem 1rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600;">총 검사 행수</div>
              <div style="font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); margin-top: 2px;">
                {{ validationResult.totalRows.toLocaleString() }}
              </div>
            </div>
            <div style="padding: 0.75rem 1rem; background: rgba(34, 197, 94, 0.06); border: 1px solid rgba(34, 197, 94, 0.25); border-radius: 8px;">
              <div style="font-size: 0.75rem; color: #15803d; font-weight: 600;">정상 통과</div>
              <div style="font-size: 1.25rem; font-weight: 800; color: #15803d; margin-top: 2px;">
                {{ validationResult.validRows.toLocaleString() }}
              </div>
            </div>
            <div style="padding: 0.75rem 1rem; background: rgba(239, 68, 68, 0.06); border: 1px solid rgba(239, 68, 68, 0.25); border-radius: 8px;">
              <div style="font-size: 0.75rem; color: #b91c1c; font-weight: 600;">오류 및 위반</div>
              <div style="font-size: 1.25rem; font-weight: 800; color: #b91c1c; margin-top: 2px;">
                {{ validationResult.invalidRows.toLocaleString() }}
              </div>
            </div>
            <div style="padding: 0.75rem 1rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600;">적합률</div>
              <div style="font-size: 1.25rem; font-weight: 800; color: var(--va-primary); margin-top: 2px;">
                {{ Math.round((validationResult.validRows / (validationResult.totalRows || 1)) * 100) }}%
              </div>
            </div>
          </div>

          <!-- Filter & Toggle Bar -->
          <div v-if="validationResult.invalidRows > 0" style="display: flex; justify-content: space-between; align-items: center;">
            <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.85rem; font-weight: 600; color: var(--va-text-primary);">
              <input type="checkbox" v-model="showOnlyErrors" style="cursor: pointer;" />
              <span>오류 발생 행만 모아보기 ({{ validationResult.invalidRows }}건)</span>
            </label>
          </div>

          <!-- DQ Violation Table -->
          <div style="flex: 1; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; background: var(--va-background-card);">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="position: sticky; top: 0; z-index: 2; background: var(--va-background-secondary); border-bottom: 2px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.6rem; width: 60px; text-align: center;">행 번호</th>
                  <th style="padding: 0.5rem 0.6rem; width: 80px; text-align: center;">검증 결과</th>
                  <th style="padding: 0.5rem 0.6rem; width: 140px;">대상 필드</th>
                  <th style="padding: 0.5rem 0.6rem; width: 90px; text-align: center;">심각도</th>
                  <th style="padding: 0.5rem 0.6rem;">위반 사유 및 검칙</th>
                  <th style="padding: 0.5rem 0.6rem; width: 150px;">입력된 값</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="row in filteredValidationDetails" :key="row.rowNumber">
                  <!-- Valid Row -->
                  <tr v-if="row.violations.length === 0" style="border-bottom: 1px solid var(--va-background-border);">
                    <td style="padding: 0.45rem 0.6rem; text-align: center; font-weight: bold; font-family: monospace;">{{ row.rowNumber + 1 }}</td>
                    <td style="padding: 0.45rem 0.6rem; text-align: center;">
                      <va-badge text="통과" color="success" size="small" />
                    </td>
                    <td colspan="4" style="padding: 0.45rem 0.6rem; color: var(--va-text-secondary); font-style: italic;">
                      검칙 위반 없음 (정상 데이터)
                    </td>
                  </tr>
                  <!-- Invalid Row with Violations -->
                  <tr v-for="(v, vIdx) in row.violations" :key="`${row.rowNumber}-${vIdx}`" style="border-bottom: 1px solid var(--va-background-border); background: rgba(239, 68, 68, 0.03);">
                    <td v-if="vIdx === 0" :rowspan="row.violations.length" style="padding: 0.45rem 0.6rem; text-align: center; font-weight: bold; font-family: monospace; border-right: 1px solid var(--va-background-border); vertical-align: top;">
                      {{ row.rowNumber + 1 }}
                    </td>
                    <td v-if="vIdx === 0" :rowspan="row.violations.length" style="padding: 0.45rem 0.6rem; text-align: center; border-right: 1px solid var(--va-background-border); vertical-align: top;">
                      <va-badge text="오류" color="danger" size="small" />
                    </td>
                    <td style="padding: 0.45rem 0.6rem; font-weight: 600;">
                      <code>{{ v.fieldKey }}</code>
                    </td>
                    <td style="padding: 0.45rem 0.6rem; text-align: center;">
                      <va-badge :text="v.severity" :color="v.severity === 'ERROR' ? 'danger' : 'warning'" size="small" />
                    </td>
                    <td style="padding: 0.45rem 0.6rem; color: var(--va-danger); font-weight: 500;">
                      {{ getValidationMessage(v.message) }}
                    </td>
                    <td style="padding: 0.45rem 0.6rem; font-family: monospace; color: var(--va-text-secondary); word-break: break-all;">
                      {{ v.actualValue || '(빈 값)' }}
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </template>
      </div>

      <!-- Step 4: Uploading Execution -->
      <div v-else-if="step === 4" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 3rem 1.5rem; gap: 1.25rem; flex: 1;">
        <div style="width: 100%; max-width: 480px; display: flex; flex-direction: column; gap: 0.75rem;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 1rem; color: var(--va-text-primary);">
              {{ progress === 100 ? '업로드 완료!' : '데이터 일괄 반영 중...' }}
            </span>
            <span style="font-weight: 800; color: var(--va-primary); font-size: 1.1rem;">
              {{ Math.round(progress) }}%
            </span>
          </div>

          <va-progress-bar
            :model-value="progress"
            color="primary"
            size="large"
            animated
          />

          <p v-if="uploadError" style="color: var(--va-danger); font-size: 0.85rem; text-align: center;">
            ❌ {{ uploadError }}
          </p>
        </div>
      </div>

      <!-- Actions / Footer Buttons -->
      <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 0.5rem; border-top: 1px solid var(--va-background-border); margin-top: auto;">
        <div>
          <va-button
            v-if="step > 1 && step < 4 && !validating"
            preset="secondary"
            icon="arrow_back"
            @click="step--"
          >
            이전 단계
          </va-button>
        </div>

        <div style="display: flex; gap: 0.5rem;">
          <va-button
            v-if="step !== 4"
            preset="secondary"
            @click="$emit('close')"
          >
            {{ t('excel_uploader.btn_cancel') || '취소' }}
          </va-button>

          <!-- Step 2 -> Step 3 -->
          <va-button
            v-if="step === 2"
            color="primary"
            icon-right="arrow_forward"
            @click="runValidation"
          >
            {{ t('excel_uploader.btn_validate_upload') || '데이터 품질 검증 시작' }}
          </va-button>

          <!-- Step 3 Upload Action -->
          <va-button
            v-if="step === 3 && !validating && validationResult"
            :color="validationResult.invalidRows === 0 ? 'success' : 'warning'"
            icon="cloud_upload"
            @click="proceedUpload"
          >
            {{ validationResult.invalidRows === 0 ? (t('excel_uploader.btn_start_upload') || '전체 일괄 업로드 실행') : (t('excel_uploader.btn_upload_valid_only', { count: validationResult.validRows }) || `정상 데이터(${validationResult.validRows}건)만 업로드`) }}
          </va-button>

          <!-- Step 4 Finish Action -->
          <va-button
            v-if="step === 4 && progress === 100"
            color="success"
            icon="check"
            @click="$emit('close')"
          >
            {{ t('excel_uploader.btn_done') || '완료' }}
          </va-button>
        </div>
      </div>
    </div>
  </AppModal>
</template>

<script setup>
import { ref, computed } from 'vue';
import ExcelJS from 'exceljs';
import { saveAs } from 'file-saver';
import { useI18n } from 'vue-i18n';
import { useCookie } from '#app';
import { useCustomFetch } from '~/composables/useCustomFetch';

const { t, locale } = useI18n();

const props = defineProps({
  nodeId: { type: String, required: true },
  nodeFields: { type: Array, required: true },
  domainReferences: { type: Object, default: () => ({}) }
});

const emit = defineEmits(['close', 'uploaded']);

const userCookie = useCookie('user_data');

const { customFetch } = useCustomFetch();

const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value;
  }
  return null;
});


const stepsMeta = computed(() => [
  { step: 1, label: t('excel_uploader.step1_title') || '파일 선택', desc: t('excel_uploader.step1_desc') || '템플릿 및 파일 등록' },
  { step: 2, label: t('excel_uploader.step2_title') || '필드 매핑', desc: t('excel_uploader.step2_desc') || '엑셀 열과 스키마 연결' },
  { step: 3, label: t('excel_uploader.step3_title') || '품질 검증', desc: t('excel_uploader.step3_desc') || 'DQ 검칙 무결성 검사' },
  { step: 4, label: t('excel_uploader.step4_title') || '일괄 업로드', desc: t('excel_uploader.step4_desc') || '마스터 레코드 반영' }
]);

const step = ref(1);
const uploadErrorMsg = ref('');
const parsedData = ref([]);
const excelHeaders = ref([]);
const mapping = ref({}); // { fieldKey: excelHeaderName }
const progress = ref(0);
const uploadError = ref(null);

// Validation state
const validating = ref(false);
const validationResult = ref(null);
const showOnlyErrors = ref(true);

const handleDrop = (e) => {
  const files = e.dataTransfer?.files;
  if (files && files.length > 0) {
    handleFileUpload({ target: { files } });
  }
};

const getTranslatedName = (nameObj) => {

  if (!nameObj) return '';
  if (typeof nameObj === 'string') return nameObj;
  return nameObj[locale.value] || nameObj.ko || nameObj.en || '';
};

const downloadTemplate = async () => {
  const workbook = new ExcelJS.Workbook();
  const sheet = workbook.addWorksheet('Template');

  const headers = [];
  const validations = []; // { colIndex: 1, type: 'SELECT', options: ['A','B'] }
  const colWidths = [];

  let colIndex = 1;
  props.nodeFields.forEach(f => {
    if (f.type === 'CALCULATED') return;
    
    const fieldName = getTranslatedName(f.name);
    
    // Parse options for SELECT fields
    let parsedOpts = [];
    if (['SELECT', 'MULTI_SELECT'].includes(f.type) && f.options) {
      try {
        const arr = JSON.parse(f.options);
        parsedOpts = arr.map(a => {
          if (typeof a === 'object') {
            const labelStr = a.label ? getTranslatedName(a.label) : a.value;
            return labelStr || a.key || JSON.stringify(a);
          }
          return a;
        });
      } catch (e) {}
    }

    const agWidth = (f.tableColumnWidth && f.tableColumnWidth > 0) ? f.tableColumnWidth : null;
    const excelWidth = agWidth
      ? Math.max(15, Math.min(60, Math.round(agWidth / 7.5)))
      : ((f.gridWidth && f.gridWidth > 0) ? Math.max(15, f.gridWidth * 2.5) : 25);

    if (f.type === 'MULTILINGUAL') {
      headers.push(`${fieldName} (ko)`);
      headers.push(`${fieldName} (en)`);
      colWidths.push(excelWidth, excelWidth);
      colIndex += 2;
    } else {
      headers.push(fieldName);
      colWidths.push(excelWidth);
      if (parsedOpts.length > 0) {
        validations.push({ colIndex, options: parsedOpts });
      }
      colIndex++;
    }
  });
  
  // Set header row
  sheet.addRow(headers);
  sheet.getRow(1).font = { bold: true };
  sheet.getRow(1).fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFEEEEEE' } };

  // Convert column index (1-based) to letter (A, B, C...)
  const getColLetter = (idx) => {
    let temp, letter = '';
    while (idx > 0) {
      temp = (idx - 1) % 26;
      letter = String.fromCharCode(temp + 65) + letter;
      idx = (idx - temp - 1) / 26;
    }
    return letter;
  };

  // Apply Data Validations for up to 500 rows
  validations.forEach(val => {
    const colLetter = getColLetter(val.colIndex);
    // formula1 requires double quotes around comma separated string
    const formulaStr = '"' + val.options.join(',').replace(/"/g, '') + '"'; 
    sheet.dataValidations.add(`${colLetter}2:${colLetter}500`, {
      type: 'list',
      allowBlank: true,
      showErrorMessage: true,
      errorStyle: 'warning',
      errorTitle: 'Invalid Selection',
      error: 'Please select a value from the drop-down list.',
      formulae: [formulaStr]
    });
  });

  // Adjust column widths according to schema (gridWidth)
  sheet.columns.forEach((column, idx) => {
    column.width = colWidths[idx] || 25;
  });

  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  saveAs(blob, "Upload_Template.xlsx");
};

const handleFileUpload = (e) => {
  const file = e.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = async (evt) => {
    try {
      const arrayBuffer = evt.target.result;
      const wb = new ExcelJS.Workbook();
      await wb.xlsx.load(arrayBuffer);
      const ws = wb.worksheets[0];
      
      if (!ws || ws.rowCount < 2) {
        uploadErrorMsg.value = "The Excel file does not contain enough data.";
        return;
      }
      
      const headers = [];
      const firstRow = ws.getRow(1);
      firstRow.eachCell((cell, colNumber) => {
        headers[colNumber - 1] = cell.value ? String(cell.value) : '';
      });
      excelHeaders.value = headers.filter(Boolean);
      
      // Convert rows to array of objects based on header
      const rows = [];
      for (let i = 2; i <= ws.rowCount; i++) {
        const row = ws.getRow(i);
        if (!row.hasValues) continue;
        const obj = {};
        for (let j = 0; j < excelHeaders.value.length; j++) {
          const header = excelHeaders.value[j];
          let colIdx = -1;
          firstRow.eachCell((cell, colNumber) => {
            if (cell.value && String(cell.value) === header) {
              colIdx = colNumber;
            }
          });
          if (colIdx !== -1) {
            const cellVal = row.getCell(colIdx).value;
            obj[header] = cellVal !== null && cellVal !== undefined ? cellVal : '';
          } else {
            obj[header] = '';
          }
        }
        rows.push(obj);
      }
      
      parsedData.value = rows;
      
      // Auto-map based on exact translation match
      props.nodeFields.forEach(field => {
        const fieldName = getTranslatedName(field.name).toLowerCase();
        if (field.type === 'MULTILINGUAL') {
          const matchKo = excelHeaders.value.find(h => h && h.toLowerCase() === `${fieldName} (ko)`);
          const matchEn = excelHeaders.value.find(h => h && h.toLowerCase() === `${fieldName} (en)`);
          mapping.value[field.key + '_ko'] = matchKo || null;
          mapping.value[field.key + '_en'] = matchEn || null;
        } else {
          const match = excelHeaders.value.find(h => h && h.toLowerCase() === fieldName);
          mapping.value[field.key] = match || null;
        }
      });
      
      step.value = 2;
    } catch (err) {
      console.error(err);
      uploadErrorMsg.value = "Error parsing Excel file.";
    }
  };
  reader.readAsArrayBuffer(file);
};

/**
 * Excel 행을 RecordRequest 형태로 변환하는 헬퍼 함수.
 * 검증과 업로드 모두에서 재사용합니다.
 */
const transformRowToRequest = (row) => {
  const dataObj = {};
  props.nodeFields.forEach(field => {
    if (field.type === 'MULTILINGUAL') {
      const koHeader = mapping.value[field.key + '_ko'];
      const enHeader = mapping.value[field.key + '_en'];
      if (koHeader || enHeader) {
        dataObj[field.key] = {
          ko: koHeader ? String(row[koHeader] || '') : '',
          en: enHeader ? String(row[enHeader] || '') : ''
        };
      }
    } else if (field.type === 'DOMAIN_REFERENCE') {
      const excelHeader = mapping.value[field.key];
      if (excelHeader && row[excelHeader] !== undefined) {
        const val = String(row[excelHeader]);
        const refData = props.domainReferences[field.key];
        
        if (refData && val) {
          const targetFields = refData.fields || [];
          const idFieldId = refData.domainInfo?.identifierFieldId;
          const idFieldInfo = targetFields.find(f => f.id === idFieldId);
          
          const matchedRecord = (refData.records || []).find(r => {
            if (!r.data) return false;
            try {
              const parsed = JSON.parse(r.data);
              if (idFieldInfo && String(parsed[idFieldInfo.key]) === val) return true;
            } catch (e) {}
            return false;
          });
          
          dataObj[field.key] = matchedRecord ? matchedRecord.id : val;
        } else {
          dataObj[field.key] = val;
        }
      }
    } else {
      const excelHeader = mapping.value[field.key];
      if (excelHeader && row[excelHeader] !== undefined) {
        dataObj[field.key] = String(row[excelHeader]);
      }
    }
  });
  
  return {
    data: JSON.stringify(dataObj),
    requesterId: currentUser.value?.uuid || '123e4567-e89b-12d3-a456-426614174000',
    comment: 'Bulk upload via Excel'
  };
};

/**
 * 매핑 완료 후 batch-validate API를 호출하여 행 단위 사전 검증을 수행합니다.
 */
const runValidation = async () => {
  // 필수 필드 매핑 검증
  const missingReq = props.nodeFields.filter(f => {
    if (!f.required) return false;
    if (f.type === 'MULTILINGUAL') return !mapping.value[f.key + '_ko'];
    return !mapping.value[f.key];
  });
  
  if (missingReq.length > 0) {
    uploadErrorMsg.value = "Please map all required fields: " + missingReq.map(f => getTranslatedName(f.name)).join(', ');
    return;
  }

  step.value = 3;
  validating.value = true;
  validationResult.value = null;
  showOnlyErrors.value = true;

  try {
    const requests = parsedData.value.map(row => transformRowToRequest(row));

    const result = await customFetch(`/api/nodes/${props.nodeId}/records/batch-validate`, {
      method: 'POST',
      body: requests
    });

    validationResult.value = result;
  } catch (err) {
    console.error('Validation failed:', err);
    uploadErrorMsg.value = '검증 중 오류가 발생했습니다: ' + (err.data?.message || err.message || '');
    step.value = 2;
  } finally {
    validating.value = false;
  }
};

/**
 * 검증 통과한 행만 실제 업로드를 수행합니다.
 */
const proceedUpload = async () => {
  step.value = 4;
  progress.value = 10;
  uploadError.value = null;

  // 검증 결과에서 유효한 행만 필터링
  const validRowNumbers = new Set();
  if (validationResult.value && validationResult.value.details) {
    for (const detail of validationResult.value.details) {
      if (detail.valid) {
        validRowNumbers.add(detail.rowNumber - 1); // 0-based index
      }
    }
  } else {
    // 검증 결과 없으면 전체 업로드
    parsedData.value.forEach((_, idx) => validRowNumbers.add(idx));
  }

  const validRows = parsedData.value.filter((_, idx) => validRowNumbers.has(idx));

  if (validRows.length === 0) {
    uploadError.value = '업로드 가능한 유효한 행이 없습니다.';
    return;
  }

  try {
    const batchSize = 100;
    let uploadedCount = 0;

    for (let i = 0; i < validRows.length; i += batchSize) {
      const chunk = validRows.slice(i, i + batchSize);
      const requests = chunk.map(row => transformRowToRequest(row));

      await customFetch(`/api/nodes/${props.nodeId}/records/batch`, {
        method: 'POST',
        body: requests
      });

      uploadedCount += chunk.length;
      progress.value = 10 + ((uploadedCount / validRows.length) * 90);
    }
    
    progress.value = 100;
    setTimeout(() => { emit('uploaded'); }, 1000);
    
  } catch (err) {
    console.error(err);
    uploadError.value = "Upload failed: " + (err.data?.message || err.message || "An error occurred");
  }
};

const filteredValidationDetails = computed(() => {
  if (!validationResult.value?.details) return [];
  if (showOnlyErrors.value) {
    return validationResult.value.details.filter(d => !d.valid);
  }
  return validationResult.value.details;
});

const getValidationMessage = (msgMap) => {
  if (!msgMap) return '검증 규칙 위반';
  if (typeof msgMap === 'string') return msgMap;
  return msgMap[locale.value] || msgMap.ko || msgMap.en || Object.values(msgMap)[0] || '검증 규칙 위반';
};
</script>

<style scoped>
.mapping-select {
  width: 100%;
  padding: 0.4rem 0.6rem;
  font-size: 0.85rem;
  border: 1px solid var(--va-background-border);
  border-radius: 6px;
  background: var(--va-background-element);
  color: var(--va-text-primary);
  outline: none;
  transition: border-color 0.2s;
}

.mapping-select:focus {
  border-color: var(--va-primary);
}
</style>
