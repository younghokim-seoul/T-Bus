package com.windnsoft.tbus.designsystem.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Entities.AdvEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * 공용 광고 배너 슬라이더 컴포넌트 (Compose)
 *
 * - 광고 목록이 비어있으면 영역을 렌더링하지 않음 (GONE)
 * - 광고가 2개 이상이면 앞뒤에 더미 노드를 추가한 (n + 2) 구조로 좌우 무한 루프 스크롤 지원
 *   [last, item_0, item_1, ..., item_{n-1}, first]
 * - 사용자가 배너를 터치/홀드하거나 좌우로 스와이프하는 동안 자동 슬라이드 일시정지,
 *   손을 떼면 그 시점부터 다시 5초 카운트다운 후 다음 배너로 이동
 */
@Composable
fun TBusAdBannerPager(
    ads: List<AdvEntity>,
    modifier: Modifier = Modifier,
    autoScrollIntervalMs: Long = 2_000L,
    bannerAspectRatio: Float = 2.1f,
) {
    val validAds = remember(ads) {
        ads.filter { !it.photo.isNullOrBlank() }
    }

    if (validAds.isEmpty()) {
        return
    }

    val realCount = validAds.size

    if (realCount == 1) {
        val singleUrl = remember(validAds[0].photo) {
            GlobalVars.GetAdFullPath(validAds[0].photo)
        }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(bannerAspectRatio)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AdBannerImage(imageUrl = singleUrl)
        }
        return
    }

    // n + 2 구조: [맨 끝 아이템 복제, 실제 0 .. n-1 아이템, 맨 앞 아이템 복제]
    val extendedAds = remember(validAds) {
        buildList(realCount + 2) {
            add(validAds.last())
            addAll(validAds)
            add(validAds.first())
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { extendedAds.size },
    )

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var isTouched by remember { mutableStateOf(false) }
    val isUserInteracting = isTouched || isDragged

    // 사용자 스와이프로 양 끝 더미 페이지(0 또는 n + 1)에 도달해 멈추면 실제 페이지(n 또는 1)로 즉시 무애니메이션 이동
    LaunchedEffect(pagerState, realCount) {
        snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
            .collect { (isScrolling, currentPage) ->
                if (!isScrolling) {
                    when (currentPage) {
                        0 -> pagerState.scrollToPage(realCount)
                        realCount + 1 -> pagerState.scrollToPage(1)
                    }
                }
            }
    }

    // 5초마다 오른쪽(다음 페이지)으로 자동 슬라이드
    // (사용자가 터치 중이거나 드래그 중이면 타이머 중단, 제스처가 끝나면 다시 5초 뒤부터 재개)
    LaunchedEffect(pagerState, realCount, isUserInteracting, autoScrollIntervalMs) {
        if (isUserInteracting) return@LaunchedEffect
        while (true) {
            delay(autoScrollIntervalMs)
            try {
                when (pagerState.currentPage) {
                    0 -> pagerState.scrollToPage(realCount)
                    realCount + 1 -> pagerState.scrollToPage(1)
                }
                val nextTarget = (pagerState.currentPage + 1).coerceAtMost(realCount + 1)
                pagerState.animateScrollToPage(
                    page = nextTarget,
                    animationSpec = tween(durationMillis = 450),
                )
                if (pagerState.currentPage == realCount + 1) {
                    pagerState.scrollToPage(1)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 스크롤 충돌 시 다음 주기에 재시도
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(bannerAspectRatio)
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isTouched = true
                    try {
                        waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    } finally {
                        isTouched = false
                    }
                }
            },
        beyondViewportPageCount = 1,
    ) { pageIndex ->
        val item = extendedAds[pageIndex]
        val imageUrl = remember(item.photo) {
            GlobalVars.GetAdFullPath(item.photo)
        }
        AdBannerImage(imageUrl = imageUrl)
    }
}

@Composable
private fun AdBannerImage(imageUrl: String) {
    GlideImage(
        imageModel = { imageUrl },
        modifier = Modifier.fillMaxSize(),
        requestOptions = {
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        },
        imageOptions = ImageOptions(
            contentScale = ContentScale.FillBounds,
            alignment = Alignment.Center,
        ),
    )
}
