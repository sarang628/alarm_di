package com.sarang.alarm_test_app.di.alarm_di

import android.util.Log
import com.sarang.torang.BuildConfig
import com.sryang.torang.data1.alarm.AlarmListItem
import com.sryang.torang.data1.alarm.AlarmType
import com.sryang.torang.data1.alarm.AlarmUser
import com.sryang.torang.usecase.GetAlarmUseCase
import com.sryang.torang_repository.api.ApiAlarm
import com.sryang.torang_repository.data.RemoteAlarm
import com.sryang.torang_repository.data.dao.LoggedInUserDao
import com.sryang.torang_repository.session.SessionService
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
        loggedInUserDao: LoggedInUserDao
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

fun RemoteAlarm.toAlarmListItem(): AlarmListItem {
    Log.d("RemoteAlarm", this.toString())
    return AlarmListItem(
        id = this.alarmId,
        user = AlarmUser(name = this.otherUser.userName),
        contents = this.contents,
        otherPictureUrl = BuildConfig.PROFILE_IMAGE_SERVER_URL + this.otherUser.profilePicUrl,
        createdDate = this.createDate,
        indexDate = "",
        type = AlarmType.LIKE
    )
}