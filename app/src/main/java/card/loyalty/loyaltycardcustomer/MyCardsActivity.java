package card.loyalty.loyaltycardcustomer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import card.loyalty.loyaltycardcustomer.adapters.LoyaltyCardsRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyCard;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.Vendor;
import card.loyalty.loyaltycardcustomer.observables.RxFirebase;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

public class MyCardsActivity extends AppCompatActivity {

    private static final String TAG = "MyCardsActivity";

    // Adapter for recycler view
    private LoyaltyCardsRecyclerAdapter mRecyclerAdapter;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Firebase Database
    private DatabaseReference mRootRef;
    private DatabaseReference mLoyaltyCardsRef;
    private ValueEventListener mValueEventListener;
    private Query mQuery;

    private List<LoyaltyCard> mCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cards);

        // Firebase initialisations
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyCardsRef = mRootRef.child("LoyaltyCards");

        // Initialise cards list
        mCards = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_myCards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerAdapter = new LoyaltyCardsRecyclerAdapter(mCards);
        recyclerView.setAdapter(mRecyclerAdapter);


        // Firebase Auth confirmation
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInItitailise(user);
                } else {
                    onSignedOutCleanup();
                    Intent intent = new Intent(MyCardsActivity.this, CustomerLandingActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mQuery.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    private void attachObservableDatabaseListener() {
        mQuery = mLoyaltyCardsRef.orderByChild("customerID").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");
                        mCards.clear();
                        for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyCard card = cardSnapshot.getValue(LoyaltyCard.class);
                            card.setCardID(cardSnapshot.getKey());
                            mCards.add(card);
                        }

                        // Create an observable stream from the cards retrieved
                        Observable.fromIterable(mCards)
                                .observeOn(Schedulers.io())
                                .concatMap(new Function<LoyaltyCard, ObservableSource<?>>() {
                                    @Override
                                    public ObservableSource<?> apply(@io.reactivex.annotations.NonNull LoyaltyCard loyaltyCard) throws Exception {
                                        // create two new streams to get the vendor and offer
                                        Observable<Vendor> ven = RxFirebase.getVendor(mRootRef, loyaltyCard.vendorID);
                                        Observable<LoyaltyOffer> off = RxFirebase.getLoyaltyOffer(mRootRef, loyaltyCard.offerID);
                                        Observable<LoyaltyCard> card = Observable.just(loyaltyCard);
                                        Function3<Vendor, LoyaltyOffer, LoyaltyCard, LoyaltyCard> f = new Function3<Vendor, LoyaltyOffer, LoyaltyCard, LoyaltyCard>() {
                                            @Override
                                            public LoyaltyCard apply(@io.reactivex.annotations.NonNull Vendor vendor, @io.reactivex.annotations.NonNull LoyaltyOffer offer, @io.reactivex.annotations.NonNull LoyaltyCard loyaltyCard) throws Exception {
                                                loyaltyCard.setBusinessName(vendor.businessName);
                                                loyaltyCard.setOfferDescription(offer.description);
                                                return loyaltyCard;
                                            }
                                        };
                                        // zip the two observable streams together and returns a new observable
                                        Observable<LoyaltyCard> observable = Observable.zip(ven, off, card, f);
                                        return observable;
                                    }
                                }).retry(3)
                                .doOnError(new Consumer<Throwable>() {
                                    @Override
                                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                        Log.d(TAG, "MyCardsActivity: Observable from iterable: " + throwable.getMessage());
                                    }
                                }).onErrorReturnItem(new LoyaltyCard())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        // set the cards to the recycler adapter
                                        mRecyclerAdapter.setCards(mCards);
                                    }
                                })
                                .subscribe();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: database error occurred. Details: " + databaseError.getDetails() + ", Message: " + databaseError.getMessage());
                }
            };
        }
        mQuery.addValueEventListener(mValueEventListener);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Add the firebase auth state listener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove the firebase auth state listener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void onSignedInItitailise(FirebaseUser user) {
        attachObservableDatabaseListener();
        Log.d(TAG, "onSignedInItitailise: MyCardsActivity, UID = " + user.getUid());
    }

    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }
}
