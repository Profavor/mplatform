import 'package:freezed_annotation/freezed_annotation.dart';

part 'inbox_folder_count_model.freezed.dart';

@freezed
class InboxFolderCountModel with _$InboxFolderCountModel {
  const factory InboxFolderCountModel({
    required String folder,
    @Default(0) int total,
    @Default(0) int unread,
  }) = _InboxFolderCountModel;

  factory InboxFolderCountModel.fromJson(Map<String, dynamic> json) {
    return InboxFolderCountModel(
      folder: (json['folder'] ?? 'INBOX').toString(),
      total: ((json['total'] ?? 0) as num).toInt(),
      unread: ((json['unread'] ?? 0) as num).toInt(),
    );
  }
}
