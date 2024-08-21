package com.sarang.alarm_test_app.di.alarm_di

import android.util.Log
import com.sarang.torang.BuildConfig
import com.sarang.torang.data1.alarm.AlarmListItem
import com.sarang.torang.data1.alarm.AlarmType
import com.sarang.torang.data1.alarm.AlarmUser
import com.sarang.torang.usecase.GetAlarmUseCase
import com.sarang.torang.api.ApiAlarm
import com.sarang.torang.data.dao.LoggedInUserDao
import com.sarang.torang.data.remote.response.AlarmAlarmModel
import com.sarang.torang.session.SessionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
            override suspend fun getAlarm(): List<AlarmListItem> {
                var list: List<AlarmListItem> = ArrayList()
                sessionService.getToken()?.let {
                    list = apiAlarm.getAlarms(it).map { it.toAlarmListItem() }
                }
                return list
            }

            override val isLogin: Flow<Boolean>
                get() = loggedInUserDao.getLoggedInUser().map { it != null }
        }
    }
}

fun AlarmAlarmModel.toAlarmListItem(): AlarmListItem {
    Log.d("RemoteAlarm", this.toString())
    return AlarmListItem(
        id = this.alarmId,
        user = AlarmUser(name = this.otherUser.userName),
        contents = this.contents,
        otherPictureUrl = BuildConfig.PROFILE_IMAGE_SERVER_URL + this.otherUser.profilePicUrl,
        createdDate = this.createDate,
        indexDate = "",
        type = if (alarmType == "COMMENT") AlarmType.COMMENT else if (alarmType == "LIKE") AlarmType.LIKE else if (alarmType == "FOLLOW") AlarmType.FOLLOW else AlarmType.REPLY,
        pictureUrl = BuildConfig.REVIEW_IMAGE_SERVER_URL + pictureUrl,
        reviewId = reviewId,
        otherUserId = otherUserId
    )
}