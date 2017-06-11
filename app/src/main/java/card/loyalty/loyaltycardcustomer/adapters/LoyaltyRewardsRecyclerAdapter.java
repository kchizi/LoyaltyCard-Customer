package card.loyalty.loyaltycardcustomer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.data_models.LoyaltyReward;

/**
 * Created by Caleb T on 12/06/2017.
 */

public class LoyaltyRewardsRecyclerAdapter extends RecyclerView.Adapter<LoyaltyRewardsRecyclerAdapter.LoyaltyRewardsViewHolder> {

    private static final String TAG = "LRRA";

    private List<LoyaltyReward> mRewards;

    static class LoyaltyRewardsViewHolder extends RecyclerView.ViewHolder {
        TextView businessName = null;
        TextView rewardOffer = null;

        public LoyaltyRewardsViewHolder(View itemView) {
            super(itemView);
            this.businessName = (TextView) itemView.findViewById(R.id.reward_business_name);
            this.rewardOffer = (TextView) itemView.findViewById(R.id.reward_description);
        }
    }

    public LoyaltyRewardsRecyclerAdapter(List<LoyaltyReward> offers) {
        mRewards = offers;
    }

    public void setOffers(List<LoyaltyReward> offers) {
        mRewards = offers;
        notifyDataSetChanged();
    }

    // Creates new views (invoked by the layout manager)
    @Override
    public LoyaltyRewardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loyalty_rewards_list_item, parent, false);

        LoyaltyRewardsViewHolder holder = new LoyaltyRewardsViewHolder(view);

        return holder;
    }

    // Replace the contents of a view (invoked by layout manager)
    @Override
    public void onBindViewHolder(LoyaltyRewardsViewHolder viewHolder, int position) {
        LoyaltyReward reward = mRewards.get(position);
        viewHolder.businessName.setText(reward.vendorID);
        viewHolder.rewardOffer.setText(reward.rewardDesc);
    }

    @Override
    public int getItemCount() {
        return ((mRewards != null) && (mRewards.size() != 0) ? mRewards.size() : 0);
    }

    public LoyaltyReward getRewards(int position) {
        return ((mRewards != null) && (mRewards.size() != 0) ? mRewards.get(position) : null);
    }

}
