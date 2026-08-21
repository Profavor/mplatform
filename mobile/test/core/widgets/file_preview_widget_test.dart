import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_widget_from_html/flutter_widget_from_html.dart';
import 'package:mplatform_mobile/core/config/app_config.dart';
import 'package:mplatform_mobile/core/widgets/file_preview_widget.dart';

void main() {
  group('FilePreviewWidget Tests (TDD - HTML & Images)', () {
    test('extractFileUrls: extracts single and multiple file URLs correctly', () {
      final urls1 = FilePreviewWidget.extractFileUrls('/api/files/download/image.jpg');
      expect(urls1, equals(['/api/files/download/image.jpg']));

      final urls2 = FilePreviewWidget.extractFileUrls('["/api/files/download/1.png", "/api/files/download/2.jpg"]');
      expect(urls2, equals(['/api/files/download/1.png', '/api/files/download/2.jpg']));

      final urls3 = FilePreviewWidget.extractFileUrls('[/api/files/download/sample.jpg?name=test.jpg]');
      expect(urls3, equals(['/api/files/download/sample.jpg?name=test.jpg']));

      // HTML content should not be extracted as raw file URLs
      final html = '<p><img src="/api/files/download/test.png">Sample</p>';
      final urlsHtml = FilePreviewWidget.extractFileUrls(html);
      expect(urlsHtml, isEmpty);
    });

    test('isHtmlContent: detects HTML tags accurately', () {
      expect(FilePreviewWidget.isHtmlContent('<p>Hello <strong>World</strong></p>'), isTrue);
      expect(FilePreviewWidget.isHtmlContent('plain text'), isFalse);
      expect(FilePreviewWidget.isHtmlContent('plain text', fieldType: 'HTML'), isTrue);
    });

    test('processHtmlContent: appends baseUrl and token parameter correctly', () {
      final html = '<p><img src="/api/files/download/05c71b0.png?name=image.png&size=68139" /></p>';
      final processed = FilePreviewWidget.processHtmlContent(
        html,
        'http://localhost:8080',
        token: 'test_token_123',
      );
      expect(processed, contains('src="http://localhost:8080/api/files/download/05c71b0.png?name=image.png&size=68139&token=test_token_123"'));

      // Case when URL has no existing query params
      final htmlSimple = '<p><img src="/api/files/download/photo.jpg" /></p>';
      final processedSimple = FilePreviewWidget.processHtmlContent(
        htmlSimple,
        'http://localhost:8080',
        token: 'test_token_123',
      );
      expect(processedSimple, contains('src="http://localhost:8080/api/files/download/photo.jpg?token=test_token_123"'));
    });

    testWidgets('renders HTML content with HtmlWidget', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            appConfigProvider.overrideWithValue(
              const AppConfig(
                issuer: 'http://localhost:8080',
                apiBaseUrl: 'http://localhost:8080',
                clientId: 'test-client',
                redirectUri: 'http://localhost:5000',
              ),
            ),
          ],
          child: const MaterialApp(
            home: Scaffold(
              body: FilePreviewWidget(
                rawValue: '<p>테스트 <strong>굵은 글씨</strong></p>',
                fieldType: 'HTML',
              ),
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();
      expect(find.byType(HtmlWidget), findsOneWidget);
    });
  });
}
