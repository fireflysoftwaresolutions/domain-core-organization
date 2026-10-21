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

package io.fireflysoftware.domain.organization.core.branch.commands;

import io.fireflysoftware.core.org.sdk.model.BranchDTO;
import io.fireflysoftware.domain.organization.core.branch.StoredBranch;
import org.fireflyframework.cqrs.command.Command;

import java.util.UUID;

public record UpdateBranchCommand(UUID id, BranchDTO draft) implements Command<StoredBranch> {
}
