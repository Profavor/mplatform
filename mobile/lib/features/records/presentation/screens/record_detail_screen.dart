import 'package:flutter/material.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/core/widgets/file_preview_widget.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';

import 'package:mplatform_mobile/features/records/presentation/screens/record_history_screen.dart';
import 'package:mplatform_mobile/features/records/domain/models/domain_model.dart';

class RecordDetailScreen extends ConsumerStatefulWidget {
  final RecordItem record;
  final List<FieldDefinition> fieldDefinitions;
  final DomainModel domain;

  const RecordDetailScreen({
    super.key,
    required this.record,
    required this.fieldDefinitions,
    required this.domain,
  });

  @override
  ConsumerState<RecordDetailScreen> createState() => _RecordDetailScreenState();
}

class _RecordDetailScreenState extends ConsumerState<RecordDetailScreen> {
  final Map<String, String> _decryptedValues = {};
  bool _isDecrypting = false;

  Future<void> _showDecryptDialog(FieldDefinition f) async {
    String reason = '';
    final l10n = AppLocalizations.of(context);
    
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

  Future<void> _decryptField(String fieldKey, String reason) async {
    setState(() => _isDecrypting = true);
    try {
      final repo = RecordsRepository(ref.read(dioProvider));

      final result = await repo.decryptRecordFields(
        recordId: widget.record.recordId,
        fieldKeys: [fieldKey],
        accessReason: reason,
      );

      if (mounted) {
        setState(() {
          _decryptedValues.addAll(result);
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(AppLocalizations.of(context).decryptSuccessNotice)),
        );
        
        Future.delayed(const Duration(seconds: 30), () {
          if (mounted) {
            setState(() {
              _decryptedValues.remove(fieldKey);
            });
          }
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('${AppLocalizations.of(context).decryptFailedNotice} $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isDecrypting = false);
    }
  }

  int _getTimezoneOffset(WidgetRef ref) {
    final prefs = ref.read(sharedPreferencesProvider);
    final tz = prefs.getString('user_personal_timezone') ?? 'Asia/Seoul';
    return DateHelper.getTimezoneOffset(tz);
  }

  int _selectedSectorIndex = 0;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final fallbackDisplayId = UuidFormatter.format(widget.record.recordId, prefix: 'REC');
    
    String displayId = fallbackDisplayId;
    if (widget.domain.identifierFieldId != null) {
      for (final f in widget.fieldDefinitions) {
        if (f.id == widget.domain.identifierFieldId) {
          final val = widget.record.attributes[f.fieldName];
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

    final sortedFields = List<FieldDefinition>.from(
      widget.fieldDefinitions.where((f) => !f.fieldName.toLowerCase().startsWith('_idx_') && !f.fieldName.startsWith('_')),
    )..sort((a, b) => a.displayOrder.compareTo(b.displayOrder));

    // Grouping by Sector -> Group -> Fields
    final Map<String, Map<String, List<FieldDefinition>>> groupedFields = {};
    final String defaultSector = l10n.generalInfo;
    final String defaultGroup = l10n.keyInfo;

    for (final f in sortedFields) {
      final sector = f.sectorName.isNotEmpty ? f.sectorName : defaultSector;
      final group = f.groupName.isNotEmpty ? f.groupName : defaultGroup;

      groupedFields.putIfAbsent(sector, () => {});
      groupedFields[sector]!.putIfAbsent(group, () => []);
      groupedFields[sector]![group]!.add(f);
    }

    final sectors = groupedFields.keys.toList();

    return Scaffold(
      backgroundColor: Colors.blueGrey[50],
      appBar: AppBar(
        title: Text('${l10n.targetTypeRecord} - $displayId', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        iconTheme: const IconThemeData(color: Colors.black87),
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Colors.deepPurple.shade700, Colors.deepPurple.shade500],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(12),
                  boxShadow: [
                    BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 8, offset: const Offset(0, 4)),
                  ],
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          displayId,
                          style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          widget.record.createdBy != null ? 'Creator: ${widget.record.createdBy}' : l10n.targetTypeRecord,
                          style: TextStyle(color: Colors.deepPurple.shade100, fontSize: 13),
                        ),
                      ],
                    ),
                    if (widget.record.updatedAt != null || widget.record.createdAt != null)
                      Material(
                        color: Colors.transparent,
                        child: InkWell(
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => RecordHistoryScreen(
                                  recordId: widget.record.recordId,
                                  fieldDefinitions: widget.fieldDefinitions,
                                  displayId: displayId,
                                ),
                              ),
                            );
                          },
                          borderRadius: BorderRadius.circular(8),
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            decoration: BoxDecoration(
                              color: Colors.white.withOpacity(0.15),
                              borderRadius: BorderRadius.circular(8),
                              border: Border.all(color: Colors.white.withOpacity(0.3)),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                const Icon(Icons.history, color: Colors.white, size: 18),
                                const SizedBox(width: 6),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.end,
                                  children: [
                                    Text(l10n.viewHistory, style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold)),
                                    const SizedBox(height: 2),
                                    Text(
                                      DateHelper.formatWithOffset(
                                        widget.record.updatedAt ?? widget.record.createdAt, 
                                        _getTimezoneOffset(ref), 
                                        pattern: 'yyyy-MM-dd HH:mm'
                                      ),
                                      style: const TextStyle(color: Colors.white, fontSize: 10),
                                    ),
                                  ],
                                ),
                                const SizedBox(width: 4),
                                const Icon(Icons.chevron_right, color: Colors.white70, size: 16),
                              ],
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.deepPurple.shade200, width: 1.5),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(l10n.recordData, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87, fontSize: 16)),
                    ),
                    const Divider(height: 1),
                    Container(
                      padding: const EdgeInsets.all(16.0),
                      width: double.infinity,
                      decoration: const BoxDecoration(
                        color: Color(0xFFFAFAFA),
                        borderRadius: BorderRadius.vertical(bottom: Radius.circular(8)),
                      ),
                      child: sectors.isEmpty
                          ? const Text('데이터가 없습니다.', style: TextStyle(color: Colors.grey))
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
                                              color: isSelected ? Colors.deepPurple.shade700 : Colors.grey.shade300,
                                              borderRadius: BorderRadius.circular(20),
                                            ),
                                            child: Row(
                                              children: [
                                                Icon(Icons.folder, color: isSelected ? Colors.white : Colors.grey.shade600, size: 16),
                                                const SizedBox(width: 6),
                                                Text(
                                                  L10nHelper.parseLocalizedMap(sectors[idx], context),
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
                                    ...groupedFields[sectors[_selectedSectorIndex]]!.entries.map((groupEntry) {
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
                                            title: Text(L10nHelper.parseLocalizedMap(groupName, context), style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54, fontSize: 14)),
                                            children: [
                                              Padding(
                                                padding: const EdgeInsets.all(16.0),
                                                child: Column(
                                                  crossAxisAlignment: CrossAxisAlignment.start,
                                                  children: fields.map((fDef) {
                                                    final originalVal = widget.record.attributes[fDef.fieldName];
                                                    final bool isDecrypted = _decryptedValues.containsKey(fDef.fieldName);
                                                    
                                                    final displayVal = isDecrypted
                                                        ? _decryptedValues[fDef.fieldName]!
                                                        : (originalVal != null && originalVal.toString().isNotEmpty 
                                                            ? L10nHelper.parseLocalizedMap(originalVal, context) 
                                                            : '-');
                                                    
                                                    final bool isEmpty = originalVal == null || originalVal.toString().trim().isEmpty;
                                                    final bool needsDecryptIcon = !isEmpty && fDef.isEncrypted && !isDecrypted && displayVal != '-';

                                                    return Padding(
                                                      padding: const EdgeInsets.only(bottom: 12.0),
                                                      child: Column(
                                                        crossAxisAlignment: CrossAxisAlignment.start,
                                                        children: [
                                                          Row(
                                                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                                            children: [
                                                              Row(
                                                                children: [
                                                                  Text(L10nHelper.parseLocalizedMap(fDef.fieldLabel, context), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.black87)),
                                                                  if (fDef.isEncrypted) ...[
                                                                    const SizedBox(width: 4),
                                                                    const Icon(Icons.lock, size: 14, color: Colors.amber),
                                                                  ],
                                                                ],
                                                              ),
                                                              if (needsDecryptIcon)
                                                                InkWell(
                                                                  onTap: _isDecrypting ? null : () => _showDecryptDialog(fDef),
                                                                  borderRadius: BorderRadius.circular(12),
                                                                  child: Icon(
                                                                    Icons.remove_red_eye_outlined,
                                                                    size: 20,
                                                                    color: _isDecrypting ? Colors.grey : Colors.deepPurple,
                                                                  ),
                                                                ),
                                                            ],
                                                          ),
                                                          const SizedBox(height: 4),
                                                          Container(
                                                            width: double.infinity,
                                                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                                                            decoration: BoxDecoration(
                                                              color: isDecrypted ? Colors.deepPurple.shade50 : Colors.grey[100],
                                                              borderRadius: BorderRadius.circular(6),
                                                              border: isDecrypted ? Border.all(color: Colors.deepPurple.shade200) : null,
                                                            ),
                                                            child: isDecrypted
                                                                ? Text(
                                                                    displayVal,
                                                                    style: TextStyle(
                                                                      fontSize: 13,
                                                                      color: Colors.deepPurple.shade800,
                                                                      fontWeight: FontWeight.bold,
                                                                    ),
                                                                  )
                                                                : FilePreviewWidget(
                                                                    rawValue: originalVal,
                                                                    fieldType: fDef.fieldType,
                                                                    fallbackTextStyle: const TextStyle(fontSize: 13, color: Colors.black87),
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
                                    }),
                                ],
                              ),
                      ), // closes Container
                    ], // closes Column.children
                  ), // closes Column
              ), // closes Container
            ), // closes Padding
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }
}
