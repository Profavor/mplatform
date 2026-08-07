import 'package:freezed_annotation/freezed_annotation.dart';

part 'chat_attachment.freezed.dart';
part 'chat_attachment.g.dart';

@freezed
class ChatAttachment with _$ChatAttachment {
  const factory ChatAttachment({
    required String fileName,
    required String fileUrl,
    required String fileType,
    @Default(0) int fileSize,
  }) = _ChatAttachment;

  factory ChatAttachment.fromJson(Map<String, dynamic> json) => _$ChatAttachmentFromJson(json);
}
