package com.windnsoft.tbus.Fragment

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Entities.AdvEntity
import com.windnsoft.tbus.General.WsUtils
import com.windnsoft.tbus.Http.ResponseCodes
import com.windnsoft.tbus.feature.board.BoardApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.StringReader

/**
 * XML 화면(CoListActivity, InfoListActivity 등)에서 공용으로 사용할 수 있는
 * n + 2 구조 무한 루프 & 5초 자동 슬라이드 광고 배너 커스텀 뷰
 */
class AdBannerSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private val layoutManager: LinearLayoutManager
    private val snapHelper: PagerSnapHelper
    private val adapter = BannerAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var loadJob: Job? = null
    private val gson = GsonBuilder().setLenient().create()

    private var realCount: Int = 0

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (realCount < 2 || !isAttachedToWindow || visibility != View.VISIBLE) return
            val currentPos = getCurrentSnappedPosition()
            if (currentPos != RecyclerView.NO_POSITION) {
                val nextPos = currentPos + 1
                if (nextPos < adapter.itemCount) {
                    recyclerView.smoothScrollToPosition(nextPos)
                }
            }
            mainHandler.postDelayed(this, AUTO_SCROLL_INTERVAL_MS)
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        visibility = View.GONE

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
            )
            this.layoutManager = this@AdBannerSliderView.layoutManager
            this.adapter = this@AdBannerSliderView.adapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        stopAutoScroll()
                    }

                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (realCount >= 2) {
                            val pos = getCurrentSnappedPosition()
                            when (pos) {
                                0 -> recyclerView.scrollToPosition(realCount)
                                realCount + 1 -> recyclerView.scrollToPosition(1)
                            }
                            startAutoScroll()
                        }
                    }
                }
            }
        })

        addView(recyclerView)
    }

    fun loadAds(bid: Int) {
        loadJob?.cancel()
        stopAutoScroll()
        loadJob = scope.launch {
            try {
                val response = BoardApiService.instance.getAdvList(bid = bid)
                if (response.code == ResponseCodes.CodeOk && !response.msg.isNullOrBlank()) {
                    val listType = object : TypeToken<List<AdvEntity>>() {}.type
                    val reader = JsonReader(StringReader(response.msg)).apply {
                        isLenient = true
                    }
                    val ads = gson.fromJson<List<AdvEntity>>(reader, listType)
                        ?.filter { !it.photo.isNullOrBlank() }
                        ?: emptyList()
                    setAdItems(ads)
                } else {
                    setAdItems(emptyList())
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load adv list for bid=$bid")
                setAdItems(emptyList())
            }
        }
    }

    fun setAdItems(ads: List<AdvEntity>) {
        stopAutoScroll()
        if (ads.isEmpty()) {
            realCount = 0
            adapter.submitList(emptyList())
            visibility = View.GONE
            return
        }

        val screenSize = WsUtils.getDeviceScreenSize(context)
        val bannerHeight = (screenSize.x / BANNER_ASPECT_RATIO).toInt()
        val lp = layoutParams
        if (lp != null) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = bannerHeight
            layoutParams = lp
        }

        realCount = ads.size
        val extendedList = if (realCount == 1) {
            ads
        } else {
            buildList(realCount + 2) {
                add(ads.last())
                addAll(ads)
                add(ads.first())
            }
        }

        visibility = View.VISIBLE
        adapter.submitList(extendedList)

        if (realCount >= 2) {
            recyclerView.scrollToPosition(1)
            startAutoScroll()
        } else {
            recyclerView.scrollToPosition(0)
        }
    }

    private fun getCurrentSnappedPosition(): Int {
        val snapView = snapHelper.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

    private fun startAutoScroll() {
        mainHandler.removeCallbacks(autoScrollRunnable)
        if (realCount >= 2 && isAttachedToWindow && visibility == View.VISIBLE) {
            mainHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS)
        }
    }

    private fun stopAutoScroll() {
        mainHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                stopAutoScroll()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    startAutoScroll()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAutoScroll()
    }

    override fun onDetachedFromWindow() {
        stopAutoScroll()
        scope.coroutineContext.cancelChildren()
        super.onDetachedFromWindow()
    }

    private class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {
        private var items: List<AdvEntity> = emptyList()

        fun submitList(newItems: List<AdvEntity>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                scaleType = ImageView.ScaleType.FIT_XY
            }
            return BannerViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            val item = items[position]
            val url = GlobalVars.GetAdFullPath(item.photo)
            Glide.with(holder.imageView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView)
        }

        override fun getItemCount(): Int = items.size

        class BannerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }

    companion object {
        private const val AUTO_SCROLL_INTERVAL_MS = 5_000L
        private const val BANNER_ASPECT_RATIO = 2.1f
    }
}
