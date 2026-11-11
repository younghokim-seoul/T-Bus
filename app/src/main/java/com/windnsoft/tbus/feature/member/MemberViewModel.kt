package com.windnsoft.tbus.feature.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.Entities.UserTypes
import com.windnsoft.tbus.Http.ResponseCodes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.StringReader

enum class MemberTabType(
    val typeCode: Int,
    val label: String,
) {
    Bus(typeCode = UserTypes.Bus, label = "버스회원"),
    Company(typeCode = UserTypes.Co, label = "업체회원"),
}

data class MemberDetailUiState(
    val member: UserEntity,
    val isLoadingDetail: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MemberViewModel : ViewModel() {

    private val apiService = MemberApiService.instance
    private val gson = GsonBuilder().setLenient().create()

    private val _selectedTab = MutableStateFlow(MemberTabType.Bus)
    val selectedTab: StateFlow<MemberTabType> = _selectedTab.asStateFlow()

    private val _searchInput = MutableStateFlow("")
    val searchInput: StateFlow<String> = _searchInput.asStateFlow()

    private val _appliedSearchQuery = MutableStateFlow("")
    val appliedSearchQuery: StateFlow<String> = _appliedSearchQuery.asStateFlow()

    private val _selectedMemberDetail = MutableStateFlow<MemberDetailUiState?>(null)
    val selectedMemberDetail: StateFlow<MemberDetailUiState?> = _selectedMemberDetail.asStateFlow()

    private var detailJob: Job? = null

    val pagedMembers: Flow<PagingData<UserEntity>> = combine(
        _selectedTab,
        _appliedSearchQuery,
    ) { tab, query ->
        tab to query.trim()
    }
        .distinctUntilChanged()
        .flatMapLatest { (tab, keyword) ->
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_LIMIT,
                    initialLoadSize = PAGE_LIMIT,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    MemberPagingSource(
                        apiService = apiService,
                        gson = gson,
                        userType = tab.typeCode,
                        keyword = keyword,
                        pageSize = PAGE_LIMIT,
                    )
                },
            ).flow
        }
        .cachedIn(viewModelScope)

    fun selectTab(tab: MemberTabType) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
        }
    }

    fun onSearchInputChange(value: String) {
        _searchInput.value = value
        if (value.isBlank() && _appliedSearchQuery.value.isNotEmpty()) {
            _appliedSearchQuery.value = ""
        }
    }

    fun submitSearch() {
        _appliedSearchQuery.value = _searchInput.value.trim()
    }

    fun openMemberDetail(member: UserEntity) {
        detailJob?.cancel()
        _selectedMemberDetail.value = MemberDetailUiState(
            member = member,
            isLoadingDetail = true,
        )
        detailJob = viewModelScope.launch {
            try {
                val response = apiService.getUserInfo(member.id)
                if (response.code == ResponseCodes.CodeOk && !response.msg.isNullOrBlank()) {
                    val reader = JsonReader(StringReader(response.msg)).apply {
                        isLenient = true
                    }
                    val detailUser = gson.fromJson<UserEntity>(reader, UserEntity::class.java)
                    if (detailUser != null) {
                        if (!detailUser.photo.isNullOrBlank()) {
                            val listType = object : TypeToken<List<String>>() {}.type
                            val photoReader = JsonReader(StringReader(detailUser.photo)).apply {
                                isLenient = true
                            }
                            detailUser.photoList = gson.fromJson(photoReader, listType)
                        }
                        if (detailUser.name.isNullOrBlank()) detailUser.name = member.name
                        if (detailUser.company_name.isNullOrBlank()) detailUser.company_name = member.company_name
                        if (detailUser.tel.isNullOrBlank()) detailUser.tel = member.tel
                        _selectedMemberDetail.value = MemberDetailUiState(
                            member = detailUser,
                            isLoadingDetail = false,
                        )
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load user detail for uid=${member.id}")
            }
            _selectedMemberDetail.value = _selectedMemberDetail.value?.copy(
                isLoadingDetail = false,
            )
        }
    }

    fun dismissMemberDetail() {
        detailJob?.cancel()
        _selectedMemberDetail.value = null
    }

    companion object {
        const val PAGE_LIMIT = 20
    }
}
