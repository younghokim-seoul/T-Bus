package com.windnsoft.tbus.feature

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.General.WsUtils
import com.windnsoft.tbus.Http.ResponseCodes
import com.windnsoft.tbus.Http.ResponseMessageEntity
import com.windnsoft.tbus.R
import com.windnsoft.tbus.designsystem.component.TBusButton
import com.windnsoft.tbus.designsystem.component.TBusButtonVariant
import com.windnsoft.tbus.designsystem.component.TBusHorizontalDivider
import com.windnsoft.tbus.designsystem.component.TBusIconButton
import com.windnsoft.tbus.designsystem.component.TBusScaffold
import com.windnsoft.tbus.designsystem.component.TBusSurface
import com.windnsoft.tbus.designsystem.component.TBusText
import com.windnsoft.tbus.designsystem.component.TBusTextVariant
import com.windnsoft.tbus.designsystem.component.TBusTopAppBar
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import com.windnsoft.tbus.feature.member.MemberApiService
import com.windnsoft.tbus.feature.member.MemberPhotoViewerActivity
import com.windnsoft.tbus.utils.EdgeToEdgeConfig
import com.windnsoft.tbus.utils.EdgeToEdgeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.ceil
import kotlin.math.max

data class CompanyLatLngMarker(
    val id: Int = 0,
    val co: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val addr: String? = null,
) {
    val displayTitle: String
        get() = co?.replace("\n", " ")?.trim()?.takeIf { it.isNotEmpty() } ?: "업소 #$id"

    val position: LatLng
        get() = LatLng(lat, lng)
}

private object CompanyMarkerIconHelper {
    fun getMarkerIcon(
        context: Context,
        title: String,
        isSelected: Boolean,
        cache: MutableMap<String, BitmapDescriptor>,
    ): BitmapDescriptor {
        val cleanTitle = title.replace(Regex("\\s+"), " ").trim()
        val maxLen = if (isSelected) 15 else 10
        val label = if (cleanTitle.length > maxLen) {
            cleanTitle.take(maxLen - 1) + "…"
        } else {
            cleanTitle
        }
        val cacheKey = "${label}_${isSelected}"
        return cache.getOrPut(cacheKey) {
            createMarkerBitmapDescriptor(context, label, isSelected)
        }
    }

