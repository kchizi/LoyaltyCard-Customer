package card.loyalty.loyaltycardcustomer.data_models;

/**
 * Created by Sam on 26/04/2017.
 */

public class Vendor {

    // Public fields for Firebase interaction
    public String businessName;
    public String businessAddress;

    private String vendorID;

    public Vendor() {}

    public Vendor(String businessName, String businessAddress) {

        this.businessName = businessName;
        this.businessAddress = businessAddress;
    }


    public String retrieveVendorID() {
        return vendorID;
    }
    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getBusinessName() {
        return businessName;
    }
    }





