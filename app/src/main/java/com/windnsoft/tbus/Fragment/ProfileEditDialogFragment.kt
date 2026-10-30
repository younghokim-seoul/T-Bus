package com.windnsoft.tbus.Fragment

import android.app.DialogFragment
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.windnsoft.tbus.Common.SharedPreferencesCache
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.Entities.UserTypes
import com.windnsoft.tbus.General.DialogDismissListener
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
                }
            }
        }
        return composeView
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val currentDialog = dialog
        val window = currentDialog?.window
        if (window != null && activity != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setGravity(Gravity.CENTER)

            window.decorView.setViewTreeLifecycleOwner(this)
            window.decorView.setViewTreeViewModelStoreOwner(this)
            window.decorView.setViewTreeSavedStateRegistryOwner(this)

            val availableBounds = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(availableBounds)
            val density = resources.displayMetrics.density
            val width = min(
                (availableBounds.width() * 0.9f).roundToInt(),
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

        @JvmStatic
        fun newInstance(): ProfileEditDialogFragment {
            return ProfileEditDialogFragment()
        }
    }
}

@Composable
private fun ProfileEditDialogContent(
    onCancel: () -> Unit,
    onConfirm: (UserEntity) -> Unit,
) {
    val cachedUser = remember { SharedPreferencesCache.getCurrentUser() }
    val userType = cachedUser.type

    // 기본값: 아이디(수정 불가) 및 암호(수정 가능)
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
            // 상단 헤더 (PrivacyPolicyFragment와 동일한 패딩/타이포그래피 톤)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(TBusTheme.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.lg),
            ) {
                when (userType) {
                    UserTypes.Bus -> {
                        // 관광버스 회원 (2) — 추후 상세 UI 확장 가능, 현재 기본 프로필 필드 표시
                        NormalUserProfileEditFields(
                            userId = userId,
                            password = password,
                            showPassword = showPassword,
                            onPasswordChange = { password = it },
                            onTogglePasswordVisibility = { showPassword = !showPassword },
                        )
                    }

                    UserTypes.Co -> {
                        // 업체회원 (3) — 추후 상세 UI 확장 가능, 현재 기본 프로필 필드 표시
                        NormalUserProfileEditFields(
                            userId = userId,
                            password = password,
                            showPassword = showPassword,
                            onPasswordChange = { password = it },
                            onTogglePasswordVisibility = { showPassword = !showPassword },
                        )
                    }

                    UserTypes.Normal -> {
                        // 일반회원 (1): 아이디(디폴트 표시, 수정 불가) + 암호(디폴트 표시, 수정 가능)
                        NormalUserProfileEditFields(
                            userId = userId,
                            password = password,
                            showPassword = showPassword,
                            onPasswordChange = { password = it },
                            onTogglePasswordVisibility = { showPassword = !showPassword },
                        )
                    }

                    else -> {
                        NormalUserProfileEditFields(
                            userId = userId,
                            password = password,
                            showPassword = showPassword,
                            onPasswordChange = { password = it },
                            onTogglePasswordVisibility = { showPassword = !showPassword },
                        )
                    }
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
                            this.password = password
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
