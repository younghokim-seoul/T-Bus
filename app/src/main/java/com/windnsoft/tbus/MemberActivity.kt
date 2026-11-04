package com.windnsoft.tbus

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.General.WsUtils
import com.windnsoft.tbus.designsystem.component.TBusButton
import com.windnsoft.tbus.designsystem.component.TBusButtonVariant
import com.windnsoft.tbus.designsystem.component.TBusCard
import com.windnsoft.tbus.designsystem.component.TBusIconButton
import com.windnsoft.tbus.designsystem.component.TBusPillButton
import com.windnsoft.tbus.designsystem.component.TBusScaffold
import com.windnsoft.tbus.designsystem.component.TBusText
import com.windnsoft.tbus.designsystem.component.TBusTextField
import com.windnsoft.tbus.designsystem.component.TBusTextVariant
import com.windnsoft.tbus.designsystem.component.TBusTopAppBar
import com.windnsoft.tbus.designsystem.theme.TBusTheme
import com.windnsoft.tbus.feature.member.MemberDetailUiState
import com.windnsoft.tbus.feature.member.MemberPhotoViewerActivity
import com.windnsoft.tbus.feature.member.MemberTabType
import com.windnsoft.tbus.feature.member.MemberViewModel
import com.windnsoft.tbus.utils.EdgeToEdgeConfig
import com.windnsoft.tbus.utils.EdgeToEdgeDefaults

class MemberActivity : ComponentActivity() {

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
                val memberViewModel: MemberViewModel = viewModel()
                val selectedTab by memberViewModel.selectedTab.collectAsState()
                val searchInput by memberViewModel.searchInput.collectAsState()
                val selectedMemberDetail by memberViewModel.selectedMemberDetail.collectAsState()
                val lazyPagingItems = memberViewModel.pagedMembers.collectAsLazyPagingItems()

                MemberScreen(
                    selectedTab = selectedTab,
                    onTabSelected = memberViewModel::selectTab,
                    searchInput = searchInput,
                    onSearchInputChange = memberViewModel::onSearchInputChange,
                    onSearchSubmit = memberViewModel::submitSearch,
                    lazyPagingItems = lazyPagingItems,
                    selectedMemberDetail = selectedMemberDetail,
                    onMemberClickItem = memberViewModel::openMemberDetail,
                    onDismissMemberDetail = memberViewModel::dismissMemberDetail,
                    onCloseClick = { finish() },
                    onNewsClick = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onCompanyClick = {
                        startActivity(Intent(this, CoActivity::class.java))
                        finish()
                    },
                    onInfoClick = {
                        startActivity(Intent(this, InfoActivity::class.java))
                        finish()
                    },
                    onMemberClick = {},
                    onTalkieClick = {
                        WsUtils.runExternalAppOrGooglePlay(this, GlobalVars.WalieTaliePackageName)
                    },
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
fun MemberScreen(
    selectedTab: MemberTabType,
    onTabSelected: (MemberTabType) -> Unit,
    searchInput: String,
    onSearchInputChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    lazyPagingItems: LazyPagingItems<UserEntity>,
    selectedMemberDetail: MemberDetailUiState?,
    onMemberClickItem: (UserEntity) -> Unit,
    onDismissMemberDetail: () -> Unit,
    onCloseClick: () -> Unit,
    onNewsClick: () -> Unit,
    onCompanyClick: () -> Unit,
    onInfoClick: () -> Unit,
    onMemberClick: () -> Unit,
    onTalkieClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = selectedMemberDetail != null) {
        onDismissMemberDetail()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TBusScaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TBusTopAppBar(
                    title = "회원",
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
            bottomBar = {
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
                        painter = painterResource(id = R.drawable.news_off),
                        contentDescription = "소식",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onNewsClick),
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
                        painter = painterResource(id = R.drawable.member_on),
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
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = TBusTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.sm),
            ) {
                // 1. 상단 버스회원 / 업체회원 토글
                MemberTypeToggleBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        focusManager.clearFocus()
                        onTabSelected(tab)
                    },
                )

