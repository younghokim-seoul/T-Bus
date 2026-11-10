package com.windnsoft.tbus.feature.board

import com.google.gson.GsonBuilder
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Http.ResponseMessageEntity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface BoardApiService {

    @FormUrlEncoded
    @POST(GlobalVars.PathBoardListPaging)
    suspend fun getBoardListPaging(
        @Field("bid") bid: Int,
        @Field("pid") pid: Int,
    ): ResponseMessageEntity

    @FormUrlEncoded
    @POST(GlobalVars.PathBoardMyList)
    suspend fun getMyBoardList(
        @Field("uid") uid: Int,
    ): ResponseMessageEntity

    @FormUrlEncoded
    @POST(GlobalVars.PathNoticeList)
    suspend fun getNoticeList(
        @Field("bid") bid: Int,
    ): ResponseMessageEntity

    @FormUrlEncoded
    @POST(GlobalVars.PathAdvList)
    suspend fun getAdvList(
        @Field("bid") bid: Int,
    ): ResponseMessageEntity

    @POST(GlobalVars.PathBusEstimateList)
    suspend fun getBusEstimateList(): ResponseMessageEntity

    companion object {
        private val gson by lazy {
            GsonBuilder()
                .setLenient()
                .create()
        }

        val instance: BoardApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl("${GlobalVars.Protocol}://${GlobalVars.Domain}/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(BoardApiService::class.java)
        }
    }
}
