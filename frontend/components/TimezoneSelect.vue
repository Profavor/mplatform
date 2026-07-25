<template>
  <va-select
    v-model="internalValue"
    :options="timezoneOptions"
    value-by="value"
    text-by="label"
    :label="label || $t('label_timezone') || 'Timezone'"
    :placeholder="placeholder || $t('placeholder_timezone') || 'Select timezone'"
    :outline="outline !== undefined ? outline : true"
    class="w-full"
    :tabindex="tabindex"
  >
    <template #prependInner>
      <slot name="icon">
        <va-icon name="schedule" color="secondary" />
      </slot>
    </template>
  </va-select>
</template>

<script lang="ts">
export const defaultTimezoneOptions = [
  { label: '[GMT+09:00] Asia/Seoul (Seoul)', value: 'Asia/Seoul' },
  { label: '[GMT+09:00] Asia/Tokyo (Tokyo)', value: 'Asia/Tokyo' },
  { label: '[GMT+08:00] Asia/Shanghai (Shanghai)', value: 'Asia/Shanghai' },
  { label: '[GMT+08:00] Asia/Hong Kong (Hong Kong)', value: 'Asia/Hong_Kong' },
  { label: '[GMT+08:00] Asia/Singapore (Singapore)', value: 'Asia/Singapore' },
  { label: '[GMT+05:30] Asia/Kolkata (Kolkata)', value: 'Asia/Kolkata' },
  { label: '[GMT+04:00] Asia/Dubai (Dubai)', value: 'Asia/Dubai' },
  { label: '[GMT+07:00] Asia/Jakarta (Jakarta)', value: 'Asia/Jakarta' },
  { label: '[GMT+00:00] Europe/London (London)', value: 'Europe/London' },
  { label: '[GMT+01:00] Europe/Paris (Paris)', value: 'Europe/Paris' },
  { label: '[GMT+01:00] Europe/Berlin (Berlin)', value: 'Europe/Berlin' },
  { label: '[GMT+03:00] Europe/Moscow (Moscow)', value: 'Europe/Moscow' },
  { label: '[GMT-05:00] America/New York (New York)', value: 'America/New_York' },
  { label: '[GMT-06:00] America/Chicago (Chicago)', value: 'America/Chicago' },
  { label: '[GMT-07:00] America/Denver (Denver)', value: 'America/Denver' },
  { label: '[GMT-08:00] America/Los Angeles (Los Angeles)', value: 'America/Los_Angeles' },
  { label: '[GMT-09:00] America/Anchorage (Anchorage)', value: 'America/Anchorage' },
  { label: '[GMT-10:00] America/Honolulu (Honolulu)', value: 'America/Honolulu' },
  { label: '[GMT-03:00] America/Sao Paulo (Sao Paulo)', value: 'America/Sao_Paulo' },
  { label: '[GMT+10:00] Australia/Sydney (Sydney)', value: 'Australia/Sydney' },
  { label: '[GMT+12:00] Pacific/Auckland (Auckland)', value: 'Pacific/Auckland' },
  { label: '[GMT+02:00] Africa/Cairo (Cairo)', value: 'Africa/Cairo' },
  { label: '[GMT+02:00] Africa/Johannesburg (Johannesburg)', value: 'Africa/Johannesburg' },
  { label: '[GMT+00:00] UTC (Coordinated Universal Time)', value: 'UTC' },
]
</script>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

const props = defineProps<{
  modelValue?: string
  label?: string
  placeholder?: string
  outline?: boolean
  tabindex?: number | string
  syncWithCookie?: boolean
}>()

const emit = defineEmits(['update:modelValue', 'change'])

const timezoneOptions = defaultTimezoneOptions

const internalValue = computed({
  get() {
    if (props.modelValue) return props.modelValue
    if (props.syncWithCookie) {
      try {
        const cookieTz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
        return cookieTz || 'Asia/Seoul'
      } catch {
        return 'Asia/Seoul'
      }
    }
    return 'Asia/Seoul'
  },
  set(val: string) {
    emit('update:modelValue', val)
    emit('change', val)
    if (props.syncWithCookie) {
      try {
        const cookieTz = useCookie('timezone', { default: () => 'Asia/Seoul' })
        cookieTz.value = val
      } catch {
        // Silent fallback
      }
    }
  },
})

onMounted(() => {
  if (props.syncWithCookie && !props.modelValue) {
    const cookieTz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
    if (cookieTz) {
      emit('update:modelValue', cookieTz)
    }
  }
})
</script>
