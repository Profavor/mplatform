import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_widget_from_html/flutter_widget_from_html.dart';
import 'package:mplatform_mobile/core/config/app_config.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/widgets/authenticated_image.dart';
import 'package:mplatform_mobile/core/widgets/image_viewer_dialog.dart';
import 'package:url_launcher/url_launcher.dart';

class FilePreviewWidget extends ConsumerWidget {
  final dynamic rawValue;
  final String? fieldType;
  final TextStyle? fallbackTextStyle;

  const FilePreviewWidget({
    super.key,
    required this.rawValue,
    this.fieldType,
    this.fallbackTextStyle,
  });

  /// Checks if value is an HTML/RichText string
  static bool isHtmlContent(dynamic value, {String? fieldType}) {
    if (value == null) return false;
    final type = fieldType?.toUpperCase();
    if (type == 'HTML' || type == 'RICHTEXT' || type == 'RICH_TEXT') {
      return true;
    }
    final str = value.toString().trim();
    if (str.isEmpty) return false;

    // Check for common HTML tags
    final htmlTagRegex = RegExp(r'<(\/?[a-z][a-z0-9]*)\b[^>]*>', caseSensitive: false);
    return htmlTagRegex.hasMatch(str);
  }

  /// Extracts file URLs from dynamic value (String, List, JSON array string).
  static List<String> extractFileUrls(dynamic value, {String? fieldType}) {
    if (value == null) return [];
    if (isHtmlContent(value, fieldType: fieldType)) {
      return []; // Don't treat entire HTML document as a raw file URL list
    }
    if (value is List) {
      return value.map((e) => e.toString().trim()).where((e) => e.isNotEmpty).toList();
    }
    final str = value.toString().trim();
    if (str.isEmpty || str == '-') return [];

    if (str.startsWith('[') && str.endsWith(']')) {
      try {
        final decoded = jsonDecode(str);
        if (decoded is List) {
          return decoded.map((e) => e.toString().trim()).where((e) => e.isNotEmpty).toList();
        }
      } catch (_) {
        final inner = str.substring(1, str.length - 1).trim();
        if (inner.isNotEmpty) {
          final items = inner.split(',');
          final List<String> result = [];
          for (final item in items) {
            String cleaned = item.trim();
            if (cleaned.startsWith('"') && cleaned.endsWith('"')) {
              cleaned = cleaned.substring(1, cleaned.length - 1);
            } else if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
              cleaned = cleaned.substring(1, cleaned.length - 1);
            }
            if (cleaned.isNotEmpty && (cleaned.startsWith('/') || cleaned.startsWith('http://') || cleaned.startsWith('https://'))) {
              result.add(cleaned);
            }
          }
          return result;
        }
      }
    }

    if (str.startsWith('/api/files/download') || str.startsWith('http://') || str.startsWith('https://')) {
      return [str];
    }

