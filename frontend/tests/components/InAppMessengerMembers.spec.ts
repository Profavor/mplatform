import { describe, it, expect } from 'vitest'

describe('InAppMessenger Member Identification & Safe Display Logic (TDD)', () => {
  // 1. isMe 판별 로직 테스트
  const checkIsMe = (member: any, myId: string, myName: string, jwtSub: string) => {
    if (!member) return false
    const mId = String(member.userId || member.id || '')
    const mUsername = String(member.username || '')

    if (myId && mId === myId) return true
    if (myName && (mUsername === myName || mId === myName)) return true
    if (jwtSub && (mId === jwtSub || mUsername === jwtSub)) return true
    return false
  }

  // 2. isCreator 판별 로직 테스트
  const checkIsCreator = (member: any, roomCreatedBy: string, isRoomCreator: boolean, isMeResult: boolean) => {
    if (!roomCreatedBy || !member) return false
    const cId = String(roomCreatedBy)
    const mId = String(member.userId || member.id || '')
    const mUsername = String(member.username || '')

    if (cId === mId) return true
    if (cId === mUsername) return true
    if (isRoomCreator && isMeResult) return true
    return false
  }

  // 3. Raw UUID 노출 방지 포맷터 테스트
  const formatMemberDisplay = (member: any, availableUsers: any[] = []) => {
    if (!member) return ''
    const rawUsername = member.username || member.userId || ''
    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(rawUsername)
    
    if (isUuid) {
      const matched = availableUsers.find(u => u.id === rawUsername || u.uuid === rawUsername)
      if (matched && matched.username) {
        return matched.username
      }
      return `USER-${rawUsername.substring(0, 8)}`
    }
    return rawUsername
  }

  it('Keycloak UUID 또는 DB User ID/Username 중 어느 것으로 전달되어도 isMe가 정상 판별된다', () => {
    const myId = 'local-db-user-id-9999'
    const myName = 'superadmin'
    const jwtSub = 'b1c33d81-4d26-4706-8c19-3b989e191e5b'

    // 1. member.username이 superadmin인 경우
    expect(checkIsMe({ userId: 'local-db-user-id-9999', username: 'superadmin' }, myId, myName, jwtSub)).toBe(true)

    // 2. member.userId가 Keycloak UUID인 경우
    expect(checkIsMe({ userId: 'b1c33d81-4d26-4706-8c19-3b989e191e5b', username: 'superadmin' }, myId, myName, jwtSub)).toBe(true)

    // 3. 다른 사용자
    expect(checkIsMe({ userId: 'other-id', username: 'testuser' }, myId, myName, jwtSub)).toBe(false)
  })

  it('방 생성자(createdBy)가 UUID 또는 username인 경우에도 isCreator가 정상 판별된다', () => {
    const roomCreatedBy = 'b1c33d81-4d26-4706-8c19-3b989e191e5b'

    // 방장 본인
    const memberMe = { userId: 'local-db-user-id-9999', username: 'superadmin' }
    expect(checkIsCreator(memberMe, roomCreatedBy, true, true)).toBe(true)

    // 일반 멤버
    const memberOther = { userId: 'other-user-id', username: 'guest' }
    expect(checkIsCreator(memberOther, roomCreatedBy, true, false)).toBe(false)
  })

  it('사용자 목록에 Raw UUID가 절대 노출되지 않고 식별 명칭 또는 매핑된 이름으로 표출된다', () => {
    const availableUsers = [
      { id: 'b1c33d81-4d26-4706-8c19-3b989e191e5b', username: 'superadmin' }
    ]

    // 1. 매핑된 사용자가 있는 UUID
    const display1 = formatMemberDisplay({ userId: 'b1c33d81-4d26-4706-8c19-3b989e191e5b', username: 'b1c33d81-4d26-4706-8c19-3b989e191e5b' }, availableUsers)
    expect(display1).toBe('superadmin')

    // 2. 매핑되지 않은 임의의 UUID인 경우 안전한 식별 명칭(USER-xxxx)으로 변환
    const display2 = formatMemberDisplay({ userId: 'a1a2a3a4-1111-2222-3333-444455556666', username: 'a1a2a3a4-1111-2222-3333-444455556666' }, [])
    expect(display2).toBe('USER-a1a2a3a4')
    expect(display2).not.toContain('a1a2a3a4-1111-2222-3333-444455556666')

    // 3. 정상 사용자명인 경우 그대로 반환
    const display3 = formatMemberDisplay({ userId: 'user-01', username: 'honggildong' }, availableUsers)
    expect(display3).toBe('honggildong')
  })
})
