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
 * Transaction date filter utility class
 * Provides functionality to filter transaction data by date
 */
public class TransactionDateFilter {

    private TransactionDateFilter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Filter out all transactions for the current day based on the date of the first transaction
     *
     * @param transactions Transaction list
     * @return Today's transaction list, returns empty list if input is empty
     */
    public static List<Transaction> getTodayTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Transaction> todayTransactions = new ArrayList<>();

        // Get today's Calendar instance
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

                // Compare if year, month, and day are the same
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