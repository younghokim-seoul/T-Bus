package com.windnsoft.tbus.Fragment

import android.app.Activity
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
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
import com.windnsoft.tbus.BuildConfig
import com.windnsoft.tbus.General.DialogDismissListener
import com.windnsoft.tbus.designsystem.component.TBusAppUpdateDialogContent
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.roundToInt

class AppUpdateDialogFragment :
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
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        dialog?.setCanceledOnTouchOutside(false)
        return ComposeView(activity).apply {
            setViewTreeLifecycleOwner(this@AppUpdateDialogFragment)
            setViewTreeViewModelStoreOwner(this@AppUpdateDialogFragment)
            setViewTreeSavedStateRegistryOwner(this@AppUpdateDialogFragment)
            setContent {
                TBusTheme(darkTheme = false) {
                    TBusAppUpdateDialogContent(
                        onConfirmClick = {
                            openOneStoreAppPage(activity)
                            dismissAllowingStateLoss()
                        },
                        onCancelClick = {
                            dismissAllowingStateLoss()
                        },
                    )
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
                (availableBounds.width() * 0.86f).roundToInt(),
                (360 * density).roundToInt(),
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
        const val TAG = "AppUpdateDialogFragment"
        const val ONESTORE_PRODUCT_ID = "0000738186"
        const val ONESTORE_WEB_URL = "https://m.onestore.co.kr/v2/ko-kr/app/$ONESTORE_PRODUCT_ID?scYn=Y"
        const val ONESTORE_INTENT_URI = "onestore://common/product/$ONESTORE_PRODUCT_ID"

        @Volatile
        private var hasCheckedInSession = false

        private val httpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(7, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .build()
        }

        private val checkScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        @JvmStatic
        fun newInstance(): AppUpdateDialogFragment {
            return AppUpdateDialogFragment()
        }

        @JvmStatic
        fun show(activity: Activity?) {
            if (activity == null || activity.isFinishing || activity.isDestroyed) return
            val fm = activity.fragmentManager
            if (fm.findFragmentByTag(TAG) != null) return

            val dialog = newInstance()
            dialog.setStyle(STYLE_NO_TITLE, 0)
            dialog.show(fm, TAG)
        }

        @JvmStatic
        fun checkAndShowIfNeeded(activity: Activity?) {
            if (activity == null || activity.isFinishing || activity.isDestroyed) return
            if (hasCheckedInSession) return
            hasCheckedInSession = true

            checkScope.launch {
                val updateNeeded = withContext(Dispatchers.IO) {
                    isUpdateAvailableFromOneStore()
                }
                if (updateNeeded && !activity.isFinishing && !activity.isDestroyed) {
                    show(activity)
                }
            }
        }

        private fun isUpdateAvailableFromOneStore(): Boolean {
            return try {
                val request = Request.Builder()
                    .url(ONESTORE_WEB_URL)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return false
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) return false

                    val versionCodeRegex = Regex("""\\?"packageVer\\?"\s*:\s*\\?"(\d+)\\?"""")
                    val remoteVersionCode = versionCodeRegex.find(body)
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.toIntOrNull()

                    val versionNameRegex = Regex("""\\?"packageVerNm\\?"\s*:\s*\\?"([^"\\]+)\\?"""")
                    val remoteVersionName = versionNameRegex.find(body)
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.trim()

                    Timber.tag(TAG).i(
                        "OneStore version check -> local(versionCode=%d, versionName=%s), remote(packageVer=%s, packageVerNm=%s)",
                        BuildConfig.VERSION_CODE,
                        BuildConfig.VERSION_NAME,
                        remoteVersionCode?.toString() ?: "null",
                        remoteVersionName ?: "null",
                    )

                    if (remoteVersionCode != null) {
                        val updateNeeded = remoteVersionCode > BuildConfig.VERSION_CODE
                        Timber.tag(TAG).i(
                            "OneStore versionCode compare -> remote(%d) > local(%d) = %b",
                            remoteVersionCode,
                            BuildConfig.VERSION_CODE,
                            updateNeeded,
                        )
                        return updateNeeded
                    }

                    if (!remoteVersionName.isNullOrEmpty()) {
                        val updateNeeded = compareVersionNames(remoteVersionName, BuildConfig.VERSION_NAME) > 0
                        Timber.tag(TAG).i(
                            "OneStore versionName compare -> remote(%s) > local(%s) = %b",
                            remoteVersionName,
                            BuildConfig.VERSION_NAME,
                            updateNeeded,
                        )
                        return updateNeeded
                    }

                    false
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to check app update from OneStore")
                false
            }
        }

        private fun compareVersionNames(remote: String, local: String): Int {
            val remoteParts = remote.split('.').map { it.toIntOrNull() ?: 0 }
            val localParts = local.split('.').map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r != l) return r.compareTo(l)
            }
            return 0
        }

        @JvmStatic
        fun openOneStoreAppPage(context: Context?) {
            if (context == null) return
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ONESTORE_INTENT_URI)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ONESTORE_WEB_URL)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                } catch (ignored: Exception) {
                    Timber.e(ignored, "Failed to open OneStore page")
                }
            }
        }
    }
}
