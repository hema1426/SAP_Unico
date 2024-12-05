package com.winapp.saperpUNICO.model;

public class CustSeperateGroupModel {

    private String customerGroupCode;
    private String customerGroupName;

    public String getCustomerGroupCode() {
        return customerGroupCode;
    }

    public void setCustomerGroupCode(String customerGroupCode) {
        this.customerGroupCode = customerGroupCode;
    }

    public String getCustomerGroupName() {
        return customerGroupName;
    }

    public void setCustomerGroupName(String customerGroupName) {
        this.customerGroupName = customerGroupName;
    }

    @Override
    public String toString() {
        return customerGroupName;
    }
}
