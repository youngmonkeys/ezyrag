package org.youngmonkeys.ezyrag.constant;

public enum VectorDatabaseProvider {
    QDRANT;

    public boolean equalsValue(String value) {
        return toString().equals(value);
    }
}
