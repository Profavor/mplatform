/**
 * AG-Grid 다국어 (ko / en) localeText 매핑 헬퍼
 */

export const AG_GRID_LOCALE_KO: Record<string, string> = {
  // Pagination
  page: '페이지',
  more: '더보기',
  to: '~',
  of: '/',
  next: '다음',
  last: '마지막',
  first: '처음',
  previous: '이전',
  pageSizeSelectorLabel: '페이지 당 건수:',

  // Loading & Empty States
  loadingOoo: '데이터를 불러오는 중입니다...',
  noRowsToShow: '조회된 레코드가 없습니다.',

  // Selection & Checkbox
  selectAll: '전체 선택',
  selectAllSearchResults: '검색 결과 전체 선택',
  searchOoo: '검색...',
  blanks: '(빈값)',
  blank: '빈값',
  notBlank: '값 있음',

  // Filters
  filterOoo: '필터링...',
  applyFilter: '필터 적용',
  equals: '같음 (=)',
  notEqual: '같지 않음 (!=)',
  contains: '포함',
  notContains: '포함하지 않음',
  startsWith: '시작 문자',
  endsWith: '끝 문자',
  andCondition: '그리고 (AND)',
  orCondition: '또는 (OR)',
  clearFilter: '필터 초기화',
  resetFilter: '필터 리셋',

  // Column Menu
  pinColumn: '열 고정',
  pinLeft: '왼쪽 고정',
  pinRight: '오른쪽 고정',
  noPin: '고정 해제',
  autosizeThiscolumn: '이 열 너비 자동 맞춤',
  autosizeAllColumns: '모든 열 너비 자동 맞춤',
  resetColumns: '열 설정 초기화',
  columns: '컬럼 목록',
  filters: '필터 목록',

  // Sorting
  sortAscending: '오름차순 정렬',
  sortDescending: '내림차순 정렬',
  sortNone: '정렬 해제'
}

export const AG_GRID_LOCALE_EN: Record<string, string> = {
  // Pagination
  page: 'Page',
  more: 'More',
  to: 'to',
  of: 'of',
  next: 'Next',
  last: 'Last',
  first: 'First',
  previous: 'Previous',
  pageSizeSelectorLabel: 'Page Size:',

  // Loading & Empty States
  loadingOoo: 'Loading data...',
  noRowsToShow: 'No records to display.',

  // Selection & Checkbox
  selectAll: 'Select All',
  selectAllSearchResults: 'Select All Search Results',
  searchOoo: 'Search...',
  blanks: '(Blanks)',
  blank: 'Blank',
  notBlank: 'Not Blank',

  // Filters
  filterOoo: 'Filter...',
  applyFilter: 'Apply Filter',
  equals: 'Equals',
  notEqual: 'Not Equal',
  contains: 'Contains',
  notContains: 'Does not contain',
  startsWith: 'Starts with',
  endsWith: 'Ends with',
  andCondition: 'AND',
  orCondition: 'OR',
  clearFilter: 'Clear Filter',
  resetFilter: 'Reset Filter',

  // Column Menu
  pinColumn: 'Pin Column',
  pinLeft: 'Pin Left',
  pinRight: 'Pin Right',
  noPin: 'No Pin',
  autosizeThiscolumn: 'Autosize This Column',
  autosizeAllColumns: 'Autosize All Columns',
  resetColumns: 'Reset Columns',
  columns: 'Columns',
  filters: 'Filters',

  // Sorting
  sortAscending: 'Sort Ascending',
  sortDescending: 'Sort Descending',
  sortNone: 'Clear Sort'
}

export const getAgGridLocaleText = (locale: string): Record<string, string> => {
  return locale === 'en' ? AG_GRID_LOCALE_EN : AG_GRID_LOCALE_KO
}
