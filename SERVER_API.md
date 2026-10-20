# T-Bus 서버 도메인 이전 및 REST API 명세서 (Client 전용)

> 본 문서는 서버 도메인/경로 이전에 따른 Android 프로젝트 설정 변경 사항과 신규 서버의 Swagger(`openapi.yaml`) 명세 중 **`admin`을 제외한 클라이언트(`users`, `board`, `reply`, `notice`) API**를 정리한 문서입니다.

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

[`GlobalVars.java`](app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java) 내 도메인 및 루트 경로 상수가 전체 API 및 이미지 URL 생성에 사용됩니다.

```java
// app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java
public final static String Protocol = "http";
public final static String Domain = "15.164.102.251:8080"; // 기존: "1.234.63.224"

final static String PathRoot = "TBusWebapp2";              // 기존: "TBusWebapp"
```

- [`HttpRequestParams.buildUrl()`](app/src/main/java/com/windnsoft/tbus/Http/HttpRequestParams.java)는 내부적으로 `Uri.Builder.authority(domain)`를 사용하므로 `15.164.102.251:8080` 형태의 포트 포함 주소를 그대로 지원합니다.
- Android 9(API 28) 이상을 타겟팅하여 빌드/실행할 경우 `http://` 평문 통신을 허용하기 위해 [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml)의 `<application>` 태그에 `android:usesCleartextTraffic="true"` 설정이 필요합니다.

---

## 2. 공통 API 규격

- **HTTP Method**: 모든 API는 **`POST`** 방식으로 호출합니다. (대부분의 서블릿은 `GET` 요청 시 `404` 반환)
- **요청 Content-Type**:
  - 일반 요청: `application/x-www-form-urlencoded`
  - 파일 업로드: `multipart/form-data`
- **공통 응답 구조 (`ResponseBasic` / 앱 내 [`ResponseMessageEntity`](app/src/main/java/com/windnsoft/tbus/Http/ResponseMessageEntity.java))**:
  ```json
  {
    "code": 200,
    "msg": "{ ...JSON 문자열로 직렬화된 결과 객체 또는 배열... }"
  }
  ```
  - `code` (`integer`): 처리 결과 코드 (`200` = 성공)
  - `msg` (`string`): 성공 시 결과 객체/리스트가 **JSON 문자열**로 직렬화되어 담기며, 실패 시 오류 메시지가 반환됩니다.
- **HTTP 응답 상태 코드**:
  - `200 OK`: 처리 결과 반환 (`code` 필드로 성공 여부 판단)
  - `400 Bad Request`: 요청 파라미터 오류
  - `404 Not Found`: 필수 파라미터 누락 또는 `GET` 요청

---

## 3. 공통 코드표 (앱 내 상수 기준)

### 3.1 회원 유형 ([`UserTypes.java`](app/src/main/java/com/windnsoft/tbus/Entities/UserTypes.java))
| 값 (`type`) | 상수명 | 설명 |
| :---: | :--- | :--- |
| `1` | `UserTypes.Normal` | 일반 회원 |
| `2` | `UserTypes.Bus` | 관광버스 기사 회원 |
| `3` | `UserTypes.Co` | 업체 회원 |

### 3.2 게시판 ID ([`BoardIDs.java`](app/src/main/java/com/windnsoft/tbus/Entities/BoardIDs.java))
| 값 (`bid`) | 상수명 | 게시판명 | 테이블 키 (`title` / `reply`) |
| :---: | :--- | :--- | :--- |
| `1` | `BoardIDs.Free` | 자유게시판 | `bfree` / `bfreere` |
| `2` | `BoardIDs.Bus` | 관광버스 | `bbus` / `bbusre` |
| `4` | `BoardIDs.Traction` | 긴급견인 | `btraction` / `btractionre` |
| `9` | `BoardIDs.My` | 내가쓴글 | - |
| `11` | `BoardIDs.Deals` | 직거래장터 | `bdeals` / `bdealsre` |
| `12` | `BoardIDs.Interdiction` | 단속공유 | `binterdiction` / `binterdictionre` |
| `13` | `BoardIDs.Dispense` | 배차공차 | `bdispense` / `bdispensere` |
| `14` | `BoardIDs.News` | 업계뉴스 | `bnews` / `bnewsre` |
| `15` | `BoardIDs.Traffic` | 교통정보 | `btraffic` / `btrafficre` |
| `16` | `BoardIDs.Festival` | 축제일정 | `bfestival` / `bfestivalre` |
| `110` | `BoardIDs.Estimate` | 버스견적 | `estimate` / `estimatere` |
| `120` | `BoardIDs.Mountain` | 산행, 여행 | `mountain` / `mountainre` |

