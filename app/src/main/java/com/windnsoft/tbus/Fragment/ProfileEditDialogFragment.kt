package com.windnsoft.tbus.Fragment

import android.app.Activity
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.canhub.cropper.CropImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Common.SharedPreferencesCache
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.Entities.UserTypes
import com.windnsoft.tbus.General.DialogDismissListener
import com.windnsoft.tbus.Http.ResponseCodes
import com.windnsoft.tbus.R
import com.windnsoft.tbus.designsystem.component.TBusButton
import com.windnsoft.tbus.designsystem.component.TBusButtonVariant
import com.windnsoft.tbus.designsystem.component.TBusHorizontalDivider
import com.windnsoft.tbus.designsystem.component.TBusSurface
import com.windnsoft.tbus.designsystem.component.TBusText
import com.windnsoft.tbus.designsystem.component.TBusTextButton
import com.windnsoft.tbus.designsystem.component.TBusTextField
import com.windnsoft.tbus.designsystem.component.TBusTextVariant
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import com.windnsoft.tbus.feature.member.MemberApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

class ProfileEditDialogFragment :
    DialogFragment(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var dismissListener: DialogDismissListener? = null

    private var pendingCropSourceUri by mutableStateOf<Uri?>(null)
    private var pickedProfilePhotoPath by mutableStateOf<String?>(null)
    private val newlyPickedBusinessCardPaths = mutableStateListOf<String>()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedStateRegistryController.performRestore(savedInstanceState)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        val composeView = ComposeView(activity).apply {
            setViewTreeLifecycleOwner(this@ProfileEditDialogFragment)
            setViewTreeViewModelStoreOwner(this@ProfileEditDialogFragment)
            setViewTreeSavedStateRegistryOwner(this@ProfileEditDialogFragment)
            setContent {
                TBusTheme(darkTheme = false) {
                    ProfileEditDialogContent(
                        pickedProfilePhotoPath = pickedProfilePhotoPath,
                        newlyPickedBusinessCardPaths = newlyPickedBusinessCardPaths,
                        onConsumeNewlyPickedCards = { newlyPickedBusinessCardPaths.clear() },
                        onRequestPickProfilePhoto = { launchGooglePhotoPickerForProfile() },
                        onRequestPickBusinessCardPhoto = { launchGooglePhotoPickerForBusinessCard() },
                        onCancel = { dismiss() },
                        onConfirm = { updatedUser ->
                            SharedPreferencesCache.setCurrentUser(updatedUser)
                            Toast.makeText(
                                activity,
                                "프로필이 수정되었습니다.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            dismiss()
                        },
                    )

                    pendingCropSourceUri?.let { sourceUri ->
                        ProfileImageCropDialog(
                            sourceUri = sourceUri,
                            onDismiss = { pendingCropSourceUri = null },
                            onCropSuccess = { croppedBitmap ->
                                val savedPath = saveCroppedBitmapToFile(activity, croppedBitmap)
                                if (savedPath != null) {
                                    pickedProfilePhotoPath = savedPath
                                }
                                pendingCropSourceUri = null
                            },
                        )
                    }
                }
            }
        }
        return composeView
    }

    private fun launchGooglePhotoPickerForProfile() {
        val act = activity ?: return
        val intent = ActivityResultContracts.PickVisualMedia().createIntent(
            act,
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
        startActivityForResult(intent, REQUEST_PICK_PROFILE_PHOTO)
    }

    private fun launchGooglePhotoPickerForBusinessCard() {
        val act = activity ?: return
        val intent = ActivityResultContracts.PickMultipleVisualMedia(10).createIntent(
            act,
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
        startActivityForResult(intent, REQUEST_PICK_BUSINESS_CARD_PHOTO)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val act = activity ?: return

        when (requestCode) {
            REQUEST_PICK_PROFILE_PHOTO -> {
                val uri = ActivityResultContracts.PickVisualMedia().parseResult(resultCode, data)
                    ?: data?.data
                if (uri != null) {
                    pendingCropSourceUri = uri
                }
            }

            REQUEST_PICK_BUSINESS_CARD_PHOTO -> {
                val uris = ActivityResultContracts.PickMultipleVisualMedia(10)
                    .parseResult(resultCode, data)
                    .ifEmpty { listOfNotNull(data?.data) }
                uris.forEach { uri ->
                    copyUriToInternalStorage(act, uri, "card")?.let { savedPath ->
                        newlyPickedBusinessCardPaths.add(savedPath)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val currentDialog = dialog
        val window = currentDialog?.window
        if (window != null && activity != null) {
            window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            window.setGravity(Gravity.CENTER)

            window.decorView.setViewTreeLifecycleOwner(this)
            window.decorView.setViewTreeViewModelStoreOwner(this)
            window.decorView.setViewTreeSavedStateRegistryOwner(this)

            val availableBounds = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(availableBounds)
            val density = resources.displayMetrics.density
            val width = min(
                (availableBounds.width() * 0.92f).roundToInt(),
                (600 * density).roundToInt(),
            )
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedStateRegistryController.performSave(outState)
    }

    override fun onStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        dismissListener?.onDismiss(dialog)
        super.onDismiss(dialog)
    }

    fun setDismissListener(listener: DialogDismissListener?) {
        this.dismissListener = listener
    }

    companion object {
        const val TAG = "ProfileEditDialogFragment"
        private const val REQUEST_PICK_PROFILE_PHOTO = 21001
        private const val REQUEST_PICK_BUSINESS_CARD_PHOTO = 21002

        @JvmStatic
        fun newInstance(): ProfileEditDialogFragment {
            return ProfileEditDialogFragment()
        }
    }
}

@Composable
private fun ProfileEditDialogContent(
    pickedProfilePhotoPath: String?,
    newlyPickedBusinessCardPaths: List<String>,
    onConsumeNewlyPickedCards: () -> Unit,
    onRequestPickProfilePhoto: () -> Unit,
    onRequestPickBusinessCardPhoto: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: (UserEntity) -> Unit,
) {
    val cachedUser = remember { SharedPreferencesCache.getCurrentUser() }
    val userType = cachedUser.type

    // 일반회원/기본값 상태
    var userId by remember {
        mutableStateOf(
            cachedUser.tel?.takeIf { it.isNotBlank() }
                ?: cachedUser.name?.takeIf { it.isNotBlank() }
                ?: if (cachedUser.id > 0) cachedUser.id.toString() else "",
        )
    }
    var password by remember {
        mutableStateOf(cachedUser.password.orEmpty())
    }
    var showPassword by remember { mutableStateOf(false) }

    // 버스회원(type=2) 전용 상태
    var tel by remember {
        mutableStateOf(
            cachedUser.tel?.takeIf { it.isNotBlank() }
                ?: SharedPreferencesCache.getRealPhoneNumber().orEmpty(),
        )
    }
    var garage by remember { mutableStateOf(cachedUser.garage.orEmpty()) }
    var ride by remember { mutableStateOf(cachedUser.ride.orEmpty()) }
    var companyName by remember { mutableStateOf(cachedUser.company_name.orEmpty()) }
    var passengers by remember {
        mutableStateOf(cachedUser.passengers?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var driverName by remember { mutableStateOf(cachedUser.name.orEmpty()) }
    var possibleDate by remember { mutableStateOf(cachedUser.possible_date.orEmpty()) }
    var carNumber by remember { mutableStateOf(cachedUser.car_number.orEmpty()) }
    var profilePhoto by remember { mutableStateOf(cachedUser.profile.orEmpty()) }
    val businessCardPhotos = remember {
        mutableStateListOf<String>().apply {
            val initialList = cachedUser.photoList?.takeIf { it.isNotEmpty() }
                ?: parsePhotoJsonList(cachedUser.photo)
            addAll(initialList)
        }
    }

    // 크롭 완료된 프로필 사진 반영
    LaunchedEffect(pickedProfilePhotoPath) {
        if (!pickedProfilePhotoPath.isNullOrBlank()) {
            profilePhoto = pickedProfilePhotoPath
        }
    }

    // 구글 이미지 피커에서 선택된 명함 사진 추가 반영
    LaunchedEffect(newlyPickedBusinessCardPaths.size) {
        if (newlyPickedBusinessCardPaths.isNotEmpty()) {
            businessCardPhotos.addAll(newlyPickedBusinessCardPaths)
            onConsumeNewlyPickedCards()
        }
    }

    // 버스회원이고 상세 필드가 비어있는 경우 서버에서 초기 상세 정보 조회
    LaunchedEffect(cachedUser.id, userType) {
        if (userType == UserTypes.Bus && cachedUser.id > 0 && garage.isBlank() && companyName.isBlank()) {
            val remoteUser: UserEntity? = withContext(Dispatchers.IO) {
                runCatching<UserEntity?> {
                    val response = MemberApiService.instance.getUserInfo(cachedUser.id)
                    if (response.code == ResponseCodes.CodeOk && !response.msg.isNullOrBlank()) {
                        Gson().fromJson(response.msg, UserEntity::class.java)
                    } else {
                        null
                    }
                }.getOrNull()
            }
            if (remoteUser != null) {
                if (tel.isBlank() && !remoteUser.tel.isNullOrBlank()) tel = remoteUser.tel
                if (garage.isBlank() && !remoteUser.garage.isNullOrBlank()) garage = remoteUser.garage
                if (ride.isBlank() && !remoteUser.ride.isNullOrBlank()) ride = remoteUser.ride
                if (companyName.isBlank() && !remoteUser.company_name.isNullOrBlank()) {
                    companyName = remoteUser.company_name
                }
                if (passengers.isBlank() && (remoteUser.passengers ?: 0) > 0) {
                    passengers = remoteUser.passengers.toString()
                }
                if (driverName.isBlank() && !remoteUser.name.isNullOrBlank()) {
                    driverName = remoteUser.name
                }
                if (possibleDate.isBlank() && !remoteUser.possible_date.isNullOrBlank()) {
                    possibleDate = remoteUser.possible_date
                }
                if (carNumber.isBlank() && !remoteUser.car_number.isNullOrBlank()) {
                    carNumber = remoteUser.car_number
                }
                if (profilePhoto.isBlank() && !remoteUser.profile.isNullOrBlank()) {
                    profilePhoto = remoteUser.profile
                }
                if (businessCardPhotos.isEmpty()) {
                    val remotePhotos = remoteUser.photoList?.takeIf { it.isNotEmpty() }
                        ?: parsePhotoJsonList(remoteUser.photo)
                    businessCardPhotos.addAll(remotePhotos)
                }
            }
        }
    }

    TBusSurface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp),
        shape = TBusTheme.shapes.cardLarge,
        color = TBusTheme.colors.surfaceRaised,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 상단 헤더 타이틀
            TBusText(
                text = "프로필 수정",
                variant = TBusTextVariant.Title,
                color = TBusTheme.colors.content,
                style = TBusTheme.typography.title.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TBusTheme.spacing.xl,
                        top = TBusTheme.spacing.xxl,
                        end = TBusTheme.spacing.xl,
                        bottom = TBusTheme.spacing.xl,
                    ),
            )

            TBusHorizontalDivider()

            // 본문 영역 (회원 타입별 UI 분기)
            if (userType == UserTypes.Bus) {
                // 버스 회원 (2): LazyColumn으로 입력 필드 + 프로필 사진/설정 + 명함 이미지 리스트 구성
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(TBusTheme.spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.lg),
                ) {
                    item(key = "bus_text_fields") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
                        ) {
                            LabeledProfileTextField(
                                title = "전화번호",
                                value = tel,
                                onValueChange = { tel = it },
                                enabled = false,
                                readOnly = true,
                            )

                            LabeledProfileTextField(
                                title = "차고지역",
                                value = garage,
                                onValueChange = { garage = it },
                                placeholder = "차고지역을 입력하세요",
                            )

                            LabeledProfileTextField(
                                title = "승차 지역",
                                value = ride,
                                onValueChange = { ride = it },
                                placeholder = "승차 지역을 입력하세요",
                            )

                            LabeledProfileTextField(
                                title = "회사 상호",
                                value = companyName,
                                onValueChange = { companyName = it },
                                placeholder = "회사 상호를 입력하세요",
                            )

                            LabeledProfileTextField(
                                title = "승차 인승",
                                value = passengers,
                                onValueChange = { input ->
                                    passengers = input.filter { it.isDigit() }
                                },
                                placeholder = "승차 인승을 입력하세요",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                ),
                            )

                            LabeledProfileTextField(
                                title = "운전자명",
                                value = driverName,
                                onValueChange = { driverName = it },
                                placeholder = "운전자명을 입력하세요",
                            )

                            LabeledProfileTextField(
                                title = "운행 가능일",
                                value = possibleDate,
                                onValueChange = { possibleDate = it },
                                placeholder = "운행 가능일을 입력하세요",
                            )

                            LabeledProfileTextField(
                                title = "차량 번호",
                                value = carNumber,
                                onValueChange = { carNumber = it },
                                placeholder = "차량 번호를 입력하세요",
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                ),
                            )
                        }
                    }

                    // 프로필 사진 + 사진설정 / 사진추가(명함필수) 버튼 영역
                    item(key = "bus_photo_controls") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = TBusTheme.spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.lg),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(TBusTheme.colors.surface)
                                    .border(
                                        width = 1.dp,
                                        color = TBusTheme.colors.line,
                                        shape = RoundedCornerShape(16.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                val resolvedModel = remember(profilePhoto) {
                                    resolveImageModel(profilePhoto)
                                }
                                if (resolvedModel != null) {
                                    GlideImage(
                                        imageModel = { resolvedModel },
                                        modifier = Modifier.fillMaxSize(),
                                        imageOptions = ImageOptions(
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center,
                                        ),
                                        failure = {
                                            Image(
                                                painter = painterResource(id = R.drawable.person),
                                                contentDescription = "기본 프로필",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp),
                                                contentScale = ContentScale.Fit,
                                            )
                                        },
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.person),
                                        contentDescription = "기본 프로필",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.sm),
                            ) {
                                TBusButton(
                                    text = "사진설정",
                                    onClick = onRequestPickProfilePhoto,
                                    variant = TBusButtonVariant.Secondary,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                TBusButton(
                                    text = "사진추가(명함필수)",
                                    onClick = onRequestPickBusinessCardPhoto,
                                    variant = TBusButtonVariant.Primary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    // 선택한 명함 이미지 리스트 (X 버튼으로 제거 가능)
                    itemsIndexed(
                        items = businessCardPhotos,
                        key = { index, path -> "${index}_$path" },
                    ) { index, photoPath ->
                        val resolvedCardModel = remember(photoPath) {
                            resolveImageModel(photoPath)
                        }
                        if (resolvedCardModel != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TBusTheme.colors.surface),
                            ) {
                                GlideImage(
                                    imageModel = { resolvedCardModel },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    requestOptions = {
                                        RequestOptions()
                                            .override(1080, 1440)
                                            .format(DecodeFormat.PREFER_RGB_565)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .fitCenter()
                                    },
                                    imageOptions = ImageOptions(
                                        contentScale = ContentScale.FillWidth,
                                        alignment = Alignment.Center,
                                        requestSize = IntSize(1080, 1440),
                                    ),
                                )

                                IconButton(
                                    onClick = {
                                        if (index in businessCardPhotos.indices) {
                                            businessCardPhotos.removeAt(index)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "명함 삭제",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(TBusTheme.spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.lg),
                ) {
                    NormalUserProfileEditFields(
                        userId = userId,
                        password = password,
                        showPassword = showPassword,
                        onPasswordChange = { password = it },
                        onTogglePasswordVisibility = { showPassword = !showPassword },
                    )
                }
            }

            TBusHorizontalDivider()

            // 하단 버튼 영역 (취소 / 확인)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TBusTheme.spacing.xl),
                horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = TBusTheme.shapes.pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TBusTheme.colors.surface,
                        contentColor = TBusTheme.colors.content,
                    ),
                    contentPadding = PaddingValues(12.dp),
                ) {
                    TBusText(
                        text = stringResource(id = R.string.btn_cancel),
                        variant = TBusTextVariant.Label,
                        color = TBusTheme.colors.content,
                    )
                }

                TBusButton(
                    text = stringResource(id = R.string.btn_ok),
                    onClick = {
                        val updated = SharedPreferencesCache.getCurrentUser().apply {
                            if (userType == UserTypes.Bus) {
                                this.tel = tel.trim()
                                this.garage = garage.trim()
                                this.ride = ride.trim()
                                this.company_name = companyName.trim()
                                this.passengers = passengers.trim().toIntOrNull() ?: 0
                                this.name = driverName.trim()
                                this.possible_date = possibleDate.trim()
                                this.car_number = carNumber.trim()
                                this.profile = profilePhoto
                                this.photoList = ArrayList(businessCardPhotos)
                                this.photo = Gson().toJson(businessCardPhotos)
                            } else {
                                this.password = password
                            }
                        }
                        onConfirm(updated)
                    },
                    variant = TBusButtonVariant.Primary,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                )
            }
        }
    }
}

@Composable
private fun LabeledProfileTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TBusText(
            text = title,
            variant = TBusTextVariant.Label,
            color = TBusTheme.colors.content,
            style = TBusTheme.typography.label.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )

        TBusTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            placeholder = placeholder.ifBlank { null },
            singleLine = true,
            keyboardOptions = keyboardOptions,
        )
    }
}

