void main() {
  try {
    final uri = Uri.parse('http://localhost:8080/api/files/download/xyz.xlsx?name=old_name');
    final fileName = 'new_name.xlsx';
    final token = 'my_token';
    final newUri = uri.replace(queryParameters: {
      ...uri.queryParameters,
      'token': token,
      if (fileName != null) 'name': fileName,
    });
    print(newUri.toString());
  } catch (e, st) {
    print('Error: $e\n$st');
  }
}
