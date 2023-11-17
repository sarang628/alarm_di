package com.sarang.alarm_test_app.di.alarm_di

import android.util.Log
import com.sarang.alarm.service.GetAlarmUseCase
import com.sarang.alarm.uistate.AlarmListItem
import com.sarang.alarm.uistate.AlarmType
import com.sarang.alarm.uistate.User
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
                var list: List<AlarmListItem> = ArrayList<AlarmListItem>()
                sessionService.getToken()?.let {
                    list = apiAlarm.getAlarms(it).stream().map {
                        it.toAlarmListItem()
                    }.toList()
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
        user = User(name = this.otherUser.userName ?: ""),
        contents = this.contents,
        otherPictureUrl = this.otherUser.profilePicUrl,
        createdDate = this.createDate,
        indexDate = "",
        type = AlarmType.LIKE
    )
}