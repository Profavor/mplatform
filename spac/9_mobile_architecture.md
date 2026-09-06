# 9. 크로스플랫폼 모바일 앱 아키텍처 (Mobile App Specifications)

본 문서는 `mobile/` 디렉토리에 구축된 **Flutter 기반 크로스플랫폼(Android, iOS, Web) 모바일 애플리케이션**의 아키텍처 및 상세 명세서이다.

---

## 9.1 모바일 기술 스택 (Tech Stack)

| 영역 | 기술 / 라이브러리 | 용도 및 세부 설명 |
|---|---|---|
| **Framework** | **Flutter 3.x (Dart 3.x)** | Android, iOS, Web 크로스플랫폼 단일 코드베이스 |
| **State Management** | **Flutter Riverpod (^2.5.1)** | 컴파일 타임 안전성 및 의존성 주입(DI) 상태 관리 |
| **Routing** | **GoRouter (^14.0.0)** | 선언적 URL 라우팅 및 딥링크, 네비게이션 가드 지원 |
| **Network Client** | **Dio (^5.4.0)** | JWT 토큰 자동 주입, Refresh 토큰 인터셉터, 타임존 헤더 |
| **WebSocket** | **STOMP Client (`stomp_dart_client`)** | 실시간 인앱 채팅 및 푸시 알림 수신 |
| **Local Storage** | **Flutter Secure Storage & Shared Preferences**| JWT 토큰 암호화 저장 및 사용자 설정 캐싱 |
| **UI Components** | **Material 3 Design** | 반응형 모바일 디자인, 다크 모드, 접근성 지원 |

---

## 9.2 Feature-First 디렉토리 구조

```text
mobile/lib/
├── main.dart                       # 앱 진입점 및 ProviderScope 설정
├── core/                           # 공통 코어 모듈
│   ├── config/                     # 환경 변수 및 앱 설정
│   ├── constants/                  # API 엔드포인트, 공통 상수
│   ├── network/                    # Dio 클라이언트, 인증/타임존 인터셉터
│   ├── router/                     # GoRouter 라우팅 테이블
│   ├── theme/                      # 테마 및 컬러 팔레트
│   └── utils/                      # 날짜 변환, 마스킹 포맷터 헬퍼
└── features/                       # 기능별 모듈 (Feature-First)
    ├── auth/                       # 로그인, 토큰 관리, 권한 가드
    ├── dashboard/                  # 모바일 KPI 대시보드 및 결재 대기 현황
    ├── approvals/                  # 결재 대기/기안 목록, 상세 조회, 승인/반려/위임
    ├── records/                    # 도메인/노드별 레코드 탐색, 검색, 상세 뷰
    ├── dq/                         # 도메인 품질 점수 및 위반 목록 모니터링
    ├── search/                     # OpenSearch 연동 전역 통합 검색
    ├── chat/                       # STOMP 기반 실시간 1:1 및 그룹 채팅
    ├── notifications/              # 실시간 시스템 푸시 알림 센터
    ├── files/                      # MinIO 연동 첨부파일 뷰어 및 다운로드
    ├── navigation/                 # 하단 바텀 네비게이션 및 사이드 드로어
    └── home/                       # 메인 홈 화면
```

---

## 9.3 주요 기능 명세 (Core Features)

### 1. 결재 승인/반려 & 위임 (`features/approvals`)
- **결재함 모바일 뷰**: 내 결재 대기 목록(`todos`), 내 기안 목록(`my-requests`)을 카드 뷰 형태로 제공.
- **결재 상세 & 차이점(Diff)**: 변경 전/후 데이터를 모바일 화면에 최적화된 Diff 형태로 표출.
- **원클릭 승인/반려**: 결재 코멘트 입력 및 단계 승인/반려 즉시 처리.
- **부재 시 결재 위임**: 모바일에서 대결자 및 위임 기간 등록.

### 2. 레코드 탐색 & 다축 필터링 (`features/records`)
- **분류 트리 네비게이션**: 도메인 → 분류축 → 노드 계층을 모바일 아코디언 트리로 탐색.
- **레코드 상세 및 마스킹 해제**: 민감 정보 마스킹 표시 및 사유 입력 기반 마스킹 해제 지원.
- **다축 서브 노드 조회**: 주 분류 외 서브 분류축 소속 정보 확인.

### 3. 실시간 STOMP 채팅 & 푸시 알림 (`features/chat`, `features/notifications`)
- **인앱 메신저**: 웹소켓을 통한 실시간 텍스트 송수신 및 다국어 번역 보기.
- **실시간 알림**: 결재 요청 인입, 결재 완료, DQ 경보 발생 시 인앱 푸시 및 알림 센터 집계.

### 4. DQ 모바일 모니터링 (`features/dq`)
- 도메인별 품질 점수(DQ Score), 시계열 트렌드 미니 차트, 주요 위반 통계 카드 표출.

### 5. 보안 및 타임존 인터셉터 (`core/network`)
- **보안 토큰 관리**: `FlutterSecureStorage`에 Access/Refresh 토큰을 암호화 보관하고 만료 시 자동 재발급(Silent Refresh).
- **타임존 자동 보정**: 디바이스 로컬 타임존 오프셋을 HTTP 헤더로 전송하여 일시 데이터 왜곡 방지.

### 6. 2FA / OTP 모바일 다중인증 (`features/auth`)
- **다중 인증 흐름**: 로그인 성공 시 2FA 활성화 사용자에게 6자리 TOTP/이메일 OTP 또는 일회용 긴급 백업코드 입력 화면을 연속 노출하여 2단계 검증 수행.

### 7. 부동산 임대차 (`LEASE_CONTRACT`) 리스크 모바일 위젯 (`features/dashboard`)
- **만기 & 연체 관제**: 만기 30일/7일 이내 도래 계약 및 월세 연체 상태를 모바일 대시보드 상단 배너/카드로 표출하여 자산 회수 즉각 대응 지원.

### 8. 모바일 데이터 계보 요약 뷰 (`features/records`)
- **5단계 파이프라인 모바일 카드**: Ingestion부터 Golden Record까지의 변환 단계를 모바일 세로형 스텝 카드로 간결하게 시각화.

---

## 9.4 모바일 앱 빌드 및 구동

```bash
cd mobile

# 1. 의존성 패키지 설치
flutter pub get

# 2. 로컬 디바이스 또는 에뮬레이터 실행
flutter run

# 3. 플랫폼별 릴리즈 빌드
flutter build apk --release       # Android APK
flutter build appbundle --release # Android App Bundle
flutter build ipa --release       # iOS IPA
flutter build web --release --base-href /mobile/ # Web 프로덕션 빌드
```

---

## 9.5 Kubernetes Nginx 웹 배포 및 공인 도메인 서빙
- Flutter Web 빌드 산출물은 경량 Nginx 컨테이너로 패키징되어 K8s 클러스터(`k8s/32-mobile.yaml`)에 배포된다.
- Ingress 라우팅에 의해 공인 도메인 및 로컬에서 서비스된다:
  - **공인 도메인**: [`https://mdm.mplat.store/mobile/`](https://mdm.mplat.store/mobile/)
  - **로컬 서비스**: `http://localhost:8082`

