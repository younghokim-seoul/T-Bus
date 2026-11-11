# T-Bus REST API 명세서 (`TBusWebapp2`)

> 본 문서는 `http://3.37.185.61:8080/TBusWebapp2/swagger/openapi.yaml` (Swagger UI: `http://3.37.185.61:8080/TBusWebapp2/swagger/index.html`) 기준으로 TBusWebapp2 서블릿 기반 REST API 전체 명세와 Android 프로젝트 내 연동 위치를 정리한 문서입니다.

---

## 1. 공통 규격

- **Base URL**: `http://3.37.185.61:8080/TBusWebapp2/` ([`GlobalVars.java`](app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java))
- **HTTP Method**: 모든 API는 **`POST`**로 호출합니다. (대부분의 서블릿은 `GET` 요청 시 `404`를 반환)
- **Content-Type**:
  - 일반 요청: `application/x-www-form-urlencoded`
  - 파일 업로드 요청: `multipart/form-data`
- **공통 응답 구조 (`ResponseBasic` / [`ResponseMessageEntity`](app/src/main/java/com/windnsoft/tbus/Http/ResponseMessageEntity.java))**:
  ```json
  {
    "code": 200,
    "msg": "{ ... JSON 문자열 ... }"
  }
  ```
  - `code`: 처리 결과 코드 (`200` = `ResponseCodes.CodeOk` 성공)
  - `msg`: 성공 시 결과 객체(또는 배열)를 **JSON 문자열로 직렬화한 값**, 실패 시 오류 메시지
- **이미지 정적 경로**:
  - 일반 업로드 사진: `http://3.37.185.61:8080/tbuspic/{filename}` (`GlobalVars.GetPictureFullPath(filename)`)
  - 광고 사진: `http://3.37.185.61:8080/tbuspic/adpic/{filename}` (`GlobalVars.GetAdFullPath(filename)`)

---

## 2. 회원 / 업체 API (`users`)

### 2.1 회원 로그인
- **Endpoint**: `POST /servlet/users/login`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 전화번호(`tel`)로 회원을 조회합니다. 성공 시 `msg`에 [`UserEntity`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java) JSON 문자열이 반환됩니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 | 예시 |
  | :--- | :---: | :---: | :--- | :--- |
  | `tel` | `string` | Y | 회원 전화번호 | `"01012345678"` |
- **Android 연동**: [`UsersOperator.login()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java)

---

### 2.2 일반/업체 회원가입 (Form)
- **Endpoint**: `POST /servlet/users/join`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 일반회원(`type=1`) 등의 회원가입을 처리합니다. 성공 시 `msg`에 생성된 [`UserEntity`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java) JSON 문자열 반환.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `type` | `integer` | Y | 회원 유형 (`1`: 일반, `2`: 버스, `3`: 업체) |
  | `tel` | `string` | Y | 전화번호 |
  | `name` | `string` | N | 이름 / 닉네임 |
  | `garage` | `string` | N | 차고지 |
  | `ride` | `string` | N | 보유차량(승차 지역) |
  | `passengers` | `integer` | N | 탑승 인원(승차 인승) |
  | `capacity` | `integer` | N | 수용 인원 |
  | `car_number` | `string` | N | 차량 번호 |
  | `product` | `string` | N | 주요 취급품목 |
  | `sectors` | `string` | N | 업종 |
  | `locale` | `integer` / `string` | N | 지역 |
  | `addr` | `string` | N | 상세 주소 |
  | `manager_name` | `string` | N | 담당자 성함 |
  | `manager_tel` | `string` | N | 담당자 연락처 |
  | `contact` | `string` | N | 대표 연락처 |
  | `usage` | `string` | N | 용도 |
  | `menu` | `string` | N | 주요 메뉴 |
  | `capacity_copy1` | `string` | N | 수용 인원 보조 필드 |
  | `time` | `string` | N | 영업시간 |
  | `closed` | `string` | N | 휴무일 |
  | `parking` | `string` | N | 주차 가능 여부/대수 |
  | `booking` | `boolean` | N | 예약 가능 여부 |
  | `detail` | `string` | N | 상세 설명 |
- **Android 연동**: [`UsersOperator.register()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java)

---

