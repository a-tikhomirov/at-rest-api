package ru.at.rest.api.cucumber.plugin;

import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.plugin.event.TestCase;
import io.qameta.allure.SeverityLevel;

import java.util.Arrays;

/**
 * Parser for tags.
 */
class TagParser {
    private static final String FLAKY = "@FLAKY";
    private static final String KNOWN = "@KNOWN";
    private static final String MUTED = "@MUTED";

    private final Feature feature;
    private final TestCase scenario;

    TagParser(final Feature feature, final TestCase scenario) {
        this.feature = feature;
        this.scenario = scenario;
    }

    public boolean isFlaky() {
        return getStatusDetailByTag(FLAKY);
    }

    public boolean isMuted() {
        return getStatusDetailByTag(MUTED);
    }

    public boolean isKnown() {
        return getStatusDetailByTag(KNOWN);
    }

    private boolean getStatusDetailByTag(final String tagName) {
        return scenario.getTags().stream()
                .anyMatch(tag -> tag.equalsIgnoreCase(tagName))
                || feature.getTagsList().stream()
                .anyMatch(tag -> tag.getName().equalsIgnoreCase(tagName));
    }

    public boolean isResultTag(final String tag) {
        return Arrays.asList(new String[]{FLAKY, KNOWN, MUTED})
                .contains(tag.toUpperCase());
    }

    public boolean isPureSeverityTag(final String tag) {
        return Arrays.stream(SeverityLevel.values())
                .map(SeverityLevel::value)
                .map(value -> "@" + value)
                .anyMatch(value -> value.equalsIgnoreCase(tag));
    }

}
