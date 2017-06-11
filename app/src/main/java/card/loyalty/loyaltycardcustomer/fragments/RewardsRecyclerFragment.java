package card.loyalty.loyaltycardcustomer.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import card.loyalty.loyaltycardcustomer.CustomerActivity;
import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.adapters.LoyaltyRewardsRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyReward;

/**
 * Created by Caleb T on 12/06/2017.
 */

public class RewardsRecyclerFragment extends Fragment implements RewardsRecyclerClickListener.OnRecyclerClickListener {

    private static final String TAG = "RRF";

    public static final String FRAGMENT_TAG = "RRF";

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123;

    // Firebase Database References
    private DatabaseReference mRootRef;
    private DatabaseReference mLoyaltyRewardsRef;
    private ValueEventListener mValueEventListener;
    private Query mQuery;

    // Firebase User ID
    private String mUid;

    // Progress Spinner
    private ProgressBar spinner;

    // RecyclerView Objects
    protected RecyclerView recyclerView;
    protected LoyaltyRewardsRecyclerAdapter recyclerAdapter;
    protected RecyclerView.LayoutManager layoutManager;

    // List of Offers created
    protected List<LoyaltyReward> mRewards;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets UID
        mFirebaseAuth = FirebaseAuth.getInstance();

        mRewards = new ArrayList<>();

        // Sets Database Reference
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyRewardsRef = mRootRef.child("LoyaltyRewards");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reward_recycler, container, false);
        view.setTag(TAG);

        // Links Recycler View
        recyclerView = (RecyclerView) view.findViewById(R.id.rewards_recycler);
        // Sets Recycler View Layout
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Add the RecyclerClickListener
        recyclerView.addOnItemTouchListener(new RewardsRecyclerClickListener(getContext(), recyclerView, this));

        // Creates recyclerAdapter for content
        recyclerAdapter = new LoyaltyRewardsRecyclerAdapter(mRewards);
        recyclerView.setAdapter(recyclerAdapter);

        spinner = (ProgressBar)view.findViewById(R.id.card_spinner);
        spinner.setVisibility(View.VISIBLE);


        // Firebase AuthState Listener - stops null pointer problems with retrieve UID
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: is signed_in:" + user.getUid());
                    // Attaches the Database listner
                    mUid = mFirebaseAuth.getCurrentUser().getUid();
                    attachDatabaseReadListener();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: is signed_out");
                }
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        detachDatabaseReadListener();
    }

    // Database listener retrieves offers from Firebase and sets data to recycler view recyclerAdapter
    private void attachDatabaseReadListener() {
        mQuery = mLoyaltyRewardsRef.orderByChild("customerID").equalTo(mUid);
        Log.d(TAG, "Current mUid: " + mUid);

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange started");
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");
                        mRewards.clear();
                        for (DataSnapshot rewardSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyReward reward = rewardSnapshot.getValue(LoyaltyReward.class);
                            reward.setRewardID(rewardSnapshot.getKey());
                            mRewards.add(reward);
                            Log.d(TAG, "Current reward description: " + reward.rewardDesc);
                        }
                        recyclerAdapter.setOffers(mRewards);
                        spinner.setVisibility(View.GONE);
                    } else {
                        // Removes
                        Log.d(TAG, "dataSnapshot doesn't exist");
                        if(getFragmentManager().getBackStackEntryCount() > 0 ) {
                            getFragmentManager().popBackStack();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
        } else {
            Log.d(TAG, "mValueEventListener is not null");
        }
        Log.d(TAG, "addValueEventListener added to mQuery");
        mQuery.addValueEventListener(mValueEventListener);
    }

    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mQuery.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    // launch the scanner on click
    @Override
    public void onClick(View view, int position) {
        Toast.makeText(getContext(), "Reward Pressed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClick(View view, int position) {
        Toast.makeText(getContext(), "Reward Long Pressed", Toast.LENGTH_SHORT).show();
    }
}
