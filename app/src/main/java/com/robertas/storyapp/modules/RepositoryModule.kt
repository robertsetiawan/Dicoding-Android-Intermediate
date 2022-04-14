package com.robertas.storyapp.modules

import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.network.StoryNetwork
import com.robertas.storyapp.models.network.UserNetwork
import com.robertas.storyapp.repositories.UserAccountRepository
import com.robertas.storyapp.repositories.UserStoryRepository
import com.robertas.storyapp.utils.StoryMapper
import com.robertas.storyapp.utils.UserMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindStoryMapper(storyMapper: StoryMapper): IDomainMapper<StoryNetwork, Story>

    @Binds
    abstract fun bindUserMapper(userMapper: UserMapper): IDomainMapper<UserNetwork, User>

    @Binds
    abstract fun bindUserAccountRepository(userAccountRepository: UserAccountRepository):
            UserRepository

    @Binds
    abstract fun bindStoryRepository(userStoryRepository: UserStoryRepository): StoryRepository
}