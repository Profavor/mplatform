import 'dart:convert';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'field_definition.freezed.dart';
part 'field_definition.g.dart';

String _parseLocalizedName(dynamic value) {
  if (value is String) return value;
  if (value is Map) {
    return jsonEncode(value);
  }
  return '';
}

Object? _readFieldName(Map<dynamic, dynamic> json, String key) {
  return json['fieldKey'] ?? json['fieldName'] ?? json['key'] ?? '';
}

Object? _readFieldLabel(Map<dynamic, dynamic> json, String key) {
  return json['name'] ?? json['fieldLabel'] ?? json['label'] ?? '';
}

Object? _readFieldType(Map<dynamic, dynamic> json, String key) {
  return json['type'] ?? json['fieldType'] ?? 'STRING';
}

Object? _readShowInList(Map<dynamic, dynamic> json, String key) {
  return json['isTable'] ?? json['showInList'] ?? false;
}

Object? _readIsEncrypted(Map<dynamic, dynamic> json, String key) {
  return json['isEncrypted'] ?? json['is_encrypted'] ?? json['encrypted'] ?? false;
}

Object? _readMaskingPattern(Map<dynamic, dynamic> json, String key) {
  return json['maskingPattern'] ?? json['masking_pattern'];
}

Object? _readDisplayOrder(Map<dynamic, dynamic> json, String key) {
  return json['fieldOrder'] ?? json['displayOrder'] ?? 0;
}

Object? _readOptions(Map<dynamic, dynamic> json, String key) {
  final val = json['options'];
  if (val is List) return val;
  if (val is Map) {
    if (val.isEmpty) return [];
    return val.values.map((e) => e.toString()).toList();
  }
  if (val is String) {
    try {
      final decoded = jsonDecode(val);
      if (decoded is List) return decoded;
      if (decoded is Map) return decoded.values.map((e) => e.toString()).toList();
    } catch (_) {}
  }
  return [];
}

Object? _readLocalizedGroup(Map<dynamic, dynamic> json, String key) {
  if (json['fieldGroup'] != null && json['fieldGroup'] is Map) {
    return json['fieldGroup']['name'];
  }
  return json['group'];
}

Object? _readLocalizedSector(Map<dynamic, dynamic> json, String key) {
  if (json['fieldGroup'] != null && json['fieldGroup'] is Map) {
    final fg = json['fieldGroup'] as Map;
    if (fg['sector'] != null && fg['sector'] is Map) {
      return fg['sector']['name'];
    }
  }
  return json['sector'];
}

@freezed
class FieldDefinition with _$FieldDefinition {
  const factory FieldDefinition({
    required String id,
    @JsonKey(readValue: _readFieldName) required String fieldName,
    @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName) required String fieldLabel,
    @JsonKey(readValue: _readFieldType) required String fieldType,
    @Default(false) bool required,
    @JsonKey(readValue: _readShowInList) @Default(false) bool showInList,
    @JsonKey(readValue: _readOptions) @Default([]) List<String> options,
    @JsonKey(readValue: _readDisplayOrder) @Default(0) int displayOrder,
    @JsonKey(readValue: _readIsEncrypted) @Default(false) bool isEncrypted,
    @JsonKey(readValue: _readMaskingPattern) String? maskingPattern,
    @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName) @Default('') String groupName,
    @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName) @Default('') String sectorName,
  }) = _FieldDefinition;

  factory FieldDefinition.fromJson(Map<String, dynamic> json) => _$FieldDefinitionFromJson(json);
}
