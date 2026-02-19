# 📸 Genius Shot (Genius Shot)

**Genius Shot**은 AI 기술을 활용하여 갤러리의 사진을 자동으로 분석하고, 스마트하게 정리 및 검색할 수 있도록 돕는 안드로이드 애플리케이션입니다. 사용자가 잠든 사이 AI가 사진을 라벨링하여, 원하는 사진을 키워드만으로 즉시 찾아낼 수 있습니다.

## ✨ 주요 기능 (Key Features)

### 🔍 AI 스마트 검색 (Smart Search)
* **키워드 기반 검색**: "고양이", "바다", "음식" 등 사진 속 객체를 키워드로 검색하여 즉시 찾아낼 수 있습니다.
* **다국어 자동 번역**: 한국어로 검색해도 내부적으로 영어 라벨과 매칭하여 정확한 결과를 제공합니다.
* **초고속 응답**: 분석된 데이터가 로컬 Room DB에 최적화되어 있어 수만 장의 사진도 순식간에 검색합니다.

### 🤖 자동 이미지 라벨링 (Auto Labeling)
* **ML Kit 통합**: Google의 온디바이스 머신러닝 기술을 사용하여 외부 서버 전송 없이 안전하게 사진을 분석합니다.
* **백그라운드 처리**: 앱을 사용하지 않는 시간에도 WorkManager를 통해 새로운 사진을 지속적으로 분석합니다.

### 🔋 스마트 스케줄링 (Smart Scheduling)
* **새벽 시간대 동작**: 시스템 자원을 많이 사용하는 분석 작업은 사용자가 잠든 새벽(03:00 ~ 05:00)에 실행됩니다.
* **최적의 실행 조건**: 배터리 소모를 방지하기 위해 '충전 중' 및 '기기 유휴 상태'일 때만 동작하도록 설계되었습니다.

### 📂 효율적인 갤러리 관리
* **날짜별 그룹화**: 사진을 촬영 날짜별로 깔끔하게 분류하여 보여줍니다.
* **다중 선택 및 삭제**: 여러 장의 사진을 한꺼번에 선택하여 공유하거나 안전하게 삭제할 수 있습니다.
* **중복 사진 스캔**: 유사하거나 중복된 사진을 찾아내어 저장 공간을 효율적으로 확보할 수 있도록 돕습니다.

---

## ✨ 최근 업데이트 사항 (v1.1)
* **Clean Architecture 도입**: Domain, Data, Presentation 계층 분리로 안정성 강화
* **검색 성능 최적화**: Room DB 인덱싱 및 필터링 로직 개선
* **백그라운드 워커 개선**: 특정 시간대 실행 및 제약 조건 추가

---

## 🛠 기술 스택
| 분류 | 기술 |
| :--- | :--- |
| **Language** | Kotlin (2.1.0) |
| **UI Framework** | Jetpack Compose |
| **Architecture** | Clean Architecture, MVVM Pattern |
| **Database** | Room (2.8.4) |
| **DI** | Hilt (2.59.1) |
| **AI/ML** | Google ML Kit (Image Labeling, Translation) |
| **Jetpack** | WorkManager, Paging3, ViewModel |

---

## 🚀 시작하기
1. **저장소 클론**
   ```bash
   git clone [https://github.com/jongmun-choi2/genius_shot.git](https://github.com/jongmun-choi2/genius_shot.git)
