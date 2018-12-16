package com.skynet.stream.network.api;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by thaopt on 9/6/17.
 */

public interface ApiService {
    public static String API_ROOT = "https://pricingcompare.herokuapp.com/";
    @GET("getUpdate")
    Call<ApiResponse<String>> getUpdate();
    @GET("downloadfile")
    Call<Boolean> download();
    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl);
}
