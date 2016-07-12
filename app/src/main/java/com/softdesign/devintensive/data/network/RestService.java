package com.softdesign.devintensive.data.network;

import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * REST сервис (интерфейс для описания запросов к REST API)
 */
public interface RestService {

    @POST("login")
    Call<UserModelRes> loginUser(@Body UserLoginReq req);

    @Multipart
    @POST("profile/edit")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    @GET
    Call<ResponseBody> getImage(@Url String url);

}
