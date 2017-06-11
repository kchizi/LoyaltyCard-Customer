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
 * Created by Caleb T on 10/06/2017.
 */

public class LoyaltyRewardRecyclerAdapter extends RecyclerView.Adapter<LoyaltyRewardRecyclerAdapter.LoyaltyRewardViewHolder> {

    private static final String TAG = "LoyalRewardRecyclerAdapter";

    private List<LoyaltyReward> mRewards;

    public LoyaltyRewardRecyclerAdapter(List<LoyaltyReward> rewards) {
        mRewards = rewards;
    }

    public void setRewards(List<LoyaltyReward> rewards) {
        mRewards = rewards;
        notifyDataSetChanged();
    }

    @Override
    public LoyaltyRewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loyalty_reward_list_item, parent, false);
        LoyaltyRewardViewHolder holder = new LoyaltyRewardViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(LoyaltyRewardViewHolder holder, int position) {
        LoyaltyReward reward = mRewards.get(position);
        holder.businessName.setText(reward.retrieveBusinessName());
        holder.rewardDesc.setText(reward.retrieveReward());
    }

    @Override
    public int getItemCount() {
        return ((mRewards != null) && (mRewards.size() != 0)) ? mRewards.size() : 0;
    }

    public LoyaltyReward getRewards(int position) {
        return ((mRewards != null) && (mRewards.size() != 0)) ? mRewards.get(position) : null;
    }

    static class LoyaltyRewardViewHolder extends RecyclerView.ViewHolder {
        TextView businessName = null;
        TextView rewardDesc = null;

        public LoyaltyRewardViewHolder(View itemView) {
            super(itemView);
            this.businessName = (TextView) itemView.findViewById(R.id.reward_businessName);
            this.rewardDesc = (TextView) itemView.findViewById(R.id.reward_rewardDesc);
        }
    }
}
