<template>
  <!-- 도메인에 4가지 속성 필드(id/name/desc/image) 중 하나라도 설정된 경우 위젯 렌더링 -->
  <DomainRecordHeaderWidget
    v-if="hasAttributeFields"
    :domain="domain"
    :record-data="recordData"
    :fields="fields"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DomainRecordHeaderWidget from './DomainRecordHeaderWidget.vue'

const props = defineProps<{
  domain?: Record<string, any> | null
  recordData: Record<string, any>
  fields: Record<string, any>[]
}>()

/** 4가지 도메인 속성 필드 중 하나라도 설정된 경우 위젯 표시 */
const hasAttributeFields = computed(() =>
  !!(
    props.domain?.identifierFieldId ||
    props.domain?.displayNameFieldId ||
    props.domain?.descriptionFieldId ||
    props.domain?.imageFieldId
  )
)
</script>
