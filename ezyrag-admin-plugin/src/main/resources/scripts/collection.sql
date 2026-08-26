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

CREATE TABLE IF NOT EXISTS `ezyrag_collections` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `vector_db_service` varchar(120) NOT NULL,
    `name` varchar(120) NOT NULL,
    `display_name` varchar(120),
    `base_url` varchar(300),
    `vector_size` bigint unsigned NOT NULL,
    `distance` varchar(50),
    `status` varchar(50) NOT NULL,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_vector_db_service_name` (`vector_db_service`, `name`),
    INDEX `index_vector_db_service_pagination` (`vector_db_service`, `distance`, `status`, `vector_size`, `id`),
    INDEX `index_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
