package com.dspread.pos.common.room;

import android.app.Application;

import com.dspread.pos.utils.TRACE;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;

public class TransactionRecordRepository {

    private final TransactionRecordDao transactionRecordDao;
    private final ExecutorService executorService;

    // Single instance
    private static volatile TransactionRecordRepository instance;


    private TransactionRecordRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionRecordDao = database.transactionRecordDao();
        executorService = Executors.newSingleThreadExecutor();
    }


    /**
     * Get singleton instance (requires Application parameter)
     *
     * @ param application instance
     * @ return TransactionRecordRepository singleton
     */
    public static TransactionRecordRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (TransactionRecordRepository.class) {
                if (instance == null) {
                    instance = new TransactionRecordRepository(application);
                }
            }
        }
        return instance;
    }

    /**
     * destory this a single instance
     */
    public static void destroy() {
        if (instance != null) {
            instance.executorService.shutdown();
            instance = null;
        }
    }

    public void insertAsync(TransactionRecord transactionRecord, InsertCallback callback) {
        executorService.execute(() -> {
            long id = transactionRecordDao.insert(transactionRecord);
            if (callback != null) {
                callback.onInserted(id);
                String deviceSn = transactionRecord.getDeviceSn();
                TRACE.d("insert deviceSN:" + deviceSn);
            }
        });
    }

    public List<TransactionRecord> getAllRecords() {
        return transactionRecordDao.getAllRecords();
    }

    public List<TransactionRecord> getRecordsByDeviceSn(String deviceSn) {
        return transactionRecordDao.getRecordsByDeviceSn(deviceSn);
    }

    public List<TransactionRecord> getLast3DaysRecordsByDeviceSn(String deviceSn) {

        return transactionRecordDao.getLast3DaysRecordsByDeviceSn(deviceSn);
    }

    public LiveData<List<TransactionRecord>> getRecordsByTransactionType(String transactionType) {
        return transactionRecordDao.getRecordsByTransactionType(transactionType);
    }

    public List<TransactionRecord> getRecordsByDate(String date) {
        return transactionRecordDao.getRecordsByDate(date);
    }

    public TransactionRecord getRecordById(long id) {
        return transactionRecordDao.getRecordById(id);
    }

    public void deleteById(long id) {
        executorService.execute(() -> transactionRecordDao.deleteById(id));
    }

    public void deleteOldRecords(long date) {
        executorService.execute(() -> transactionRecordDao.deleteOldRecords(date));
    }

    public int getRecordCount() {
        return transactionRecordDao.getRecordCount();
    }

    public void deleteAll() {
        executorService.execute(transactionRecordDao::deleteAll);
    }

    public interface InsertCallback {
        void onInserted(long id);
    }
}