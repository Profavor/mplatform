import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';

part 'records_page_response.freezed.dart';
part 'records_page_response.g.dart';

@freezed
class RecordsPageResponse with _$RecordsPageResponse {
  const factory RecordsPageResponse({
    @Default([]) List<RecordItem> content,
    @Default(0) int totalElements,
    @Default(0) int totalPages,
    @Default(0) int number,
    @Default(20) int size,
    @Default(true) bool first,
    @Default(true) bool last,
  }) = _RecordsPageResponse;

  factory RecordsPageResponse.fromJson(Map<String, dynamic> json) => _$RecordsPageResponseFromJson(json);
}
