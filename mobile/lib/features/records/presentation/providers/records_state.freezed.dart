// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'records_state.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$RecordsState {
  List<DomainModel> get domains => throw _privateConstructorUsedError;
  String? get selectedDomainId => throw _privateConstructorUsedError;
  List<ClassificationNodeModel> get nodeTree =>
      throw _privateConstructorUsedError;
  String? get selectedNodeId => throw _privateConstructorUsedError;
  List<FieldDefinition> get fieldDefinitions =>
      throw _privateConstructorUsedError;
  List<RecordItem> get records => throw _privateConstructorUsedError;
  int get currentPage => throw _privateConstructorUsedError;
  int get pageSize => throw _privateConstructorUsedError;
  int get totalElements => throw _privateConstructorUsedError;
  int get totalPages => throw _privateConstructorUsedError;
  bool get isLoading => throw _privateConstructorUsedError;
  bool get isLoadingMore => throw _privateConstructorUsedError;
  String? get searchQuery => throw _privateConstructorUsedError;
  String? get errorMessage => throw _privateConstructorUsedError;

  /// Create a copy of RecordsState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecordsStateCopyWith<RecordsState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecordsStateCopyWith<$Res> {
  factory $RecordsStateCopyWith(
    RecordsState value,
    $Res Function(RecordsState) then,
  ) = _$RecordsStateCopyWithImpl<$Res, RecordsState>;
  @useResult
  $Res call({
    List<DomainModel> domains,
    String? selectedDomainId,
    List<ClassificationNodeModel> nodeTree,
    String? selectedNodeId,
    List<FieldDefinition> fieldDefinitions,
    List<RecordItem> records,
    int currentPage,
    int pageSize,
    int totalElements,
    int totalPages,
    bool isLoading,
    bool isLoadingMore,
    String? searchQuery,
    String? errorMessage,
  });
}

