package com.dspread.pos.ui.transaction;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionSorter {

    public static void sortTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        Collections.sort(transactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                // 处理 null 值的情况：null 的排在后面
                if (t1.getTransactionDate() == null && t2.getTransactionDate() == null) {
                    return 0; // 两个都为 null，视为相等
                }
                if (t1.getTransactionDate() == null) {
                    return 1; // t1 为 null，t2 不为 null，t1 排在后面
                }
                if (t2.getTransactionDate() == null) {
                    return -1; // t2 为 null，t1 不为 null，t1 排在前面
                }

                // 比较日期
                int dateComparison = t2.getTransactionDate().compareTo(t1.getTransactionDate());
                if (dateComparison != 0) {
                    return dateComparison; // 日期不同，直接返回比较结果
                }

                // 日期相同，比较时间
                // 处理时间为 null 的情况
                if (t1.getTransactionTime() == null && t2.getTransactionTime() == null) {
                    return 0;
                }
                if (t1.getTransactionTime() == null) {
                    return 1; // t1 时间为 null，排在后面
                }
                if (t2.getTransactionTime() == null) {
                    return -1; // t2 时间为 null，排在后面
                }

                // 比较时间
                return t2.getTransactionTime().compareTo(t1.getTransactionTime());
            }
        });
    }

    // 使用方法示例
    public void exampleUsage(List<Transaction> transactions) {
        // 对交易列表进行排序
        sortTransactions(transactions);

        // 现在 transactions 已经按照要求排序：日期时间最近的排在上面，null 的排在最后
    }
}