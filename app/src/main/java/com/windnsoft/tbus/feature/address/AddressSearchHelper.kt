package com.windnsoft.tbus.feature.address

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.windnsoft.tbus.BuildConfig
import com.windnsoft.tbus.Http.ResponseCodes
import com.windnsoft.tbus.R
import com.windnsoft.tbus.feature.member.MemberApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DaumPostcodeData(
    val zonecode: String = "",
    val address: String = "",
    val roadAddress: String = "",
    val jibunAddress: String = "",
    val autoRoadAddress: String = "",
    val autoJibunAddress: String = "",
    val sido: String = "",
    val sigungu: String = "",
    val bname: String = "",
    val buildingName: String = "",
    val userSelectedType: String = "",
) {
    val primaryRoadAddress: String
        get() = roadAddress.trim()
            .ifEmpty { autoRoadAddress.trim() }
            .ifEmpty { address.trim() }
            .ifEmpty { jibunAddress.trim() }
            .ifEmpty { autoJibunAddress.trim() }

    val fallbackJibunAddress: String
        get() = jibunAddress.trim()
            .ifEmpty { autoJibunAddress.trim() }
            .ifEmpty { address.trim() }
}

data class MappedTBusAddress(
    val locale1: String,
    val locale2: String,
    val baseAddrWithoutLocale: String,
    val displayBaseAddress: String,
    val geocodePrimaryQuery: String,
    val geocodeFallbackQuery: String,
) {
    val locale: String
        get() = listOf(locale1.trim(), locale2.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    fun buildFinalAddr(detailAddress: String): String {
        val cleanBase = baseAddrWithoutLocale.trim().ifEmpty { displayBaseAddress.trim() }
        val cleanDetail = detailAddress.trim()
        return if (cleanDetail.isEmpty()) {
            cleanBase
        } else {
            "$cleanBase $cleanDetail".trim()
        }
    }
}

data class ResolvedLatLng(
    val lat: Double,
    val lng: Double,
    val source: String,
)

data class UserLatLngPayload(
    val id: Int,
    val co: String,
    val lat: Double,
    val lng: Double,
    val addr: String,
)

object AddressSearchHelper {

    interface CoordinateCallback {
        fun onResult(result: ResolvedLatLng?)
    }

    interface UpdateLatLngCallback {
        fun onComplete(latLngSynced: Boolean)
    }

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val kakaoHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 다음(카카오) 우편번호 검색 결과를 기존 T-Bus 서버/앱(`array.xml`) 규격의 `locale` 및 `addr`로 변환합니다.
     */
    @JvmStatic
    fun mapToTBusAddress(context: Context, data: DaumPostcodeData): MappedTBusAddress {
        val locale1Options = context.resources.getStringArray(R.array.locale).toList()
        val rawSido = data.sido.trim()
        val rawSigungu = data.sigungu.trim()

        // 1. 특수 행정구역 개편 매핑 (세종특별자치시 / 대구 편입 군위군)
        val (mappedLocale1, preMappedLocale2) = when {
            rawSido.contains("세종") -> "충청남도" to "연기군"
            rawSido.contains("대구") && rawSigungu.startsWith("군위군") -> "경상북도" to "군위군"
            else -> normalizeSidoToTBusLocale1(rawSido, locale1Options) to null
        }

        val subLocaleOptions = getSubLocaleOptions(context, locale1Options, mappedLocale1)
        val mappedLocale2 = preMappedLocale2 ?: normalizeSigunguToTBusLocale2(
            locale1 = mappedLocale1,
            rawSigungu = rawSigungu,
            subOptions = subLocaleOptions,
        )

        val roadOrMain = data.primaryRoadAddress
        val bldg = data.buildingName.trim()
        val displayWithBuilding = if (bldg.isNotEmpty() && !roadOrMain.contains(bldg)) {
            "$roadOrMain ($bldg)"
        } else {
            roadOrMain
        }

        val strippedBase = if (rawSido.contains("세종")) {
            // 충청남도 연기군으로 매핑되더라도 실제 주소 표기는 세종특별자치시 도로명이 보이도록 유지
            displayWithBuilding
        } else {
            stripLeadingSidoSigungu(
                fullAddress = displayWithBuilding,
                rawSido = rawSido,
                mappedLocale1 = mappedLocale1,
                rawSigungu = rawSigungu,
                mappedLocale2 = mappedLocale2,
            )
        }

        return MappedTBusAddress(
            locale1 = mappedLocale1,
            locale2 = mappedLocale2,
            baseAddrWithoutLocale = strippedBase.ifEmpty { displayWithBuilding },
            displayBaseAddress = displayWithBuilding,
            geocodePrimaryQuery = roadOrMain,
            geocodeFallbackQuery = data.fallbackJibunAddress,
        )
    }

    /**
     * `locale`과 `addr`을 합칠 때 앞부분 시/도·시/군/구가 중복되지 않도록 조합합니다.
     */
    @JvmStatic
    fun formatCompanyFullAddress(locale: String?, addr: String?): String {
        val cleanLocale = locale?.trim().orEmpty()
        val cleanAddr = addr?.trim().orEmpty()
        if (cleanLocale.isEmpty()) return cleanAddr
        if (cleanAddr.isEmpty()) return cleanLocale

        if (cleanAddr.startsWith(cleanLocale)) {
            return cleanAddr
        }
        val localeParts = cleanLocale.split(Regex("\\s+")).filter { it.isNotBlank() }
        val l1 = localeParts.getOrNull(0).orEmpty()
        val l2 = localeParts.getOrNull(1).orEmpty()
        val shortL1 = shortenSido(l1)
        if (shortL1.isNotEmpty() && l2.isNotEmpty()) {
            val shortPrefix = "$shortL1 $l2"
            val compactPrefix = "$shortL1$l2"
            if (cleanAddr.startsWith(shortPrefix)) {
                return "$cleanLocale ${cleanAddr.removePrefix(shortPrefix).trim()}".trim()
            }
            if (cleanAddr.startsWith(compactPrefix)) {
                return "$cleanLocale ${cleanAddr.removePrefix(compactPrefix).trim()}".trim()
            }
        }
        return "$cleanLocale $cleanAddr".trim()
    }

    /**
     * 1순위: 카카오 로컬 REST API (`dapi.kakao.com/v2/local/search/address.json`)
     * 2순위(폴백): Android 내장 `Geocoder` (도로명 -> 지번 순)
     */
    suspend fun resolveCoordinates(
        context: Context,
        primaryAddress: String,
        fallbackAddress: String = "",
    ): ResolvedLatLng? = withContext(Dispatchers.IO) {
        val cleanPrimary = primaryAddress.trim()
        val cleanFallback = fallbackAddress.trim()
        if (cleanPrimary.isEmpty() && cleanFallback.isEmpty()) return@withContext null

        val queries = listOf(cleanPrimary, cleanFallback)
            .filter { it.isNotEmpty() }
            .distinct()

        // 1. 카카오 로컬 주소 검색 REST API 시도
        val kakaoKey = BuildConfig.KAKAO_REST_API_KEY.trim()
        if (kakaoKey.isNotEmpty()) {
            for (query in queries) {
                queryKakaoAddressApi(kakaoKey, query)?.let { return@withContext it }
            }
        }

        // 2. Android 내장 Geocoder 폴백 (도로명 -> 지번)
        val appContext = context.applicationContext ?: context
        for (query in queries) {
            queryAndroidGeocoder(appContext, query)?.let { return@withContext it }
        }

        null
    }

    /**
     * Java (`RegisterCoFragment`)에서 비동기로 좌표를 선조회할 때 사용하는 헬퍼.
     */
    @JvmStatic
    fun resolveCoordinatesAsync(
        context: Context,
        primaryAddress: String,
        fallbackAddress: String,
        callback: CoordinateCallback,
    ) {
        val appContext = context.applicationContext ?: context
        backgroundScope.launch {
            val resolved = resolveCoordinates(appContext, primaryAddress, fallbackAddress)
            withContext(Dispatchers.Main) {
                callback.onResult(resolved)
            }
        }
    }

    /**
     * `/servlet/users/updateLatLng` 호출 (실패 시 1회 자동 재시도 포함).
     */
    suspend fun updateCompanyLatLngWithRetry(
        uid: Int,
        companyName: String,
        lat: Double,
        lng: Double,
        fullAddress: String,
    ): Boolean = withContext(Dispatchers.IO) {
        if (uid <= 0 || !isValidCoordinate(lat, lng)) return@withContext false

        val payload = UserLatLngPayload(
            id = uid,
            co = companyName.trim(),
            lat = lat,
            lng = lng,
            addr = fullAddress.trim(),
        )
        val userJson = gson.toJson(payload)

        repeat(2) { attempt ->
            val ok = runCatching {
                val res = MemberApiService.instance.updateLatLng(userJson = userJson)
                res.code == ResponseCodes.CodeOk
            }.onFailure { e ->
                Timber.w(e, "updateLatLng attempt ${attempt + 1} failed for uid=$uid")
            }.getOrDefault(false)

            if (ok) {
                Timber.d("updateLatLng succeeded on attempt ${attempt + 1} for uid=$uid ($lat, $lng)")
                return@withContext true
            }
            if (attempt == 0) {
                delay(400L)
            }
        }
        false
    }

    /**
     * Java (`RegisterCoFragment`)에서 가입 완료(`joinCo`) 직후 좌표 조회(미완료 시 폴백 조회) 및 `updateLatLng` 전송을 수행합니다.
     */
    @JvmStatic
    fun resolveAndUploadLatLngFromJava(
        context: Context,
        uid: Int,
        companyName: String,
        fullAddress: String,
        primaryQuery: String,
        fallbackQuery: String,
        preResolved: ResolvedLatLng?,
        callback: UpdateLatLngCallback,
    ) {
        val appContext = context.applicationContext ?: context
        backgroundScope.launch {
            val coords = preResolved ?: resolveCoordinates(appContext, primaryQuery, fallbackQuery)
            val synced = if (coords != null) {
                updateCompanyLatLngWithRetry(
                    uid = uid,
                    companyName = companyName,
                    lat = coords.lat,
                    lng = coords.lng,
                    fullAddress = fullAddress,
                )
            } else {
                Timber.w("Could not resolve coordinates for uid=$uid, query=$primaryQuery")
                false
            }
            withContext(Dispatchers.Main) {
                callback.onComplete(synced)
            }
        }
    }

    private fun queryKakaoAddressApi(apiKey: String, query: String): ResolvedLatLng? {
        return runCatching {
            val url = "https://dapi.kakao.com/v2/local/search/address.json"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("query", query)
                ?.build()
                ?: return null

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "KakaoAK $apiKey")
                .get()
                .build()

            kakaoHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Timber.w("Kakao address API returned HTTP ${response.code}: $bodyStr")
                    return null
                }
                val root = JsonParser.parseString(bodyStr).asJsonObject
                val documents = root.getAsJsonArray("documents") ?: return null
                if (documents.size() == 0) return null

                val first = documents[0].asJsonObject
                val lng = first.get("x")?.asString?.toDoubleOrNull() ?: return null
                val lat = first.get("y")?.asString?.toDoubleOrNull() ?: return null
                if (!isValidCoordinate(lat, lng)) return null

                ResolvedLatLng(lat = lat, lng = lng, source = "kakao")
            }
        }.onFailure { e ->
            Timber.w(e, "Kakao address API error for query=$query")
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun queryAndroidGeocoder(context: Context, query: String): ResolvedLatLng? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            val geocoder = Geocoder(context, Locale.KOREA)
            val results = geocoder.getFromLocationName(query, 1)
            val first = results?.firstOrNull() ?: return null
            val lat = first.latitude
            val lng = first.longitude
            if (!isValidCoordinate(lat, lng)) return null
            ResolvedLatLng(lat = lat, lng = lng, source = "geocoder")
        }.onFailure { e ->
            Timber.w(e, "Android Geocoder error for query=$query")
        }.getOrNull()
    }

    private fun isValidCoordinate(lat: Double, lng: Double): Boolean {
        return !lat.isNaN() &&
            !lng.isNaN() &&
            lat in -90.0..90.0 &&
            lng in -180.0..180.0 &&
            (lat != 0.0 || lng != 0.0)
    }

    private fun normalizeSidoToTBusLocale1(rawSido: String, locale1Options: List<String>): String {
        val mapped = when {
            rawSido.startsWith("서울") -> "서울특별시"
            rawSido.startsWith("부산") -> "부산광역시"
            rawSido.startsWith("대구") -> "대구광역시"
            rawSido.startsWith("인천") -> "인천광역시"
            rawSido.startsWith("광주") -> "광주광역시"
            rawSido.startsWith("대전") -> "대전광역시"
            rawSido.startsWith("울산") -> "울산광역시"
            rawSido.startsWith("경기") -> "경기도"
            rawSido.startsWith("강원") -> "강원도"
            rawSido.startsWith("충북") || rawSido.startsWith("충청북") -> "충청북도"
            rawSido.startsWith("충남") || rawSido.startsWith("충청남") -> "충청남도"
            rawSido.startsWith("전북") || rawSido.startsWith("전라북") -> "전라북도"
            rawSido.startsWith("전남") || rawSido.startsWith("전라남") -> "전라남도"
            rawSido.startsWith("경북") || rawSido.startsWith("경상북") -> "경상북도"
            rawSido.startsWith("경남") || rawSido.startsWith("경상남") -> "경상남도"
            rawSido.startsWith("제주") -> "제주특별자치도"
            else -> rawSido
        }
        return mapped.takeIf { it in locale1Options } ?: locale1Options.firstOrNull().orEmpty()
    }

    private fun normalizeSigunguToTBusLocale2(
        locale1: String,
        rawSigungu: String,
        subOptions: List<String>,
    ): String {
        if (subOptions.isEmpty()) return rawSigungu
        val firstToken = rawSigungu.split(Regex("\\s+")).firstOrNull()?.trim().orEmpty()
        if (firstToken.isEmpty()) return subOptions.first()

        // 시·군·구 명칭 변경/승격/분구 보정 (array.xml 호환)
        val adjusted = when {
            locale1 == "인천광역시" && firstToken == "미추홀구" -> "남구"
            locale1 == "인천광역시" && (firstToken == "영종구" || firstToken == "제물포구") -> "중구"
            locale1 == "인천광역시" && (firstToken == "서해구" || firstToken == "검단구") -> "서구"
            locale1 == "경기도" && firstToken == "여주시" -> "여주군"
            locale1 == "충청남도" && firstToken == "당진시" -> "당진군"
            else -> firstToken
        }

        if (adjusted in subOptions) return adjusted

        // 시/군 접미어 차이 보정 (예: xxx시 <-> xxx군)
        val stem = adjusted.removeSuffix("시").removeSuffix("군").removeSuffix("구")
        if (stem.length >= 2) {
            subOptions.firstOrNull { it.startsWith(stem) }?.let { return it }
        }

        return subOptions.first()
    }

    private fun getSubLocaleOptions(
        context: Context,
        locale1Options: List<String>,
        selectedLocale1: String,
    ): List<String> {
        val arrayResId = when (locale1Options.indexOf(selectedLocale1)) {
            0 -> R.array.localeSeoul
            1 -> R.array.localeBusan
            2 -> R.array.localeDaegu
            3 -> R.array.localeIncheon
            4 -> R.array.localeGwangju
            5 -> R.array.localeDaejeon
            6 -> R.array.localeUlsan
            7 -> R.array.localeGyeonggi
            8 -> R.array.localeGangwon
            9 -> R.array.localeChungcheongbuk
            10 -> R.array.localeChungcheongnam
            11 -> R.array.localeJeollabuk
            12 -> R.array.localeJeollanam
            13 -> R.array.localeGyeongsangbuk
            14 -> R.array.localeGyeongsangnam
            15 -> R.array.localeJeju
            else -> R.array.localeSeoul
        }
        return context.resources.getStringArray(arrayResId).filter { !it.contains("전체") }
    }

    private fun stripLeadingSidoSigungu(
        fullAddress: String,
        rawSido: String,
        mappedLocale1: String,
        rawSigungu: String,
        mappedLocale2: String,
    ): String {
        val tokens = fullAddress.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.size <= 2) return fullAddress.trim()

        var dropCount = 0
        val firstToken = tokens[0]
        val sidoCandidates = setOf(
            rawSido,
            mappedLocale1,
            shortenSido(mappedLocale1),
        ).filter { it.isNotEmpty() }

        if (sidoCandidates.any { firstToken == it }) {
            dropCount = 1
            val secondToken = tokens.getOrNull(1).orEmpty()
            val rawFirstSigungu = rawSigungu.split(Regex("\\s+")).firstOrNull().orEmpty()
            // 예: "경기 수원시 팔달구 효원로 1" -> "경기", "수원시" 까지만 제거하고 "팔달구 효원로 1" 유지
            if (secondToken.isNotEmpty() &&
                (secondToken == mappedLocale2 || secondToken == rawFirstSigungu)
            ) {
                dropCount = 2
            }
        }

        return tokens.drop(dropCount).joinToString(" ").trim()
    }

    private fun shortenSido(locale1: String): String {
        return when (locale1) {
            "서울특별시" -> "서울"
            "부산광역시" -> "부산"
            "대구광역시" -> "대구"
            "인천광역시" -> "인천"
            "광주광역시" -> "광주"
            "대전광역시" -> "대전"
            "울산광역시" -> "울산"
            "경기도" -> "경기"
            "강원도" -> "강원"
            "충청북도" -> "충북"
            "충청남도" -> "충남"
            "전라북도" -> "전북"
            "전라남도" -> "전남"
            "경상북도" -> "경북"
            "경상남도" -> "경남"
            "제주특별자치도" -> "제주"
            else -> ""
        }
    }
}
