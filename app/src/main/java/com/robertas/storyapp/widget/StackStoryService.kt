package com.robertas.storyapp.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StackStoryService: RemoteViewsService() {
    @Inject
    lateinit var userStoryRepository: StoryRepository

    @Inject
    lateinit var userAccountRepository: UserRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext, userAccountRepository, userStoryRepository)
}