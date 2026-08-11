import 'dart:io';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';

final fileServiceProvider = Provider<FileService>((ref) {
  final dio = ref.watch(dioProvider);
  return FileService(dio);
});

class FileService {
  final Dio _dio;

  FileService(this._dio);

  Future<Map<String, dynamic>> uploadFile(File file) async {
    String fileName = file.path.split('/').last;

    FormData formData = FormData.fromMap({
      "file": await MultipartFile.fromFile(file.path, filename: fileName),
    });

    try {
      final response = await _dio.post(
        '/files/upload',
        data: formData,
        options: Options(
          contentType: 'multipart/form-data',
        ),
      );
      
      if (response.statusCode == 200) {
        return response.data; // e.g., { "fileName": "...", "url": "..." }
      } else {
        throw Exception("Failed to upload file. Status code: ${response.statusCode}");
      }
    } on DioException catch (e) {
      throw Exception("DioException: ${e.message}");
    }
  }
}
