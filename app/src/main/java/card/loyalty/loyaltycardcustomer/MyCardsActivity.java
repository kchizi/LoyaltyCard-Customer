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
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private DatabaseReference mLoyaltyOffersRef;
    private DatabaseReference mLoyaltyCardsRef;
    private DatabaseReference mVendorsRef;
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
        mLoyaltyOffersRef = mRootRef.child("LoyaltyOffers");
        mLoyaltyCardsRef = mRootRef.child("LoyaltyCards");
        mVendorsRef = mRootRef.child("Vendors");

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

                        Observable.fromIterable(mCards)
                                .observeOn(Schedulers.io())
                                .concatMap(loyaltyCard -> {
                                    Observable<String> biz = getBizName(loyaltyCard.vendorID);
                                    Observable<String> off = getOfferDesc(loyaltyCard.offerID);
                                    Observable<LoyaltyCard> card = Observable.just(loyaltyCard);
                                    Function3 function31 = new Function3<String, String, LoyaltyCard, LoyaltyCard>() {
                                        @Override
                                        public LoyaltyCard apply(@io.reactivex.annotations.NonNull String bizName, @io.reactivex.annotations.NonNull String offDesc, @io.reactivex.annotations.NonNull LoyaltyCard loyaltyCard) throws Exception {
                                            loyaltyCard.setBusinessName(bizName);
                                            loyaltyCard.setOfferDescription(offDesc);
                                            return loyaltyCard;
                                        }
                                    };
                                    Observable<LoyaltyCard> observable = Observable.zip(biz, off, card, function31);
                                    return observable;
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete(() -> {
                                    mRecyclerAdapter.setCards(mCards);
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

    private Observable<String> getBizName(String vendorID) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull ObservableEmitter<String> e) throws Exception {
                Query query = mVendorsRef.orderByKey().equalTo(vendorID);
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot vendorSnapshot : dataSnapshot.getChildren()) {
                                Vendor vendor = vendorSnapshot.getValue(Vendor.class);
                                e.onNext(vendor.businessName);
                                e.onComplete();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                };
                query.addListenerForSingleValueEvent(listener);
            }
        });
    }


    private Observable<String> getOfferDesc(String offerID) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull ObservableEmitter<String> e) throws Exception {
                Query query = mLoyaltyOffersRef.orderByKey().equalTo(offerID);
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                                LoyaltyOffer offer = offerSnapshot.getValue(LoyaltyOffer.class);
                                String bn = (offer.description != null) ? offer.description : "Vendor";
                                e.onNext(bn);
                                e.onComplete();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                };
                query.addListenerForSingleValueEvent(listener);
            }
        });
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
        Log.d(TAG, "onSignedInItitailise: MyCardsActivity, UID = " + mFirebaseAuth.getCurrentUser().getUid());
    }

    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }
}
