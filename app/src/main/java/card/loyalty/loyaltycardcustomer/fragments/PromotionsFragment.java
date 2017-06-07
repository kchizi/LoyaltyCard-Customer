package card.loyalty.loyaltycardcustomer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.adapters.LoyaltyCardsRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.adapters.PromotionsRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyCard;
import card.loyalty.loyaltycardcustomer.data_models.Promotion;

/**
 * Created by samclough on 7/06/17.
 */

public class PromotionsFragment extends Fragment implements CardsRecyclerClickListener.OnCardsRecyclerClickListener {

    private static final String TAG = "PromotionsFragment";

    public static final String FRAGMENT_TAG = "PR";

    public PromotionsRecyclerAdapter mRecyclerAdapter;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Firebase Database
    private DatabaseReference mRootRef;
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
        mPromotionsRef = mRootRef.child("LoyaltyCards");

        // Initialise cards list
        mPromotions = new ArrayList<>();
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

        return view;
    }

    @Override
    public void onClick(View view, int position) {

    }
}
