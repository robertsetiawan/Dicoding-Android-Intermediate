package com.robertas.storyapp

import android.content.Intent
import android.widget.RemoteViewsService
import com.robertas.storyapp.abstractions.StoryRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StackStoryService: RemoteViewsService() {
    @Inject
    lateinit var userStoryRepository: StoryRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext, userStoryRepository)
}