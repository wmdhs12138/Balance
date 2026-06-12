package io.github.wmdhs12138.balance.core.database

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDatabaseSchemaTest {
    @Test
    fun versionOneSchemaMatchesCurrentBaseline() {
        val schema = JSONObject(
            File("schemas/io.github.wmdhs12138.balance.core.database.AppDatabase/1.json").readText(),
        )
        val database = schema.getJSONObject("database")
        val providers = database.getJSONArray("entities").getJSONObject(0)
        val fields = providers.getJSONArray("fields")
        val columnNames = (0 until fields.length()).map { index ->
            fields.getJSONObject(index).getString("columnName")
        }

        assertEquals(1, database.getInt("version"))
        assertEquals("providers", providers.getString("tableName"))
        assertTrue("loginUrl" in columnNames)
        assertTrue("balanceEndpointHint" in columnNames)
        assertTrue("lastErrorText" in columnNames)
        assertTrue("lastSuccessAtMillis" in columnNames)
        assertTrue("lastAttemptAtMillis" in columnNames)
    }
}
