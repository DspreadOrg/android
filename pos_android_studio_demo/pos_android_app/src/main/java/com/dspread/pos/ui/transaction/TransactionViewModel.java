package com.dspread.pos.ui.transaction;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.common.http.RetrofitClient;
import com.dspread.pos.common.http.api.RequestOnlineAuthAPI;
import com.dspread.pos.common.http.model.AuthRequest;
import com.dspread.pos.common.http.model.TransactionRequest;
import com.dspread.pos.common.room.TransactionRecord;
import com.dspread.pos.common.room.TransactionRecordRepository;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.ui.payment.PaymentModel;
import com.dspread.pos.ui.setting.device_config.DeviceConfigItem;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.JsonUtil;
import com.dspread.pos.utils.TRACE;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class TransactionViewModel extends BaseAppViewModel {
    private final TransactionRecordRepository transactionRecordRepository;
    public MutableLiveData<List<Transaction>> transactionList = new MutableLiveData<>();

    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<Boolean> isEmpty = new ObservableField<>(false);
    public ObservableField<Boolean> isTransactionHeader = new ObservableField<>(true);
    public ObservableField<Boolean> isTransactionViewAll = new ObservableField<>(true);

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRecordRepository = TransactionRecordRepository.getInstance(application);
    }

    public void init() {
        isLoading.set(true);
        String filterType = SPUtils.getInstance().getString("filterType", "all");
        requestTransactionRequest(filterType);
    }

    public void refreshWithFilter(String filter) {
        isLoading.set(true);
        requestTransactionRequest(filter);
    }

    public void requestTransactionRequest(String filter) {
        isLoading.set(true);
        String deviceSn = SPUtils.getInstance().getString("posID", "");
        executor.execute(() -> {
            List<TransactionRecord> records;

            switch (filter) {
                case "1":
                    records = transactionRecordRepository.getRecordsByDate(DeviceUtils.getDeviceDate());
                    break;
                case "3":
                    records = transactionRecordRepository.getLast3DaysRecordsByDeviceSn(deviceSn);
                    break;
                case "all":
                default:
                    records = transactionRecordRepository.getRecordsByDeviceSn(deviceSn);
                    break;
            }

            List<Transaction> transactions = convertToTransactionList(records);

            mainHandler.post(() -> {
                transactionList.setValue(transactions);
                isLoading.set(false);
            });
        });
    }
    private List<Transaction> convertToTransactionList(List<TransactionRecord> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<Transaction> transactionList = new ArrayList<>(records.size());
        for (TransactionRecord record : records) {
            Transaction transaction = convertToTransaction(record);
            if (transaction != null) {
                transactionList.add(transaction);
            }
        }
        return transactionList;
    }

    private Transaction convertToTransaction(TransactionRecord record) {
        if (record == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(record.getId());
        transaction.setDeviceSn(record.getDeviceSn());
        transaction.setAmount(Double.parseDouble(record.getAmount()));
        transaction.setTransactionDate(record.getDeviceDate());
        transaction.setTransactionTime(record.getDeviceTime());
        transaction.setTransactionType(record.getTransactionType());
        transaction.setCardOrg(record.getCardOrg());
        transaction.setMaskPan(record.getMaskPan());
        transaction.setPayType(record.getPayType());
        transaction.setTransResult(record.getTransResult());
        return transaction;
    }
}
