import 'dart:convert';
import 'package:flutter/material.dart';

class L10nHelper {
  /// Parses a dynamic value that might be a localized map (e.g. {"ko": "인치국", "en": "Lin Chigoog"}).
  /// Extracts the string matching the current locale, falling back to 'ko', 'en', or the first available value.
  static String parseLocalizedMap(dynamic value, BuildContext context) {
    if (value == null) return '';
    
    dynamic mapValue = value;
    if (value is String) {
      if (value.startsWith('{') && value.endsWith('}')) {
        try {
          mapValue = jsonDecode(value);
        } catch (_) {
          return value;
        }
      } else {
        return value;
      }
    }
    
    if (mapValue is Map) {
      if (mapValue.isEmpty) return '';
      
      try {
        final localeCode = Localizations.localeOf(context).languageCode;
        if (mapValue.containsKey(localeCode) && mapValue[localeCode] != null && mapValue[localeCode].toString().isNotEmpty) {
          return mapValue[localeCode].toString();
        }
      } catch (_) {
        // Ignore if Localizations not found
      }
      
      if (mapValue.containsKey('ko') && mapValue['ko'] != null && mapValue['ko'].toString().isNotEmpty) return mapValue['ko'].toString();
      if (mapValue.containsKey('en') && mapValue['en'] != null && mapValue['en'].toString().isNotEmpty) return mapValue['en'].toString();
      
      return mapValue.values.first.toString();
    }
    
    return mapValue.toString();
  }
}
