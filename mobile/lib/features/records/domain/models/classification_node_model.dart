import 'dart:convert';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'classification_node_model.freezed.dart';
part 'classification_node_model.g.dart';

String _parseLocalizedName(dynamic value) {
  if (value is String) return value;
  if (value is Map) {
    return jsonEncode(value);
  }
  return '';
}

@freezed
class ClassificationNodeModel with _$ClassificationNodeModel {
  const factory ClassificationNodeModel({
    required String id,
    String? domainId,
    @JsonKey(fromJson: _parseLocalizedName) @Default('') String name,
    @Default('') String path,
    @Default(0) int depth,
    @Default([]) List<ClassificationNodeModel> children,
  }) = _ClassificationNodeModel;

  factory ClassificationNodeModel.fromJson(Map<String, dynamic> json) => _$ClassificationNodeModelFromJson(json);
}
