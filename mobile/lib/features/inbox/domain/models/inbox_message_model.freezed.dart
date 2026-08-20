// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'inbox_message_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$InboxMessageModel {
  String get id => throw _privateConstructorUsedError;
  String? get recipientId => throw _privateConstructorUsedError;
  String get senderId => throw _privateConstructorUsedError;
  String get senderName => throw _privateConstructorUsedError;
  String? get senderEmail => throw _privateConstructorUsedError;
  String get subject => throw _privateConstructorUsedError;
  String get body => throw _privateConstructorUsedError;
  String get importance =>
      throw _privateConstructorUsedError; // "NORMAL", "HIGH", "URGENT"
  String get messageType => throw _privateConstructorUsedError;
  String? get parentMessageId => throw _privateConstructorUsedError;
  String? get rootMessageId => throw _privateConstructorUsedError;
  String? get relatedApprovalId => throw _privateConstructorUsedError;
  bool get isDraft => throw _privateConstructorUsedError;
  bool get isRead => throw _privateConstructorUsedError;
  bool get isStarred => throw _privateConstructorUsedError;
  String get folder => throw _privateConstructorUsedError;
  bool get hasAttachments => throw _privateConstructorUsedError;
  int get attachmentCount => throw _privateConstructorUsedError;
  int get recipientCount => throw _privateConstructorUsedError;
  int get threadCount => throw _privateConstructorUsedError;
  List<InboxRecipientModel> get toRecipients =>
      throw _privateConstructorUsedError;
  List<InboxRecipientModel> get ccRecipients =>
      throw _privateConstructorUsedError;
  List<InboxAttachmentModel> get attachments =>
      throw _privateConstructorUsedError;
  String? get sentAt => throw _privateConstructorUsedError;
  String? get createdAt => throw _privateConstructorUsedError;

  /// Create a copy of InboxMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $InboxMessageModelCopyWith<InboxMessageModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxMessageModelCopyWith<$Res> {
  factory $InboxMessageModelCopyWith(
    InboxMessageModel value,
    $Res Function(InboxMessageModel) then,
  ) = _$InboxMessageModelCopyWithImpl<$Res, InboxMessageModel>;
  @useResult
  $Res call({
    String id,
    String? recipientId,
    String senderId,
    String senderName,
    String? senderEmail,
    String subject,
    String body,
    String importance,
    String messageType,
    String? parentMessageId,
    String? rootMessageId,
    String? relatedApprovalId,
    bool isDraft,
    bool isRead,
    bool isStarred,
    String folder,
    bool hasAttachments,
    int attachmentCount,
    int recipientCount,
    int threadCount,
    List<InboxRecipientModel> toRecipients,
    List<InboxRecipientModel> ccRecipients,
    List<InboxAttachmentModel> attachments,
    String? sentAt,
    String? createdAt,
  });
}

