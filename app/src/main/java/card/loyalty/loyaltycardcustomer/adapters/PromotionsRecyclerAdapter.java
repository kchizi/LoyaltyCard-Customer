package card.loyalty.loyaltycardcustomer.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by samclough on 7/06/17.
 */

public class PromotionsRecyclerAdapter extends RecyclerView.Adapter<PromotionsRecyclerAdapter.PromotionsViewHolder> {


    @Override
    public PromotionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(PromotionsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class PromotionsViewHolder extends ViewHolder{
        public PromotionsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
