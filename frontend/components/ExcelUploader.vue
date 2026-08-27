<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-3xl flex flex-col max-h-[90vh]">
      <!-- Header -->
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ t('excel_uploader.title') }}</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-500 focus:outline-none">
          <span class="text-2xl">&times;</span>
        </button>
      </div>

      <!-- Body -->
      <div class="p-6 overflow-y-auto flex-1">
        <div v-if="uploadErrorMsg" class="mb-4 p-3 bg-red-100 border border-red-300 text-red-700 rounded-md text-sm flex justify-between items-center">
          <span>{{ uploadErrorMsg }}</span>
          <button @click="uploadErrorMsg = ''" class="font-bold ml-2">&times;</button>
        </div>
        
        <!-- Step 1: File Upload -->
        <div v-if="step === 1" class="flex flex-col items-center justify-center border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-10 bg-gray-50 dark:bg-gray-900">
          
          <div class="mb-6 flex flex-col items-center">
            <p class="text-sm text-gray-500 mb-2">{{ t('excel_uploader.supported_formats') }}</p>
            <button @click="downloadTemplate" class="text-sm text-blue-600 hover:text-blue-800 underline flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
              {{ t('excel_uploader.download_template') }}
            </button>
          </div>

          <svg class="w-12 h-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path></svg>
          <p class="text-gray-700 dark:text-gray-300 mb-4 text-center">{{ t('excel_uploader.drag_drop_file') }}</p>
          <input type="file" ref="fileInput" accept=".xlsx, .xls, .csv" class="hidden" @change="handleFileUpload" />
          <button @click="$refs.fileInput.click()" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium transition-colors">
            {{ t('excel_uploader.selected_file') }}
          </button>
        </div>

        <!-- Step 2: Mapping -->
        <div v-else-if="step === 2" class="space-y-6">
          <div class="bg-blue-50 dark:bg-blue-900/30 p-4 rounded-md">
            <p class="text-sm text-blue-700 dark:text-blue-300">
              {{ t('excel_uploader.step2') }} ({{ parsedData.length }} rows)
            </p>
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead class="bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('excel_uploader.target_field') }}</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('excel_uploader.source_column') }}</th>
                </tr>
              </thead>
              <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                <tr v-for="field in nodeFields" :key="field.id">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white flex items-center gap-2">
                    {{ getTranslatedName(field.name) }}
                    <span v-if="field.type === 'MULTILINGUAL'" class="text-xs font-normal text-blue-500 bg-blue-100 px-2 py-0.5 rounded ml-2">(i18n)</span>
                    <span v-if="field.required" class="text-red-500">*</span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div v-if="field.type === 'MULTILINGUAL'" class="flex flex-col gap-2">
                      <div class="flex items-center gap-2">
                        <span class="text-xs font-bold w-6">ko:</span>
                        <select v-model="mapping[field.key + '_ko']" class="block w-full pl-3 pr-10 py-1 text-base border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md transition-colors">
                          <option :value="null">{{ t('excel_uploader.ignore_column') }}</option>
                          <option v-for="header in excelHeaders" :key="header" :value="header">{{ header }}</option>
                        </select>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-xs font-bold w-6">en:</span>
                        <select v-model="mapping[field.key + '_en']" class="block w-full pl-3 pr-10 py-1 text-base border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md transition-colors">
                          <option :value="null">{{ t('excel_uploader.ignore_column') }}</option>
                          <option v-for="header in excelHeaders" :key="header" :value="header">{{ header }}</option>
                        </select>
                      </div>
                    </div>
                    <select v-else v-model="mapping[field.key]" class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md transition-colors">
                      <option :value="null">{{ t('excel_uploader.ignore_column') }}</option>
                      <option v-for="header in excelHeaders" :key="header" :value="header">
                        {{ header }}
                      </option>
                    </select>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Step 3: Validation Report -->
        <div v-else-if="step === 3" class="space-y-4">
          <!-- Validating Spinner -->
          <div v-if="validating" class="flex flex-col items-center justify-center p-10 space-y-3">
            <div class="w-full bg-gray-200 rounded-full h-3 dark:bg-gray-700 overflow-hidden">
              <div class="bg-indigo-500 h-3 rounded-full animate-pulse" style="width: 60%"></div>
            </div>
            <p class="text-sm font-medium text-gray-600 dark:text-gray-300">{{ t('excel_uploader.row_validating') }}</p>
          </div>

          <!-- Validation Result -->
          <template v-else-if="validationResult">
            <!-- Summary Banner -->
            <div class="rounded-lg p-4 flex items-center gap-4"
                 :class="validationResult.invalidRows === 0
                   ? 'bg-green-50 dark:bg-green-900/30 border border-green-200 dark:border-green-800'
                   : 'bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800'">
              <div class="text-3xl">{{ validationResult.invalidRows === 0 ? '✅' : '⚠️' }}</div>
              <div>
                <p class="font-semibold text-sm" :class="validationResult.invalidRows === 0 ? 'text-green-800 dark:text-green-200' : 'text-red-800 dark:text-red-200'">
                  {{ validationResult.invalidRows === 0 ? t('excel_uploader.all_rows_valid') : t('excel_uploader.violations_found', { count: validationResult.invalidRows }) }}
                </p>
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  {{ t('excel_uploader.validation_summary', {
                    total: validationResult.totalRows,
                    valid: validationResult.validRows,
                    invalid: validationResult.invalidRows
                  }) }}
                </p>
              </div>
            </div>

            <!-- Filter Toggle -->
            <div v-if="validationResult.invalidRows > 0" class="flex items-center gap-2">
              <label class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300 cursor-pointer select-none">
                <input type="checkbox" v-model="showOnlyErrors" class="rounded border-gray-300 text-red-600 focus:ring-red-500" />
                {{ t('excel_uploader.show_only_errors') }}
              </label>
            </div>

            <!-- Violation Details Table -->
            <div v-if="filteredValidationDetails.length > 0" class="overflow-x-auto max-h-[45vh] overflow-y-auto border border-gray-200 dark:border-gray-700 rounded-lg">
              <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700 text-sm">
                <thead class="bg-gray-50 dark:bg-gray-800 sticky top-0 z-10">
                  <tr>
                    <th class="px-4 py-2.5 text-left font-semibold text-gray-600 dark:text-gray-300 w-16">{{ t('excel_uploader.col_row') }}</th>
                    <th class="px-4 py-2.5 text-left font-semibold text-gray-600 dark:text-gray-300 w-20">{{ t('excel_uploader.col_result') }}</th>
                    <th class="px-4 py-2.5 text-left font-semibold text-gray-600 dark:text-gray-300">{{ t('excel_uploader.col_violated_field') }}</th>
                    <th class="px-4 py-2.5 text-left font-semibold text-gray-600 dark:text-gray-300">{{ t('excel_uploader.col_severity') }}</th>
                    <th class="px-4 py-2.5 text-left font-semibold text-gray-600 dark:text-gray-300">{{ t('excel_uploader.col_violation_reason') }}</th>
                    <th class="px-4 py-2.5 text-left font-semibold text-gray-600 dark:text-gray-300">{{ t('excel_uploader.col_input_value') }}</th>
                  </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-100 dark:divide-gray-800">
                  <template v-for="row in filteredValidationDetails" :key="row.rowNumber">
                    <!-- Row with no violations -->
                    <tr v-if="row.violations.length === 0" class="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                      <td class="px-4 py-2 font-mono text-gray-700 dark:text-gray-300">{{ row.rowNumber + 1 }}</td>
                      <td class="px-4 py-2">
                        <span class="inline-flex items-center gap-1 text-green-600 dark:text-green-400 font-medium text-xs">
                          <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
                          통과
                        </span>
                      </td>
                      <td colspan="4" class="px-4 py-2 text-gray-400 dark:text-gray-500 italic">-</td>
                    </tr>
                    <!-- Row with violations: one <tr> per violation -->
                    <tr v-for="(v, vIdx) in row.violations" :key="`${row.rowNumber}-${vIdx}`" class="hover:bg-red-50/50 dark:hover:bg-red-900/10">
                      <td v-if="vIdx === 0" :rowspan="row.violations.length" class="px-4 py-2 font-mono text-gray-700 dark:text-gray-300 align-top border-r border-gray-100 dark:border-gray-800">
                        {{ row.rowNumber + 1 }}
                      </td>
                      <td v-if="vIdx === 0" :rowspan="row.violations.length" class="px-4 py-2 align-top border-r border-gray-100 dark:border-gray-800">
                        <span class="inline-flex items-center gap-1 text-red-600 dark:text-red-400 font-medium text-xs">
                          <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>
                          실패
                        </span>
                      </td>
                      <td class="px-4 py-2">
                        <code class="text-xs bg-gray-100 dark:bg-gray-700 px-1.5 py-0.5 rounded text-gray-800 dark:text-gray-200">{{ v.fieldKey }}</code>
                      </td>
                      <td class="px-4 py-2">
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full"
                              :class="v.severity === 'ERROR'
                                ? 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300'
                                : 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/50 dark:text-yellow-300'">
                          {{ v.severity }}
                        </span>
                      </td>
                      <td class="px-4 py-2 text-gray-600 dark:text-gray-400 text-xs">
                        {{ getValidationMessage(v.message) }}
                      </td>
                      <td class="px-4 py-2">
                        <code class="text-xs bg-gray-50 dark:bg-gray-800 px-1.5 py-0.5 rounded text-gray-600 dark:text-gray-400">
                          {{ v.actualValue || '(빈 값)' }}
                        </code>
                      </td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>
          </template>
        </div>

        <!-- Step 4: Upload Progress -->
        <div v-else-if="step === 4" class="flex flex-col items-center justify-center p-10 space-y-4">
          <div class="w-full bg-gray-200 rounded-full h-4 dark:bg-gray-700 mb-2 overflow-hidden relative">
            <div class="bg-blue-600 h-4 rounded-full transition-all duration-300" :style="{ width: progress + '%' }"></div>
          </div>
          <p class="text-lg font-medium text-gray-700 dark:text-gray-300">
            Processing... {{ Math.round(progress) }}%
          </p>
          <p v-if="uploadError" class="text-red-600 dark:text-red-400 mt-4">{{ uploadError }}</p>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-3 bg-gray-50 dark:bg-gray-800">
        <button v-if="step !== 4" @click="$emit('close')" class="px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-600 transition-colors">
          {{ t('excel_uploader.btn_cancel') }}
        </button>
        <button v-if="step === 2" @click="runValidation" class="px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors">
          {{ t('excel_uploader.btn_validate_upload') }}
        </button>
        <button v-if="step === 3 && !validating && validationResult" @click="step = 2" class="px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-600 transition-colors">
          {{ t('excel_uploader.btn_edit_mapping') }}
        </button>
        <button v-if="step === 3 && !validating && validationResult" @click="proceedUpload" class="px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white transition-colors"
                :class="validationResult.invalidRows === 0 ? 'bg-green-600 hover:bg-green-700' : 'bg-orange-500 hover:bg-orange-600'"
                :title="validationResult.invalidRows > 0 ? t('excel_uploader.tooltip_valid_only') : ''">
          {{ validationResult.invalidRows === 0 ? t('excel_uploader.btn_start_upload') : t('excel_uploader.btn_upload_valid_only', { count: validationResult.validRows }) }}
        </button>
        <button v-if="step === 4 && progress === 100" @click="$emit('close')" class="px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-colors">
          {{ t('excel_uploader.btn_done') }}
        </button>
      </div>
    </div>
  </div>
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
