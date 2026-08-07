import 'dart:convert';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'domain_model.freezed.dart';
part 'domain_model.g.dart';

String _parseLocalizedName(dynamic value) {
  if (value is String) return value;
  if (value is Map) {
    return jsonEncode(value);
  }
  return '';
}

@freezed
class DomainModel with _$DomainModel {
  const factory DomainModel({
    required String id,
    @JsonKey(fromJson: _parseLocalizedName) required String name,
    @JsonKey(fromJson: _parseLocalizedName) @Default('') String description,
    @Default(true) bool active,
    String? identifierFieldId,
    String? displayNameFieldId,
    String? descriptionFieldId,
  }) = _DomainModel;

  factory DomainModel.fromJson(Map<String, dynamic> json) => _$DomainModelFromJson(json);
}
