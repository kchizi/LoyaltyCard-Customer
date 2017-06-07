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

    // default constructor required for Firebase
    public Promotion(){}

    public Promotion(String title, String description, String creationDate, String expiryDate, String vendorId) {
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.expiryDate = expiryDate;
        this.vendorId = vendorId;
    }

}
