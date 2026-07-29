<template>
  <div style="padding: 2rem; text-align: center;">
    <va-progress-circle indeterminate color="primary" />
  </div>
</template>

<script setup>
definePageMeta({
  redirect: '/admin/integration/channels'
})

onMounted(() => {
  navigateTo('/admin/integration/channels', { replace: true })
})
</script>
