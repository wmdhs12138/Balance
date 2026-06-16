package io.github.wmdhs12138.balance.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.wmdhs12138.balance.core.model.ThemeMode

@Composable
/** 应用主题样式 方法。 */
fun BalanceTheme(
    themeMode: ThemeMode,
    seedColor: Long,
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val context = LocalContext.current
    val seed = Color(seedColor.normalizedSeedColor())
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme(seed)
        else -> lightScheme(seed)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

/** 将主题种子色限制为 ARGB 无符号范围。 */
private fun Long.normalizedSeedColor(): Long = this and 0xFFFFFFFFL

/** 处理lightScheme 方法。 */
private fun lightScheme(seed: Color): ColorScheme {
    val primary = seed
    val secondary = seed.withSaturation(0.42f).withValue(0.46f)
    val tertiary = seed.shiftHue(42f).withSaturation(0.56f).withValue(0.50f)
    val background = seed.blend(Color.White, 0.96f)
    val surfaceVariant = seed.blend(Color.White, 0.84f)
    val primaryContainer = seed.blend(Color.White, 0.78f)
    val secondaryContainer = secondary.blend(Color.White, 0.80f)
    val tertiaryContainer = tertiary.blend(Color.White, 0.80f)
    return lightColorScheme(
        primary = primary,
        onPrimary = primary.readableTextColor(),
        primaryContainer = primaryContainer,
        onPrimaryContainer = primaryContainer.readableTextColor(),
        secondary = secondary,
        onSecondary = secondary.readableTextColor(),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = secondaryContainer.readableTextColor(),
        tertiary = tertiary,
        onTertiary = tertiary.readableTextColor(),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = tertiaryContainer.readableTextColor(),
        background = background,
        onBackground = Color(0xFF191C1A),
        surface = background,
        onSurface = Color(0xFF191C1A),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = surfaceVariant.readableTextColor(),
        outline = seed.blend(Color(0xFF5F6360), 0.58f),
    )
}

/** 处理darkScheme 方法。 */
private fun darkScheme(seed: Color): ColorScheme {
    val primary = seed.withValue(0.82f).withSaturation(0.62f)
    val secondary = seed.withSaturation(0.34f).withValue(0.76f)
    val tertiary = seed.shiftHue(42f).withSaturation(0.50f).withValue(0.80f)
    val background = seed.blend(Color(0xFF101412), 0.92f)
    val surfaceVariant = seed.blend(Color(0xFF2D332F), 0.58f)
    val primaryContainer = seed.withValue(0.34f).withSaturation(0.70f)
    val secondaryContainer = secondary.blend(Color(0xFF202622), 0.62f)
    val tertiaryContainer = tertiary.blend(Color(0xFF202631), 0.62f)
    return darkColorScheme(
        primary = primary,
        onPrimary = primary.readableTextColor(),
        primaryContainer = primaryContainer,
        onPrimaryContainer = primaryContainer.readableTextColor(),
        secondary = secondary,
        onSecondary = secondary.readableTextColor(),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = secondaryContainer.readableTextColor(),
        tertiary = tertiary,
        onTertiary = tertiary.readableTextColor(),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = tertiaryContainer.readableTextColor(),
        background = background,
        onBackground = Color(0xFFE0E3DF),
        surface = background,
        onSurface = Color(0xFFE0E3DF),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = surfaceVariant.readableTextColor(),
        outline = seed.blend(Color(0xFF8B938B), 0.56f),
    )
}

/** 处理blend 方法。 */
private fun Color.blend(other: Color, otherWeight: Float): Color {
    val clamped = otherWeight.coerceIn(0f, 1f)
    val selfWeight = 1f - clamped
    return Color(
        red = red * selfWeight + other.red * clamped,
        green = green * selfWeight + other.green * clamped,
        blue = blue * selfWeight + other.blue * clamped,
        alpha = alpha * selfWeight + other.alpha * clamped,
    )
}

/** 处理shiftHue 方法。 */
private fun Color.shiftHue(degrees: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toArgb(), hsv)
    hsv[0] = (hsv[0] + degrees).mod(360f)
    return Color(AndroidColor.HSVToColor((alpha * 255).toInt(), hsv))
}

/** 处理withSaturation 方法。 */
private fun Color.withSaturation(saturation: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toArgb(), hsv)
    hsv[1] = saturation.coerceIn(0f, 1f)
    return Color(AndroidColor.HSVToColor((alpha * 255).toInt(), hsv))
}

/** 处理withValue 方法。 */
private fun Color.withValue(value: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toArgb(), hsv)
    hsv[2] = value.coerceIn(0f, 1f)
    return Color(AndroidColor.HSVToColor((alpha * 255).toInt(), hsv))
}

/** 处理readableTextColor 方法。 */
private fun Color.readableTextColor(): Color = if (luminance() > 0.45f) {
    Color(0xFF101412)
} else {
    Color.White
}