@Composable
private fun ProfileImageCropDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onCropSuccess: (Bitmap) -> Unit,
) {
    var cropImageViewRef by remember { mutableStateOf<CropImageView?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            // 상단 바: 제목 + 회전 버튼 + 닫기 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TBusText(
                    text = "프로필 사진 자르기",
                    variant = TBusTextVariant.Title,
                    color = Color.White,
                    style = TBusTheme.typography.title.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(
                        onClick = { cropImageViewRef?.rotateImage(90) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "회전",
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White,
                        )
                    }
                }
            }

            // 중앙: android-image-cropper 의 CropImageView
            AndroidView(
                factory = { ctx ->
                    CropImageView(ctx).apply {
                        setAspectRatio(1, 1)
                        setFixedAspectRatio(true)
                        guidelines = CropImageView.Guidelines.ON
                        scaleType = CropImageView.ScaleType.FIT_CENTER
                        setImageUriAsync(sourceUri)
                        cropImageViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            // 하단: 취소 / 자르기 완료 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = TBusTheme.shapes.pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                    ),
                ) {
                    TBusText(
                        text = "취소",
                        variant = TBusTextVariant.Label,
                        color = Color.White,
                    )
                }

                TBusButton(
                    text = "자르기 완료",
                    onClick = {
                        val cropped = cropImageViewRef?.getCroppedImage(512, 512)
                        if (cropped != null) {
                            onCropSuccess(cropped)
                        } else {
                            onDismiss()
                        }
                    },
                    variant = TBusButtonVariant.Primary,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                )
            }
        }
    }
}

