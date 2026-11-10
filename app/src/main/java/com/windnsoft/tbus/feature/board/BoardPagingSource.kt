package com.windnsoft.tbus.feature.board

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.windnsoft.tbus.Entities.BoardIDs
import com.windnsoft.tbus.Entities.BoardItemEntity
import com.windnsoft.tbus.General.WsUtils
import com.windnsoft.tbus.Http.ResponseCodes
import timber.log.Timber
import java.io.StringReader
import java.text.Collator
import java.util.Collections

class BoardPagingSource(
    private val apiService: BoardApiService,
    private val gson: Gson,
    private val bid: Int,
    private val uid: Int,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) : PagingSource<Int, BoardItemEntity>() {

    override fun getRefreshKey(state: PagingState<Int, BoardItemEntity>): Int? {
        if (bid == BoardIDs.My) return STARTING_PAGE_INDEX
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BoardItemEntity> {
        val currentPage = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = if (bid == BoardIDs.My) {
                apiService.getMyBoardList(uid = uid)
            } else {
                apiService.getBoardListPaging(
                    bid = bid,
                    pid = currentPage,
                )
            }

            if (response.code != ResponseCodes.CodeOk) {
                return LoadResult.Error(
                    IllegalStateException("서버 응답 오류 (code=${response.code})")
                )
            }

            val rawMsg = response.msg
            val entityList: MutableList<BoardItemEntity> = if (rawMsg.isNullOrBlank()) {
                mutableListOf()
            } else {
                val listType = object : TypeToken<MutableList<BoardItemEntity>>() {}.type
                val reader = JsonReader(StringReader(rawMsg)).apply {
                    isLenient = true
                }
                gson.fromJson<MutableList<BoardItemEntity>>(reader, listType) ?: mutableListOf()
            }

            val photoListType = object : TypeToken<List<String>>() {}.type
            for (entity in entityList) {
                if (!entity.photos.isNullOrBlank()) {
                    try {
                        val photoReader = JsonReader(StringReader(entity.photos)).apply {
                            isLenient = true
                        }
                        entity.photo = gson.fromJson<List<String>>(photoReader, photoListType) ?: emptyList()
                    } catch (e: Exception) {
                        entity.photo = emptyList()
                    }
                }
                if (entity.time != null) {
                    entity.time = WsUtils.formatBoardDate(entity.time)
                }
            }

            if (bid == BoardIDs.My) {
                val collator = Collator.getInstance()
                Collections.sort(entityList) { a, b ->
                    collator.compare(b.time ?: "", a.time ?: "")
                }
                return LoadResult.Page(
                    data = entityList,
                    prevKey = null,
                    nextKey = null,
                )
            }

            val nextKey = if (entityList.size < pageSize) {
                null
            } else {
                currentPage + 1
            }

            LoadResult.Page(
                data = entityList,
                prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to load board list for bid=$bid, pid=$currentPage")
            LoadResult.Error(e)
        }
    }

    companion object {
        const val STARTING_PAGE_INDEX = 0
        const val DEFAULT_PAGE_SIZE = 15
    }
}
