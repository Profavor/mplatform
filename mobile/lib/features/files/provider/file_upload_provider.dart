import 'dart:io';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/features/files/service/file_service.dart';

final fileUploadProvider = StateNotifierProvider<FileUploadNotifier, AsyncValue<Map<String, dynamic>?>>((ref) {
  final fileService = ref.watch(fileServiceProvider);
  return FileUploadNotifier(fileService);
});

class FileUploadNotifier extends StateNotifier<AsyncValue<Map<String, dynamic>?>> {
  final FileService _fileService;

  FileUploadNotifier(this._fileService) : super(const AsyncValue.data(null));

  Future<void> uploadFile(File file) async {
    state = const AsyncValue.loading();
    try {
      final result = await _fileService.uploadFile(file);
      state = AsyncValue.data(result);
    } catch (e, stackTrace) {
      state = AsyncValue.error(e, stackTrace);
    }
  }
}
