// ignore_for_file: avoid_web_libraries_in_flutter

import 'dart:html' as html;
import 'dart:ui_web' as ui_web;
import 'package:flutter/material.dart';

Widget buildWebVideoPlayer(String url, double width, double height) {
  final viewId = 'video-player-${url.hashCode}';
  
  // ignore: undefined_prefixed_name
  ui_web.platformViewRegistry.registerViewFactory(viewId, (int viewId) {
    final videoElement = html.VideoElement()
      ..src = url
      ..controls = true
      ..crossOrigin = 'anonymous'
      ..style.width = '100%'
      ..style.height = '100%'
      ..style.border = 'none'
      ..style.backgroundColor = 'black';
    return videoElement;
  });

  return SizedBox(
    width: width,
    height: height,
    child: HtmlElementView(viewType: viewId),
  );
}
