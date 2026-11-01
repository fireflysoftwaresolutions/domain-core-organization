/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.domain.organization.web.owner;
import io.fireflysoftware.domain.organization.core.owner.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;import java.util.UUID;
@RestController
@ConditionalOnProperty(name="firefly.organization.current-owner.enabled",havingValue="true",matchIfMissing=false)
@SecurityScheme(name="organizationBearer",type=SecuritySchemeType.HTTP,scheme="bearer",bearerFormat="JWT")
@SecurityRequirement(name="organizationBearer")
@RequestMapping("/api/v1/organization/tenants/{tenantId}/banks/{bankId}/enrollments")
public final class CurrentBankOwnerController{
 private final CurrentBankOwnerReadService service;
 public CurrentBankOwnerController(CurrentBankOwnerReadService service){this.service=service;}
 @Operation(operationId="getCurrentBankOwner",tags="Bank Enrollment Owner",summary="Read an exact current Bank enrollment owner",description="Requires current administrative authority at both boundaries and a fresh original owner response. The bounded no-store summary is not financial, product, currency or book admission.")
 @io.swagger.v3.oas.annotations.responses.ApiResponses({
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Exact bounded no-store administrative owner summary",content=@io.swagger.v3.oas.annotations.media.Content(mediaType="application/json",schema=@io.swagger.v3.oas.annotations.media.Schema(implementation=CurrentBankOwnerView.class))),
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400",description="Invalid exact owner selectors",content=@io.swagger.v3.oas.annotations.media.Content),
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403",description="Current administrative read denied",content=@io.swagger.v3.oas.annotations.media.Content),
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409",description="The exact owner is not current",content=@io.swagger.v3.oas.annotations.media.Content),
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503",description="A current source or finite observation is unavailable",content=@io.swagger.v3.oas.annotations.media.Content)})
 @GetMapping(value="/{enrollmentId}/versions/{version}/current",produces=MediaType.APPLICATION_JSON_VALUE)
 public Mono<ResponseEntity<CurrentBankOwnerView>> current(@PathVariable("tenantId") UUID tenantId,@PathVariable("bankId") UUID bankId,@PathVariable("enrollmentId") UUID enrollmentId,@PathVariable("version") long version,@RequestHeader(value="X-Firefly-Operation-Deadline",required=false) Long operationDeadline){if(version<=0||operationDeadline!=null&&operationDeadline<=0)return Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Positive exact enrollment version required"));return Mono.defer(()->service.current(new CurrentBankOwnerQuery(tenantId,bankId,enrollmentId,version),operationDeadline==null?null:java.time.Instant.ofEpochMilli(operationDeadline))).map(value->ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value));}
}
