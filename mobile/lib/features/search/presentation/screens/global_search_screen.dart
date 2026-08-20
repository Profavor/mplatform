import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';

class GlobalSearchScreen extends ConsumerStatefulWidget {
  const GlobalSearchScreen({super.key});

  @override
  ConsumerState<GlobalSearchScreen> createState() => _GlobalSearchScreenState();
}

class _GlobalSearchScreenState extends ConsumerState<GlobalSearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  List<dynamic> _results = [];
  bool _isLoading = false;

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _performSearch(String query) async {
    if (query.trim().isEmpty) {
      setState(() => _results = []);
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final dio = ref.read(dioProvider);
      final response = await dio.get(
        '/api/v1/search',
        queryParameters: {'q': query, 'size': 10},
      );
      
      if (response.statusCode == 200 && response.data != null) {
        final dynamic data = response.data;
        final content = data is Map ? (data['content'] as List<dynamic>? ?? []) : (data is List ? data : []);
        setState(() {
          _results = content;
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Search failed')),
        );
      }
      setState(() {
        _results = [];
      });
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: TextField(
          controller: _searchController,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Search anywhere...',
            border: InputBorder.none,
            hintStyle: TextStyle(color: Colors.white70),
          ),
          style: const TextStyle(color: Colors.white, fontSize: 18),
          onSubmitted: _performSearch,
        ),
        backgroundColor: Colors.indigo[800],
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.clear),
            onPressed: () {
              _searchController.clear();
              setState(() => _results = []);
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _results.isEmpty
              ? Center(
                  child: Text(
                    _searchController.text.isEmpty
                        ? 'Type to search...'
                        : 'No results found.',
                    style: const TextStyle(color: Colors.grey, fontSize: 16),
                  ),
                )
              : ListView.builder(
                  itemCount: _results.length,
                  itemBuilder: (context, index) {
                    final record = _results[index];
                    return ListTile(
                      leading: const Icon(Icons.data_object, color: Colors.indigo),
                      title: Text(record['id'] ?? 'Unknown ID'),
                      subtitle: Text(
                        record['data']?.toString() ?? 'No data',
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      onTap: () {
                        // Navigate to detail
                      },
                    );
                  },
                ),
    );
  }
}
