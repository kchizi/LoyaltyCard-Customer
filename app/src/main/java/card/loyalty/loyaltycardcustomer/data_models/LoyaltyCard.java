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

    // Hybrid key for search
    public String offerID_customerID;

    private String cardID;

    public LoyaltyCard() {};

    public String retrieveCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }
}
