# T-Bus 서버 도메인 이전 및 앱 호출 Endpoint 명세서

> 본 문서는 서버 도메인/경로 이전에 따른 Android 프로젝트 설정 변경 사항과 **앱([`GlobalVars.java`](app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java))에서 실제로 호출하는 16개 Endpoint 명세 및 신규 서버 연동 테스트 결과**를 정리한 문서입니다. (`admin` 및 앱 미사용 서블릿 제외)

---

## 1. 서버 도메인 및 경로 이전 정보

### 1.1 서버 주소 비교

| 구분 | 이전 서버 (기존 코드) | 신규 이전 서버 |
| :--- | :--- | :--- |
| **Protocol** | `http` | `http` |
| **Domain (Host:Port)** | `1.234.63.224` | `15.164.102.251:8080` |
| **Context Path (`PathRoot`)** | `TBusWebapp` | `TBusWebapp2` |
| **API Base URL** | `http://1.234.63.224/TBusWebapp` | `http://15.164.102.251:8080/TBusWebapp2` |
| **이미지 기본 경로 (`tbuspic`)** | `http://1.234.63.224/tbuspic/{filename}` | `http://15.164.102.251:8080/tbuspic/{filename}` |
| **광고 이미지 경로 (`adpic`)** | `http://1.234.63.224/tbuspic/adpic/{filename}` | `http://15.164.102.251:8080/tbuspic/adpic/{filename}` |
| **Swagger UI** | - | `http://15.164.102.251:8080/TBusWebapp2/swagger/index.html` |
| **OpenAPI Spec** | - | `http://15.164.102.251:8080/TBusWebapp2/swagger/openapi.yaml` |

### 1.2 Android 프로젝트 내 수정 위치

[`GlobalVars.java`](app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java) 내 `Domain`과 `PathRoot` 2곳만 변경하면 앱에서 호출하는 모든 API와 이미지 경로가 신규 서버를 바라보게 됩니다.

```java
// app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java
public final static String Protocol = "http";
public final static String Domain = "15.164.102.251:8080"; // 기존: "1.234.63.224"

final static String PathRoot = "TBusWebapp2";              // 기존: "TBusWebapp"
```

> **참고**: `minSdk 28` 이상 환경에서 `http://` 평문 통신을 수행하려면 [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml)의 `<application>` 태그에 `android:usesCleartextTraffic="true"` 속성이 필요합니다.

---

## 2. 앱 실제 호출 Endpoint 목록 및 테스트 결과 요약 (총 16개)

