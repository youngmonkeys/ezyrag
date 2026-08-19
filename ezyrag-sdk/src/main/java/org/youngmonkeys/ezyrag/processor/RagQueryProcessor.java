package org.youngmonkeys.ezyrag.processor;

import org.youngmonkeys.ezyrag.model.RagQueryModel;

public interface RagQueryProcessor {

    RagQueryModel process(RagQueryModel query);

    String getName();
}
