package com.hei.TDOASFinal.model;

public class BankAccount extends FinancialAccount {
    private String holderName;
    private Bank bankName;
    private Integer bankCode;
    private Integer bankBranchCode;
    private Integer bankAccountNumber;
    private Integer bankAccountKey;
    private Double amount;

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public Bank getBankName() { return bankName; }
    public void setBankName(Bank bankName) { this.bankName = bankName; }
    public Integer getBankCode() { return bankCode; }
    public void setBankCode(Integer bankCode) { this.bankCode = bankCode; }
    public Integer getBankBranchCode() { return bankBranchCode; }
    public void setBankBranchCode(Integer bankBranchCode) { this.bankBranchCode = bankBranchCode; }
    public Integer getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(Integer bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public Integer getBankAccountKey() { return bankAccountKey; }
    public void setBankAccountKey(Integer bankAccountKey) { this.bankAccountKey = bankAccountKey; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
