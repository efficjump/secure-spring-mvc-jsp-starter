package com.example.webstarter.localization;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class MessageKeyCatalog {

    private final Set<String> messageKeys;

    public MessageKeyCatalog(@Value("${spring.messages.basename:messages}") String basenames) {
        TreeSet<String> keys = new TreeSet<>();
        Arrays.stream(basenames.split(","))
                .map(String::strip)
                .filter(basename -> !basename.isBlank())
                .forEach(basename -> loadKeys(basename, keys));
        if (keys.isEmpty()) {
            throw new IllegalStateException("No default message keys were found");
        }
        messageKeys = Set.copyOf(keys);
    }

    public Set<String> keys() {
        return messageKeys;
    }

    private void loadKeys(String basename, Set<String> keys) {
        String normalized = basename
                .replaceFirst("^classpath:", "")
                .replace('.', '/')
                .replaceFirst("^/+", "");
        ClassPathResource resource = new ClassPathResource(normalized + ".properties");
        Properties properties = new Properties();
        try (InputStreamReader reader =
                     new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            properties.load(reader);
            keys.addAll(properties.stringPropertyNames());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load default message bundle", exception);
        }
    }
}