                // 2. 이름, 전화번호, 업체명 검색 TextField + 검색 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TBusTextField(
                        value = searchInput,
                        onValueChange = onSearchInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = "이름, 전화번호, 업체명 검색",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                onSearchSubmit()
                            },
                        ),
                    )

                    TBusButton(
                        text = "검색",
                        onClick = {
                            focusManager.clearFocus()
                            onSearchSubmit()
                        },
                        modifier = Modifier.height(54.dp),
                        variant = TBusButtonVariant.Primary,
                    )
                }

                // 3. 페이징 LazyColumn (전화번호부 카드 리스트)
                key(selectedTab) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = TBusTheme.spacing.sm),
                    ) {
                        val refreshState = lazyPagingItems.loadState.refresh
                        val isTabSwitching = lazyPagingItems.itemCount > 0 &&
                            lazyPagingItems.peek(0)?.let { it.type != 0 && it.type != selectedTab.typeCode } == true
                        val listState = remember { LazyListState() }

                        LaunchedEffect(refreshState, isTabSwitching) {
                            if (refreshState is LoadState.NotLoading && !isTabSwitching) {
                                listState.scrollToItem(0)
                            }
                        }

                        when {
                            refreshState is LoadState.Loading || isTabSwitching -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        color = TBusTheme.colors.route,
                                    )
                                }
                            }

                            refreshState is LoadState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(TBusTheme.spacing.lg),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.sm),
                                    ) {
                                        TBusText(
                                            text = "회원 목록을 불러오지 못했습니다.",
                                            variant = TBusTextVariant.Body,
                                            color = TBusTheme.colors.contentMuted,
                                        )
                                        TBusButton(
                                            text = "다시 시도",
                                            onClick = { lazyPagingItems.retry() },
                                            variant = TBusButtonVariant.Secondary,
                                        )
                                    }
                                }
                            }

                            refreshState is LoadState.NotLoading && lazyPagingItems.itemCount == 0 -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    TBusText(
                                        text = "검색된 회원이 없습니다.",
                                        variant = TBusTextVariant.Body,
                                        color = TBusTheme.colors.contentMuted,
                                    )
                                }
                            }

                            else -> {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = TBusTheme.spacing.xs),
                                    verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
                                ) {
                                    items(
                                        count = lazyPagingItems.itemCount,
                                        key = { index ->
                                            val item = lazyPagingItems.peek(index)
                                            if (item != null) "${item.type}_${item.id}" else "placeholder_$index"
                                        },
                                        contentType = { "member_card" },
                                    ) { index ->
                                        val member = lazyPagingItems[index]
                                        if (member != null) {
                                            val displayName = member.name?.takeIf { it.isNotBlank() } ?: "이름 없음"
                                            val displayCompany = member.company_name?.takeIf { it.isNotBlank() } ?: "업체 정보 없음"
                                            val profileUrl = remember(member.profile) {
                                                member.profile?.takeIf { it.isNotBlank() }?.let {
                                                    GlobalVars.GetPictureFullPath(it)
                                                }
                                            }
                                            MemberPhonebookItemCard(
                                                displayName = displayName,
                                                displayCompany = displayCompany,
                                                profileUrl = profileUrl,
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    onMemberClickItem(member)
                                                },
                                            )
                                        }
                                    }

                                    when (lazyPagingItems.loadState.append) {
                                        is LoadState.Loading -> {
                                            item(key = "append_loading") {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = TBusTheme.spacing.md),
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
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = TBusTheme.spacing.sm),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically,
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
                }
            }
        }

        // 4. 풀스크린 반투명 Dim 회원 상세 오버레이 다이얼로그
        if (selectedMemberDetail != null) {
            MemberDetailOverlay(
                detailState = selectedMemberDetail,
                onDismiss = onDismissMemberDetail,
            )
        }
    }
}

