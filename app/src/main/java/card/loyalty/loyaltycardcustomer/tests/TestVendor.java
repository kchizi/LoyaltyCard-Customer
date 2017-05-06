package card.loyalty.loyaltycardcustomer.tests;

import org.junit.Assert;
import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 * For testing that the vendor received was the vendor created
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class TestVendor {

    @Test
    public void test() {
        Assert.assertEquals("Test Business", TestsActivity.mVendorReturned.businessName);
    }

}