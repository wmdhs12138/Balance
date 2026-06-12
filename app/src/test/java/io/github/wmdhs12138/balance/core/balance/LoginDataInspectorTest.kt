package io.github.wmdhs12138.balance.core.balance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginDataInspectorTest {
    @Test
    fun findsNestedTokenInsideStorageJsonString() {
        val storage = """
            {
              "localStorage": {
                "persist:auth": "{\"session\":{\"accessToken\":\"abc-token\"}}"
              },
              "sessionStorage": {}
            }
        """.trimIndent()

        assertEquals("abc-token", LoginDataInspector.findAuthToken(storage))
        assertTrue(LoginDataInspector.hasLoginData("", storage))
    }

    @Test
    fun findsUserIdInsideNestedObject() {
        val storage = """
            {
              "localStorage": {
                "user": { "profile": { "userId": "42" } }
              },
              "sessionStorage": {}
            }
        """.trimIndent()

        assertEquals("42", LoginDataInspector.findUserId(storage))
    }

    @Test
    fun invalidStorageDoesNotCountAsLoginDataWithoutCookie() {
        assertFalse(LoginDataInspector.hasLoginData("", "not json"))
    }
}
