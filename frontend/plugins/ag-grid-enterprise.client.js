import { AllEnterpriseModule, LicenseManager, ModuleRegistry } from 'ag-grid-enterprise'
import { AllCommunityModule } from 'ag-grid-community'

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  const licenseKey = config?.public?.agGridLicense

  if (licenseKey && typeof licenseKey === 'string' && licenseKey.trim().length > 0) {
    try {
      LicenseManager.setLicenseKey(licenseKey.trim())
      ModuleRegistry.registerModules([AllEnterpriseModule])
    } catch (e) {
      console.warn('Failed to initialize AG Grid Enterprise license, falling back to community:', e)
      ModuleRegistry.registerModules([AllCommunityModule])
    }
  } else {
    // When no license key is configured, register Community modules to prevent trial license warnings (#95)
    ModuleRegistry.registerModules([AllCommunityModule])
  }
})
