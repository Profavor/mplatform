import 'package:freezed_annotation/freezed_annotation.dart';

part 'inbox_recipient_model.freezed.dart';

@freezed
class InboxRecipientModel with _$InboxRecipientModel {
  const factory InboxRecipientModel({
    required String userId,
    required String name,
    String? email,
    required String recipientType, // "TO", "CC", "BCC"
    @Default(false) bool isRead,
    String? readAt,
    @Default(false) bool isRecalled,
    String? recalledAt,
  }) = _InboxRecipientModel;

  factory InboxRecipientModel.fromJson(Map<String, dynamic> json) {
    return InboxRecipientModel(
      userId: (json['userId'] ?? json['id'] ?? '').toString(),
      name: (json['name'] ?? json['username'] ?? '').toString(),
      email: json['email']?.toString(),
      recipientType: (json['recipientType'] ?? 'TO').toString(),
      isRead: json['isRead'] == true,
      readAt: json['readAt']?.toString(),
      isRecalled: json['isRecalled'] == true,
      recalledAt: json['recalledAt']?.toString(),
    );
  }
}
