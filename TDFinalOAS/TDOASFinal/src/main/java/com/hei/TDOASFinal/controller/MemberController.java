package com.hei.TDOASFinal.controller;

import com.hei.TDOASFinal.model.Member;
import com.hei.TDOASFinal.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<Member> create(@RequestBody List<Map<String, Object>> payload) {
        return memberService.createAll(payload);
    }
}