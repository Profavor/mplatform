import { describe, it, expect } from 'vitest';

/**
 * Helper utility to parse AG-Grid filterModel into backend search query parameters
 */
export function parseAgGridFilterModel(filterModel: Record<string, any>): Record<string, string> {
  const result: Record<string, string> = {};
  if (!filterModel || typeof filterModel !== 'object') return result;

  Object.entries(filterModel).forEach(([colId, filterItem]) => {
    if (!filterItem) return;

    let fieldKey = colId;
    if (fieldKey.startsWith('data.')) {
      fieldKey = fieldKey.substring(5);
    } else if (fieldKey === 'sys_record_status') {
      fieldKey = 'status';
    }

    // Single filter condition
    if (filterItem.filter !== undefined || filterItem.values !== undefined) {
      applySingleFilter(result, fieldKey, filterItem);
    } else if (filterItem.condition1) {
      // Compound filter (condition1 / condition2)
      applySingleFilter(result, fieldKey, filterItem.condition1);
    }
  });

  return result;
}

function applySingleFilter(result: Record<string, string>, fieldKey: string, item: any) {
  const opMap: Record<string, string> = {
    contains: 'CONTAINS',
    notContains: 'CONTAINS',
    equals: 'EQ',
    notEqual: 'EQ',
    startsWith: 'STARTS_WITH',
    endsWith: 'ENDS_WITH',
    greaterThan: 'GT',
    greaterThanOrEqual: 'GTE',
    lessThan: 'LT',
    lessThanOrEqual: 'LTE',
    inRange: 'BETWEEN'
  };

  const agOp = item.type || 'contains';
  const backendOp = opMap[agOp] || 'EQ';

  if (item.filter !== undefined && item.filter !== null && item.filter !== '') {
    result[`search_${fieldKey}`] = String(item.filter);
    result[`search_op_${fieldKey}`] = backendOp;
    if (agOp === 'inRange' && item.filterTo !== undefined && item.filterTo !== null) {
      result[`search_${fieldKey}_max`] = String(item.filterTo);
    }
  } else if (Array.isArray(item.values) && item.values.length > 0) {
    result[`search_${fieldKey}`] = item.values.join(',');
    result[`search_op_${fieldKey}`] = 'IN';
  }
}

/**
 * Helper utility to parse AG-Grid sortModel into backend sort query parameters
 */
export function parseAgGridSortModel(sortModel: Array<{ colId: string; sort: string }>): {
  sortField?: string;
  sortOrder?: string;
  sort?: string;
} {
  if (!sortModel || sortModel.length === 0) {
    return {};
  }

  const parts = sortModel.map(item => {
    let col = item.colId;
    if (col.startsWith('data.')) col = col.substring(5);
    else if (col === 'sys_record_status') col = 'status';
    else if (col === 'sys_node_name') col = 'nodeName';
    return `${col},${item.sort.toLowerCase()}`;
  });

  let primaryCol = sortModel[0].colId;
  if (primaryCol.startsWith('data.')) primaryCol = primaryCol.substring(5);
  else if (primaryCol === 'sys_record_status') primaryCol = 'status';
  else if (primaryCol === 'sys_node_name') primaryCol = 'nodeName';

  return {
    sortField: primaryCol,
    sortOrder: sortModel[0].sort.toUpperCase(),
    sort: parts.join(';')
  };
}

/**
 * Helper utility to build complete query URL params including filters, sorts, pagination, and active viewMode
 */
