package com.winapp.saperp.model;

import java.util.ArrayList;

public class AppUtils {
    public static ArrayList<CustomerModel> customersList;
    public static ArrayList<ProductsModel> productsList;

    public static ArrayList<CustomerModel> getCustomersList() {
        return customersList;
    }

    public static void setCustomersList(ArrayList<CustomerModel> customersList) {
        AppUtils.customersList = customersList;
    }

    public static ArrayList<ProductsModel> getProductsList() {
        return productsList;
    }

    public static void setProductsList(ArrayList<ProductsModel> productsList) {
        AppUtils.productsList = productsList;
    }
}
