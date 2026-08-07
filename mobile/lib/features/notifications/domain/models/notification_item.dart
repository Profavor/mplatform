class NotificationItem {
  final String id;
  final String title;
  final String content;
  final String targetId; // Raw UUID from server (must be formatted on UI)
  final String targetType; // e.g. APPROVAL or CHAT or RECORD
  final String createdAt;
  final bool isRead;

  const NotificationItem({
    required this.id,
    required this.title,
    required this.content,
    required this.targetId,
    required this.targetType,
    required this.createdAt,
    this.isRead = false,
  });

  factory NotificationItem.fromJson(Map<String, dynamic> json) {
    return NotificationItem(
      id: json['id']?.toString() ?? '',
      title: json['title']?.toString() ?? '',
      content: json['content']?.toString() ?? '',
      targetId: json['targetId']?.toString() ?? json['referenceId']?.toString() ?? '',
      targetType: json['targetType']?.toString() ?? 'SYSTEM',
      createdAt: json['createdAt']?.toString() ?? '',
      isRead: json['isRead'] == true || json['isRead'] == 'true' || json['read'] == true,
    );
  }

  NotificationItem copyWith({bool? isRead}) {
    return NotificationItem(
      id: id,
      title: title,
      content: content,
      targetId: targetId,
      targetType: targetType,
      createdAt: createdAt,
      isRead: isRead ?? this.isRead,
    );
  }
}
