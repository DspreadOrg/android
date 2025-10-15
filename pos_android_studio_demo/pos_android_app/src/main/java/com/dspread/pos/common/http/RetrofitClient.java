package com.dspread.pos.common.http;

import com.dspread.pos.utils.TRACE;
import com.google.gson.GsonBuilder;

import me.goldze.mvvmhabit.utils.KLog;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static volatile RetrofitClient instance;
    private final Retrofit retrofit;

    private RetrofitClient() {
        Interceptor requestInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                
                // 打印请求URL
                TRACE.d("Request URL: " + request.url());
                
                // 打印请求头
                Headers headers = request.headers();
                StringBuilder headersBuilder = new StringBuilder("Request Headers:\n");
                for (int i = 0; i < headers.size(); i++) {
                    headersBuilder.append(headers.name(i)).append(": ").append(headers.value(i)).append("\n");
                }
                TRACE.d(headersBuilder.toString());
                
                // 打印请求体
                if (request.body() != null) {
                    // 旧版本 OkHttp 读取请求体的方式
                    Buffer buffer = new Buffer();
                    request.body().writeTo(buffer);
                    String requestBody = buffer.readUtf8(); // 读取请求体内容
                    TRACE.d("Request Body: " + requestBody);
                }
                
                // 执行请求并获取响应
                Response response = chain.proceed(request);
                
                // 打印响应状态码和消息
                TRACE.d("Response Status: " + response.code() + " " + response.message());
                
                // 打印响应头
                Headers responseHeaders = response.headers();
                StringBuilder responseHeadersBuilder = new StringBuilder("Response Headers:\n");
                for (int i = 0; i < responseHeaders.size(); i++) {
                    responseHeadersBuilder.append(responseHeaders.name(i)).append(": ").append(responseHeaders.value(i)).append("\n");
                }
                TRACE.d(responseHeadersBuilder.toString());
                
                // 打印响应体
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    // 复制响应体，避免流被消费后无法使用
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE); // 请求所有数据
                    Buffer buffer = source.buffer();
                    
                    // 克隆缓冲区，以便既能记录日志又不影响原响应
                    Buffer cloneBuffer = buffer.clone();
                    String responseBodyString = cloneBuffer.readUtf8();
                    TRACE.d("Response Body: " + responseBodyString);
                }
                
                return response;
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(requestInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://ypparbjfugzgwijijfnb.supabase.co")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient();
                }
            }
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }
}