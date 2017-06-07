package card.loyalty.loyaltycardcustomer.observables;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import card.loyalty.loyaltycardcustomer.data_models.LoyaltyOffer;
import card.loyalty.loyaltycardcustomer.data_models.Vendor;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.twitter.sdk.android.core.TwitterCore.TAG;

/**
 * Created by Sam on 2/05/2017.
 */

public class RxFirebase {


    /**
     * Gets an Observable that will retrieve and emit a requested LoyaltyOffer from Firebase
     * @param ref Firebase LoyaltyOffers DatabaseReference
     * @param offerID The Key of the LoyaltyOffer to retrieve
     * @return LoyaltyOffer
     */
    public static Observable<LoyaltyOffer> getLoyaltyOffer(final DatabaseReference ref, final String offerID) {
        return Observable.create(new ObservableOnSubscribe<LoyaltyOffer>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<LoyaltyOffer> e) throws Exception {
                DatabaseReference lRef = ref.child("LoyaltyOffers");
                Query query = lRef.orderByKey().equalTo(offerID);
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                                LoyaltyOffer offer = offerSnapshot.getValue(LoyaltyOffer.class);
                                e.onNext(offer);
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

    /**
     * Gets an Observable that will retrieve and emit a requested Vendor from Firebase
     * @param ref Firebase root DatabaseReference
     * @param vendorID The Key of the Vendor to retrieve
     * @return LoyaltyOffer
     */
    public static Observable<Vendor> getVendor(final DatabaseReference ref, final String vendorID) {
        return Observable.create(new ObservableOnSubscribe<Vendor>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<Vendor> e) throws Exception {
                DatabaseReference vRef = ref.child("Vendors");
                Query query = vRef.orderByKey().equalTo(vendorID);
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot vendorSnapshot : dataSnapshot.getChildren()) {
                                Vendor vendor = vendorSnapshot.getValue(Vendor.class);
                                e.onNext(vendor);
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


    // TODO create observable to get promotions (possibly)

}
