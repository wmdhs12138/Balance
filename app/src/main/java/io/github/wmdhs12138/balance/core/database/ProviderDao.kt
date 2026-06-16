package io.github.wmdhs12138.balance.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
/** ProviderDao 接口。 */
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY name COLLATE NOCASE ASC")
    /** 监听服务商列表 方法。 */
    fun observeProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers ORDER BY name COLLATE NOCASE ASC")
    /** 读取服务商列表 方法。 */
    suspend fun getProviders(): List<ProviderEntity>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    /** 按 ID 读取服务商 方法。 */
    suspend fun getProvider(id: Long): ProviderEntity?

    @Query("SELECT * FROM providers WHERE baseUrl = :baseUrl LIMIT 1")
    /** 处理getProviderByBaseUrl 方法。 */
    suspend fun getProviderByBaseUrl(baseUrl: String): ProviderEntity?

    @Query("SELECT COUNT(*) FROM providers")
    /** 处理count 方法。 */
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    /** 插入服务商 方法。 */
    suspend fun insert(provider: ProviderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    /** 批量插入服务商 方法。 */
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Update
    /** 更新服务商 方法。 */
    suspend fun update(provider: ProviderEntity)

    @Query("UPDATE providers SET status = :status, balanceText = :balanceText, balanceUnit = :balanceUnit, lastErrorText = NULL, lastSuccessAtMillis = :timestamp, lastAttemptAtMillis = :timestamp WHERE id = :id")
    /** 处理updateBalanceSuccess 方法。 */
    suspend fun updateBalanceSuccess(
        id: Long,
        status: io.github.wmdhs12138.balance.core.model.BalanceStatus,
        balanceText: String?,
        balanceUnit: io.github.wmdhs12138.balance.core.model.BalanceUnit,
        timestamp: Long,
    )

    @Query("UPDATE providers SET status = :status, lastErrorText = :lastErrorText, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    /** 处理updateBalanceFailure 方法。 */
    suspend fun updateBalanceFailure(
        id: Long,
        status: io.github.wmdhs12138.balance.core.model.BalanceStatus,
        lastErrorText: String?,
        lastAttemptAtMillis: Long,
    )

    @Query("UPDATE providers SET encryptedLoginPayload = :encryptedLoginPayload, status = :status, lastErrorText = NULL, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    /** 处理updateEncryptedLogin 方法。 */
    suspend fun updateEncryptedLogin(
        id: Long,
        encryptedLoginPayload: String,
        status: io.github.wmdhs12138.balance.core.model.BalanceStatus,
        lastAttemptAtMillis: Long,
    )

    @Query("UPDATE providers SET balanceEndpointHint = :parserLabel, lastErrorText = NULL, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    /** 更新解析器标签 方法。 */
    suspend fun updateParserLabel(
        id: Long,
        parserLabel: String?,
        lastAttemptAtMillis: Long,
    )

    @Query("UPDATE providers SET name = :name, balanceEndpointHint = :parserLabel, balanceUnitOverride = :balanceUnitOverride, lastErrorText = NULL, lastAttemptAtMillis = :lastAttemptAtMillis WHERE id = :id")
    /** 更新服务商设置 方法。 */
    suspend fun updateProviderSettings(
        id: Long,
        name: String,
        parserLabel: String?,
        balanceUnitOverride: io.github.wmdhs12138.balance.core.model.BalanceUnit,
        lastAttemptAtMillis: Long,
    )

    @Query("DELETE FROM providers WHERE id = :id")
    /** 删除服务商 方法。 */
    suspend fun deleteProvider(id: Long)

    @Query("DELETE FROM providers")
    /** 删除全部服务商 方法。 */
    suspend fun deleteAllProviders()
}
