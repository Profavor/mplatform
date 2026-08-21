import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';

class HtmlHelper {
  /// HTML 태그를 제거하고 HTML 엔티티를 디코딩하여 깨끗한 일반 텍스트 요약(Snippet)을 생성합니다.
  static String toPlainText(String? html) {
    if (html == null || html.trim().isEmpty) return '';
    String text = html;

    // 1. Convert block elements and line breaks to spacing
    text = text.replaceAll(RegExp(r'<br\s*/?>', caseSensitive: false), ' ');
    text = text.replaceAll(RegExp(r'</?(?:p|div|tr|li|h[1-6]|blockquote|hr|section|article)[^>]*>', caseSensitive: false), ' ');

    // 2. Strip all remaining HTML tags
    text = text.replaceAll(RegExp(r'<[^>]*>'), '');

    // 3. Decode common HTML entities
    text = text
        .replaceAll('&nbsp;', ' ')
        .replaceAll('&amp;', '&')
        .replaceAll('&lt;', '<')
        .replaceAll('&gt;', '>')
        .replaceAll('&quot;', '"')
        .replaceAll('&#39;', "'")
        .replaceAll('&apos;', "'");

    // 4. Decode numeric character references if any (e.g. &#10;)
    text = text.replaceAllMapped(RegExp(r'&#(\d+);'), (match) {
      final code = int.tryParse(match.group(1) ?? '');
      return code != null ? String.fromCharCode(code) : match.group(0)!;
    });

    // 5. Normalize consecutive spaces
    text = text.replaceAll(RegExp(r'\s+'), ' ').trim();

    return text;
  }

  /// 문자열이 HTML 태그를 포함하고 있는지 판별합니다.
  static bool isHtml(String? text) {
    if (text == null || text.trim().isEmpty) return false;
    return RegExp(r'<(\/?[a-z][a-z0-9]*)\b[^>]*>', caseSensitive: false).hasMatch(text);
  }

  /// HTML 본문 또는 텍스트 내의 사용자 UUID를 사용자 이름으로 치환하거나 마스킹 처리합니다.
  static String replaceUserUuids(String? text, List<UserModel> users) {
    if (text == null || text.isEmpty) return '';
    final uuidRegex = RegExp(r'[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}');
    return text.replaceAllMapped(uuidRegex, (match) {
      final uuid = match.group(0)!;
      final matchedUser = users.where((u) => u.id.toLowerCase() == uuid.toLowerCase()).firstOrNull;
      if (matchedUser != null && matchedUser.username.isNotEmpty) {
        return matchedUser.username;
      }
      return UuidFormatter.format(uuid, prefix: 'USER');
    });
  }
}
