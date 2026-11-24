package com.zeriklabs.examen3;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.zeriklabs.examen3.adapter.ProductAdapter;
import com.zeriklabs.examen3.model.Product;
import com.zeriklabs.examen3.model.ProductResponse;
import com.zeriklabs.examen3.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ExtendedFloatingActionButton btnConsultar;
    private ProgressBar fabProgress;
    private RecyclerView recyclerViewProducts;
    private View layoutError;
    private TextView textViewErrorMessage;

    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    private boolean isFirstLoad = true;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_main);
        }

        btnConsultar = findViewById(R.id.btnConsultar);
        fabProgress = findViewById(R.id.fabProgress);
        Button btnReintentar = findViewById(R.id.btnReintentar);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        layoutError = findViewById(R.id.layoutError);
        textViewErrorMessage = findViewById(R.id.textViewErrorMessage);

        recyclerViewProducts.setHasFixedSize(true);
        recyclerViewProducts.setItemViewCacheSize(20);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        // Animación dinámica del FAB al hacer scroll en la lista
        recyclerViewProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (isLoading) return; // Si está cargando, no tocar el botón

                if (dy > 0 && btnConsultar.isExtended()) {
                    btnConsultar.shrink(); // Ocultar texto, dejar solo icono
                } else if (dy < 0 && !btnConsultar.isExtended()) {
                    btnConsultar.extend(); // Mostrar texto completo
                }
            }
        });

        productAdapter = new ProductAdapter(this, productList, this);
        recyclerViewProducts.setAdapter(productAdapter);

        btnConsultar.setOnClickListener(v -> cargarProductos());
        btnReintentar.setOnClickListener(v -> cargarProductos());

        cargarProductos();
    }

    private void cargarProductos() {
        mostrarCargando(true);
        ocultarError();

        RetrofitClient.getApiService(getApplicationContext()).getProducts().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                // Añadimos un retraso artificial de 1.2 segundos para apreciar la animación
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    mostrarCargando(false);

                    if (response.isSuccessful() && response.body() != null) {
                        List<Product> nuevosProductos = response.body().getProducts();

                        // Verificar si hay cambios reales en los datos (por cantidad de items en este caso)
                        boolean hayCambios = productList.size() != nuevosProductos.size();

                        productList = nuevosProductos;
                        productAdapter.setProductList(productList);

                        if (isFirstLoad) {
                            if (!productList.isEmpty()) {
                                recyclerViewProducts.post(() -> ejecutarAnimacionEntrada());
                            }
                            isFirstLoad = false;
                        } else {
                            // Si es una recarga manual, mostramos el estado del catálogo
                            if (hayCambios) {
                                Toast.makeText(MainActivity.this, "¡Nuevos productos encontrados!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "El catálogo está al día", Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (productList.isEmpty()) {
                            mostrarError("No se encontraron productos disponibles.");
                        }
                    } else {
                        String errorMsg = "Error en la respuesta del servidor (Código " + response.code() + ")";
                        mostrarError(errorMsg);
                    }
                }, 1200); // 1200 milisegundos = 1.2 segundos
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                // También retrasamos el error para que no sea un pantallazo brusco
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    mostrarCargando(false);
                    mostrarError("No se pudo conectar con el servidor. Verifica tu conexión a Internet.");
                }, 1200);
            }
        });
    }

    private void ejecutarAnimacionEntrada() {
        Context context = recyclerViewProducts.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerViewProducts.setLayoutAnimation(controller);
        productAdapter.notifyDataSetChanged();
        recyclerViewProducts.scheduleLayoutAnimation();
    }

    private void mostrarCargando(boolean cargando) {
        isLoading = cargando;
        if (cargando) {
            btnConsultar.extend(); // Forzar expansión para mostrar mensaje de carga
            btnConsultar.setText(R.string.loading_message);
            // Ocultamos el icono para no superponer, dejándolo transparente para conservar el espacio
            btnConsultar.setIconTint(ColorStateList.valueOf(Color.TRANSPARENT));
            btnConsultar.setEnabled(false);
            fabProgress.setVisibility(View.VISIBLE);

            if (isFirstLoad) {
                recyclerViewProducts.setVisibility(View.INVISIBLE);
            }
        } else {
            btnConsultar.setText(R.string.btn_consult_api);
            // Restauramos el color del icono
            btnConsultar.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorOnPrimary)));
            btnConsultar.setEnabled(true);
            fabProgress.setVisibility(View.GONE);

            recyclerViewProducts.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarError(String mensaje) {
        layoutError.setVisibility(View.VISIBLE);
        textViewErrorMessage.setText(mensaje);
        recyclerViewProducts.setVisibility(View.GONE);
    }

    private void ocultarError() {
        layoutError.setVisibility(View.GONE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_PRODUCT, product);
        startActivity(intent);
    }
}
