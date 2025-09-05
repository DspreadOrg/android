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
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class TransactionViewModel extends BaseAppViewModel {

    private static final String AUTHFROMISSUER_URL = "https://ypparbjfugzgwijijfnb.supabase.co/functions/v1/get-transaction-records";


    public MutableLiveData<List<Transaction>> transactionList = new MutableLiveData<>();
    private RequestOnlineAuthAPI apiService;
    private List<Transaction> paymentList;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getInstance().create(RequestOnlineAuthAPI.class);
    }

    public void init() {
        requestTransactionRequest("all");
        paymentList = new ArrayList<>();
       /* paymentList.add(new Transaction("12/8/24", "$500.00", "4712******8415-16:07", "Paid"));
        paymentList.add(new Transaction("15/8/24", "-$43.00", "4712******8415-14:10", "Voided"));
        paymentList.add(new Transaction("20/8/24", "$500.00", "VISA 4712******8415-11:07", "Paid"));
        paymentList.add(new Transaction("5/7/24", "$300.00", "4712******8415-10:07", "Paid"));
        paymentList.add(new Transaction("10/7/24", "-$20.00", "4712******8415-09:07", "Voided"));
        paymentList.add(new Transaction("3/9/24", "$150.00", "4712******8415-08:07", "Paid"));*/

    }


    public void requestTransactionRequest(String  filter) {
        TRACE.d("result network requestTransactionRequest");
        TransactionRequest transactionRequest = createAuthRequest(filter);
        addSubscribe(apiService.getTransaction(AUTHFROMISSUER_URL, transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    TRACE.i("result network rsp code=" + response.getResult());

//                    JSONArray jsonArray = (JSONArray) response.getResult();
//                    if(jsonArray.length() >0){
//
//                    }
                    String jsonString = JsonUtil.toJsonString(response.getResult());
                    List<Transaction> transactions = JsonParser.parseTransactionList(jsonString);
                    transactionList.setValue(transactions);
                }, throwable -> {
                    ToastUtils.showShort("The network is failed：" + throwable.getMessage());
                }));
    }

    private TransactionRequest createAuthRequest(String  filter) {
        String deviceSn = SPUtils.getInstance().getString("posID", "");
        return new TransactionRequest(deviceSn,filter);
    }

}
