package org.youngmonkeys.ezyrag.chunker;

import org.youngmonkeys.ezyrag.model.DataChunk;

import java.util.List;

public interface RagDataChunker {

    List<DataChunk> chunk(String data);

    String getName();
}
