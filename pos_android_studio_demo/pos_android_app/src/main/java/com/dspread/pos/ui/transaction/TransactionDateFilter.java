package com.dspread.pos.ui.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 交易日期过滤工具类
 * 提供根据日期过滤交易数据的功能
 */
public class TransactionDateFilter {

    private TransactionDateFilter() {
        // 私有构造方法，防止实例化
    }

    /**
     * 根据第一笔交易的日期过滤出当日所有交易数据
     *
     * @param transactions 交易列表
     * @return 当日交易列表，如果输入为空则返回空列表
     */
    public static List<Transaction> getTodayTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Transaction> todayTransactions = new ArrayList<>();

        // 获取今天的Calendar实例
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (Transaction transaction : transactions) {
            if (transaction != null && transaction.getTransactionDate() != null) {
                Calendar transactionCalendar = Calendar.getInstance();
                transactionCalendar.setTime(stringToDate(transaction.getTransactionDate().replaceAll("-", "/")));

                int transactionYear = transactionCalendar.get(Calendar.YEAR);
                int transactionMonth = transactionCalendar.get(Calendar.MONTH);
                int transactionDay = transactionCalendar.get(Calendar.DAY_OF_MONTH);

                // 比较年月日是否相同
                if (todayYear == transactionYear &&
                        todayMonth == transactionMonth &&
                        todayDay == transactionDay) {
                    todayTransactions.add(transaction);
                }
            }
        }
        return todayTransactions;
    }


    public static Date stringToDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}