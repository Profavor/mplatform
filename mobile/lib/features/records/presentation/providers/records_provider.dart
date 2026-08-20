import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_state.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';

final recordsRepositoryProvider = Provider<RecordsRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return RecordsRepository(dio);
});

class RecordsController extends StateNotifier<RecordsState> {
  final RecordsRepository _repository;

  RecordsController(this._repository) : super(const RecordsState());

  Future<void> loadInitialData() async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final domains = await _repository.getDomains();
      if (domains.isEmpty) {
        state = state.copyWith(domains: [], isLoading: false);
        return;
      }
      
      final initialDomainId = domains.first.id;
      
      try {
        final fields = await _repository.getFieldDefinitions(initialDomainId);
        final tree = await _repository.getClassificationTree(initialDomainId);
        final searchFields = _getSearchFields(initialDomainId, fields);
        final pageRes = await _repository.getRecords(
          domainId: initialDomainId, 
          page: 0, 
          size: state.pageSize,
          searchFields: searchFields,
        );
        
        state = state.copyWith(
          domains: domains,
          selectedDomainId: initialDomainId,
          nodeTree: tree,
          selectedNodeId: null,
          fieldDefinitions: fields,
          records: pageRes.content,
          currentPage: pageRes.number,
          totalElements: pageRes.totalElements,
          totalPages: pageRes.totalPages,
          isLoading: false,
        );
      } catch (e, st) {
        print('Error loading fields or records: $e\n$st');
        // 도메인은 로드 성공했으나 필드나 레코드 로드 실패 시 도메인 목록은 유지
        state = state.copyWith(
          domains: domains,
          selectedDomainId: initialDomainId,
          isLoading: false,
          errorMessage: e.toString()
        );
      }
    } catch (e, st) {
      print('Error loading domains: $e\n$st');
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }

  Future<void> selectDomain(String domainId) async {
    if (state.selectedDomainId == domainId && state.records.isNotEmpty) return;

    state = state.copyWith(
      selectedDomainId: domainId,
      isLoading: true,
      currentPage: 0,
      records: [],
      errorMessage: null,
    );

    try {
      final fields = await _repository.getFieldDefinitions(domainId);
      final tree = await _repository.getClassificationTree(domainId);
      final searchFields = _getSearchFields(domainId, fields);
      final pageRes = await _repository.getRecords(
        domainId: domainId,
        nodeId: null,
        page: 0,
        size: state.pageSize,
        searchQuery: state.searchQuery,
        searchFields: searchFields,
      );

      state = state.copyWith(
        fieldDefinitions: fields,
        nodeTree: tree,
        selectedNodeId: null,
        records: pageRes.content,
        currentPage: pageRes.number,
        totalElements: pageRes.totalElements,
        totalPages: pageRes.totalPages,
        isLoading: false,
      );
    } catch (e, st) {
      print('Error in selectDomain: $e\n$st');
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }

  Future<void> search(String query) async {
    final domainId = state.selectedDomainId;
    if (domainId == null) return;

    state = state.copyWith(searchQuery: query, isLoading: true, currentPage: 0);

    try {
      final searchFields = _getSearchFields(domainId, state.fieldDefinitions);
      final pageRes = await _repository.getRecords(
        domainId: domainId,
        nodeId: state.selectedNodeId,
        page: 0,
        size: state.pageSize,
        searchQuery: query,
        searchFields: searchFields,
      );

      state = state.copyWith(
        records: pageRes.content,
        currentPage: pageRes.number,
        totalElements: pageRes.totalElements,
        totalPages: pageRes.totalPages,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }

  Future<void> loadNextPage() async {
    final domainId = state.selectedDomainId;
    if (domainId == null || state.isLoadingMore || state.currentPage + 1 >= state.totalPages) {
      return;
    }

    final nextPage = state.currentPage + 1;
    state = state.copyWith(isLoadingMore: true, errorMessage: null);

    try {
      final searchFields = _getSearchFields(domainId, state.fieldDefinitions);
      final pageRes = await _repository.getRecords(
        domainId: domainId,
        nodeId: state.selectedNodeId,
        page: nextPage,
        size: state.pageSize,
        searchQuery: state.searchQuery,
        searchFields: searchFields,
      );

      state = state.copyWith(
        records: [...state.records, ...pageRes.content],
        currentPage: pageRes.number,
        totalElements: pageRes.totalElements,
        totalPages: pageRes.totalPages,
        isLoadingMore: false,
      );
    } catch (e) {
      state = state.copyWith(isLoadingMore: false, errorMessage: e.toString());
    }
  }

  Future<void> selectNode(String? nodeId) async {
    final domainId = state.selectedDomainId;
    if (domainId == null || state.selectedNodeId == nodeId) return;

    state = state.copyWith(selectedNodeId: nodeId, isLoading: true, currentPage: 0);

    try {
      final searchFields = _getSearchFields(domainId, state.fieldDefinitions);
      final pageRes = await _repository.getRecords(
        domainId: domainId,
        nodeId: nodeId,
        page: 0,
        size: state.pageSize,
        searchQuery: state.searchQuery,
        searchFields: searchFields,
      );

      state = state.copyWith(
        records: pageRes.content,
        currentPage: pageRes.number,
        totalElements: pageRes.totalElements,
        totalPages: pageRes.totalPages,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }

  Future<void> refresh() async {
    final domainId = state.selectedDomainId;
    if (domainId == null) {
      await loadInitialData();
      return;
    }

    state = state.copyWith(isLoading: true, currentPage: 0, errorMessage: null);
    try {
      final fields = await _repository.getFieldDefinitions(domainId);
      final tree = await _repository.getClassificationTree(domainId);
      final searchFields = _getSearchFields(domainId, fields);
      final pageRes = await _repository.getRecords(
        domainId: domainId,
        nodeId: state.selectedNodeId,
        page: 0,
        size: state.pageSize,
        searchQuery: state.searchQuery,
        searchFields: searchFields,
      );

      state = state.copyWith(
        fieldDefinitions: fields,
        nodeTree: tree,
        records: pageRes.content,
        currentPage: pageRes.number,
        totalElements: pageRes.totalElements,
        totalPages: pageRes.totalPages,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }

  List<String> _getSearchFields(String domainId, List<FieldDefinition> fields) {
    if (state.domains.isEmpty) return [];
    final domain = state.domains.firstWhere((d) => d.id == domainId, orElse: () => state.domains.first);
    final searchFields = <String>[];
    
    if (domain.identifierFieldId != null) {
      final f = fields.where((f) => f.id == domain.identifierFieldId).firstOrNull;
      if (f != null) searchFields.add(f.fieldName);
    }
    if (domain.displayNameFieldId != null) {
      final f = fields.where((f) => f.id == domain.displayNameFieldId).firstOrNull;
      if (f != null) searchFields.add(f.fieldName);
    }
    if (domain.descriptionFieldId != null) {
      final f = fields.where((f) => f.id == domain.descriptionFieldId).firstOrNull;
      if (f != null) searchFields.add(f.fieldName);
    }
    return searchFields;
  }
}



final recordsControllerProvider = StateNotifierProvider<RecordsController, RecordsState>((ref) {
  final repo = ref.watch(recordsRepositoryProvider);
  return RecordsController(repo);
});
