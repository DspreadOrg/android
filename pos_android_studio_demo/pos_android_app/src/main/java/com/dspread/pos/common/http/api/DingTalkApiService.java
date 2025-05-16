package com.dspread.pos.common.http.api;

import java.util.Map;

import io.reactivex.Observable;
import me.goldze.mvvmhabit.http.BaseResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface DingTalkApiService {
    @POST
    Observable<BaseResponse> sendMessage(@Url String url, @Body Map<String, Object> body);
}