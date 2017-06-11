package card.loyalty.loyaltycardcustomer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.File;
import java.io.IOException;

import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.Promotion;
import card.loyalty.loyaltycardcustomer.data_models.Vendor;
import card.loyalty.loyaltycardcustomer.observables.RxFirebase;
import card.loyalty.loyaltycardcustomer.tests.TestA;
import card.loyalty.loyaltycardcustomer.tests.TestB;
import card.loyalty.loyaltycardcustomer.tests.TestC;
import card.loyalty.loyaltycardcustomer.tests.TestD;
import card.loyalty.loyaltycardcustomer.tests.TestLoyatlyOffer;
import card.loyalty.loyaltycardcustomer.tests.TestPromotion;
import card.loyalty.loyaltycardcustomer.tests.TestVendor;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class TestsActivity extends AppCompatActivity {
    private static final String TAG = "TestsActivity";

    private static FirebaseAuth mAuth;
    private static DatabaseReference mTestsReference;
    private static StorageReference mStorageReference;

    // Total number of tests including setup tests and teardown tests
    private static final int NUMBER_OF_TESTS = 11;
    private static final int NUMBER_OF_SETUP_TESTS = 4;
    private static final int NUMBER_OF_TEARDOWN_TESTS = 4;

    // Total number of successful tests so far
    private static int mTestsSuccessful;

    // Number of tests done in the setup section. Used to see if it is time to launch the main tests section
    private static int mSetupTestsDone;

    // Number of tests done in the main test section (excluding the setup and teardown tests).
    // This is used so that the test callbacks can see if they should initiate the teardown code.
    private static int mMainTestsDone;

    public static Boolean mTestA_Passed;
    public static Boolean mTestB_Passed;
    public static Boolean mTestC_Passed;
    public static Boolean mTestD_Passed;
    public static LoyaltyOffer mOfferReturned;
    public static Vendor mVendorReturned;
    public static Promotion mPromotionReturned;
    public static File mUploadContent;
    public static File mDownloadContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);

        mTestsSuccessful = 0;

        mAuth = FirebaseAuth.getInstance();
        mTestsReference = FirebaseDatabase.getInstance().getReference().child("Tests");
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Tests");

        updateView();

        preTests();

    }

    private void updateView() {
        TextView totalTests = (TextView) findViewById(R.id.text_totalTests);
        TextView passedTests = (TextView) findViewById(R.id.text_passedTests);
        totalTests.setText("" + NUMBER_OF_TESTS);
        if (!passedTests.getText().toString().equals("SOME FAILED")) {
            passedTests.setText("" + mTestsSuccessful);
        }
    }

    private void testFailed() {
        TextView passedTests = (TextView) findViewById(R.id.text_passedTests);
        passedTests.setText("SOME FAILED");
    }

    /**
     * The tests that are the main purpose for testing
     */
    private void tests() {

        mMainTestsDone = 0;

        // Test RxFirebase getLoyaltyOffer
        RxFirebase.getLoyaltyOffer(mTestsReference, "TestKey").subscribe(new Consumer<LoyaltyOffer>() {
            @Override
            public void accept(@NonNull LoyaltyOffer loyaltyOffer) throws Exception {
                mOfferReturned = loyaltyOffer;

                JUnitCore core = new JUnitCore();
                Result result = core.run(TestLoyatlyOffer.class);
                Log.d(TAG, "tests() getLoyaltyOffer successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mMainTestsDone++;
                    if (mMainTestsDone == (NUMBER_OF_TESTS - NUMBER_OF_SETUP_TESTS - NUMBER_OF_TEARDOWN_TESTS)) postTests();
                } else {
                    testFailed();
                }
            }
        });

        // Test RxFirebase getVendor
        RxFirebase.getVendor(mTestsReference, "TestKey").subscribe(new Consumer<Vendor>() {
            @Override
            public void accept(@NonNull Vendor vendor) throws Exception {
                mVendorReturned = vendor;

                JUnitCore core = new JUnitCore();
                Result result = core.run(TestVendor.class);
                Log.d(TAG, "tests() getVendor successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mMainTestsDone++;
                    if (mMainTestsDone == (NUMBER_OF_TESTS - NUMBER_OF_SETUP_TESTS - NUMBER_OF_TEARDOWN_TESTS)) postTests();
                } else {
                    testFailed();
                }
            }
        });

        // Test RxFirebase getPromotion
        RxFirebase.getPromotion(mTestsReference, "TestKey").subscribe(new Consumer<Promotion>() {
            @Override
            public void accept(@NonNull Promotion promotion) throws Exception {
                mPromotionReturned = promotion;

                JUnitCore core = new JUnitCore();
                Result result = core.run(TestPromotion.class);
                Log.d(TAG, "tests() getPromotion successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mMainTestsDone++;
                    if (mMainTestsDone == (NUMBER_OF_TESTS - NUMBER_OF_SETUP_TESTS - NUMBER_OF_TEARDOWN_TESTS)) postTests();
                } else {
                    testFailed();
                }
            }
        });
    }

    /**
     * Tests to be conducted before the main set of tests. These create necessary preconditions for tests.
     * If these tests fail the others most likely cannot pass
     */
    private void preTests() {

        mSetupTestsDone = 0;

        // Tests whether a loyalty offer is created
        createTestOffer().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestA_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestA.class);
                Log.d(TAG, "preTests() partA successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mSetupTestsDone++;
                    if (mSetupTestsDone == NUMBER_OF_SETUP_TESTS) tests();
                } else {
                    testFailed();
                }
            }
        });

        // Tests whether a vendor is created
        createTestVendor().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestB_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestB.class);
                Log.d(TAG, "preTests() partB successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mSetupTestsDone++;
                    if (mSetupTestsDone == NUMBER_OF_SETUP_TESTS) tests();
                } else {
                    testFailed();
                }
            }
        });
        // Tests that a file was uploaded to storage
        uploadTestStorage().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestC_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestC.class);
                Log.d(TAG, "preTests() partC successful: " + result.wasSuccessful());

                if(result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mSetupTestsDone++;
                    if (mSetupTestsDone == NUMBER_OF_SETUP_TESTS) tests();
                }else{
                    testFailed();
                }
            }
        });

        // Tests whether a promotion is created
        createTestPromotion().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestD_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestD.class);
                Log.d(TAG, "preTests() partD successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                    mSetupTestsDone++;
                    if (mSetupTestsDone == NUMBER_OF_SETUP_TESTS) tests();
                } else {
                    testFailed();
                }
            }
        });

}

    /**
     * Tests to be conducted after the main tests
     */
    private void postTests() {

        // tests whether the offer is removed
        removeTestOffer().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestA_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestA.class);
                Log.d(TAG, "postTests() partA successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();
                } else {
                    testFailed();
                }
            }
        });

        // tests whether the vendor is removed
        removeTestVendor().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestB_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestB.class);
                Log.d(TAG, "postTests() partB successful: " + result.wasSuccessful());

                if (result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();
                } else {
                    testFailed();
                }
            }
        });
        // Tests that a file was retrieved from storage
        downloadTestStorage().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestC_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestC.class);
                Log.d(TAG, "postTests() partC successful: " + result.wasSuccessful());

                if(result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();

                }else{
                    testFailed();
                }
            }
        });

        // Tests whether the promotion is removed
        removeTestPromotion().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                mTestD_Passed = aBoolean;
                JUnitCore core = new JUnitCore();
                Result result = core.run(TestD.class);
                Log.d(TAG, "postTests() partD successful: " + result.wasSuccessful());

                if(result.wasSuccessful()) {
                    mTestsSuccessful++;
                    updateView();
                } else {
                    testFailed();
                }
            }
        });
    }

    // THIS SECTION IS TO DEFINE OBSERVABLES USED IN THE SETUP AND TEARDOWN SECTIONS

    // Creates a vendor for testing
    private static Observable<Boolean> createTestVendor() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                Vendor vendor = new Vendor("Test Business","test");
                DatabaseReference ref = mTestsReference.child("Vendors");
                ref.child("TestKey").setValue(vendor, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // fails
                            e.onNext(Boolean.FALSE);
                            e.onComplete();
                        } else {
                            e.onNext(Boolean.TRUE);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

    // Deletes test vendor
    private static Observable<Boolean> removeTestVendor() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                DatabaseReference ref = mTestsReference.child("Vendors");
                ref.child("TestKey").removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            e.onNext(Boolean.FALSE);
                            e.onComplete();
                        } else {
                            e.onNext(Boolean.TRUE);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }


    // Creates a loyalty offer for testing with
    private static Observable<Boolean> createTestOffer() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                LoyaltyOffer newOffer = new LoyaltyOffer("Test", "Test", "Test", "Test");
                DatabaseReference ref = mTestsReference.child("LoyaltyOffers");
                ref.child("TestKey").setValue(newOffer, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // fails if database error
                            e.onNext(Boolean.FALSE);
                            e.onComplete();
                        } else {
                            e.onNext(Boolean.TRUE);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

    // Removes test loyalty offer
    private static Observable<Boolean> removeTestOffer() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                DatabaseReference ref = mTestsReference.child("LoyaltyOffers");
                ref.child("TestKey").removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // fails if database error
                            e.onNext(Boolean.FALSE);
                            e.onComplete();
                        } else {
                            e.onNext(Boolean.TRUE);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

    // Creates a temporary file that is uploaded to storage
    private static Observable<Boolean> uploadTestStorage(){
        return Observable.create(new ObservableOnSubscribe<Boolean> () {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {

                File uploadFile = File.createTempFile("Test", ".tmp");
                mUploadContent = uploadFile;
                Log.d(TAG, "uploadFile content: " + mUploadContent);

                e.onNext(Boolean.TRUE);
                e.onComplete();

                UploadTask uploadTask = mStorageReference.child("Test").putFile(Uri.fromFile(uploadFile));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    }
                });
            }
        });
    }

    // Downloads the uploaded file from storage and deletes it
    private static Observable<Boolean> downloadTestStorage(){

        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {

                mStorageReference.child("Test").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                   @Override
                    public void onSuccess(Uri uri) {
                        try {
                            File downloadFile = File.createTempFile("returnFile", "test");
                            mDownloadContent = downloadFile;
                            Log.d(TAG, "downloadFile content: "+mDownloadContent);
                            mStorageReference.child("Test").delete();

                        } catch (IOException e1) {
                            e1.printStackTrace();

                        }
                    }
                });

                e.onNext(Boolean.TRUE);
                e.onComplete();
            }
        });
    }

    // Creates a promotion for testing with
    private static Observable<Boolean> createTestPromotion() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                Promotion newPromotion = new Promotion("promo", "promo", "promo", "promo", "promo");
                DatabaseReference ref = mTestsReference.child("Promotions");
                ref.child("TestKey").setValue(newPromotion, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // fails if database error
                            e.onNext(Boolean.FALSE);
                            e.onComplete();
                        } else {
                            e.onNext(Boolean.TRUE);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

    // Removes test promotion
    private static Observable<Boolean> removeTestPromotion() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                DatabaseReference ref = mTestsReference.child("Promotions");
                ref.child("TestKey").removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // fails if database error
                            e.onNext(Boolean.FALSE);
                            e.onComplete();
                        } else {
                            e.onNext(Boolean.TRUE);
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

}