| 분류 | GlobalVars 상수명 | Endpoint 경로 | 앱 내 호출 위치 | 신규 서버 상태 |
| :---: | :--- | :--- | :--- | :---: |
| **회원/업체** | `PathUserLoginUrl` | `POST /servlet/users/login` | `UsersOperator.login()` (`IntroActivity`, `RegisterNormalFragment`) | ✅ 정상 (`200`) |
| **회원/업체** | `PathUserJoinUrl` | `POST /servlet/users/join` | `UsersOperator.register()` (`RegisterNormalFragment`) | ✅ 스펙 일치 |
| **회원/업체** | `PathUserJoinBusUrl` | `POST /servlet/users/joinBus` | `UsersOperator.registerBus()` (`RegisterBusFragment`) | ✅ 스펙 일치 |
| **회원/업체** | `PathUserJoinCoUrl` | `POST /servlet/users/joinCo` | `UsersOperator.registerCo()` (`RegisterCoFragment`) | ✅ 스펙 일치 |
| **회원/업체** | `PathUserInfoUrl` | `POST /servlet/users/getInfo` | `UsersOperator.getUserInfo()` (`UserDetailFragment`) | ✅ 정상 (`200`) |
| **회원/업체** | `PathCoListUrl` | `POST /servlet/users/getCoList` | `CompanyListView.loadList()` | ✅ 정상 (`200`) |
| **게시판** | `PathBoardList` | `POST /servlet/board/getList` | `BoardListView.loadList()` | ✅ 정상 (`200`) |
| **게시판** | `PathBoardMyList` | `POST /servlet/board/getMyList` | `BoardListView.loadList()` (`bid == BoardIDs.My`) | ✅ 정상 (`200`) |
| **게시판** | `PathUploadBoard` | `POST /servlet/board/Upload` | `BoardOperator.upload()` (`BoardWriteFragment`) | ✅ 스펙 일치 |
| **버스견적** | `PathBusEstimateList` | `POST /servlet/board/getBusEstimateList` | `BusEstimateListView.loadList()` | ✅ 정상 (`200`) |
| **버스견적** | `PathUploadBusEstimate` | `POST /servlet/board/UploadBusEstimate` | `BoardOperator.uploadBusEstimate()` (`BusEstimateWriteFragment`) | ✅ 스펙 일치 |
| **산악/여행** | `PathMountainList` | `POST /servlet/board/getMountainList` | `MountainListView.loadList()` | ✅ 정상 (`200`) |
| **산악/여행** | `PathUploadMountain` | `POST /servlet/board/UploadMountain` | `BoardOperator.uploadMountain()` (`MountainWriteFragment`) | ✅ 스펙 일치 |
| **산악/여행** | `PathMountainBookStateChange` | `POST /servlet/board/updateMountainState` | `MountainDetailFragment.sendBookStateChange()` | ✅ 스펙 일치 |
| **댓글** | `PathReplyList` | `POST /servlet/reply/getList` | `ReplayOperator.getList()` (각 상세 Fragment) | ✅ 정상 (`200`) |
| **댓글** | `PathReplyInsert` | `POST /servlet/reply/insert` | `ReplayOperator.insert()` (각 상세 Fragment) | ✅ 스펙 일치 |
| **공지사항** | `PathNoticeList` | `POST /servlet/notice/getList` | `BoardListView.loadNotice()` | ✅ 정상 (`200`) |
| **정적 이미지** | `GetPictureFullPath()` | `GET /tbuspic/{filename}` | 프로필 및 게시글/업체 첨부 이미지 로드 | ✅ 정상 (`200`) |
| **광고 이미지** | `GetAdFullPath()` | `GET /tbuspic/adpic/{1,2,3}.jpg` | `MainActivity`, `CoListActivity`, `InfoListActivity` | ✅ 정상 (`200`) |

> **참고**: `GlobalVars.PathUploadUserPicture` (`/servlet/users/UploadPicture`) 및 `UsersOperator.getCoUserList()`는 코드 내 선언만 되어 있고 앱 UI 어디에서도 호출되지 않는 미사용 코드입니다.

---

## 3. 공통 API 규격 및 상수 코드표

### 3.1 요청 및 응답 규격
- **HTTP Method**: 모든 API는 **`POST`** 방식으로 호출합니다. (`GET` 요청 시 `404` 반환)
- **Content-Type**:
  - 일반 요청: `application/x-www-form-urlencoded`
  - 파일 업로드: `multipart/form-data`
- **공통 응답 구조 (`ResponseBasic` / 앱 내 [`ResponseMessageEntity`](app/src/main/java/com/windnsoft/tbus/Http/ResponseMessageEntity.java))**:
  ```json
  {
    "code": 200,
    "msg": "{ ...JSON 문자열로 직렬화된 결과 객체 또는 배열... }"
  }
  ```

