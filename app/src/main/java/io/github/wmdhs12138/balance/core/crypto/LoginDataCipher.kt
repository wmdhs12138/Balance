package io.github.wmdhs12138.balance.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** LoginDataCipher 类。 */
class LoginDataCipher {
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /** 加密登录数据 方法。 */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return listOf(cipher.iv, cipherText)
            .joinToString(separator = ".") { Base64.encodeToString(it, Base64.NO_WRAP) }
    }

    /** 解密登录数据 方法。 */
    fun decrypt(payload: String): String {
        val parts = payload.split(".")
        require(parts.size == 2) { "Invalid encrypted login payload." }

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return cipher.doFinal(cipherText).toString(Charsets.UTF_8)
    }

    /** 获取或创建加密密钥 方法。 */
    private fun getOrCreateKey(): SecretKey {
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        /** 处理ANDROID_KEYSTORE 常量。 */
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        /** 处理KEY_ALIAS 常量。 */
        const val KEY_ALIAS = "balance_checker_login_data"
        /** 处理TRANSFORMATION 常量。 */
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        /** 处理GCM_TAG_LENGTH_BITS 常量。 */
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
