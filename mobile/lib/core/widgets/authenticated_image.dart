import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/config/app_config.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';

/// Dio API 클라이언트(인증 인터셉터)를 통해 보안 이미지를 로드하여 표시하는 위젯.
///
/// 상대 경로(`/api/files/...`)나 절대 URL(`http://...`)을 자동으로 정규화하며,
/// Authorization Bearer 토큰을 실어 안전하게 이미지를 로드합니다.
class AuthenticatedImage extends ConsumerStatefulWidget {
  final String url;
  final double? width;
  final double? height;
  final BoxFit? fit;
  final BorderRadius? borderRadius;
  final Widget? placeholder;
  final Widget? errorWidget;

  const AuthenticatedImage({
    super.key,
    required this.url,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.borderRadius,
    this.placeholder,
    this.errorWidget,
  });

  @override
  ConsumerState<AuthenticatedImage> createState() => _AuthenticatedImageState();
}

class _AuthenticatedImageState extends ConsumerState<AuthenticatedImage> {
  Uint8List? _bytes;
  bool _isLoading = true;
  bool _hasError = false;
  String? _lastLoadedUrl;

  @override
  void initState() {
    super.initState();
    _loadImage();
  }

  @override
  void didUpdateWidget(covariant AuthenticatedImage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.url != widget.url) {
      _loadImage();
    }
  }

  Future<void> _loadImage() async {
    if (widget.url.trim().isEmpty) {
      if (mounted) {
        setState(() {
          _hasError = true;
          _isLoading = false;
        });
      }
      return;
    }

    final appConfig = ref.read(appConfigProvider);
    final baseUrl = appConfig.apiBaseUrl;

    // 상대 경로를 baseUrl과 결합
    String fullUrl = widget.url;
    if (!fullUrl.startsWith('http://') && !fullUrl.startsWith('https://')) {
      fullUrl = fullUrl.startsWith('/') ? '$baseUrl$fullUrl' : '$baseUrl/$fullUrl';
    }

    if (_lastLoadedUrl == fullUrl && _bytes != null) {
      return; // 이미 동일 URL로 로드된 상태
    }

    setState(() {
      _isLoading = true;
      _hasError = false;
    });

    try {
      final dio = ref.read(dioProvider);
      final response = await dio.get<List<int>>(
        fullUrl,
        options: Options(responseType: ResponseType.bytes),
      );

      if (mounted && response.data != null) {
        setState(() {
          _bytes = Uint8List.fromList(response.data!);
          _isLoading = false;
          _hasError = false;
          _lastLoadedUrl = fullUrl;
        });
      } else if (mounted) {
        setState(() {
          _hasError = true;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _hasError = true;
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    Widget content;

    if (_isLoading) {
      content = widget.placeholder ??
          Container(
            width: widget.width,
            height: widget.height ?? 120,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: Colors.grey.shade100,
              borderRadius: widget.borderRadius,
            ),
            child: const SizedBox(
              width: 24,
              height: 24,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          );
    } else if (_hasError || _bytes == null) {
      content = widget.errorWidget ??
          Container(
            width: widget.width,
            height: widget.height ?? 120,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: Colors.grey.shade100,
              borderRadius: widget.borderRadius,
            ),
            child: const Icon(Icons.broken_image, color: Colors.grey, size: 32),
          );
    } else {
      content = Image.memory(
        _bytes!,
        width: widget.width,
        height: widget.height,
        fit: widget.fit,
      );
    }

    if (widget.borderRadius != null) {
      return ClipRRect(
        borderRadius: widget.borderRadius!,
        child: content,
      );
    }

    return content;
  }
}
