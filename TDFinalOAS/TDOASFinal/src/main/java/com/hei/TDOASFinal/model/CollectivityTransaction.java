package com.hei.TDOASFinal.model;

import java.time.LocalDate;

public class CollectivityTransaction {
    private String id;
    private LocalDate creationDate;
    private Double amount;
    private PaymentMode paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }
    public FinancialAccount getAccountCredited() { return accountCredited; }
    public void setAccountCredited(FinancialAccount accountCredited) { this.accountCredited = accountCredited; }
    public Member getMemberDebited() { return memberDebited; }
    public void setMemberDebited(Member memberDebited) { this.memberDebited = memberDebited; }
}
