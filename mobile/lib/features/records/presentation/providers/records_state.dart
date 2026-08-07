import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mplatform_mobile/features/records/domain/models/classification_node_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/domain_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';

part 'records_state.freezed.dart';

@freezed
class RecordsState with _$RecordsState {
  const factory RecordsState({
    @Default([]) List<DomainModel> domains,
    String? selectedDomainId,
    @Default([]) List<ClassificationNodeModel> nodeTree,
    String? selectedNodeId,
    @Default([]) List<FieldDefinition> fieldDefinitions,
    @Default([]) List<RecordItem> records,
    @Default(0) int currentPage,
    @Default(20) int pageSize,
    @Default(0) int totalElements,
    @Default(0) int totalPages,
    @Default(false) bool isLoading,
    @Default(false) bool isLoadingMore,
    String? searchQuery,
    String? errorMessage,
  }) = _RecordsState;
}
