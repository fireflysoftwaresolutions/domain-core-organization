/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.domain.organization.core.owner.impl;
import io.fireflysoftware.domain.organization.core.owner.*;
import org.fireflyframework.core.transaction.R2dbcOperationBudget;
import org.fireflyframework.security.api.domain.SecurityPrincipal;
import org.fireflyframework.security.spi.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.time.*;
import java.util.*;
import java.util.function.LongSupplier;
public final class CurrentBankOwnerReadServiceImpl implements CurrentBankOwnerReadService {
 private static final Duration MAXIMUM=Duration.ofSeconds(10);
 private final SecurityContextPort contexts; private final PolicyDecisionPort policies; private final CurrentBankOwnerSource source; private final Clock clock; private final LongSupplier nanos;
 public CurrentBankOwnerReadServiceImpl(SecurityContextPort contexts,PolicyDecisionPort policies,CurrentBankOwnerSource source,Clock clock,LongSupplier nanos){this.contexts=Objects.requireNonNull(contexts);this.policies=Objects.requireNonNull(policies);this.source=Objects.requireNonNull(source);this.clock=Objects.requireNonNull(clock);this.nanos=Objects.requireNonNull(nanos);}
 @Override public Mono<CurrentBankOwnerView> current(CurrentBankOwnerQuery query,Instant notAfter){return Mono.defer(()->{
  Objects.requireNonNull(query);long before=nanos.getAsLong();Instant started=clock.instant();Instant initialUntil=notAfter==null?started.plus(MAXIMUM):earliest(started.plus(MAXIMUM),notAfter);var initial=budget(started,initialUntil,before);
  return contexts.currentPrincipal().switchIfEmpty(Mono.error(forbidden())).flatMap(actor->{
   requireActor(actor,query);Instant callerUntil=(Instant)actor.claims().get("exp");Instant originalUntil=earliest(initialUntil,callerUntil);var original=budget(started,originalUntil,before);
   return authorize(actor,query,original).then(Mono.defer(()->source.read(query,original))).switchIfEmpty(Mono.error(unavailable())).flatMap(observation->{
    var view=observation.view();
    if(!view.matches(query)||view.currentUntilEpochMillis()<=clock.millis())return Mono.error(unavailable());
    var delivery=budget(started,earliest(earliest(originalUntil,Instant.ofEpochMilli(view.observationUntilEpochMillis())),Instant.ofEpochMilli(view.currentUntilEpochMillis()).minusMillis(1)),before).intersect(observation.window());
    return contexts.currentPrincipal().filter(actor::equals).switchIfEmpty(Mono.error(forbidden())).flatMap(current->authorize(current,query,delivery)).then(Mono.fromSupplier(()->{Instant sampled=clock.instant();long handoff=sampled.plus(delivery.remaining()).toEpochMilli();return view.withObservationUntil(handoff);})).timeout(delivery.remaining());
   }).timeout(original.remaining());
  }).timeout(initial.remaining());
 }).onErrorMap(error->error instanceof ResponseStatusException?error:unavailable());}
 private Mono<Void> authorize(SecurityPrincipal actor,CurrentBankOwnerQuery query,R2dbcOperationBudget budget){return Mono.defer(()->{
  requireActor(actor,query);budget.remaining();return policies.authorize(actor,"organization.bank.enrollment.owner.current.read",query.resource(),Map.of("tenantId",query.tenantId().toString(),"bankId",query.bankId().toString(),"enrollmentId",query.enrollmentId().toString(),"version",Long.toString(query.version()),"purpose","ADMINISTRATIVE_CURRENT_OWNER_READ")).filter(d->d.granted()&&d.obligations().isEmpty()).switchIfEmpty(Mono.error(forbidden())).timeout(budget.remaining()).doOnNext(d->{requireActor(actor,query);budget.remaining();}).then();});}
 private void requireActor(SecurityPrincipal actor,CurrentBankOwnerQuery query){if(actor==null||actor.subject()==null||actor.subject().isBlank()||actor.issuer()==null||actor.issuer().isBlank()||!query.tenantId().toString().equals(actor.tenantId())||!(actor.claims().get("exp") instanceof Instant expires)||!expires.isAfter(clock.instant()))throw forbidden();}
 private R2dbcOperationBudget budget(Instant started,Instant until,long before){var v=R2dbcOperationBudget.observed(started,until,before,MAXIMUM,clock,nanos);v.remaining();return v;}
 private static Instant earliest(Instant a,Instant b){return a.isBefore(b)?a:b;}
 private static ResponseStatusException forbidden(){return new ResponseStatusException(HttpStatus.FORBIDDEN,"Current exact administrative owner-read authority required");}
 private static ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Current owner observation unavailable");}
}
