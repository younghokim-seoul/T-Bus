package com.windnsoft.tbus

import android.Manifest
import android.app.DialogFragment
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.zxing.integration.android.IntentIntegrator
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.windnsoft.tbus.CallSms.CallSmsListFragment
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Common.SharedPreferencesCache
import com.windnsoft.tbus.Entities.AdvEntity
import com.windnsoft.tbus.Entities.BoardIDs
import com.windnsoft.tbus.Entities.BoardItemEntity
import com.windnsoft.tbus.Entities.BusEstimateEntity
import com.windnsoft.tbus.Entities.NoticeEntity
import com.windnsoft.tbus.Entities.UserTypes
import com.windnsoft.tbus.Fragment.AppUpdateDialogFragment
import com.windnsoft.tbus.Fragment.BoardDetailFragment
import com.windnsoft.tbus.Fragment.BoardWriteFragment
import com.windnsoft.tbus.Fragment.BusEstimateDetailFragment
import com.windnsoft.tbus.Fragment.BusEstimateWriteFragment
import com.windnsoft.tbus.Fragment.NoticeDetailFragment
import com.windnsoft.tbus.Fragment.PrivacyPolicyFragment
import com.windnsoft.tbus.Fragment.ProfileEditDialogFragment
import com.windnsoft.tbus.General.DialogDismissListener
import com.windnsoft.tbus.General.WsUtils
import com.windnsoft.tbus.Http.HttpRequestAsync
import com.windnsoft.tbus.Operator.BoardOperator
import com.windnsoft.tbus.designsystem.component.TBusAdBannerPager
import com.windnsoft.tbus.designsystem.component.TBusButton
import com.windnsoft.tbus.designsystem.component.TBusButtonVariant
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import com.windnsoft.tbus.feature.FamousCompanyActivity
import com.windnsoft.tbus.feature.board.EstimateListUiState
import com.windnsoft.tbus.feature.board.MainBoardTab
import com.windnsoft.tbus.feature.board.MainBoardViewModel
import com.windnsoft.tbus.utils.EdgeToEdgeConfig
import com.windnsoft.tbus.utils.EdgeToEdgeDefaults
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.ArrayList

class MainActivity : ComponentActivity() {

    private val boardViewModel: MainBoardViewModel by viewModels()

    private val edgeToEdgeConfig: EdgeToEdgeConfig
        get() = EdgeToEdgeDefaults.darkTransparent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdge(edgeToEdgeConfig)

        val initialBid = intent?.extras?.takeIf { it.containsKey("bid") }?.getInt("bid") ?: BoardIDs.Free
        val initialTab = MainBoardTab.fromBid(initialBid)
        Timber.d("initialBid : %d, initialTab : %s", initialBid, initialTab.name)
        boardViewModel.selectTab(initialTab)

