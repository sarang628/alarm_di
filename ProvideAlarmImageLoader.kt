package com.sarang.torang.di.alarm_di

import TorangAsyncImage
import com.sarang.torang.compose.AlarmImageLoaderType

val provideAlarmImageLoader : AlarmImageLoaderType = { TorangAsyncImage(
        modifier = it.modifier,
        model = it.model,
        progressSize = it.progressSize,
        errorIconSize = it.errorIconSize,
        contentScale = it.contentScale
    )
}