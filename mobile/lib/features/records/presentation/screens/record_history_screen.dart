import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';

class RecordHistoryScreen extends ConsumerStatefulWidget {
  final String recordId;
  final List<FieldDefinition> fieldDefinitions;
  final String displayId;

  const RecordHistoryScreen({
    super.key,
    required this.recordId,
    required this.fieldDefinitions,
    required this.displayId,
  });

  @override
  ConsumerState<RecordHistoryScreen> createState() => _RecordHistoryScreenState();
}

class _RecordHistoryScreenState extends ConsumerState<RecordHistoryScreen> {
  late Future<List<Map<String, dynamic>>> _historyFuture;
  final Map<String, String> _decryptedValues = {};
  bool _isDecrypting = false;

  @override
  void initState() {
    super.initState();
    _fetchHistory();
  }

  void _fetchHistory() {
    final repo = RecordsRepository(ref.read(dioProvider));
    _historyFuture = repo.getRecordHistory(widget.recordId);
  }

  int _getTimezoneOffset() {
    final prefs = ref.read(sharedPreferencesProvider);
    final tz = prefs.getString('user_personal_timezone') ?? 'Asia/Seoul';
    return DateHelper.getTimezoneOffset(tz);
  }

  FieldDefinition? _getFieldDefinition(String fieldName) {
    for (final f in widget.fieldDefinitions) {
      if (f.fieldName == fieldName) return f;
    }
    return null;
  }

  String _getFieldLabel(String fieldName, BuildContext context) {
    final label = _getFieldDefinition(fieldName)?.fieldLabel ?? fieldName;
    return L10nHelper.parseLocalizedMap(label, context);
  }

  Map<String, dynamic> _parseData(dynamic data) {
    if (data is Map<String, dynamic>) return data;
    if (data is Map) return data.cast<String, dynamic>();
    if (data is String) {
      if (data.trim().isEmpty) return {};
      try {
        final decoded = jsonDecode(data);
        if (decoded is Map) return decoded.cast<String, dynamic>();
      } catch (_) {}
    }
    return {};
  }

