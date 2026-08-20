import 'package:freezed_annotation/freezed_annotation.dart';

part 'inbox_attachment_model.freezed.dart';

@freezed
class InboxAttachmentModel with _$InboxAttachmentModel {
  const factory InboxAttachmentModel({
    required String id,
    required String fileName,
    required int fileSize,
    String? contentType,
  }) = _InboxAttachmentModel;

  factory InboxAttachmentModel.fromJson(Map<String, dynamic> json) {
    return InboxAttachmentModel(
      id: (json['id'] ?? '').toString(),
      fileName: (json['fileName'] ?? json['name'] ?? '').toString(),
      fileSize: ((json['fileSize'] ?? json['size'] ?? 0) as num).toInt(),
      contentType: json['contentType']?.toString(),
    );
  }
}
