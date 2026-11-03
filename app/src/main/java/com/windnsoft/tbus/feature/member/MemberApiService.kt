package com.windnsoft.tbus.feature.member

import com.google.gson.GsonBuilder
import com.windnsoft.tbus.Common.GlobalVars
import com.windnsoft.tbus.Http.ResponseMessageEntity
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @FormUrlEncoded
    @POST(GlobalVars.PathUpdateMemberUrl)
    suspend fun updateMember(
        @Field("user_json") userJson: String,
    ): ResponseMessageEntity

    @Multipart
    @POST(GlobalVars.PathUpdateProfilePictureUrl)
    suspend fun updateProfilePicture(
        @Part("uid") uid: RequestBody,
        @Part file: MultipartBody.Part,
    ): ResponseMessageEntity

    @Multipart
    @POST(GlobalVars.PathInsertUserPhotoUrl)
    suspend fun insertUserPhoto(
        @Part("uid") uid: RequestBody,
        @Part file: MultipartBody.Part,
    ): ResponseMessageEntity

    @FormUrlEncoded
    @POST(GlobalVars.PathRemoveUserPhotoUrl)
    suspend fun removeUserPhoto(
        @Field("uid") uid: Int,
        @Field("pos") pos: Int,
    ): ResponseMessageEntity

    @FormUrlEncoded
    @POST(GlobalVars.PathGetLatLngListUrl)
    suspend fun getLatLngListRaw(
        @Field("token") token: String = "1",
    ): ResponseBody

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
