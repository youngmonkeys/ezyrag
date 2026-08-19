package org.youngmonkeys.ezyrag.chunker;

import org.youngmonkeys.ezyrag.model.DataChunkModel;

import java.util.List;

public interface RagDataChunker {

    List<DataChunkModel> chunk(String data);

    String getName();
}
