package com.naufal.younifirst.api;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;


public interface ApiService {
    @GET("api/postingan")
    Call<Object> getPostingan(@Query("user_id") String userId);
}