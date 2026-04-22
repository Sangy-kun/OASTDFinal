package com.hei.TDOASFinal.service;

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

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final CollectivityRepository collectivityRepository;

    public MemberService(MemberRepository memberRepository,
                         CollectivityRepository collectivityRepository) {
        this.memberRepository = memberRepository;
        this.collectivityRepository = collectivityRepository;
    }

    public List<Member> createAll(List<Map<String, Object>> payloads) {
        List<Member> result = new ArrayList<>();
        for (Map<String, Object> p : payloads) {
            result.add(createOne(p));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Member createOne(Map<String, Object> p) {
        boolean regPaid = Boolean.TRUE.equals(p.get("registrationFeePaid"));
        boolean duePaid = Boolean.TRUE.equals(p.get("membershipDuesPaid"));
        if (!regPaid || !duePaid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration fee and membership dues must be paid.");
        }

        String collectivityId = (String) p.get("collectivityIdentifier");
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Collectivity not found: " + collectivityId);
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        List<String> refereeIds = (List<String>) p.get("referees");
        if (refereeIds == null || refereeIds.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least 2 confirmed referees are required.");
        }

        List<Member> referees = new ArrayList<>();
        long fromTarget = 0;

        for (String rid : refereeIds) {
            try {
                Member referee = memberRepository.findById(rid)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Referee not found: " + rid));
                if (!"SENIOR".equalsIgnoreCase(referee.getOccupation())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Referee " + rid + " must be a confirmed member (SENIOR).");
                }
                if (collectivityId.equals(referee.getCollectivityId())) fromTarget++;
                referees.add(referee);
            } catch (SQLException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        if (fromTarget < (refereeIds.size() - fromTarget)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Referees from the target collectivity must be >= referees from other collectivities.");
        }

        Member member = new Member();
        member.setFirstName((String) p.get("firstName"));
        member.setLastName((String) p.get("lastName"));
        member.setBirthDate((String) p.get("birthDate"));
        member.setGender((String) p.get("gender"));
        member.setAddress((String) p.get("address"));
        member.setProfession((String) p.get("profession"));
        member.setPhoneNumber(Long.parseLong(p.get("phoneNumber").toString()));
        member.setEmail((String) p.get("email"));
        member.setOccupation((String) p.get("occupation"));
        member.setReferees(referees);

        try {
            Member saved = memberRepository.save(member);
            memberRepository.updateCollectivityId(saved.getId(), collectivityId);
            saved.setReferees(referees);
            return saved;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}