        setContent {
            TBusTheme {
                val selectedTab by boardViewModel.selectedTab.collectAsState()
                val notices by boardViewModel.notices.collectAsState()
                val adItems by boardViewModel.adItems.collectAsState()
                val estimateUiState by boardViewModel.estimateUiState.collectAsState()
                val lazyPagingItems = boardViewModel.pagedBoardItems.collectAsLazyPagingItems()

                MainBoardScreen(
                    selectedTab = selectedTab,
                    notices = notices,
                    adItems = adItems,
                    lazyPagingItems = lazyPagingItems,
                    estimateUiState = estimateUiState,
                    onTabSelected = { tab ->
                        if (tab == MainBoardTab.Bus) {
                            val currentUser = SharedPreferencesCache.getCurrentUser()
                            if (currentUser == null || currentUser.type != UserTypes.Bus) {
                                Toast.makeText(this@MainActivity, "관광버스 회원 전용", Toast.LENGTH_SHORT).show()
                                return@MainBoardScreen
                            }
                        }
                        boardViewModel.selectTab(tab)
                    },
                    onRetryEstimate = { boardViewModel.loadBusEstimateList() },
                    onNoticeClick = { notice ->
                        openNoticeView(selectedTab.bid, notice)
                    },
                    onBoardItemClick = { item ->
                        openBoardItemView(selectedTab.bid, item)
                    },
                    onEstimateItemClick = { estimate ->
                        openEstimateItemView(BoardIDs.Estimate, estimate)
                    },
                    onFamousCompanyClick = {
                        startActivity(Intent(this@MainActivity, FamousCompanyActivity::class.java))
                    },
                    onQrClick = {
                        openQrScanner()
                    },
                    onInviteClick = {
                        openCallSmsFragment()
                    },
                    onWriteClick = {
                        openWrite(selectedTab.bid)
                    },
                    onProfileEditClick = {
                        openProfileEdit()
                    },
                    onPrivacyPolicyClick = {
                        openPrivacyPolicy()
                    },
                    onCloseClick = {
                        finish()
                    },
                    onCompanyClick = {
                        startActivity(Intent(this@MainActivity, CoActivity::class.java))
                        finish()
                    },
                    onInfoClick = {
                        startActivity(Intent(this@MainActivity, InfoActivity::class.java))
                        finish()
                    },
                    onMemberClick = {
                        startActivity(Intent(this@MainActivity, MemberActivity::class.java))
                        finish()
                    },
                    onTalkieClick = {
                        WsUtils.runWalkieTalkieApp(this@MainActivity)
                    },
                )
            }
        }

        AppUpdateDialogFragment.checkAndShowIfNeeded(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val bid = intent.extras?.takeIf { it.containsKey("bid") }?.getInt("bid") ?: return
        boardViewModel.selectTabByBid(bid)
    }

    private fun applyEdgeToEdge(config: EdgeToEdgeConfig) {
        enableEdgeToEdge(
            statusBarStyle = config.statusBarStyle,
            navigationBarStyle = config.navigationBarStyle,
        )
    }

    private fun openBoardItemView(boardID: Int, entity: BoardItemEntity) {
        val targetBid = if (entity.bid > 0) entity.bid else boardID
        val fragment = BoardDetailFragment.newInstance(targetBid, entity)
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        fragment.setDismissListener(object : DialogDismissListener() {
            override fun onDismiss(dialogInterface: android.content.DialogInterface?) {
                // 상세화면 닫힘 처리
            }
        })
        fragment.show(fragmentManager, BoardDetailFragment.TAG)
    }

    private fun openNoticeView(boardID: Int, entity: NoticeEntity) {
        val targetBid = if (entity.bid > 0) entity.bid else boardID
        val fragment = NoticeDetailFragment.newInstance(targetBid, entity)
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        fragment.show(fragmentManager, NoticeDetailFragment.TAG)
    }

    private fun openEstimateItemView(boardID: Int, entity: BusEstimateEntity) {
        val fragment = BusEstimateDetailFragment.newInstance(boardID, entity)
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        fragment.setDismissListener(object : DialogDismissListener() {
            override fun onDismiss(dialogInterface: android.content.DialogInterface?) {
                boardViewModel.loadBusEstimateList()
            }
        })
        fragment.show(fragmentManager, BusEstimateDetailFragment.TAG)
    }

    fun openWrite(boardID: Int) {
        if (boardID == BoardIDs.Estimate) {
            val fragment = BusEstimateWriteFragment.newInstance()
            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
            fragment.setDismissListener(object : DialogDismissListener() {
                override fun onDismiss(dialogInterface: android.content.DialogInterface?) {
                    boardViewModel.refreshCurrentTab()
                }
            })
            fragment.show(fragmentManager, BusEstimateWriteFragment.TAG)
            return
        }

        val fragment = BoardWriteFragment.newInstance(boardID)
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        fragment.setDismissListener(object : DialogDismissListener() {
            override fun onDismiss(dialogInterface: android.content.DialogInterface?) {
                boardViewModel.refreshCurrentTab()
            }
        })
        fragment.show(fragmentManager, BoardWriteFragment.TAG)
    }

