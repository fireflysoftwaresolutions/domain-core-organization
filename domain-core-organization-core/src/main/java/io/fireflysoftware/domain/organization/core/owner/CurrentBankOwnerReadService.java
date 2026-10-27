/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.domain.organization.core.owner;
import reactor.core.publisher.Mono;import java.time.Instant;
public interface CurrentBankOwnerReadService {
 default Mono<CurrentBankOwnerView> current(CurrentBankOwnerQuery query){return current(query,null);}
 Mono<CurrentBankOwnerView> current(CurrentBankOwnerQuery query,Instant notAfter);
}
