/*
 * Copyright 2026 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fireflysoftware.domain.organization.core.branch;

import io.fireflysoftware.core.org.sdk.model.BranchDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Default adapter. Holds rows in memory and does not call the core. */
public final class SandboxBranchAdapter implements BranchPort {
    private final ConcurrentHashMap<UUID, BranchDTO> rows = new ConcurrentHashMap<>();

    @Override
    public Mono<BranchView> read(UUID id) {
        if (id != null && rows.containsKey(id)) {
            return Mono.just(view(id, rows.get(id)));
        }
        UUID resolved = id == null ? UUID.fromString("00000000-0000-4000-8000-000000000001") : id;
        return Mono.just(new BranchView(resolved, "HQ"));
    }

    @Override
    public Mono<StoredBranch> load(UUID id) {
        BranchDTO body = id == null ? null : rows.get(id);
        if (body == null) {
            return Mono.error(new IllegalStateException("Branch " + id + " was not found"));
        }
        return Mono.just(new StoredBranch(id, body));
    }

    @Override
    public Mono<StoredBranch> create(BranchDTO draft) {
        if (draft == null) {
            return Mono.error(new IllegalArgumentException("Branch is required"));
        }
        UUID id = UUID.randomUUID();
        rows.put(id, draft);
        return Mono.just(new StoredBranch(id, draft));
    }

    @Override
    public Mono<StoredBranch> update(UUID id, BranchDTO draft) {
        if (id == null || !rows.containsKey(id)) {
            return Mono.error(new IllegalStateException("Branch " + id + " was not found"));
        }
        if (draft == null) {
            return Mono.error(new IllegalArgumentException("Branch is required"));
        }
        rows.put(id, draft);
        return Mono.just(new StoredBranch(id, draft));
    }

    @Override
    public Mono<Void> remove(UUID id) {
        if (id != null) {
            rows.remove(id);
        }
        return Mono.empty();
    }

    private static BranchView view(UUID id, BranchDTO body) {
        String label = body == null ? null : body.getCode();
        return new BranchView(id, label);
    }
}
