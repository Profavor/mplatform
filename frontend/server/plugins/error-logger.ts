export default defineNitroPlugin((nitroApp) => {
  nitroApp.hooks.hook('error', (error, { event }) => {
    console.error('NITRO ERROR:', error)
    if (error.cause) {
      console.error('CAUSE:', error.cause)
    }
  })
})
