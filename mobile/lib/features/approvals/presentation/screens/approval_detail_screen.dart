import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/core/widgets/file_preview_widget.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/approvals/presentation/widgets/approval_timeline_stepper_widget.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_compose_screen.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';
import 'package:url_launcher/url_launcher.dart';

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
  String? _enrichedClassificationPath;
  final Map<String, String> _decryptedValues = {};
  final Map<String, int> _remainingSeconds = {};
  final Map<String, Timer> _countdownTimers = {};
  bool _isDecrypting = false;

  @override
  void initState() {
    super.initState();
    _fetchDetail();
  }

  @override
  void dispose() {
    for (var timer in _countdownTimers.values) {
      timer.cancel();
    }
    _countdownTimers.clear();
    super.dispose();
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
    if (itemToUse.nodeId == null && itemToUse.domainId == null) return;
    setState(() => _isLoadingFields = true);
    
    try {
      final repo = ref.read(recordsRepositoryProvider);
      List<FieldDefinition> fields = [];
      if (itemToUse.nodeId != null && itemToUse.nodeId!.isNotEmpty) {
        fields = await repo.getEffectiveNodeFields(itemToUse.nodeId!);
      } else if (itemToUse.domainId != null && itemToUse.domainId!.isNotEmpty) {
        fields = await repo.getFieldDefinitions(itemToUse.domainId!);
      }

      // Classification Path enrichment if domainName is missing
      String? enrichedPath = itemToUse.classificationPath;
      if (enrichedPath == null || !enrichedPath.contains('>')) {
        try {
          final domains = await repo.getDomains();
          final domain = domains.firstWhere(
            (d) => d.id == itemToUse.domainId,
            orElse: () => domains.first,
          );
          if (domain.name.isNotEmpty) {
            final nodePart = enrichedPath ?? '';
            if (nodePart.isNotEmpty && nodePart != domain.name) {
              enrichedPath = '${domain.name} > $nodePart';
            } else {
              enrichedPath = domain.name;
            }
          }
        } catch (_) {}
      }
      
      if (mounted) {
        setState(() {
          _fieldDefs = fields;
          _enrichedClassificationPath = enrichedPath;
          _isLoadingFields = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoadingFields = false);
      }
    }
  }

  Future<void> _showDecryptDialog(FieldDefinition f) async {
    final l10n = AppLocalizations.of(context)!;
    String reason = '';
    
    final bool? confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.viewReasonTitle),
        content: TextField(
          autofocus: true,
          decoration: InputDecoration(
            hintText: l10n.viewReasonHint,
            border: const OutlineInputBorder(),
          ),
          onChanged: (v) => reason = v,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text(l10n.cancel),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text(l10n.confirm),
          ),
        ],
      ),
    );

    if (confirm == true && reason.trim().isNotEmpty) {
      _decryptField(f.fieldName, reason.trim());
    } else if (confirm == true) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(l10n.viewReasonEmpty)),
        );
      }
    }
  }

  void _hideDecryptedField(String fieldKey) {
    _countdownTimers[fieldKey]?.cancel();
    _countdownTimers.remove(fieldKey);
    _remainingSeconds.remove(fieldKey);
    setState(() {
      _decryptedValues.remove(fieldKey);
      _decryptedValues.remove(fieldKey.toLowerCase());
      _decryptedValues.remove(fieldKey.toUpperCase());
    });
  }

  Future<void> _decryptField(String fieldKey, String reason) async {
    setState(() => _isDecrypting = true);
    try {
      final repo = RecordsRepository(ref.read(dioProvider));
      final targetRecordId = _currentItem.targetId.isNotEmpty ? _currentItem.targetId : _currentItem.approvalId;

      final result = await repo.decryptRecordFields(
        recordId: targetRecordId,
        fieldKeys: [fieldKey],
        accessReason: reason,
      );

      if (mounted) {
        _countdownTimers[fieldKey]?.cancel();
        _remainingSeconds[fieldKey] = 30;

        setState(() {
          _decryptedValues.addAll(result);
        });

        _countdownTimers[fieldKey] = Timer.periodic(const Duration(seconds: 1), (timer) {
          if (!mounted) {
            timer.cancel();
            return;
          }
          final current = _remainingSeconds[fieldKey] ?? 0;
          if (current <= 1) {
            timer.cancel();
            _countdownTimers.remove(fieldKey);
            _remainingSeconds.remove(fieldKey);
            setState(() {
              _decryptedValues.remove(fieldKey);
              _decryptedValues.remove(fieldKey.toLowerCase());
              _decryptedValues.remove(fieldKey.toUpperCase());
            });
          } else {
            setState(() {
              _remainingSeconds[fieldKey] = current - 1;
            });
          }
        });

        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(AppLocalizations.of(context)!.decryptSuccessNotice)),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('${AppLocalizations.of(context)!.decryptFailedNotice} $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isDecrypting = false);
    }
  }

  Future<void> _handleCancelSubmission() async {
    final l10n = AppLocalizations.of(context)!;
    final reasonController = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(l10n.cancelApproval),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(l10n.cancelApprovalConfirm),
            const SizedBox(height: 12),
            TextField(
              controller: reasonController,
              decoration: InputDecoration(
                hintText: l10n.bodyPlaceholder,
                border: const OutlineInputBorder(),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(l10n.cancel),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () => Navigator.pop(ctx, true),
            child: Text(l10n.confirm),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      final notifier = ref.read(approvalsControllerProvider.notifier);
      final success = await notifier.cancelApproval(
        _currentItem.approvalId,
        reason: reasonController.text.trim(),
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(success ? l10n.cancelApprovalSuccess : l10n.error),
          ),
        );
        if (success) Navigator.pop(context);
      }
    }
  }

  ApprovalItem get _currentItem => _detailedItem ?? widget.item;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final currentUser = ref.watch(authControllerProvider).valueOrNull;
    final offset = DateHelper.getTimezoneOffset(ref.read(sharedPreferencesProvider).getString('user_personal_timezone') ?? 'Asia/Seoul');
    final formattedId = UuidFormatter.format(_currentItem.approvalId, prefix: 'APR');
    final isRequester = currentUser != null &&
        (currentUser.id == _currentItem.requester || currentUser.username == _currentItem.requester);
    final canCancel = isRequester && _currentItem.status == 'PENDING';

    return Scaffold(
      backgroundColor: Colors.blueGrey[50],
      appBar: AppBar(
        title: Text(formattedId, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        iconTheme: const IconThemeData(color: Colors.black87),
        actions: [
          IconButton(
            icon: const Icon(Icons.mark_email_unread_outlined, color: Colors.deepPurple),
            tooltip: l10n.createRoleTitle,
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (ctx) => InboxComposeScreen(
                    relatedApprovalId: _currentItem.approvalId,
                    defaultRecipientUserId: _currentItem.requester,
                  ),
                ),
              );
            },
          ),
          if (canCancel)
            IconButton(
              icon: const Icon(Icons.cancel_outlined, color: Colors.red),
              tooltip: l10n.cancelApproval,
              onPressed: _handleCancelSubmission,
            ),
          IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.of(context).pop()),
        ],
      ),
      body: _isLoadingDetail || _isLoadingFields 
        ? const Center(child: CircularProgressIndicator()) 
        : SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildCancellationBanner(context),
                _buildHeaderCard(context, offset),
                const SizedBox(height: 14),
                _buildDataSection(context),
                const SizedBox(height: 14),
                ApprovalTimelineStepperWidget(item: _currentItem, offsetHours: offset),
              ],
            ),
          ),
    );
  }

  Widget _buildCancellationBanner(BuildContext context) {
    if (_currentItem.status != 'CANCELLED') return const SizedBox.shrink();

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.red.shade50,
        borderRadius: BorderRadius.circular(8),
        border: Border(left: BorderSide(color: Colors.red.shade700, width: 4)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(Icons.cancel, color: Colors.red.shade700, size: 20),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '이 결재는 기안자에 의해 상신 취소되었습니다.',
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red.shade700, fontSize: 13),
                ),
                if (_currentItem.requestReason != null && _currentItem.requestReason!.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '상신 취소 사유: ',
                        style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red.shade900, fontSize: 12),
                      ),
                      Expanded(
                        child: Text(
                          _currentItem.requestReason!,
                          style: TextStyle(color: Colors.red.shade900, fontSize: 12),
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderCard(BuildContext context, int offset) {
    final l10n = AppLocalizations.of(context)!;
    final reqDateStr = _currentItem.requestDate != null
        ? DateHelper.formatWithOffset(_currentItem.requestDate, offset, pattern: 'yyyy. MM. dd. HH:mm:ss')
        : '';
    final targetTypeLabel = _getTargetTypeLabel(context, _currentItem.targetType);

    // Parse localized classification path e.g. "임직원 > 정규직"
    final effectivePath = _enrichedClassificationPath ?? _currentItem.classificationPath;
    String? localizedPath;
    if (effectivePath != null && effectivePath.isNotEmpty) {
      final parts = effectivePath.split('>');
      localizedPath = parts.map((p) => L10nHelper.parseLocalizedMap(p.trim(), context)).join(' > ');
    }

    Color statusColor = Colors.orange.shade700;
    String statusText = l10n.statusPending;
    if (_currentItem.status == 'APPROVED') {
      statusColor = Colors.green.shade600;
      statusText = l10n.statusApproved;
    } else if (_currentItem.status == 'REJECTED') {
      statusColor = Colors.red.shade600;
      statusText = l10n.statusRejected;
    } else if (_currentItem.status == 'CANCELLED') {
      statusColor = Colors.red.shade700;
      statusText = 'CANCELLED';
    }

    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: Colors.grey.shade200),
      ),
      color: Colors.white,
      child: Padding(
        padding: const EdgeInsets.all(14.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: Colors.amber.shade800,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    targetTypeLabel,
                    style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(width: 6),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: statusColor,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    statusText,
                    style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
            if (localizedPath != null && localizedPath.isNotEmpty) ...[
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.blue.shade200),
                ),
                child: Text(
                  localizedPath,
                  style: TextStyle(fontSize: 11, color: Colors.blue.shade800, fontWeight: FontWeight.w600),
                ),
              ),
            ],
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.person_outline, size: 13, color: Colors.grey.shade600),
                    const SizedBox(width: 2),
                    Text(
                      '${l10n.applicantUser}: ${_currentItem.requester}',
                      style: TextStyle(fontSize: 11, color: Colors.grey.shade700),
                    ),
                  ],
                ),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.access_time, size: 13, color: Colors.grey.shade600),
                    const SizedBox(width: 2),
                    Text(reqDateStr, style: TextStyle(fontSize: 11, color: Colors.grey.shade700)),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _getTargetTypeLabel(BuildContext context, String targetType) {
    final l10n = AppLocalizations.of(context)!;
    switch (targetType.toUpperCase()) {
      case 'MEMO':
        return '메모 결재';
      case 'RECORD_UPDATE':
        return l10n.targetTypeRecordUpdate;
      case 'RECORD_CREATE':
      case 'RECORD':
        return l10n.targetTypeRecordCreate;
      case 'RECORD_DELETE':
        return l10n.targetTypeRecordDelete;
      case 'SCHEMA_CHANGE':
        return l10n.targetTypeSchemaChange;
      case 'SANDBOX':
        return l10n.targetTypeSandbox;
      default:
        return targetType;
    }
  }

  Widget _buildMemoSection(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final title = _currentItem.payload['title']?.toString() ?? '(제목 없음)';
    final content = _currentItem.payload['content']?.toString() ?? '';
    final attachmentsRaw = _currentItem.payload['attachments'];
    final attachments = attachmentsRaw is List ? attachmentsRaw : [];

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
          title: const Text('결재 내용', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
          children: [
            Container(
              padding: const EdgeInsets.all(16.0),
              width: double.infinity,
              color: Colors.grey[50],
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(8),
                  border: Border(left: BorderSide(color: Colors.blue.shade600, width: 4)),
                  boxShadow: [
                    BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 4, offset: const Offset(0, 2)),
                  ],
                ),
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Title
                    Text(
                      '결재 $title',
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Colors.black87),
                    ),
                    const SizedBox(height: 12),

                    // Attachments if any
                    if (attachments.isNotEmpty) ...[
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Icon(Icons.attach_file, size: 15, color: Colors.indigo),
                          const SizedBox(width: 4),
                          Text(
                            '첨부파일:',
                            style: TextStyle(fontWeight: FontWeight.w600, fontSize: 12, color: Colors.grey.shade700),
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Wrap(
                        spacing: 8,
                        runSpacing: 6,
                        children: attachments.map((att) {
                          final fileName = (att is Map ? att['fileName'] : att)?.toString() ?? 'attachment';
                          final downloadUrl = (att is Map ? att['downloadUrl'] : null)?.toString() ?? '/api/files/download/${Uri.encodeComponent(fileName)}';
                          return FilePreviewWidget(
                            rawValue: downloadUrl,
                            fieldType: 'FILE',
                          );
                        }).toList(),
                      ),
                      const SizedBox(height: 12),
                    ],

                    // Content Rich Text / Body
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.grey.shade50,
                        borderRadius: BorderRadius.circular(6),
                        border: Border.all(color: Colors.grey.shade200),
                      ),
                      child: FilePreviewWidget(
                        rawValue: content,
                        fieldType: 'HTML',
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDataSection(BuildContext context) {
    if (_currentItem.targetType == 'MEMO') {
      return _buildMemoSection(context);
    }
    final l10n = AppLocalizations.of(context)!;
    final Map<String, Map<String, List<MapEntry<FieldDefinition, Map<String, dynamic>>>>> groupedData = {};
    final String defaultSector = l10n.general;
    final String defaultGroup = l10n.basicInfo;

    final beforeMap = _currentItem.payload['before'] is Map ? Map<String, dynamic>.from(_currentItem.payload['before'] as Map) : <String, dynamic>{};
    final afterMap = _currentItem.payload['after'] is Map ? Map<String, dynamic>.from(_currentItem.payload['after'] as Map) : <String, dynamic>{};
    
    final isDiffView = _currentItem.targetType == 'RECORD_UPDATE' || _currentItem.payload.containsKey('before') || _currentItem.payload.containsKey('after');
    final rawKeys = isDiffView ? {...beforeMap.keys, ...afterMap.keys} : _currentItem.payload.keys;
    final keysToProcess = rawKeys
        .where((k) => !k.toLowerCase().startsWith('_idx_') && !k.startsWith('_'))
        .toList();

    if (keysToProcess.isNotEmpty) {
      final fieldMap = _fieldDefs != null ? {for (var f in _fieldDefs!) f.fieldName.toLowerCase(): f} : <String, FieldDefinition>{};

      for (var fieldCode in keysToProcess) {
        final beforeVal = isDiffView ? (beforeMap[fieldCode] ?? beforeMap[fieldCode.toUpperCase()] ?? beforeMap[fieldCode.toLowerCase()]) : null;
        final afterVal = isDiffView ? (afterMap[fieldCode] ?? afterMap[fieldCode.toUpperCase()] ?? afterMap[fieldCode.toLowerCase()]) : _currentItem.payload[fieldCode];

        final fDef = fieldMap[fieldCode.toLowerCase()];

        // Change detection: Filter out unchanged fields for RECORD_UPDATE
        if (isDiffView) {
          final isEnc = fDef?.isEncrypted == true ||
              (afterVal != null && afterVal.toString().startsWith('vault:v1:')) ||
              (beforeVal != null && beforeVal.toString().startsWith('vault:v1:')) ||
              (beforeVal != null && beforeVal.toString().contains('***')) ||
              (afterVal != null && afterVal.toString().contains('***'));

          if (!isEnc) {
            final beforeStr = beforeVal != null ? jsonEncode(beforeVal) : '';
            final afterStr = afterVal != null ? jsonEncode(afterVal) : '';
            if (beforeStr == afterStr) {
              continue; // Skip unchanged non-encrypted field
            }
          }
        }

        final sector = fDef?.sectorName.isNotEmpty == true ? fDef!.sectorName : defaultSector;
        final group = fDef?.groupName.isNotEmpty == true ? fDef!.groupName : defaultGroup;

        groupedData.putIfAbsent(sector, () => {});
        groupedData[sector]!.putIfAbsent(group, () => []);
        
        final defToUse = fDef ?? FieldDefinition(
          id: 'dummy-id',
          fieldName: fieldCode,
          fieldLabel: fieldCode,
          fieldType: 'TEXT',
          isEncrypted: false,
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
          title: Text(l10n.general, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
          children: [
            Container(
              padding: const EdgeInsets.all(12.0),
              width: double.infinity,
              color: Colors.grey[50],
              child: sectors.isEmpty
                  ? Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(l10n.emptyNotification, style: const TextStyle(color: Colors.grey)),
                    )
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Tab Bar for Sectors
                        if (sectors.isNotEmpty)
                          SizedBox(
                            height: 38,
                            child: ListView.builder(
                              scrollDirection: Axis.horizontal,
                              itemCount: sectors.length,
                              itemBuilder: (ctx, idx) {
                                final isSelected = _selectedSectorIndex == idx;
                                return GestureDetector(
                                  onTap: () => setState(() => _selectedSectorIndex = idx),
                                  child: Container(
                                    margin: const EdgeInsets.only(right: 8),
                                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                                    decoration: BoxDecoration(
                                      color: isSelected ? Colors.blue.shade700 : Colors.grey.shade300,
                                      borderRadius: BorderRadius.circular(20),
                                    ),
                                    child: Row(
                                      children: [
                                        Icon(Icons.folder, color: isSelected ? Colors.white : Colors.grey.shade600, size: 15),
                                        const SizedBox(width: 6),
                                        Text(
                                          L10nHelper.parseLocalizedMap(sectors[idx], context),
                                          style: TextStyle(
                                            color: isSelected ? Colors.white : Colors.grey.shade700,
                                            fontWeight: FontWeight.bold,
                                            fontSize: 12.5,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              },
                            ),
                          ),
                        const SizedBox(height: 12),
                        
                        // Selected Sector Content (Groups as Comparison Tables)
                        if (sectors.isNotEmpty && _selectedSectorIndex < sectors.length)
                          ...groupedData[sectors[_selectedSectorIndex]]!.entries.map((groupEntry) {
                            final groupName = groupEntry.key;
                            final fields = groupEntry.value;

                            return Container(
                              margin: const EdgeInsets.only(bottom: 12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(6),
                                border: Border.all(color: Colors.grey.shade300),
                              ),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  // Group Header
                                  Container(
                                    width: double.infinity,
                                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                                    decoration: BoxDecoration(
                                      color: Colors.grey.shade100,
                                      borderRadius: const BorderRadius.vertical(top: Radius.circular(5)),
                                      border: Border(bottom: BorderSide(color: Colors.grey.shade300)),
                                    ),
                                    child: Text(
                                      L10nHelper.parseLocalizedMap(groupName, context),
                                      style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87, fontSize: 13),
                                    ),
                                  ),
                                  // 3-Column Table Header
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                                    decoration: BoxDecoration(
                                      color: Colors.grey.shade50,
                                      border: Border(bottom: BorderSide(color: Colors.grey.shade200)),
                                    ),
                                    child: Row(
                                      children: [
                                        Expanded(
                                          flex: 3,
                                          child: Text(l10n.fieldName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11.5, color: Colors.black54)),
                                        ),
                                        Expanded(
                                          flex: 4,
                                          child: Text(l10n.beforeValue, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11.5, color: Colors.red)),
                                        ),
                                        Expanded(
                                          flex: 4,
                                          child: Text(l10n.afterValue, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11.5, color: Colors.green)),
                                        ),
                                      ],
                                    ),
                                  ),
                                  // Table Rows
                                  ...fields.map((fEntry) {
                                    final fDef = fEntry.key;
                                    final vals = fEntry.value;
                                    final beforeRaw = vals['before'];
                                    final afterRaw = vals['after'];
                                    
                                    // 100% DB 스키마 속성(fDef.isEncrypted) 및 실제 데이터 암호화 프리픽스(vault:v1:)로만 동적 판정 (하드코딩 0%)
                                    final isEncrypted = fDef.isEncrypted ||
                                        (afterRaw != null && afterRaw.toString().startsWith('vault:v1:')) ||
                                        (beforeRaw != null && beforeRaw.toString().startsWith('vault:v1:'));
                                    final isDecrypted = _decryptedValues.containsKey(fDef.fieldName);
                                    final decryptedVal = _decryptedValues[fDef.fieldName];

                                    final beforeDisplay = _formatMaskedValue(beforeRaw, isEncrypted, null);
                                    final afterDisplay = _formatMaskedValue(afterRaw, isEncrypted, decryptedVal);

                                    return Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                      decoration: BoxDecoration(
                                        border: Border(bottom: BorderSide(color: Colors.grey.shade100)),
                                      ),
                                      child: Row(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          // Column 1: Field Name
                                          Expanded(
                                            flex: 3,
                                            child: Row(
                                              crossAxisAlignment: CrossAxisAlignment.start,
                                              children: [
                                                Flexible(
                                                  child: Text(
                                                    L10nHelper.parseLocalizedMap(fDef.fieldLabel, context),
                                                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.black87),
                                                  ),
                                                ),
                                                if (isEncrypted) ...[
                                                  const SizedBox(width: 4),
                                                  const Icon(Icons.lock, size: 13, color: Colors.amber),
                                                ],
                                              ],
                                            ),
                                          ),
                                          const SizedBox(width: 6),
                                          // Column 2: Before Value
                                          Expanded(
                                            flex: 4,
                                            child: Container(
                                              padding: const EdgeInsets.all(6),
                                              decoration: BoxDecoration(
                                                color: Colors.red.shade50.withOpacity(0.6),
                                                borderRadius: BorderRadius.circular(4),
                                              ),
                                              child: _buildValueWidget(
                                                context,
                                                rawValue: isEncrypted ? beforeDisplay : beforeRaw,
                                                fDef: fDef,
                                                isEncrypted: isEncrypted,
                                                isBefore: true,
                                              ),
                                            ),
                                          ),
                                          const SizedBox(width: 6),
                                          // Column 3: After Value + Unmask Button
                                          Expanded(
                                            flex: 4,
                                            child: Container(
                                              padding: const EdgeInsets.all(6),
                                              decoration: BoxDecoration(
                                                color: Colors.green.shade50.withOpacity(0.6),
                                                borderRadius: BorderRadius.circular(4),
                                              ),
                                              child: Column(
                                                crossAxisAlignment: CrossAxisAlignment.start,
                                                children: [
                                                  _buildValueWidget(
                                                    context,
                                                    rawValue: isEncrypted ? afterDisplay : afterRaw,
                                                    fDef: fDef,
                                                    isEncrypted: isEncrypted,
                                                    isBefore: false,
                                                    isDecrypted: isDecrypted,
                                                  ),
                                                  if (isEncrypted) ...[
                                                    const SizedBox(height: 4),
                                                    InkWell(
                                                      onTap: _isDecrypting
                                                          ? null
                                                          : () => isDecrypted
                                                              ? _hideDecryptedField(fDef.fieldName)
                                                              : _showDecryptDialog(fDef),
                                                      child: Row(
                                                        mainAxisSize: MainAxisSize.min,
                                                        children: [
                                                          Icon(
                                                            isDecrypted ? Icons.visibility_off : Icons.lock_open,
                                                            size: 13,
                                                            color: Colors.blue.shade700,
                                                          ),
                                                          const SizedBox(width: 3),
                                                          Text(
                                                            isDecrypted
                                                                ? '${l10n.hideOriginal}${(_remainingSeconds[fDef.fieldName] != null) ? " (00:${_remainingSeconds[fDef.fieldName]!.toString().padLeft(2, '0')})" : ""}'
                                                                : l10n.viewOriginal,
                                                            style: TextStyle(
                                                              fontSize: 11,
                                                              color: Colors.blue.shade700,
                                                              decoration: TextDecoration.underline,
                                                            ),
                                                          ),
                                                        ],
                                                      ),
                                                    ),
                                                  ],
                                                ],
                                              ),
                                            ),
                                          ),
                                        ],
                                      ),
                                    );
                                  }).toList(),
                                ],
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

  /// Specialized value renderer for Reference, Multilingual, Table/Array, Files, etc.
  Widget _buildValueWidget(
    BuildContext context, {
    required dynamic rawValue,
    required FieldDefinition fDef,
    required bool isEncrypted,
    required bool isBefore,
    bool isDecrypted = false,
  }) {
    if (rawValue == null || rawValue.toString().trim().isEmpty || rawValue == '-') {
      return Text(
        '(없음)',
        style: TextStyle(
          fontSize: 11.5,
          color: isBefore ? Colors.red.shade400 : Colors.grey.shade500,
        ),
      );
    }

    // 1. Array / List / Table format (e.g. 학력 이력)
    dynamic parsedJson = rawValue;
    if (rawValue is String && (rawValue.trim().startsWith('[') || rawValue.trim().startsWith('{'))) {
      try {
        parsedJson = jsonDecode(rawValue);
      } catch (_) {}
    }

    if (parsedJson is List && parsedJson.isNotEmpty && parsedJson.first is Map) {
      return _buildArrayTable(context, parsedJson.cast<Map<String, dynamic>>(), fDef, isBefore);
    }

    // 2. Multilingual JSON map (e.g. {"ko": "...", "en": "..."})
    if (parsedJson is Map && (parsedJson.containsKey('ko') || parsedJson.containsKey('en'))) {
      final localizedVal = L10nHelper.parseLocalizedMap(parsedJson, context);
      return Text(
        localizedVal,
        style: TextStyle(
          fontSize: 11.5,
          fontWeight: isDecrypted ? FontWeight.bold : FontWeight.normal,
          color: isBefore ? Colors.red.shade800 : Colors.green.shade900,
        ),
      );
    }

    // 3. Reference (DOMAIN_REFERENCE) format
    if (fDef.fieldType == 'DOMAIN_REFERENCE' || _isUuid(rawValue.toString())) {
      final uuidStr = rawValue.toString();
      final formattedRef = UuidFormatter.format(uuidStr, prefix: 'REC');
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
        decoration: BoxDecoration(
          color: isBefore ? Colors.red.shade100 : Colors.blue.shade50,
          borderRadius: BorderRadius.circular(4),
          border: Border.all(color: isBefore ? Colors.red.shade200 : Colors.blue.shade200),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.link, size: 12, color: isBefore ? Colors.red.shade700 : Colors.blue.shade700),
            const SizedBox(width: 4),
            Text(
              '[$formattedRef]',
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.bold,
                color: isBefore ? Colors.red.shade900 : Colors.blue.shade900,
              ),
            ),
          ],
        ),
      );
    }

    // 4. File / Image / HTML Preview
    return FilePreviewWidget(
      rawValue: rawValue,
      fieldType: fDef.fieldType,
      fallbackTextStyle: TextStyle(
        fontSize: 11.5,
        fontWeight: isDecrypted ? FontWeight.bold : FontWeight.normal,
        color: isBefore ? Colors.red.shade800 : Colors.green.shade900,
        decoration: (isBefore && !isEncrypted) ? TextDecoration.lineThrough : null,
      ),
    );
  }

  /// Renders a dynamic sub-table for Array/List data (e.g. 학력 이력)
  Widget _buildArrayTable(
    BuildContext context,
    List<Map<String, dynamic>> items,
    FieldDefinition fDef,
    bool isBefore,
  ) {
    if (items.isEmpty) return const Text('-');

    // 1. Column Schemas from fDef.rawOptions
    List<Map<String, dynamic>> columns = [];
    if (fDef.rawOptions != null && fDef.rawOptions!.isNotEmpty) {
      try {
        final decoded = jsonDecode(fDef.rawOptions!);
        if (decoded is Map) {
          if (decoded['tableSchema'] != null && decoded['tableSchema']['columns'] is List) {
            columns = (decoded['tableSchema']['columns'] as List).cast<Map<String, dynamic>>();
          } else if (decoded['columns'] is List) {
            columns = (decoded['columns'] as List).cast<Map<String, dynamic>>();
          }
        }
      } catch (_) {}
    }

    final columnMap = {for (var c in columns) (c['key'] ?? c['fieldKey'] ?? '').toString(): c};
    final keys = items.first.keys.toList();

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Table(
        defaultColumnWidth: const IntrinsicColumnWidth(),
        border: TableBorder.all(color: isBefore ? Colors.red.shade200 : Colors.green.shade200, width: 0.8),
        children: [
          // Header Row
          TableRow(
            decoration: BoxDecoration(color: isBefore ? Colors.red.shade100 : Colors.green.shade100),
            children: keys.map((k) {
              final col = columnMap[k];
              final colLabel = col != null ? (col['name'] ?? col['label'] ?? col['fieldLabel'] ?? k) : k;
              final localizedCol = L10nHelper.parseLocalizedMap(colLabel, context);

              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
                child: Text(
                  localizedCol.isNotEmpty ? localizedCol : k,
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 11,
                    color: isBefore ? Colors.red.shade900 : Colors.green.shade900,
                  ),
                ),
              );
            }).toList(),
          ),
          // Data Rows
          ...items.map((row) => TableRow(
            children: keys.map((k) {
              final val = row[k];
              final col = columnMap[k];
              final displayVal = _formatTableCellValue(context, val, col);

              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
                child: Text(
                  displayVal,
                  style: TextStyle(
                    fontSize: 11,
                    color: isBefore ? Colors.red.shade800 : Colors.green.shade900,
                  ),
                ),
              );
            }).toList(),
          )),
        ],
      ),
    );
  }

  String _formatTableCellValue(BuildContext context, dynamic val, Map<String, dynamic>? col) {
    if (val == null || val.toString().trim().isEmpty || val == '-') return '-';

    // SELECT Type Option Label Translation
    if (col != null) {
      final colType = (col['type'] ?? col['fieldType'] ?? '').toString().toUpperCase();
      dynamic rawOpts = col['options'];
      if (rawOpts is String) {
        try {
          rawOpts = jsonDecode(rawOpts);
        } catch (_) {}
      }

      if (colType == 'SELECT' || rawOpts is List) {
        if (rawOpts is List) {
          for (var opt in rawOpts) {
            if (opt is Map) {
              final optVal = (opt['value'] ?? opt['key'] ?? opt['code'] ?? '').toString();
              if (optVal.toLowerCase() == val.toString().toLowerCase()) {
                final optLabel = opt['label'] ?? opt['name'] ?? optVal;
                return L10nHelper.parseLocalizedMap(optLabel, context);
              }
            }
          }
        }
      }
    }

    // Multilingual JSON Map
    if (val is Map) {
      return L10nHelper.parseLocalizedMap(val, context);
    }
    if (val is String && val.trim().startsWith('{') && val.trim().endsWith('}')) {
      try {
        final decoded = jsonDecode(val);
        if (decoded is Map) {
          return L10nHelper.parseLocalizedMap(decoded, context);
        }
      } catch (_) {}
    }

    return val.toString();
  }

  bool _isUuid(String str) {
    final uuidRegex = RegExp(r'^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$');
    return uuidRegex.hasMatch(str.trim());
  }

  String _formatMaskedValue(dynamic val, bool isEncrypted, String? decryptedVal) {
    if (decryptedVal != null && decryptedVal.isNotEmpty) return decryptedVal;
    if (val == null || val.toString().trim().isEmpty || val == '-') return '(없음)';
    final str = val.toString().trim();
    if (isEncrypted || str.startsWith('vault:v1:')) {
      if (str.contains('*')) return str;
      if (str.startsWith('vault:v1:')) {
        return '******';
      }
      if (str.length > 6) {
        return '${str.substring(0, 6)}******';
      }
      return '******';
    }
    return str;
  }
}
