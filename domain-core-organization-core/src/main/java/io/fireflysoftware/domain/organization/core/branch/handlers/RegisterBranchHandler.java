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

package io.fireflysoftware.domain.organization.core.branch.handlers;

import io.fireflysoftware.domain.organization.core.branch.BranchPort;
import io.fireflysoftware.domain.organization.core.branch.StoredBranch;
import io.fireflysoftware.domain.organization.core.branch.commands.RegisterBranchCommand;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import reactor.core.publisher.Mono;

@CommandHandlerComponent
public class RegisterBranchHandler extends CommandHandler<RegisterBranchCommand, StoredBranch> {
    private final BranchPort port;

    public RegisterBranchHandler(BranchPort port) {
        this.port = port;
    }

    @Override
    protected Mono<StoredBranch> doHandle(RegisterBranchCommand command) {
        return port.create(command.draft());
    }
}