### 2.3 업체 회원가입 (프로필 및 업체 사진 포함)
- **Endpoint**: `POST /servlet/users/joinCo`
- **Content-Type**: `multipart/form-data`
- **설명**: 업체회원(`type=3`) 가입 및 프로필/업체 사진을 업로드합니다.
- **요청 파트**:
  | 파트명 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `user` | `string` | Y | `UserEntity` JSON 문자열 (예: `{"type":3,"tel":"01012345678","name":"업체명",...}`) |
  | `profile` | `binary (file)` | N | 대표 프로필 이미지 파일 |
  | `file0`, `file1`, ... | `binary (file)` | N | 추가 업체 사진 파일 목록 |
- **Android 연동**: [`UsersOperator.registerCo()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java)

---

### 2.4 버스 기사 회원가입 (프로필 및 명함/차량 사진 포함)
- **Endpoint**: `POST /servlet/users/joinBus`
- **Content-Type**: `multipart/form-data`
- **설명**: 관광버스 회원(`type=2`) 가입 및 프로필/명함 사진을 업로드합니다.
- **요청 파트**:
  | 파트명 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `type` | `integer` | N | 회원 유형 (`2`) |
  | `garage` | `string` | N | 차고지역 |
  | `ride` | `string` | N | 승차 지역(보유차량) |
  | `company_name` | `string` | N | 회사 상호 |
  | `passengers` | `integer` | N | 승차 인승 |
  | `name` | `string` | N | 운전자명 |
  | `tel` | `string` | N | 전화번호 |
  | `possible_date` | `string` | N | 운행 가능일 |
  | `car_number` | `string` | N | 차량 번호 |
  | `profile` | `binary (file)` | N | 프로필 이미지 파일 |
  | `file0`, `file1`, ... | `binary (file)` | N | 명함/차량 추가 사진 파일 목록 |
- **Android 연동**: [`UsersOperator.registerBus()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java)

---