/// @nodoc
class _$InboxMessageModelCopyWithImpl<$Res, $Val extends InboxMessageModel>
    implements $InboxMessageModelCopyWith<$Res> {
  _$InboxMessageModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of InboxMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? recipientId = freezed,
    Object? senderId = null,
    Object? senderName = null,
    Object? senderEmail = freezed,
    Object? subject = null,
    Object? body = null,
    Object? importance = null,
    Object? messageType = null,
    Object? parentMessageId = freezed,
    Object? rootMessageId = freezed,
    Object? relatedApprovalId = freezed,
    Object? isDraft = null,
    Object? isRead = null,
    Object? isStarred = null,
    Object? folder = null,
    Object? hasAttachments = null,
    Object? attachmentCount = null,
    Object? recipientCount = null,
    Object? threadCount = null,
    Object? toRecipients = null,
    Object? ccRecipients = null,
    Object? attachments = null,
    Object? sentAt = freezed,
    Object? createdAt = freezed,
  }) {
    return _then(
      _value.copyWith(
            id: null == id
                ? _value.id
                : id // ignore: cast_nullable_to_non_nullable
                      as String,
            recipientId: freezed == recipientId
                ? _value.recipientId
                : recipientId // ignore: cast_nullable_to_non_nullable
                      as String?,
            senderId: null == senderId
                ? _value.senderId
                : senderId // ignore: cast_nullable_to_non_nullable
                      as String,
            senderName: null == senderName
                ? _value.senderName
                : senderName // ignore: cast_nullable_to_non_nullable
                      as String,
            senderEmail: freezed == senderEmail
                ? _value.senderEmail
                : senderEmail // ignore: cast_nullable_to_non_nullable
                      as String?,
            subject: null == subject
                ? _value.subject
                : subject // ignore: cast_nullable_to_non_nullable
                      as String,
            body: null == body
                ? _value.body
                : body // ignore: cast_nullable_to_non_nullable
                      as String,
            importance: null == importance
                ? _value.importance
                : importance // ignore: cast_nullable_to_non_nullable
                      as String,
            messageType: null == messageType
                ? _value.messageType
                : messageType // ignore: cast_nullable_to_non_nullable
                      as String,
            parentMessageId: freezed == parentMessageId
                ? _value.parentMessageId
                : parentMessageId // ignore: cast_nullable_to_non_nullable
                      as String?,
            rootMessageId: freezed == rootMessageId
                ? _value.rootMessageId
                : rootMessageId // ignore: cast_nullable_to_non_nullable
                      as String?,
            relatedApprovalId: freezed == relatedApprovalId
                ? _value.relatedApprovalId
                : relatedApprovalId // ignore: cast_nullable_to_non_nullable
                      as String?,
            isDraft: null == isDraft
                ? _value.isDraft
                : isDraft // ignore: cast_nullable_to_non_nullable
                      as bool,
            isRead: null == isRead
                ? _value.isRead
                : isRead // ignore: cast_nullable_to_non_nullable
                      as bool,
            isStarred: null == isStarred
                ? _value.isStarred
                : isStarred // ignore: cast_nullable_to_non_nullable
                      as bool,
            folder: null == folder
                ? _value.folder
                : folder // ignore: cast_nullable_to_non_nullable
                      as String,
            hasAttachments: null == hasAttachments
                ? _value.hasAttachments
                : hasAttachments // ignore: cast_nullable_to_non_nullable
                      as bool,
            attachmentCount: null == attachmentCount
                ? _value.attachmentCount
                : attachmentCount // ignore: cast_nullable_to_non_nullable
                      as int,
            recipientCount: null == recipientCount
                ? _value.recipientCount
                : recipientCount // ignore: cast_nullable_to_non_nullable
                      as int,
            threadCount: null == threadCount
                ? _value.threadCount
                : threadCount // ignore: cast_nullable_to_non_nullable
                      as int,
            toRecipients: null == toRecipients
                ? _value.toRecipients
                : toRecipients // ignore: cast_nullable_to_non_nullable
                      as List<InboxRecipientModel>,
            ccRecipients: null == ccRecipients
                ? _value.ccRecipients
                : ccRecipients // ignore: cast_nullable_to_non_nullable
                      as List<InboxRecipientModel>,
            attachments: null == attachments
                ? _value.attachments
                : attachments // ignore: cast_nullable_to_non_nullable
                      as List<InboxAttachmentModel>,
            sentAt: freezed == sentAt
                ? _value.sentAt
                : sentAt // ignore: cast_nullable_to_non_nullable
                      as String?,
            createdAt: freezed == createdAt
                ? _value.createdAt
                : createdAt // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$InboxMessageModelImplCopyWith<$Res>
    implements $InboxMessageModelCopyWith<$Res> {
  factory _$$InboxMessageModelImplCopyWith(
    _$InboxMessageModelImpl value,
    $Res Function(_$InboxMessageModelImpl) then,
  ) = __$$InboxMessageModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String id,
    String? recipientId,
    String senderId,
    String senderName,
    String? senderEmail,
    String subject,
    String body,
    String importance,
    String messageType,
    String? parentMessageId,
    String? rootMessageId,
    String? relatedApprovalId,
    bool isDraft,
    bool isRead,
    bool isStarred,
    String folder,
    bool hasAttachments,
    int attachmentCount,
    int recipientCount,
    int threadCount,
    List<InboxRecipientModel> toRecipients,
    List<InboxRecipientModel> ccRecipients,
    List<InboxAttachmentModel> attachments,
    String? sentAt,
    String? createdAt,
  });
}

/// @nodoc
class __$$InboxMessageModelImplCopyWithImpl<$Res>
    extends _$InboxMessageModelCopyWithImpl<$Res, _$InboxMessageModelImpl>
    implements _$$InboxMessageModelImplCopyWith<$Res> {
  __$$InboxMessageModelImplCopyWithImpl(
    _$InboxMessageModelImpl _value,
    $Res Function(_$InboxMessageModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of InboxMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? recipientId = freezed,
    Object? senderId = null,
    Object? senderName = null,
    Object? senderEmail = freezed,
    Object? subject = null,
    Object? body = null,
    Object? importance = null,
    Object? messageType = null,
    Object? parentMessageId = freezed,
    Object? rootMessageId = freezed,
    Object? relatedApprovalId = freezed,
    Object? isDraft = null,
    Object? isRead = null,
    Object? isStarred = null,
    Object? folder = null,
    Object? hasAttachments = null,
    Object? attachmentCount = null,
    Object? recipientCount = null,
    Object? threadCount = null,
    Object? toRecipients = null,
    Object? ccRecipients = null,
    Object? attachments = null,
    Object? sentAt = freezed,
    Object? createdAt = freezed,
  }) {
    return _then(
      _$InboxMessageModelImpl(
        id: null == id
            ? _value.id
            : id // ignore: cast_nullable_to_non_nullable
                  as String,
        recipientId: freezed == recipientId
            ? _value.recipientId
            : recipientId // ignore: cast_nullable_to_non_nullable
                  as String?,
        senderId: null == senderId
            ? _value.senderId
            : senderId // ignore: cast_nullable_to_non_nullable
                  as String,
        senderName: null == senderName
            ? _value.senderName
            : senderName // ignore: cast_nullable_to_non_nullable
                  as String,
        senderEmail: freezed == senderEmail
            ? _value.senderEmail
            : senderEmail // ignore: cast_nullable_to_non_nullable
                  as String?,
        subject: null == subject
            ? _value.subject
            : subject // ignore: cast_nullable_to_non_nullable
                  as String,
        body: null == body
            ? _value.body
            : body // ignore: cast_nullable_to_non_nullable
                  as String,
        importance: null == importance
            ? _value.importance
            : importance // ignore: cast_nullable_to_non_nullable
                  as String,
        messageType: null == messageType
            ? _value.messageType
            : messageType // ignore: cast_nullable_to_non_nullable
                  as String,
        parentMessageId: freezed == parentMessageId
            ? _value.parentMessageId
            : parentMessageId // ignore: cast_nullable_to_non_nullable
                  as String?,
        rootMessageId: freezed == rootMessageId
            ? _value.rootMessageId
            : rootMessageId // ignore: cast_nullable_to_non_nullable
                  as String?,
        relatedApprovalId: freezed == relatedApprovalId
            ? _value.relatedApprovalId
            : relatedApprovalId // ignore: cast_nullable_to_non_nullable
                  as String?,
        isDraft: null == isDraft
            ? _value.isDraft
            : isDraft // ignore: cast_nullable_to_non_nullable
                  as bool,
        isRead: null == isRead
            ? _value.isRead
            : isRead // ignore: cast_nullable_to_non_nullable
                  as bool,
        isStarred: null == isStarred
            ? _value.isStarred
            : isStarred // ignore: cast_nullable_to_non_nullable
                  as bool,
        folder: null == folder
            ? _value.folder
            : folder // ignore: cast_nullable_to_non_nullable
                  as String,
        hasAttachments: null == hasAttachments
            ? _value.hasAttachments
            : hasAttachments // ignore: cast_nullable_to_non_nullable
                  as bool,
        attachmentCount: null == attachmentCount
            ? _value.attachmentCount
            : attachmentCount // ignore: cast_nullable_to_non_nullable
                  as int,
        recipientCount: null == recipientCount
            ? _value.recipientCount
            : recipientCount // ignore: cast_nullable_to_non_nullable
                  as int,
        threadCount: null == threadCount
            ? _value.threadCount
            : threadCount // ignore: cast_nullable_to_non_nullable
                  as int,
        toRecipients: null == toRecipients
            ? _value._toRecipients
            : toRecipients // ignore: cast_nullable_to_non_nullable
                  as List<InboxRecipientModel>,
        ccRecipients: null == ccRecipients
            ? _value._ccRecipients
            : ccRecipients // ignore: cast_nullable_to_non_nullable
                  as List<InboxRecipientModel>,
        attachments: null == attachments
            ? _value._attachments
            : attachments // ignore: cast_nullable_to_non_nullable
                  as List<InboxAttachmentModel>,
        sentAt: freezed == sentAt
            ? _value.sentAt
            : sentAt // ignore: cast_nullable_to_non_nullable
                  as String?,
        createdAt: freezed == createdAt
            ? _value.createdAt
            : createdAt // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$InboxMessageModelImpl implements _InboxMessageModel {
  const _$InboxMessageModelImpl({
    required this.id,
    this.recipientId,
    required this.senderId,
    required this.senderName,
    this.senderEmail,
    required this.subject,
    required this.body,
    this.importance = 'NORMAL',
    this.messageType = 'INTERNAL',
    this.parentMessageId,
    this.rootMessageId,
    this.relatedApprovalId,
    this.isDraft = false,
    this.isRead = false,
    this.isStarred = false,
    this.folder = 'INBOX',
    this.hasAttachments = false,
    this.attachmentCount = 0,
    this.recipientCount = 0,
    this.threadCount = 1,
    final List<InboxRecipientModel> toRecipients = const [],
    final List<InboxRecipientModel> ccRecipients = const [],
    final List<InboxAttachmentModel> attachments = const [],
    this.sentAt,
    this.createdAt,
  }) : _toRecipients = toRecipients,
       _ccRecipients = ccRecipients,
       _attachments = attachments;

  @override
  final String id;
  @override
  final String? recipientId;
  @override
  final String senderId;
  @override
  final String senderName;
  @override
  final String? senderEmail;
  @override
  final String subject;
  @override
  final String body;
  @override
  @JsonKey()
  final String importance;
  // "NORMAL", "HIGH", "URGENT"
  @override
  @JsonKey()
  final String messageType;
  @override
  final String? parentMessageId;
  @override
  final String? rootMessageId;
  @override
  final String? relatedApprovalId;
  @override
  @JsonKey()
  final bool isDraft;
  @override
  @JsonKey()
  final bool isRead;
  @override
  @JsonKey()
  final bool isStarred;
  @override
  @JsonKey()
  final String folder;
  @override
  @JsonKey()
  final bool hasAttachments;
  @override
  @JsonKey()
  final int attachmentCount;
  @override
  @JsonKey()
  final int recipientCount;
  @override
  @JsonKey()
  final int threadCount;
  final List<InboxRecipientModel> _toRecipients;
  @override
  @JsonKey()
  List<InboxRecipientModel> get toRecipients {
    if (_toRecipients is EqualUnmodifiableListView) return _toRecipients;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_toRecipients);
  }

  final List<InboxRecipientModel> _ccRecipients;
  @override
  @JsonKey()
  List<InboxRecipientModel> get ccRecipients {
    if (_ccRecipients is EqualUnmodifiableListView) return _ccRecipients;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_ccRecipients);
  }

  final List<InboxAttachmentModel> _attachments;
  @override
  @JsonKey()
  List<InboxAttachmentModel> get attachments {
    if (_attachments is EqualUnmodifiableListView) return _attachments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_attachments);
  }

  @override
  final String? sentAt;
  @override
  final String? createdAt;

  @override
  String toString() {
    return 'InboxMessageModel(id: $id, recipientId: $recipientId, senderId: $senderId, senderName: $senderName, senderEmail: $senderEmail, subject: $subject, body: $body, importance: $importance, messageType: $messageType, parentMessageId: $parentMessageId, rootMessageId: $rootMessageId, relatedApprovalId: $relatedApprovalId, isDraft: $isDraft, isRead: $isRead, isStarred: $isStarred, folder: $folder, hasAttachments: $hasAttachments, attachmentCount: $attachmentCount, recipientCount: $recipientCount, threadCount: $threadCount, toRecipients: $toRecipients, ccRecipients: $ccRecipients, attachments: $attachments, sentAt: $sentAt, createdAt: $createdAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InboxMessageModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.recipientId, recipientId) ||
                other.recipientId == recipientId) &&
            (identical(other.senderId, senderId) ||
                other.senderId == senderId) &&
            (identical(other.senderName, senderName) ||
                other.senderName == senderName) &&
            (identical(other.senderEmail, senderEmail) ||
                other.senderEmail == senderEmail) &&
            (identical(other.subject, subject) || other.subject == subject) &&
            (identical(other.body, body) || other.body == body) &&
            (identical(other.importance, importance) ||
                other.importance == importance) &&
            (identical(other.messageType, messageType) ||
                other.messageType == messageType) &&
            (identical(other.parentMessageId, parentMessageId) ||
                other.parentMessageId == parentMessageId) &&
            (identical(other.rootMessageId, rootMessageId) ||
                other.rootMessageId == rootMessageId) &&
            (identical(other.relatedApprovalId, relatedApprovalId) ||
                other.relatedApprovalId == relatedApprovalId) &&
            (identical(other.isDraft, isDraft) || other.isDraft == isDraft) &&
            (identical(other.isRead, isRead) || other.isRead == isRead) &&
            (identical(other.isStarred, isStarred) ||
                other.isStarred == isStarred) &&
            (identical(other.folder, folder) || other.folder == folder) &&
            (identical(other.hasAttachments, hasAttachments) ||
                other.hasAttachments == hasAttachments) &&
            (identical(other.attachmentCount, attachmentCount) ||
                other.attachmentCount == attachmentCount) &&
            (identical(other.recipientCount, recipientCount) ||
                other.recipientCount == recipientCount) &&
            (identical(other.threadCount, threadCount) ||
                other.threadCount == threadCount) &&
            const DeepCollectionEquality().equals(
              other._toRecipients,
              _toRecipients,
            ) &&
            const DeepCollectionEquality().equals(
              other._ccRecipients,
              _ccRecipients,
            ) &&
            const DeepCollectionEquality().equals(
              other._attachments,
              _attachments,
            ) &&
            (identical(other.sentAt, sentAt) || other.sentAt == sentAt) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt));
  }

  @override
  int get hashCode => Object.hashAll([
    runtimeType,
    id,
    recipientId,
    senderId,
    senderName,
    senderEmail,
    subject,
    body,
    importance,
    messageType,
    parentMessageId,
    rootMessageId,
    relatedApprovalId,
    isDraft,
    isRead,
    isStarred,
    folder,
    hasAttachments,
    attachmentCount,
    recipientCount,
    threadCount,
    const DeepCollectionEquality().hash(_toRecipients),
    const DeepCollectionEquality().hash(_ccRecipients),
    const DeepCollectionEquality().hash(_attachments),
    sentAt,
    createdAt,
  ]);

  /// Create a copy of InboxMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$InboxMessageModelImplCopyWith<_$InboxMessageModelImpl> get copyWith =>
      __$$InboxMessageModelImplCopyWithImpl<_$InboxMessageModelImpl>(
        this,
        _$identity,
      );
}