    private fun createMarkerBitmapDescriptor(
        context: Context,
        label: String,
        isSelected: Boolean,
    ): BitmapDescriptor {
        val density = context.resources.displayMetrics.density
        val fontSizeSp = if (isSelected) 11.5f else 10.5f
        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSizeSp,
            context.resources.displayMetrics,
        )

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) {
                android.graphics.Color.WHITE
            } else {
                android.graphics.Color.parseColor("#191F28")
            }
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textWidth = textPaint.measureText(label)
        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent

        val horizontalPadding = (if (isSelected) 8f else 6.5f) * density
        val verticalPadding = (if (isSelected) 4.5f else 3.5f) * density
        val dotRadius = (if (isSelected) 0f else 2.5f) * density
        val dotSpacing = (if (isSelected) 0f else 3.5f) * density
        val triangleWidth = (if (isSelected) 7f else 6f) * density
        val triangleHeight = (if (isSelected) 5f else 4f) * density
        val cornerRadius = (if (isSelected) 8f else 6f) * density
        val strokeWidth = (if (isSelected) 1.8f else 1.2f) * density

        val hasDot = !isSelected
        val contentWidth = (if (hasDot) dotRadius * 2 + dotSpacing else 0f) + textWidth
        val bubbleWidth = horizontalPadding * 2 + contentWidth + strokeWidth * 2
        val bubbleHeight = verticalPadding * 2 + textHeight

        val totalWidth = ceil(bubbleWidth).toInt().coerceAtLeast(1)
        val totalHeight = ceil(bubbleHeight + triangleHeight + strokeWidth).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokeInset = strokeWidth / 2f
        val left = strokeInset
        val top = strokeInset
        val right = totalWidth.toFloat() - strokeInset
        val bottom = bubbleHeight
        val centerX = totalWidth / 2f
        val triangleTipY = totalHeight.toFloat() - strokeInset

        val path = Path().apply {
            moveTo(left + cornerRadius, top)
            lineTo(right - cornerRadius, top)
            quadTo(right, top, right, top + cornerRadius)
            lineTo(right, bottom - cornerRadius)
            quadTo(right, bottom, right - cornerRadius, bottom)

            lineTo(centerX + triangleWidth / 2f, bottom)
            lineTo(centerX, triangleTipY)
            lineTo(centerX - triangleWidth / 2f, bottom)

            lineTo(left + cornerRadius, bottom)
            quadTo(left, bottom, left, bottom - cornerRadius)
            lineTo(left, top + cornerRadius)
            quadTo(left, top, left + cornerRadius, top)
            close()
        }

        // Fill Paint
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isSelected) {
                android.graphics.Color.parseColor("#E53935") // Active Primary Red
            } else {
                android.graphics.Color.WHITE
            }
        }
        canvas.drawPath(path, fillPaint)

        // Stroke Paint
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = if (isSelected) {
                android.graphics.Color.parseColor("#B71C1C")
            } else {
                android.graphics.Color.parseColor("#E53935")
            }
        }
        canvas.drawPath(path, strokePaint)

        // Draw Content (dot + text)
        val bubbleCenterY = (top + bottom) / 2f
        var startX = (totalWidth - contentWidth) / 2f

        if (hasDot) {
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = android.graphics.Color.parseColor("#E53935")
            }
            val dotCenterX = startX + dotRadius
            canvas.drawCircle(dotCenterX, bubbleCenterY, dotRadius, dotPaint)
            startX += dotRadius * 2 + dotSpacing
        }

        val textBaseline = bubbleCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(label, startX, textBaseline, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

class FamousCompanyActivity : ComponentActivity() {

    private val edgeToEdgeConfig: EdgeToEdgeConfig
        get() = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> EdgeToEdgeDefaults.darkTransparent()
            else -> EdgeToEdgeDefaults.lightTransparent()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdge(edgeToEdgeConfig)
        setContent {
            TBusTheme {
                FamousCompanyScreen(
                    onCloseClick = { finish() },
                )
            }
        }
    }

    private fun applyEdgeToEdge(config: EdgeToEdgeConfig) {
        enableEdgeToEdge(
            statusBarStyle = config.statusBarStyle,
            navigationBarStyle = config.navigationBarStyle,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamousCompanyScreen(
    onCloseClick: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val defaultCenter = remember { LatLng(36.3504, 127.8500) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(checkLocationPermission(context))
    }

    var companyMarkers by remember { mutableStateOf<List<CompanyLatLngMarker>>(emptyList()) }
    var isLoadingMarkers by remember { mutableStateOf(true) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var hasAdjustedInitialBounds by remember { mutableStateOf(false) }

    var selectedMarker by remember { mutableStateOf<CompanyLatLngMarker?>(null) }
    val companyDetailCache = remember { mutableStateMapOf<Int, UserEntity>() }
    val markerIconCache = remember { mutableMapOf<String, BitmapDescriptor>() }
    var isLoadingSelectedDetail by remember { mutableStateOf(false) }
    var detailDialogEntity by remember { mutableStateOf<UserEntity?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 7f)
    }

    val boundsPaddingPx = remember(density) {
        with(density) { 56.dp.roundToPx() }
    }

    suspend fun animateToAllMarkersBounds(markers: List<CompanyLatLngMarker>) {
        val validMarkers = markers.filter { it.lat in 33.0..39.5 && it.lng in 124.0..132.0 }
            .ifEmpty { markers }
        if (validMarkers.isEmpty()) return

        try {
            if (validMarkers.size == 1) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(validMarkers.first().position, 14f),
                    durationMs = 800,
                )
            } else {
                val boundsBuilder = LatLngBounds.builder()
                validMarkers.forEach { boundsBuilder.include(it.position) }
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), boundsPaddingPx),
                    durationMs = 900,
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to animate camera to marker bounds")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
    }

    // 1. 화면 진입 시 위치 권한 체크 및 마커 데이터 로드
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }

        isLoadingMarkers = true
        val loadedMarkers = withContext(Dispatchers.IO) {
            runCatching {
                val rawBody = MemberApiService.instance.getLatLngListRaw().string()
                parseCompanyMarkers(rawBody)
            }.onFailure { e ->
                Timber.e(e, "Failed to load company markers")
            }.getOrDefault(emptyList())
        }
        companyMarkers = loadedMarkers
        isLoadingMarkers = false

        if (loadedMarkers.isEmpty()) {
            Toast.makeText(context, "업소 위치 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. 현재 위치 조회 (내 위치 마커 표시용)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fetchCurrentLocation(context) { latLng ->
                currentLocation = latLng
            }
        }
    }

    // 3. 지도 로드 및 마커 데이터 수신 완료 시 모든 마커 바운더리에 맞춰 카메라 줌 자동 조절
    LaunchedEffect(isMapLoaded, companyMarkers) {
        if (isMapLoaded && companyMarkers.isNotEmpty() && !hasAdjustedInitialBounds) {
            hasAdjustedInitialBounds = true
            animateToAllMarkersBounds(companyMarkers)
        }
    }

    // 4. 마커 선택 시 해당 업소 상세 정보(/servlet/users/getInfo) 비동기 조회
    LaunchedEffect(selectedMarker?.id) {
        val marker = selectedMarker ?: return@LaunchedEffect
        if (marker.id <= 0 || companyDetailCache.containsKey(marker.id)) {
            isLoadingSelectedDetail = false
            return@LaunchedEffect
        }

        isLoadingSelectedDetail = true
        val fetchedDetail = withContext(Dispatchers.IO) {
            runCatching {
                val res = MemberApiService.instance.getUserInfo(marker.id)
                if (res.code == ResponseCodes.CodeOk && !res.msg.isNullOrBlank()) {
                    Gson().fromJson(res.msg, UserEntity::class.java)?.apply {
                        val parsedPhotos = parsePhotoJsonList(this.photo)
                        if (parsedPhotos.isNotEmpty()) {
                            this.photoList = ArrayList(parsedPhotos)
                        }
                    }
                } else {
                    null
                }
            }.onFailure { e ->
                Timber.e(e, "Failed to load company detail for uid=${marker.id}")
            }.getOrNull()
        }
        if (fetchedDetail != null) {
            companyDetailCache[marker.id] = fetchedDetail
        }
        isLoadingSelectedDetail = false
    }

    TBusScaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TBusTopAppBar(
                title = "소문난 업소",
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_mdpi),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(
                                start = TBusTheme.spacing.lg,
                                end = TBusTheme.spacing.sm,
                            )
                            .size(36.dp),
                    )
                },
                actions = {
                    TBusIconButton(onClick = onCloseClick) {
                        Image(
                            painter = painterResource(id = R.drawable.xbutton_black),
                            contentDescription = stringResource(R.string.btn_close),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        TBusSurface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(
                    start = TBusTheme.spacing.lg,
                    top = TBusTheme.spacing.sm,
                    end = TBusTheme.spacing.lg,
                    bottom = TBusTheme.spacing.lg,
                ),
            shape = TBusTheme.shapes.cardLarge,
            color = TBusTheme.colors.surface,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = hasLocationPermission,
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = hasLocationPermission,
                        zoomControlsEnabled = selectedMarker == null,
                        mapToolbarEnabled = false,
                    ),
                    onMapLoaded = {
                        isMapLoaded = true
                    },
                    onMapClick = {
                        selectedMarker = null
                    },
                ) {
                    // 내 현재 위치 마커 (파란색 계열)
                    currentLocation?.let { loc ->
                        Marker(
                            state = remember(loc) { MarkerState(position = loc) },
                            title = "현재 위치",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        )
                    }

                    // 소문난 업소 마커 리스트
                    companyMarkers.forEach { marker ->
                        val isSelected = selectedMarker?.id == marker.id
                        Marker(
                            state = remember(marker.id, marker.lat, marker.lng) {
                                MarkerState(position = marker.position)
                            },
                            icon = remember(marker.id, marker.displayTitle, isSelected) {
                                CompanyMarkerIconHelper.getMarkerIcon(
                                    context = context,
                                    title = marker.displayTitle,
                                    isSelected = isSelected,
                                    cache = markerIconCache,
                                )
                            },
                            anchor = Offset(0.5f, 1.0f),
                            zIndex = if (isSelected) 2f else 1f,
                            onClick = {
                                selectedMarker = marker
                                coroutineScope.launch {
                                    runCatching {
                                        val targetZoom = max(cameraPositionState.position.zoom, 13.5f)
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newLatLngZoom(
                                                marker.position,
                                                targetZoom,
                                            ),
                                            durationMs = 550,
                                        )
                                    }
                                }
                                true
                            },
                        )
                    }
                }

                // 상단 좌측 오버레이: 전체 업소 수 표시 및 전체 바운더리 줌 리셋 버튼
                if (companyMarkers.isNotEmpty() || isLoadingMarkers) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = companyMarkers.isNotEmpty()) {
                                selectedMarker = null
                                coroutineScope.launch {
                                    animateToAllMarkersBounds(companyMarkers)
                                }
                            },
                        shape = RoundedCornerShape(50),
                        color = TBusTheme.colors.surfaceRaised.copy(alpha = 0.94f),
                        shadowElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (isLoadingMarkers) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = TBusTheme.colors.route,
                                    strokeWidth = 2.dp,
                                )
                                TBusText(
                                    text = "업소 불러오는 중...",
                                    variant = TBusTextVariant.Caption,
                                    color = TBusTheme.colors.content,
                                    style = TBusTheme.typography.caption.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "전체 보기",
                                    tint = TBusTheme.colors.route,
                                    modifier = Modifier.size(16.dp),
                                )
                                TBusText(
                                    text = "전체 업소 ${companyMarkers.size}곳 한눈에 보기",
                                    variant = TBusTextVariant.Caption,
                                    color = TBusTheme.colors.content,
                                    style = TBusTheme.typography.caption.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                }

                // 하단 선택된 업소 정보 카드
                AnimatedVisibility(
                    visible = selectedMarker != null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                ) {
                    selectedMarker?.let { marker ->
                        val detail = companyDetailCache[marker.id]
                        FamousCompanyBottomCard(
                            marker = marker,
                            detail = detail,
                            isLoadingDetail = isLoadingSelectedDetail && detail == null,
                            onDismiss = { selectedMarker = null },
                            onOpenDetailDialog = {
                                if (detail != null) {
                                    detailDialogEntity = detail
                                }
                            },
                            onCallClick = { phone ->
                                WsUtils.phoneCall(context, phone)
                            },
                            onPhotoClick = { photoPath ->
                                context.startActivity(
                                    MemberPhotoViewerActivity.createIntent(
                                        context = context,
                                        imageUrl = GlobalVars.GetPictureFullPath(photoPath),
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    // 상세보기 모달 다이얼로그
    detailDialogEntity?.let { company ->
        FamousCompanyDetailDialog(
            company = company,
            onDismiss = { detailDialogEntity = null },
            onCallClick = { phone ->
                WsUtils.phoneCall(context, phone)
            },
            onPhotoClick = { photoPath ->
                context.startActivity(
                    MemberPhotoViewerActivity.createIntent(
                        context = context,
                        imageUrl = GlobalVars.GetPictureFullPath(photoPath),
                    ),
                )
            },
        )
    }
}

@Composable
private fun FamousCompanyBottomCard(
    marker: CompanyLatLngMarker,
    detail: UserEntity?,
    isLoadingDetail: Boolean,
    onDismiss: () -> Unit,
    onOpenDetailDialog: () -> Unit,
    onCallClick: (String) -> Unit,
    onPhotoClick: (String) -> Unit,
) {
    val title = detail?.company_name?.takeIf { it.isNotBlank() } ?: marker.displayTitle
    val sector = detail?.sectors?.takeIf { it.isNotBlank() }
    val productOrMenu = listOfNotNull(
        detail?.product?.takeIf { it.isNotBlank() },
        detail?.menu?.takeIf { it.isNotBlank() && it != detail.product },
    ).joinToString(" · ")
    val fullAddress = listOfNotNull(
        detail?.locale?.takeIf { it.isNotBlank() },
        detail?.addr?.takeIf { it.isNotBlank() } ?: marker.addr?.takeIf { it.isNotBlank() },
    ).joinToString(" ").trim()
    val operatingInfo = listOfNotNull(
        detail?.time?.takeIf { it.isNotBlank() }?.let { "영업: $it" },
        detail?.parking?.takeIf { it.isNotBlank() }?.let { "주차: $it" },
        detail?.capacity?.takeIf { it > 0 }?.let { "수용: ${it}명" },
    ).joinToString("  |  ")
    val phoneToCall = detail?.contact?.takeIf { it.isNotBlank() }
        ?: detail?.manager_tel?.takeIf { it.isNotBlank() }
        ?: detail?.tel?.takeIf { it.isNotBlank() }

    val photos = remember(detail?.photo, detail?.photoList, detail?.profile) {
        val list = detail?.photoList?.takeIf { it.isNotEmpty() }
            ?: parsePhotoJsonList(detail?.photo)
        if (list.isNotEmpty()) {
            list
        } else if (!detail?.profile.isNullOrBlank()) {
            listOf(detail!!.profile)
        } else {
            emptyList()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = TBusTheme.shapes.cardLarge,
        color = TBusTheme.colors.surfaceRaised,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TBusTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
        ) {
            // 상단 영역: 썸네일 + 상호명/업종/메뉴 + 닫기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
            ) {
                // 대표 이미지 썸네일
                val thumbPath = photos.firstOrNull()
                if (thumbPath != null) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TBusTheme.colors.surface)
                            .border(1.dp, TBusTheme.colors.line, RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(thumbPath) },
                    ) {
                        GlideImage(
                            imageModel = { GlobalVars.GetPictureFullPath(thumbPath) },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                            ),
                        )
                        if (photos.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                            ) {
                                TBusText(
                                    text = "+${photos.size}",
                                    variant = TBusTextVariant.Caption,
                                    color = Color.White,
                                    style = TBusTheme.typography.caption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 업종 및 예약가능 뱃지
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (sector != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TBusTheme.colors.route.copy(alpha = 0.14f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                TBusText(
                                    text = sector,
                                    variant = TBusTextVariant.Caption,
                                    color = TBusTheme.colors.route,
                                    style = TBusTheme.typography.caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                        }
                        if (detail?.booking == true) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TBusTheme.colors.signal.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                TBusText(
                                    text = "예약가능",
                                    variant = TBusTextVariant.Caption,
                                    color = TBusTheme.colors.signal,
                                    style = TBusTheme.typography.caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                        }
                    }

                    TBusText(
                        text = title,
                        variant = TBusTextVariant.Title,
                        color = TBusTheme.colors.content,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TBusTheme.typography.title.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )

                    if (productOrMenu.isNotBlank()) {
                        TBusText(
                            text = productOrMenu,
                            variant = TBusTextVariant.Body,
                            color = TBusTheme.colors.contentMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TBusTheme.typography.body.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = TBusTheme.colors.contentMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // 상세 정보 영역 (로딩 중이거나 주소/영업정보 표시)
            if (isLoadingDetail) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TBusTheme.colors.route,
                        strokeWidth = 2.dp,
                    )
                    TBusText(
                        text = "업소 상세 정보를 불러오는 중입니다...",
                        variant = TBusTextVariant.Caption,
                        color = TBusTheme.colors.contentMuted,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (fullAddress.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TBusTheme.colors.route,
                                modifier = Modifier.size(15.dp),
                            )
                            TBusText(
                                text = fullAddress,
                                variant = TBusTextVariant.Caption,
                                color = TBusTheme.colors.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TBusTheme.typography.caption.copy(fontSize = 13.sp),
                            )
                        }
                    }

                    if (operatingInfo.isNotBlank()) {
                        TBusText(
                            text = operatingInfo,
                            variant = TBusTextVariant.Caption,
                            color = TBusTheme.colors.contentMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TBusTheme.typography.caption.copy(fontSize = 12.sp),
                        )
                    }
                }
            }

            // 하단 액션 버튼 (상세정보 / 전화걸기)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.sm),
            ) {
                TBusButton(
                    text = "상세정보",
                    onClick = onOpenDetailDialog,
                    enabled = detail != null,
                    variant = TBusButtonVariant.Secondary,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp),
                    leadingIcon = { tint ->
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )

                TBusButton(
                    text = if (!phoneToCall.isNullOrBlank()) "전화걸기" else "번호없음",
                    onClick = {
                        if (!phoneToCall.isNullOrBlank()) {
                            onCallClick(phoneToCall)
                        }
                    },
                    enabled = !phoneToCall.isNullOrBlank(),
                    variant = TBusButtonVariant.Primary,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp),
                    leadingIcon = { tint ->
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun FamousCompanyDetailDialog(
    company: UserEntity,
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit,
    onPhotoClick: (String) -> Unit,
) {
    val photos = remember(company.photo, company.photoList, company.profile) {
        val list = company.photoList?.takeIf { it.isNotEmpty() }
            ?: parsePhotoJsonList(company.photo)
        if (list.isNotEmpty()) {
            list
        } else if (!company.profile.isNullOrBlank()) {
            listOf(company.profile)
        } else {
            emptyList()
        }
    }
    val fullAddress = listOfNotNull(
        company.locale?.takeIf { it.isNotBlank() },
        company.addr?.takeIf { it.isNotBlank() },
    ).joinToString(" ").trim()
    val phoneToCall = company.contact?.takeIf { it.isNotBlank() }
        ?: company.manager_tel?.takeIf { it.isNotBlank() }
        ?: company.tel?.takeIf { it.isNotBlank() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        TBusSurface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp),
            shape = TBusTheme.shapes.cardLarge,
            color = TBusTheme.colors.surfaceRaised,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = TBusTheme.spacing.xl,
                            top = TBusTheme.spacing.lg,
                            end = TBusTheme.spacing.md,
                            bottom = TBusTheme.spacing.md,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (!company.sectors.isNullOrBlank()) {
                            TBusText(
                                text = company.sectors,
                                variant = TBusTextVariant.Caption,
                                color = TBusTheme.colors.route,
                                style = TBusTheme.typography.caption.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                        TBusText(
                            text = company.company_name?.takeIf { it.isNotBlank() } ?: "업소 상세정보",
                            variant = TBusTextVariant.Title,
                            color = TBusTheme.colors.content,
                            style = TBusTheme.typography.title.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = TBusTheme.colors.content,
                        )
                    }
                }

                TBusHorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(TBusTheme.spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
                ) {
                    if (photos.isNotEmpty()) {
                        item(key = "photos") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                itemsIndexed(photos) { _, photoPath ->
                                    Box(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(TBusTheme.colors.surface)
                                            .border(1.dp, TBusTheme.colors.line, RoundedCornerShape(14.dp))
                                            .clickable { onPhotoClick(photoPath) },
                                    ) {
                                        GlideImage(
                                            imageModel = { GlobalVars.GetPictureFullPath(photoPath) },
                                            modifier = Modifier.fillMaxSize(),
                                            imageOptions = ImageOptions(
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center,
                                            ),
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    item(key = "info_rows") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CompanyDetailRow("취급품목", company.product)
                            CompanyDetailRow("주요메뉴", company.menu)
                            CompanyDetailRow("주소", fullAddress)
                            CompanyDetailRow("대표연락처", company.contact)
                            CompanyDetailRow("담당자", company.manager_name)
                            CompanyDetailRow("담당자연락처", company.manager_tel ?: company.tel)
                            CompanyDetailRow("수용인원", company.capacity?.takeIf { it > 0 }?.let { "${it}명" })
                            CompanyDetailRow("영업시간", company.time)
                            CompanyDetailRow("휴무일", company.closed)
                            CompanyDetailRow("주차", company.parking)
                            CompanyDetailRow("예약가능", if (company.booking == true) "가능" else "불가능")
                            CompanyDetailRow("용도", company.usage)
                            CompanyDetailRow("상세설명", company.detail)
                        }
                    }
                }

                TBusHorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TBusTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
                ) {
                    TBusButton(
                        text = "닫기",
                        onClick = onDismiss,
                        variant = TBusButtonVariant.Secondary,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp),
                    )

                    if (!phoneToCall.isNullOrBlank()) {
                        TBusButton(
                            text = "전화걸기",
                            onClick = { onCallClick(phoneToCall) },
                            variant = TBusButtonVariant.Primary,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
                            leadingIcon = { tint ->
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyDetailRow(
    label: String,
    value: String?,
) {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        TBusText(
            text = label,
            variant = TBusTextVariant.Caption,
            color = TBusTheme.colors.contentMuted,
            modifier = Modifier.width(88.dp),
            style = TBusTheme.typography.caption.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        TBusText(
            text = trimmed,
            variant = TBusTextVariant.Body,
            color = TBusTheme.colors.content,
            modifier = Modifier.weight(1f),
            style = TBusTheme.typography.body.copy(fontSize = 14.sp),
        )
    }
}

private fun parseCompanyMarkers(rawBody: String): List<CompanyLatLngMarker> {
    if (rawBody.isBlank()) return emptyList()
    return runCatching {
        val gson = Gson()
        val okJsonStart = rawBody.indexOf("{\"code\":200")
            .takeIf { it >= 0 }
            ?: rawBody.indexOf("{")
        if (okJsonStart < 0) return emptyList()

        val jsonSlice = rawBody.substring(okJsonStart)
        val response = gson.fromJson(jsonSlice, ResponseMessageEntity::class.java)
        if (response == null || response.code != ResponseCodes.CodeOk || response.msg.isNullOrBlank()) {
            return emptyList()
        }

        val listType = object : TypeToken<List<CompanyLatLngMarker>>() {}.type
        val parsed = gson.fromJson<List<CompanyLatLngMarker>>(response.msg, listType).orEmpty()
        parsed.filter { marker ->
            marker.id > 0 &&
                !marker.lat.isNaN() &&
                !marker.lng.isNaN() &&
                marker.lat in -90.0..90.0 &&
                marker.lng in -180.0..180.0 &&
                (marker.lat != 0.0 || marker.lng != 0.0)
        }
    }.getOrElse { e ->
        Timber.e(e, "Failed to parse company markers")
        emptyList()
    }
}

private fun parsePhotoJsonList(rawPhotoJson: String?): List<String> {
    if (rawPhotoJson.isNullOrBlank()) return emptyList()
    return runCatching {
        val listType = object : TypeToken<List<String>>() {}.type
        Gson().fromJson<List<String>>(rawPhotoJson, listType)
            ?.filter { it.isNotBlank() }
            .orEmpty()
    }.getOrDefault(emptyList())
}

private fun checkLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    onLocationResult: (LatLng) -> Unit,
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    fusedClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token,
    ).addOnSuccessListener { location: Location? ->
        if (location != null) {
            Timber.d("Current location from FusedLocation: ${location.latitude}, ${location.longitude}")
            onLocationResult(LatLng(location.latitude, location.longitude))
        } else {
            fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                val resolved = lastLoc ?: getFallbackLastLocation(context)
                if (resolved != null) {
                    Timber.d("Fallback last location: ${resolved.latitude}, ${resolved.longitude}")
                    onLocationResult(LatLng(resolved.latitude, resolved.longitude))
                }
            }
        }
    }.addOnFailureListener { e ->
        Timber.w(e, "getCurrentLocation failed, trying fallback")
        val fallback = getFallbackLastLocation(context)
        if (fallback != null) {
            onLocationResult(LatLng(fallback.latitude, fallback.longitude))
        }
    }
}

@SuppressLint("MissingPermission")
private fun getFallbackLastLocation(context: Context): Location? {
    return try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        when {
            gps != null && net != null -> if (gps.time >= net.time) gps else net
            gps != null -> gps
            else -> net
        }
    } catch (e: Exception) {
        null
    }
}