### 3.2 주요 코드 상수
- **회원 유형 ([`UserTypes.java`](app/src/main/java/com/windnsoft/tbus/Entities/UserTypes.java))**: `1` = 일반(`Normal`), `2` = 관광버스 기사(`Bus`), `3` = 업체(`Co`)
- **게시판 ID ([`BoardIDs.java`](app/src/main/java/com/windnsoft/tbus/Entities/BoardIDs.java))**:
  - `1`: 자유게시판 (`Free`)
  - `2`: 관광버스 (`Bus`)
  - `4`: 긴급견인 (`Traction`)
  - `9`: 내가쓴글 (`My`)
  - `11`: 직거래장터 (`Deals`)
  - `12`: 단속공유 (`Interdiction`)
  - `13`: 배차공차 (`Dispense`)
  - `14`: 업계뉴스 (`News`)
  - `15`: 교통정보 (`Traffic`)
  - `16`: 축제일정 (`Festival`)
  - `110`: 버스견적 (`Estimate`)
  - `120`: 산행, 여행 (`Mountain`)
- **산악 게시글 조회 필터 ([`MountainListType.java`](app/src/main/java/com/windnsoft/tbus/Entities/MountainListType.java))**: `1` = 전체(`All`), `2` = 예약 가능(`BookEnabled`), `3` = 예약 완료(`BookDisabled`)

---

## 4. 앱 호출 Endpoint 상세 명세

### 4.1 회원 / 업체 (`users`)

#### 1) 회원 로그인 — `POST /servlet/users/login`
- **상수**: `GlobalVars.PathUserLoginUrl`
- **호출부**: [`UsersOperator.login()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L78-L86)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `tel` | `string` | O | 전화번호 (예: `"01012345678"`) |
- **응답**: `msg`에 [`UserEntity`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java) JSON 문자열 반환 (미가입 번호인 경우 `{"code":405}`)

#### 2) 일반 회원가입 — `POST /servlet/users/join`
- **상수**: `GlobalVars.PathUserJoinUrl`
- **호출부**: [`UsersOperator.register()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L24-L76)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `type`(필수), `tel`(필수), `name` 등 `UserEntity` 필드

#### 3) 버스 기사 회원가입 — `POST /servlet/users/joinBus`
- **상수**: `GlobalVars.PathUserJoinBusUrl`
- **호출부**: [`UsersOperator.registerBus()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L88-L128)
- **Content-Type**: `multipart/form-data`
- **요청 파트**: `type`, `tel`, `name`, `garage`, `ride`, `company_name`, `passengers`, `possible_date`, `car_number`, `profile`(파일), `file0..N`(추가 사진 파일)

#### 4) 업체 회원가입 — `POST /servlet/users/joinCo`
- **상수**: `GlobalVars.PathUserJoinCoUrl`
- **호출부**: [`UsersOperator.registerCo()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L130-L146)
- **Content-Type**: `multipart/form-data`
- **요청 파트**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `user` | `string` | O | `UserEntity` JSON 문자열 |
  | `profile` | `binary` | X | 대표 프로필 이미지 |
  | `file0..N` | `binary` | X | 업체 추가 이미지 |

