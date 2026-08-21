package org.youngmonkeys.ezyrag.reader;

import java.io.File;

import static com.tvd12.ezyfox.io.EzyStrings.EMPTY_STRING;
import static org.youngmonkeys.ezyplatform.constant.CommonConstants.ZERO;

public interface RagMediaTextReader {

    Iterable<String> read(File path);

    default String getMimeType() {
        return EMPTY_STRING;
    }

    default String getExtension() {
        return EMPTY_STRING;
    }

    default int getPriority() {
        return ZERO;
    }
}
