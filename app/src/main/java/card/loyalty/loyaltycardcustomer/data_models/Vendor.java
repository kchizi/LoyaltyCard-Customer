package card.loyalty.loyaltycardcustomer.data_models;

/**
 * Created by Sam on 26/04/2017.
 */

public class Vendor {

    // Public fields for Firebase interaction
    public String businessName;
//    public String address;

    private String vendorID;

    public Vendor() {}

    public String retrieveVendorID() {
        return vendorID;
    }
    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }
}
