package card.loyalty.loyaltycardcustomer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestsActivity extends AppCompatActivity {
    private static final String TAG = "TestsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);


        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        Result result = core.run(UnitTests.class);

        Log.d(TAG, "number of tests: " + result.getRunCount());
        Log.d(TAG, "failure count: " + result.getFailureCount());
        Log.d(TAG, "was successful:" + result.wasSuccessful());
    }


}
