import 'dart:convert';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'record_item.freezed.dart';
part 'record_item.g.dart';

String _idFromJson(dynamic value) => value?.toString() ?? '';

Map<String, dynamic> _dataFromJson(dynamic value) {
  if (value is Map) return value as Map<String, dynamic>;
  if (value is String) {
    try {
      return jsonDecode(value) as Map<String, dynamic>;
    } catch (_) {}
  }
  return <String, dynamic>{};
}

Object? _readDomainId(Map<dynamic, dynamic> json, String key) {
  if (json.containsKey('domainId')) return json['domainId'];
  if (json.containsKey('node') && json['node'] is Map) {
    final node = json['node'] as Map;
    if (node.containsKey('domain') && node['domain'] is Map) {
      return node['domain']['id'];
    }
  }
  return null;
}

Object? _readNodeId(Map<dynamic, dynamic> json, String key) {
  if (json.containsKey('node') && json['node'] is Map) {
    return json['node']['id'];
  }
  return null;
}

Object? _readNodeName(Map<dynamic, dynamic> json, String key) {
  if (json.containsKey('node') && json['node'] is Map) {
    return json['node']['name'];
  }
  return null;
}

Object? _readNodePath(Map<dynamic, dynamic> json, String key) {
  if (json.containsKey('node') && json['node'] is Map) {
    return json['node']['path'];
  }
  return null;
}

String _parseLocalizedName(dynamic value) {
  if (value is String) return value;
  if (value is Map) {
    return jsonEncode(value);
  }
  return '';
}

@freezed
class RecordItem with _$RecordItem {
  const factory RecordItem({
    @JsonKey(name: 'id', fromJson: _idFromJson) required String recordId,
    @JsonKey(readValue: _readDomainId, fromJson: _idFromJson) @Default('') String domainId,
    @JsonKey(name: 'data', fromJson: _dataFromJson) @Default({}) Map<String, dynamic> attributes,
    @JsonKey(readValue: _readNodeId, fromJson: _idFromJson) String? nodeId,
    @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName) @Default('') String nodeName,
    @JsonKey(readValue: _readNodePath) @Default('') String nodePath,
    String? createdBy,
    String? createdAt,
    String? updatedAt,
  }) = _RecordItem;

  factory RecordItem.fromJson(Map<String, dynamic> json) => _$RecordItemFromJson(json);
}
