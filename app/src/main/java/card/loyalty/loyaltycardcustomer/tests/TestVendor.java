package card.loyalty.loyaltycardcustomer.tests;

import org.junit.Assert;
import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 * For testing that the vendor received from firebase was the vendor created
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class TestVendor {

    // Tests that the business name is retrieved correctly
    @Test
    public void testVendorBusinessName() {
        Assert.assertEquals("Test Business", TestsActivity.mVendorReturned.businessName);
    }

    // Tests that the business address is retrieved correctly
    @Test
    public void testVendorAddress() {
        Assert.assertEquals("test", TestsActivity.mVendorReturned.businessAddress);
    }

}