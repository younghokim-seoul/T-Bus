# 게시판(`BoardListView`) 구조 및 페이징 처리 분석 문서

> 본 문서는 T-Bus 프로젝트의 메인/정보 게시판 탭 전환 동작 방식, `BoardListView`의 데이터 로딩 구조, 레거시 문제점 및 페이징(`Paging`) 도입을 위한 기술 분석 내용을 정리한 문서입니다.

---

## 1. 현재 게시판 탭 전환 및 `BoardListView` 동작 구조

### 1.1 탭 버튼 클릭 시 동작 흐름 (`MainActivity`)
- **관련 코드**: [`MainActivity.java`](app/src/main/java/com/windnsoft/tbus/MainActivity.java) (`onClick()`, `intentBoard()`, `loadList()`)
- **동작 방식**:
  1. 상단 탭 버튼(`boardFreeTab`, `boardBusTab`, `boardEstimateTab`, `boardTractionTab`, `boardMyTab`) 클릭 시 [`intentBoard(int bid)`](app/src/main/java/com/windnsoft/tbus/MainActivity.java#L249-L254)가 호출됩니다.
  2. `intentBoard(bid)`는 하나의 `BoardListView` 인스턴스만 갱신하는 것이 아니라, **`Intent`에 `bid`(게시판 ID)를 담아 `startActivity(intent)`로 `MainActivity`를 새로 실행하고 기존 `MainActivity`를 `finish()`로 종료**합니다.
  3. 새로 생성된 `MainActivity`의 `onCreate()`에서 `bid` 값에 따라 레이아웃을 세팅합니다.
     - `BoardIDs.Estimate` (견적 게시판): [`bus_estimate_activity.xml`](app/src/main/res/layout/bus_estimate_activity.xml) (`BusEstimateListView` 사용)
     - 그 외 탭 (`Free`, `Bus`, `Traction`, `My`): [`board_activity.xml`](app/src/main/res/layout/board_activity.xml) (`BoardListView` 사용)

### 1.2 `BoardListView` 공유 범위
[`BoardListView`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java)는 `RecyclerView`를 상속받은 커스텀 뷰로, 아래 화면들에서 공통으로 사용됩니다.

| 액티비티 | 레이아웃 | 대상 게시판 (`BoardIDs`) |
| :--- | :--- | :--- |
| [`MainActivity`](app/src/main/java/com/windnsoft/tbus/MainActivity.java) | [`board_activity.xml`](app/src/main/res/layout/board_activity.xml) | `Free`(자유), `Bus`(관광버스), `Traction`(견인), `My`(내글) |
| [`InfoListActivity`](app/src/main/java/com/windnsoft/tbus/InfoListActivity.java) | [`info_list_activity.xml`](app/src/main/res/layout/info_list_activity.xml) | `Deals`(매매), `Dispense`(배차), `Interdiction`(단속), `Festival`(축제), `News`(뉴스), `Traction`(견인), `Traffic`(교통) |

### 1.3 탭 클릭 시마다 API 재호출 여부
- **결론**: **탭 버튼을 누를 때마다 매번 서버 API를 새로 호출합니다.**
- **호출 상세 ([`BoardListView.loadList()`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L73-L136))**:
  - **자유게시판 (`BoardIDs.Free`)**:
    1. 공지사항 목록 조회: `POST /servlet/notice/getList` (`bid`)
    2. 게시글 전체 목록 조회: `POST /servlet/board/getList` (`bid`)
  - **내글보기 (`BoardIDs.My`)**:
    - 내가 쓴 글 전체 목록 조회: `POST /servlet/board/getMyList` (`uid`)
  - **기타 모든 일반/정보 게시판 (`Bus`, `Traction`, `Deals` 등)**:
    - 게시글 전체 목록 조회: `POST /servlet/board/getList` (`bid`)
- 또한 액티비티가 매번 재생성되므로 상단 광고 이미지(`loadAd()`)와 업데이트 체크(`AppUpdateDialogFragment.checkAndShowIfNeeded()`)도 탭 전환 시마다 반복 호출됩니다.

---

## 2. 현행 레거시 코드의 주요 문제점

1. **탭 전환 시 액티비티 재시작 (`startActivity` + `finish`)**
   - 단순 탭 전환임에도 액티비티 생명주기를 처음부터 다시 타면서 화면 깜빡임이 발생하고 불필요한 리소스(광고 이미지 재로딩 등)가 소모됩니다.
2. **페이징 미적용 (전체 데이터 일괄 조회)**
   - `/servlet/board/getList`를 통해 해당 게시판의 모든 게시글을 한 번에 받아와 `adapter.setList(list)`로 덮어씌웁니다.
   - 게시글 수가 누적될수록 네트워크 응답 지연, JSON 파싱 부하, 메모리 사용량 증가 문제가 발생합니다.
3. **뷰(`BoardListView`) 내부에 네트워크/데이터 파싱 로직 강결합**
   - 커스텀 `RecyclerView` 클래스 내부에서 `HttpRequestAsync` 호출, `Gson` 파싱, 날짜 포맷 변환(`WsUtils.formatBoardDate`), 정렬(`sortListIfNeeded`)까지 직접 수행하고 있습니다.
4. **공지사항(`Notice`) 비동기 경쟁 상태 (Race Condition)**
   - `BoardIDs.Free`일 때 `loadNotice()`와 `loadList()`를 동시에 비동기로 호출하지만, `loadNotice()` 완료 시점에 `adapter.notifyDataSetChanged()`를 호출하지 않아 게시글 목록보다 공지사항 응답이 늦게 오면 상단 공지 헤더가 화면에 표시되지 않을 수 있습니다.
5. **싱글턴 `HttpRequestParams` 사용 주의**
   - `HttpRequestParams.getInstance()`가 싱글턴 인스턴스를 초기화하여 반환하는 구조이므로 연속/동시 요청 시 파라미터 간섭 가능성에 유의해야 합니다.

---

## 3. 페이징(`Paging`) 처리 도입 가이드

### 3.1 서버 API 확인 ([`API_SPEC.md`](API_SPEC.md#32-게시글-목록-조회-페이징))
백엔드(`TBusWebapp2`)에는 이미 페이지 번호(`pid = 0, 1, 2, ...`) 기반 페이징 엔드포인트가 정상 동작하고 있습니다.

- **Endpoint**: `POST /servlet/board/getListPaging`
- **요청 파라미터**:
  | 파라미터 | 타입 | 필수 | 설명 |
  | :--- | :---: | :---: | :--- |
  | `bid` | `integer` | Y | 게시판 ID (`BoardIDs`) |
  | `pid` | `integer` | Y | 페이지 번호 (`0`부터 시작: `0`, `1`, `2`, `3` ... / 페이지당 **15개**씩 반환) |
- **실제 서버 응답 검증 결과 (`bid=1` 기준)**:
  - 기존 `/servlet/board/getList` (`bid=1`): 최신 글 **50개**만 고정 반환 (`id: 60014 ~ 59964`) 후 끝남
  - `/servlet/board/getListPaging` (`bid=1&pid=0`): 1페이지 **15개** 반환 (`id: 60014 ~ 60000`)
  - `/servlet/board/getListPaging` (`bid=1&pid=1`): 2페이지 **15개** 반환 (`id: 59999 ~ 59984`)
  - `/servlet/board/getListPaging` (`bid=1&pid=2`): 3페이지 **15개** 반환 (`id: 59983 ~ 59969`)
- **현재 안드로이드 앱에서 누적이 안 되는 이유**:
  1. [`GlobalVars.java`](app/src/main/java/com/windnsoft/tbus/Common/GlobalVars.java#L38) 및 [`BoardListView.java`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L131-L133)에서 `/servlet/board/getListPaging`이 아닌 **구버전 `/servlet/board/getList`를 호출**하고 있으며, `pid` 파라미터를 아예 보내지 않고 있습니다.
  2. [`BoardListView.java`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L175-L185)에 스크롤 하단 감지(`OnScrollListener`) 로직이 없고, [`adapter.setList(list)`](app/src/main/java/com/windnsoft/tbus/Fragment/BoardListView.java#L232-L235)에서 기존 리스트에 누적(`addAll`)하는 대신 `items = list`로 덮어쓰기만 하고 있습니다.

### 3.2 `BoardListView` 페이징 구현 방향
`BoardListView` 한 곳만 개선하면 `MainActivity`와 `InfoListActivity`의 모든 게시판에 공통 적용됩니다.

1. **페이징 상태 변수 추가 (`BoardListView`)**:
   - `int currentPage = 0`: 현재 페이지 번호 (`pid = 0, 1, 2...`)
   - `boolean isLoading = false`: 중복 API 호출 방지 플래그
   - `boolean isLastPage = false`: 응답 개수가 15개 미만(`entityList.size() < 15`)일 때 `true` 처리
2. **초기 로드(`loadList()`)와 추가 로드(`loadNextPage()`) 구현**:
   - 초기 로드(`loadList()`): `currentPage = 0`, `isLastPage = false`로 초기화 후 `pid=0` 호출하여 `adapter.setList(entityList)` 세팅
   - 추가 로드(`loadNextPage()`): `pid = currentPage + 1`로 호출하여 응답받은 리스트를 기존 `adapter.items`에 `addAll()`로 누적 및 `notifyDataSetChanged()`(또는 `notifyItemRangeInserted()`) 호출
3. **`RecyclerView.OnScrollListener` 등록**:
   - `LinearLayoutManager.findLastVisibleItemPosition()`을 확인하여 리스트 하단 근처에 도달했을 때 `!isLoading && !isLastPage && bid != BoardIDs.My`이면 다음 페이지(`pid + 1`) 호출
4. **(선택) 탭 전환 개선 (`MainActivity`)**:
   - 탭 클릭 시 `startActivity()` + `finish()` 대신 현재 액티비티에서 탭 UI 상태와 `BoardID`만 변경하고 `boardListView.setBoardId(bid)` 후 `boardListView.loadList()`를 호출하도록 변경하면 화면 깜빡임과 불필요한 재로딩을 제거할 수 있습니다. (단, `Estimate` 탭은 레이아웃이 다르므로 기존 분기 유지 또는 View 전환 처리)
