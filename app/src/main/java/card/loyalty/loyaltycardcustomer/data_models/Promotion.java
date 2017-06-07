package card.loyalty.loyaltycardcustomer.data_models;

/**
 * Created by samclough on 7/06/17.
 */

public class Promotion {

    public String title;
    public String description;
    public String creationDate;
    public String expiryDate;
    public String vendorId;

    // Stores the vendor
    private Vendor vendor;

    // default constructor required for Firebase
    public Promotion(){}

    public Promotion(String title, String description, String creationDate, String expiryDate, String vendorId) {
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.expiryDate = expiryDate;
        this.vendorId = vendorId;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Vendor retrieveVendor() {
        return this.vendor;
    }

}
