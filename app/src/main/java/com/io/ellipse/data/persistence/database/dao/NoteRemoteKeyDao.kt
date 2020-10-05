package com.io.ellipse.data.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.io.ellipse.data.persistence.database.entity.NoteRemoteKeyEntity

@Dao
interface NoteRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<NoteRemoteKeyEntity>)

    @Query("SELECT * FROM note_remote_keys WHERE id = :id")
    suspend fun remoteKeysRepoId(id: String): NoteRemoteKeyEntity?

    @Query("DELETE FROM note_remote_keys")
    suspend fun clearRemoteKeys()
}