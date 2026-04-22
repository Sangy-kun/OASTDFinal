package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.model.Collectivity;
import com.hei.TDOASFinal.model.CollectivityStructure;
import com.hei.TDOASFinal.model.Member;
import com.hei.TDOASFinal.repository.CollectivityRepository;
import com.hei.TDOASFinal.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CollectivityService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    public CollectivityService(CollectivityRepository collectivityRepository,
                               MemberRepository memberRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
    }

    public List<Collectivity> createAll(List<Map<String, Object>> payloads) {
        List<Collectivity> result = new ArrayList<>();
        for (Map<String, Object> p : payloads) {
            result.add(createOne(p));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Collectivity createOne(Map<String, Object> p) {
        if (!Boolean.TRUE.equals(p.get("federationApproval"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Federation approval is required.");
        }

        Map<String, String> structurePayload = (Map<String, String>) p.get("structure");
        if (structurePayload == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Structure is required.");
        }

        List<String> memberIds = (List<String>) p.get("members");
        if (memberIds == null || memberIds.size() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least 10 members are required.");
        }

        List<Member> members = new ArrayList<>();
        for (String id : memberIds) members.add(resolve(id));

        CollectivityStructure structure = new CollectivityStructure();
        structure.setPresident(resolve(structurePayload.get("president")));
        structure.setVicePresident(resolve(structurePayload.get("vicePresident")));
        structure.setTreasurer(resolve(structurePayload.get("treasurer")));
        structure.setSecretary(resolve(structurePayload.get("secretary")));

        Collectivity col = new Collectivity();
        col.setId(UUID.randomUUID().toString());
        col.setLocation((String) p.get("location"));
        col.setStructure(structure);
        col.setMembers(members);

        try {
            return collectivityRepository.save(col);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Member resolve(String id) {
        try {
            return memberRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Member not found: " + id));
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}