### 2.5 회원 상세 정보 조회
- **Endpoint**: `POST /servlet/users/getInfo`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 회원 고유 ID(`uid`)로 회원의 전체 상세 정보를 조회합니다. 성공 시 `msg`에 [`UserEntity`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java) JSON 문자열 반환.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 회원 ID (`user.id`) |
- **Android 연동**:
  - [`MemberApiService.getUserInfo()`](app/src/main/java/com/windnsoft/tbus/feature/member/MemberApiService.kt)
  - [`UsersOperator.getUserInfo()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java)

---

### 2.6 회원 목록 조회 (페이징)
- **Endpoint**: `POST /servlet/users/getUserList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 회원 유형별 페이징된 회원 목록을 조회합니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 | 예시 |
  | :--- | :---: | :---: | :--- | :--- |
  | `type` | `integer` | Y | 회원 유형 (`1`, `2`, `3`) | `2` |
  | `page` | `integer` | Y | 페이지 번호 | `1` |
  | `limit` | `integer` | Y | 페이지당 개수 | `20` |
- **Android 연동**: [`MemberApiService.getUserList()`](app/src/main/java/com/windnsoft/tbus/feature/member/MemberApiService.kt)

---

### 2.7 회원 검색
- **Endpoint**: `POST /servlet/users/searchUserList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 회원 유형 및 키워드로 회원을 검색합니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `type` | `integer` | Y | 회원 유형 |
  | `keyword` | `string` | Y | 검색어 |
- **Android 연동**: [`MemberApiService.searchUserList()`](app/src/main/java/com/windnsoft/tbus/feature/member/MemberApiService.kt)

---

### 2.8 업종/지역별 업체 목록 조회
- **Endpoint**: `POST /servlet/users/getCoList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 업종(`sectors`)과 지역(`locale`)으로 업체 목록을 조회합니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `sectors` | `string` | Y | 업종 |
  | `locale` | `string` | Y | 지역 |

---

### 2.9 업체 위치(위경도) 목록 조회 (소문난 업소 지도 마커용)
- **Endpoint**: `POST /servlet/users/getLatLngList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 전체 등록 업체의 지도 마커 표시용 위치(위도/경도) 및 상호명 목록을 일괄 조회합니다. 마커 클릭 시 해당 항목의 `id`를 이용해 `POST /servlet/users/getInfo` (`uid = id`)를 호출하면 업체 상세 정보([`UserEntity`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java))를 조회할 수 있습니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `token` | `string` | Y | 토큰 문자열 (임의 값 전달 시 서버가 앞단에 `{"code":405,"msg":"tk"}`를 출력한 뒤 이어서 `{"code":200,"msg":"[...]"}` JSON을 반환함) |
- **응답 `msg` 내부 배열 요소 구조 (`UserLatLngEntity`)**:
  ```json
  [
    {
      "id": 520,
      "co": "전남 무안 윤희네뻘낙지",
      "lat": 34.9873465,
      "lng": 126.476711,
      "addr": ""
    }
  ]
  ```
  - `id` (`int`): 업체 회원 고유 ID (`uid`)
  - `co` (`string`): 업체 상호명 (`company_name`)
  - `lat` (`double`): 위도 (Latitude)
  - `lng` (`double`): 경도 (Longitude)
  - `addr` (`string`): 주소

---

### 2.10 회원 정보 수정 (프로필 텍스트 정보 수정)
- **Endpoint**: `POST /servlet/users/updateMember`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 회원의 프로필 텍스트 정보를 수정합니다. `UserEntity` 객체를 JSON 문자열로 직렬화하여 `user_json` 필드로 전송합니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 | 예시 |
  | :--- | :---: | :---: | :--- | :--- |
  | `user_json` | `string` | Y | `UserEntity` JSON 문자열 | `'{"id":1,"name":"홍길동","garage":"서울",...}'` |
- **응답**:
  - `200`: `ResponseBasic` (`code == 200` 성공)
  - `400`: 요청 파라미터 오류

---

### 2.11 회원 위치(위경도) 수정
- **Endpoint**: `POST /servlet/users/updateLatLng`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `user_json` | `string` | Y | `UserLatLngEntity` JSON 문자열 |

---

### 2.12 프로필 사진 변경
- **Endpoint**: `POST /servlet/users/updateProfilePicture`
- **Content-Type**: `multipart/form-data`
- **설명**: 회원의 대표 프로필 사진을 변경(업로드)합니다.
- **요청 파트 (`UidFile`)**:
  | 파트명 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 회원 ID (`user.id`) |
  | `file` | `binary (file)` | Y | 변경할 프로필 이미지 파일 |
- **응답**:
  - `200`: `ResponseBasic` (`code == 200` 성공 시 `msg`에 저장된 파일명 등 반환)

---

### 2.13 회원(명함/차량/업체) 사진 추가
- **Endpoint**: `POST /servlet/users/insertUserPhoto`
- **Content-Type**: `multipart/form-data`
- **설명**: 회원의 추가 사진(`photo` 배열 내 항목, 예: 명함 사진)을 1장씩 추가 업로드합니다.
- **요청 파트 (`UidFile`)**:
  | 파트명 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 회원 ID (`user.id`) |
  | `file` | `binary (file)` | Y | 추가할 이미지 파일 |
- **응답**:
  - `200`: `ResponseBasic`
  - `400`: 요청 파라미터 오류

---

### 2.14 회원(명함/차량/업체) 사진 삭제
- **Endpoint**: `POST /servlet/users/removeUserPhoto`
- **Content-Type**: `application/x-www-form-urlencoded`
- **설명**: 회원의 추가 사진 목록에서 특정 인덱스(`pos`)의 사진을 삭제합니다.
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 회원 ID (`user.id`) |
  | `pos` | `integer` | Y | 삭제할 사진의 위치(인덱스, 0부터 시작) |
- **응답**:
  - `200`: `ResponseBasic`
  - `404`: 필수 파라미터 누락

---

## 3. 게시판 / 견적 / 산악 API (`board`)

### 3.1 게시글 목록 조회
- **Endpoint**: `POST /servlet/board/getList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID (`BoardIDs`) |

### 3.2 게시글 목록 조회 (페이징)
- **Endpoint**: `POST /servlet/board/getListPaging`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID |
  | `pid` | `integer` | N | 페이지 기준 게시글 ID |

### 3.3 내가 작성한 게시글 목록 조회
- **Endpoint**: `POST /servlet/board/getMyList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 회원 ID |

### 3.4 게시글 상세 조회
- **Endpoint**: `POST /servlet/board/getDetail`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID |
  | `id` | `integer (int64)` | Y | 게시글 ID |

### 3.5 게시글 작성 (이미지 첨부)
- **Endpoint**: `POST /servlet/board/Upload`
- **Content-Type**: `multipart/form-data`
- **요청 파트**:
  | 파트명 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 작성자 회원 ID |
  | `bid` | `integer` | Y | 게시판 ID |
  | `contents` | `string` | Y | 게시글 내용 |
  | `files` (`file0` 등) | `binary[]` | N | 첨부 이미지 파일 (필드명 무관) |

### 3.6 버스 견적 목록 조회
- **Endpoint**: `POST /servlet/board/getBusEstimateList`
- **Content-Type**: 파라미터 없음 (`POST`)

### 3.7 버스 견적 등록
- **Endpoint**: `POST /servlet/board/UploadBusEstimate`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `estimate` | `string` | Y | 견적 엔티티 JSON 문자열 |

### 3.8 산악 게시글 목록 조회
- **Endpoint**: `POST /servlet/board/getMountainList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `listType` | `integer` | Y | 목록 유형 |

### 3.9 산악 게시글 등록
- **Endpoint**: `POST /servlet/board/UploadMountain`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `mountain` | `string` | Y | 산악 엔티티 JSON 문자열 |

### 3.10 산악 게시글 상태 변경
- **Endpoint**: `POST /servlet/board/updateMountainState`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `uid` | `integer` | Y | 회원 ID |
  | `id` | `integer (int64)` | Y | 게시글 ID |
  | `state` | `boolean` | Y | 변경할 상태값 |

---

## 4. 댓글 API (`reply`)

### 4.1 댓글 목록 조회
- **Endpoint**: `POST /servlet/reply/getList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID |
  | `id` | `integer (int64)` | Y | 게시글 ID |

### 4.2 댓글 등록
- **Endpoint**: `POST /servlet/reply/insert`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID |
  | `uid` | `integer` | Y | 작성자 회원 ID |
  | `idx` | `integer (int64)` | Y | 게시글 ID |
  | `text` | `string` | Y | 댓글 내용 |

---

## 5. 공지사항 API (`notice`)

### 5.1 공지사항 목록 조회
- **Endpoint**: `POST /servlet/notice/getList`
- **Content-Type**: `application/x-www-form-urlencoded`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID |

---

## 6. 관리자 API (`admin`)

> 일부 관리자 API는 관리자 로그인 세션 쿠키(`JSESSIONID`)가 필요합니다.

| Endpoint | Method | Content-Type | 필수 파라미터 | 설명 |
| :--- | :---: | :--- | :--- | :--- |
| `/servlet/admin/createNotice` | `POST` | `x-www-form-urlencoded` | `bid`, `title`, `contents` | 공지사항 등록 |
| `/servlet/admin/editNotice` | `POST` | `x-www-form-urlencoded` | `id`, `title`, `contents` | 공지사항 수정 |
| `/servlet/admin/eraseNotice` | `POST` | `x-www-form-urlencoded` | `id` | 공지사항 삭제 |
| `/servlet/admin/UploadAdv` | `POST` | `multipart/form-data` | `id`, `uid`, `file` (선택) | 광고 등록 (이미지 업로드) |
| `/servlet/admin/getAdvList` | `POST` | `x-www-form-urlencoded` | `bid` | 광고 목록 조회 |
| `/servlet/Admin/EraseAdv` | `POST` | `x-www-form-urlencoded` | `id` (관리자 세션 필요) | 광고 삭제 |
| `/Admin/PermitUser` | `POST` | `x-www-form-urlencoded` | `uid` | 회원 가입 승인 |
| `/Admin/EditUser` | `POST` | `x-www-form-urlencoded` | `uid` (`name`, `phone`, `company_name`, `forbidden`) | 회원 정보 수정 (관리자) |
| `/Admin/EraseUser` | `POST` | `x-www-form-urlencoded` | `uid` (관리자 세션 필요) | 회원 삭제 |
| `/Admin/EraseBoard` | `POST` | `x-www-form-urlencoded` | `bid`, `id` (관리자 세션 필요) | 게시글 삭제 |
