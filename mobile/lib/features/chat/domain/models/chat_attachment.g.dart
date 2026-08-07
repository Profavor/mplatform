// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chat_attachment.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ChatAttachmentImpl _$$ChatAttachmentImplFromJson(Map<String, dynamic> json) =>
    _$ChatAttachmentImpl(
      fileName: json['fileName'] as String,
      fileUrl: json['fileUrl'] as String,
      fileType: json['fileType'] as String,
      fileSize: (json['fileSize'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$ChatAttachmentImplToJson(
  _$ChatAttachmentImpl instance,
) => <String, dynamic>{
  'fileName': instance.fileName,
  'fileUrl': instance.fileUrl,
  'fileType': instance.fileType,
  'fileSize': instance.fileSize,
};
