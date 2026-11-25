package com.windnsoft.tbus.feature.address

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog as ComposeDialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.windnsoft.tbus.R
import com.windnsoft.tbus.designsystem.component.TBusButton
import com.windnsoft.tbus.designsystem.component.TBusButtonVariant
import com.windnsoft.tbus.designsystem.component.TBusHorizontalDivider
import com.windnsoft.tbus.designsystem.component.TBusSurface
import com.windnsoft.tbus.designsystem.component.TBusText
import com.windnsoft.tbus.designsystem.component.TBusTextVariant
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import timber.log.Timber
import kotlin.math.min
import kotlin.math.roundToInt

internal object DaumPostcodeWebViewSupport {
    private const val BASE_URL = "https://postcode.map.kakao.com/"
    private const val BRIDGE_NAME = "AndroidPostcodeBridge"

    private val POSTCODE_HTML = """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    overflow: hidden;
                    background-color: #FFFFFF;
                }
                #layer {
                    width: 100%;
                    height: 100%;
                }
            </style>
        </head>
        <body>
            <div id="layer"></div>
            <script>
                function initPostcode() {
                    try {
                        var elementLayer = document.getElementById('layer');
                        var PostcodeConstructor = (window.kakao && window.kakao.Postcode)
                            ? window.kakao.Postcode
                            : daum.Postcode;
                        new PostcodeConstructor({
                            oncomplete: function(data) {
                                var payload = {
                                    zonecode: data.zonecode || "",
                                    address: data.address || "",
                                    roadAddress: data.roadAddress || "",
                                    jibunAddress: data.jibunAddress || "",
                                    autoRoadAddress: data.autoRoadAddress || "",
                                    autoJibunAddress: data.autoJibunAddress || "",
                                    sido: data.sido || "",
                                    sigungu: data.sigungu || "",
                                    bname: data.bname || "",
                                    buildingName: data.buildingName || "",
                                    userSelectedType: data.userSelectedType || ""
                                };
                                if (window.$BRIDGE_NAME && window.$BRIDGE_NAME.onAddressSelected) {
                                    window.$BRIDGE_NAME.onAddressSelected(JSON.stringify(payload));
                                }
                            },
                            width: '100%',
                            height: '100%',
                            maxSuggestItems: 5
                        }).embed(elementLayer, { autoClose: false });

                        if (window.$BRIDGE_NAME && window.$BRIDGE_NAME.onReady) {
                            window.$BRIDGE_NAME.onReady();
                        }
                    } catch (e) {
                        if (window.$BRIDGE_NAME && window.$BRIDGE_NAME.onLoadError) {
                            window.$BRIDGE_NAME.onLoadError();
                        }
                    }
                }
            </script>
            <script
                src="https://t1.kakaocdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
                onload="initPostcode()"
                onerror="if (window.$BRIDGE_NAME && window.$BRIDGE_NAME.onLoadError) { window.$BRIDGE_NAME.onLoadError(); }">
            </script>
        </body>
        </html>
    """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    fun createConfiguredWebView(
        context: Context,
        onReady: () -> Unit,
        onError: () -> Unit,
        onSelected: (DaumPostcodeData) -> Unit,
    ): WebView {
        val mainHandler = Handler(Looper.getMainLooper())
        val gson = Gson()

        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onReady() {
                        mainHandler.post { onReady() }
                    }

                    @JavascriptInterface
                    fun onLoadError() {
                        mainHandler.post { onError() }
                    }

                    @JavascriptInterface
                    fun onAddressSelected(json: String?) {
                        if (json.isNullOrBlank()) return
                        val parsed = runCatching {
                            gson.fromJson(json, DaumPostcodeData::class.java)
                        }.onFailure { e ->
                            Timber.e(e, "Failed to parse DaumPostcodeData")
                        }.getOrNull() ?: return

                        mainHandler.post { onSelected(parsed) }
                    }
                },
                BRIDGE_NAME,
            )

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?,
                ) {
                    super.onReceivedError(view, request, error)
                    val url = request?.url?.toString().orEmpty()
                    if (request?.isForMainFrame == true || url.contains("postcode.v2.js")) {
                        mainHandler.post { onError() }
                    }
                }
            }
        }
    }

    fun loadPostcodePage(webView: WebView) {
        webView.loadDataWithBaseURL(
            BASE_URL,
            POSTCODE_HTML,
            "text/html",
            "UTF-8",
            null,
        )
    }
}

/**
 * Compose 화면(`ProfileEditDialogFragment`)용 다음(카카오) 우편번호 검색 다이얼로그.
 */
