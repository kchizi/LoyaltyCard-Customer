package card.loyalty.loyaltycardcustomer.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyCard;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.Vendor;
import card.loyalty.loyaltycardcustomer.observables.RxFirebase;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Sam on 20/05/2017.
 */

public class CardDetailsFragment extends Fragment {

    private static final String TAG = "CardDetailsFragment";

    // The database key of the card to view
    private String mKey;
    // the card currently being viewed
    private LoyaltyCard mCard;
    // Firebase root database reference
    private DatabaseReference mRootRef;

    private StorageReference mStorage;

    private FirebaseAuth mFirebaseAuth;

    // Gets a new instance and passes it the database key of the card to view
    public static Fragment newInstance(String key) {
        CardDetailsFragment cardDetailsFragment = new CardDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putString("Key", key);
        cardDetailsFragment.setArguments(bundle);

        return cardDetailsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKey = getArguments().getString("Key");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_details, container, false);
        view.setTag(TAG);

        // Get loyaltycards database reference
        DatabaseReference cardsReference = FirebaseDatabase.getInstance().getReference().child("LoyaltyCards");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Create a reference with an initial file path and name

        Query query = cardsReference.orderByKey().equalTo(mKey);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                        mCard = cardSnapshot.getValue(LoyaltyCard.class);
                        mCard.setCardID(cardSnapshot.getKey());
                    }

                    populateDetailView(mCard);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: datbase error occured");
            }
        };
        query.addListenerForSingleValueEvent(listener);

        return view;
    }

    private void populateDetailView(final LoyaltyCard loyaltyCard) {
        Observable<Vendor> ven = RxFirebase.getVendor(mRootRef, loyaltyCard.vendorID);
        Observable<LoyaltyOffer> off = RxFirebase.getLoyaltyOffer(mRootRef, loyaltyCard.offerID);
        Observable<LoyaltyCard> card = Observable.just(loyaltyCard);
        Function3<Vendor, LoyaltyOffer, LoyaltyCard, LoyaltyCard> f = new Function3<Vendor, LoyaltyOffer, LoyaltyCard, LoyaltyCard>() {
            @Override
            public LoyaltyCard apply(@io.reactivex.annotations.NonNull Vendor vendor, @io.reactivex.annotations.NonNull LoyaltyOffer offer, @io.reactivex.annotations.NonNull LoyaltyCard loyaltyCard) throws Exception {
                Log.d(TAG, "applying offer/vendor");

                loyaltyCard.setVendor(vendor);
                loyaltyCard.setOffer(offer);

                return loyaltyCard;
            }
        };

        // zip the two observable streams together and returns a new observable
        Observable<LoyaltyCard> observable = Observable.zip(ven, off, card, f);

        observable.retry(3)
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
                        Log.d(TAG, "doOnComplete filling card details");
                        fillCardDetails(loyaltyCard);
                    }
                })
                .subscribe();
    }


    private void fillCardDetails(LoyaltyCard card) {
        Activity activity = getActivity();

        ProgressBar spinner = (ProgressBar) activity.findViewById(R.id.detail_spinner);


        TextView businessName = (TextView) activity.findViewById(R.id.detail_bizName);
        TextView businessAddress = (TextView) activity.findViewById(R.id.detail_bizAddr);
        TextView offerDescription = (TextView) activity.findViewById(R.id.detail_offerDesc);
        TextView purchasesPerReward = (TextView) activity.findViewById(R.id.detail_ppr);
        TextView reward = (TextView) activity.findViewById(R.id.detail_reward);
        TextView purchaseCount = (TextView) activity.findViewById(R.id.detail_purchCount);
        TextView purchasesToNextReward = (TextView) activity.findViewById(R.id.detail_purchToNext);
        final ImageView imageView = (ImageView) activity.findViewById(R.id.imageView);
        final ImageView imageView2 = (ImageView) activity.findViewById(R.id.product_image);

        spinner.setVisibility(View.VISIBLE);

        mStorage.child("Images/" +card.offerID.toString()+ "/" +card.vendorID.toString()+ "/").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).override(100,100).error(R.drawable.placeholder).into(imageView2);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Glide.with(getApplicationContext()).load(R.drawable.placeholder).override(100,100).into(imageView2);
            }
        });

        businessName.setText(card.retrieveVendor().businessName);
        businessAddress.setText(card.retrieveVendor().businessAddress);
        offerDescription.setText(card.retrieveOffer().description);
        purchasesPerReward.setText("Purchases Per Reward: " + card.retrieveOffer().purchasesPerReward);
        reward.setText("Reward: " + card.retrieveOffer().reward);
        purchaseCount.setText("Purchases Count: " + card.purchaseCount);
        int ppr = Integer.parseInt(card.retrieveOffer().purchasesPerReward);
        int pc = Integer.parseInt(card.purchaseCount);
        int ptnr = ppr - pc % ppr;
        purchasesToNextReward.setText("Purchases to Next Reward: " + ptnr);

        spinner.setVisibility(View.GONE);
    }
}