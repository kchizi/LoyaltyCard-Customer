package card.loyalty.loyaltycardcustomer.fragments;

import android.content.Intent;
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

/**
 * Created by Sam on 20/05/2017.
 */

public class CardsRecyclerFragment extends Fragment implements CardsRecyclerClickListener.OnCardsRecyclerClickListener {

    private static final String TAG = "CardsRecyclerFragment";
    private static final int RC_SIGN_IN = 123;

    public static final String EXTRA_CARD_ID = "CardID";

    public static final String FRAGMENT_TAG = "CR";

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

    // ProgressBar
    private ProgressBar spinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase initialisations
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyCardsRef = mRootRef.child("LoyaltyCards");

        // Initialise cards list
        mCards = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards_recycler, container, false);
        view.setTag(TAG);


        // Find recycler and set layout manager to linear layout manager
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_myCards);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add click listener
        recyclerView.addOnItemTouchListener(new CardsRecyclerClickListener(getContext(), recyclerView, this));

        // create recycler adapter and set as adapter for cards recycler
        mRecyclerAdapter = new LoyaltyCardsRecyclerAdapter(mCards);
        recyclerView.setAdapter(mRecyclerAdapter);

        spinner = (ProgressBar)view.findViewById(R.id.card_spinner);
        spinner.setVisibility(View.VISIBLE);



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
        Log.d(TAG, "onSignedInIitailise: MyCardsActivity, UID = " + user.getUid());
    }

    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }

    // Attaches the database listener to trigger callback when Firebase data changes
    private void attachDatabaseListener() {
        mQuery = mLoyaltyCardsRef.orderByChild("customerID").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");

                        // Create new array list so the cards displayed don't change until all data available
                        final ArrayList<LoyaltyCard> cards = new ArrayList<>();
                        //mCards.clear();

                        for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyCard card = cardSnapshot.getValue(LoyaltyCard.class);
                            card.setCardID(cardSnapshot.getKey());
                            cards.add(card); //TODO check valid
                        }


                        populateRecycler(cards);
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

    private void populateRecycler(final ArrayList<LoyaltyCard> cards) {
        // Create an observable stream from the cards retrieved
        Observable.fromIterable(cards) //TODO check valid
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
//                /* To help with understanding functionality, uncomment this code. This is how you would trigger behaviour
//                   when each card was available. Here it just changes the business name to "Debug Test" but could also
//                   be used to populate recycler gradually as cards were available */
//                .doOnNext(new Consumer<Object>() {
//                    @Override
//                    public void accept(@io.reactivex.annotations.NonNull Object loyaltyCard) throws Exception {
//                        ((LoyaltyCard) loyaltyCard).setBusinessName("Debug Test");
//                    }
//                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        // set the cards to the recycler adapter
                        mCards = cards;  // TODO check valid
                        mRecyclerAdapter.setCards(mCards);
                        spinner.setVisibility(View.GONE);
                    }
                })
                .subscribe();
                spinner.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view, int position) {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .replace(R.id.content, CardDetailsFragment.newInstance(mCards.get(position).retrieveCardID()))
                .addToBackStack(null)
                .commit();
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
