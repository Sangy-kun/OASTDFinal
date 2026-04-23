package com.hei.TDOASFinal.controller;

import com.hei.TDOASFinal.model.*;
import com.hei.TDOASFinal.service.CollectivityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collectivities")
public class CollectivityController {

    private final CollectivityService collectivityService;

    public CollectivityController(CollectivityService collectivityService) {
        this.collectivityService = collectivityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<Collectivity> create(@RequestBody List<Map<String, Object>> payload) {
        return collectivityService.createAll(payload);
    }

    @PutMapping("/{id}/informations")
    public Collectivity updateInformation(
            @PathVariable String id,
            @RequestBody CollectivityInformation payload) {
        return collectivityService.updateInformation(id, payload);
    }

    @GetMapping("/{id}/membershipFees")
    public List<MembershipFee> getMembershipFees(@PathVariable String id) {
        return collectivityService.getMembershipFees(id);
    }

    @PostMapping("/{id}/membershipFees")
    public List<MembershipFee> createMembershipFees(
            @PathVariable String id,
            @RequestBody List<CreateMembershipFee> payload) {
        return collectivityService.createMembershipFees(id, payload);
    }

    @GetMapping("/{id}/transactions")
    public List<CollectivityTransaction> getTransactions(
            @PathVariable String id,
            @RequestParam("from") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam("to") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) {
        return collectivityService.getTransactions(id, from, to);
    }

    @GetMapping("/{id}")
    public Collectivity getById(@PathVariable String id) {
        return collectivityService.getById(id);
    }

    @GetMapping("/{id}/financialAccounts")
    public List<FinancialAccount> getFinancialAccounts(
            @PathVariable String id,
            @RequestParam(value = "at", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate at) {
        return collectivityService.getFinancialAccounts(id, at);
    }
}