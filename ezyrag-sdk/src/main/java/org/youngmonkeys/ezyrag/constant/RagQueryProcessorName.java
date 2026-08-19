package org.youngmonkeys.ezyrag.constant;

public enum RagQueryProcessorName {
    NORMALIZE;

    public boolean equalsValue(String value) {
        return toString().equals(value);
    }
}
