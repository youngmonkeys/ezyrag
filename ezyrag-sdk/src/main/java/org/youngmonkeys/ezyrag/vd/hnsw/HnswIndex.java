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

package org.youngmonkeys.ezyrag.vd.hnsw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HnswIndex {

    private final int maxM;
    private final int maxM0;
    private final int efConstruction;
    private final double levelMultiplier;

    private final Map<Long, Node> nodesById = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile Node entryPoint;
    private volatile int maxLevel = -1;

    public HnswIndex() {
        this(16, 200);
    }

    public HnswIndex(int maxM, int efConstruction) {
        this.maxM = maxM;
        this.maxM0 = maxM * 2;
        this.efConstruction = efConstruction;
        this.levelMultiplier = 1.0 / Math.log(maxM);
    }

    public void insert(long id, float[] vector) {
        float[] normalized = normalize(vector);
        lock.writeLock().lock();
        try {
            int level = randomLevel();
            Node node = new Node(id, normalized, level);
            if (entryPoint == null) {
                entryPoint = node;
                maxLevel = level;
                nodesById.put(id, node);
                return;
            }
            Node curr = entryPoint;
            float currDist = distance(normalized, curr.vector);
            for (int lc = maxLevel; lc > level; --lc) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (long neighborId : curr.neighbors(lc)) {
                        Node neighbor = nodesById.get(neighborId);
                        if (neighbor == null) {
                            continue;
                        }
                        float d = distance(normalized, neighbor.vector);
                        if (d < currDist) {
                            currDist = d;
                            curr = neighbor;
                            changed = true;
                        }
                    }
                }
            }
            int topLayer = Math.min(level, maxLevel);
            for (int lc = topLayer; lc >= 0; --lc) {
                List<Candidate> candidates =
                    searchLayer(normalized, curr, efConstruction, lc, false);
                int neighborLimit = lc == 0 ? maxM0 : maxM;
                List<Candidate> selected = candidates.size() > neighborLimit
                    ? candidates.subList(0, neighborLimit)
                    : candidates;
                for (Candidate c : selected) {
                    node.connect(lc, c.id);
                    Node neighbor = nodesById.get(c.id);
                    if (neighbor != null) {
                        neighbor.connect(lc, id);
                        pruneNeighbors(neighbor, lc);
                    }
                }
                if (!candidates.isEmpty()) {
                    Node next = nodesById.get(candidates.get(0).id);
                    if (next != null) {
                        curr = next;
                    }
                }
            }
            nodesById.put(id, node);
            if (level > maxLevel) {
                maxLevel = level;
                entryPoint = node;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(long id) {
        lock.writeLock().lock();
        try {
            Node node = nodesById.get(id);
            if (node != null) {
                node.deleted = true;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<SearchResult> search(float[] queryVector, int k, int ef) {
        float[] normalized = normalize(queryVector);
        lock.readLock().lock();
        try {
            if (entryPoint == null) {
                return Collections.emptyList();
            }
            Node curr = entryPoint;
            float currDist = distance(normalized, curr.vector);
            for (int lc = maxLevel; lc > 0; --lc) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (long neighborId : curr.neighbors(lc)) {
                        Node neighbor = nodesById.get(neighborId);
                        if (neighbor == null) {
                            continue;
                        }
                        float d = distance(normalized, neighbor.vector);
                        if (d < currDist) {
                            currDist = d;
                            curr = neighbor;
                            changed = true;
                        }
                    }
                }
            }
            List<Candidate> candidates =
                searchLayer(normalized, curr, Math.max(ef, k), 0, true);
            List<SearchResult> results =
                new ArrayList<>(Math.min(k, candidates.size()));
            for (int i = 0; i < candidates.size() && results.size() < k; ++i) {
                Candidate c = candidates.get(i);
                results.add(new SearchResult(c.id, 1f - c.dist));
            }
            return results;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        int count = 0;
        for (Node node : nodesById.values()) {
            if (!node.deleted) {
                ++count;
            }
        }
        return count;
    }

    private List<Candidate> searchLayer(
        float[] target,
        Node entry,
        int ef,
        int layer,
        boolean excludeDeleted
    ) {
        Set<Long> visited = new HashSet<>();
        PriorityQueue<Candidate> candidateQueue =
            new PriorityQueue<>(Comparator.comparingDouble(c -> c.dist));
        PriorityQueue<Candidate> resultQueue =
            new PriorityQueue<>((a, b) -> Float.compare(b.dist, a.dist));

        float entryDist = distance(target, entry.vector);
        Candidate entryCandidate = new Candidate(entry.id, entryDist);
        candidateQueue.add(entryCandidate);
        visited.add(entry.id);
        if (!excludeDeleted || !entry.deleted) {
            resultQueue.add(entryCandidate);
        }

        while (!candidateQueue.isEmpty()) {
            Candidate current = candidateQueue.poll();
            if (!resultQueue.isEmpty()
                && current.dist > resultQueue.peek().dist
                && resultQueue.size() >= ef) {
                break;
            }
            Node currentNode = nodesById.get(current.id);
            if (currentNode == null) {
                continue;
            }
            for (long neighborId : currentNode.neighbors(layer)) {
                if (!visited.add(neighborId)) {
                    continue;
                }
                Node neighborNode = nodesById.get(neighborId);
                if (neighborNode == null) {
                    continue;
                }
                float d = distance(target, neighborNode.vector);
                if (resultQueue.size() < ef || d < resultQueue.peek().dist) {
                    Candidate candidate = new Candidate(neighborId, d);
                    candidateQueue.add(candidate);
                    if (!excludeDeleted || !neighborNode.deleted) {
                        resultQueue.add(candidate);
                        if (resultQueue.size() > ef) {
                            resultQueue.poll();
                        }
                    }
                }
            }
        }
        List<Candidate> result = new ArrayList<>(resultQueue);
        result.sort(Comparator.comparingDouble(c -> c.dist));
        return result;
    }

    private void pruneNeighbors(Node node, int layer) {
        List<Long> neighbors = node.neighbors(layer);
        int limit = layer == 0 ? maxM0 : maxM;
        if (neighbors.size() <= limit) {
            return;
        }
        List<Candidate> candidates = new ArrayList<>(neighbors.size());
        for (long neighborId : neighbors) {
            Node neighbor = nodesById.get(neighborId);
            if (neighbor != null) {
                candidates.add(
                    new Candidate(neighborId, distance(node.vector, neighbor.vector))
                );
            }
        }
        candidates.sort(Comparator.comparingDouble(c -> c.dist));
        List<Long> kept = new ArrayList<>(limit);
        for (int i = 0; i < Math.min(limit, candidates.size()); ++i) {
            kept.add(candidates.get(i).id);
        }
        node.setNeighbors(layer, kept);
    }

    private int randomLevel() {
        double r = ThreadLocalRandom.current().nextDouble();
        return (int) Math.floor(-Math.log(r) * levelMultiplier);
    }

    private static float[] normalize(float[] vector) {
        float norm = 0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        float[] result = new float[vector.length];
        if (norm == 0f) {
            return result;
        }
        for (int i = 0; i < vector.length; ++i) {
            result[i] = vector[i] / norm;
        }
        return result;
    }

    private static float distance(float[] a, float[] b) {
        float dot = 0f;
        for (int i = 0; i < a.length; ++i) {
            dot += a[i] * b[i];
        }
        return 1f - dot;
    }

    private static final class Node {
        final long id;
        final float[] vector;
        final int level;
        final List<List<Long>> neighborsByLevel;
        volatile boolean deleted;

        Node(long id, float[] vector, int level) {
            this.id = id;
            this.vector = vector;
            this.level = level;
            this.neighborsByLevel = new ArrayList<>(level + 1);
            for (int i = 0; i <= level; ++i) {
                neighborsByLevel.add(new ArrayList<>());
            }
        }

        List<Long> neighbors(int layer) {
            return layer < neighborsByLevel.size()
                ? neighborsByLevel.get(layer)
                : Collections.emptyList();
        }

        void connect(int layer, long neighborId) {
            List<Long> list = neighbors(layer);
            if (layer < neighborsByLevel.size() && !list.contains(neighborId)) {
                list.add(neighborId);
            }
        }

        void setNeighbors(int layer, List<Long> value) {
            if (layer < neighborsByLevel.size()) {
                neighborsByLevel.set(layer, value);
            }
        }
    }

    private static final class Candidate {
        final long id;
        final float dist;

        Candidate(long id, float dist) {
            this.id = id;
            this.dist = dist;
        }
    }

    public static final class SearchResult {
        private final long id;
        private final float score;

        SearchResult(long id, float score) {
            this.id = id;
            this.score = score;
        }

        public long getId() {
            return id;
        }

        public float getScore() {
            return score;
        }
    }
}
