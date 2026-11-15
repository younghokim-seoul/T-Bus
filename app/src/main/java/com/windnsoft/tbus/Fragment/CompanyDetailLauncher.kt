package com.windnsoft.tbus.Fragment

import android.app.Activity
import android.app.DialogFragment
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.windnsoft.tbus.Entities.UserEntity
import com.windnsoft.tbus.Http.ResponseCodes
import com.windnsoft.tbus.feature.member.MemberApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.StringReader

/**
 * 광고 배너 등에서 업체 UID로 상세 정보(`/servlet/users/getInfo`)를 조회한 뒤
 * [CompanyDetailFragment]를 표시하는 공용 헬퍼.
 */
object CompanyDetailLauncher {
    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    fun openByUid(
        activity: Activity,
        scope: CoroutineScope,
        uid: Int,
    ): Job? {
        if (uid <= 0) {
            Toast.makeText(activity, "업체 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return null
        }
        if (activity.isFinishing || activity.isDestroyed) return null
        val fragmentManager = activity.fragmentManager
        if (fragmentManager.findFragmentByTag(CompanyDetailFragment.TAG) != null) return null

        return scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    MemberApiService.instance.getUserInfo(uid)
                }
                Timber.d("getUserInfo(uid=%d) for CompanyDetail: code=%d", uid, response.code)

                if (activity.isFinishing || activity.isDestroyed) return@launch

                if (response.code == ResponseCodes.CodeOk && !response.msg.isNullOrBlank()) {
                    val user = parseUserEntity(response.msg)
                    if (user != null) {
                        if (!fragmentManager.isStateSaved &&
                            fragmentManager.findFragmentByTag(CompanyDetailFragment.TAG) == null
                        ) {
                            val fragment = CompanyDetailFragment.newInstance(user)
                            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
                            fragment.show(fragmentManager, CompanyDetailFragment.TAG)
                        }
                    } else {
                        Toast.makeText(activity, "업체 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "업체 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to open CompanyDetailFragment for uid=$uid")
                if (!activity.isFinishing && !activity.isDestroyed) {
                    Toast.makeText(activity, "업체 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseUserEntity(rawJson: String): UserEntity? {
        val reader = JsonReader(StringReader(rawJson)).apply {
            isLenient = true
        }
        val user = gson.fromJson<UserEntity>(reader, UserEntity::class.java) ?: return null
        if (user.photoList.isNullOrEmpty() && !user.photo.isNullOrBlank()) {
            val parsedPhotos = runCatching {
                val listType = object : TypeToken<List<String>>() {}.type
                val photoReader = JsonReader(StringReader(user.photo)).apply {
                    isLenient = true
                }
                gson.fromJson<List<String>>(photoReader, listType)
                    ?.filter { it.isNotBlank() }
                    .orEmpty()
            }.getOrDefault(emptyList())
            if (parsedPhotos.isNotEmpty()) {
                user.photoList = ArrayList(parsedPhotos)
            }
        }
        return user
    }
}
