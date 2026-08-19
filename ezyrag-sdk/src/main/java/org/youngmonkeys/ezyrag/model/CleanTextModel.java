package org.youngmonkeys.ezyrag.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CleanTextModel {
    private final String text;
    private final int cleanTime;

    public CleanTextModel toCleanedText(
        String cleanedText
    ) {
        return new CleanTextModel(
            cleanedText,
            cleanTime + 1
        );
    }
}
