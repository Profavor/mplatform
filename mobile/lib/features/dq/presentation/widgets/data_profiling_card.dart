import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';

class DataProfilingCard extends ConsumerStatefulWidget {
  const DataProfilingCard({super.key});

  @override
  ConsumerState<DataProfilingCard> createState() => _DataProfilingCardState();
}

class _DataProfilingCardState extends ConsumerState<DataProfilingCard> {
  bool _isLoading = true;
  List<dynamic> _profiles = [];

  @override
  void initState() {
    super.initState();
    _fetchProfiling();
  }

  Future<void> _fetchProfiling() async {
    if (!mounted) return;
    try {
      final dio = ref.read(dioProvider);

      // Fetch domains to get an ID
      final domainRes = await dio.get('/api/domains');
      
      if (domainRes.statusCode == 200 && (domainRes.data as List).isNotEmpty) {
        final domainId = domainRes.data[0]['id'];
        
        final profRes = await dio.get('/api/domains/$domainId/profiling');
        if (profRes.statusCode == 200) {
          if (mounted) {
            setState(() {
              _profiles = profRes.data as List<dynamic>;
              _isLoading = false;
            });
          }
          return;
        }
      }
    } catch (e) {
      // Ignored for demo
    }

    if (mounted) {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Center(child: CircularProgressIndicator()),
        ),
      );
    }

    if (_profiles.isEmpty) {
      return const SizedBox.shrink(); // Don't show if no data
    }

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.analytics, color: Colors.indigo),
                const SizedBox(width: 8),
                const Text(
                  'Data Profiling Overview',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                ),
              ],
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 160,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _profiles.length,
                itemBuilder: (context, index) {
                  final prof = _profiles[index];
                  final double nullRatio = prof['nullRatio'] ?? 0.0;
                  final Color ratioColor = nullRatio > 0.5 ? Colors.red : (nullRatio > 0.1 ? Colors.orange : Colors.green);
                  final String rawName = prof['fieldName'] ?? prof['fieldKey'] ?? '';
                  final String fieldName = L10nHelper.parseLocalizedMap(rawName, context);
                  final String? fieldKey = prof['fieldKey']?.toString();
                  final bool showKeyChip = fieldKey != null && fieldKey.isNotEmpty && fieldKey != fieldName;

                  return Container(
                    width: 200,
                    margin: const EdgeInsets.only(right: 12),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      border: Border.all(color: Colors.grey.shade300),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Flexible(
                              child: Text(
                                fieldName,
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13.5, color: Colors.black87),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            if (showKeyChip) ...[
                              const SizedBox(width: 4),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1.5),
                                decoration: BoxDecoration(
                                  color: Colors.grey.shade100,
                                  borderRadius: BorderRadius.circular(4),
                                  border: Border.all(color: Colors.grey.shade300),
                                ),
                                child: Text(
                                  fieldKey,
                                  style: TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.w600,
                                    color: Colors.grey.shade700,
                                    fontFamily: 'monospace',
                                  ),
                                ),
                              ),
                            ],
                          ],
                        ),
                        const Divider(height: 14),
                        Text('Total: ${prof['totalCount']}', style: const TextStyle(fontSize: 12)),
                        const SizedBox(height: 4),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text('Null Ratio:', style: TextStyle(fontSize: 12)),
                            Text(
                              '${(nullRatio * 100).toStringAsFixed(1)}%',
                              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: ratioColor),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text('Cardinality: ${prof['cardinality']}', style: const TextStyle(fontSize: 12)),
                        
                        const Spacer(),
                        LinearProgressIndicator(
                          value: nullRatio,
                          backgroundColor: Colors.grey.shade200,
                          color: ratioColor,
                        ),
                      ],
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
