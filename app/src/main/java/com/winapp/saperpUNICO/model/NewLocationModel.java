package com.winapp.saperpUNICO.model;

import java.util.ArrayList;

public class NewLocationModel {

    public ArrayList<LocationDetails> locationDetailsArrayList;

    public ArrayList<LocationDetails> getLocationDetailsArrayList() {
        return locationDetailsArrayList;
    }

    public void setLocationDetailsArrayList(ArrayList<LocationDetails> locationDetailsArrayList) {
        this.locationDetailsArrayList = locationDetailsArrayList;
    }

    public static class LocationDetails {

        String locationCode;
        String locationName;

        public String getLocationCode() {
            return locationCode;
        }

        public void setLocationCode(String locationCode) {
            this.locationCode = locationCode;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }
    }
}
