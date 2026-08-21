import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/records/domain/models/domain_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/classification_node_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';
import 'package:mplatform_mobile/features/records/presentation/screens/record_detail_screen.dart';

class RecordsListScreen extends ConsumerStatefulWidget {
  const RecordsListScreen({super.key});

  @override
  ConsumerState<RecordsListScreen> createState() => _RecordsListScreenState();
}

class _RecordsListScreenState extends ConsumerState<RecordsListScreen> {
  final ScrollController _scrollController = ScrollController();
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        ref.read(recordsControllerProvider.notifier).loadInitialData();
      }
    });
    _scrollController.addListener(_onScroll);
  }

  void _onScroll() {
    if (mounted && _scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200) {
      ref.read(recordsControllerProvider.notifier).loadNextPage();
    }
  }

  List<ClassificationNodeModel> _flattenTree(List<ClassificationNodeModel> nodes) {
    final result = <ClassificationNodeModel>[];
    for (final node in nodes) {
      result.add(node);
      if (node.children.isNotEmpty) {
        result.addAll(_flattenTree(node.children));
      }
    }
    return result;
  }

  Map<String, String> _buildLocalizedPaths(List<ClassificationNodeModel> nodes, BuildContext context, [String parentPath = '']) {
    final Map<String, String> paths = {};
    for (final node in nodes) {
      final localizedName = L10nHelper.parseLocalizedMap(node.name, context);
      final currentPath = parentPath.isEmpty ? '/$localizedName' : '$parentPath/$localizedName';
      paths[node.id] = currentPath;
      if (node.children.isNotEmpty) {
        paths.addAll(_buildLocalizedPaths(node.children, context, currentPath));
      }
    }
    return paths;
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(recordsControllerProvider);
    final localizedPathsMap = _buildLocalizedPaths(state.nodeTree, context);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.masterDataRecordList),
        elevation: 2,
        backgroundColor: Colors.deepPurple,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: l10n.refresh,
            onPressed: () {
              ref.read(recordsControllerProvider.notifier).refresh();
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Domain Selector & Search Bar (100% Dynamic i18n & Schema)
          Container(
            padding: const EdgeInsets.all(12.0),
            color: Colors.grey[100],
            child: Column(
              children: [
                Row(
                  children: [
                    Text('${l10n.domain}: ', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    const SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonHideUnderline(
                        child: DropdownButton<String>(
                          isExpanded: true,
                          value: state.selectedDomainId,
                          hint: Text(l10n.domain),
                          items: state.domains.map((DomainModel d) {
                            return DropdownMenuItem<String>(
                              value: d.id,
                              child: Text(L10nHelper.parseLocalizedMap(d.name, context), style: const TextStyle(fontSize: 14)),
                            );
                          }).toList(),
                          onChanged: (String? newDomainId) {
                            if (newDomainId != null) {
                              ref.read(recordsControllerProvider.notifier).selectDomain(newDomainId);
                            }
                          },
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Text('${l10n.classification}: ', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    const SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonHideUnderline(
                        child: DropdownButton<String>(
                          isExpanded: true,
                          value: state.selectedNodeId,
                          hint: Text(l10n.allCategories, style: const TextStyle(fontSize: 14)),
                          items: [
                            DropdownMenuItem<String>(
                              value: null,
                              child: Text(l10n.allCategories, style: const TextStyle(fontSize: 14)),
                            ),
                            ..._flattenTree(state.nodeTree).map((ClassificationNodeModel node) {
                              final label = localizedPathsMap[node.id] ?? L10nHelper.parseLocalizedMap(node.name, context);
                              return DropdownMenuItem<String>(
                                value: node.id,
                                child: Text(label, style: const TextStyle(fontSize: 14), maxLines: 1, overflow: TextOverflow.ellipsis),
                              );
                            }),
                          ],
                          onChanged: (String? newNodeId) {
                            ref.read(recordsControllerProvider.notifier).selectNode(newNodeId);
                          },
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _searchController,
                  decoration: InputDecoration(
                    hintText: l10n.searchKeyword,
                    prefixIcon: const Icon(Icons.search, size: 20),
                    suffixIcon: _searchController.text.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear, size: 18),
                            onPressed: () {
                              _searchController.clear();
                              ref.read(recordsControllerProvider.notifier).search('');
                            },
                          )
                        : null,
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                    contentPadding: const EdgeInsets.symmetric(vertical: 0, horizontal: 12),
                  ),
                  onSubmitted: (val) {
                    ref.read(recordsControllerProvider.notifier).search(val);
                  },
                ),
              ],
            ),
          ),
          // Total Records Count Bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '${l10n.tableRecordCount}: ${state.totalElements}${l10n.recordsCountSuffix}',
                  style: TextStyle(fontSize: 13, color: Colors.grey[800], fontWeight: FontWeight.w600),
                ),
                if (state.isLoading)
                  const SizedBox(
                    width: 16,
                    height: 16,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
              ],
            ),
          ),
          const Divider(height: 1),
          // Dynamic Server-Side Pagination Records List View
          Expanded(
            child: RefreshIndicator(
              onRefresh: () async {
                ref.read(recordsControllerProvider.notifier).loadInitialData();
              },
              child: state.records.isEmpty && !state.isLoading
                  ? CustomScrollView(
                      physics: const AlwaysScrollableScrollPhysics(),
                      slivers: [
                        SliverFillRemaining(
                          child: Center(
                            child: Text(l10n.noDomainRecords, style: TextStyle(color: Colors.grey[600], fontSize: 16)),
                          ),
                        ),
                      ],
                    )
                  : ListView.builder(
                      controller: _scrollController,
                      physics: const AlwaysScrollableScrollPhysics(),
                      padding: const EdgeInsets.all(12),
                      itemCount: state.records.length + (state.isLoadingMore ? 1 : 0),
                      itemBuilder: (context, index) {
                      if (index == state.records.length) {
                        return const Padding(
                          padding: EdgeInsets.symmetric(vertical: 16),
                          child: Center(child: CircularProgressIndicator()),
                        );
                      }

                      final RecordItem record = state.records[index];
                      final domain = state.domains.firstWhere(
                        (d) => d.id == state.selectedDomainId,
                        orElse: () => state.domains.first,
                      );
                      
                      final String fallbackDisplayId = UuidFormatter.format(record.recordId, prefix: 'REC');
                      String displayId = fallbackDisplayId;
                      if (domain.identifierFieldId != null) {
                        for (final f in state.fieldDefinitions) {
                          if (f.id == domain.identifierFieldId) {
                            final val = record.attributes[f.fieldName];
                            if (val != null) {
                              if (val is Map) {
                                final parsed = L10nHelper.parseLocalizedMap(val, context);
                                if (parsed.isNotEmpty) displayId = parsed;
                              } else if (val.toString().trim().isNotEmpty) {
                                displayId = val.toString();
                              }
                            }
                            break;
                          }
                        }
                      }

                      return Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        elevation: 2,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        child: InkWell(
                          borderRadius: BorderRadius.circular(10),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => RecordDetailScreen(
                                  record: record,
                                  fieldDefinitions: state.fieldDefinitions,
                                  domain: domain,
                                ),
                              ),
                            );
                          },
                          child: Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Expanded(
                                      child: Row(
                                        children: [
                                          Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                            decoration: BoxDecoration(
                                              color: Colors.deepPurple[50],
                                              borderRadius: BorderRadius.circular(6),
                                              border: Border.all(color: Colors.deepPurple.shade200),
                                            ),
                                            child: Text(
                                              displayId,
                                              style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.deepPurple, fontSize: 12),
                                            ),
                                          ),
                                          if (record.nodePath.isNotEmpty || record.nodeName.isNotEmpty)
                                            Expanded(
                                              child: Container(
                                                margin: const EdgeInsets.only(left: 8),
                                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                                decoration: BoxDecoration(
                                                  color: Colors.grey[200],
                                                  borderRadius: BorderRadius.circular(4),
                                                ),
                                                child: Builder(builder: (ctx) {
                                                  final localizedNodePath = record.nodeId != null && localizedPathsMap.containsKey(record.nodeId)
                                                      ? localizedPathsMap[record.nodeId]!
                                                      : L10nHelper.parseLocalizedMap(record.nodePath.isNotEmpty ? record.nodePath : record.nodeName, context);
                                                  return Text(
                                                    localizedNodePath,
                                                    style: TextStyle(fontSize: 12, color: Colors.grey[800], fontWeight: FontWeight.w500),
                                                    maxLines: 1,
                                                    overflow: TextOverflow.ellipsis,
                                                  );
                                                }),
                                              ),
                                            ),
                                        ],
                                      ),
                                    ),
                                    if (record.createdAt != null)
                                      Padding(
                                        padding: const EdgeInsets.only(left: 8.0),
                                        child: Text(
                                          DateHelper.formatWithOffset(
                                            record.createdAt, 
                                            DateHelper.getTimezoneOffset(ref.read(sharedPreferencesProvider).getString('user_personal_timezone') ?? 'Asia/Seoul'), 
                                            pattern: 'yyyy-MM-dd HH:mm'
                                          ),
                                          style: TextStyle(fontSize: 11, color: Colors.grey[500]),
                                        ),
                                      ),
                                  ],
                                ),
                                const SizedBox(height: 12),
                                // 규칙: 하드코딩 금지. 도메인 스키마에 정의된 3대 핵심 속성만 추출
                                ...() {
                                  final displayFields = <FieldDefinition>[];
                                  final idField = state.fieldDefinitions.where((f) => f.id == domain.identifierFieldId).firstOrNull;
                                  final nameField = state.fieldDefinitions.where((f) => f.id == domain.displayNameFieldId).firstOrNull;
                                  final descField = state.fieldDefinitions.where((f) => f.id == domain.descriptionFieldId).firstOrNull;

                                  if (idField != null) displayFields.add(idField);
                                  if (nameField != null) displayFields.add(nameField);
                                  if (descField != null) displayFields.add(descField);

                                  // 3대 속성이 하나도 지정 안 된 경우 대비 (안전망)
                                  if (displayFields.isEmpty) {
                                    displayFields.addAll(state.fieldDefinitions.where((f) => f.showInList).take(3));
                                  }
                                  return displayFields.map((FieldDefinition f) {
                                    final dynamic val = record.attributes[f.fieldName];
                                    return Padding(
                                      padding: const EdgeInsets.only(bottom: 6.0),
                                      child: Row(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          SizedBox(
                                            width: 90,
                                            child: Text(
                                              L10nHelper.parseLocalizedMap(f.fieldLabel, context),
                                              style: TextStyle(fontSize: 13, color: Colors.grey[700], fontWeight: FontWeight.w500),
                                            ),
                                          ),
                                          Expanded(
                                            child: Text(
                                              val != null && val.toString().isNotEmpty ? L10nHelper.parseLocalizedMap(val, context) : '-',
                                              style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: Colors.black87),
                                            ),
                                          ),
                                        ],
                                      ),
                                    );
                                  });
                                }(),
                              ],
                            ),
                          ),
                        ),
                      );
                    },
                  ),
            ),
          ),
        ],
      ),
    );
  }
}
