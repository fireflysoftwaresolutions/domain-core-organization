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

package io.fireflysoftware.domain.organization.core.branch.workflows;

import io.fireflysoftware.core.org.sdk.model.BranchDTO;
import io.fireflysoftware.domain.organization.core.branch.StoredBranch;
import io.fireflysoftware.domain.organization.core.branch.commands.RegisterBranchCommand;
import io.fireflysoftware.domain.organization.core.branch.commands.RemoveBranchCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterBranchSagaTest {
    @Mock
    private CommandBus commandBus;

    @Test
    void registerStoresTheCoreResult() {
        UUID id = UUID.randomUUID();
        StoredBranch created = new StoredBranch(id, new BranchDTO());
        when(commandBus.send(any(RegisterBranchCommand.class))).thenReturn(Mono.just(created));
        RegisterBranchSaga saga = new RegisterBranchSaga(commandBus);
        ExecutionContext ctx = ExecutionContext.forSaga(RegisterBranchSaga.NAME, "test");

        StepVerifier.create(saga.register(new RegisterBranchCommand(new BranchDTO()), ctx))
                .expectNext(created)
                .verifyComplete();
        assertThat(ctx.getVariableAs("created", StoredBranch.class).id()).isEqualTo(id);
    }

    @Test
    void removeSendsDeleteForTheCreatedRecord() {
        UUID id = UUID.randomUUID();
        when(commandBus.send(any(RemoveBranchCommand.class))).thenReturn(Mono.empty());
        RegisterBranchSaga saga = new RegisterBranchSaga(commandBus);

        StepVerifier.create(saga.remove(new StoredBranch(id, new BranchDTO())))
                .verifyComplete();
        ArgumentCaptor<RemoveBranchCommand> captor = ArgumentCaptor.forClass(RemoveBranchCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
    }
}
