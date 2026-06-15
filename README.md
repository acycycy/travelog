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

### 07. 정렬 기능
- 최신순 정렬
- 오래된순 정렬

### 08. 컨텍스트 메뉴
- 수정
- 삭제

### 09. 진행 예정
- 부가 기능들 추가
- 통계 화면 기능 보강
- UI 디자인 개선
- 입력값 예외 처리 강화
- 앱 아이콘 및 화면 디자인 정리
- 최종 테스트 및 버그 수정

### 09. 상세화면 추가 및 UI 개선
- 여행 항목 클릭 시 상세화면(TravelDetailActivity)으로 이동
- 상세화면에서 여행지명, 날짜, 메모, 사진 표시
- 상세화면에서 수정하기 버튼으로 수정 화면 이동
- 컨텍스트 메뉴 수정/삭제 콜백 분리 (클릭 → 상세, 팝업 수정 → 수정화면)
- Fragment 백스택 관리 추가 (뒤로가기 정상 동작)
- Edge-to-Edge 비활성화로 ActionBar 가림 현상 수정

### 10. 별점 및 다중 사진 첨부 기능 구현
- 여행 기록에 별점(1~5점) 추가
- 사진 여러 장 첨부 기능 구현
- 사진별 코멘트 입력 기능 구현
- 코루틴 비동기 이미지 로딩 구현
- 이번 달 여행 수 통계 추가
- 빈 목록 empty state 화면 추가

### 11. 여행 계획 기능 구현
- 여행 계획 CRUD 구현 (추가 / 수정 / 삭제 / 상세)
- D-DAY 카운터 자동 계산 표시
- 날짜 범위 선택 기능 추가 (시작일 ~ 종료일)
- 계획 완료 시 여행 기록으로 자동 전환 기능 구현
- BottomNavigationView 계획 탭 추가

### 12. 구글 지도 및 위치 선택 기능 구현
- Google Maps SDK 연동
- 여행 기록 상세화면 지도 표시 (저장 위치 → 여행지명 Geocoder 순)
- 지도에서 직접 핀 찍어 위치 선택 기능 구현 (MapPickerActivity)
- 위치명 검색으로 핀 설정 기능 추가
- 여행 계획 상세화면 지도 표시 추가

### 13. 다중 핀 기능 구현
- 지도에서 핀 여러 개 추가 가능 (탭으로 추가, 핀 탭으로 삭제)
- 상세화면에서 여러 핀 모두 표시 및 자동 카메라 맞춤

