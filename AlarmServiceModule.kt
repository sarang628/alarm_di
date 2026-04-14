package com.sarang.torang.di.alarm_di

import android.util.Log
import com.sarang.torang.BuildConfig
import com.sarang.torang.api.ApiAlarm
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.data.remote.response.AlarmApiModel
import com.sarang.torang.data1.alarm.AlarmListItemUIState
import com.sarang.torang.data1.alarm.AlarmType
import com.sarang.torang.data1.alarm.AlarmUser
import com.sarang.torang.session.SessionService
import com.sarang.torang.usecase.GetAlarmUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

const val tag = "__AlarmServiceModule"
@InstallIn(SingletonComponent::class)
@Module
class AlarmServiceModule {
    @Provides
    fun provideAlarmService(
        apiAlarm: ApiAlarm,
        sessionService: SessionService,
        loggedInUserDao: LoggedInUserDao,
    ): GetAlarmUseCase {
        return object : GetAlarmUseCase {
            override suspend fun getAlarm(): List<AlarmListItemUIState> {
                var list: List<AlarmListItemUIState> = emptyList()
                try {
                    sessionService.getToken()?.let {
                        list = apiAlarm.findAll(it)
                            .body()
                            ?.toAlarmListItemUiState()
                            ?: emptyList()
                    }
                } catch (e : Exception){
                    Log.e(tag, "알람 리스트 로딩 실패 ${e.message}")
                }
                return list
            }

            override val isLogin: Flow<Boolean>
                get() = loggedInUserDao.getLoggedInUserFlow().map { it != null }
        }
    }
}

fun List<AlarmApiModel>.toAlarmListItemUiState(): List<AlarmListItemUIState> {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val now = Date(System.currentTimeMillis())
    val today: List<AlarmListItemUIState> = this.filter { data ->
        try {
            val d = sdf.parse(data.createDate)
            val diffHour: Long = TimeUnit.HOURS.convert(
                abs(d.time - now.time),
                TimeUnit.MILLISECONDS
            )
            return@filter diffHour < 24
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        false
    }.map {
        it.toAlarmListItem()
    }
    val thisWeek: List<AlarmListItemUIState> = this.filter { data ->
        try {
            val d: Date = sdf.parse(data.createDate) as Date
            val diffHour: Long = TimeUnit.HOURS.convert(
                abs(d.time - now.time),
                TimeUnit.MILLISECONDS
            )
            return@filter diffHour > 24 && diffHour < 24 * 7
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        false
    }.map {
        it.toAlarmListItem()
    }
    val thisMonth: List<AlarmListItemUIState> = this.filter { data ->
        try {
            val d: Date = sdf.parse(data.createDate) as Date
            val diffHour: Long = TimeUnit.HOURS.convert(
                abs(d.time - now.time),
                TimeUnit.MILLISECONDS
            )
            return@filter diffHour > 24 * 7 && diffHour < 24 * 30
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        false
    }.map {
        it.toAlarmListItem()
    }
    val other: List<AlarmListItemUIState> = this.filter { data ->
        try {
            val d: Date = sdf.parse(data.createDate) as Date
            val diffHour: Long = TimeUnit.HOURS.convert(
                abs(d.time - now.time),
                TimeUnit.MILLISECONDS
            )
            return@filter diffHour > 24 * 30
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        false
    }.map {
        it.toAlarmListItem()
    }
    val list1 = ArrayList<AlarmListItemUIState>()
    if (today.isNotEmpty()) {
        list1.add(AlarmListItemUIState.Header(title = "오늘"))
        list1.addAll(today)
    }
    if (thisWeek.isNotEmpty()) {
        list1.add(AlarmListItemUIState.Header(title = "이번주"))
        list1.addAll(thisWeek)
    }
    if (thisMonth.isNotEmpty()) {
        list1.add(AlarmListItemUIState.Header(title = "이번달"))
        list1.addAll(thisMonth)
    }
    if (other.isNotEmpty()) {
        list1.add(AlarmListItemUIState.Header(title = "오래전"))
        list1.addAll(other)
    }
    return list1
}

fun AlarmApiModel.toAlarmListItem(): AlarmListItemUIState {
    return AlarmListItemUIState.Item(
        id              = this.alarmId,
        user            = AlarmUser(name = this.otherUser.userName),
        contents        = this.contents,
        otherPictureUrl = BuildConfig.PROFILE_IMAGE_SERVER_URL + this.otherUser.profilePicUrl,
        createdDate     = this.createDate,
        type            = if (alarmType == "COMMENT") AlarmType.COMMENT else if (alarmType == "LIKE") AlarmType.LIKE else if (alarmType == "FOLLOW") AlarmType.FOLLOW else AlarmType.REPLY,
        pictureUrl      = BuildConfig.REVIEW_IMAGE_SERVER_URL + pictureUrl,
        reviewId        = reviewId,
        otherUserId     = otherUserId
    )
}