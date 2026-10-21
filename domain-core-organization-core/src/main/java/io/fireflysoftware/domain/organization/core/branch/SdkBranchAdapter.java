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

import io.fireflysoftware.core.org.sdk.api.BranchManagementApi;
import io.fireflysoftware.core.org.sdk.model.BranchDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Live adapter. Selected with firefly.organization.branch.adapter=core. */
public final class SdkBranchAdapter implements BranchPort {
    private final BranchManagementApi api;

    public SdkBranchAdapter(BranchManagementApi api) {
        this.api = api;
    }

    @Override
    public Mono<BranchView> read(UUID id) {
        return api.getBranchById(id, null).map(dto -> new BranchView(dto.getId(), dto.getCode()));
    }

    @Override
    public Mono<StoredBranch> load(UUID id) {
        return api.getBranchById(id, null).map(dto -> new StoredBranch(dto.getId() == null ? id : dto.getId(), dto));
    }

    @Override
    public Mono<StoredBranch> create(BranchDTO draft) {
        return api.createBranch(draft, key()).map(dto -> new StoredBranch(dto.getId(), dto));
    }

    @Override
    public Mono<StoredBranch> update(UUID id, BranchDTO draft) {
        return api.updateBranch(id, draft, key()).map(dto -> new StoredBranch(dto.getId() == null ? id : dto.getId(), dto));
    }

    @Override
    public Mono<Void> remove(UUID id) {
        return api.deleteBranch(id, key()).then();
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }
}