    private fun openCallSmsFragment() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    val existing = fragmentManager.findFragmentByTag(CallSmsListFragment.TAG)
                    if (existing != null) {
                        fragmentManager.beginTransaction().remove(existing).commit()
                    }

                    val fragment = CallSmsListFragment.newInstance()
                    fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
                    fragment.show(fragmentManager, CallSmsListFragment.TAG)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(
                        this@MainActivity,
                        "연락처 권한이 거부되어 초대 목록을 불러올 수 없습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            })
            .setDeniedMessage("초대할 연락처 목록을 불러오기 위해 주소록 권한이 필요합니다.\n\n[설정] > [권한]에서 권한을 허용해주세요.")
            .setPermissions(Manifest.permission.READ_CONTACTS)
            .check()
    }

    private fun openQrScanner() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    val integrator = IntentIntegrator(this@MainActivity)
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    integrator.setPrompt("QR 코드를 스캔해주세요.")
                    integrator.setBeepEnabled(true)
                    integrator.setBarcodeImageEnabled(false)
                    integrator.setOrientationLocked(false)
                    integrator.initiateScan()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(
                        this@MainActivity,
                        "카메라 권한이 거부되어 QR 스캔을 실행할 수 없습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            })
            .setDeniedMessage("QR 코드 스캔을 위해 카메라 권한이 필요합니다.\n\n[설정] > [권한]에서 권한을 허용해주세요.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val qrContent = result.contents
            if (!qrContent.isNullOrBlank()) {
                uploadQrContentToMainBoard(qrContent.trim())
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadQrContentToMainBoard(qrContent: String) {
        val currentTabBid = boardViewModel.selectedTab.value.bid
        val targetBid = if (currentTabBid != BoardIDs.Estimate && currentTabBid != BoardIDs.My) {
            currentTabBid
        } else {
            BoardIDs.Free
        }

        val user = SharedPreferencesCache.getCurrentUser()
        if (user == null || user.id <= 0) {
            Toast.makeText(this@MainActivity, "로그인 후 이용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        BoardOperator.upload(
            this@MainActivity,
            targetBid,
            qrContent,
            ArrayList<File>(),
            object : HttpRequestAsync.OnCompleteListener {
                override fun onSuccess(result: String?) {
                    Toast.makeText(this@MainActivity, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                    boardViewModel.selectTabByBid(targetBid)
                }

                override fun onFail(result: String?) {
                    Toast.makeText(this@MainActivity, "등록에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    boardViewModel.selectTabByBid(targetBid)
                }

                override fun onError(code: Int, result: String?) {
                    Toast.makeText(this@MainActivity, "등록 중 오류가 발생했습니다. ($code)", Toast.LENGTH_SHORT).show()
                    boardViewModel.selectTabByBid(targetBid)
                }
            },
        )
    }

    private fun openProfileEdit() {
        val dialog = ProfileEditDialogFragment.newInstance()
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        dialog.show(fragmentManager, ProfileEditDialogFragment.TAG)
    }

    private fun openPrivacyPolicy() {
        val dialog = PrivacyPolicyFragment()
        val bundle = Bundle().apply {
            putBoolean("one button", true)
        }
        dialog.arguments = bundle
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        dialog.show(fragmentManager, PrivacyPolicyFragment.TAG)
    }
}

@Composable
private fun MainBoardScreen(
    selectedTab: MainBoardTab,
    notices: List<NoticeEntity>,
    adItems: List<AdvEntity>,
    lazyPagingItems: LazyPagingItems<BoardItemEntity>,
    estimateUiState: EstimateListUiState,
    onTabSelected: (MainBoardTab) -> Unit,
    onRetryEstimate: () -> Unit,
    onNoticeClick: (NoticeEntity) -> Unit,
    onBoardItemClick: (BoardItemEntity) -> Unit,
    onEstimateItemClick: (BusEstimateEntity) -> Unit,
    onFamousCompanyClick: () -> Unit,
    onQrClick: () -> Unit,
    onInviteClick: () -> Unit,
    onWriteClick: () -> Unit,
    onProfileEditClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onCloseClick: () -> Unit,
    onCompanyClick: () -> Unit,
    onInfoClick: () -> Unit,
    onMemberClick: () -> Unit,
    onTalkieClick: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    val backgroundColor = when (selectedTab) {
        MainBoardTab.Free -> Color(0xFFF0F8FF)
        MainBoardTab.Bus -> Color(0xFFFFF0F0)
        MainBoardTab.Estimate -> Color(0xFFF0FFF0)
        MainBoardTab.Traction -> Color(0xFFFFFFF0)
        MainBoardTab.My -> Color(0xFFFFF0FF)
    }

    val currentUser = SharedPreferencesCache.getCurrentUser()
    val showWriteButton = when (selectedTab) {
        MainBoardTab.My -> false
        MainBoardTab.Bus -> currentUser?.type == UserTypes.Bus
        else -> true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(260.dp),
                drawerShape = RectangleShape,
                drawerContainerColor = Color.White,
            ) {
                // 드로어 상단 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3F51B5))
                        .statusBarsPadding()
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_mdpi),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(28.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // 프로필 수정
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable {
                            coroutineScope.launch {
                                drawerState.close()
                                onProfileEditClick()
                            }
                        }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "프로필 수정",
                        color = Color.Black,
                        fontSize = 16.sp,
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F7))

                // 개인정보 보호방침 및 약관
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable {
                            coroutineScope.launch {
                                drawerState.close()
                                onPrivacyPolicyClick()
                            }
                        }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "개인정보 보호방침 및 약관",
                        color = Color.Black,
                        fontSize = 16.sp,
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F7))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
        ) {
            // 1. 상단 헤더 (layout_head)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3F51B5))
                    .statusBarsPadding()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hamburger_black),
                    contentDescription = "메뉴",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 5.dp)
                        .padding(1.dp)
                        .clickable {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_mdpi),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp),
                )

                Text(
                    text = "소식",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .weight(8f)
                        .padding(start = 4.dp),
                )

                Image(
                    painter = painterResource(id = R.drawable.xbutton_black),
                    contentDescription = "닫기",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 5.dp)
                        .padding(1.dp)
                        .clickable(onClick = onCloseClick),
                )
            }

            // 2. 상단 광고 배너 (advImg, bid = 1 소식) - 견적 탭 외 표시
            if (selectedTab != MainBoardTab.Estimate) {
                TBusAdBannerPager(ads = adItems)
            }

            // 3. 5개 게시판 탭 바 (38dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MainBoardTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabBgColor = if (isSelected) Color(0xEE555599) else Color(0xFF8899DD)
                    val tabBorderColor = if (isSelected) Color.White else Color(0x88EEEEEE)
                    val tabTextColor = if (isSelected) Color.White else Color.Black

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(tabBgColor, RoundedCornerShape(5.dp))
                            .border(1.dp, tabBorderColor, RoundedCornerShape(5.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.label,
                            color = tabTextColor,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // 4. 중앙 리스트 영역 + 하단 우측 플로팅 액션 버튼들
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (selectedTab == MainBoardTab.Estimate) {
                    BusEstimateListSection(
                        uiState = estimateUiState,
                        onRetry = onRetryEstimate,
                        onItemClick = onEstimateItemClick,
                    )
                } else {
                    key(selectedTab) {
                        PagedBoardListSection(
                            selectedTab = selectedTab,
                            notices = if (selectedTab == MainBoardTab.Free) notices else emptyList(),
                            lazyPagingItems = lazyPagingItems,
                            onNoticeClick = onNoticeClick,
                            onBoardItemClick = onBoardItemClick,
                        )
                    }
                }

                // 하단 우측 액션 버튼 바 (소문난 업소, QR 코드, 친구 초대, 글쓰기)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 소문난 업소
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp, top = 5.dp, end = 58.dp, bottom = 5.dp)
                            .size(53.dp)
                            .padding(2.dp)
                            .background(Color(0xFFFBC02D), CircleShape)
                            .clip(CircleShape)
                            .clickable(onClick = onFamousCompanyClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "소문난\n업소",
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // QR 코드
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .size(53.dp)
                            .padding(2.dp)
                            .background(Color(0xFFF31010), CircleShape)
                            .clip(CircleShape)
                            .clickable(onClick = onQrClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "QR\n코드",
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // 초대 버튼
                    Image(
                        painter = painterResource(id = R.drawable.freind),
                        contentDescription = "초대",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(53.dp)
                            .clickable(onClick = onInviteClick),
                    )

                    // 글쓰기 버튼
                    if (showWriteButton) {
                        Image(
                            painter = painterResource(id = R.drawable.write),
                            contentDescription = "글쓰기",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(5.dp)
                                .size(53.dp)
                                .clickable(onClick = onWriteClick),
                        )
                    }
                }
            }

            // 5. 하단 네비게이션 바 (layout_bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .navigationBarsPadding()
                    .height(70.dp)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.news_on),
                    contentDescription = "소식",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Image(
                    painter = painterResource(id = R.drawable.company_off),
                    contentDescription = "업체",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onCompanyClick),
                )
                Image(
                    painter = painterResource(id = R.drawable.info_off),
                    contentDescription = "정보",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onInfoClick),
                )
                Image(
                    painter = painterResource(id = R.drawable.member_off),
                    contentDescription = "회원",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onMemberClick),
                )
                Image(
                    painter = painterResource(id = R.drawable.walkietalkie_off),
                    contentDescription = "무전기",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onTalkieClick),
                )
            }
        }
    }
}

