package com.example.android_lab.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApiService {
    @POST("webhook/api/vietqr/payment")
    Call<PaymentResponse> createPayment(@Body PaymentRequest request);
}
