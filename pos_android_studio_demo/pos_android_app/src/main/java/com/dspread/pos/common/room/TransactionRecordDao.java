package com.dspread.pos.common.room;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface TransactionRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransactionRecord transactionRecord);

    @Query("SELECT * FROM transaction_record")
    List<TransactionRecord> getAllRecords();

    @Query("SELECT * FROM transaction_record WHERE device_sn = :deviceSn ORDER BY created_at DESC")
    List<TransactionRecord> getRecordsByDeviceSn(String deviceSn);

    @Query("SELECT * FROM transaction_record WHERE transaction_type = :transactionType ORDER BY created_at DESC")
    LiveData<List<TransactionRecord>> getRecordsByTransactionType(String transactionType);

    @Query("SELECT * FROM transaction_record WHERE device_date = :date ORDER BY created_at DESC")
    List<TransactionRecord> getRecordsByDate(String date);

    @Query("SELECT * FROM transaction_record WHERE id = :id")
    TransactionRecord getRecordById(long id);

    @Query("SELECT COUNT(*) FROM transaction_record")
    int getRecordCount();

    @Query("DELETE FROM transaction_record WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM transaction_record WHERE created_at < :date")
    void deleteOldRecords(long date);

    @Query("DELETE FROM transaction_record")
    void deleteAll();

    //:Querying device data within three days (including today, yesterday, and the day before yesterday)
    @Query("SELECT * FROM transaction_record " + "WHERE device_sn = :deviceSn " + "AND device_date >= date('now', 'localtime', '-2 days') " + "AND device_date <= date('now', 'localtime') " + "ORDER BY created_at DESC")
    List<TransactionRecord> getLast3DaysRecordsByDeviceSn(String deviceSn);
}