/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.domain.organization.core.owner;
import java.util.*;
public record CurrentBankOwnerQuery(UUID tenantId, UUID bankId, UUID enrollmentId, long version) {
 public CurrentBankOwnerQuery { Objects.requireNonNull(tenantId);Objects.requireNonNull(bankId);Objects.requireNonNull(enrollmentId);if(version<=0)throw new IllegalArgumentException("Positive exact owner revision required"); }
 public String resource(){return "tenant/"+tenantId+"/bank/"+bankId+"/enrollment/"+enrollmentId+"/version/"+version;}
}
