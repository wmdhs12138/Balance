package io.github.wmdhs12138.balance.core.model

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
        fun fromPreference(value: String?): AppLanguage {
            return entries.firstOrNull { it.preferenceValue == value } ?: System
        }
    }
}
