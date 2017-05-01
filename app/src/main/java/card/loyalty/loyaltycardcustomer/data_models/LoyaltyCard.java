package card.loyalty.loyaltycardcustomer.data_models;

/**
 * Created by Sam on 25/04/2017.
 */

public class LoyaltyCard {

    // Public fields for Firebase interaction
    public String offerID;
    public String customerID;
    public String purchaseCount;
    public String rewardsIssued;
    public String rewardsClaimed;
    public String vendorID;

    // TODO: add vendor ID to database and as public method in both projects

    // Hybrid key for search
    public String offerID_customerID;

    // Database key
    private String cardID;

    // Private fields for details that need displaying but are from separate database tables/branches
    private String offerDescription;
    private String businessName;

    public LoyaltyCard() {};

    // retrievers instead of getters so Firebase won't have access
    public String retrieveCardID() {
        return cardID;
    }

    public String retrieveOfferDescription() {
        return offerDescription;
    }

    public String retrieveBusinessName() {
        return businessName;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}
