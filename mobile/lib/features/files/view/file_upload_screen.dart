import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:file_picker/file_picker.dart';
import 'package:mplatform_mobile/features/files/provider/file_upload_provider.dart';

class FileUploadScreen extends ConsumerWidget {
  const FileUploadScreen({super.key});

  Future<void> _pickAndUploadFile(WidgetRef ref) async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();

    if (result != null && result.files.single.path != null) {
      File file = File(result.files.single.path!);
      ref.read(fileUploadProvider.notifier).uploadFile(file);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uploadState = ref.watch(fileUploadProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('File Upload Test (MinIO & Redis)'),
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton.icon(
                onPressed: uploadState.isLoading ? null : () => _pickAndUploadFile(ref),
                icon: const Icon(Icons.upload_file),
                label: const Text('Select File to Upload'),
              ),
              const SizedBox(height: 24),
              uploadState.when(
                data: (data) {
                  if (data == null) {
                    return const Text('No file uploaded yet.');
                  }
                  return Column(
                    children: [
                      const Icon(Icons.check_circle, color: Colors.green, size: 48),
                      const SizedBox(height: 16),
                      Text('Success!', style: Theme.of(context).textTheme.titleLarge),
                      Text('File: ${data["fileName"]}'),
                      Text('URL: ${data["url"]}'),
                    ],
                  );
                },
                loading: () => const CircularProgressIndicator(),
                error: (error, stack) => Text(
                  'Upload Failed:\n$error',
                  style: const TextStyle(color: Colors.red),
                  textAlign: TextAlign.center,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
