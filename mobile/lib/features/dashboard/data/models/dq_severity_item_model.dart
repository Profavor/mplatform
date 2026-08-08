import 'package:freezed_annotation/freezed_annotation.dart';

part 'dq_severity_item_model.freezed.dart';
part 'dq_severity_item_model.g.dart';

@freezed
class DqSeverityItemModel with _$DqSeverityItemModel {
  const factory DqSeverityItemModel({
    required String severity,
    required int count,
  }) = _DqSeverityItemModel;

  factory DqSeverityItemModel.fromJson(Map<String, dynamic> json) =>
      _$DqSeverityItemModelFromJson(json);
}
