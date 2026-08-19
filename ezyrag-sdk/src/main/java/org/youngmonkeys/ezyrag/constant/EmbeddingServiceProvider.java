package org.youngmonkeys.ezyrag.constant;

public enum EmbeddingServiceProvider {
    OPENAI;

    public boolean equalsValue(String value) {
        return toString().equals(value);
    }
}
