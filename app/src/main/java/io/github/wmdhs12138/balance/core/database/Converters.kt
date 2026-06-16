package io.github.wmdhs12138.balance.core.database

import androidx.room.TypeConverter
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.BalanceUnit

/** Converters 类。 */
class Converters {
    @TypeConverter
    /** 处理balanceStatusToString 方法。 */
    fun balanceStatusToString(value: BalanceStatus): String = value.name

    @TypeConverter
    /** 处理stringToBalanceStatus 方法。 */
    fun stringToBalanceStatus(value: String): BalanceStatus =
        runCatching { BalanceStatus.valueOf(value) }.getOrDefault(BalanceStatus.Failed)

    @TypeConverter
    /** 处理balanceUnitToString 方法。 */
    fun balanceUnitToString(value: BalanceUnit): String = value.storageValue

    @TypeConverter
    /** 处理stringToBalanceUnit 方法。 */
    fun stringToBalanceUnit(value: String?): BalanceUnit = BalanceUnit.fromStorage(value)
}
