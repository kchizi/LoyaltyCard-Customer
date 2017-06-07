package card.loyalty.loyaltycardcustomer.data_models;

/**
 * Created by samclough on 7/06/17.
 */

public class Voucher {

    public String customerID;
    public String promoID;
    public String vendorID;

    public Voucher() {}

    public Voucher(String customerID, String promotionID, String vendorID) {
        this.customerID = customerID;
        this.promoID = promotionID;
        this.vendorID = vendorID;
    }
}
