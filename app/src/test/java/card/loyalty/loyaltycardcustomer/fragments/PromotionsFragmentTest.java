package card.loyalty.loyaltycardcustomer.fragments;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Created by samclough on 11/06/17.
 */
public class PromotionsFragmentTest {

    private static PromotionsFragment mFragment;

    @BeforeClass
    public static void createFragment() {
        mFragment = new PromotionsFragment();
    }

    @AfterClass
    public static void tearDown() {
        mFragment = null;
    }


    /**
     *  Tests that a date that has passed is recognised as passed
     */
    @Test
    public void testPassedDate() {
        try {
            Assert.assertTrue("06/06/2017 should be recognised as in the past, today is ", mFragment.dateHasPassed("06/06/2017"));
        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     *  Tests that a future date (distant future so that this test will last a long time) is recognised as not having passed
     */
    @Test
    public void testFutureDate() {
        try {
            Assert.assertFalse("01/01/2050 should be recognised as in the future", mFragment.dateHasPassed("01/01/2050"));
        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     *  Tests that an invalid date throws an exception when checking that it is a passed date
     */
    @Test (expected = ParseException.class)
    public void testInvalidPassedDate() throws ParseException {
        mFragment.dateHasPassed("1/1///11111");
    }

}