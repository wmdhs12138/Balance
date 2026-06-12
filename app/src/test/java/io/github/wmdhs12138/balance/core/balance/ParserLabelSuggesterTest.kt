package io.github.wmdhs12138.balance.core.balance

import org.junit.Assert.assertEquals
import org.junit.Test

class ParserLabelSuggesterTest {
    @Test
    fun suggestsDeepSeekForOfficialConsole() {
        assertEquals("DeepSeek", ParserLabelSuggester.suggest("https://platform.deepseek.com"))
    }

    @Test
    fun suggestsSub2ApiForSub2LikeHost() {
        assertEquals("Sub2API", ParserLabelSuggester.suggest("https://sub2.example.com"))
    }

    @Test
    fun fallsBackToNewApi() {
        assertEquals("NewAPI", ParserLabelSuggester.suggest("https://relay.example.com"))
    }
}
