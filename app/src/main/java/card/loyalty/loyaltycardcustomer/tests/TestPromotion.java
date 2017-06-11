package card.loyalty.loyaltycardcustomer.tests;

import junit.framework.Assert;

import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 *  Tests that the promotion received is the same promotion that was set up
 *
 * Created by samclough on 11/06/17.
 */

public class TestPromotion {

    // tests that the creation date is set
    @Test
    public void testCreationDate() {
        Assert.assertEquals("promo", TestsActivity.mPromotionReturned.creationDate);
    }

    // tests that the description is set
    @Test
    public void testDescription() {
        Assert.assertEquals("promo", TestsActivity.mPromotionReturned.description);
    }

    // tests that the expiry date is set
    @Test
    public void testExpiryDate() {
        Assert.assertEquals("promo", TestsActivity.mPromotionReturned.expiryDate);
    }

    // tests that the title is set
    @Test
    public void testTitle() {
        Assert.assertEquals("promo", TestsActivity.mPromotionReturned.title);
    }

    // tests that the vendorId is set
    @Test
    public void testVendorId() {
        Assert.assertEquals("promo", TestsActivity.mPromotionReturned.vendorId);
    }

}
