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

package org.youngmonkeys.ezyrag.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static org.youngmonkeys.ezyrag.constant.EzyRagTableNames.TABLE_NAME_COLLECTION_SEGMENT;

@Getter
@Setter
@ToString
@Entity
@Table(name = TABLE_NAME_COLLECTION_SEGMENT)
@AllArgsConstructor
@NoArgsConstructor
public class RagCollectionSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "collection_id")
    private long collectionId;

    @Column(name = "segment_no")
    private long segmentNo;

    @Column(name = "segment_type")
    private String segmentType;

    @Column(name = "status")
    private String status;

    @Column(name = "points_count")
    private long pointsCount;

    @Column(name = "min_point_id")
    private Long minPointId;

    @Column(name = "max_point_id")
    private Long maxPointId;

    @Column(name = "index_version")
    private long indexVersion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
