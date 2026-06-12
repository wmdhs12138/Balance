package io.github.wmdhs12138.balance.feature.main

import org.junit.Assert.assertEquals
import org.junit.Test

class MainDisplayFormattersTest {
    @Test
    fun normalizesCurrencySymbolsAndPrecision() {
        assertEquals("¥ 12.30", "￥12.3".normalizeBalanceText())
        assertEquals("$ 8.00", "$8".normalizeBalanceText())
    }

    @Test
    fun leavesUnknownBalanceTextUntouched() {
        assertEquals("12 credits", "12 credits".normalizeBalanceText())
    }
}
