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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
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
                        onUploadingStateChanged = { uploading ->
                            isCancelable = !uploading
                            dialog?.setCanceledOnTouchOutside(!uploading)
                        },
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
    onUploadingStateChanged: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onConfirm: (UserEntity) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cachedUser = remember { SharedPreferencesCache.getCurrentUser() }
    val userType = cachedUser.type

    val sectorOptions = remember(context) {
        context.resources.getStringArray(R.array.sector).toList()
    }
    val locale1Options = remember(context) {
        context.resources.getStringArray(R.array.locale).toList()
    }

    var isUploading by remember { mutableStateOf(false) }
    var serverUserSnapshot by remember { mutableStateOf<UserEntity?>(null) }
    var uploadedLocalProfilePath by remember { mutableStateOf<String?>(null) }
    val uploadedLocalCardPaths = remember { mutableStateListOf<String>() }

    // 업종 / 지역 선택 팝업 표시 상태
    var showSectorPopup by remember { mutableStateOf(false) }
    var showLocale1Popup by remember { mutableStateOf(false) }
    var showLocale2Popup by remember { mutableStateOf(false) }

    // 공통 및 버스회원(type=2) 상태
    var tel by remember {
        mutableStateOf(
            cachedUser.tel?.takeIf { it.isNotBlank() }
                ?: SharedPreferencesCache.getRealPhoneNumber().orEmpty(),
        )
    }
    var normalUserName by remember {
        mutableStateOf(cachedUser.name.orEmpty())
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

    // 업소회원(type=3) 상태
    val initialLocaleParts = remember {
        cachedUser.locale.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    val initialLocale1 = initialLocaleParts.getOrNull(0)
        ?.takeIf { it in locale1Options }
        ?: locale1Options.firstOrNull().orEmpty()
    val initialLocale2Options = getSubLocaleOptions(context, locale1Options, initialLocale1)
    val initialLocale2 = initialLocaleParts.getOrNull(1)
        ?.takeIf { it in initialLocale2Options }
        ?: initialLocale2Options.firstOrNull().orEmpty()

    var product by remember { mutableStateOf(cachedUser.product.orEmpty()) }
    var sectors by remember {
        mutableStateOf(
            cachedUser.sectors?.takeIf { it.isNotBlank() }
                ?: sectorOptions.firstOrNull().orEmpty(),
        )
    }
    var locale1 by remember { mutableStateOf(initialLocale1) }
    var locale2 by remember { mutableStateOf(initialLocale2) }
    val locale2Options = remember(context, locale1Options, locale1) {
        getSubLocaleOptions(context, locale1Options, locale1)
    }
    var managerName by remember {
        mutableStateOf(
            cachedUser.manager_name?.takeIf { it.isNotBlank() }
                ?: cachedUser.name.orEmpty(),
        )
    }
    var managerTel by remember { mutableStateOf(cachedUser.manager_tel.orEmpty()) }
    var contact by remember { mutableStateOf(cachedUser.contact.orEmpty()) }
    var menu by remember { mutableStateOf(cachedUser.menu.orEmpty()) }
    var capacity by remember {
        mutableStateOf(cachedUser.capacity?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var businessTime by remember { mutableStateOf(cachedUser.time.orEmpty()) }
    var closedInfo by remember { mutableStateOf(cachedUser.closed.orEmpty()) }
    var parkingInfo by remember { mutableStateOf(cachedUser.parking.orEmpty()) }
    var bookingAvailable by remember { mutableStateOf(cachedUser.booking ?: false) }

    var profilePhoto by remember { mutableStateOf(cachedUser.profile.orEmpty()) }
    var initialServerProfilePhoto by remember {
        mutableStateOf(cachedUser.profile?.takeIf { !isLocalFilePath(it) }.orEmpty())
    }
    val initialServerPhotos = remember {
        mutableStateListOf<String>().apply {
            val initialList = (cachedUser.photoList?.takeIf { it.isNotEmpty() }
                ?: parsePhotoJsonList(cachedUser.photo))
                .filter { !isLocalFilePath(it) }
            addAll(initialList)
        }
    }
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

    // 구글 이미지 피커에서 선택된 명함/업체 사진 추가 반영
    LaunchedEffect(newlyPickedBusinessCardPaths.size) {
        if (newlyPickedBusinessCardPaths.isNotEmpty()) {
            businessCardPhotos.addAll(newlyPickedBusinessCardPaths)
            onConsumeNewlyPickedCards()
        }
    }

    // 서버에서 최신 회원 상세 정보 조회 및 기준 상태 동기화
    LaunchedEffect(cachedUser.id, userType) {
        if (cachedUser.id > 0) {
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
                serverUserSnapshot = remoteUser
                if (!remoteUser.profile.isNullOrBlank()) {
                    initialServerProfilePhoto = remoteUser.profile
                }
                val remotePhotos = (remoteUser.photoList?.takeIf { it.isNotEmpty() }
                    ?: parsePhotoJsonList(remoteUser.photo))
                    .filter { !isLocalFilePath(it) }
                initialServerPhotos.clear()
                initialServerPhotos.addAll(remotePhotos)

                if (tel.isBlank() && !remoteUser.tel.isNullOrBlank()) tel = remoteUser.tel
                if (normalUserName.isBlank() && !remoteUser.name.isNullOrBlank()) {
                    normalUserName = remoteUser.name
                }

                if (userType == UserTypes.Bus) {
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
                    if ((profilePhoto.isBlank() || isLocalFilePath(profilePhoto)) &&
                        pickedProfilePhotoPath.isNullOrBlank() &&
                        !remoteUser.profile.isNullOrBlank()
                    ) {
                        profilePhoto = remoteUser.profile
                    }
                    if (businessCardPhotos.isEmpty() || businessCardPhotos.all { isLocalFilePath(it) }) {
                        val pendingLocal = businessCardPhotos.filter { isLocalFilePath(it) }
                        businessCardPhotos.clear()
                        businessCardPhotos.addAll(remotePhotos)
                        businessCardPhotos.addAll(pendingLocal)
                    }
                } else if (userType == UserTypes.Co) {
                    if (companyName.isBlank() && !remoteUser.company_name.isNullOrBlank()) {
                        companyName = remoteUser.company_name
                    }
                    if (product.isBlank() && !remoteUser.product.isNullOrBlank()) {
                        product = remoteUser.product
                    }
                    if (!remoteUser.sectors.isNullOrBlank()) {
                        sectors = remoteUser.sectors
                    }
                    if (!remoteUser.locale.isNullOrBlank()) {
                        val parts = remoteUser.locale.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                        val parsedL1 = parts.getOrNull(0)?.takeIf { it in locale1Options } ?: locale1
                        val subOptions = getSubLocaleOptions(context, locale1Options, parsedL1)
                        val parsedL2 = parts.getOrNull(1)?.takeIf { it in subOptions }
                            ?: subOptions.firstOrNull().orEmpty()
                        locale1 = parsedL1
                        locale2 = parsedL2
                    }
                    if (managerName.isBlank()) {
                        managerName = remoteUser.manager_name?.takeIf { it.isNotBlank() }
                            ?: remoteUser.name.orEmpty()
                    }
                    if (managerTel.isBlank() && !remoteUser.manager_tel.isNullOrBlank()) {
                        managerTel = remoteUser.manager_tel
                    }
                    if (contact.isBlank() && !remoteUser.contact.isNullOrBlank()) {
                        contact = remoteUser.contact
                    }
                    if (menu.isBlank() && !remoteUser.menu.isNullOrBlank()) {
                        menu = remoteUser.menu
                    }
                    if (capacity.isBlank() && (remoteUser.capacity ?: 0) > 0) {
                        capacity = remoteUser.capacity.toString()
                    }
                    if (businessTime.isBlank() && !remoteUser.time.isNullOrBlank()) {
                        businessTime = remoteUser.time
                    }
                    if (closedInfo.isBlank() && !remoteUser.closed.isNullOrBlank()) {
                        closedInfo = remoteUser.closed
                    }
                    if (parkingInfo.isBlank() && !remoteUser.parking.isNullOrBlank()) {
                        parkingInfo = remoteUser.parking
                    }
                    if (cachedUser.booking == null && remoteUser.booking != null) {
                        bookingAvailable = remoteUser.booking
                    }
                    if ((profilePhoto.isBlank() || isLocalFilePath(profilePhoto)) &&
                        pickedProfilePhotoPath.isNullOrBlank() &&
                        !remoteUser.profile.isNullOrBlank()
                    ) {
                        profilePhoto = remoteUser.profile
                    }
                    if (businessCardPhotos.isEmpty() || businessCardPhotos.all { isLocalFilePath(it) }) {
                        val pendingLocal = businessCardPhotos.filter { isLocalFilePath(it) }
                        businessCardPhotos.clear()
                        businessCardPhotos.addAll(remotePhotos)
                        businessCardPhotos.addAll(pendingLocal)
                    }
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
            if (userType == UserTypes.Bus || userType == UserTypes.Co) {
                // 버스 회원 (2) & 업소 회원 (3): LazyColumn으로 입력 필드 + 프로필 사진/설정 + 이미지 리스트 구성
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(TBusTheme.spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.lg),
                ) {
                    if (userType == UserTypes.Bus) {
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
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "승차 지역",
                                    value = ride,
                                    onValueChange = { ride = it },
                                    placeholder = "승차 지역을 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "회사 상호",
                                    value = companyName,
                                    onValueChange = { companyName = it },
                                    placeholder = "회사 상호를 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "승차 인승",
                                    value = passengers,
                                    onValueChange = { input ->
                                        passengers = input.filter { it.isDigit() }
                                    },
                                    placeholder = "승차 인승을 입력하세요",
                                    enabled = !isUploading,
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
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "운행 가능일",
                                    value = possibleDate,
                                    onValueChange = { possibleDate = it },
                                    placeholder = "운행 가능일을 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "차량 번호",
                                    value = carNumber,
                                    onValueChange = { carNumber = it },
                                    placeholder = "차량 번호를 입력하세요",
                                    enabled = !isUploading,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done,
                                    ),
                                )
                            }
                        }
                    } else {
                        item(key = "co_text_fields") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
                            ) {
                                LabeledProfileTextField(
                                    title = "전화번호",
                                    value = tel,
                                    onValueChange = {},
                                    enabled = false,
                                    readOnly = true,
                                )

                                LabeledProfileTextField(
                                    title = "업체명",
                                    value = companyName,
                                    onValueChange = { companyName = it },
                                    placeholder = "업체명을 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "대표상품",
                                    value = product,
                                    onValueChange = { product = it },
                                    placeholder = "대표상품을 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileSelectorField(
                                    title = "업종",
                                    selectedText = sectors.ifBlank { "업종 선택" },
                                    enabled = !isUploading,
                                    onClick = { showSectorPopup = true },
                                )

                                LabeledRegionSelectorFields(
                                    title = "지역",
                                    locale1Text = locale1.ifBlank { "시/도 선택" },
                                    locale2Text = locale2.ifBlank { "구/군 선택" },
                                    enabled = !isUploading,
                                    onClickLocale1 = { showLocale1Popup = true },
                                    onClickLocale2 = { showLocale2Popup = true },
                                )

                                LabeledProfileTextField(
                                    title = "담당자 성명",
                                    value = managerName,
                                    onValueChange = { managerName = it },
                                    placeholder = "담당자 성명을 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "담당자 연락처",
                                    value = managerTel,
                                    onValueChange = { managerTel = it },
                                    placeholder = "담당자 연락처를 입력하세요",
                                    enabled = !isUploading,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next,
                                    ),
                                )

                                LabeledProfileTextField(
                                    title = "업체 연락처",
                                    value = contact,
                                    onValueChange = { contact = it },
                                    placeholder = "업체 연락처를 입력하세요",
                                    enabled = !isUploading,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next,
                                    ),
                                )

                                LabeledProfileTextField(
                                    title = "메뉴 정보",
                                    value = menu,
                                    onValueChange = { menu = it },
                                    placeholder = "메뉴 정보를 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "최대 수용인원",
                                    value = capacity,
                                    onValueChange = { input ->
                                        capacity = input.filter { it.isDigit() }
                                    },
                                    placeholder = "최대 수용인원을 입력하세요",
                                    enabled = !isUploading,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next,
                                    ),
                                )

                                LabeledProfileTextField(
                                    title = "영업시간",
                                    value = businessTime,
                                    onValueChange = { businessTime = it },
                                    placeholder = "영업시간을 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "휴무 정보",
                                    value = closedInfo,
                                    onValueChange = { closedInfo = it },
                                    placeholder = "휴무 정보를 입력하세요",
                                    enabled = !isUploading,
                                )

                                LabeledProfileTextField(
                                    title = "버스 주차 정보",
                                    value = parkingInfo,
                                    onValueChange = { parkingInfo = it },
                                    placeholder = "버스 주차 정보를 입력하세요",
                                    enabled = !isUploading,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done,
                                    ),
                                )

                                ProfileCheckboxRow(
                                    label = "예약 가능",
                                    checked = bookingAvailable,
                                    enabled = !isUploading,
                                    onCheckedChange = { bookingAvailable = it },
                                )
                            }
                        }
                    }

                    // 프로필 사진 + 사진설정 / 사진추가 버튼 영역
                    item(key = "photo_controls") {
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
                                    enabled = !isUploading,
                                    variant = TBusButtonVariant.Secondary,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                TBusButton(
                                    text = if (userType == UserTypes.Co) "사진추가(필수)" else "사진추가(명함필수)",
                                    onClick = onRequestPickBusinessCardPhoto,
                                    enabled = !isUploading,
                                    variant = TBusButtonVariant.Primary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    // 선택한 이미지 리스트 (X 버튼으로 제거 가능)
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
                                        if (!isUploading && index in businessCardPhotos.indices) {
                                            businessCardPhotos.removeAt(index)
                                        }
                                    },
                                    enabled = !isUploading,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "사진 삭제",
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
                        tel = tel,
                        userName = normalUserName,
                        enabled = !isUploading,
                        onUserNameChange = { normalUserName = it },
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
                    enabled = !isUploading,
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
                    enabled = !isUploading,
                    onClick = {
                        if (isUploading) return@TBusButton
                        if (cachedUser.id <= 0) {
                            Toast.makeText(
                                context,
                                "회원 정보를 확인할 수 없습니다.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@TBusButton
                        }
                        if (userType != UserTypes.Bus &&
                            userType != UserTypes.Co &&
                            normalUserName.trim().isEmpty()
                        ) {
                            Toast.makeText(
                                context,
                                "이름을 입력해주세요.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@TBusButton
                        }

                        isUploading = true
                        onUploadingStateChanged(true)

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    val gson = Gson()
                                    val baseUser = serverUserSnapshot ?: SharedPreferencesCache.getCurrentUser()
                                    val memberToUpdate = gson.fromJson(
                                        gson.toJson(baseUser),
                                        UserEntity::class.java,
                                    ) ?: UserEntity()

                                    memberToUpdate.id = cachedUser.id
                                    memberToUpdate.type = userType

                                    when (userType) {
                                        UserTypes.Bus -> {
                                            memberToUpdate.tel = tel.trim()
                                            memberToUpdate.garage = garage.trim()
                                            memberToUpdate.ride = ride.trim()
                                            memberToUpdate.company_name = companyName.trim()
                                            memberToUpdate.passengers = passengers.trim().toIntOrNull() ?: 0
                                            memberToUpdate.name = driverName.trim()
                                            memberToUpdate.possible_date = possibleDate.trim()
                                            memberToUpdate.car_number = carNumber.trim()
                                            memberToUpdate.profile = initialServerProfilePhoto.ifBlank {
                                                baseUser.profile?.takeIf { !isLocalFilePath(it) }
                                            }
                                            memberToUpdate.photo = gson.toJson(initialServerPhotos.toList())
                                            memberToUpdate.photoList = null
                                        }

                                        UserTypes.Co -> {
                                            memberToUpdate.tel = tel.trim()
                                            memberToUpdate.company_name = companyName.trim()
                                            memberToUpdate.product = product.trim()
                                            memberToUpdate.sectors = sectors.trim()
                                            memberToUpdate.locale = listOf(locale1.trim(), locale2.trim())
                                                .filter { it.isNotEmpty() }
                                                .joinToString(" ")
                                            memberToUpdate.manager_name = managerName.trim()
                                            memberToUpdate.name = managerName.trim().ifEmpty {
                                                baseUser.name.orEmpty()
                                            }
                                            memberToUpdate.manager_tel = managerTel.trim()
                                            memberToUpdate.contact = contact.trim()
                                            memberToUpdate.menu = menu.trim()
                                            memberToUpdate.capacity = capacity.trim().toIntOrNull() ?: 0
                                            memberToUpdate.time = businessTime.trim()
                                            memberToUpdate.closed = closedInfo.trim()
                                            memberToUpdate.parking = parkingInfo.trim()
                                            memberToUpdate.booking = bookingAvailable
                                            memberToUpdate.profile = initialServerProfilePhoto.ifBlank {
                                                baseUser.profile?.takeIf { !isLocalFilePath(it) }
                                            }
                                            memberToUpdate.photo = gson.toJson(initialServerPhotos.toList())
                                            memberToUpdate.photoList = null
                                        }

                                        else -> {
                                            memberToUpdate.tel = tel.trim()
                                            memberToUpdate.name = normalUserName.trim()
                                        }
                                    }

                                    // 1. 회원 텍스트 정보 수정 (/servlet/users/updateMember)
                                    val updateRes = MemberApiService.instance.updateMember(
                                        userJson = gson.toJson(memberToUpdate),
                                    )
                                    if (updateRes.code != ResponseCodes.CodeOk) {
                                        throw IllegalStateException("updateMember failed: code=${updateRes.code}, msg=${updateRes.msg}")
                                    }

                                    // 2. 버스 회원 또는 업소 회원인 경우 프로필 사진 및 추가 사진 변경사항 반영
                                    if (userType == UserTypes.Bus || userType == UserTypes.Co) {
                                        // 2-1. 프로필 사진이 새로 선택된 로컬 파일인 경우 업로드 (/servlet/users/updateProfilePicture)
                                        if (isLocalFilePath(profilePhoto) && profilePhoto != uploadedLocalProfilePath) {
                                            val profileFile = File(profilePhoto)
                                            if (!profileFile.exists()) {
                                                throw IllegalStateException("Profile file not found: $profilePhoto")
                                            }
                                            val uidBody = cachedUser.id.toString()
                                                .toRequestBody("text/plain".toMediaTypeOrNull())
                                            val filePart = MultipartBody.Part.createFormData(
                                                "file",
                                                profileFile.name,
                                                profileFile.asRequestBody("image/jpeg".toMediaTypeOrNull()),
                                            )
                                            val profileRes = MemberApiService.instance.updateProfilePicture(
                                                uid = uidBody,
                                                file = filePart,
                                            )
                                            if (profileRes.code != ResponseCodes.CodeOk) {
                                                throw IllegalStateException("updateProfilePicture failed: code=${profileRes.code}")
                                            }
                                            uploadedLocalProfilePath = profilePhoto
                                        }

                                        // 2-2. 기존 서버 사진 중 삭제된 항목을 역순 인덱스로 제거 (/servlet/users/removeUserPhoto)
                                        val currentServerPhotosSnapshot = initialServerPhotos.toList()
                                        for (idx in currentServerPhotosSnapshot.indices.reversed()) {
                                            val serverPhotoPath = currentServerPhotosSnapshot[idx]
                                            if (serverPhotoPath !in businessCardPhotos) {
                                                val removeRes = MemberApiService.instance.removeUserPhoto(
                                                    uid = cachedUser.id,
                                                    pos = idx,
                                                )
                                                if (removeRes.code != ResponseCodes.CodeOk) {
                                                    throw IllegalStateException("removeUserPhoto failed at pos=$idx: code=${removeRes.code}")
                                                }
                                                initialServerPhotos.removeAt(idx)
                                            }
                                        }

                                        // 2-3. 새로 추가된 로컬 사진들을 순차 업로드 (/servlet/users/insertUserPhoto)
                                        val localCardSnapshots = businessCardPhotos
                                            .filter { isLocalFilePath(it) && it !in uploadedLocalCardPaths }
                                        for (localPath in localCardSnapshots) {
                                            val cardFile = File(localPath)
                                            if (!cardFile.exists()) {
                                                throw IllegalStateException("Photo file not found: $localPath")
                                            }
                                            val uidBody = cachedUser.id.toString()
                                                .toRequestBody("text/plain".toMediaTypeOrNull())
                                            val filePart = MultipartBody.Part.createFormData(
                                                "file",
                                                cardFile.name,
                                                cardFile.asRequestBody("image/jpeg".toMediaTypeOrNull()),
                                            )
                                            val insertRes = MemberApiService.instance.insertUserPhoto(
                                                uid = uidBody,
                                                file = filePart,
                                            )
                                            if (insertRes.code != ResponseCodes.CodeOk) {
                                                throw IllegalStateException("insertUserPhoto failed: code=${insertRes.code}")
                                            }
                                            uploadedLocalCardPaths.add(localPath)
                                        }
                                    }

                                    // 3. 서버에서 최종 반영된 회원 정보를 다시 조회하여 동기화 (/servlet/users/getInfo)
                                    val latestInfoRes = runCatching {
                                        MemberApiService.instance.getUserInfo(cachedUser.id)
                                    }.getOrNull()

                                    val finalUser = if (latestInfoRes != null &&
                                        latestInfoRes.code == ResponseCodes.CodeOk &&
                                        !latestInfoRes.msg.isNullOrBlank()
                                    ) {
                                        gson.fromJson(latestInfoRes.msg, UserEntity::class.java)
                                    } else {
                                        null
                                    } ?: memberToUpdate

                                    finalUser.apply {
                                        if (userType == UserTypes.Bus || userType == UserTypes.Co) {
                                            val resolvedPhotos = this.photoList?.takeIf { it.isNotEmpty() }
                                                ?: parsePhotoJsonList(this.photo)
                                            this.photoList = ArrayList(resolvedPhotos)
                                            this.photo = gson.toJson(resolvedPhotos)
                                        } else {
                                            this.tel = tel.trim()
                                            this.name = normalUserName.trim()
                                        }
                                    }
                                }
                            }

                            isUploading = false
                            onUploadingStateChanged(false)

                            result.onSuccess { updatedUser ->
                                onConfirm(updatedUser)
                            }.onFailure { error ->
                                Timber.e(error, "Profile update failed")
                                Toast.makeText(
                                    context,
                                    "프로필 수정에 실패했습니다. 다시 시도해주세요.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
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

    if (showSectorPopup) {
        ProfileSelectionPopupDialog(
            title = "업종 선택",
            options = sectorOptions,
            selectedOption = sectors,
            onSelect = { selected ->
                sectors = selected
                showSectorPopup = false
            },
            onDismiss = { showSectorPopup = false },
        )
    }

    if (showLocale1Popup) {
        ProfileSelectionPopupDialog(
            title = "지역(시/도) 선택",
            options = locale1Options,
            selectedOption = locale1,
            onSelect = { selected ->
                locale1 = selected
                val newSubOptions = getSubLocaleOptions(context, locale1Options, selected)
                if (locale2 !in newSubOptions) {
                    locale2 = newSubOptions.firstOrNull().orEmpty()
                }
                showLocale1Popup = false
            },
            onDismiss = { showLocale1Popup = false },
        )
    }

    if (showLocale2Popup) {
        ProfileSelectionPopupDialog(
            title = "상세 지역(구/군) 선택",
            options = locale2Options,
            selectedOption = locale2,
            onSelect = { selected ->
                locale2 = selected
                showLocale2Popup = false
            },
            onDismiss = { showLocale2Popup = false },
        )
    }

    if (isUploading) {
        ProfileUploadLoadingDialog()
    }
}

@Composable
private fun ProfileUploadLoadingDialog() {
    Dialog(
        onDismissRequest = { /* 업로드 중 닫기 불가 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        TBusSurface(
            shape = TBusTheme.shapes.cardLarge,
            color = TBusTheme.colors.surfaceRaised,
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = TBusTheme.spacing.xxl,
                        vertical = TBusTheme.spacing.xl,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.lg),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = TBusTheme.colors.route,
                    strokeWidth = 3.dp,
                )
                TBusText(
                    text = "업로드 중입니다...",
                    variant = TBusTextVariant.Body,
                    color = TBusTheme.colors.content,
                    style = TBusTheme.typography.body.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
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
private fun LabeledProfileSelectorField(
    title: String,
    selectedText: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
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

        ProfileSelectorBox(
            text = selectedText,
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LabeledRegionSelectorFields(
    title: String,
    locale1Text: String,
    locale2Text: String,
    enabled: Boolean = true,
    onClickLocale1: () -> Unit,
    onClickLocale2: () -> Unit,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.sm),
        ) {
            ProfileSelectorBox(
                text = locale1Text,
                enabled = enabled,
                onClick = onClickLocale1,
                modifier = Modifier.weight(1f),
            )

            ProfileSelectorBox(
                text = locale2Text,
                enabled = enabled,
                onClick = onClickLocale2,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProfileSelectorBox(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(TBusTheme.colors.surface)
            .border(
                width = 1.dp,
                color = TBusTheme.colors.line,
                shape = shape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TBusText(
            text = text,
            variant = TBusTextVariant.Body,
            color = if (enabled) TBusTheme.colors.content else TBusTheme.colors.contentMuted,
            style = TBusTheme.typography.body.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
        )

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "선택",
            tint = if (enabled) TBusTheme.colors.content else TBusTheme.colors.contentMuted,
        )
    }
}

@Composable
private fun ProfileCheckboxRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TBusTheme.colors.surface)
            .border(
                width = 1.dp,
                color = TBusTheme.colors.line,
                shape = shape,
            )
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = TBusTheme.colors.route,
                uncheckedColor = TBusTheme.colors.contentMuted,
            ),
        )

        TBusText(
            text = label,
            variant = TBusTextVariant.Body,
            color = TBusTheme.colors.content,
            style = TBusTheme.typography.body.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun ProfileSelectionPopupDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        TBusSurface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            shape = TBusTheme.shapes.cardLarge,
            color = TBusTheme.colors.surfaceRaised,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
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
                    TBusText(
                        text = title,
                        variant = TBusTextVariant.Title,
                        color = TBusTheme.colors.content,
                        style = TBusTheme.typography.title.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )

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
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(
                        items = options,
                        key = { it },
                    ) { option ->
                        val isSelected = option == selectedOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option) }
                                .background(
                                    if (isSelected) {
                                        TBusTheme.colors.route.copy(alpha = 0.08f)
                                    } else {
                                        Color.Transparent
                                    },
                                )
                                .padding(
                                    horizontal = TBusTheme.spacing.xl,
                                    vertical = 14.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TBusText(
                                text = option,
                                variant = TBusTextVariant.Body,
                                color = if (isSelected) TBusTheme.colors.route else TBusTheme.colors.content,
                                style = TBusTheme.typography.body.copy(
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                ),
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = TBusTheme.colors.route,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
    tel: String,
    userName: String,
    enabled: Boolean = true,
    onUserNameChange: (String) -> Unit,
) {
    // 전화번호 (디폴트 표시, 수정 불가)
    LabeledProfileTextField(
        title = "전화번호",
        value = tel,
        onValueChange = {},
        enabled = false,
        readOnly = true,
    )

    // 이름 (디폴트 표시, 수정 가능)
    LabeledProfileTextField(
        title = "이름",
        value = userName,
        onValueChange = onUserNameChange,
        placeholder = "이름을 입력하세요",
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
    )
}

private fun isLocalFilePath(rawPath: String?): Boolean {
    val trimmed = rawPath?.trim().orEmpty()
    if (trimmed.isEmpty()) return false
    return trimmed.startsWith("/") || trimmed.startsWith("file://")
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
