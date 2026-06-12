package io.github.wmdhs12138.balance.core.balance

import org.junit.Assert.assertEquals
import org.junit.Test

class BalanceFetchExceptionTest {
    @Test
    fun defaultsToUnknownReason() {
        val exception = BalanceFetchException("failed")

        assertEquals(BalanceFetchFailureReason.Unknown, exception.reason)
    }

    @Test
    fun mapsHttpStatusToFailureReason() {
        assertEquals(
            BalanceFetchFailureReason.NeedsLogin,
            HttpResponse(statusCode = 401, contentType = "application/json", body = "{}").failureReason,
        )
        assertEquals(
            BalanceFetchFailureReason.Forbidden,
            HttpResponse(statusCode = 403, contentType = "application/json", body = "{}").failureReason,
        )
        assertEquals(
            BalanceFetchFailureReason.NotFound,
            HttpResponse(statusCode = 404, contentType = "application/json", body = "{}").failureReason,
        )
        assertEquals(
            BalanceFetchFailureReason.Unknown,
            HttpResponse(statusCode = 500, contentType = "application/json", body = "{}").failureReason,
        )
    }
}