@Composable
private fun MemberTypeToggleBar(
    selectedTab: MemberTabType,
    onTabSelected: (MemberTabType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TBusTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
    ) {
        MemberTabType.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                TBusPillButton(
                    label = tab.label,
                    selected = isSelected,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun MemberPhonebookItemCard(
    displayName: String,
    displayCompany: String,
    profileUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TBusCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(
            horizontal = TBusTheme.spacing.md,
            vertical = TBusTheme.spacing.sm,
        ),
        containerColor = TBusTheme.colors.surfaceRaised,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TBusTheme.colors.line),
                contentAlignment = Alignment.Center,
            ) {
                if (profileUrl != null) {
                    GlideImage(
                        imageModel = { profileUrl },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        requestOptions = {
                            RequestOptions()
                                .override(144, 144)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                        },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            requestSize = IntSize(144, 144),
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

            Spacer(modifier = Modifier.width(TBusTheme.spacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                TBusText(
                    text = displayName,
                    variant = TBusTextVariant.Label,
                    color = TBusTheme.colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TBusText(
                    text = displayCompany,
                    variant = TBusTextVariant.Caption,
                    color = TBusTheme.colors.contentMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MemberDetailOverlay(
    detailState: MemberDetailUiState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val member = detailState.member
    val displayName = member.name?.takeIf { it.isNotBlank() } ?: "이름 없음"
    val displayCompany = member.company_name?.takeIf { it.isNotBlank() } ?: "업체 정보 없음"
    val phoneNumber = member.tel?.trim().orEmpty()
    val profileUrl = member.profile?.takeIf { it.isNotBlank() }?.let {
        GlobalVars.GetPictureFullPath(it)
    }
    val photoUrls = remember(member.photoList, member.profile) {
        val list = member.photoList
            ?.filter { it.isNotBlank() }
            ?.map { GlobalVars.GetPictureFullPath(it) }
            .orEmpty()
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TBusTheme.spacing.lg),
        ) {
            // 1. 좌측 상단 X 닫기 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TBusTheme.spacing.xs),
                contentAlignment = Alignment.CenterStart,
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(TBusTheme.spacing.sm))

            // 2. 그 아래 프로필 이미지 + 옆에 이름 / 업소명 (흰색 텍스트)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TBusTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (profileUrl != null) {
                        GlideImage(
                            imageModel = { profileUrl },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
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

                Spacer(modifier = Modifier.width(TBusTheme.spacing.md))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TBusText(
                        text = displayName,
                        variant = TBusTextVariant.Title,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TBusText(
                        text = displayCompany,
                        variant = TBusTextVariant.Body,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // 3. 그 아래 가운데 정렬 원형 아이콘 버튼 3개 (파란 전화 / 주황 문자 / 초록 연락처)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 파란색 동그라미 전화 아이콘
                MemberCircleActionButton(
                    backgroundColor = Color(0xFF2196F3),
                    icon = Icons.Default.Call,
                    contentDescription = "전화 걸기",
                    onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            WsUtils.phoneCall(context, phoneNumber)
                        } else {
                            Toast.makeText(context, "등록된 전화번호가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                )

                // 주황색 동그라미 문자 아이콘
                MemberCircleActionButton(
                    backgroundColor = Color(0xFFFF9800),
                    icon = Icons.Default.Email,
                    contentDescription = "문자 보내기",
                    onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            WsUtils.composeSms(context, phoneNumber, null)
                        } else {
                            Toast.makeText(context, "등록된 전화번호가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                )

                // 초록색 동그라미 연락처 아이콘
                MemberCircleActionButton(
                    backgroundColor = Color(0xFF4CAF50),
                    icon = Icons.Default.Person,
                    contentDescription = "연락처 저장",
                    onClick = {
                        try {
                            val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                                type = ContactsContract.RawContacts.CONTENT_TYPE
                                putExtra(ContactsContract.Intents.Insert.NAME, displayName)
                                if (phoneNumber.isNotEmpty()) {
                                    putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
                                }
                                if (!member.company_name.isNullOrBlank()) {
                                    putExtra(ContactsContract.Intents.Insert.COMPANY, member.company_name)
                                }
                            }
                            context.startActivity(contactIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "연락처 앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            }

            // 4. 그 아래 이미지 영역 (LazyColumn + 다운샘플링으로 대용량 원본 이미지 버벅임 방지)
            if (detailState.isLoadingDetail && photoUrls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = TBusTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        items = photoUrls,
                        key = { it },
                        contentType = { "detail_photo" },
                    ) { imageUrl ->
                        GlideImage(
                            imageModel = { imageUrl },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    context.startActivity(
                                        MemberPhotoViewerActivity.createIntent(context, imageUrl)
                                    )
                                },
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
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCircleActionButton(
    backgroundColor: Color,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}
