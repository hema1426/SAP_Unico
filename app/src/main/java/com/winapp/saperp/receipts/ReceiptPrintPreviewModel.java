package com.winapp.saperp.receipts;

import com.cete.dynamicpdf.merger.S;

import java.util.ArrayList;

public class ReceiptPrintPreviewModel {

    private String customerCode;
    private String customerName;
    private String receiptNumber;
    private String receiptDate;
    private String payMode;
    private String paymentType;
    private String totalAmount;
    private String chequeNo;
    private String chequeDate;
    private String bankCode;
    private String bankName;
    private String bankTransferDate;
    public ArrayList<ReceiptsDetails> receiptsDetailsList;
    private String creditAmount;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getBankTransferDate() {
        return bankTransferDate;
    }

    public void setBankTransferDate(String bankTransferDate) {
        this.bankTransferDate = bankTransferDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
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

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ArrayList<ReceiptsDetails> getReceiptsDetailsList() {
        return receiptsDetailsList;
    }

    public void setReceiptsDetailsList(ArrayList<ReceiptsDetails> receiptsDetailsList) {
        this.receiptsDetailsList = receiptsDetailsList;
    }

    public static class ReceiptsDetails{
        private String invoiceNumber;
        private String invoiceDate;
        private String amount;
        private String discountAmount;


        public String getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(String discountAmount) {
            this.discountAmount = discountAmount;
        }

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        public String getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
}