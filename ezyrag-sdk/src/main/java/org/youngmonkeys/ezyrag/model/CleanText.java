package org.youngmonkeys.ezyrag.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CleanText {
    private final String text;
    private final int cleanTime;

    public CleanText toCleanedText(
        String cleanedText
    ) {
        return new CleanText(
            cleanedText,
            cleanTime + 1
        );
    }
}
