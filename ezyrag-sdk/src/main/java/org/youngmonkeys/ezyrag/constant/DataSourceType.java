package org.youngmonkeys.ezyrag.constant;

public enum DataSourceType {
    TEXT;

    public boolean equalsValue(String value) {
        return toString().equals(value);
    }
}