@Composable
private fun NormalUserProfileEditFields(
    userId: String,
    password: String,
    showPassword: Boolean,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
    // 아이디 (디폴트 표시, 수정 불가)
    TBusTextField(
        value = userId,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        readOnly = true,
        label = "아이디",
        singleLine = true,
    )

    // 암호 (디폴트 표시, 수정 가능)
    TBusTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = true,
        label = "암호",
        placeholder = "암호를 입력하세요",
        singleLine = true,
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        trailingIcon = {
            TBusTextButton(
                text = if (showPassword) "숨김" else "보기",
                onClick = onTogglePasswordVisibility,
            )
        },
    )
}

private fun resolveImageModel(rawPath: String?): Any? {
    val trimmed = rawPath?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    return when {
        trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("content://") ||
            trimmed.startsWith("file://") -> trimmed
        trimmed.startsWith("/") -> File(trimmed)
        else -> GlobalVars.GetPictureFullPath(trimmed)
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

private fun saveCroppedBitmapToFile(context: Context?, bitmap: Bitmap): String? {
    if (context == null) return null
    return runCatching {
        val dir = File(context.filesDir, "profile_photos").apply {
            if (!exists()) mkdirs()
        }
        val outFile = File(dir, "profile_crop_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        outFile.absolutePath
    }.getOrNull()
}

private fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String): String? {
    return runCatching {
        val dir = File(context.filesDir, "profile_photos").apply {
            if (!exists()) mkdirs()
        }
        val outFile = File(dir, "${prefix}_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        } ?: return null
        outFile.absolutePath
    }.getOrNull()
}
