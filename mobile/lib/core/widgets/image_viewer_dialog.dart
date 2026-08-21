import 'package:flutter/material.dart';
import 'package:mplatform_mobile/core/widgets/authenticated_image.dart';

/// 이미지를 전체 화면으로 확대하여 보고, 핀치 제스처, 더블 탭, 줌인/아웃 버튼으로 자유롭게 확대/축소할 수 있는 뷰어 다이얼로그.
class ImageViewerDialog extends StatefulWidget {
  final String imageUrl;
  final String? title;

  const ImageViewerDialog({
    super.key,
    required this.imageUrl,
    this.title,
  });

  /// 큰 화면 줌인/아웃 다이얼로그를 표시합니다.
  static Future<void> show(
    BuildContext context, {
    required String imageUrl,
    String? title,
  }) {
    return showDialog<void>(
      context: context,
      barrierColor: Colors.black.withOpacity(0.88),
      useSafeArea: false,
      builder: (ctx) => ImageViewerDialog(
        imageUrl: imageUrl,
        title: title,
      ),
    );
  }

  @override
  State<ImageViewerDialog> createState() => _ImageViewerDialogState();
}

class _ImageViewerDialogState extends State<ImageViewerDialog> with SingleTickerProviderStateMixin {
  late TransformationController _transformationController;
  late AnimationController _animationController;
  Animation<Matrix4>? _animation;
  TapDownDetails? _doubleTapDetails;
  double _currentScale = 1.0;

  @override
  void initState() {
    super.initState();
    _transformationController = TransformationController();
    _transformationController.addListener(_onTransformationChanged);
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 200),
    )..addListener(() {
        if (_animation != null) {
          _transformationController.value = _animation!.value;
        }
      });
  }

  void _onTransformationChanged() {
    final scale = _transformationController.value.getMaxScaleOnAxis();
    if ((scale - _currentScale).abs() > 0.01 && mounted) {
      setState(() {
        _currentScale = scale;
      });
    }
  }

  @override
  void dispose() {
    _transformationController.removeListener(_onTransformationChanged);
    _transformationController.dispose();
    _animationController.dispose();
    super.dispose();
  }

  void _handleDoubleTapDown(TapDownDetails details) {
    _doubleTapDetails = details;
  }

  void _handleDoubleTap() {
    final position = _doubleTapDetails?.localPosition ?? Offset.zero;
    final Matrix4 endMatrix;

    if (_currentScale > 1.2) {
      // 1.0 배율로 복귀
      endMatrix = Matrix4.identity();
    } else {
      // 탭한 지점 기준 2.5배 줌인
      endMatrix = Matrix4.identity()
        ..translate(-position.dx * 1.5, -position.dy * 1.5)
        ..scale(2.5);
    }

    _animateToMatrix(endMatrix);
  }

  void _zoomIn() {
    final newScale = (_currentScale * 1.3).clamp(0.5, 6.0);
    _animateToScale(newScale);
  }

  void _zoomOut() {
    final newScale = (_currentScale / 1.3).clamp(0.5, 6.0);
    _animateToScale(newScale);
  }

  void _resetZoom() {
    _animateToMatrix(Matrix4.identity());
  }

  void _animateToScale(double targetScale) {
    final currentMatrix = _transformationController.value;
    final currentScale = currentMatrix.getMaxScaleOnAxis();
    final factor = targetScale / currentScale;

    final endMatrix = currentMatrix.clone()..scale(factor, factor);
    _animateToMatrix(endMatrix);
  }

  void _animateToMatrix(Matrix4 targetMatrix) {
    _animation = Matrix4Tween(
      begin: _transformationController.value,
      end: targetMatrix,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    ));
    _animationController.forward(from: 0);
  }

  @override
  Widget build(BuildContext context) {
    final titleText = widget.title ?? '';

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          // 배경 탭 시 줌 상태가 아닐 때 닫기
          Positioned.fill(
            child: GestureDetector(
              onTap: () {
                if (_currentScale <= 1.05) {
                  Navigator.of(context).pop();
                }
              },
              child: Container(color: Colors.transparent),
            ),
          ),

          // 핀치 줌 / 팬 이동 / 더블 탭 줌인 뷰어
          Positioned.fill(
            child: GestureDetector(
              onDoubleTapDown: _handleDoubleTapDown,
              onDoubleTap: _handleDoubleTap,
              child: Center(
                child: InteractiveViewer(
                  transformationController: _transformationController,
                  minScale: 0.5,
                  maxScale: 6.0,
                  boundaryMargin: const EdgeInsets.all(500),
                  clipBehavior: Clip.none,
                  child: AuthenticatedImage(
                    url: widget.imageUrl,
                    fit: BoxFit.contain,
                    errorWidget: const Padding(
                      padding: EdgeInsets.all(32.0),
                      child: Icon(Icons.broken_image, color: Colors.white70, size: 64),
                    ),
                  ),
                ),
              ),
            ),
          ),

          // 상단 툴바 (닫기 버튼, 제목, 줌 아웃/인/리셋 컨트롤)
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: SafeArea(
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.black.withOpacity(0.75),
                      Colors.transparent,
                    ],
                  ),
                ),
                child: Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.close, color: Colors.white),
                      tooltip: '닫기',
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                    const SizedBox(width: 4),
                    Expanded(
                      child: Text(
                        titleText,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                        ),
                        overflow: TextOverflow.ellipsis,
                        maxLines: 1,
                      ),
                    ),
                    // 줌 아웃 버튼 (-)
                    IconButton(
                      icon: const Icon(Icons.zoom_out, color: Colors.white),
                      tooltip: '축소',
                      onPressed: _zoomOut,
                    ),
                    // 현재 배율 & 리셋 버튼 (100%)
                    TextButton(
                      onPressed: _resetZoom,
                      child: Text(
                        '${(_currentScale * 100).toInt()}%',
                        style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                      ),
                    ),
                    // 줌 인 버튼 (+)
                    IconButton(
                      icon: const Icon(Icons.zoom_in, color: Colors.white),
                      tooltip: '확대',
                      onPressed: _zoomIn,
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
