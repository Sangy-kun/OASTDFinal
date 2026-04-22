package com.hei.TDOASFinal.model;

public class CreateMemberPayment {
    private Integer amount;
    private String membershipFeeIdentifier;
    private String accountCreditedIdentifier;
    private PaymentMode paymentMode;

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getMembershipFeeIdentifier() { return membershipFeeIdentifier; }
    public void setMembershipFeeIdentifier(String membershipFeeIdentifier) { this.membershipFeeIdentifier = membershipFeeIdentifier; }
    public String getAccountCreditedIdentifier() { return accountCreditedIdentifier; }
    public void setAccountCreditedIdentifier(String accountCreditedIdentifier) { this.accountCreditedIdentifier = accountCreditedIdentifier; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }
}
