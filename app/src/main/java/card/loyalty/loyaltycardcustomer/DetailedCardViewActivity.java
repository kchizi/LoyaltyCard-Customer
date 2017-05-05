package card.loyalty.loyaltycardcustomer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

public class DetailedCardViewActivity extends AppCompatActivity {
    private static final String TAG = "DetailedCardViewActivit";

    private String mKey;
    private LoyaltyCard mCard;

    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_card_view);

        mKey = getIntent().getStringExtra(MyCardsActivity.EXTRA_CARD_ID);

        // Get loyaltycards database reference
        DatabaseReference cardsReference = FirebaseDatabase.getInstance().getReference().child("LoyaltyCards");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        Query query = cardsReference.orderByKey().equalTo(mKey);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot cardSnapshot: dataSnapshot.getChildren()) {
                        mCard = cardSnapshot.getValue(LoyaltyCard.class);
                        mCard.setCardID(cardSnapshot.getKey());
                    }

                    Observable.just(mCard)
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
                                            loyaltyCard.setVendor(vendor);
                                            loyaltyCard.setOffer(offer);
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
                                    fillCardDetails();
                                }
                            })
                            .subscribe();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: datbase error occured");
            }
        };
        query.addListenerForSingleValueEvent(listener);
    }

    private void fillCardDetails() {
        TextView businessName = (TextView) findViewById(R.id.detail_bizName);
        TextView businessAddress = (TextView) findViewById(R.id.detail_bizAddr);
        TextView offerDescription = (TextView) findViewById(R.id.detail_offerDesc);
        TextView purchasesPerReward = (TextView) findViewById(R.id.detail_ppr);
        TextView reward = (TextView) findViewById(R.id.detail_reward);
        TextView purchaseCount = (TextView) findViewById(R.id.detail_purchCount);
        TextView purchasesToNextReward = (TextView) findViewById(R.id.detail_purchToNext);
        TextView rewardsIssued = (TextView) findViewById(R.id.detail_rewardsIssued);
        TextView rewardsClaimed = (TextView) findViewById(R.id.detail_rewardsClaimed);

        businessName.setText(mCard.retrieveVendor().businessName);
        businessAddress.setText("TODO");
        offerDescription.setText(mCard.retrieveOffer().description);
        purchasesPerReward.setText("Purchases Per Reward: " + mCard.retrieveOffer().purchasesPerReward);
        reward.setText("Reward: " + mCard.retrieveOffer().reward);
        purchaseCount.setText("Purchase Count: " + mCard.purchaseCount);
        int ppr = Integer.parseInt(mCard.retrieveOffer().purchasesPerReward);
        int pc = Integer.parseInt(mCard.purchaseCount);
        int ptnr = ppr - pc % ppr;
        purchasesToNextReward.setText("Purchases to Next Reward: " + ptnr);
        rewardsIssued.setText("Rewards Issued: " + (pc / ppr));
        rewardsClaimed.setText("TODO");

    }

}
