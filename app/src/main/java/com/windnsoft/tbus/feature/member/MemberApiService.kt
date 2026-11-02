package com.windnsoft.tbus.feature.member

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

interface MemberApiService {

    @FormUrlEncoded
    @POST(GlobalVars.PathUserListUrl)
    suspend fun getUserList(
        @Field("type") type: Int,
        @Field("page") page: Int,
        @Field("limit") limit: Int,
    ): ResponseMessageEntity

    @FormUrlEncoded
    @POST(GlobalVars.PathUserInfoUrl)
    suspend fun getUserInfo(
        @Field("uid") uid: Int,
    ): ResponseMessageEntity

    companion object {
        private val gson by lazy {
            GsonBuilder()
                .setLenient()
                .create()
        }

        val instance: MemberApiService by lazy {
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
                .create(MemberApiService::class.java)
        }
    }
}
