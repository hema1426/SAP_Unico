package com.winapp.saperpUNICO.model;

import java.util.ArrayList;

public class ReportPostingInvSOModel {

    private String fromDate;
    private String toDate;
    private String companyId;

    public ArrayList<ReportInvoiceDetails> reportInvoiceDetailsArrayList;
    public ArrayList<ReportSODetails> reportSODetailsArrayList;

    public ArrayList<ReportInvoiceDetails> getReportInvoiceDetailsArrayList() {
        return reportInvoiceDetailsArrayList;
    }

    public void setReportInvoiceDetailsArrayList(ArrayList<ReportInvoiceDetails> reportInvoiceDetailsArrayList) {
        this.reportInvoiceDetailsArrayList = reportInvoiceDetailsArrayList;
    }

    public ArrayList<ReportSODetails> getReportSODetailsArrayList() {
        return reportSODetailsArrayList;
    }

    public void setReportSODetailsArrayList(ArrayList<ReportSODetails> reportSODetailsArrayList) {
        this.reportSODetailsArrayList = reportSODetailsArrayList;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }


    public static class ReportInvoiceDetails{

       private String invoiceNo;
        private String invoiceDate;
        private String total;
        private String balance;
        private String customerCode;
        private String customerName;

        public String getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public String getInvoiceNo() {
            return invoiceNo;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
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
    }
    public static class ReportSODetails{

        private String invoiceNo;
        private String invoiceDate;
        private String total;
        private String balance;
        private String customerCode;
        private String customerName;

        public String getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public String getInvoiceNo() {
            return invoiceNo;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
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
    }

}
