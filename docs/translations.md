# Translation Guide

Balance uses Android string resources for user-facing UI text.

To add a language:

1. Copy `app/src/main/res/values/strings.xml`.
2. Create a locale folder such as `app/src/main/res/values-es/`.
3. Paste the file as `strings.xml`.
4. Translate only the text values. Keep every `name` unchanged.
5. Add the language to `AppLanguage` and `AppLanguage.displayName()` if it should appear in Settings.

Existing language folders:

- `values/`: English fallback and translation key reference
- `values-zh-rCN/`: Simplified Chinese
- `values-ru/`: Russian
- `values-fr/`: French
- `values-de/`: German
- `values-ja/`: Japanese

Parser identifiers such as `NewApi` and `Sub2API` are intentionally not translated because they are technical labels.
