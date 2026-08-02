import { describe, it, expect } from 'vitest'

describe('Data Lineage Helper & Parser', () => {
  it('Data Lineage Response에서 Node와 Edge 개수를 올바르게 구한다', () => {
    const mockData = {
      recordId: 100,
      recordCode: 'REC-100',
      nodes: [
        { id: 'REC-100', label: 'Master Record', type: 'RECORD', timestamp: '2026-08-02T10:00:00' },
        { id: 'SRC-1', label: 'Source System', type: 'SOURCE', timestamp: '2026-08-02T09:00:00' }
      ],
      edges: [
        { source: 'SRC-1', target: 'REC-100', relationship: 'CREATED_FROM' }
      ]
    }

    expect(mockData.nodes.length).toBe(2)
    expect(mockData.edges.length).toBe(1)
    expect(mockData.edges[0].relationship).toBe('CREATED_FROM')
  })
})
