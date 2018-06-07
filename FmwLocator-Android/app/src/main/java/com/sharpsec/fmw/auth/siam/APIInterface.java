package com.sharpsec.fmw.auth.siam;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by anupamchugh on 09/01/17.
 */

interface APIInterface {

    @POST("/auth")
    Call<CognitoAuthResponse> loginUser(@Body CognitoPostUser user);

    @GET("/auth/logout/{userName}")
    Call<SiamLogoutResponse> logoutUser(@Header("authorization") String bearerToken, @Path("userName") String userName);
}
