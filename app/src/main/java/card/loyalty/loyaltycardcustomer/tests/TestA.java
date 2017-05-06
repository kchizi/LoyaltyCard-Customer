package card.loyalty.loyaltycardcustomer.tests;

import org.junit.Assert;
import org.junit.Test;

import card.loyalty.loyaltycardcustomer.TestsActivity;

/**
 *  For asynchronous testing. This tests the test A passed. Can be used for any test, the purpose
 *  of splitting into A and B is that we don't know the order of execution so don't want the variable
 *  to change before the test is executed. If doing two tests simultaneously as we currently are we need
 *  test A and test B, if more async tests to run simultaneously then more needed
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class TestA {

    @Test
    public void test() {
        Assert.assertTrue(TestsActivity.mTestA_Passed);
    }

}