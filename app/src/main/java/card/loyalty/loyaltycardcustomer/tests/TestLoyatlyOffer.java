package card.loyalty.loyaltycardcustomer.tests;

import org.junit.Assert;
import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 * For testing that the loyalty offer retrieved was the loyalty offer created.
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class TestLoyatlyOffer {

    @Test
    public void test() {
        Assert.assertEquals("Test", TestsActivity.mOfferReturned.description);
    }

}