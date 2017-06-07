package card.loyalty.loyaltycardcustomer.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import card.loyalty.loyaltycardcustomer.R;
import card.loyalty.loyaltycardcustomer.data_models.Promotion;

/**
 * Created by samclough on 7/06/17.
 */

public class PromotionsRecyclerAdapter extends RecyclerView.Adapter<PromotionsRecyclerAdapter.PromotionsViewHolder> {
    private static final String TAG = "PromotionsRecyclerAdapt";

    private List<Promotion> mPromotions;

    public PromotionsRecyclerAdapter(List<Promotion> promotions) {
        mPromotions = promotions;
    }

    public void setPromotions(List<Promotion> promotions) {
        mPromotions = promotions;
        notifyDataSetChanged();
    }

    @Override
    public PromotionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.promotion_list_item, parent, false);
        PromotionsViewHolder holder = new PromotionsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PromotionsViewHolder holder, int position) {
        Promotion promotion = mPromotions.get(position);
        holder.promoTitle.setText(promotion.title);
        holder.promoDescription.setText(promotion.description);

        if (promotion.retrieveVendor() != null) {
            holder.promoVendorName.setText(promotion.retrieveVendor().businessName);
            holder.promoVendorAddress.setText(promotion.retrieveVendor().businessAddress);
        } else {
            holder.promoVendorName.setText("");
            holder.promoVendorAddress.setText("");
        }
        holder.promoExpiry.setText(promotion.expiryDate);
    }

    @Override
    public int getItemCount() {
        return ((mPromotions != null) && (mPromotions.size() != 0)) ? mPromotions.size() : 0;
    }

    public Promotion getPromotion(int position) {
        return ((mPromotions != null) && (mPromotions.size() != 0)) ? mPromotions.get(position) : null;
    }

    static class PromotionsViewHolder extends ViewHolder{

        TextView promoTitle = null;
        TextView promoDescription = null;
        TextView promoVendorName = null;
        TextView promoVendorAddress = null;
        TextView promoExpiry = null;

        public PromotionsViewHolder(View itemView) {
            super(itemView);
            this.promoTitle = (TextView) itemView.findViewById(R.id.text_promoTitle);
            this.promoDescription = (TextView) itemView.findViewById(R.id.text_promoDesc);
            this.promoVendorName = (TextView) itemView.findViewById(R.id.text_promoVendorName);
            this.promoVendorAddress = (TextView) itemView.findViewById(R.id.text_promoVendorAddress);
            this.promoExpiry = (TextView) itemView.findViewById(R.id.text_promoExpiry);
        }
    }
}
