package io.github.wmdhs12138.balance.core.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlNormalizerTest {
    @Test
    fun normalizeAddsHttpsAndKeepsOriginStable() {
        val normalized = UrlNormalizer.normalize("example.com/path/?q=1")

        assertEquals("https://example.com/path?q=1", normalized?.displayUrl)
        assertEquals("https://example.com", normalized?.origin)
    }

    @Test
    fun normalizeLowercasesHostAndKeepsPort() {
        val normalized = UrlNormalizer.normalize("HTTP://Example.COM:3000/admin")

        assertEquals("http://example.com:3000/admin", normalized?.displayUrl)
        assertEquals("http://example.com:3000", normalized?.origin)
    }

    @Test
    fun normalizeRejectsInvalidProviderUrl() {
        assertNull(UrlNormalizer.normalize("not-a-host"))
    }
}
