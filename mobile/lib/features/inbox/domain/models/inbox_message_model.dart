import 'package:freezed_annotation/freezed_annotation.dart';
import 'inbox_attachment_model.dart';
import 'inbox_recipient_model.dart';

part 'inbox_message_model.freezed.dart';

@freezed
class InboxMessageModel with _$InboxMessageModel {
  const factory InboxMessageModel({
    required String id,
    String? recipientId,
    required String senderId,
    required String senderName,
    String? senderEmail,
    required String subject,
    required String body,
    @Default('NORMAL') String importance, // "NORMAL", "HIGH", "URGENT"
    @Default('INTERNAL') String messageType,
    String? parentMessageId,
    String? rootMessageId,
    String? relatedApprovalId,
    @Default(false) bool isDraft,
    @Default(false) bool isRead,
    @Default(false) bool isStarred,
    @Default('INBOX') String folder,
    @Default(false) bool hasAttachments,
    @Default(0) int attachmentCount,
    @Default(0) int recipientCount,
    @Default(1) int threadCount,
    @Default([]) List<InboxRecipientModel> toRecipients,
    @Default([]) List<InboxRecipientModel> ccRecipients,
    @Default([]) List<InboxAttachmentModel> attachments,
    String? sentAt,
    String? createdAt,
  }) = _InboxMessageModel;

  factory InboxMessageModel.fromJson(Map<String, dynamic> json) {
    List<InboxRecipientModel> toList = [];
    if (json['toRecipients'] is List) {
      toList = (json['toRecipients'] as List)
          .map((e) => InboxRecipientModel.fromJson(e as Map<String, dynamic>))
          .toList();
    }

    List<InboxRecipientModel> ccList = [];
    if (json['ccRecipients'] is List) {
      ccList = (json['ccRecipients'] as List)
          .map((e) => InboxRecipientModel.fromJson(e as Map<String, dynamic>))
          .toList();
    }

    List<InboxAttachmentModel> attList = [];
    if (json['attachments'] is List) {
      attList = (json['attachments'] as List)
          .map((e) => InboxAttachmentModel.fromJson(e as Map<String, dynamic>))
          .toList();
    }

    return InboxMessageModel(
      id: (json['id'] ?? '').toString(),
      recipientId: json['recipientId']?.toString(),
      senderId: (json['senderId'] ?? '').toString(),
      senderName: (json['senderName'] ?? json['sender'] ?? '').toString(),
      senderEmail: json['senderEmail']?.toString(),
      subject: (json['subject'] ?? '').toString(),
      body: (json['body'] ?? json['content'] ?? '').toString(),
      importance: (json['importance'] ?? 'NORMAL').toString(),
      messageType: (json['messageType'] ?? 'INTERNAL').toString(),
      parentMessageId: json['parentMessageId']?.toString(),
      rootMessageId: json['rootMessageId']?.toString(),
      relatedApprovalId: json['relatedApprovalId']?.toString(),
      isDraft: json['isDraft'] == true || json['draft'] == true,
      isRead: json['isRead'] == true || json['read'] == true,
      isStarred: json['isStarred'] == true || json['starred'] == true,
      folder: (json['folder'] ?? 'INBOX').toString(),
      hasAttachments: json['hasAttachments'] == true || attList.isNotEmpty,
      attachmentCount: ((json['attachmentCount'] ?? attList.length) as num).toInt(),
      recipientCount: ((json['recipientCount'] ?? (toList.length + ccList.length)) as num).toInt(),
      threadCount: ((json['threadCount'] ?? 1) as num).toInt(),
      toRecipients: toList,
      ccRecipients: ccList,
      attachments: attList,
      sentAt: json['sentAt']?.toString(),
      createdAt: (json['createdAt'] ?? json['sentAt'])?.toString(),
    );
  }
}
