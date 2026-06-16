package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceFetcherHttpBranchesTest {
    @Test
    fun deepSeekMapsForbiddenResponse() = runBlocking {
        val fetcher = DeepSeekBalanceFetcher(
            FakeBalanceHttpClient(
                HttpResponse(
                    statusCode = 403,
                    contentType = "application/json",
                    body = """{"message":"forbidden"}""",
                ),
            ),
        )

        val exception = assertBalanceFetchException {
            fetcher.fetch(provider(), apiKeyPayload("sk-test"))
        }

        assertEquals(BalanceFetchFailureReason.Forbidden, exception.reason)
        assertTrue(exception.message.orEmpty().contains("HTTP 403"))
        assertTrue(exception.message.orEmpty().contains("forbidden"))
    }

    @Test
    fun deepSeekRejectsHtmlResponse() = runBlocking {
        val fetcher = DeepSeekBalanceFetcher(
            FakeBalanceHttpClient(
                HttpResponse(
                    statusCode = 200,
                    contentType = "text/html",
                    body = "<html></html>",
                ),
            ),
        )

        val exception = assertBalanceFetchException {
            fetcher.fetch(provider(), apiKeyPayload("sk-test"))
        }

        assertEquals(BalanceFetchFailureReason.ParserMismatch, exception.reason)
    }

    @Test
    fun newApiMapsNotFoundAfterTryingAllEndpoints() = runBlocking {
        val client = FakeBalanceHttpClient(
            List(7) {
                HttpResponse(
                    statusCode = 404,
                    contentType = "application/json",
                    body = """{"message":"missing"}""",
                )
            },
        )
        val fetcher = NewApiBalanceFetcher(client)

        val exception = assertBalanceFetchException {
            fetcher.fetch(provider(), webSessionPayload())
        }

        assertEquals(BalanceFetchFailureReason.NotFound, exception.reason)
        assertEquals(7, client.requestedUrls.size)
    }

    @Test
    fun sub2ApiMapsHtmlAsParserMismatch() = runBlocking {
        val fetcher = Sub2ApiBalanceFetcher(
            FakeBalanceHttpClient(
                List(5) {
                    HttpResponse(
                        statusCode = 200,
                        contentType = "text/html",
                        body = "<!doctype html>",
                    )
                },
            ),
        )

        val exception = assertBalanceFetchException {
            fetcher.fetch(provider(), webSessionPayload("""{"localStorage":{"auth_token":"token"}}"""))
        }

        assertEquals(BalanceFetchFailureReason.ParserMismatch, exception.reason)
    }

    private fun provider(): ProviderEntity = ProviderEntity(
        id = 1,
        name = "Test",
        baseUrl = "https://example.com",
        loginUrl = "https://example.com",
    )

    private fun webSessionPayload(storage: String = """{"localStorage":{"token":"token","uid":"1"}}"""): String {
        return """
            {
              "type": "web_session",
              "url": "https://example.com",
              "cookies": "session=test",
              "storage": ${JSONObjectString(storage)},
              "userAgent": "test-agent"
            }
        """.trimIndent()
    }

    private suspend fun assertBalanceFetchException(block: suspend () -> Unit): BalanceFetchException {
        return runCatching { block() }
            .exceptionOrNull() as? BalanceFetchException
            ?: error("Expected BalanceFetchException")
    }
}

private class FakeBalanceHttpClient(
    responses: List<HttpResponse>,
) : BalanceHttpClient {
    constructor(response: HttpResponse) : this(listOf(response))

    private val responses = ArrayDeque(responses)
    val requestedUrls = mutableListOf<String>()

    override suspend fun get(
        url: String,
        headers: Map<String, String>,
        connectTimeoutMillis: Int,
        readTimeoutMillis: Int,
        maxBodyBytes: Int,
    ): HttpResponse {
        requestedUrls += url
        return responses.removeFirstOrNull() ?: error("No fake response for $url")
    }
}

private class JSONObjectString(private val value: String) {
    override fun toString(): String = org.json.JSONObject.quote(value)
}
