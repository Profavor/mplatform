// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'records_page_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$RecordsPageResponseImpl _$$RecordsPageResponseImplFromJson(
  Map<String, dynamic> json,
) => _$RecordsPageResponseImpl(
  content:
      (json['content'] as List<dynamic>?)
          ?.map((e) => RecordItem.fromJson(e as Map<String, dynamic>))
          .toList() ??
      const [],
  totalElements: (json['totalElements'] as num?)?.toInt() ?? 0,
  totalPages: (json['totalPages'] as num?)?.toInt() ?? 0,
  number: (json['number'] as num?)?.toInt() ?? 0,
  size: (json['size'] as num?)?.toInt() ?? 20,
  first: json['first'] as bool? ?? true,
  last: json['last'] as bool? ?? true,
);

Map<String, dynamic> _$$RecordsPageResponseImplToJson(
  _$RecordsPageResponseImpl instance,
) => <String, dynamic>{
  'content': instance.content,
  'totalElements': instance.totalElements,
  'totalPages': instance.totalPages,
  'number': instance.number,
  'size': instance.size,
  'first': instance.first,
  'last': instance.last,
};
