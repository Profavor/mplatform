import 'package:http/http.dart' as http;
import 'dart:convert';
void main() async {
  final loginUrl = Uri.parse('http://localhost:8080/api/auth/login');
  final loginRes = await http.post(
    loginUrl,
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({'username': 'admin', 'password': 'password'})
  );
  print('LOGIN RESP: ${loginRes.body}');
}
