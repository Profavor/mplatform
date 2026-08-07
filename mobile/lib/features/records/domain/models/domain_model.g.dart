// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'domain_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$DomainModelImpl _$$DomainModelImplFromJson(Map<String, dynamic> json) =>
    _$DomainModelImpl(
      id: json['id'] as String,
      name: _parseLocalizedName(json['name']),
      description: json['description'] == null
          ? ''
          : _parseLocalizedName(json['description']),
      active: json['active'] as bool? ?? true,
      identifierFieldId: json['identifierFieldId'] as String?,
      displayNameFieldId: json['displayNameFieldId'] as String?,
      descriptionFieldId: json['descriptionFieldId'] as String?,
    );

Map<String, dynamic> _$$DomainModelImplToJson(_$DomainModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': instance.description,
      'active': instance.active,
      'identifierFieldId': instance.identifierFieldId,
      'displayNameFieldId': instance.displayNameFieldId,
      'descriptionFieldId': instance.descriptionFieldId,
    };
