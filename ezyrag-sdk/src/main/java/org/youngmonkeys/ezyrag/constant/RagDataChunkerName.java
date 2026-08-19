package org.youngmonkeys.ezyrag.constant;

public enum RagDataChunkerName {
    HIERARCHICAL;

    public boolean equalsValue(String value) {
        return toString().equals(value);
    }
}
