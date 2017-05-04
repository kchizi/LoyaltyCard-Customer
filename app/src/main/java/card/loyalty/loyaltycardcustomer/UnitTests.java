package card.loyalty.loyaltycardcustomer;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <bizName href="http://d.android.com/tools/testing">Testing documentation</bizName>
 */
public class UnitTests {
    private static final String TAG = "UnitTests";

    private final String USER = "GUWnFftjV7dtAKgf34jGebQRSLr2";

    private static FirebaseAuth mAuth;

    @BeforeClass

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void temp() throws Exception {
        mAuth = FirebaseAuth.getInstance();
        Assert.assertEquals(USER, mAuth.getCurrentUser().getUid());
    }

    @Test
    public void test


//    private Observable<Boolean> setupObservable() {
//        Log.d(TAG, "setupObservable: start");
//        return Observable.create(new ObservableOnSubscribe<Boolean>() {
//            @Override
//            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<Boolean> e) throws Exception {
//                Log.d(TAG, "subscribe: start");
//                mAuth.createUserWithEmailAndPassword("unit@test", "unit")
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                Log.d(TAG, "onComplete: start");
//                                if (task.isSuccessful()) {
//                                    Log.d(TAG, "createUserWithEmail:success");
//                                    e.onNext(Boolean.TRUE);
//                                    e.onComplete();
//                                } else {
//                                    e.onNext(Boolean.FALSE);
//                                    e.onComplete();
//                                }
//
//                            }
//                        });
//            }
//        });
//
//    }
//
//    private Observable teardownObservable() {
//        return Observable.create(new ObservableOnSubscribe<Boolean>() {
//            @Override
//            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<Boolean> e) throws Exception {
//                mAuth.getCurrentUser().delete()
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    Log.d(TAG, "onComplete: user deleted");
//                                    e.onNext(Boolean.TRUE);
//                                    e.onComplete();
//                                } else {
//                                    e.onNext(Boolean.FALSE);
//                                    e.onComplete();
//                                }
//                            }
//                        });
//            }
//        });
//    }
}