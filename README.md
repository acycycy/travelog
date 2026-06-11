# Travelog
Android 기반 여행 기록 앱 프로젝트

## 사용자가 여행 기록을 저장하고 조회

## 개발 환경
- Kotlin
- Android Studio
- SQLite

## 구현 목표
- Fragment 기반 화면 구성
- RecyclerView 여행 목록
- SQLite CRUD
- 사진 선택 기능
- BottomNavigationView

## 진행 상황

### 01. 앱 기본 구조
- 프로젝트 생성
- GitHub 연동
- BottomNavigationView 구현
- HomeFragment 생성
- StatsFragment 생성
- Fragment 화면 전환 구현

### 02. SQLite 저장 및 조회
- Travel 데이터 클래스 생성
- RecyclerView 구현
- TravelAdapter 구현
- TravelDatabaseHelper 구현
- 여행 등록 기능 구현
- 여행 목록 조회 기능 구현

### 03. 여행 삭제 기능 구현
- RecyclerView 항목 길게 누르기 구현
- AlertDialog 삭제 확인창 구현
- SQLite 데이터 삭제 기능 구현
- 삭제 후 목록 자동 갱신

### 04. 여행 수정 기능 구현
- RecyclerView 클릭 이벤트 구현
- 수정 모드 추가
- 기존 데이터 자동 입력
- SQLite Update 구현
- 수정 후 목록 자동 갱신(onResume)

### 05. 여행 사진 첨부 기능 구현
- 갤러리 사진 선택 기능 구현
- ImageView 미리보기 구현
- 이미지 Uri SQLite 저장
- 여행 목록에 썸네일 표시

### 06. 여행 검색 기능 구현
- 검색창 추가
- 제목 및 메모 검색
- 실시간 필터링 구현

### 07. 진행 예정
- 부가 기능들 추가
- 통계 화면 기능 보강
- UI 디자인 개선
- 입력값 예외 처리 강화
- 앱 아이콘 및 화면 디자인 정리
- 최종 테스트 및 버그 수정