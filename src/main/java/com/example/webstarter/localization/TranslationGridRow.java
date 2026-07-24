package com.example.webstarter.localization;

import java.util.List;

public record TranslationGridRow(
        String messageKey,
        List<TranslationGridCell> cells) {
}
