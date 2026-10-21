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
import io.fireflysoftware.domain.organization.core.branch.commands.LoadBranchCommand;
import io.fireflysoftware.domain.organization.core.branch.commands.RegisterBranchCommand;
import io.fireflysoftware.domain.organization.core.branch.commands.UpdateBranchCommand;
import io.fireflysoftware.domain.organization.core.branch.workflows.RegisterBranchSaga;
import io.fireflysoftware.domain.organization.core.branch.workflows.UpdateBranchSaga;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BranchService {
    private final SagaEngine engine;
    private final BranchPort port;

    public BranchService(SagaEngine engine, BranchPort port) {
        this.engine = engine;
        this.port = port;
    }

    public Mono<BranchView> read(UUID id) {
        return port.read(id);
    }

    public Mono<StoredBranch> register(BranchDTO draft) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(RegisterBranchSaga.STEP, new RegisterBranchCommand(draft))
                .build();
        return engine.execute(RegisterBranchSaga.NAME, inputs)
                .flatMap(result -> outcome(result, RegisterBranchSaga.STEP, StoredBranch.class));
    }

    public Mono<StoredBranch> update(UUID id, BranchDTO draft) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(UpdateBranchSaga.LOAD, new LoadBranchCommand(id))
                .forStepId(UpdateBranchSaga.STEP, new UpdateBranchCommand(id, draft))
                .build();
        return engine.execute(UpdateBranchSaga.NAME, inputs)
                .flatMap(result -> outcome(result, UpdateBranchSaga.STEP, StoredBranch.class));
    }

    private static <T> Mono<T> outcome(SagaResult result, String stepId, Class<T> type) {
        if (result.isFailed()) {
            Throwable error = result.error().orElseGet(() ->
                    new IllegalStateException("Saga " + result.sagaName() + " failed"));
            return Mono.error(error);
        }
        return Mono.justOrEmpty(result.resultOf(stepId, type))
                .switchIfEmpty(Mono.error(new IllegalStateException("Saga step " + stepId + " produced no result")));
    }
}
