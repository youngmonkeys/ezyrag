package org.youngmonkeys.ezyrag.constant;

public enum RagDataRetrieverName {
    DATABASE;

    public boolean equalsValue(String value) {
        return toString().equals(value);
    }
}