@Composable
fun DaumPostcodePopupDialog(
    onAddressSelected: (DaumPostcodeData) -> Unit,
    onDismiss: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                destroy()
            }
            webViewRef = null
        }
    }

    ComposeDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        TBusSurface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(540.dp),
            shape = TBusTheme.shapes.cardLarge,
            color = TBusTheme.colors.surfaceRaised,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = TBusTheme.spacing.xl,
                            top = TBusTheme.spacing.md,
                            end = TBusTheme.spacing.sm,
                            bottom = TBusTheme.spacing.md,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TBusText(
                        text = "주소 검색",
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    AndroidView(
                        factory = { ctx ->
                            DaumPostcodeWebViewSupport.createConfiguredWebView(
                                context = ctx,
                                onReady = {
                                    isLoading = false
                                    hasError = false
                                },
                                onError = {
                                    isLoading = false
                                    hasError = true
                                },
                                onSelected = { selected ->
                                    onAddressSelected(selected)
                                },
                            ).also { wv ->
                                webViewRef = wv
                                DaumPostcodeWebViewSupport.loadPostcodePage(wv)
                            }
                        },
                        update = { wv ->
                            if (reloadTrigger > 0 && hasError) {
                                isLoading = true
                                hasError = false
                                DaumPostcodeWebViewSupport.loadPostcodePage(wv)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (isLoading && !hasError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TBusTheme.colors.surfaceRaised),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = TBusTheme.colors.route,
                                strokeWidth = 3.dp,
                            )
                        }
                    }

                    if (hasError) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TBusTheme.colors.surfaceRaised)
                                .padding(TBusTheme.spacing.xl),
                            verticalArrangement = Arrangement.spacedBy(
                                TBusTheme.spacing.md,
                                Alignment.CenterVertically,
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            TBusText(
                                text = "주소 검색 화면을 불러오지 못했습니다.\n네트워크 연결을 확인해주세요.",
                                variant = TBusTextVariant.Body,
                                color = TBusTheme.colors.contentMuted,
                            )
                            TBusButton(
                                text = "다시 시도",
                                onClick = {
                                    reloadTrigger++
                                    webViewRef?.let { wv ->
                                        isLoading = true
                                        hasError = false
                                        DaumPostcodeWebViewSupport.loadPostcodePage(wv)
                                    }
                                },
                                variant = TBusButtonVariant.Primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 레거시 `android.app.Activity` / `DialogFragment` (`RegisterCoFragment`)용 우편번호 검색 다이얼로그.
 */
class DaumPostcodeDialogFragment : DialogFragment() {

    fun interface OnAddressSelectedListener {
        fun onAddressSelected(data: DaumPostcodeData)
    }

    private var listener: OnAddressSelectedListener? = null
    private var webView: WebView? = null

    fun setOnAddressSelectedListener(listener: OnAddressSelectedListener?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val ctx = activity
        val density = resources.displayMetrics.density

        val rootCard = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * density
                setColor(AndroidColor.WHITE)
            }
            clipToOutline = true
        }

        // 상단 헤더 ("주소 검색" + 닫기 버튼)
        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val hPad = (18 * density).roundToInt()
            val vPad = (10 * density).roundToInt()
            setPadding(hPad, vPad, (10 * density).roundToInt(), vPad)
        }

        val titleView = TextView(ctx).apply {
            text = "주소 검색"
            setTextColor(AndroidColor.parseColor("#191F28"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeBtn = ImageButton(ctx).apply {
            setImageResource(R.drawable.xbutton_black)
            setBackgroundColor(AndroidColor.TRANSPARENT)
            val size = (36 * density).roundToInt()
            val pad = (8 * density).roundToInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = LinearLayout.LayoutParams(size, size)
            setOnClickListener { dismissAllowingStateLoss() }
        }

        header.addView(titleView)
        header.addView(closeBtn)

        val divider = View(ctx).apply {
            setBackgroundColor(AndroidColor.parseColor("#E5E8EB"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (1 * density).roundToInt().coerceAtLeast(1),
            )
        }

        // 본문 컨테이너 (WebView + 로딩바 + 에러 재시도 레이어)
        val contentFrame = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (460 * density).roundToInt(),
            )
        }

        val progressOverlay = FrameLayout(ctx).apply {
            setBackgroundColor(AndroidColor.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            addView(
                ProgressBar(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    )
                },
            )
        }

        val errorOverlay = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            visibility = View.GONE
            setBackgroundColor(AndroidColor.WHITE)
            val pad = (24 * density).roundToInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

        val errorText = TextView(ctx).apply {
            text = "주소 검색 화면을 불러오지 못했습니다.\n네트워크 연결을 확인해주세요."
            gravity = Gravity.CENTER
            setTextColor(AndroidColor.parseColor("#4E5968"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }

        val retryBtn = Button(ctx).apply {
            text = "다시 시도"
            val topMargin = (14 * density).roundToInt()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                this.topMargin = topMargin
            }
            setOnClickListener {
                errorOverlay.visibility = View.GONE
                progressOverlay.visibility = View.VISIBLE
                webView?.let { DaumPostcodeWebViewSupport.loadPostcodePage(it) }
            }
        }

        errorOverlay.addView(errorText)
        errorOverlay.addView(retryBtn)

        val configuredWebView = DaumPostcodeWebViewSupport.createConfiguredWebView(
            context = ctx,
            onReady = {
                progressOverlay.visibility = View.GONE
                errorOverlay.visibility = View.GONE
            },
            onError = {
                progressOverlay.visibility = View.GONE
                errorOverlay.visibility = View.VISIBLE
            },
            onSelected = { selectedData ->
                listener?.onAddressSelected(selectedData)
                dismissAllowingStateLoss()
            },
        )
        webView = configuredWebView

        contentFrame.addView(configuredWebView)
        contentFrame.addView(progressOverlay)
        contentFrame.addView(errorOverlay)

        rootCard.addView(header)
        rootCard.addView(divider)
        rootCard.addView(contentFrame)

        DaumPostcodeWebViewSupport.loadPostcodePage(configuredWebView)
        return rootCard
    }

    override fun onStart() {
        super.onStart()
        val dlg: Dialog = dialog ?: return
        val window = dlg.window ?: return
        val act = activity ?: return

        window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        window.setGravity(Gravity.CENTER)

        val bounds = Rect()
        act.window.decorView.getWindowVisibleDisplayFrame(bounds)
        val density = resources.displayMetrics.density
        val width = min(
            (bounds.width() * 0.94f).roundToInt(),
            (560 * density).roundToInt(),
        )
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        webView?.apply {
            stopLoading()
            destroy()
        }
        webView = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "DaumPostcodeDialogFragment"

        @JvmStatic
        fun newInstance(): DaumPostcodeDialogFragment {
            return DaumPostcodeDialogFragment()
        }
    }
}
