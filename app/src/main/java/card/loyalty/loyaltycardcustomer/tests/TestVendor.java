package card.loyalty.loyaltycardcustomer.tests;

import org.junit.Assert;
import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class TestVendor {

    @Test
    public void test() {
        Assert.assertEquals("Test Business", TestsActivity.mVendorReturned.businessName);
    }

}