abstract class _InboxMessageModel implements InboxMessageModel {
  const factory _InboxMessageModel({
    required final String id,
    final String? recipientId,
    required final String senderId,
    required final String senderName,
    final String? senderEmail,
    required final String subject,
    required final String body,
    final String importance,
    final String messageType,
    final String? parentMessageId,
    final String? rootMessageId,
    final String? relatedApprovalId,
    final bool isDraft,
    final bool isRead,
    final bool isStarred,
    final String folder,
    final bool hasAttachments,
    final int attachmentCount,
    final int recipientCount,
    final int threadCount,
    final List<InboxRecipientModel> toRecipients,
    final List<InboxRecipientModel> ccRecipients,
    final List<InboxAttachmentModel> attachments,
    final String? sentAt,
    final String? createdAt,
  }) = _$InboxMessageModelImpl;

  @override
  String get id;
  @override
  String? get recipientId;
  @override
  String get senderId;
  @override
  String get senderName;
  @override
  String? get senderEmail;
  @override
  String get subject;
  @override
  String get body;
  @override
  String get importance; // "NORMAL", "HIGH", "URGENT"
  @override
  String get messageType;
  @override
  String? get parentMessageId;
  @override
  String? get rootMessageId;
  @override
  String? get relatedApprovalId;
  @override
  bool get isDraft;
  @override
  bool get isRead;
  @override
  bool get isStarred;
  @override
  String get folder;
  @override
  bool get hasAttachments;
  @override
  int get attachmentCount;
  @override
  int get recipientCount;
  @override
  int get threadCount;
  @override
  List<InboxRecipientModel> get toRecipients;
  @override
  List<InboxRecipientModel> get ccRecipients;
  @override
  List<InboxAttachmentModel> get attachments;
  @override
  String? get sentAt;
  @override
  String? get createdAt;

  /// Create a copy of InboxMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$InboxMessageModelImplCopyWith<_$InboxMessageModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
