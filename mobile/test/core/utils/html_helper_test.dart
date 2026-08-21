import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/utils/html_helper.dart';

void main() {
  group('HtmlHelper Unit Tests', () {
    test('toPlainText strips complex HTML tags and decodes entities properly', () {
      const html = '<p><br>나야 나~ 너는 잘 지내지?<br></p><hr><p><strong>----- 원본 메시지 -----</strong><br><strong>보낸 사람:</strong> superadmin &lt;profavor.lin@mplatform.com&gt;</p>';
      final result = HtmlHelper.toPlainText(html);

      expect(result, contains('나야 나~ 너는 잘 지내지?'));
      expect(result, contains('----- 원본 메시지 -----'));
      expect(result, contains('보낸 사람: superadmin <profavor.lin@mplatform.com>'));
      expect(result.contains('<p>'), isFalse);
      expect(result.contains('</p>'), isFalse);
      expect(result.contains('<br>'), isFalse);
      expect(result.contains('<hr>'), isFalse);
      expect(result.contains('<strong>'), isFalse);
      expect(result.contains('&lt;'), isFalse);
      expect(result.contains('&gt;'), isFalse);
    });

    test('toPlainText handles blockquotes and nested tags', () {
      const html = '<p>왜 불러?<br><br></p><hr><blockquote><p>아야어여</p></blockquote><p></p>';
      final result = HtmlHelper.toPlainText(html);

      expect(result, contains('왜 불러?'));
      expect(result, contains('아야어여'));
      expect(result.contains('<blockquote>'), isFalse);
      expect(result.contains('</blockquote>'), isFalse);
    });

    test('isHtml returns true for HTML text and false for plain text', () {
      expect(HtmlHelper.isHtml('<p>hello</p>'), isTrue);
      expect(HtmlHelper.isHtml('hello <br/> world'), isTrue);
      expect(HtmlHelper.isHtml('plain text with no tags'), isFalse);
      expect(HtmlHelper.isHtml(''), isFalse);
      expect(HtmlHelper.isHtml(null), isFalse);
    });
  });
}
