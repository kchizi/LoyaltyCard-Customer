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

    // Database listener for recylcer view
    private void attachDatabaseReadListener() {
        mQuery = mLoyaltyCardsRef.orderByChild("customerID").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");
                        mCards.clear();
                        for (DataSnapshot cardSnapshot: dataSnapshot.getChildren()) {
                            LoyaltyCard card = cardSnapshot.getValue(LoyaltyCard.class);
                            card.setCardID(cardSnapshot.getKey());
                            mCards.add(card);
                        }
                        mRecyclerAdapter.setCards(mCards);
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
    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mQuery.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
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
        attachDatabaseReadListener();
        Log.d(TAG, "onSignedInItitailise: MyCardsActivity, UID = " + mFirebaseAuth.getCurrentUser().getUid());
    }

    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }
}
