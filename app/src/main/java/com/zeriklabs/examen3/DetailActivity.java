package com.zeriklabs.examen3;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zeriklabs.examen3.model.Product;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "PRODUCT_EXTRA";

    private ImageView imageViewDetailProduct;
    private TextView textViewDetailTitle;
    private TextView textViewDetailCategory;
    private TextView textViewDetailPrice;
    private TextView textViewDetailBrand;
    private TextView textViewDetailRating;
    private TextView textViewDetailDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_detail);

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_detail);
        }

        imageViewDetailProduct = findViewById(R.id.imageViewDetailProduct);
        textViewDetailTitle = findViewById(R.id.textViewDetailTitle);
        textViewDetailCategory = findViewById(R.id.textViewDetailCategory);
        textViewDetailPrice = findViewById(R.id.textViewDetailPrice);
        textViewDetailBrand = findViewById(R.id.textViewDetailBrand);
        textViewDetailRating = findViewById(R.id.textViewDetailRating);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);

        Product product = getProductFromIntent();

        if (product != null) {
            populateProductDetails(product);
        } else {
            Toast.makeText(this, "Error al cargar la información del producto", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private Product getProductFromIntent() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_PRODUCT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return getIntent().getSerializableExtra(EXTRA_PRODUCT, Product.class);
            } else {
                return (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
            }
        }
        return null;
    }

    private void populateProductDetails(Product product) {
        textViewDetailTitle.setText(product.getTitle());

        String category = product.getCategory() != null ? product.getCategory() : "N/A";
        textViewDetailCategory.setText(getString(R.string.category_format, category));

        textViewDetailPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

        String brandText = product.getBrand() != null && !product.getBrand().isEmpty()
                ? product.getBrand()
                : getString(R.string.no_brand);
        textViewDetailBrand.setText(getString(R.string.brand_format, brandText));

        textViewDetailRating.setText(String.format(Locale.getDefault(), "★ %.2f / 5.0", product.getRating()));
        textViewDetailDescription.setText(product.getDescription());

        Glide.with(this)
                .load(product.getThumbnail())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(imageViewDetailProduct);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
