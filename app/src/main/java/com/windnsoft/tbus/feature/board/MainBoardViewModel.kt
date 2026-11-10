package com.windnsoft.tbus.feature.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.windnsoft.tbus.Common.SharedPreferencesCache
import com.windnsoft.tbus.Entities.AdvEntity
import com.windnsoft.tbus.Entities.BoardIDs
import com.windnsoft.tbus.Entities.BoardItemEntity
import com.windnsoft.tbus.Entities.BusEstimateEntity
import com.windnsoft.tbus.Entities.NoticeEntity
import com.windnsoft.tbus.Http.ResponseCodes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.StringReader

enum class MainBoardTab(
    val bid: Int,
    val label: String,
) {
    Free(bid = BoardIDs.Free, label = "게시판"),
    Bus(bid = BoardIDs.Bus, label = "관광버스"),
    Estimate(bid = BoardIDs.Estimate, label = "버스견적"),
    Traction(bid = BoardIDs.Traction, label = "긴급견인"),
    My(bid = BoardIDs.My, label = "내가쓴글");

    companion object {
        fun fromBid(bid: Int): MainBoardTab {
            return entries.firstOrNull { it.bid == bid } ?: Free
        }
    }
}

sealed interface EstimateListUiState {
    data object Loading : EstimateListUiState
    data class Success(val items: List<BusEstimateEntity>) : EstimateListUiState
    data class Error(val message: String) : EstimateListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainBoardViewModel : ViewModel() {

    private val apiService = BoardApiService.instance
    private val gson = GsonBuilder().setLenient().create()

    private val _selectedTab = MutableStateFlow(MainBoardTab.Free)
    val selectedTab: StateFlow<MainBoardTab> = _selectedTab.asStateFlow()

    private val _refreshVersion = MutableStateFlow(0)

    private val _notices = MutableStateFlow<List<NoticeEntity>>(emptyList())
    val notices: StateFlow<List<NoticeEntity>> = _notices.asStateFlow()

    private val _adItems = MutableStateFlow<List<AdvEntity>>(emptyList())
    val adItems: StateFlow<List<AdvEntity>> = _adItems.asStateFlow()

    private val _estimateUiState = MutableStateFlow<EstimateListUiState>(EstimateListUiState.Loading)
    val estimateUiState: StateFlow<EstimateListUiState> = _estimateUiState.asStateFlow()

    private var noticeJob: Job? = null
    private var advJob: Job? = null
    private var estimateJob: Job? = null

    val pagedBoardItems: Flow<PagingData<BoardItemEntity>> = combine(
        _selectedTab,
        _refreshVersion,
    ) { tab, _ ->
        tab
    }
        .flatMapLatest { tab ->
            if (tab == MainBoardTab.Estimate) {
                flowOf(PagingData.empty())
            } else {
                val uid = SharedPreferencesCache.getCurrentUser()?.id ?: 0
                Pager(
                    config = PagingConfig(
                        pageSize = BoardPagingSource.DEFAULT_PAGE_SIZE,
                        initialLoadSize = BoardPagingSource.DEFAULT_PAGE_SIZE,
                        prefetchDistance = 3,
                        enablePlaceholders = false,
                    ),
                    pagingSourceFactory = {
                        BoardPagingSource(
                            apiService = apiService,
                            gson = gson,
                            bid = tab.bid,
                            uid = uid,
                            pageSize = BoardPagingSource.DEFAULT_PAGE_SIZE,
                        )
                    },
                ).flow
            }
        }
        .cachedIn(viewModelScope)

    init {
        loadNotices(BoardIDs.Free)
        loadAdvList(bid = 1)
    }

    fun loadAdvList(bid: Int = 1) {
        advJob?.cancel()
        advJob = viewModelScope.launch {
            try {
                val response = apiService.getAdvList(bid = bid)
                if (response.code == ResponseCodes.CodeOk && !response.msg.isNullOrBlank()) {
                    val listType = object : TypeToken<List<AdvEntity>>() {}.type
                    val reader = JsonReader(StringReader(response.msg)).apply {
                        isLenient = true
                    }
                    val parsed = gson.fromJson<List<AdvEntity>>(reader, listType)
                        ?.filter { !it.photo.isNullOrBlank() }
                        ?: emptyList()
                    _adItems.value = parsed
                } else {
                    _adItems.value = emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load adv list for bid=$bid")
                _adItems.value = emptyList()
            }
        }
    }

    fun selectTab(tab: MainBoardTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
            when (tab) {
                MainBoardTab.Free -> loadNotices(BoardIDs.Free)
                MainBoardTab.Estimate -> loadBusEstimateList()
                else -> Unit
            }
        }
    }

    fun selectTabByBid(bid: Int) {
        val target = MainBoardTab.fromBid(bid)
        if (_selectedTab.value == target) {
            refreshCurrentTab()
        } else {
            selectTab(target)
        }
    }

    fun refreshCurrentTab() {
        val current = _selectedTab.value
        when (current) {
            MainBoardTab.Estimate -> loadBusEstimateList()
            MainBoardTab.Free -> {
                loadNotices(BoardIDs.Free)
                _refreshVersion.value += 1
            }
            else -> {
                _refreshVersion.value += 1
            }
        }
    }

    fun loadNotices(bid: Int = BoardIDs.Free) {
        noticeJob?.cancel()
        noticeJob = viewModelScope.launch {
            try {
                val response = apiService.getNoticeList(bid = bid)
                if (response.code == ResponseCodes.CodeOk && !response.msg.isNullOrBlank()) {
                    val listType = object : TypeToken<List<NoticeEntity>>() {}.type
                    val reader = JsonReader(StringReader(response.msg)).apply {
                        isLenient = true
                    }
                    val parsed = gson.fromJson<List<NoticeEntity>>(reader, listType) ?: emptyList()
                    _notices.value = parsed
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load notice list for bid=$bid")
            }
        }
    }

    fun loadBusEstimateList() {
        estimateJob?.cancel()
        _estimateUiState.value = EstimateListUiState.Loading
        estimateJob = viewModelScope.launch {
            try {
                val response = apiService.getBusEstimateList()
                if (response.code != ResponseCodes.CodeOk) {
                    _estimateUiState.value = EstimateListUiState.Error(
                        response.msg ?: "버스 견적 목록을 불러오지 못했습니다."
                    )
                    return@launch
                }
                val rawMsg = response.msg
                val items: List<BusEstimateEntity> = if (rawMsg.isNullOrBlank()) {
                    emptyList()
                } else {
                    val listType = object : TypeToken<List<BusEstimateEntity>>() {}.type
                    val reader = JsonReader(StringReader(rawMsg)).apply {
                        isLenient = true
                    }
                    gson.fromJson<List<BusEstimateEntity>>(reader, listType) ?: emptyList()
                }
                _estimateUiState.value = EstimateListUiState.Success(items)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load bus estimate list")
                _estimateUiState.value = EstimateListUiState.Error("버스 견적 목록을 불러오지 못했습니다.")
            }
        }
    }
}
