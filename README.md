# Android Native Application for Vo-Donate

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.0-blue?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Material%20Design%203-grey?style=flat&logo=materialdesign)](https://m3.material.io/)

<br>

##  목차

- [스크린샷](#스크린샷)
- [주요 기능](#주요-기능)
- [기술 스택 및 아키텍처](#기술-스택-및-아키텍처)
    - [아키텍처](#아키텍처)
    - [사용된 주요 라이브러리](#사용된-주요-라이브러리)
- [UI 구성](#ui-구성)
    - [디자인 시스템](#디자인-시스템)
    - [주요 화면 설명](#주요-화면-설명)
- [프로젝트 구조](#프로젝트-구조)
- [개발 환경](#개발-환경)
- [향후 개선 사항](#향후-개선-사항)

<br>

## 스크린샷

| Auth | Vote and Donation 
| :------: | :------: |
| ![image](https://github.com/user-attachments/assets/58c3f548-bd40-41ba-83fa-bd3a747a21ba) | ![image](https://github.com/user-attachments/assets/4fb74f58-7404-4bff-abc8-0787dcedafaa) |
| Make a Proposal | User Information |
| :------: | :------: |
| ![image](https://github.com/user-attachments/assets/33e1e270-361e-4211-affe-bdec649d2a96) | ![image](https://github.com/user-attachments/assets/13fbf18b-15e8-4d4d-b275-92fdee9966bc) |

<br>

## 주요 기능

- **사용자 인증**: ID/PW 기반 로그인 및 회원가입 기능.
- **제안 목록 조회**:
    - 투표 진행 중인 제안 목록
    - 기부 진행 중인 제안 목록
    - 완료된 제안 목록 (성공/실패)
- **제안 상세 정보 확인**: 제안 내용, 목표 금액, 현재 투표/기부 현황 등.
- **투표 기능**: 사용자는 활성화된 제안에 대해 찬성/반대 투표 가능.
- **기부 기능**: 사용자는 원하는 제안에 특정 금액을 기부 가능.
- **사용자 정보 관리**:
    - 사용자 기본 정보 (ID, 이름, 지갑 주소 등) 조회.
    - 사용자의 잔액 확인.
    - 사용자가 생성한 제안 목록 확인 (진행중/완료됨).
- **제안 생성**: 사용자가 새로운 제안을 등록하는 기능. (해당 기능이 있다면 명시)
- **실시간 업데이트**: (선택 사항) 제안 목록 및 상태 자동 새로고침 또는 수동 새로고침 기능.

<br>

## 기술 스택 및 아키텍처

본 프로젝트는 최신 Android 개발 트렌드를 따르며, 다음과 같은 기술 스택과 아키텍처를 사용합니다.

### 아키텍처

- **Modern Android Development (MAD)**: Kotlin, Jetpack Compose, Coroutines, Flow, Hilt 등 Google에서 권장하는 최신 기술 스택을 적극 활용합니다.
- **MVVM (Model-View-ViewModel)**: UI 로직과 비즈니스 로직을 분리하여 테스트 용이성과 유지보수성을 높입니다.
    - **View (Composable UI)**: Jetpack Compose를 사용하여 선언적 UI를 구성합니다. `Scaffold`, `LazyColumn`, `HorizontalPager`, `Card` 등 다양한 Composable 함수를 활용합니다.
    - **ViewModel**: UI 상태(State)를 관리하고, UseCase 또는 Repository를 통해 비즈니스 로직을 처리합니다. `StateFlow`를 사용하여 UI에 반응형으로 데이터를 전달합니다.
    - **Model (Repository/UseCase/DataSource)**:
        - **Repository**: 데이터 소스(네트워크, 로컬 DB 등)를 추상화하고, ViewModel에 필요한 데이터를 제공합니다.
        - **UseCase**: 특정 비즈니스 로직을 캡슐화하여 재사용성을 높이고 ViewModel의 복잡도를 낮춥니다. (선택적 사용)
        - **DataSource**: 네트워크 API 호출(Retrofit), 로컬 데이터베이스 접근 등 실제 데이터 I/O를 담당합니다.
- **Dependency Injection**: Hilt를 사용하여 의존성 주입을 관리하여 코드의 결합도를 낮추고 테스트 용이성을 확보합니다.
- **Modularization**: 기능별 또는 계층별 모듈화를 통해 빌드 속도를 개선하고 코드의 재사용성 및 유지보수성을 높입니다. (예: `app`, `core:network`, `core:ui`, `data`, `feature:vote` 등)

### 사용된 주요 라이브러리

- **[Kotlin](https://kotlinlang.org/)**: 기본 개발 언어.
    - **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)**: 비동기 처리를 위한 코루틴 사용.
    - **[Flow](https://kotlinlang.org/docs/flow.html)**: 반응형 프로그래밍을 위한 데이터 스트림 처리.
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)**: UI 개발을 위한 선언적 UI 툴킷.
    - **[Material Design 3](https://m3.material.io/)**: Compose Material3 라이브러리를 사용하여 일관된 UI/UX 제공.
    - **[Compose Navigation](https://developer.android.com/jetpack/compose/navigation)**: 화면 간 이동 관리.
    - **[ViewModel Compose](https://developer.android.com/jetpack/compose/state#viewmodel-state)**: ViewModel과 Compose UI 간의 통합.
- **[Retrofit2](https://square.github.io/retrofit/) & [OkHttp3](https://square.github.io/okhttp/)**: 네트워크 통신 라이브러리.
- **[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)**: JSON 직렬화/역직렬화. (또는 Gson/Moshi)
- **[Timber](https://github.com/JakeWharton/timber)**: 로깅 라이브러리.

<br>

## UI 구성

### 디자인 시스템

- **Material Design 3**: 앱 전반에 걸쳐 Material 3 컴포넌트와 가이드라인을 준수하여 사용자에게 익숙하고 일관된 경험을 제공합니다.
- **Theming**: `Color.kt`, `Theme.kt`, `Type.kt` 파일을 통해 앱의 전체적인 색상, 타이포그래피, 모양(Shape)을 정의하고 일관되게 적용합니다.
- **Composable 재사용**: 공통적으로 사용되는 UI 요소(예: `InfoRow`, `ProposalItemCard`, `RefreshFloatingActionButton`)는 별도의 Composable 함수로 분리하여 재사용성을 높입니다.

### 주요 화면 설명

1.  **인증 화면 (`AuthScreen.kt` 또는 관련 파일)**
    - UI 요소: `TextField` (ID, PW 입력), `Button` (로그인, 회원가입).
    - 기능: 사용자 입력 처리, ViewModel을 통한 로그인/회원가입 요청.

2.  **메인 네비게이터 (`VoteNavigator.kt`, `UserInfoScreen.kt` 등)**
    - **`Scaffold`**: 앱의 기본적인 Material Design 구조(TopAppBar, BottomNavigation 또는 FAB 등)를 제공합니다. `paddingValues`를 올바르게 사용하여 컨텐츠가 시스템 UI와 겹치지 않도록 합니다.
    - **`HorizontalPager` 및 `TabRow`**: "투표", "기부", "완료된 제안" 등 여러 카테고리의 제안 목록을 탭으로 구분하여 보여줍니다. (`VoteNavigator.kt`) 또는 사용자의 "진행 중인 제안", "완료된 제안"을 구분합니다. (`UserInfoScreen.kt`)
    - **`LazyColumn`**: 각 탭 내에서 제안 목록을 효율적으로 표시합니다.
    - **`Card`**: 각 제안 항목 또는 사용자 정보를 시각적으로 그룹화하여 표시합니다.
        - **`VotePhaseCard`**: 투표 단계의 제안 정보를 표시하며, 투표 옵션(`RadioButton`), 진행 상황(`LinearProgressIndicator`), 남은 시간, 투표 제출 버튼 등을 포함합니다.
        - **`DonationPhaseCard`**: 기부 단계의 제안 정보를 표시하며, 목표/현재 기부액, 기부금 입력 `OutlinedTextField`, 기부 실행 버튼 등을 포함합니다.
        - **`FinishedProposalCard`**: 완료된 제안의 최종 결과(성공/실패, 최종 투표/기부 현황)를 표시합니다.
        - **`ProposalItemCard` (in `UserInfoScreen.kt`)**: 사용자가 생성한 제안의 요약 정보를 표시합니다.
    - **`FloatingActionButton` / `ExtendedFloatingActionButton`**:
        - 제안 생성 화면으로 이동하는 버튼 (`VoteNavigator.kt`).
        - 제안 목록을 새로고침하는 `RefreshFloatingActionButton` (`VoteNavigator.kt`).
    - **상태 처리**:
        - 로딩 중(`CircularProgressIndicator`), 데이터 없음, 오류 발생 시 사용자에게 적절한 UI 피드백을 제공합니다.
        - `ViewModel`의 `UiState`를 구독하여 UI를 동적으로 업데이트합니다.

3.  **사용자 정보 화면 (`UserInfoScreen.kt`)**
    - UI 요소: `Text` (사용자 ID, 이름, 지갑 주소, 소개, 잔액 등), `Divider`, 위에서 언급한 `HorizontalPager`와 `ProposalItemCard`를 사용한 사용자 제안 목록.
    - 기능: ViewModel로부터 사용자 정보 및 사용자가 생성한 제안 목록을 받아 표시.

4.  **(선택) 제안 생성 화면 (`MakeProposalScreen.kt` - 가정)**
    - UI 요소: `TextField` (제안 제목, 내용, 목표 금액 등), `Button` (제출).
    - 기능: 사용자 입력을 받아 새로운 제안을 생성하는 로직 처리.

<br>

## 프로젝트 구조

```
vo-donate/
├── app/                          # Application Module
│   ├── application /             # Init Manual Dependency Injection      
│   ├── di /                      # DI Module
│   ├── navigation /              # Jetpack Navigation 2    
│   ├── ui /                      
│   │   ├── vote /                # Voting and Donation Screen
│   │   ├── auth /                # Authorization Screen (Default)
│   │   ├── proposal /            # Make Proposal Screen
│   │   ├── user /                # User Information Screen
│   │   ├── components /          # Commonly-Used UI Components 
│   └── MainActivity.kt    
├── core/                         
│   ├── ui /                      # Defines UI Style
│   ├── android /                 # CoroutineScope Dispatchers    
│   ├── network /                 # OkHttp3 and Retrofit2  
│   ├── preferences /             # JWT Token Manager     
│   └── web3 /                    # Logic for Web3j (Not used)
├── data/                      
│   ├── repositories/             
└── build-logic/                  # Convention Plugins 
```

<br>

## 개발 환경

- **IDE**: Android Studio 2024.3.2
- **Kotlin**: 2.1.21
- **Min SDK**: 29
- **Target SDK**: 35

<br>

## 향후 개선 사항

- [ ] 테스트 코드 작성 (Unit Test, UI Test)
- [ ] CI/CD 파이프라인 구축
- [ ] 로컬 데이터 캐싱 (Room 또는 DataStore 활용)
- [ ] 알림 기능 추가 (새로운 제안, 투표/기부 마감 임박 등)
- [ ] 다크 모드 지원 개선
- [ ] 접근성 향상
