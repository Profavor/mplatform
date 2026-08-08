import 'package:freezed_annotation/freezed_annotation.dart';

part 'dq_trend_item_model.freezed.dart';
part 'dq_trend_item_model.g.dart';

@freezed
class DqTrendItemModel with _$DqTrendItemModel {
  const factory DqTrendItemModel({
    required String date,
    required int count,
  }) = _DqTrendItemModel;

  factory DqTrendItemModel.fromJson(Map<String, dynamic> json) =>
      _$DqTrendItemModelFromJson(json);
}
