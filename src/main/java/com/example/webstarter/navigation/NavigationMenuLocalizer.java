package com.example.webstarter.navigation;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class NavigationMenuLocalizer {

    private static final String MESSAGE_PREFIX = "navigation.menu.";

    private final MessageSource messageSource;

    public NavigationMenuLocalizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public NavigationMenuSummary localize(NavigationMenuSummary menu, Locale locale) {
        String messageBase = MESSAGE_PREFIX + menu.menuKey();
        String label = messageSource.getMessage(
                messageBase + ".label",
                null,
                menu.label(),
                locale);
        String group = messageSource.getMessage(
                messageBase + ".group",
                null,
                menu.menuGroup(),
                locale);
        return menu.withText(label, group);
    }
}
