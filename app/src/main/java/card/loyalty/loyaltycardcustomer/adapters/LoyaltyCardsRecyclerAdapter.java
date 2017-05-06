package card.loyalty.loyaltycardcustomer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyCard;

/**
 * RecyclerView Adapter for the LoyaltyCards Recycler on the starting activity. This follows the standard
 * structure for a recycler adapter
 * Created by Sam on 26/04/2017.
 */

public class LoyaltyCardsRecyclerAdapter extends RecyclerView.Adapter<LoyaltyCardsRecyclerAdapter.LoyaltyCardViewHolder> {
    private static final String TAG = "LoyaltyCardsRecyclerAda";

    private List<LoyaltyCard> mCards;

    public LoyaltyCardsRecyclerAdapter(List<LoyaltyCard> cards) {
        mCards = cards;
    }

    public void setCards(List<LoyaltyCard> cards) {
        mCards = cards;
        notifyDataSetChanged();
    }

    @Override
    public LoyaltyCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loyalty_card_list_item, parent, false);
        LoyaltyCardViewHolder holder = new LoyaltyCardViewHolder(view);
        return  holder;
    }

    @Override
    public void onBindViewHolder(LoyaltyCardViewHolder holder, int position) {
        LoyaltyCard card = mCards.get(position);
        holder.businessName.setText(card.retrieveBusinessName());
        holder.offerDescription.setText(card.retrieveOfferDescription());
        holder.purchaseCount.setText("Purchases: " + card.purchaseCount);
    }

    @Override
    public int getItemCount() {
        return ((mCards != null) && (mCards.size() != 0)) ? mCards.size() : 0;
    }

    public LoyaltyCard getCard(int position) {
        return ((mCards != null) && (mCards.size() != 0)) ? mCards.get(position) : null;
    }

    static class LoyaltyCardViewHolder extends RecyclerView.ViewHolder {
        TextView businessName = null;
        TextView offerDescription = null;
        TextView purchaseCount = null;

        public LoyaltyCardViewHolder(View itemView) {
            super(itemView);
            this.businessName = (TextView) itemView.findViewById(R.id.text_businessName);
            this.offerDescription = (TextView) itemView.findViewById(R.id.text_offerDescription);
            this.purchaseCount = (TextView) itemView.findViewById(R.id.text_purchaseCount);
        }
    }
}
