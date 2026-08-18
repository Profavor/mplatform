<template>
  <div class="inbox-page-wrapper">
    <InboxModal
      :model-value="true"
      @update:model-value="onModalClose"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useHead } from '#imports'
import { useI18n } from 'vue-i18n'
import InboxModal from '~/components/inbox/InboxModal.vue'

const { t } = useI18n()
const router = useRouter()

useHead({ title: t('inbox.title') })

const onModalClose = (val: boolean) => {
  if (!val) {
    if (window.history.length > 1) {
      router.back()
    } else {
      router.push('/')
    }
  }
}
</script>

<style scoped>
.inbox-page-wrapper {
  padding: 1rem;
}
</style>
