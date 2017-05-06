package card.loyalty.loyaltycardcustomer.tests;

import org.junit.Assert;
import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 * For testing that the loyalty offer retrieved from firebase was the loyalty offer created.
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class TestLoyatlyOffer {

    // Tests that the offer description is retrieved correctly
    @Test
    public void testOfferDescription() {
        Assert.assertEquals("Test", TestsActivity.mOfferReturned.description);
    }

    // Tests that the vendorID is retrieved correctly
    @Test
    public void testOfferVendorID() {
        Assert.assertEquals("Test", TestsActivity.mOfferReturned.vendorID);
    }

    // Tests that the purchases per reward count is retrieved correctly
    @Test
    public void testOfferPurchasesPerReward() {
        Assert.assertEquals("Test", TestsActivity.mOfferReturned.purchasesPerReward);
    }

    // Tests that the reward (description of reward) is retrieved correctly
    @Test
    public void testOfferReward() {
        Assert.assertEquals("Test", TestsActivity.mOfferReturned.reward);
    }


}