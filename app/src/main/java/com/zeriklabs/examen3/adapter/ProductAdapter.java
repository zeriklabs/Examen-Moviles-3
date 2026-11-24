package com.zeriklabs.examen3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.zeriklabs.examen3.R;
import com.zeriklabs.examen3.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private List<Product> productList;
    private final OnProductClickListener clickListener;

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener clickListener) {
        this.context = context;
        this.productList = productList != null ? new ArrayList<>(productList) : new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void setProductList(List<Product> newProductList) {
        final List<Product> oldList = new ArrayList<>(this.productList);
        final List<Product> newList = newProductList != null ? newProductList : new ArrayList<>();

        // Optimización: Uso de DiffUtil en lugar de notifyDataSetChanged()
        // Calcula únicamente los elementos que cambiaron (inserciones, eliminaciones, actualizaciones)
        // evitando que toda la lista parpadee o se renderice desde cero.
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(oldList.get(oldItemPosition).getTitle(), newList.get(newItemPosition).getTitle());
            }
        });

        this.productList.clear();
        this.productList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, clickListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewProduct;
        private final TextView textViewTitle;
        private final TextView textViewCategory;
        private final TextView textViewRating;
        private final TextView textViewDescription;
        private final TextView textViewPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }

        public void bind(Product product, OnProductClickListener listener) {
            textViewTitle.setText(product.getTitle());
            textViewCategory.setText(product.getCategory());
            if (textViewRating != null) {
                textViewRating.setText(String.format(Locale.getDefault(), "★ %.2f", product.getRating()));
            }
            textViewDescription.setText(product.getDescription());
            textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

            Glide.with(itemView.getContext())
                    .load(product.getThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(200, 200)
                    .format(DecodeFormat.PREFER_RGB_565) // Optimización: Reduce uso de RAM por imagen en un 50%
                    .dontAnimate()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(imageViewProduct);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