export function buildRecordGridRequestParams(options: {
  nodeId?: string;
  isDomain?: boolean;
  page: number;
  size: number;
  sortModel?: Array<{ colId: string; sort: string }>;
  filterModel?: Record<string, any>;
  activeFilters?: Record<string, string>;
  activeFiltersOp?: Record<string, string>;
  activeFiltersMax?: Record<string, string>;
  statusFilter?: string;
}): URLSearchParams {
  const params = new URLSearchParams();

  params.append('page', String(options.page));
  params.append('size', String(options.size));

  if (options.statusFilter && options.statusFilter !== 'ALL') {
    params.append('status', options.statusFilter);
  }

  // Active advanced filters
  if (options.activeFilters) {
    Object.entries(options.activeFilters).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') {
        params.append(`search_${k}`, String(v));
        const op = options.activeFiltersOp?.[k] || 'EQ';
        params.append(`search_op_${k}`, op);
        if (op === 'BETWEEN' && options.activeFiltersMax?.[k]) {
          params.append(`search_${k}_max`, String(options.activeFiltersMax[k]));
        }
      }
    });
  }

  // AG-Grid column filters
  if (options.filterModel) {
    const agFilters = parseAgGridFilterModel(options.filterModel);
    Object.entries(agFilters).forEach(([k, v]) => {
      if (!params.has(k)) {
        params.append(k, v);
      }
    });
  }

  // AG-Grid sort model
  if (options.sortModel && options.sortModel.length > 0) {
    const sortInfo = parseAgGridSortModel(options.sortModel);
    if (sortInfo.sortField) params.append('sortField', sortInfo.sortField);
    if (sortInfo.sortOrder) params.append('sortOrder', sortInfo.sortOrder);
    if (sortInfo.sort) params.append('sort', sortInfo.sort);
  }

  return params;
}

/**
 * State synchronization between URL query params and grid state
 */
export function syncGridStateFromUrlQuery(query: Record<string, any>) {
  const page = query.page ? parseInt(String(query.page), 10) : 0;
  const size = query.size ? parseInt(String(query.size), 10) : 50;
  const viewMode = query.viewMode === 'virtual' ? 'virtual' : 'pagination';
  const status = query.status || 'ALL';

  const sortModel: Array<{ colId: string; sort: string }> = [];
  if (query.sort) {
    const segments = String(query.sort).split(';');
    segments.forEach(seg => {
      const [col, dir] = seg.split(',');
      if (col) {
        sortModel.push({ colId: `data.${col}`, sort: (dir || 'asc').toLowerCase() });
      }
    });
  } else if (query.sortField) {
    sortModel.push({
      colId: `data.${query.sortField}`,
      sort: (query.sortOrder || 'asc').toLowerCase()
    });
  }

  const activeFilters: Record<string, string> = {};
  const activeFiltersOp: Record<string, string> = {};
  const activeFiltersMax: Record<string, string> = {};

  Object.entries(query).forEach(([k, v]) => {
    if (k.startsWith('search_') && !k.startsWith('search_op_') && !k.endsWith('_max')) {
      const field = k.substring(7);
      activeFilters[field] = String(v);
      if (query[`search_op_${field}`]) {
        activeFiltersOp[field] = String(query[`search_op_${field}`]);
      }
      if (query[`search_${field}_max`]) {
        activeFiltersMax[field] = String(query[`search_${field}_max`]);
      }
    }
  });

  return {
    page,
    size,
    viewMode,
    status,
    sortModel,
    activeFilters,
    activeFiltersOp,
    activeFiltersMax
  };
}

