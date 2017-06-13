package card.loyalty.loyaltycardcustomer.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.adapters.LoyaltyRewardRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyReward;
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
    protected LoyaltyRewardRecyclerAdapter recyclerAdapter;
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
        recyclerAdapter = new LoyaltyRewardRecyclerAdapter(mRewards);
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

                        final ArrayList<LoyaltyReward> rewards = new ArrayList<>();

                        for (DataSnapshot rewardSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyReward reward = rewardSnapshot.getValue(LoyaltyReward.class);
                            reward.setRewardID(rewardSnapshot.getKey());
                            rewards.add(0, reward);
                        }
                        populateRewards(rewards);
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

    public void populateRewards(final ArrayList<LoyaltyReward> loyaltyRewards) {
        Observable.fromIterable(loyaltyRewards)
                .observeOn(Schedulers.io())
                .flatMap(new Function<LoyaltyReward, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@io.reactivex.annotations.NonNull LoyaltyReward loyaltyReward) throws Exception {
                        Observable<Vendor> ven = RxFirebase.getVendor(mRootRef, loyaltyReward.vendorID);
                        Observable<LoyaltyOffer> off = RxFirebase.getLoyaltyOffer(mRootRef, loyaltyReward.offerID);
                        Observable<LoyaltyReward> reward = Observable.just(loyaltyReward);

                        Function3<Vendor, LoyaltyOffer, LoyaltyReward, LoyaltyReward> f = new Function3<Vendor, LoyaltyOffer, LoyaltyReward, LoyaltyReward>() {
                            @Override
                            public LoyaltyReward apply(@io.reactivex.annotations.NonNull Vendor vendor, @io.reactivex.annotations.NonNull LoyaltyOffer loyaltyOffer, @io.reactivex.annotations.NonNull LoyaltyReward loyaltyReward) throws Exception {
                                loyaltyReward.setBusinessName(vendor.businessName);
                                return loyaltyReward;
                            }
                        };
                        Observable<LoyaltyReward> observable = Observable.zip(ven, off, reward, f);
                        return observable;
                    }
                }).retry(3)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        Log.d(TAG, "Rewards Observable error");
                    }
                }).onErrorReturnItem(new LoyaltyReward())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        mRewards = loyaltyRewards;
                        recyclerAdapter.setRewards(mRewards);
                    }
                })
                .subscribe();
        spinner.setVisibility(View.GONE);
    }

    // launch the scanner on click
    @Override
    public void onClick(View view, int position) {
//        Toast.makeText(getContext(), "Reward Pressed", Toast.LENGTH_SHORT).show();
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .replace(R.id.content, RewardDetailsFragment.newInstance(mRewards.get(position).getRewardID()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLongClick(View view, int position) {
//        Toast.makeText(getContext(), "Reward Long Pressed", Toast.LENGTH_SHORT).show();
    }
}
