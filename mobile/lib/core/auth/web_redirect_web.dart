// ignore: avoid_web_libraries_in_flutter
import 'dart:html' as html;

Future<void> redirectToUrl(String url) async {
  html.window.location.assign(url);
}

void saveWebVerifier(String verifier) {
  html.window.localStorage['oidc_code_verifier'] = verifier;
}

String? getWebVerifier() {
  return html.window.localStorage['oidc_code_verifier'];
}

void deleteWebVerifier() {
  html.window.localStorage.remove('oidc_code_verifier');
}

void clearUrlParams() {
  try {
    final cleanPath = html.window.location.pathname ?? '/';
    html.window.history.replaceState({}, '', cleanPath);
  } catch (_) {}
}
