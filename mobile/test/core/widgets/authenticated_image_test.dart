import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/config/app_config.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/widgets/authenticated_image.dart';

// Valid 1x1 transparent PNG binary bytes
final kTransparentImageBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49,
  0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06,
  0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4, 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44,
  0x41, 0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D,
  0x0A, 0x2D, 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42,
  0x60, 0x82,
];

class FakeDioAdapter implements HttpClientAdapter {
  final List<int>? responseBytes;
  final bool shouldFail;

  FakeDioAdapter({this.responseBytes, this.shouldFail = false});

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    if (shouldFail) {
      throw DioException(
        requestOptions: options,
        error: '401 Unauthorized',
        type: DioExceptionType.badResponse,
        response: Response(requestOptions: options, statusCode: 401),
      );
    }
    return ResponseBody.fromBytes(
      responseBytes ?? kTransparentImageBytes,
      200,
      headers: {
        Headers.contentTypeHeader: ['image/png'],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  group('AuthenticatedImage Tests (TDD - Token Authentication & Image Loading)', () {
    testWidgets('renders placeholder and then displays image on success', (tester) async {
      final dio = Dio(BaseOptions(baseUrl: 'http://localhost:8080'));
      dio.httpClientAdapter = FakeDioAdapter();

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
            dioProvider.overrideWithValue(dio),
          ],
          child: const MaterialApp(
            home: Scaffold(
              body: AuthenticatedImage(
                url: '/api/files/download/test.png',
              ),
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();
      expect(find.byType(AuthenticatedImage), findsOneWidget);
      expect(find.byType(Image), findsOneWidget);
      expect(find.byIcon(Icons.broken_image), findsNothing);
    });

    testWidgets('renders broken image icon on network error (e.g. 401 Unauthorized)', (tester) async {
      final dio = Dio(BaseOptions(baseUrl: 'http://localhost:8080'));
      dio.httpClientAdapter = FakeDioAdapter(shouldFail: true);

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
            dioProvider.overrideWithValue(dio),
          ],
          child: const MaterialApp(
            home: Scaffold(
              body: AuthenticatedImage(
                url: '/api/files/download/test.png',
              ),
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();
      expect(find.byIcon(Icons.broken_image), findsOneWidget);
    });
  });
}
