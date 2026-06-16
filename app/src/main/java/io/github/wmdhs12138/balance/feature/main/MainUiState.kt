package io.github.wmdhs12138.balance.feature.main

import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.ThemeMode
import io.github.wmdhs12138.balance.R

/** MainUiState 数据结构。 */
data class MainUiState(
    val providers: List<Provider> = emptyList(),
    val loading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val seedColor: Long = 0xFF006C4FL,
    val language: AppLanguage = AppLanguage.System,
    val refreshingAll: Boolean = false,
    val refreshingProviderIds: Set<Long> = emptySet(),
    val sortingMode: Boolean = false,
    val pullRefreshHintShown: Boolean = false,
    val message: UiMessage? = null,
)

/** UiMessage 数据结构。 */
data class UiMessage(
    val resId: Int,
    val args: List<Any> = emptyList(),
)

/** 处理uiMessage 方法。 */
private fun uiMessage(resId: Int, vararg args: Any): UiMessage = UiMessage(resId, args.toList())

/** MainMessages 单例对象。 */
object MainMessages {
    /** 处理refreshComplete 方法。 */
    fun refreshComplete(ready: Int, needsLogin: Int, failed: Int) = uiMessage(
        R.string.message_refresh_complete,
        ready,
        needsLogin,
        failed,
    )

    val ProviderRefreshed = uiMessage(R.string.message_provider_refreshed)
    val ProviderRefreshFailed = uiMessage(R.string.message_provider_refresh_failed)
    val ProviderAdded = uiMessage(R.string.message_provider_added)
    val ProviderAlreadyExists = uiMessage(R.string.message_provider_already_exists)
    val ProviderUrlInvalid = uiMessage(R.string.field_provider_url_invalid)
    val ApiKeySaved = uiMessage(R.string.message_api_key_saved)
    val ParserUpdated = uiMessage(R.string.message_parser_updated)
    val ProviderUpdated = uiMessage(R.string.message_provider_updated)
    val ProviderOrderUpdated = uiMessage(R.string.message_provider_order_updated)
    val ProviderDeleted = uiMessage(R.string.message_provider_deleted)
    val ProviderDeletedWebDataMayRemain = uiMessage(R.string.message_provider_deleted_web_data_may_remain)
    val LocalDataCleared = uiMessage(R.string.message_local_data_cleared)
    val LocalDataClearedWebDataMayRemain = uiMessage(R.string.message_local_data_cleared_web_data_may_remain)
}
