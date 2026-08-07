// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_item.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$RecordItemImpl _$$RecordItemImplFromJson(Map<String, dynamic> json) =>
    _$RecordItemImpl(
      recordId: _idFromJson(json['id']),
      domainId: _readDomainId(json, 'domainId') == null
          ? ''
          : _idFromJson(_readDomainId(json, 'domainId')),
      attributes: json['data'] == null ? const {} : _dataFromJson(json['data']),
      nodeId: _idFromJson(_readNodeId(json, 'nodeId')),
      nodeName: _readNodeName(json, 'nodeName') == null
          ? ''
          : _parseLocalizedName(_readNodeName(json, 'nodeName')),
      nodePath: _readNodePath(json, 'nodePath') as String? ?? '',
      createdBy: json['createdBy'] as String?,
      createdAt: json['createdAt'] as String?,
      updatedAt: json['updatedAt'] as String?,
    );

Map<String, dynamic> _$$RecordItemImplToJson(_$RecordItemImpl instance) =>
    <String, dynamic>{
      'id': instance.recordId,
      'domainId': instance.domainId,
      'data': instance.attributes,
      'nodeId': instance.nodeId,
      'nodeName': instance.nodeName,
      'nodePath': instance.nodePath,
      'createdBy': instance.createdBy,
      'createdAt': instance.createdAt,
      'updatedAt': instance.updatedAt,
    };
