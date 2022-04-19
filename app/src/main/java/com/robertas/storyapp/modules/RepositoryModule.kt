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
import com.robertas.storyapp.utils.StoryNetworkMapper
import com.robertas.storyapp.utils.UserNetworkMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindStoryMapper(storyMapper: StoryNetworkMapper): IDomainMapper<StoryNetwork, Story>

    @Binds
    abstract fun bindUserMapper(userMapper: UserNetworkMapper): IDomainMapper<UserNetwork, User>

    @Binds
    abstract fun bindUserAccountRepository(userAccountRepository: UserAccountRepository):
            UserRepository

    @Binds
    abstract fun bindStoryRepository(userStoryRepository: UserStoryRepository): StoryRepository
}