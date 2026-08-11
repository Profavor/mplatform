import 'dart:convert';
import 'package:mplatform_mobile/features/auth/domain/models/auth_response.dart';

void main() {
  final jsonStr = '''{
    "token": "abc",
    "refreshToken": "def",
    "username": "admin",
    "role": "ROLE_ADMIN",
    "uuid": "98e891b4-760a-4601-a95d-914000ac0233",
    "id": "98e891b4-760a-4601-a95d-914000ac0233",
    "organizationId": "27d01b56-1930-4218-b477-38c5a229d68d",
    "timezone": "Asia/Seoul",
    "permissions": ["ROLE_ADMIN"],
    "mustChangePassword": false
  }''';
  
  final json = jsonDecode(jsonStr);
  try {
    final resp = AuthResponse.fromJson(json);
    print('SUCCESS: \');
  } catch (e, st) {
    print('ERROR: \');
    print(st);
  }
}

