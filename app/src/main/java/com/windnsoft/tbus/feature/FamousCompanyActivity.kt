package com.windnsoft.tbus.feature

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.windnsoft.tbus.R
import com.windnsoft.tbus.designsystem.component.TBusIconButton
import com.windnsoft.tbus.designsystem.component.TBusScaffold
import com.windnsoft.tbus.designsystem.component.TBusSurface
import com.windnsoft.tbus.designsystem.component.TBusTopAppBar
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import kotlinx.coroutines.launch
import timber.log.Timber

class FamousCompanyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TBusTheme {
                FamousCompanyScreen(
                    onCloseClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamousCompanyScreen(
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultSeoul = remember { LatLng(37.5665, 126.9780) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(checkLocationPermission(context))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultSeoul, 14f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (!granted) {
            Toast.makeText(context, "위치 권한이 거부되어 기본 위치로 표시합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 화면 진입 시 위치 권한 체크 및 요청
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 위치 권한이 허용되면 현재 위치를 가져와 카메라 포커스 이동
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fetchCurrentLocation(context) { latLng ->
                currentLocation = latLng
                coroutineScope.launch {
                    try {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                            durationMs = 800
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to animate camera to current location")
                    }
                }
            }
        }
    }

    TBusScaffold(
        modifier = Modifier.fillMaxSize(),
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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = hasLocationPermission,
                    zoomControlsEnabled = true
                )
            ) {
                currentLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = loc),
                        title = "현재 위치"
                    )
                }
            }
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    onLocationResult: (LatLng) -> Unit
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    fusedClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
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
