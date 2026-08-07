import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';

class ApprovalDetailScreen extends ConsumerStatefulWidget {
  final ApprovalItem item;

  const ApprovalDetailScreen({super.key, required this.item});

  @override
  ConsumerState<ApprovalDetailScreen> createState() => _ApprovalDetailScreenState();
}

class _ApprovalDetailScreenState extends ConsumerState<ApprovalDetailScreen> {
  List<FieldDefinition>? _fieldDefs;
  int _selectedSectorIndex = 0;
  bool _isLoadingFields = false;
  ApprovalItem? _detailedItem;
  bool _isLoadingDetail = true;

  @override
  void initState() {
    super.initState();
    _fetchDetail();
  }

  Future<void> _fetchDetail() async {
    final repo = ref.read(approvalsRepositoryProvider);
    final detailed = await repo.getApprovalDetail(widget.item.approvalId);
    if (mounted) {
      setState(() {
        _detailedItem = detailed;
        _isLoadingDetail = false;
      });
      _fetchFieldDefinitions(detailed ?? widget.item);
    }
  }

  Future<void> _fetchFieldDefinitions(ApprovalItem itemToUse) async {
    if (itemToUse.domainId == null) return;
    setState(() => _isLoadingFields = true);
    
    try {
      final repo = ref.read(recordsRepositoryProvider);
      final fields = await repo.getFieldDefinitions(itemToUse.domainId!);
      
      if (mounted) {
        setState(() {
          _fieldDefs = fields;
          _isLoadingFields = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoadingFields = false);
      }
    }
  }

  ApprovalItem get _currentItem => _detailedItem ?? widget.item;

  @override
  Widget build(BuildContext context) {
    final offset = DateHelper.getTimezoneOffset(ref.read(sharedPreferencesProvider).getString('user_personal_timezone') ?? 'Asia/Seoul');
    return Scaffold(
      backgroundColor: Colors.blueGrey[50],
      appBar: AppBar(
        title: const Text('결재 내역 상세', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        iconTheme: const IconThemeData(color: Colors.black87),
        actions: [
          IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.of(context).pop()),
        ],
      ),
      body: _isLoadingDetail || _isLoadingFields 
        ? const Center(child: CircularProgressIndicator()) 
        : SingleChildScrollView(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildDataSection(context),
                const SizedBox(height: 20),
                _buildTimelineSummary(context, offset),
                const SizedBox(height: 20),
                _buildDetailedStatus(context, offset),
              ],
            ),
          ),
    );
  }

  Widget _buildDataSection(BuildContext context) {
    // 1. Group payload fields by Sector -> Group
    final Map<String, Map<String, List<MapEntry<FieldDefinition, Map<String, dynamic>>>>> groupedData = {};
    const String defaultSector = '일반정보';
    const String defaultGroup = '키 정보';

    final beforeMap = _currentItem.payload['before'] is Map ? _currentItem.payload['before'] as Map<String, dynamic> : <String, dynamic>{};
    final afterMap = _currentItem.payload['after'] is Map ? _currentItem.payload['after'] as Map<String, dynamic> : <String, dynamic>{};
    
    // If it doesn't have before/after (e.g. legacy or other target), fallback to normal payload
    final isDiffView = _currentItem.payload.containsKey('before') || _currentItem.payload.containsKey('after');
    final keysToProcess = isDiffView ? {...beforeMap.keys, ...afterMap.keys} : _currentItem.payload.keys;

    if (_fieldDefs != null && keysToProcess.isNotEmpty) {
      final fieldMap = {for (var f in _fieldDefs!) f.fieldName: f};

      for (var fieldCode in keysToProcess) {
        final beforeVal = isDiffView ? beforeMap[fieldCode] : null;
        final afterVal = isDiffView ? afterMap[fieldCode] : _currentItem.payload[fieldCode];
        
        // Skip unmodified fields in Diff View
        if (isDiffView) {
          final beforeJson = beforeVal != null ? jsonEncode(beforeVal) : null;
          final afterJson = afterVal != null ? jsonEncode(afterVal) : null;
          if (beforeJson == afterJson) continue;
        }

        final fDef = fieldMap[fieldCode];
        final sector = fDef?.sectorName.isNotEmpty == true ? fDef!.sectorName : defaultSector;
        final group = fDef?.groupName.isNotEmpty == true ? fDef!.groupName : defaultGroup;

        groupedData.putIfAbsent(sector, () => {});
        groupedData[sector]!.putIfAbsent(group, () => []);
        
        final defToUse = fDef ?? FieldDefinition(
          id: 'dummy-id',
          fieldName: fieldCode,
          fieldLabel: fieldCode, // fallback to code
          fieldType: 'TEXT',
          displayOrder: 999,
        );

        groupedData[sector]![group]!.add(MapEntry(defToUse, {'before': beforeVal, 'after': afterVal}));
      }
    }

    final sectors = groupedData.keys.toList();

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.blue.shade200, width: 1.5),
      ),
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          initiallyExpanded: true,
          title: const Text('요청 데이터', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
          children: [
            Container(
              padding: const EdgeInsets.all(16.0),
              width: double.infinity,
              color: Colors.grey[50],
              child: sectors.isEmpty
                  ? const Text('수정된 데이터가 없습니다.', style: TextStyle(color: Colors.grey))
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Tab Bar for Sectors
                        if (sectors.isNotEmpty)
                          SizedBox(
                            height: 40,
                            child: ListView.builder(
                              scrollDirection: Axis.horizontal,
                              itemCount: sectors.length,
                              itemBuilder: (ctx, idx) {
                                final isSelected = _selectedSectorIndex == idx;
                                return GestureDetector(
                                  onTap: () => setState(() => _selectedSectorIndex = idx),
                                  child: Container(
                                    margin: const EdgeInsets.only(right: 8),
                                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                                    decoration: BoxDecoration(
                                      color: isSelected ? Colors.blue.shade700 : Colors.grey.shade300,
                                      borderRadius: BorderRadius.circular(20),
                                    ),
                                    child: Row(
                                      children: [
                                        Icon(Icons.folder, color: isSelected ? Colors.white : Colors.grey.shade600, size: 16),
                                        const SizedBox(width: 6),
                                        Text(
                                          sectors[idx],
                                          style: TextStyle(
                                            color: isSelected ? Colors.white : Colors.grey.shade700,
                                            fontWeight: FontWeight.bold,
                                            fontSize: 13,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              },
                            ),
                          ),
                        const SizedBox(height: 16),
                        
                        // Selected Sector Content (Groups as Accordions)
                        if (sectors.isNotEmpty && _selectedSectorIndex < sectors.length)
                          ...groupedData[sectors[_selectedSectorIndex]]!.entries.map((groupEntry) {
                            final groupName = groupEntry.key;
                            final fields = groupEntry.value;

                            return Card(
                              margin: const EdgeInsets.only(bottom: 12),
                              elevation: 1,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                                side: BorderSide(color: Colors.grey.shade300),
                              ),
                              child: Theme(
                                data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
                                child: ExpansionTile(
                                  initiallyExpanded: true,
                                  tilePadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 0),
                                  title: Text(groupName, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54, fontSize: 14)),
                                  children: [
                                    Padding(
                                      padding: const EdgeInsets.all(16.0),
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: fields.map((fEntry) {
                                          final fDef = fEntry.key;
                                          final vals = fEntry.value;
                                          final beforeRaw = vals['before'];
                                          final afterRaw = vals['after'];
                                          
                                          final beforeStr = (beforeRaw != null && beforeRaw.toString().isNotEmpty) 
                                              ? L10nHelper.parseLocalizedMap(beforeRaw, context) : '-';
                                          final afterStr = (afterRaw != null && afterRaw.toString().isNotEmpty) 
                                              ? L10nHelper.parseLocalizedMap(afterRaw, context) : '-';

                                          return Padding(
                                            padding: const EdgeInsets.only(bottom: 16.0),
                                            child: Column(
                                              crossAxisAlignment: CrossAxisAlignment.start,
                                              children: [
                                                Row(
                                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                                  children: [
                                                    Row(
                                                      children: [
                                                        Text(fDef.fieldLabel, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.black87)),
                                                        if (fDef.isEncrypted) ...[
                                                          const SizedBox(width: 4),
                                                          const Icon(Icons.lock, size: 14, color: Colors.amber),
                                                        ],
                                                      ],
                                                    ),
                                                    Container(
                                                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                                      decoration: BoxDecoration(
                                                        color: Colors.grey.shade200,
                                                        borderRadius: BorderRadius.circular(4),
                                                      ),
                                                      child: const Text('수정됨', style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: Colors.black54)),
                                                    ),
                                                  ],
                                                ),
                                                const SizedBox(height: 8),
                                                if (beforeStr != afterStr && beforeRaw != null)
                                                  Container(
                                                    width: double.infinity,
                                                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                                                    decoration: BoxDecoration(
                                                      color: Colors.red.shade50,
                                                      border: Border.all(color: Colors.red.shade100),
                                                      borderRadius: const BorderRadius.vertical(top: Radius.circular(6)),
                                                    ),
                                                    child: Row(
                                                      children: [
                                                        Icon(Icons.remove_circle_outline, size: 14, color: Colors.red.shade400),
                                                        const SizedBox(width: 8),
                                                        Expanded(child: Text(beforeStr, style: TextStyle(fontSize: 13, color: Colors.red.shade700, decoration: TextDecoration.lineThrough))),
                                                      ],
                                                    ),
                                                  ),
                                                Container(
                                                  width: double.infinity,
                                                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                                                  decoration: BoxDecoration(
                                                    color: Colors.green.shade50,
                                                    border: Border.all(color: Colors.green.shade200),
                                                    borderRadius: (beforeStr != afterStr && beforeRaw != null) 
                                                        ? const BorderRadius.vertical(bottom: Radius.circular(6))
                                                        : BorderRadius.circular(6),
                                                  ),
                                                  child: Row(
                                                    children: [
                                                      Icon(Icons.add_circle_outline, size: 14, color: Colors.green.shade600),
                                                      const SizedBox(width: 8),
                                                      Expanded(child: Text(afterStr, style: TextStyle(fontSize: 13, color: Colors.green.shade800))),
                                                    ],
                                                  ),
                                                ),
                                              ],
                                            ),
                                          );
                                        }).toList(),
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            );
                          }).toList(),
                      ],
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimelineSummary(BuildContext context, int offset) {
    final dateStr = _currentItem.requestDate != null ? DateHelper.formatWithOffset(_currentItem.requestDate, offset, pattern: 'MM/dd HH:mm:ss') : '';
    final reviewedStr = _currentItem.reviewedDate != null ? DateHelper.formatWithOffset(_currentItem.reviewedDate, offset, pattern: 'MM/dd HH:mm:ss') : '';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('결재라인 (요약):', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.black54)),
          const SizedBox(height: 16),
          Row(
            children: [
              _buildTimelineNode('0', '${_currentItem.requester} (상신완료)', dateStr, true),
              Expanded(
                child: Container(
                  height: 2,
                  color: Colors.blue.shade800,
                  margin: const EdgeInsets.symmetric(horizontal: 8),
                ),
              ),
              _buildTimelineNode('1', _currentItem.status == 'PENDING' ? '결재 대기' : '시스템 반영 (완료)', reviewedStr, _currentItem.status != 'PENDING'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineNode(String number, String label, String subLabel, bool active) {
    return Column(
      children: [
        CircleAvatar(
          radius: 12,
          backgroundColor: active ? Colors.blue.shade800 : Colors.grey.shade300,
          child: Text(number, style: TextStyle(color: active ? Colors.white : Colors.black54, fontSize: 12, fontWeight: FontWeight.bold)),
        ),
        const SizedBox(height: 8),
        Text(label, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
        if (subLabel.isNotEmpty) Text(subLabel, style: const TextStyle(fontSize: 10, color: Colors.grey)),
      ],
    );
  }

  Widget _buildDetailedStatus(BuildContext context, int offset) {
    final reqDateStr = _currentItem.requestDate != null ? DateHelper.formatWithOffset(_currentItem.requestDate, offset, pattern: 'yyyy. M. d. a h:mm:ss 처리됨') : '';
    final reasonText = _currentItem.requestReason != null && _currentItem.requestReason!.isNotEmpty ? '"${_currentItem.requestReason}"' : '"상신의견 없음"';

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('결재선 현황', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      CircleAvatar(radius: 10, backgroundColor: Colors.blue.shade800, child: const Text('0', style: TextStyle(color: Colors.white, fontSize: 10))),
                      const SizedBox(width: 8),
                      Text('기안 - ${_currentItem.requester}', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.blue.shade800, fontSize: 13)),
                    ],
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    color: Colors.blue.shade400,
                    child: const Text('기안완료', style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Align(
                alignment: Alignment.centerRight,
                child: Text(reqDateStr, style: const TextStyle(fontSize: 11, color: Colors.black54)),
              ),
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  border: Border(left: BorderSide(color: Colors.blue.shade700, width: 3)),
                  color: Colors.grey.shade50,
                ),
                child: Text(reasonText, style: const TextStyle(fontStyle: FontStyle.italic, color: Colors.black87, fontSize: 13)),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
