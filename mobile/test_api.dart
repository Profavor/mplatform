import 'dart:convert';
import 'package:http/http.dart' as http;

void main() async {
  final loginUrl = Uri.parse('http://localhost:8080/api/auth/login');
  
  print('Logging in...');
  final loginRes = await http.post(
    loginUrl,
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({'username': 'admin', 'password': 'password'}) // use admin/admin if needed, I'll try both
  );
  
  if (loginRes.statusCode != 200) {
    print('Login failed: ${loginRes.statusCode} ${loginRes.body}');
    // let's try admin/admin
    final loginRes2 = await http.post(
      loginUrl,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'username': 'admin', 'password': 'admin'})
    );
    if (loginRes2.statusCode != 200) {
      print('Login with admin/admin also failed: ${loginRes2.statusCode} ${loginRes2.body}');
      return;
    }
  }

  // Assuming it succeeded, parse token
  final token = jsonDecode(loginRes.statusCode == 200 ? loginRes.body : '{"token": "dummy"}')['token'] ?? jsonDecode(loginRes.statusCode == 200 ? loginRes.body : '{"accessToken": "dummy"}')['accessToken'];
  
  print('Got token, fetching my-requests...');
  
  final reqUrl = Uri.parse('http://localhost:8080/api/approval-requests/my-requests');
  final reqRes = await http.get(reqUrl, headers: {'Authorization': 'Bearer $token'});
  
  print('Response status: ${reqRes.statusCode}');
  print('Response body: ${reqRes.body}');
}
