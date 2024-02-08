package com.winapp.saperp.model;

import java.util.ArrayList;

public class SettlementReceiptModel {
    // Define the variables for the Settlement
    private String receiptNo;
    private String receiptDate;
    private String customerName;
    private String customerCode;
    private String paidAmount;
    private String creditAmount;
    private String finalPaidAmount;
    private String paymode;
    private String bankCode;
    private String chequeNo;
    private String chequeDate;
    private String totalInvoiceAmount;
    private String totalPaidAmount;
    private String totalLessAmount;
    private String totalCashAmount;
    private String totalChequeAmount;
    private ArrayList<CurrencyDenomination> currencyDenominations;
    private ArrayList<Expense> expenses;

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(String paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getFinalPaidAmount() {
        return finalPaidAmount;
    }

    public void setFinalPaidAmount(String finalPaidAmount) {
        this.finalPaidAmount = finalPaidAmount;
    }

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getChequeNo() {
        return chequeNo;
    }

    public void setChequeNo(String chequeNo) {
        this.chequeNo = chequeNo;
    }

    public String getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(String chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getTotalInvoiceAmount() {
        return totalInvoiceAmount;
    }

    public void setTotalInvoiceAmount(String totalInvoiceAmount) {
        this.totalInvoiceAmount = totalInvoiceAmount;
    }

    public String getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(String totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public String getTotalLessAmount() {
        return totalLessAmount;
    }

    public void setTotalLessAmount(String totalLessAmount) {
        this.totalLessAmount = totalLessAmount;
    }

    public String getTotalCashAmount() {
        return totalCashAmount;
    }

    public void setTotalCashAmount(String totalCashAmount) {
        this.totalCashAmount = totalCashAmount;
    }

    public String getTotalChequeAmount() {
        return totalChequeAmount;
    }

    public void setTotalChequeAmount(String totalChequeAmount) {
        this.totalChequeAmount = totalChequeAmount;
    }

    public ArrayList<CurrencyDenomination> getCurrencyDenominations() {
        return currencyDenominations;
    }

    public void setCurrencyDenominations(ArrayList<CurrencyDenomination> currencyDenominations) {
        this.currencyDenominations = currencyDenominations;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    public static class CurrencyDenomination{

        // Define the variables for the Currency
        private String denomination;
        private String count;
        private String total;

        public String getDenomination() {
            return denomination;
        }

        public void setDenomination(String denomination) {
            this.denomination = denomination;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }

    public static class Expense{
        // Define the variables for the Expenses

        private String expeneName;
        private String expenseTotal;

        public String getExpeneName() {
            return expeneName;
        }

        public void setExpeneName(String expeneName) {
            this.expeneName = expeneName;
        }

        public String getExpenseTotal() {
            return expenseTotal;
        }

        public void setExpenseTotal(String expenseTotal) {
            this.expenseTotal = expenseTotal;
        }
    }
}