### 3.3 산악 게시글 조회 필터 ([`MountainListType.java`](app/src/main/java/com/windnsoft/tbus/Entities/MountainListType.java))
| 값 (`listType`) | 상수명 | 설명 |
| :---: | :--- | :--- |
| `1` | `MountainListType.All` | 전체 보기 |
| `2` | `MountainListType.BookEnabled` | 예약 가능 |
| `3` | `MountainListType.BookDisabled` | 예약 완료 |

---

## 4. 클라이언트 API 명세 (`admin` 제외)

### 4.1 회원 / 업체 (`users`)

#### 1) 회원 로그인
- **Endpoint**: `POST /servlet/users/login` (`operationId: loginUser`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 전화번호(`tel`)로 회원을 조회합니다. 성공 시 `msg`에 `UserEntity` JSON 문자열이 반환됩니다.
- **앱 내 사용처**: `GlobalVars.PathUserLoginUrl` → `UsersOperator.login()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 / 예시 |
  | :--- | :--- | :---: | :--- |
  | `tel` | `string` | O | 전화번호 (예: `"01012345678"`) |

#### 2) 일반/업체 회원가입
- **Endpoint**: `POST /servlet/users/join` (`operationId: joinUser`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathUserJoinUrl` → `UsersOperator.register()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `type` | `integer` | O | 회원 유형 (`1`: 일반, `2`: 버스, `3`: 업체) |
  | `tel` | `string` | O | 전화번호 |
  | `name` | `string` | X | 이름 |
  | `garage` | `string` | X | 차고지 |
  | `ride` | `string` | X | 차량 종류 |
  | `passengers` | `integer` | X | 탑승 인원 |
  | `capacity` | `integer` | X | 수용 인원 |
  | `car_number` | `string` | X | 차량 번호 |
  | `product` | `string` | X | 취급 품목 / 소개 |
  | `sectors` | `string` | X | 업종 |
  | `locale` | `integer` / `string` | X | 지역 |
  | `addr` | `string` | X | 주소 |
  | `manager_name` | `string` | X | 담당자 이름 |
  | `manager_tel` | `string` | X | 담당자 연락처 |
  | `contact` | `string` | X | 대표 연락처 |
  | `usage` | `string` | X | 용도 |
  | `menu` | `string` | X | 메뉴 |
  | `capacity_copy1` | `string` | X | 수용 규모 보조 필드 |
  | `time` | `string` | X | 영업 시간 |
  | `closed` | `string` | X | 휴무일 |
  | `parking` | `string` | X | 주차 가능 여부 |
  | `booking` | `boolean` | X | 예약 가능 여부 |
  | `detail` | `string` | X | 상세 설명 |

#### 3) 업체 회원가입 (프로필/사진 이미지 포함)
- **Endpoint**: `POST /servlet/users/joinCo` (`operationId: joinCoUser`)
- **Content-Type**: `multipart/form-data`
- **앱 내 사용처**: `GlobalVars.PathUserJoinCoUrl` → `UsersOperator.registerCo()`
- **요청 파트**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `user` | `string` | O | `UserEntity` JSON 문자열 (예: `{"type":3,"tel":"01012345678","name":"업체명"}`) |
  | `profile` | `binary` | X | 프로필 이미지 파일 |
  | `file0..N` | `binary` | X | 업체 추가 사진 파일 (앱에서 `file0`, `file1`... 형태로 전송) |

#### 4) 버스 기사 회원가입 (프로필/사진 이미지 포함)
- **Endpoint**: `POST /servlet/users/joinBus` (`operationId: joinBusUser`)
- **Content-Type**: `multipart/form-data`
- **앱 내 사용처**: `GlobalVars.PathUserJoinBusUrl` → `UsersOperator.registerBus()`
- **요청 파트**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `type` | `integer` | X | 회원 유형 (`2`) |
  | `garage` | `string` | X | 차고지 |
  | `ride` | `string` | X | 차량 종류 |
  | `company_name` | `string` | X | 회사명 |
  | `passengers` | `integer` | X | 탑승 정원 |
  | `name` | `string` | X | 기사 이름 |
  | `tel` | `string` | X | 전화번호 |
  | `possible_date` | `string` | X | 운행 가능일 |
  | `car_number` | `string` | X | 차량 번호 |
  | `profile` | `binary` | X | 프로필 이미지 파일 |
  | `file0..N` | `binary` | X | 차량 추가 사진 파일 (앱에서 `file0`, `file1`... 형태로 전송) |

#### 5) 회원 정보 조회
- **Endpoint**: `POST /servlet/users/getInfo` (`operationId: getUserInfo`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathUserInfoUrl` → `UsersOperator.getUserInfo()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 회원 고유 ID (`id`) |

#### 6) 회원 목록 조회 (페이징)
- **Endpoint**: `POST /servlet/users/getUserList` (`operationId: getUserList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `type` | `integer` | O | 회원 유형 |
  | `page` | `integer` | O | 페이지 번호 (예: `1`) |
  | `limit` | `integer` | O | 페이지당 개수 (예: `20`) |

#### 7) 회원 검색
- **Endpoint**: `POST /servlet/users/searchUserList` (`operationId: searchUserList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `type` | `integer` | O | 회원 유형 |
  | `keyword` | `string` | O | 검색어 |

#### 8) 업종/지역별 업체 목록 조회
- **Endpoint**: `POST /servlet/users/getCoList` (`operationId: getCoList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathCoListUrl` → `CompanyListView.loadList()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `sectors` | `string` | O | 업종 구분 |
  | `locale` | `string` | O | 지역명 (예: `"서울 강남구"`) |

#### 9) 업체 위치(위경도) 목록 조회
- **Endpoint**: `POST /servlet/users/getLatLngList` (`operationId: getCoLatLngList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `token` | `string` | O | 토큰 |

#### 10) 회원 정보 수정
- **Endpoint**: `POST /servlet/users/updateMember` (`operationId: updateUserInfo`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `user_json` | `string` | O | `UserEntity` JSON 문자열 (예: `{"id":1,"name":"홍길동"}`) |

#### 11) 회원 위치(위경도) 수정
- **Endpoint**: `POST /servlet/users/updateLatLng` (`operationId: updateUserLatLng`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `user_json` | `string` | O | `UserLatLngEntity` JSON 문자열 |

#### 12) 프로필 사진 변경
- **Endpoint**: `POST /servlet/users/updateProfilePicture` (`operationId: updateUserProfilePicture`)
- **Content-Type**: `multipart/form-data`
- **앱 내 사용처**: 미사용 *(참고: `GlobalVars.PathUploadUserPicture`는 구 경로인 `/servlet/users/UploadPicture`로 정의되어 있음)*
- **요청 파트**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 회원 ID |
  | `file` | `binary` | O | 변경할 프로필 이미지 파일 |

#### 13) 회원 사진 추가
- **Endpoint**: `POST /servlet/users/insertUserPhoto` (`operationId: insertUserPhoto`)
- **Content-Type**: `multipart/form-data`
- **앱 내 사용처**: 미사용
- **요청 파트**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 회원 ID |
  | `file` | `binary` | O | 추가할 사진 파일 |

#### 14) 회원 사진 삭제
- **Endpoint**: `POST /servlet/users/removeUserPhoto` (`operationId: removeUserPhoto`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 회원 ID |
  | `pos` | `integer` | O | 삭제할 사진 위치(인덱스) |

---

### 4.2 게시판 / 견적 / 산악 (`board`)

#### 1) 게시글 목록 조회
- **Endpoint**: `POST /servlet/board/getList` (`operationId: getBoardList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathBoardList` → `BoardListView.loadList()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `bid` | `integer` | O | 게시판 ID (`BoardIDs` 참조) |

#### 2) 게시글 목록 조회 (페이징)
- **Endpoint**: `POST /servlet/board/getListPaging` (`operationId: getBoardListPaging`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `bid` | `integer` | O | 게시판 ID |
  | `pid` | `integer` | X | 페이지 기준 게시글 ID |

#### 3) 내가 작성한 게시글 목록 조회
- **Endpoint**: `POST /servlet/board/getMyList` (`operationId: getBoardMyList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathBoardMyList` → `BoardListView.loadList()` (`bid == BoardIDs.My`)
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 회원 ID |

#### 4) 게시글 상세 조회
- **Endpoint**: `POST /servlet/board/getDetail` (`operationId: getBoardDetail`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: 미사용 *(앱에서는 목록 조회 결과를 그대로 `BoardDetailFragment`에 전달)*
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `bid` | `integer` | O | 게시판 ID |
  | `id` | `integer (int64)` | O | 게시글 ID |

#### 5) 게시글 작성 (이미지 첨부)
- **Endpoint**: `POST /servlet/board/Upload` (`operationId: uploadBoard`)
- **Content-Type**: `multipart/form-data`
- **앱 내 사용처**: `GlobalVars.PathUploadBoard` → `BoardOperator.upload()`
- **요청 파트**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 작성자 회원 ID |
  | `bid` | `integer` | O | 게시판 ID |
  | `contents` | `string` | O | 게시글 본문 내용 |
  | `files` (`file0..N`) | `binary[]` | X | 첨부 이미지 파일 (필드명 무관, 앱에서는 `file0`, `file1`... 전송) |

#### 6) 버스 견적 목록 조회
- **Endpoint**: `POST /servlet/board/getBusEstimateList` (`operationId: getBusEstimateList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathBusEstimateList` → `BusEstimateListView.loadList()`
- **요청 파라미터**: 없음 (`msg`에 `BusEstimateEntity` 리스트 JSON 문자열 반환)

#### 7) 버스 견적 등록
- **Endpoint**: `POST /servlet/board/UploadBusEstimate` (`operationId: uploadBusEstimate`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathUploadBusEstimate` → `BoardOperator.uploadBusEstimate()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `estimate` | `string` | O | `BusEstimateEntity` JSON 직렬화 문자열 (`uid`, `title`, `start_time`, `arrival_time`, `start_locale`, `arrival_locale`, `term`, `capacity`, `round`, `vehicle`, `cars` 등) |

#### 8) 산악 게시글 목록 조회
- **Endpoint**: `POST /servlet/board/getMountainList` (`operationId: getMountainList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathMountainList` → `MountainListView.loadList()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `listType` | `integer` | O | 조회 유형 (`1`: 전체, `2`: 예약 가능, `3`: 예약 완료) |

#### 9) 산악 게시글 등록
- **Endpoint**: `POST /servlet/board/UploadMountain` (`operationId: uploadMountain`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathUploadMountain` → `BoardOperator.uploadMountain()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `mountain` | `string` | O | `MountainEntity` JSON 직렬화 문자열 (`uid`, `title`, `state`, `locale`, `start_t`, `term`, `tax` 등) |

#### 10) 산악 게시글 예약 상태 변경
- **Endpoint**: `POST /servlet/board/updateMountainState` (`operationId: updateMountainState`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathMountainBookStateChange` → `MountainDetailFragment.sendBookStateChange()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `uid` | `integer` | O | 작성자 회원 ID |
  | `id` | `integer (int64)` | O | 산악 게시글 ID |
  | `state` | `boolean` | O | 예약 상태 (`true`: 예약 가능, `false`: 예약 완료) |

---

### 4.3 댓글 (`reply`)

#### 1) 댓글 목록 조회
- **Endpoint**: `POST /servlet/reply/getList` (`operationId: getReplyList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathReplyList` → `ReplayOperator.getList()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `bid` | `integer` | O | 게시판 ID |
  | `id` | `integer (int64)` | O | 게시글 ID |

#### 2) 댓글 등록
- **Endpoint**: `POST /servlet/reply/insert` (`operationId: insertReply`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathReplyInsert` → `ReplayOperator.insert()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `bid` | `integer` | O | 게시판 ID |
  | `uid` | `integer` | O | 작성자 회원 ID |
  | `idx` | `integer (int64)` | O | 게시글 ID |
  | `text` | `string` | O | 댓글 내용 |

---

### 4.4 공지사항 (`notice`)

#### 1) 공지사항 목록 조회
- **Endpoint**: `POST /servlet/notice/getList` (`operationId: getNoticeList`)
- **Content-Type**: `application/x-www-form-urlencoded`
- **앱 내 사용처**: `GlobalVars.PathNoticeList` → `BoardListView.loadNotice()`
- **요청 파라미터**:
  | 필드명 | 타입 | 필수 | 설명 |
  | :--- | :--- | :---: | :--- |
  | `bid` | `integer` | O | 게시판 ID |
