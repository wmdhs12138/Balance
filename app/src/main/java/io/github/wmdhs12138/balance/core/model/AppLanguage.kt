package io.github.wmdhs12138.balance.core.model

/** AppLanguage 枚举。 */
enum class AppLanguage(
    val preferenceValue: String,
    val languageTag: String?,
) {
    System("system", null),
    English("en", "en"),
    ChineseSimplified("zh-Hans", "zh-Hans"),
    Russian("ru", "ru"),
    French("fr", "fr"),
    German("de", "de"),
    Japanese("ja", "ja");

    companion object {
        /** 从Preference创建结果 方法。 */
        fun fromPreference(value: String?): AppLanguage {
            return entries.firstOrNull { it.preferenceValue == value } ?: System
        }
    }
}