/// @nodoc
class _$RecordsStateCopyWithImpl<$Res, $Val extends RecordsState>
    implements $RecordsStateCopyWith<$Res> {
  _$RecordsStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecordsState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? domains = null,
    Object? selectedDomainId = freezed,
    Object? nodeTree = null,
    Object? selectedNodeId = freezed,
    Object? fieldDefinitions = null,
    Object? records = null,
    Object? currentPage = null,
    Object? pageSize = null,
    Object? totalElements = null,
    Object? totalPages = null,
    Object? isLoading = null,
    Object? isLoadingMore = null,
    Object? searchQuery = freezed,
    Object? errorMessage = freezed,
  }) {
    return _then(
      _value.copyWith(
            domains: null == domains
                ? _value.domains
                : domains // ignore: cast_nullable_to_non_nullable
                      as List<DomainModel>,
            selectedDomainId: freezed == selectedDomainId
                ? _value.selectedDomainId
                : selectedDomainId // ignore: cast_nullable_to_non_nullable
                      as String?,
            nodeTree: null == nodeTree
                ? _value.nodeTree
                : nodeTree // ignore: cast_nullable_to_non_nullable
                      as List<ClassificationNodeModel>,
            selectedNodeId: freezed == selectedNodeId
                ? _value.selectedNodeId
                : selectedNodeId // ignore: cast_nullable_to_non_nullable
                      as String?,
            fieldDefinitions: null == fieldDefinitions
                ? _value.fieldDefinitions
                : fieldDefinitions // ignore: cast_nullable_to_non_nullable
                      as List<FieldDefinition>,
            records: null == records
                ? _value.records
                : records // ignore: cast_nullable_to_non_nullable
                      as List<RecordItem>,
            currentPage: null == currentPage
                ? _value.currentPage
                : currentPage // ignore: cast_nullable_to_non_nullable
                      as int,
            pageSize: null == pageSize
                ? _value.pageSize
                : pageSize // ignore: cast_nullable_to_non_nullable
                      as int,
            totalElements: null == totalElements
                ? _value.totalElements
                : totalElements // ignore: cast_nullable_to_non_nullable
                      as int,
            totalPages: null == totalPages
                ? _value.totalPages
                : totalPages // ignore: cast_nullable_to_non_nullable
                      as int,
            isLoading: null == isLoading
                ? _value.isLoading
                : isLoading // ignore: cast_nullable_to_non_nullable
                      as bool,
            isLoadingMore: null == isLoadingMore
                ? _value.isLoadingMore
                : isLoadingMore // ignore: cast_nullable_to_non_nullable
                      as bool,
            searchQuery: freezed == searchQuery
                ? _value.searchQuery
                : searchQuery // ignore: cast_nullable_to_non_nullable
                      as String?,
            errorMessage: freezed == errorMessage
                ? _value.errorMessage
                : errorMessage // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$RecordsStateImplCopyWith<$Res>
    implements $RecordsStateCopyWith<$Res> {
  factory _$$RecordsStateImplCopyWith(
    _$RecordsStateImpl value,
    $Res Function(_$RecordsStateImpl) then,
  ) = __$$RecordsStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    List<DomainModel> domains,
    String? selectedDomainId,
    List<ClassificationNodeModel> nodeTree,
    String? selectedNodeId,
    List<FieldDefinition> fieldDefinitions,
    List<RecordItem> records,
    int currentPage,
    int pageSize,
    int totalElements,
    int totalPages,
    bool isLoading,
    bool isLoadingMore,
    String? searchQuery,
    String? errorMessage,
  });
}

/// @nodoc
class __$$RecordsStateImplCopyWithImpl<$Res>
    extends _$RecordsStateCopyWithImpl<$Res, _$RecordsStateImpl>
    implements _$$RecordsStateImplCopyWith<$Res> {
  __$$RecordsStateImplCopyWithImpl(
    _$RecordsStateImpl _value,
    $Res Function(_$RecordsStateImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of RecordsState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? domains = null,
    Object? selectedDomainId = freezed,
    Object? nodeTree = null,
    Object? selectedNodeId = freezed,
    Object? fieldDefinitions = null,
    Object? records = null,
    Object? currentPage = null,
    Object? pageSize = null,
    Object? totalElements = null,
    Object? totalPages = null,
    Object? isLoading = null,
    Object? isLoadingMore = null,
    Object? searchQuery = freezed,
    Object? errorMessage = freezed,
  }) {
    return _then(
      _$RecordsStateImpl(
        domains: null == domains
            ? _value._domains
            : domains // ignore: cast_nullable_to_non_nullable
                  as List<DomainModel>,
        selectedDomainId: freezed == selectedDomainId
            ? _value.selectedDomainId
            : selectedDomainId // ignore: cast_nullable_to_non_nullable
                  as String?,
        nodeTree: null == nodeTree
            ? _value._nodeTree
            : nodeTree // ignore: cast_nullable_to_non_nullable
                  as List<ClassificationNodeModel>,
        selectedNodeId: freezed == selectedNodeId
            ? _value.selectedNodeId
            : selectedNodeId // ignore: cast_nullable_to_non_nullable
                  as String?,
        fieldDefinitions: null == fieldDefinitions
            ? _value._fieldDefinitions
            : fieldDefinitions // ignore: cast_nullable_to_non_nullable
                  as List<FieldDefinition>,
        records: null == records
            ? _value._records
            : records // ignore: cast_nullable_to_non_nullable
                  as List<RecordItem>,
        currentPage: null == currentPage
            ? _value.currentPage
            : currentPage // ignore: cast_nullable_to_non_nullable
                  as int,
        pageSize: null == pageSize
            ? _value.pageSize
            : pageSize // ignore: cast_nullable_to_non_nullable
                  as int,
        totalElements: null == totalElements
            ? _value.totalElements
            : totalElements // ignore: cast_nullable_to_non_nullable
                  as int,
        totalPages: null == totalPages
            ? _value.totalPages
            : totalPages // ignore: cast_nullable_to_non_nullable
                  as int,
        isLoading: null == isLoading
            ? _value.isLoading
            : isLoading // ignore: cast_nullable_to_non_nullable
                  as bool,
        isLoadingMore: null == isLoadingMore
            ? _value.isLoadingMore
            : isLoadingMore // ignore: cast_nullable_to_non_nullable
                  as bool,
        searchQuery: freezed == searchQuery
            ? _value.searchQuery
            : searchQuery // ignore: cast_nullable_to_non_nullable
                  as String?,
        errorMessage: freezed == errorMessage
            ? _value.errorMessage
            : errorMessage // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$RecordsStateImpl implements _RecordsState {
  const _$RecordsStateImpl({
    final List<DomainModel> domains = const [],
    this.selectedDomainId,
    final List<ClassificationNodeModel> nodeTree = const [],
    this.selectedNodeId,
    final List<FieldDefinition> fieldDefinitions = const [],
    final List<RecordItem> records = const [],
    this.currentPage = 0,
    this.pageSize = 20,
    this.totalElements = 0,
    this.totalPages = 0,
    this.isLoading = false,
    this.isLoadingMore = false,
    this.searchQuery,
    this.errorMessage,
  }) : _domains = domains,
       _nodeTree = nodeTree,
       _fieldDefinitions = fieldDefinitions,
       _records = records;

  final List<DomainModel> _domains;
  @override
  @JsonKey()
  List<DomainModel> get domains {
    if (_domains is EqualUnmodifiableListView) return _domains;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_domains);
  }

  @override
  final String? selectedDomainId;
  final List<ClassificationNodeModel> _nodeTree;
  @override
  @JsonKey()
  List<ClassificationNodeModel> get nodeTree {
    if (_nodeTree is EqualUnmodifiableListView) return _nodeTree;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_nodeTree);
  }

  @override
  final String? selectedNodeId;
  final List<FieldDefinition> _fieldDefinitions;
  @override
  @JsonKey()
  List<FieldDefinition> get fieldDefinitions {
    if (_fieldDefinitions is EqualUnmodifiableListView)
      return _fieldDefinitions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_fieldDefinitions);
  }

  final List<RecordItem> _records;
  @override
  @JsonKey()
  List<RecordItem> get records {
    if (_records is EqualUnmodifiableListView) return _records;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_records);
  }

  @override
  @JsonKey()
  final int currentPage;
  @override
  @JsonKey()
  final int pageSize;
  @override
  @JsonKey()
  final int totalElements;
  @override
  @JsonKey()
  final int totalPages;
  @override
  @JsonKey()
  final bool isLoading;
  @override
  @JsonKey()
  final bool isLoadingMore;
  @override
  final String? searchQuery;
  @override
  final String? errorMessage;

  @override
  String toString() {
    return 'RecordsState(domains: $domains, selectedDomainId: $selectedDomainId, nodeTree: $nodeTree, selectedNodeId: $selectedNodeId, fieldDefinitions: $fieldDefinitions, records: $records, currentPage: $currentPage, pageSize: $pageSize, totalElements: $totalElements, totalPages: $totalPages, isLoading: $isLoading, isLoadingMore: $isLoadingMore, searchQuery: $searchQuery, errorMessage: $errorMessage)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecordsStateImpl &&
            const DeepCollectionEquality().equals(other._domains, _domains) &&
            (identical(other.selectedDomainId, selectedDomainId) ||
                other.selectedDomainId == selectedDomainId) &&
            const DeepCollectionEquality().equals(other._nodeTree, _nodeTree) &&
            (identical(other.selectedNodeId, selectedNodeId) ||
                other.selectedNodeId == selectedNodeId) &&
            const DeepCollectionEquality().equals(
              other._fieldDefinitions,
              _fieldDefinitions,
            ) &&
            const DeepCollectionEquality().equals(other._records, _records) &&
            (identical(other.currentPage, currentPage) ||
                other.currentPage == currentPage) &&
            (identical(other.pageSize, pageSize) ||
                other.pageSize == pageSize) &&
            (identical(other.totalElements, totalElements) ||
                other.totalElements == totalElements) &&
            (identical(other.totalPages, totalPages) ||
                other.totalPages == totalPages) &&
            (identical(other.isLoading, isLoading) ||
                other.isLoading == isLoading) &&
            (identical(other.isLoadingMore, isLoadingMore) ||
                other.isLoadingMore == isLoadingMore) &&
            (identical(other.searchQuery, searchQuery) ||
                other.searchQuery == searchQuery) &&
            (identical(other.errorMessage, errorMessage) ||
                other.errorMessage == errorMessage));
  }

  @override
  int get hashCode => Object.hash(
    runtimeType,
    const DeepCollectionEquality().hash(_domains),
    selectedDomainId,
    const DeepCollectionEquality().hash(_nodeTree),
    selectedNodeId,
    const DeepCollectionEquality().hash(_fieldDefinitions),
    const DeepCollectionEquality().hash(_records),
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    isLoading,
    isLoadingMore,
    searchQuery,
    errorMessage,
  );

  /// Create a copy of RecordsState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecordsStateImplCopyWith<_$RecordsStateImpl> get copyWith =>
      __$$RecordsStateImplCopyWithImpl<_$RecordsStateImpl>(this, _$identity);
}

abstract class _RecordsState implements RecordsState {
  const factory _RecordsState({
    final List<DomainModel> domains,
    final String? selectedDomainId,
    final List<ClassificationNodeModel> nodeTree,
    final String? selectedNodeId,
    final List<FieldDefinition> fieldDefinitions,
    final List<RecordItem> records,
    final int currentPage,
    final int pageSize,
    final int totalElements,
    final int totalPages,
    final bool isLoading,
    final bool isLoadingMore,
    final String? searchQuery,
    final String? errorMessage,
  }) = _$RecordsStateImpl;

  @override
  List<DomainModel> get domains;
  @override
  String? get selectedDomainId;
  @override
  List<ClassificationNodeModel> get nodeTree;
  @override
  String? get selectedNodeId;
  @override
  List<FieldDefinition> get fieldDefinitions;
  @override
  List<RecordItem> get records;
  @override
  int get currentPage;
  @override
  int get pageSize;
  @override
  int get totalElements;
  @override
  int get totalPages;
  @override
  bool get isLoading;
  @override
  bool get isLoadingMore;
  @override
  String? get searchQuery;
  @override
  String? get errorMessage;

  /// Create a copy of RecordsState
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecordsStateImplCopyWith<_$RecordsStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
