package com.windnsoft.tbus.feature.member

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.Http.ResponseCodes
import timber.log.Timber
import java.io.StringReader

class MemberPagingSource(
    private val apiService: MemberApiService,
    private val gson: Gson,
    private val userType: Int,
    private val keyword: String = "",
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) : PagingSource<Int, UserEntity>() {

    override fun getRefreshKey(state: PagingState<Int, UserEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserEntity> {
        val currentPage = params.key ?: STARTING_PAGE_INDEX
        val trimmedKeyword = keyword.trim()
        val isSearchMode = trimmedKeyword.isNotEmpty()

        return try {
            val response = if (isSearchMode) {
                apiService.searchUserList(
                    type = userType,
                    keyword = trimmedKeyword,
                )
            } else {
                apiService.getUserList(
                    type = userType,
                    page = currentPage,
                    limit = pageSize,
                )
            }

            if (response.code != ResponseCodes.CodeOk) {
                return LoadResult.Error(
                    IllegalStateException("서버 응답 오류 (code=${response.code})")
                )
            }

            val rawMsg = response.msg
            val users: List<UserEntity> = if (rawMsg.isNullOrBlank()) {
                emptyList()
            } else {
                val listType = object : TypeToken<List<UserEntity>>() {}.type
                val reader = JsonReader(StringReader(rawMsg)).apply {
                    isLenient = true
                }
                gson.fromJson<List<UserEntity>>(reader, listType) ?: emptyList()
            }

            val nextKey = if (isSearchMode || users.size < pageSize) {
                null
            } else {
                currentPage + 1
            }

            LoadResult.Page(
                data = users,
                prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to load user list for type=$userType, keyword=$trimmedKeyword, page=$currentPage")
            LoadResult.Error(e)
        }
    }

    companion object {
        const val STARTING_PAGE_INDEX = 1
        const val DEFAULT_PAGE_SIZE = 20
    }
}
