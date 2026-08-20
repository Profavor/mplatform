import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';

class AiDqRecommendationCard extends ConsumerStatefulWidget {
  const AiDqRecommendationCard({super.key});

  @override
  ConsumerState<AiDqRecommendationCard> createState() => _AiDqRecommendationCardState();
}

class _AiDqRecommendationCardState extends ConsumerState<AiDqRecommendationCard> {
  bool _isLoading = true;
  List<dynamic> _recommendations = [];

  @override
  void initState() {
    super.initState();
    _fetchRecommendations();
  }

  Future<void> _fetchRecommendations() async {
    if (!mounted) return;
    try {
      final dio = ref.read(dioProvider);

      // Fetch domains to get an ID
      final domainRes = await dio.get('/api/domains');
      
      if (domainRes.statusCode == 200 && (domainRes.data as List).isNotEmpty) {
        final domainId = domainRes.data[0]['id'];
        
        final recRes = await dio.get('/api/v1/dq/recommendations/$domainId');
        if (recRes.statusCode == 200) {
          if (mounted) {
            setState(() {
              _recommendations = recRes.data as List<dynamic>;
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

    if (_recommendations.isEmpty) {
      return const SizedBox.shrink(); // Don't show if no AI rules
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
                const Icon(Icons.auto_awesome, color: Colors.amber),
                const SizedBox(width: 8),
                const Text(
                  'AI DQ Recommendations',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                ),
                const Spacer(),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.amber.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    '${_recommendations.length} new',
                    style: TextStyle(color: Colors.amber[900], fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            ..._recommendations.map((rec) {
              return Container(
                margin: const EdgeInsets.only(bottom: 12),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.grey.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.grey.shade200),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          rec['fieldName'] ?? '',
                          style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.indigo),
                        ),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.blue.shade100,
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            rec['recommendedRuleType'] ?? '',
                            style: TextStyle(fontSize: 10, color: Colors.blue.shade900),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text(
                      rec['reason'] ?? '',
                      style: TextStyle(fontSize: 13, color: Colors.grey.shade700),
                    ),
                    if (rec['suggestedParameter'] != null)
                      Padding(
                        padding: const EdgeInsets.only(top: 8.0),
                        child: Text(
                          'Suggested: ${rec['suggestedParameter']}',
                          style: TextStyle(fontSize: 12, color: Colors.grey.shade600, fontStyle: FontStyle.italic),
                        ),
                      ),
                  ],
                ),
              );
            }).toList(),
          ],
        ),
      ),
    );
  }
}
