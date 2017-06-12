package card.loyalty.loyaltycardcustomer.data_models;

/**
 * Created by Caleb T on 12/06/2017.
 */

public class LoyaltyReward {
    public String customerID;
    public String offerID;
    public String rewardDesc;
    public String vendorID;
    public String cardID_customerID;

    public String rewardID;

    public Vendor vendor;

    public String businessName;

    public LoyaltyReward() {}

    public LoyaltyReward(String customerID, String offerID, String rewardDesc, String vendorID) {
        this.customerID = customerID;
        this.offerID = offerID;
        this.rewardDesc = rewardDesc;
        this.vendorID = vendorID;
    }

    public String getRewardID() {
        return rewardID;
    }

    public void setRewardID(String rewardID) {
        this.rewardID = rewardID;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }
    public Vendor retrieveVendor() {
        return vendor;
    }

    public String retrieveBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

}
