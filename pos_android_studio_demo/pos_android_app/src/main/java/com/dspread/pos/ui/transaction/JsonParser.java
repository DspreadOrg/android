package com.dspread.pos.ui.transaction;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class JsonParser {

    private static final Gson gson = new Gson();

    // 将 JSON 字符串转换为 Transaction 对象列表
    public static List<Transaction> parseTransactionList(String jsonString) {
        Type transactionListType = new TypeToken<List<Transaction>>() {
        }.getType();
        return gson.fromJson(jsonString, transactionListType);
    }

    // 将 Transaction 对象列表转换为 JSON 字符串
    public static String toJson(List<Transaction> transactions) {
        return gson.toJson(transactions);
    }

    // 将单个 Transaction 对象转换为 JSON 字符串
    public static String toJson(Transaction transaction) {
        return gson.toJson(transaction);
    }

    // 将 JSON 字符串转换为单个 Transaction 对象
    public static Transaction parseTransaction(String jsonString) {
        return gson.fromJson(jsonString, Transaction.class);
    }
}