    return [];
  }

  /// Determines if a given URL is an image.
  static bool isImageUrl(String url, {String? fieldType}) {
    if (fieldType != null && (fieldType.toUpperCase() == 'IMAGE' || fieldType.toUpperCase() == 'PHOTO')) {
      return true;
    }
    final lower = url.toLowerCase();
    final imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg'];
    for (final ext in imageExtensions) {
      if (lower.contains(ext)) return true;
    }
    return false;
  }

  /// Extracts clean file name from URL
  static String extractFileName(String url) {
    try {
      final uri = Uri.parse(url);
      if (uri.queryParameters.containsKey('name')) {
        return uri.queryParameters['name']!;
      }
      final segments = uri.pathSegments;
      if (segments.isNotEmpty) {
        return Uri.decodeComponent(segments.last);
      }
    } catch (_) {}
    return url;
  }

  /// Replaces relative image URLs with absolute baseUrl and appends token parameter for authentication
  static String processHtmlContent(String html, String baseUrl, {String? token}) {
    var processed = html;
    // Replace src="/api/files/... with src="http://baseUrl/api/files/...
    processed = processed.replaceAllMapped(
      RegExp(r'src=["\x27](\/api\/files\/[^\s"\x27]+)["\x27]'),
      (match) {
        final path = match.group(1)!;
        String fullUrl = '$baseUrl$path';
        if (token != null && token.isNotEmpty && !fullUrl.contains('token=')) {
          final delimiter = fullUrl.contains('?') ? '&' : '?';
          fullUrl = '$fullUrl${delimiter}token=$token';
        }
        return 'src="$fullUrl"';
      },
    );
    return processed;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final appConfig = ref.watch(appConfigProvider);
    final baseUrl = appConfig.apiBaseUrl;
    final token = ref.watch(accessTokenProvider).valueOrNull;

    // 1. HTML / RichText Content Rendering
    if (isHtmlContent(rawValue, fieldType: fieldType)) {
      final rawHtml = rawValue.toString();
      final processedHtml = processHtmlContent(rawHtml, baseUrl, token: token);

      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
        child: HtmlWidget(
          processedHtml,
          textStyle: fallbackTextStyle ?? const TextStyle(fontSize: 13, color: Colors.black87),
          customWidgetBuilder: (element) {
            if (element.localName == 'img') {
              final src = element.attributes['src'];
              if (src != null && src.isNotEmpty) {
                return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4.0),
                  child: GestureDetector(
                    onTap: () => ImageViewerDialog.show(
                      context,
                      imageUrl: src,
                      title: extractFileName(src),
                    ),
                    child: AuthenticatedImage(
                      url: src,
                      fit: BoxFit.contain,
                      borderRadius: BorderRadius.circular(6),
                    ),
                  ),
                );
              }
            }
            return null;
          },
          onTapImage: (imageMetadata) {
            final src = imageMetadata.sources.firstOrNull?.url;
            if (src != null) {
              _showImageDialog(context, src, extractFileName(src));
            }
          },
        ),
      );
    }

    // 2. File / Image URL Rendering
    final urls = extractFileUrls(rawValue, fieldType: fieldType);
    if (urls.isEmpty) {
      final text = rawValue != null ? rawValue.toString() : '-';
      return Text(text, style: fallbackTextStyle ?? const TextStyle(fontSize: 13, color: Colors.black87));
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: urls.map((rawUrl) {
        final fullUrl = rawUrl.startsWith('http://') || rawUrl.startsWith('https://')
            ? rawUrl
            : (rawUrl.startsWith('/') ? '$baseUrl$rawUrl' : '$baseUrl/$rawUrl');
        final isImg = isImageUrl(rawUrl, fieldType: fieldType);
        final fileName = extractFileName(rawUrl);

        if (isImg) {
          return Padding(
            padding: const EdgeInsets.only(top: 4.0, bottom: 6.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                GestureDetector(
                  onTap: () => _showImageDialog(context, fullUrl, fileName),
                  child: Container(
                    constraints: const BoxConstraints(maxHeight: 180, maxWidth: double.infinity),
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.grey.shade300),
                      borderRadius: BorderRadius.circular(8),
                      color: Colors.grey.shade100,
                    ),
                    child: AuthenticatedImage(
                      url: fullUrl,
                      fit: BoxFit.cover,
                      borderRadius: BorderRadius.circular(8),
                      errorWidget: Container(
                        padding: const EdgeInsets.all(12),
                        color: Colors.grey.shade100,
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(Icons.broken_image, color: Colors.grey, size: 28),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                fileName,
                                style: const TextStyle(fontSize: 12, color: Colors.black87),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  fileName,
                  style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                  overflow: TextOverflow.ellipsis,
                  maxLines: 1,
                ),
              ],
            ),
          );
        } else {
          final downloadUrlWithToken = (token != null && token.isNotEmpty && !fullUrl.contains('token='))
              ? '$fullUrl${fullUrl.contains('?') ? '&' : '?'}token=$token'
              : fullUrl;

          return Padding(
            padding: const EdgeInsets.only(top: 4.0, bottom: 4.0),
            child: Material(
              color: Colors.grey.shade100,
              borderRadius: BorderRadius.circular(6),
              child: InkWell(
                borderRadius: BorderRadius.circular(6),
                onTap: () async {
                  final uri = Uri.parse(downloadUrlWithToken);
                  if (await canLaunchUrl(uri)) {
                    await launchUrl(uri, mode: LaunchMode.externalApplication);
                  }
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(6),
                    border: Border.all(color: Colors.grey.shade300),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.attach_file, size: 16, color: Colors.indigo),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          fileName,
                          style: const TextStyle(
                            fontSize: 12,
                            color: Colors.indigo,
                            fontWeight: FontWeight.w500,
                            decoration: TextDecoration.underline,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      const SizedBox(width: 4),
                      const Icon(Icons.download, size: 14, color: Colors.indigo),
                    ],
                  ),
                ),
              ),
            ),
          );
        }
      }).toList(),
    );
  }

  void _showImageDialog(BuildContext context, String imageUrl, String title) {
    ImageViewerDialog.show(
      context,
      imageUrl: imageUrl,
      title: title,
    );
  }
}

