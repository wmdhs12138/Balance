package io.github.wmdhs12138.balance.core.database

import androidx.room.TypeConverter
import io.github.wmdhs12138.balance.core.model.BalanceStatus

class Converters {
    @TypeConverter
    fun balanceStatusToString(value: BalanceStatus): String = value.name

    @TypeConverter
    fun stringToBalanceStatus(value: String): BalanceStatus =
        runCatching { BalanceStatus.valueOf(value) }.getOrDefault(BalanceStatus.Failed)
}
