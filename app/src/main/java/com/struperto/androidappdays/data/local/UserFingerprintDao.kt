package com.struperto.androidappdays.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFingerprintDao {
    @Query(
        """
        SELECT * FROM user_fingerprint
        WHERE id = 0
        LIMIT 1
        """,
    )
    fun observe(): Flow<UserFingerprintEntity?>

    @Query(
        """
        SELECT * FROM user_fingerprint
        WHERE id = 0
        LIMIT 1
        """,
    )
    suspend fun get(): UserFingerprintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserFingerprintEntity)
}