@Composable
private fun PagedBoardListSection(
    selectedTab: MainBoardTab,
    notices: List<NoticeEntity>,
    lazyPagingItems: LazyPagingItems<BoardItemEntity>,
    onNoticeClick: (NoticeEntity) -> Unit,
    onBoardItemClick: (BoardItemEntity) -> Unit,
) {
    val refreshState = lazyPagingItems.loadState.refresh
    val listState = remember(selectedTab) { LazyListState() }

    LaunchedEffect(refreshState) {
        if (refreshState is LoadState.NotLoading && listState.firstVisibleItemIndex == 0) {
            listState.scrollToItem(0)
        }
    }

    when {
        refreshState is LoadState.Loading && lazyPagingItems.itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = TBusTheme.colors.route,
                )
            }
        }

        refreshState is LoadState.Error && lazyPagingItems.itemCount == 0 -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "게시글 목록을 불러오지 못했습니다.",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                    )
                    TBusButton(
                        text = "다시 시도",
                        onClick = { lazyPagingItems.retry() },
                        variant = TBusButtonVariant.Secondary,
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 76.dp),
            ) {
                // 자유게시판 상단 공지사항 헤더 (notice_header_layout / notice_header_item)
                if (notices.isNotEmpty()) {
                    items(
                        items = notices,
                        key = { notice -> "notice_${notice.id}" },
                        contentType = { "notice_header" },
                    ) { notice ->
                        NoticeHeaderItemView(
                            notice = notice,
                            onClick = { onNoticeClick(notice) },
                        )
                    }
                }

                // 페이징 게시글 목록 (pid = 0, 1, 2... 15개씩 로드)
                items(
                    count = lazyPagingItems.itemCount,
                    key = { index ->
                        val item = lazyPagingItems.peek(index)
                        if (item != null) "board_${item.bid}_${item.id}_$index" else "placeholder_$index"
                    },
                    contentType = { "board_item" },
                ) { index ->
                    val item = lazyPagingItems[index]
                    if (item != null) {
                        BoardListItemCard(
                            item = item,
                            onClick = { onBoardItemClick(item) },
                        )
                    }
                }

                // 하단 추가 페이지 로딩 표시 / 재시도 버튼
                when (lazyPagingItems.loadState.append) {
                    is LoadState.Loading -> {
                        item(key = "append_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = TBusTheme.colors.route,
                                    strokeWidth = 2.5.dp,
                                )
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item(key = "append_error") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                TBusButton(
                                    text = "더 불러오기 재시도",
                                    onClick = { lazyPagingItems.retry() },
                                    variant = TBusButtonVariant.Secondary,
                                )
                            }
                        }
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun NoticeHeaderItemView(
    notice: NoticeEntity,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 2.dp)
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(1.dp, Color(0xFF888888), RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFDDAABB), RoundedCornerShape(5.dp))
                .border(1.dp, Color.White, RoundedCornerShape(5.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "공지",
                color = Color.White,
                fontSize = 13.sp,
            )
        }

        Text(
            text = notice.title.orEmpty(),
            color = Color.Black,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(5.dp),
        )
    }
}

