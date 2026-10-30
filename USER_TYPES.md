# T-Bus 회원 타입(`UserTypes`) 및 사용자 정보 조회 가이드

> 본 문서는 T-Bus Android 앱에서 현재 로그인한 사용자의 **회원 타입(일반 / 관광버스 / 업체)**을 판별하는 방법과, 각 회원 타입별 데이터 구조([`UserEntity`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java)) 및 관련 API 호출 방식을 정리한 문서입니다.

---

## 1. 회원 타입 상수 정의 ([`UserTypes.java`](app/src/main/java/com/windnsoft/tbus/Entities/UserTypes.java))

| 상수명 | 값 (`int`) | 설명 | 가입 화면 Fragment |
| :--- | :---: | :--- | :--- |
| `UserTypes.Normal` | `1` | 일반회원 | [`RegisterNormalFragment`](app/src/main/java/com/windnsoft/tbus/Fragment/RegisterNormalFragment.java) |
| `UserTypes.Bus` | `2` | 관광버스 회원 | [`RegisterBusFragment`](app/src/main/java/com/windnsoft/tbus/Fragment/RegisterBusFragment.java) |
| `UserTypes.Co` | `3` | 업체회원 | [`RegisterCoFragment`](app/src/main/java/com/windnsoft/tbus/Fragment/RegisterCoFragment.java) |

---

## 2. 현재 로그인한 회원의 타입 판별 코드

앱 시작 시 [`IntroActivity.login()`](app/src/main/java/com/windnsoft/tbus/IntroActivity.java#L105-L140) 또는 회원가입 완료 시 [`SharedPreferencesCache.setCurrentUser(resultUser)`](app/src/main/java/com/windnsoft/tbus/Common/SharedPreferencesCache.java#L129-L145)를 통해 로컬 `SharedPreferences`(`"UserInfo"`)에 현재 사용자의 기본 정보(`id`, `tel`, `name`, `type`)가 저장됩니다.

### Java 예시
```java
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserTypes;

int userType = SharedPreferencesCache.getCurrentUser().type;

if (userType == UserTypes.Bus) {
    // 관광버스 회원 (2)
} else if (userType == UserTypes.Normal) {
    // 일반회원 (1)
} else if (userType == UserTypes.Co) {
    // 업체회원 (3)
}
```

### Kotlin 예시
```kotlin
import com.windnsoft.tbus.Common.SharedPreferencesCache
import com.windnsoft.tbus.Entities.UserTypes

when (val userType = SharedPreferencesCache.getCurrentUser().type) {
    UserTypes.Bus -> {
        // 관광버스 회원 (2)
    }
    UserTypes.Normal -> {
        // 일반회원 (1)
    }
    UserTypes.Co -> {
        // 업체회원 (3)
    }
}
```

---

## 3. 로컬 캐시 vs 서버 상세 정보 조회

### 3.1 로컬 캐시 (`SharedPreferencesCache.getCurrentUser()`)
[`SharedPreferencesCache.getCurrentUser()`](app/src/main/java/com/windnsoft/tbus/Common/SharedPreferencesCache.java#L113-L127)가 반환하는 `UserEntity` 객체에는 다음 **4가지 기본 필드만** 채워져 있습니다:
- `user.id`: 회원 고유 번호 (`KeyUserIndex`, 기본값 `0`)
- `user.tel`: 전화번호 (`KeyUserPhone`, 없을 시 기기 전화번호)
- `user.name`: 이름 / 닉네임 (`KeyUserName`)
- `user.type`: 회원 타입 (`KeyUserType`, 기본값 `UserTypes.Normal` = `1`)

### 3.2 서버 상세 프로필 조회 (`UsersOperator.getUserInfo()`)
차량번호, 차고지, 업체 정보, 프로필 사진 등 전체 프로필 데이터가 필요한 경우(예: 프로필 수정 팝업 초기 데이터 로드), `user.id`를 이용해 서버에서 상세 정보를 조회합니다.

```java
long uid = SharedPreferencesCache.getCurrentUser().id;

UsersOperator.getUserInfo(uid, new HttpRequestAsync.OnCompleteListener() {
    @Override
    public void onSuccess(String result) {
        ResponseMessageEntity res = new Gson().fromJson(result, ResponseMessageEntity.class);
        if (res != null && res.code == ResponseCodes.CodeOk) {
            UserEntity detailUser = new Gson().fromJson(res.msg, UserEntity.class);
            // detailUser.type 및 회원 타입별 상세 필드 사용
        }
    }

    @Override
    public void onFail(String result) { }

    @Override
    public void onError(int code, String result) { }
});
```

---

## 4. 회원 타입별 주요 사용 필드 ([`UserEntity.java`](app/src/main/java/com/windnsoft/tbus/Entities/UserEntity.java))

### 4.1 일반회원 (`UserTypes.Normal = 1`)
- **가입 API**: `POST /servlet/users/join` ([`UsersOperator.register()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L24-L76))
- **사용 필드**:
  - `type`: `1`
  - `tel`: 전화번호
  - `name`: 이름(닉네임)

### 4.2 관광버스 회원 (`UserTypes.Bus = 2`)
- **가입 API**: `POST /servlet/users/joinBus` ([`UsersOperator.registerBus()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L88-L128))
- **사용 필드**:
  - `type`: `2`
  - `name`: 기사 성함
  - `tel`: 전화번호
  - `garage`: 차고지
  - `ride`: 차종 (보유차량)
  - `company_name`: 소속 회사명
  - `passengers`: 탑승 인원 (`Integer`)
  - `possible_date`: 운행 가능일
  - `car_number`: 차량 번호
  - `profile`: 프로필 사진 파일명 (`multipart` 파트명 `"profile"`)
  - `photoList`: 차량 사진 목록 (`multipart` 파트명 `"file0"`, `"file1"`, ... 최대 4장)

### 4.3 업체회원 (`UserTypes.Co = 3`)
- **가입 API**: `POST /servlet/users/joinCo` ([`UsersOperator.registerCo()`](app/src/main/java/com/windnsoft/tbus/Operator/UsersOperator.java#L130-L146))
- **사용 필드**:
  - `type`: `3`
  - `company_name`: 상호명
  - `product`: 주요 취급품목
  - `sectors`: 업종
  - `locale`: 지역 (`"시/도 구/군"`)
  - `addr`: 상세 주소
  - `manager_name`: 담당자 성함 (`user.name`에도 동일 값 설정)
  - `manager_tel`: 담당자 연락처
  - `contact`: 대표 연락처
  - `usage`: 용도
  - `menu`: 주요 메뉴
  - `capacity`: 수용 인원 (`Integer`)
  - `time`: 영업시간
  - `closed`: 휴무일
  - `parking`: 주차 가능 대수
  - `booking`: 예약 가능 여부 (`Boolean`)
  - `detail`: 상세 설명
  - `profile`: 대표 프로필 이미지 (`multipart` 파트명 `"profile"`)
  - `photoList`: 업체 추가 사진 목록 (`multipart` 파트명 `"file0"`, `"file1"`, ... 최대 4장)
