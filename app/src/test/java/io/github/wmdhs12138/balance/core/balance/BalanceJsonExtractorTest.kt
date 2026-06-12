package io.github.wmdhs12138.balance.core.balance

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class BalanceJsonExtractorTest {
    @Test
    fun findsNumberInsideCurrencyString() {
        val json = JSONObject("""{"data":{"wallet":{"balance":"¥ 12.34 CNY"}}}""")

        assertEquals(12.34, BalanceJsonExtractor.findNumber(json, listOf("balance"))!!, 0.001)
    }

    @Test
    fun findsNestedServerErrorMessage() {
        val message = BalanceJsonExtractor.errorMessage(
            """{"error":{"message":"token expired"}}""",
        )

        assertEquals("token expired", message)
    }

    @Test
    fun unwrapDataFallsBackToRootObject() {
        val json = BalanceJsonExtractor.unwrapData("""{"balance":3.21}""")

        assertEquals(3.21, BalanceJsonExtractor.findNumber(json, listOf("balance"))!!, 0.001)
    }
}
