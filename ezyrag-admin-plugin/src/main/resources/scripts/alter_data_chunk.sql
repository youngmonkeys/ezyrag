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

ALTER TABLE `ezyrag_data_chunks`
ADD COLUMN `collection_id` bigint unsigned NOT NULL DEFAULT 0 AFTER `source_id`,
ADD COLUMN `embedding_service` char(120) AFTER `collection_id`,
ADD INDEX `index_collection_id_pagination` (`collection_id`, `source_type`, `source_id`, `chunk_index`, `content_hash`, `id`),
ADD INDEX `index_embedding_service_id` (`embedding_service`, `id`);
