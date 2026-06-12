package io.github.wmdhs12138.balance.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY name COLLATE NOCASE ASC")
    fun observeProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers ORDER BY name COLLATE NOCASE ASC")
    suspend fun getProviders(): List<ProviderEntity>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getProvider(id: Long): ProviderEntity?

    @Query("SELECT * FROM providers WHERE baseUrl = :baseUrl LIMIT 1")
    suspend fun getProviderByBaseUrl(baseUrl: String): ProviderEntity?

    @Query("SELECT COUNT(*) FROM providers")
    suspend fun count(): Int

    @Query("UPDATE providers SET balanceEndpointHint = 'NewAPI' WHERE balanceEndpointHint = 'NewApi'")
    suspend fun normalizeNewApiParserLabel()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: ProviderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Update
    suspend fun update(provider: ProviderEntity)

    @Query("UPDATE providers SET status = :status, balanceText = :balanceText, lastErrorText = NULL, lastSuccessAtMillis = :timestamp, lastAttemptAtMillis = :timestamp WHERE id = :id")
    suspend fun updateBalanceSuccess(
        id: Long,
        status: io.github.wmdhs12138.balance.core.model.BalanceStatus,
        balanceText: String?,
        timestamp: Long,
    )

    @Query("UPDATE providers SET status = :status, lastErrorText = :lastErrorText, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    suspend fun updateBalanceFailure(
        id: Long,
        status: io.github.wmdhs12138.balance.core.model.BalanceStatus,
        lastErrorText: String?,
        lastAttemptAtMillis: Long,
    )

    @Query("UPDATE providers SET encryptedLoginPayload = :encryptedLoginPayload, status = :status, lastErrorText = NULL, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    suspend fun updateEncryptedLogin(
        id: Long,
        encryptedLoginPayload: String,
        status: io.github.wmdhs12138.balance.core.model.BalanceStatus,
        lastAttemptAtMillis: Long,
    )

    @Query("UPDATE providers SET balanceEndpointHint = :parserLabel, lastErrorText = NULL, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    suspend fun updateParserLabel(
        id: Long,
        parserLabel: String?,
        lastAttemptAtMillis: Long,
    )

    @Query("UPDATE providers SET name = :name, balanceEndpointHint = :parserLabel, lastErrorText = NULL, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    suspend fun updateProviderSettings(
        id: Long,
        name: String,
        parserLabel: String?,
        lastAttemptAtMillis: Long,
    )

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProvider(id: Long)

    @Query("DELETE FROM providers")
    suspend fun deleteAllProviders()
}