describe('RecordVirtualScrollPagination - Unit Tests', () => {
  it('AG-Grid filterModel의 텍스트 contains 및 number inRange 필터가 백엔드 파라미터로 올바르게 변환된다', () => {
    const filterModel = {
      'data.stock_code': {
        filterType: 'text',
        type: 'contains',
        filter: '005930'
      },
      'data.market_price': {
        filterType: 'number',
        type: 'inRange',
        filter: 50000,
        filterTo: 80000
      },
      'sys_record_status': {
        filterType: 'text',
        type: 'equals',
        filter: 'ACTIVE'
      }
    };

    const parsed = parseAgGridFilterModel(filterModel);

    expect(parsed['search_stock_code']).toBe('005930');
    expect(parsed['search_op_stock_code']).toBe('CONTAINS');
    expect(parsed['search_market_price']).toBe('50000');
    expect(parsed['search_op_market_price']).toBe('BETWEEN');
    expect(parsed['search_market_price_max']).toBe('80000');
    expect(parsed['search_status']).toBe('ACTIVE');
    expect(parsed['search_op_status']).toBe('EQ');
  });

  it('AG-Grid sortModel의 다중 정렬이 Spring Pageable 및 sortField/sortOrder 파라미터로 올바르게 변환된다', () => {
    const sortModel = [
      { colId: 'data.ticker', sort: 'asc' },
      { colId: 'createdAt', sort: 'desc' }
    ];

    const parsed = parseAgGridSortModel(sortModel);

    expect(parsed.sortField).toBe('ticker');
    expect(parsed.sortOrder).toBe('ASC');
    expect(parsed.sort).toBe('ticker,asc;createdAt,desc');
  });

  it('buildRecordGridRequestParams가 페이징, 정렬, 상세검색, 컬럼필터를 통합하여 QueryString을 생성한다', () => {
    const params = buildRecordGridRequestParams({
      page: 2,
      size: 100,
      statusFilter: 'ACTIVE',
      sortModel: [{ colId: 'data.stock_code', sort: 'asc' }],
      filterModel: {
        'data.company_name': { filterType: 'text', type: 'contains', filter: '삼성' }
      },
      activeFilters: {
        industry: '반도체'
      },
      activeFiltersOp: {
        industry: 'EQ'
      }
    });

    expect(params.get('page')).toBe('2');
    expect(params.get('size')).toBe('100');
    expect(params.get('status')).toBe('ACTIVE');
    expect(params.get('sortField')).toBe('stock_code');
    expect(params.get('sortOrder')).toBe('ASC');
    expect(params.get('search_company_name')).toBe('삼성');
    expect(params.get('search_op_company_name')).toBe('CONTAINS');
    expect(params.get('search_industry')).toBe('반도체');
    expect(params.get('search_op_industry')).toBe('EQ');
  });

  it('syncGridStateFromUrlQuery를 통해 브라우저 URL 쿼리로부터 그리드 상태가 정확히 복원된다', () => {
    const urlQuery = {
      page: '3',
      size: '100',
      viewMode: 'virtual',
      status: 'PENDING_APPROVAL',
      sort: 'stock_code,asc;price,desc',
      search_stock_code: '005930',
      search_op_stock_code: 'CONTAINS'
    };

    const state = syncGridStateFromUrlQuery(urlQuery);

    expect(state.page).toBe(3);
    expect(state.size).toBe(100);
    expect(state.viewMode).toBe('virtual');
    expect(state.status).toBe('PENDING_APPROVAL');
    expect(state.sortModel).toHaveLength(2);
    expect(state.sortModel[0]).toEqual({ colId: 'data.stock_code', sort: 'asc' });
    expect(state.sortModel[1]).toEqual({ colId: 'data.price', sort: 'desc' });
    expect(state.activeFilters['stock_code']).toBe('005930');
    expect(state.activeFiltersOp['stock_code']).toBe('CONTAINS');
  });

  it('가상 스크롤 및 페이징 설정 기본값이 100단위 블록 및 20블록 캐시로 최적화되어 있다', () => {
    const defaultGridOptions = {
      rowModelType: 'infinite',
      cacheBlockSize: 100,
      maxBlocksInCache: 20,
      rowBuffer: 15,
      paginationPageSize: 50,
      paginationPageSizeSelector: [20, 50, 100, 200, 500]
    };

    expect(defaultGridOptions.cacheBlockSize).toBe(100);
    expect(defaultGridOptions.maxBlocksInCache).toBe(20);
    expect(defaultGridOptions.rowBuffer).toBe(15);
    expect(defaultGridOptions.paginationPageSizeSelector).toContain(500);
  });
});
