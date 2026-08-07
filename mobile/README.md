# MPlatform Mobile & Web App (Flutter)

MPlatform의 크로스 플랫폼(Android, iOS, Web) 프론트엔드 애플리케이션입니다. Flutter 프레임워크를 기반으로 구축되었으며, 실시간 채팅, 다국어 지원, 파일/미디어 스트리밍 및 JWT 기반의 안전한 인증 기능을 제공합니다.

## 🚀 주요 기능 (Key Features)

* **실시간 채팅 (Real-time Chat):** 
  * WebSocket(`stomp_dart_client`)을 활용한 양방향 실시간 메시징.
  * 채팅방 목록 조회, 안 읽은 메시지 카운트, 읽음 처리 기능.
  * 이미지 및 동영상 파일 업로드 및 실시간 스트리밍 재생.
* **미디어 최적화 플레이어 (Cross-platform Media):** 
  * 모바일(Android/iOS) 환경에서는 네이티브 플레이어(ExoPlayer/AVPlayer)를 사용.
  * 웹(Web) 환경에서는 엄격한 브라우저 CORS 및 Range Request 제약을 우회하기 위해 `HtmlElementView`를 활용한 커스텀 네이티브 HTML `<video>` 플레이어로 자동 분기 처리.
* **안전한 인증 (Authentication):**
  * JWT (Access Token / Refresh Token) 기반 인증.
  * Dio Interceptor를 통한 토큰 자동 갱신(Refresh).
  * 동시 로그인 방지 (웹소켓 `FORCE_LOGOUT` 이벤트 감지 시 즉각적인 세션 만료 및 강제 로그아웃 처리).
* **국제화 및 타임존 (i18n & Timezone):**
  * 사용자의 로컬 타임존 정보를 식별하여 백엔드와 통신 시 일관된 시간(GMT 시차) 적용.
  * 하드코딩을 배제하고 `AppLocalizations`를 통한 완벽한 다국어(i18n) 지원.

## 🛠️ 기술 스택 (Tech Stack)

* **Framework:** Flutter (Cross-platform)
* **State Management:** Riverpod (`flutter_riverpod`)
* **Routing:** GoRouter (`go_router`)
* **Networking:** Dio (`dio`)
* **WebSocket:** Stomp Client (`stomp_dart_client`)
* **Local Storage:** Flutter Secure Storage, Shared Preferences
* **Media:** Video Player (`video_player`), File Picker

## 📁 프로젝트 구조 (Project Structure)

프로젝트는 Feature-first(기능 중심) 아키텍처를 따르고 있습니다.

```text
lib/
├── core/                   # 앱 전반에 사용되는 공통 로직 및 유틸리티
│   ├── config/             # 환경 설정 (API Base URL 등)
│   ├── l10n/               # 다국어(i18n) 번역 파일 및 설정
│   ├── network/            # Dio 클라이언트 및 Interceptor (Auth, Timezone)
│   ├── providers/          # 공통 전역 상태 (Riverpod)
│   ├── services/           # 로컬 스토리지 등 코어 서비스
│   └── utils/              # 헬퍼 함수 및 커스텀 위젯 (Web/Mobile 비디오 플레이어 분기 등)
├── features/               # 도메인/기능별 분리된 모듈
│   ├── auth/               # 로그인, 세션 관리 등 인증 도메인
│   ├── chat/               # 채팅방 목록, 채팅 화면, 웹소켓 서비스 도메인
│   └── ...                 # 추가 기능 도메인
└── main.dart               # 앱 진입점 및 전역 설정
```

## ⚠️ 개발 규칙 및 원칙 (Development Rules)

MPlatform 개발 시 반드시 준수해야 하는 규칙입니다.

1. **TDD (Test-Driven Development) 준수:**
   모든 기능 개발 및 수정 시 프론트엔드 컴포넌트/단위 테스트 스펙을 먼저 작성하여 사이드 이펙트를 방지합니다.
2. **하드코딩 절대 금지 (No Hardcoding):**
   사용자 화면에 노출되는 모든 텍스트, 라벨, 필드명 등은 소스코드 내 하드코딩을 금지합니다. 반드시 다국어(i18n) 설정 파일을 통해 조회 및 조립해야 합니다.
3. **Raw UUID 노출 금지:**
   사용자 화면에 무의미한 Raw UUID(예: `340a0917-...`)를 그대로 노출하지 마세요. 반드시 포매팅 함수(예: `REC-...`)를 거치거나 사용자 친화적인 명칭으로 치환해야 합니다.
4. **크로스 플랫폼 호환성 유지:**
   특정 플랫폼(Web, Android, iOS)에서만 동작하는 라이브러리(예: `dart:html`)를 전역으로 Import하지 마세요. 불가피할 경우 반드시 `if (dart.library.html)` 형태의 조건부 임포트와 Stub 파일을 활용하여 타 플랫폼 빌드 에러를 방지해야 합니다.

## 🏃 시작하기 (Getting Started)

### 사전 요구 사항 (Prerequisites)
* Flutter SDK (버전 3.0 이상 권장)
* Android Studio / Xcode (모바일 빌드 시)
* Chrome (웹 빌드 및 디버깅 시)

### 실행 방법 (Run)

1. **패키지 설치:**
   ```bash
   flutter pub get
   ```

2. **다국어 및 코드 제너레이터 실행 (필요 시):**
   ```bash
   flutter gen-l10n
   ```

3. **앱 실행 (플랫폼 선택):**
   * **Chrome 웹 환경에서 실행:**
     ```bash
     flutter run -d chrome
     ```
   * **안드로이드 에뮬레이터 또는 디바이스에서 실행:**
     ```bash
     flutter run -d android
     ```
   * **iOS 시뮬레이터 또는 디바이스에서 실행:**
     ```bash
     flutter run -d ios
     ```
