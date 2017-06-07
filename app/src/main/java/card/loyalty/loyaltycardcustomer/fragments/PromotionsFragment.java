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

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.adapters.PromotionsRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyCard;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.Promotion;
import card.loyalty.loyaltycardcustomer.data_models.Vendor;
import card.loyalty.loyaltycardcustomer.data_models.Voucher;
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
 * Created by samclough on 7/06/17.
 */

public class PromotionsFragment extends Fragment implements CardsRecyclerClickListener.OnCardsRecyclerClickListener {

    private static final String TAG = "PromotionsFragment";
    private static final int RC_SIGN_IN = 123;

    public static final String FRAGMENT_TAG = "PR";

    public PromotionsRecyclerAdapter mRecyclerAdapter;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Firebase Database
    private DatabaseReference mRootRef;
    private DatabaseReference mVouchersRef;
    private DatabaseReference mPromotionsRef;
    private ValueEventListener mValueEventListener;
    private Query mQuery;

    private List<Promotion> mPromotions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase initialisations
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mPromotionsRef = mRootRef.child("Promotions");

        // Vouchers is essentially a list of all customers that have been pushed promotions
        // nested within are references to all of the promotions they currently have access to
        mVouchersRef = mRootRef.child("Vouchers");

        // Initialise cards list
        mPromotions = new ArrayList<>();
        mPromotions.add(new Promotion("1", "2", "3", "4", "5"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions_recycler, container, false);
        view.setTag(TAG);

        // Find recycler and set layout manager to linear layout manager
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_promotions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add click listener
        recyclerView.addOnItemTouchListener(new CardsRecyclerClickListener(getContext(), recyclerView, this));

        // create recycler adapter and set as adapter for cards recycler
        mRecyclerAdapter = new PromotionsRecyclerAdapter(mPromotions);
        recyclerView.setAdapter(mRecyclerAdapter);

        // Firebase Auth confirmation
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitailise(user);
                } else {
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    //.setTheme(R.style.AuthTheme) //set bizName theme for Firebase UI here
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        return view;
    }

    private void onSignedInInitailise(FirebaseUser user) {
        attachDatabaseListener();
    }

    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }

    // Attaches the database listener to trigger callback when Firebase data changes
    private void attachDatabaseListener() {
        mQuery = mVouchersRef.orderByChild("customerID").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");

                        // Create new array list so the cards displayed don't change until all data available
                        final ArrayList<Voucher> vouchers = new ArrayList<>();
                        //mCards.clear();

                        for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                            Voucher voucher = cardSnapshot.getValue(Voucher.class);
                            Log.d(TAG, "onDataChange: voucher promoID: "+voucher.promoID);
                            vouchers.add(voucher);
                        }

//                        for (Voucher voucher: vouchers) {
//                            mPromotions.add(new Promotion("1", "2", "3", "4", "5"));
//                            mRecyclerAdapter.setPromotions(mPromotions);
//                        }

                        populateRecycler(vouchers);
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

    // Detaches the database listener
    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mQuery.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    private void populateRecycler(final ArrayList<Voucher> vouchers) {
        final ArrayList<Promotion> promotions = new ArrayList<>();
        Observable.fromIterable(vouchers) //TODO check valid
                .observeOn(Schedulers.io())
                .concatMap(new Function<Voucher, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@io.reactivex.annotations.NonNull Voucher voucher) throws Exception {
                        // create two new streams to get the vendor and offer
                        Observable<Vendor> ven = RxFirebase.getVendor(mRootRef, voucher.vendorID);
                        Observable<Promotion> promo = RxFirebase.getPromotion(mRootRef, voucher.promoID);
                        Observable<Promotion> promoB = RxFirebase.getPromotion(mRootRef, voucher.promoID);

                        // No Function2 available?!!! Using Function3 with duplicate promo
                        Function3<Vendor, Promotion, Promotion, Promotion> f = new Function3<Vendor, Promotion, Promotion, Promotion>() {
                            @Override
                            public Promotion apply(@io.reactivex.annotations.NonNull Vendor vendor, @io.reactivex.annotations.NonNull Promotion promo, @io.reactivex.annotations.NonNull Promotion promoB) throws Exception {
                                promo.setVendor(vendor);
                                return promo;
                            }
                        };
                        // zip the two observable streams together and returns a new observable
                        Observable<Promotion> observable = Observable.zip(ven, promo, promoB, f);
                        return observable;
                    }
                }).retry(3)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        Log.d(TAG, "PromotionsFragment: Observable from iterable: " + throwable.getMessage());
                    }
                }).onErrorReturnItem(new Promotion("_","_","_","_","_"))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Object promotion) throws Exception {
                        promotions.add((Promotion) promotion);
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        // set the cards to the recycler adapter
                        mPromotions = promotions;  // TODO check valid
                        mRecyclerAdapter.setPromotions(mPromotions);
                    }
                })
                .subscribe();
    }


    @Override
    public void onClick(View view, int position) {

    }

    @Override
    public void onResume() {
        super.onResume();

        // Add the firebase auth state listener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove the firebase auth state listener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
}