package com.dspread.pos.ui.transaction;

public class Transaction {
    private String date;
    private String amount;
    private String cardInfo;
    private String status;
    private String month; // 新增月份字段

    public Transaction(String date, String amount, String cardInfo, String status) {
        this.date = date;
        this.amount = amount;
        this.cardInfo = cardInfo;
        this.status = status;
        // 从日期中提取月份信息，例如 "12/8/24" -> "August"
        this.month = extractMonthFromDate(date);
    }

    private String extractMonthFromDate(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length >= 2) {
                int monthNum = Integer.parseInt(parts[1]);
                String[] months = {"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};
                if (monthNum >= 1 && monthNum <= 12) {
                    return months[monthNum - 1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public String getStatus() {
        return status;
    }

    public String getMonth() {
        return month;
    }
}