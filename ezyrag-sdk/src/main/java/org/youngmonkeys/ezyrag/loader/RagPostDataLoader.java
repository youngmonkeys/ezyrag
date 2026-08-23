/*
 * Copyright 2026 youngmonkeys.org
 * 
 * Licensed under the ezyplatform, Version 1.0.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://youngmonkeys.org/licenses/ezyplatform-1.0.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.youngmonkeys.ezyrag.loader;

import com.tvd12.ezyfox.util.EzyMapBuilder;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyarticle.sdk.repo.PostRepository;
import org.youngmonkeys.ezyarticle.sdk.result.PostIdAndSlugResult;
import org.youngmonkeys.ezyarticle.sdk.result.PostIdAndTitleAndContentResult;
import org.youngmonkeys.ezyarticle.sdk.result.PostSummaryResult;
import org.youngmonkeys.ezyplatform.constant.CommonContentType;
import org.youngmonkeys.ezyrag.model.RagDataSourceModel;
import org.youngmonkeys.ezyrag.model.RagInputData;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static org.youngmonkeys.ezyarticle.sdk.constant.TableNames.TABLE_NAME_POST;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_EXCERPT;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_SLUG;
import static org.youngmonkeys.ezyrag.constant.EzyRagConstants.META_KEY_TITLE;

@AllArgsConstructor
public class RagPostDataLoader implements RagDataLoader {

    private final PostRepository postRepository;

    @Override
    public Iterator<RagInputData> load(RagDataSourceModel model) {
        long postId = model.getSourceId();
        PostIdAndTitleAndContentResult post = postRepository
            .findPostIdAndTitleAndContentById(postId);
        if (post == null) {
            return Collections.emptyIterator();
        }
        PostIdAndSlugResult slugResult = postRepository
            .findPostIdAndSlugByPostId(postId);
        PostSummaryResult summaryResult = postRepository
            .findPostSummaryById(postId);
        Map<String, Object> metadata = EzyMapBuilder
            .mapBuilder()
            .put(META_KEY_TITLE, post.getTitle())
            .put(
                META_KEY_SLUG,
                slugResult != null ? slugResult.getSlug() : null
            )
            .put(
                META_KEY_EXCERPT,
                summaryResult != null ? summaryResult.getSummary() : null
            )
            .toMap();
        return Stream
            .of(post)
            .map(it ->
                RagInputData.builder()
                    .data(it.getTitle() + "\n\n" + it.getContent())
                    .dataType(CommonContentType.TEXT.toString())
                    .metadata(metadata)
                    .build()
            )
            .iterator();
    }

    @Override
    public String getDataSourceType() {
        return TABLE_NAME_POST;
    }
}
