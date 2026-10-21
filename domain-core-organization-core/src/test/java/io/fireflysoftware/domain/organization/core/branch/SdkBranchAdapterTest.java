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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SdkBranchAdapterTest {
    @Test
    void sandboxIsTheDefaultAndDoesNotOpenASocket() {
        new ApplicationContextRunner()
                .withUserConfiguration(BranchAdapterConfiguration.class)
                .run(context -> {
                    BranchPort port = context.getBean(BranchPort.class);
                    assertThat(port).isInstanceOf(SandboxBranchAdapter.class);
                    assertThat(port.read(null).block().label()).isEqualTo("HQ");
                });
    }

    @Test
    void corePropertySelectsTheSdkAdapter() {
        new ApplicationContextRunner()
                .withUserConfiguration(BranchAdapterConfiguration.class, io.fireflysoftware.domain.organization.infra.BranchClientFactory.class)
                .withPropertyValues("firefly.organization.branch.adapter=core")
                .run(context -> assertThat(context.getBean(BranchPort.class)).isInstanceOf(SdkBranchAdapter.class));
    }

    @Test
    void liveReadCallsTheCoreSdk() {
        UUID id = UUID.randomUUID();
        BranchManagementApi api = mock(BranchManagementApi.class);
        BranchDTO dto = mock(BranchDTO.class);
        when(dto.getId()).thenReturn(id);
        when(api.getBranchById(id, null)).thenReturn(Mono.just(dto));
        BranchView view = new SdkBranchAdapter(api).read(id).block();
        assertThat(view.id()).isEqualTo(id);
        verify(api).getBranchById(id, null);
    }
}