#### 5) 회원 정보 조회 — `POST /servlet/users/getInfo`
- **상수**: `GlobalVars.PathUserInfoUrl`
- **호출부**: [`UsersOperator.getUserInfo()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L148-L156)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `uid` (`integer`, 필수)

#### 6) 업종/지역별 업체 목록 조회 — `POST /servlet/users/getCoList`
- **상수**: `GlobalVars.PathCoListUrl`
- **호출부**: [`CompanyListView.loadList()`](app/src/main/java/com/windnsoft/tbus/Fragment/CompanyListView.java#L52-L99)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `sectors` (`string`, 필수), `locale` (`string`, 필수)

---

### 4.2 게시판 / 견적 / 산악 (`board`)

#### 7) 게시글 목록 조회 — `POST /servlet/board/getList`
- **상수**: `GlobalVars.PathBoardList`
- **호출부**: [`BoardListView.loadList()`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L79-L142)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `bid` (`integer`, 필수)

#### 8) 내가 쓴 게시글 목록 조회 — `POST /servlet/board/getMyList`
- **상수**: `GlobalVars.PathBoardMyList`
- **호출부**: [`BoardListView.loadList()`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L131-L135)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `uid` (`integer`, 필수)

#### 9) 게시글 작성 — `POST /servlet/board/Upload`
- **상수**: `GlobalVars.PathUploadBoard`
- **호출부**: [`BoardOperator.upload()`](app/src/main/java/com/windnsoft/tbus/Operator/BoardOperator.java#L25-L43)
- **Content-Type**: `multipart/form-data`
- **요청 파트**: `uid` (`integer`, 필수), `bid` (`integer`, 필수), `contents` (`string`, 필수), `file0..N` (`binary`, 선택)

#### 10) 버스 견적 목록 조회 — `POST /servlet/board/getBusEstimateList`
- **상수**: `GlobalVars.PathBusEstimateList`
- **호출부**: [`BusEstimateListView.loadList()`](app/src/main/java/com/windnsoft/tbus/Fragment/BusEstimateListView.java#L48-L90)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: 없음

#### 11) 버스 견적 등록 — `POST /servlet/board/UploadBusEstimate`
- **상수**: `GlobalVars.PathUploadBusEstimate`
- **호출부**: [`BoardOperator.uploadBusEstimate()`](app/src/main/java/com/windnsoft/tbus/Operator/BoardOperator.java#L45-L53)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `estimate` ([`BusEstimateEntity`](app/src/main/java/com/windnsoft/tbus/Entities/BusEstimateEntity.java) JSON 문자열, 필수)

#### 12) 산악 게시글 목록 조회 — `POST /servlet/board/getMountainList`
- **상수**: `GlobalVars.PathMountainList`
- **호출부**: [`MountainListView.loadList()`](app/src/main/java/com/windnsoft/tbus/Fragment/MountainListView.java#L52-L98)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `listType` (`integer`, 필수 — `1`: 전체, `2`: 예약 가능, `3`: 예약 완료)

#### 13) 산악 게시글 등록 — `POST /servlet/board/UploadMountain`
- **상수**: `GlobalVars.PathUploadMountain`
- **호출부**: [`BoardOperator.uploadMountain()`](app/src/main/java/com/windnsoft/tbus/Operator/BoardOperator.java#L56-L63)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `mountain` ([`MountainEntity`](app/src/main/java/com/windnsoft/tbus/Entities/MountainEntity.java) JSON 문자열, 필수)

#### 14) 산악 게시글 예약 상태 변경 — `POST /servlet/board/updateMountainState`
- **상수**: `GlobalVars.PathMountainBookStateChange`
- **호출부**: [`MountainDetailFragment.sendBookStateChange()`](app/src/main/java/com/windnsoft/tbus/Fragment/MountainDetailFragment.java#L219-L257)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `uid` (`integer`, 필수), `id` (`int64`, 필수), `state` (`boolean`, 필수)

---

### 4.3 댓글 (`reply`) & 공지사항 (`notice`)

#### 15) 댓글 목록 조회 — `POST /servlet/reply/getList`
- **상수**: `GlobalVars.PathReplyList`
- **호출부**: [`ReplayOperator.getList()`](app/src/main/java/com/windnsoft/tbus/Operator/ReplayOperator.java#L25-L32)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `bid` (`integer`, 필수), `id` (`int64`, 필수)

#### 16) 댓글 등록 — `POST /servlet/reply/insert`
- **상수**: `GlobalVars.PathReplyInsert`
- **호출부**: [`ReplayOperator.insert()`](app/src/main/java/com/windnsoft/tbus/Operator/ReplayOperator.java#L12-L23)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `bid` (`integer`, 필수), `uid` (`integer`, 필수), `idx` (`int64`, 필수), `text` (`string`, 필수)

#### 17) 공지사항 목록 조회 — `POST /servlet/notice/getList`
- **상수**: `GlobalVars.PathNoticeList`
- **호출부**: [`BoardListView.loadNotice()`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L144-L179)
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**: `bid` (`integer`, 필수)
