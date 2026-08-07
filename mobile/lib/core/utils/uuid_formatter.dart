class UuidFormatter {
  /// 무의미한 raw UUID(예: 340a0917-af0b-4d13-a1ce-479d4b2e2ca7)를 화면에 그대로 노출하지 않고
  /// 접두사를 붙인 축약된 식별 코드(예: REC-340a0917)로 치환하여 표출하는 방어 헬퍼 함수
  static String format(String? uuid, {String prefix = 'ID'}) {
    if (uuid == null || uuid.trim().isEmpty) {
      return '';
    }
    final str = uuid.trim();
    // UUID 형식 (8-4-4-4-12) 인지 확인하여 앞 8자리 추출
    final parts = str.split('-');
    if (parts.length >= 5 && parts[0].length == 8) {
      return '$prefix-${parts[0]}';
    }
    // 이미 치환된 식별 코드이거나 일반 문자열인 경우 원본 복호화 없이 앞 8자리의 포맷으로 보호 처리
    if (str.length > 12 && !str.contains('-') && RegExp(r'^[a-fA-F0-9]+$').hasMatch(str)) {
      return '$prefix-${str.substring(0, 8)}';
    }
    return str;
  }
}
