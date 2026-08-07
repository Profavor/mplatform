// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'classification_node_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ClassificationNodeModelImpl _$$ClassificationNodeModelImplFromJson(
  Map<String, dynamic> json,
) => _$ClassificationNodeModelImpl(
  id: json['id'] as String,
  domainId: json['domainId'] as String?,
  name: json['name'] == null ? '' : _parseLocalizedName(json['name']),
  path: json['path'] as String? ?? '',
  depth: (json['depth'] as num?)?.toInt() ?? 0,
  children:
      (json['children'] as List<dynamic>?)
          ?.map(
            (e) => ClassificationNodeModel.fromJson(e as Map<String, dynamic>),
          )
          .toList() ??
      const [],
);

Map<String, dynamic> _$$ClassificationNodeModelImplToJson(
  _$ClassificationNodeModelImpl instance,
) => <String, dynamic>{
  'id': instance.id,
  'domainId': instance.domainId,
  'name': instance.name,
  'path': instance.path,
  'depth': instance.depth,
  'children': instance.children,
};
