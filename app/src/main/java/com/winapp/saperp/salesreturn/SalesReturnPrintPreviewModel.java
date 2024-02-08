package com.winapp.saperp.salesreturn;

import java.util.ArrayList;

public class SalesReturnPrintPreviewModel {
    private String srNo;
    private String srDate;
    private String customerCode;
    private String customerName;
    private String itemDisc;
    private String billDisc;
    private String signFlag;
    private String subTotal;
    private String tax;
    private String netTotal;
    private String taxType;
    private String taxValue;
    private ArrayList<SalesReturnDetails> salesReturnList;

    public String getSignFlag() {
        return signFlag;
    }

    public void setSignFlag(String signFlag) {
        this.signFlag = signFlag;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(String taxValue) {
        this.taxValue = taxValue;
    }

    public String getSrNo() {
        return srNo;
    }

    public void setSrNo(String srNo) {
        this.srNo = srNo;
    }

    public String getSrDate() {
        return srDate;
    }

    public void setSrDate(String srDate) {
        this.srDate = srDate;
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

    public String getItemDisc() {
        return itemDisc;
    }

    public void setItemDisc(String itemDisc) {
        this.itemDisc = itemDisc;
    }

    public String getBillDisc() {
        return billDisc;
    }

    public void setBillDisc(String billDisc) {
        this.billDisc = billDisc;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(String netTotal) {
        this.netTotal = netTotal;
    }

    public ArrayList<SalesReturnDetails> getSalesReturnList() {
        return salesReturnList;
    }

    public void setSalesReturnList(ArrayList<SalesReturnDetails> salesReturnList) {
        this.salesReturnList = salesReturnList;
    }

    public static class SalesReturnDetails{

        private String productCode;
        private String description;
        private String qty;
        private String price;
        private String total;
        private String lqty;
        private String cqty;
        private String netqty;
        private String pcspercarton;
        private String cartonPrice;
        private String subTotal;
        private String tax;

        private String uomCode;


        public String getUomCode() {
            return uomCode;
        }

        public void setUomCode(String uomCode) {
            this.uomCode = uomCode;
        }

        public String getTax() {
            return tax;
        }

        public void setTax(String tax) {
            this.tax = tax;
        }

        public String getSubTotal() {
            return subTotal;
        }

        public void setSubTotal(String subTotal) {
            this.subTotal = subTotal;
        }

        public String getCartonPrice() {
            return cartonPrice;
        }

        public void setCartonPrice(String cartonPrice) {
            this.cartonPrice = cartonPrice;
        }

        public String getLqty() {
            return lqty;
        }

        public void setLqty(String lqty) {
            this.lqty = lqty;
        }

        public String getCqty() {
            return cqty;
        }

        public void setCqty(String cqty) {
            this.cqty = cqty;
        }

        public String getNetqty() {
            return netqty;
        }

        public void setNetqty(String netqty) {
            this.netqty = netqty;
        }

        public String getPcspercarton() {
            return pcspercarton;
        }

        public void setPcspercarton(String pcspercarton) {
            this.pcspercarton = pcspercarton;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getQty() {
            return qty;
        }

        public void setQty(String qty) {
            this.qty = qty;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
