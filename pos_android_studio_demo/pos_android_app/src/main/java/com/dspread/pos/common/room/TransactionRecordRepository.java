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

    // 单例实例
    private static volatile TransactionRecordRepository instance;

    /**
     * 私有构造函数
     */
    private TransactionRecordRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionRecordDao = database.transactionRecordDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 获取单例实例（需要Application参数）
     *
     * @param application Application实例
     * @return TransactionRecordRepository单例
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
     * 销毁单例实例（主要用于测试或清理）
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