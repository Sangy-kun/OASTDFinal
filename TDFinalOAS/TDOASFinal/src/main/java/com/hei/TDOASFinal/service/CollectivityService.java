package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.model.*;
import com.hei.TDOASFinal.repository.CollectivityRepository;
import com.hei.TDOASFinal.repository.MemberRepository;
import com.hei.TDOASFinal.repository.MembershipFeeRepository;
import com.hei.TDOASFinal.repository.TransactionRepository;
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
    private final MembershipFeeRepository membershipFeeRepository;
    private final TransactionRepository transactionRepository;

    public CollectivityService(CollectivityRepository collectivityRepository,
                               MemberRepository memberRepository,
                               MembershipFeeRepository membershipFeeRepository,
                               TransactionRepository transactionRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.membershipFeeRepository = membershipFeeRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Collectivity> createAll(List<Map<String, Object>> payloads) {
        List<Collectivity> result = new ArrayList<>();
        for (Map<String, Object> p : payloads) {
            result.add(createOne(p));
        }
        return result;
    }

    public Collectivity updateInformation(String id, CollectivityInformation info) {
        String name = info.getName();
        Integer number = info.getNumber();

        if (number == null || name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Both number and name are required.");
        }

        try {
            if (!collectivityRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Collectivity not found: " + id);
            }

            if (collectivityRepository.numberExists(number)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The number " + number + " is already taken by another collectivity.");
            }

            if (collectivityRepository.existsByName(name)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The name '" + name + "' is already taken by another collectivity.");
            }

            return collectivityRepository.updateInformation(id, name, number);

        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<MembershipFee> getMembershipFees(String collectivityId) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collectivity not found");
            }
            return membershipFeeRepository.findByCollectivityId(collectivityId);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<MembershipFee> createMembershipFees(String collectivityId, List<CreateMembershipFee> fees) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collectivity not found");
            }

            List<MembershipFee> result = new ArrayList<>();
            for (CreateMembershipFee dto : fees) {
                if (dto.getAmount() == null || dto.getAmount() < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be under 0");
                }
                if (dto.getFrequency() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unrecognized frequency");
                }

                MembershipFee fee = new MembershipFee();
                fee.setId(UUID.randomUUID().toString());
                fee.setEligibleFrom(dto.getEligibleFrom());
                fee.setFrequency(dto.getFrequency());
                fee.setAmount(dto.getAmount());
                fee.setLabel(dto.getLabel());
                fee.setStatus(ActivityStatus.ACTIVE);

                result.add(membershipFeeRepository.save(collectivityId, fee));
            }
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<CollectivityTransaction> getTransactions(String collectivityId, java.time.LocalDate from, java.time.LocalDate to) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collectivity not found");
            }
            if (from == null || to == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from and to dates are required.");
            }
            if (from.isAfter(to)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date cannot be after to date.");
            }
            return transactionRepository.findByCollectivityAndDates(collectivityId, from, to);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
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
        for (String mid : memberIds) members.add(resolve(mid));

        CollectivityStructure structure = new CollectivityStructure();
        structure.setPresident(resolve(structurePayload.get("president")));
        structure.setVicePresident(resolve(structurePayload.get("vicePresident")));
        structure.setTreasurer(resolve(structurePayload.get("treasurer")));
        structure.setSecretary(resolve(structurePayload.get("secretary")));

        Collectivity col = new Collectivity();
        col.setId(UUID.randomUUID().toString());
        col.setLocation((String) p.get("location"));
        col.setSpecialty((String) p.get("specialty"));
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