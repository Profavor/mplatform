import 'package:url_launcher/url_launcher.dart';

Future<void> redirectToUrl(String url) async {
  final uri = Uri.parse(url);
  await launchUrl(uri, mode: LaunchMode.externalApplication);
}

void saveWebVerifier(String verifier) {}
String? getWebVerifier() => null;
void deleteWebVerifier() {}
void clearUrlParams() {}