@Composable
private fun BoardListItemCard(
    item: BoardItemEntity,
    onClick: () -> Unit,
) {
    val profileUrl = remember(item.userProfile) {
        item.userProfile?.takeIf { it.isNotBlank() }?.let {
            GlobalVars.GetPictureFullPath(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(1.dp, Color(0xFF888888), RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(profileUrl = profileUrl)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = item.time.orEmpty(),
                        fontSize = 12.sp,
                        color = Color(0xFFA9A9A9),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.userName.orEmpty(),
                        color = Color.Black,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFD3D3D3),
        )

        Text(
            text = item.contents.orEmpty(),
            color = Color.Black,
            fontSize = 14.sp,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
        )
    }
}

@Composable
private fun BusEstimateListSection(
    uiState: EstimateListUiState,
    onRetry: () -> Unit,
    onItemClick: (BusEstimateEntity) -> Unit,
) {
    when (uiState) {
        is EstimateListUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = TBusTheme.colors.route,
                )
            }
        }

        is EstimateListUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = uiState.message,
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                    )
                    TBusButton(
                        text = "다시 시도",
                        onClick = onRetry,
                        variant = TBusButtonVariant.Secondary,
                    )
                }
            }
        }

        is EstimateListUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 76.dp),
            ) {
                items(
                    items = uiState.items,
                    key = { item -> "estimate_${item.id}" },
                ) { item ->
                    BusEstimateItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BusEstimateItemCard(
    item: BusEstimateEntity,
    onClick: () -> Unit,
) {
    val profileUrl = remember(item.userProfile) {
        item.userProfile?.takeIf { it.isNotBlank() }?.let {
            GlobalVars.GetPictureFullPath(it)
        }
    }

    val contentText = remember(item.start_time, item.arrival_time, item.start_locale, item.arrival_locale) {
        val timePart = if (
            item.start_time != null && item.start_time.length > 16 &&
            item.arrival_time != null && item.arrival_time.length > 16
        ) {
            "${item.start_time.substring(0, 16)} ~ ${item.arrival_time.substring(0, 16)}"
        } else {
            ""
        }
        "\n$timePart${item.start_locale.orEmpty()} ~ ${item.arrival_locale.orEmpty()}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(1.dp, Color(0xFF888888), RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(profileUrl = profileUrl)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title.orEmpty(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.userName.orEmpty(),
                        color = Color.Black,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFD3D3D3),
        )

        Text(
            text = contentText,
            color = Color.Black,
            fontSize = 14.sp,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
        )
    }
}

@Composable
private fun ProfileAvatar(profileUrl: String?) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (profileUrl != null) {
            GlideImage(
                imageModel = { profileUrl },
                modifier = Modifier.fillMaxSize(),
                requestOptions = {
                    RequestOptions()
                        .override(120, 120)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    requestSize = IntSize(120, 120),
                ),
                failure = {
                    Image(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                },
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