  Future<void> _showDecryptDialog(String historyId, FieldDefinition f) async {
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
      _decryptHistoryField(historyId, f.fieldName, reason.trim());
    } else if (confirm == true) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(l10n.viewReasonEmpty)),
        );
      }
    }
  }

  Future<void> _decryptHistoryField(String historyId, String fieldKey, String reason) async {
    setState(() => _isDecrypting = true);
    try {
      final repo = RecordsRepository(ref.read(dioProvider));
      final result = await repo.decryptHistoryFields(
        historyId: historyId,
        fieldKeys: [fieldKey],
        accessReason: reason,
      );

      if (mounted) {
        setState(() {
          // Backend returns Map<String, String> like {"EMAIL": "decrypted"}
          // We store it as composite key: historyId_fieldKey
          for (final entry in result.entries) {
            _decryptedValues['${historyId}_${entry.key}'] = entry.value;
          }
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(AppLocalizations.of(context).decryptSuccessNotice)),
        );
        
        Future.delayed(const Duration(seconds: 30), () {
          if (mounted) {
            setState(() {
              _decryptedValues.remove('${historyId}_$fieldKey');
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

  Widget _buildHistoryItem(Map<String, dynamic> history, int index, int totalCount) {
    final historyId = history['id']?.toString() ?? '';
    final offset = _getTimezoneOffset();
    final changedAtStr = history['changedAt'] != null 
        ? DateHelper.formatWithOffset(history['changedAt'].toString(), offset, pattern: 'yyyy-MM-dd HH:mm:ss')
        : '-';
    
    final changeType = history['changeType'] ?? 'UPDATE';
    final l10n = AppLocalizations.of(context);
    final changedByName = history['changedByName'] ?? l10n.systemapplied;

    Color typeColor = Colors.blue;
    String typeLabel = l10n.actionTypeUpdateShort;
    if (changeType == 'CREATE') {
      typeColor = Colors.green;
      typeLabel = l10n.actionTypeCreateShort;
    } else if (changeType == 'DELETE') {
      typeColor = Colors.red;
      typeLabel = l10n.actionTypeDeleteShort;
    }

    final prevData = _parseData(history['previousData']);
    final newData = _parseData(history['newData']);

    // Calculate diffs using JSON encode to handle nested Maps (multilingual)
    final diffs = <String, Map<String, dynamic>>{};
    for (final key in newData.keys) {
      final String pJson = jsonEncode(prevData[key]);
      final String nJson = jsonEncode(newData[key]);
      
      if (pJson != nJson) {
        diffs[key] = {
          'prev': prevData[key],
          'new': newData[key],
        };
      }
    }

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      elevation: 1,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: BorderSide(color: Colors.grey.shade300),
      ),
      child: ExpansionTile(
        initiallyExpanded: index == 0,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: typeColor.withOpacity(0.1),
                border: Border.all(color: typeColor.withOpacity(0.5)),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(typeLabel, style: TextStyle(color: typeColor, fontSize: 12, fontWeight: FontWeight.bold)),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: Text(
                changedAtStr,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
            ),
          ],
        ),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 4.0),
          child: Text('${l10n.processor}: $changedByName', style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
        ),
        children: [
          const Divider(height: 1),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16.0),
            color: Colors.grey.shade50,
            child: diffs.isEmpty
                ? Text(l10n.noChangesFound, style: const TextStyle(color: Colors.grey))
                : Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: diffs.entries.map((e) {
                      final key = e.key;
                      
                      // Ignore system fields
                      if (['id', 'createdAt', 'updatedAt', 'version'].contains(key)) {
                        return const SizedBox.shrink();
                      }

                      final fDef = _getFieldDefinition(key);
                      final isEncrypted = fDef?.isEncrypted ?? false;
                      final compositeKey = '${historyId}_$key';
                      final isDecrypted = _decryptedValues.containsKey(compositeKey);
                      
                      // Helper function to safely parse and display value
                      String getDisplayValue(dynamic val) {
                        if (val == null) return '';
                        if (isDecrypted && val.toString().contains('*')) {
                          // The backend returns a single decrypted string which applies to BOTH prev and new (since it's the record's decrypted value)
                          // In a true history system, backend should return decrypted prev and decrypted new separately.
                          // But for now, we just use the decrypted value.
                          return _decryptedValues[compositeKey]!;
                        }
                        if (val is Map) return L10nHelper.parseLocalizedMap(val, context);
                        if (val is String && val.startsWith('{') && val.endsWith('}')) {
                           try {
                             final decoded = jsonDecode(val);
                             if (decoded is Map) return L10nHelper.parseLocalizedMap(decoded, context);
                           } catch (_) {}
                        }
                        return val.toString();
                      }

                      final prevStr = getDisplayValue(e.value['prev']);
                      final nextStr = getDisplayValue(e.value['new']);

                      final bool prevIsEmpty = prevStr.trim().isEmpty;
                      final bool nextIsEmpty = nextStr.trim().isEmpty;
                      final bool needsDecryptIcon = isEncrypted && !isDecrypted && (prevStr.contains('*') || nextStr.contains('*'));

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
                                    Text(_getFieldLabel(key, context), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.black87)),
                                    if (isEncrypted) ...[
                                      const SizedBox(width: 4),
                                      const Icon(Icons.lock, size: 14, color: Colors.amber),
                                    ],
                                  ],
                                ),
                                if (needsDecryptIcon && fDef != null)
                                  InkWell(
                                    onTap: _isDecrypting ? null : () => _showDecryptDialog(historyId, fDef),
                                    borderRadius: BorderRadius.circular(12),
                                    child: Icon(
                                      Icons.remove_red_eye_outlined,
                                      size: 20,
                                      color: _isDecrypting ? Colors.grey : Colors.deepPurple,
                                    ),
                                  ),
                              ],
                            ),
                            const SizedBox(height: 6),
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: isDecrypted ? Colors.deepPurple.shade50 : Colors.white,
                                borderRadius: BorderRadius.circular(6),
                                border: Border.all(color: isDecrypted ? Colors.deepPurple.shade200 : Colors.grey.shade300),
                              ),
                              child: Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      prevIsEmpty ? '-' : prevStr,
                                      style: TextStyle(
                                        color: Colors.red.shade700, 
                                        decoration: prevIsEmpty ? null : TextDecoration.lineThrough, 
                                        fontSize: 13
                                      ),
                                    ),
                                  ),
                                  const Padding(
                                    padding: EdgeInsets.symmetric(horizontal: 8.0),
                                    child: Icon(Icons.arrow_forward, size: 16, color: Colors.grey),
                                  ),
                                  Expanded(
                                    child: Text(
                                      nextIsEmpty ? '-' : nextStr,
                                      style: TextStyle(
                                        color: isDecrypted ? Colors.deepPurple.shade800 : Colors.green.shade700, 
                                        fontWeight: FontWeight.bold, 
                                        fontSize: 13
                                      ),
                                    ),
                                  ),
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
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.blueGrey[50],
      appBar: AppBar(
        title: Text('${AppLocalizations.of(context).recordHISTORY} - ${widget.displayId}', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        iconTheme: const IconThemeData(color: Colors.black87),
      ),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: _historyFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator(color: Colors.deepPurple));
          } else if (snapshot.hasError) {
            return Center(child: Text('${AppLocalizations.of(context).failedLoadHistory} ${snapshot.error}'));
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return Center(child: Text(AppLocalizations.of(context).noHistoryData));
          }

          final histories = snapshot.data!;
          return ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 16),
            itemCount: histories.length,
            itemBuilder: (context, index) {
              return _buildHistoryItem(histories[index], index, histories.length);
            },
          );
        },
      ),
    );
  }
}
