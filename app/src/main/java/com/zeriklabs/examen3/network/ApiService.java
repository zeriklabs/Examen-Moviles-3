package com.zeriklabs.examen3.network;

import com.zeriklabs.examen3.model.ProductResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    // Optimización: "select" indica a la API que SOLO envíe los campos que realmente necesitamos.
    // Esto reduce el tamaño del JSON de respuesta en un ~60%, haciendo la descarga y el parseo mucho más rápido.
    @GET("products?select=id,title,description,category,price,rating,brand,thumbnail")
    Call<ProductResponse> getProducts();
}
