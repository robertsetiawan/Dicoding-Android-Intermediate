package com.robertas.storyapp.abstractions

import androidx.room.Dao
import androidx.room.Query
import com.robertas.storyapp.models.domain.RemoteKeys

@Dao
interface RemoteKeysDao: BaseDao<RemoteKeys> {

    @Query("DELETE FROM remote_key")
    override suspend fun deleteAll()

    @Query("SELECT * FROM remote_key WHERE id = :id")
    suspend fun getRemoteKeysFromId(id: String): RemoteKeys?
}