package com.zornflow.interfaces.web.controller;

import com.zornflow.application.dto.rulechain.CreateRuleChainRequest;
import com.zornflow.application.dto.rulechain.RuleChainResponse;
import com.zornflow.application.service.RuleChainAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/rule-chains")
@RequiredArgsConstructor
public class RuleChainAdminController {

  private final RuleChainAdminService ruleChainAdminService;

  @PostMapping
  public ResponseEntity<RuleChainResponse> createRuleChain(@Valid @RequestBody CreateRuleChainRequest request) {
    RuleChainResponse response = ruleChainAdminService.createRuleChain(request);
    return ResponseEntity.created(URI.create("/api/admin/rule-chains/" + response.id())).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<RuleChainResponse> getRuleChainById(@PathVariable String id) {
    return ruleChainAdminService.getRuleChainById(id)
      .map(ResponseEntity::ok)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RuleChain not found with id: " + id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRuleChain(@PathVariable String id) {
    ruleChainAdminService.deleteRuleChain(id);
  }
}
