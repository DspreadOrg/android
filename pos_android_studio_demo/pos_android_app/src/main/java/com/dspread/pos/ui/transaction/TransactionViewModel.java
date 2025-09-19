package com.dspread.pos.ui.transaction;

import android.app.Application;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.common.http.RetrofitClient;
import com.dspread.pos.common.http.api.RequestOnlineAuthAPI;
import com.dspread.pos.common.http.model.AuthRequest;
import com.dspread.pos.common.http.model.TransactionRequest;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.ui.payment.PaymentModel;
import com.dspread.pos.ui.setting.device_config.DeviceConfigItem;
import com.dspread.pos.utils.JsonUtil;
import com.dspread.pos.utils.TRACE;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class TransactionViewModel extends BaseAppViewModel {

    private static final String AUTHFROMISSUER_URL = "https://ypparbjfugzgwijijfnb.supabase.co/functions/v1/get-transaction-records";


    public MutableLiveData<List<Transaction>> transactionList = new MutableLiveData<>();
    private RequestOnlineAuthAPI apiService;

    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<Boolean> isEmpty = new ObservableField<>(false);
    public ObservableField<Boolean> isTransactionHeader = new ObservableField<>(true);
    public ObservableField<Boolean> isTransactionViewAll = new ObservableField<>(true);

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getInstance().create(RequestOnlineAuthAPI.class);
    }

    public void init() {
        isLoading.set(true);
        requestTransactionRequest("all");
    }


    public void refreshWithFilter(String filter) {
        isLoading.set(true);
        requestTransactionRequest(filter);
    }



    public void requestTransactionRequest(String  filter) {
        TRACE.d("result network requestTransactionRequest");
        TransactionRequest transactionRequest = createAuthRequest(filter);
        addSubscribe(apiService.getTransaction(AUTHFROMISSUER_URL, transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    isLoading.set(false);
                    TRACE.i("result network rsp code=" + response.getResult());
                    String jsonString = JsonUtil.toJsonString(response.getResult());
                    List<Transaction> transactions = JsonParser.parseTransactionList(jsonString);


                    transactionList.setValue(transactions);
                }, throwable -> {
                    isLoading.set(false);
                    ToastUtils.showShort("The network is failed：" + throwable.getMessage());
                }));
    }

    private TransactionRequest createAuthRequest(String  filter) {
        String deviceSn = SPUtils.getInstance().getString("posID", "");
        return new TransactionRequest(deviceSn,filter);
    }

}
