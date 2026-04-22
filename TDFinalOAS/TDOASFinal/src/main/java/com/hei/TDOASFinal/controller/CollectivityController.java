package com.hei.TDOASFinal.controller;

import com.hei.TDOASFinal.model.Collectivity;
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

    @PatchMapping("/{id}/identity")
    public Collectivity assignIdentity(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        return collectivityService.assignNumberAndName(id, payload);
    }
}