import { describe, it, expect } from 'vitest'
import { ref } from 'vue'

describe('Domain Save Payload Logic', () => {
  it('should correctly format identifierFieldId when null', () => {
    const newDomain = ref({
      identifierFieldId: null
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBeNull()
  })

  it('should correctly format identifierFieldId when empty string (vuestic clear)', () => {
    const newDomain = ref({
      identifierFieldId: ''
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBeNull()
  })

  it('should correctly format identifierFieldId when object is selected', () => {
    const newDomain = ref({
      identifierFieldId: { value: '123e4567-e89b-12d3-a456-426614174000', text: 'ID' }
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBe('123e4567-e89b-12d3-a456-426614174000')
  })

  it('should correctly format identifierFieldId when primitive string is bound by value-by', () => {
    const newDomain = ref({
      identifierFieldId: '123e4567-e89b-12d3-a456-426614174000'
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBe('123e4567-e89b-12d3-a456-426614174000')
  })
})
