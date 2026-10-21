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
import io.fireflysoftware.domain.organization.core.branch.commands.UpdateBranchCommand;
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
class UpdateBranchSagaTest {
    @Mock
    private CommandBus commandBus;

    @Test
    void restoreWritesThePreviousBody() {
        UUID id = UUID.randomUUID();
        BranchDTO previousBody = new BranchDTO();
        StoredBranch previous = new StoredBranch(id, previousBody);
        StoredBranch updated = new StoredBranch(id, new BranchDTO());
        when(commandBus.send(any(UpdateBranchCommand.class))).thenReturn(Mono.empty());
        UpdateBranchSaga saga = new UpdateBranchSaga(commandBus);
        ExecutionContext ctx = ExecutionContext.forSaga(UpdateBranchSaga.NAME, "test");
        ctx.putVariable("previous", previous);

        StepVerifier.create(saga.restore(updated, ctx)).verifyComplete();
        ArgumentCaptor<UpdateBranchCommand> captor = ArgumentCaptor.forClass(UpdateBranchCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
        assertThat(captor.getValue().draft()).isSameAs(previousBody);
    }
}
