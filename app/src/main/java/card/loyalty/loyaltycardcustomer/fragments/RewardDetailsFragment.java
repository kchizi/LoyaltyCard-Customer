package card.loyalty.loyaltycardcustomer.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyReward;
import card.loyalty.loyaltycardcustomer.data_models.Vendor;
import card.loyalty.loyaltycardcustomer.observables.RxFirebase;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;

/**
 * Created by Caleb T on 12/06/2017.
 */

public class RewardDetailsFragment extends Fragment {

    private static final String TAG ="RDF";

    // Database key for reward to view
    private String mKey;
    // Current card
    private LoyaltyReward reward;
    // Firebase root ref
    private DatabaseReference mRootRef;

    private StorageReference mStorage;

    private FirebaseAuth mFirebaseAtuh;

    public static Fragment newInstance(String key) {
        RewardDetailsFragment rewardDetailsFragment = new RewardDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putString("Key", key);
        rewardDetailsFragment.setArguments(bundle);

        return rewardDetailsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKey = getArguments().getString("Key");
        mFirebaseAtuh = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reward_details, container, false);
        view.setTag(TAG);

        DatabaseReference rewardsReference = FirebaseDatabase.getInstance().getReference().child("LoyaltyRewards");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        Query query = rewardsReference.orderByKey().equalTo(mKey);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot rewardSnapshot : dataSnapshot.getChildren()) {
                        reward = rewardSnapshot.getValue(LoyaltyReward.class);
                        reward.setRewardID(rewardSnapshot.getKey());
                    }

                    populateDetailView(reward);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO canncel method
            }
        };
        query.addListenerForSingleValueEvent(listener);

        return view;
    }

    public void populateDetailView(final LoyaltyReward loyaltyReward) {
        Observable<Vendor> ven = RxFirebase.getVendor(mRootRef, loyaltyReward.vendorID);
        Observable<LoyaltyOffer> off = RxFirebase.getLoyaltyOffer(mRootRef, loyaltyReward.offerID);
        Observable<LoyaltyReward> reward = Observable.just(loyaltyReward);

        Function3<Vendor, LoyaltyOffer, LoyaltyReward, LoyaltyReward> f = new Function3<Vendor, LoyaltyOffer, LoyaltyReward, LoyaltyReward>() {
            @Override
            public LoyaltyReward apply(@NonNull Vendor vendor, @NonNull LoyaltyOffer loyaltyOffer, @NonNull LoyaltyReward loyaltyReward) throws Exception {
                loyaltyReward.setVendor(vendor);

                return loyaltyReward;
            }
        };

        Observable<LoyaltyReward> observable = Observable.zip(ven, off, reward, f);

        observable.retry(3)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.d(TAG, "Rewards Observable error");
                    }
                }).onErrorReturnItem(new LoyaltyReward())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "doOnComplete filling card details");
                        fillRewardDetails(loyaltyReward);
                    }
                })
                .subscribe();
    }


    private void fillRewardDetails(LoyaltyReward reward) {
        Activity activity = getActivity();

        ProgressBar spinner = (ProgressBar) activity.findViewById(R.id.dr_spinner);

        ImageView rewardQR = (ImageView) activity.findViewById(R.id.reward_qr);
        TextView rewardDesc = (TextView) activity.findViewById(R.id.detail_reward_desc);
        TextView businessName = (TextView) activity.findViewById(R.id.detail_reward_bn);
        TextView businessAdd = (TextView) activity.findViewById(R.id.detail_reward_ba);

        // QR Generation
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(reward.rewardID, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            rewardQR.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Details fill
        rewardDesc.setText(reward.rewardDesc);
        businessName.setText(reward.retrieveVendor().businessName);
        businessAdd.setText(reward.retrieveVendor().businessAddress);

        spinner.setVisibility(View.GONE);
    }
}
