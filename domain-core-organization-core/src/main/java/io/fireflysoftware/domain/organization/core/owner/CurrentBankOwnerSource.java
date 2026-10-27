/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.domain.organization.core.owner;
import reactor.core.publisher.Mono;
import org.fireflyframework.core.transaction.R2dbcOperationBudget;
import java.util.Objects;
public interface CurrentBankOwnerSource {
 Mono<Observation> read(CurrentBankOwnerQuery query,R2dbcOperationBudget original);
 record Observation(CurrentBankOwnerView view,R2dbcOperationBudget window){public Observation{Objects.requireNonNull(view);Objects.requireNonNull(window);}}
}
