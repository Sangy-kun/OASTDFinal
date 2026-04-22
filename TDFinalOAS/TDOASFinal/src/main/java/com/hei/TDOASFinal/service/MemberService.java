package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.model.Member;
import com.hei.TDOASFinal.model.MemberPayment;
import com.hei.TDOASFinal.repository.*;
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
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final MemberPaymentRepository memberPaymentRepository;
    private final TransactionRepository transactionRepository;

    public MemberService(MemberRepository memberRepository,
                         CollectivityRepository collectivityRepository,
                         MembershipFeeRepository membershipFeeRepository,
                         FinancialAccountRepository financialAccountRepository,
                         MemberPaymentRepository memberPaymentRepository,
                         TransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.collectivityRepository = collectivityRepository;
        this.membershipFeeRepository = membershipFeeRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.memberPaymentRepository = memberPaymentRepository;
        this.transactionRepository = transactionRepository;
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

        if (collectivityId != null) {
            try {
                if (!collectivityRepository.existsById(collectivityId)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Collectivity not found: " + collectivityId);
                }
            } catch (SQLException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        List<String> refereeIds = (List<String>) p.get("referees");

        if (collectivityId != null) {
            if (refereeIds == null || refereeIds.size() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "At least 2 confirmed referees are required.");
            }
        }

        List<Member> referees = new ArrayList<>();
        long fromTarget = 0;

        if (refereeIds != null && !refereeIds.isEmpty()) {
            for (String rid : refereeIds) {
                try {
                    Member referee = memberRepository.findById(rid)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Referee not found: " + rid));
                    if (!"SENIOR".equalsIgnoreCase(referee.getOccupation())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Referee " + rid + " must be a confirmed member (SENIOR).");
                    }
                    if (collectivityId != null && collectivityId.equals(referee.getCollectivityId())) {
                        fromTarget++;
                    }
                    referees.add(referee);
                } catch (SQLException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }

            if (collectivityId != null && fromTarget < (refereeIds.size() - fromTarget)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Referees from the target collectivity must be >= referees from other collectivities.");
            }
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
        member.setReferees(referees.isEmpty() ? null : referees);

        try {
            Member saved = memberRepository.save(member);
            if (collectivityId != null) {
                memberRepository.updateCollectivityId(saved.getId(), collectivityId);
                saved.setCollectivityId(collectivityId);
            }
            saved.setReferees(referees.isEmpty() ? null : referees);
            return saved;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<MemberPayment> createPayments(String memberId, List<com.hei.TDOASFinal.model.CreateMemberPayment> payloads) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

            List<MemberPayment> result = new ArrayList<>();
            for (com.hei.TDOASFinal.model.CreateMemberPayment dto : payloads) {
                if (dto.getAmount() == null || dto.getAmount() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
                }

                com.hei.TDOASFinal.model.MembershipFee fee = membershipFeeRepository.findById(dto.getMembershipFeeIdentifier())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership fee not found"));

                com.hei.TDOASFinal.model.FinancialAccount account = financialAccountRepository.findById(dto.getAccountCreditedIdentifier())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Financial account not found"));

                MemberPayment payment = new com.hei.TDOASFinal.model.MemberPayment();
                payment.setId(java.util.UUID.randomUUID().toString());
                payment.setAmount(dto.getAmount());
                payment.setPaymentMode(dto.getPaymentMode());
                payment.setCreationDate(java.time.LocalDate.now());

                memberPaymentRepository.save(payment, memberId, fee.getId(), account.getId());

                com.hei.TDOASFinal.model.CollectivityTransaction transaction = new com.hei.TDOASFinal.model.CollectivityTransaction();
                transaction.setId(java.util.UUID.randomUUID().toString());
                transaction.setCreationDate(java.time.LocalDate.now());
                transaction.setAmount(Double.valueOf(dto.getAmount()));
                transaction.setPaymentMode(dto.getPaymentMode());
                transaction.setAccountCredited(account);
                transaction.setMemberDebited(member);

                transactionRepository.save(fee.getCollectivityId(), transaction);

                financialAccountRepository.creditAccount(account.getId(), dto.getAmount());

                result.add(payment);
            }
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}