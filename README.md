# 📸 Genius Shot (지니어스 샷)

**Genius Shot**은 AI 기술을 활용하여 사용자의 사진 촬영을 돕고, 갤러리에 쌓인 중복 사진을 스마트하게 정리해 주는 지능형 카메라 & 갤러리 애플리케이션입니다.

## ✨ 주요 기능 (Key Features)

### 1. 📷 스마트 카메라 (Smart Camera)
- **CameraX 기반 고성능 촬영:** 빠르고 안정적인 촬영 경험 제공
- **전면/후면 전환:** 원터치로 셀피 모드와 후면 카메라 전환 가능
- **줌 & 포커스:**
    - 핀치 투 줌 (Pinch-to-Zoom) 지원
    - 탭 투 포커스 (Tap-to-Focus) 및 포커스 링 UI
- **흔들림 감지:** 촬영 직후 사진의 선명도(Laplacian Variance)를 분석하여, 흔들린 사진일 경우 재촬영 팝업 노출

### 2. 🤖 AI 분석 및 정리 (Smart Analysis)
- **중복 사진 그룹화:**
    - **Pose Detection (ML Kit):** 사람의 포즈 유사도를 분석하여 같은 자세의 사진을 식별
    - **Local Difference Analysis:** 배경 및 미세한 움직임을 격자(Grid) 단위로 분석하여 정교한 중복 판별
- **베스트 샷 추천:** 흔들림이 가장 적고 구도가 안정적인 사진을 자동으로 '남김'으로 추천

### 3. 🖼️ 갤러리 및 관리 (Gallery Management)
- **날짜별 그룹화:** 촬영 날짜별로 사진을 깔끔하게 분류하여 표시 (Paging 3 적용)
- **다중 선택 모드:** 터치 & 드래그 또는 롱 클릭으로 여러 사진을 손쉽게 선택
- **삭제 및 공유:**
    - Android 10+ 범위 지정 저장소(Scoped Storage) 대응 삭제 로직 (권한 요청 처리)
    - 다중 이미지 공유 기능 (Intent)

## 🛠 기술 스택 (Tech Stack)

### Architecture
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture
- **DI:** Hilt (Dagger)
- **Async:** Coroutines & Flow

### Libraries
- **Camera:** CameraX (Core, Camera2, Lifecycle, View)
- **Image Loading:** Glide (Compose Integration)
- **List:** Paging 3 (Infinite Scroll)
- **AI/ML:** Google ML Kit (Pose Detection), TensorFlow Lite
- **Ads:** Google AdMob
