// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_definition.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$FieldDefinitionImpl _$$FieldDefinitionImplFromJson(
  Map<String, dynamic> json,
) => _$FieldDefinitionImpl(
  id: json['id'] as String,
  fieldName: _readFieldName(json, 'fieldName') as String,
  fieldLabel: _parseLocalizedName(_readFieldLabel(json, 'fieldLabel')),
  fieldType: _readFieldType(json, 'fieldType') as String,
  required: json['required'] as bool? ?? false,
  showInList: _readShowInList(json, 'showInList') as bool? ?? false,
  options:
      (_readOptions(json, 'options') as List<dynamic>?)
          ?.map((e) => e as String)
          .toList() ??
      const [],
  rawOptions: _readRawOptions(json, 'rawOptions') as String?,
  displayOrder: (_readDisplayOrder(json, 'displayOrder') as num?)?.toInt() ?? 0,
  isEncrypted: _readIsEncrypted(json, 'isEncrypted') as bool? ?? false,
  maskingPattern: json['maskingPattern'] as String?,
  groupName: _readLocalizedGroup(json, 'groupName') == null
      ? ''
      : _parseLocalizedName(_readLocalizedGroup(json, 'groupName')),
  sectorName: _readLocalizedSector(json, 'sectorName') == null
      ? ''
      : _parseLocalizedName(_readLocalizedSector(json, 'sectorName')),
);

Map<String, dynamic> _$$FieldDefinitionImplToJson(
  _$FieldDefinitionImpl instance,
) => <String, dynamic>{
  'id': instance.id,
  'fieldName': instance.fieldName,
  'fieldLabel': instance.fieldLabel,
  'fieldType': instance.fieldType,
  'required': instance.required,
  'showInList': instance.showInList,
  'options': instance.options,
  'rawOptions': instance.rawOptions,
  'displayOrder': instance.displayOrder,
  'isEncrypted': instance.isEncrypted,
  'maskingPattern': instance.maskingPattern,
  'groupName': instance.groupName,
  'sectorName': instance.sectorName,
};
