package com.dspread.pos.utils;

import java.util.Arrays;
import java.util.List;

public class AdvancedBinDetector {

    public enum CardType {
        VISA("Visa"),
        MASTERCARD("Mastercard"),
        AMEX("American Express"),
        DISCOVER("Discover"),
        UNIONPAY("UnionPay"),
        JCB("JCB"),
        DINERS_CLUB("Diners Club"),
        MAESTRO("Maestro"),
        VISA_ELECTRON("Visa Electron"),
        CHINA_TUION("China T-Union"),
        CHINA_UNIONPAY("China UnionPay"),
        INTERPAYMENT("Interpayment"),
        INSTAPAYMENT("InstaPayment"),
        UNKNOWN("Unknown");

        private final String displayName;

        CardType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 检测卡类型（精确BIN匹配）
     */
    public static CardType detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return CardType.UNKNOWN;
        }

        String cleanedNumber = cardNumber.replaceAll("\\s+", "");

        if (cleanedNumber.length() < 6) {
            return CardType.UNKNOWN;
        }

        // 获取不同长度的BIN
        String bin1 = cleanedNumber.substring(0, 1);
        String bin2 = cleanedNumber.substring(0, 2);
        String bin3 = cleanedNumber.substring(0, 3);
        String bin4 = cleanedNumber.substring(0, 4);
        String bin6 = cleanedNumber.substring(0, 6);

        // 按优先级检测（从最精确的匹配开始）
        if (isVisaElectron(bin4, bin6)) return CardType.VISA_ELECTRON;
        if (isMaestro(bin4, bin6)) return CardType.MAESTRO;
        if (isVisa(bin1, bin4, bin6)) return CardType.VISA;
        if (isMastercard(bin2, bin4, bin6)) return CardType.MASTERCARD;
        if (isAmex(bin2)) return CardType.AMEX;
        if (isDiscover(bin2, bin3, bin4, bin6)) return CardType.DISCOVER;
        if (isUnionPay(bin2, bin3)) return CardType.UNIONPAY;
        if (isJCB(bin2, bin3)) return CardType.JCB;
        if (isDinersClub(bin2, bin3, bin4)) return CardType.DINERS_CLUB;
        if (isChinaTUnion(bin2)) return CardType.CHINA_TUION;
        if (isInstaPayment(bin3)) return CardType.INSTAPAYMENT;
        if (isInterPayment(bin3)) return CardType.INTERPAYMENT;

        return CardType.UNKNOWN;
    }

    /**
     * Visa卡检测
     */
    private static boolean isVisa(String bin1, String bin4, String bin6) {
        // Visa标准BIN：以4开头，但不是Visa Electron
        return "4".equals(bin1) && !isVisaElectron(bin4, bin6);
    }

    /**
     * Visa Electron检测
     */
    private static boolean isVisaElectron(String bin4, String bin6) {
        List<String> electronBins = Arrays.asList(
                "4026", "417500", "4405", "4508", "4844",
                "4913", "4917", "5019"
        );

        for (String bin : electronBins) {
            if (bin6.startsWith(bin) || (bin.length() == 4 && bin4.equals(bin))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mastercard检测
     */
    private static boolean isMastercard(String bin2, String bin4, String bin6) {
        // 51-55
        if (bin2.matches("5[1-5]")) {
            return true;
        }

        // 2221-2720范围
        try {
            int binValue = Integer.parseInt(bin6.substring(0, 4));
            if (binValue >= 2221 && binValue <= 2720) {
                return true;
            }
        } catch (NumberFormatException e) {
            // 忽略转换错误
        }

        return false;
    }

    /**
     * American Express检测
     */
    private static boolean isAmex(String bin2) {
        return "34".equals(bin2) || "37".equals(bin2);
    }

    /**
     * Discover卡检测
     */
    private static boolean isDiscover(String bin2, String bin3, String bin4, String bin6) {
        // 6011, 644-649, 65
        if ("6011".equals(bin4) || "65".equals(bin2)) {
            return true;
        }

        // 644-649
        if (bin3.matches("64[4-9]")) {
            return true;
        }

        // 622126-622925 (China UnionPay co-branded)
        try {
            int binValue = Integer.parseInt(bin6.substring(0, 6));
            if (binValue >= 622126 && binValue <= 622925) {
                return true;
            }
        } catch (NumberFormatException e) {
            // 忽略转换错误
        }

        return false;
    }

    /**
     * UnionPay检测
     */
    private static boolean isUnionPay(String bin2, String bin3) {
        // 62开头
        if ("62".equals(bin2)) {
            return true;
        }

        // 81开头（部分UnionPay卡）
        if ("81".equals(bin2)) {
            return true;
        }

        return false;
    }

    /**
     * JCB检测
     */
    private static boolean isJCB(String bin2, String bin3) {
        // 3528-3589
        try {
            if (bin3.length() >= 3) {
                int binValue = Integer.parseInt(bin3);
                if (binValue >= 352 && binValue <= 358) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            // 忽略转换错误
        }

        // 2131, 1800
        if ("2131".equals(bin3) || "1800".equals(bin3)) {
            return true;
        }

        return false;
    }

    /**
     * Diners Club检测
     */
    private static boolean isDinersClub(String bin2, String bin3, String bin4) {
        // 300-305, 309
        if (bin3.matches("30[0-5]") || "309".equals(bin3)) {
            return true;
        }

        // 36
        if ("36".equals(bin2)) {
            return true;
        }

        // 38-39
        if (bin2.matches("3[8-9]")) {
            return true;
        }

        // 54, 55
        if ("54".equals(bin2) || "55".equals(bin2)) {
            return true;
        }

        return false;
    }

    /**
     * Maestro检测
     */
    private static boolean isMaestro(String bin4, String bin6) {
        List<String> maestroBins = Arrays.asList(
                "5018", "5020", "5038", "5893", "6304",
                "6759", "6761", "6762", "6763"
        );

        for (String bin : maestroBins) {
            if (bin6.startsWith(bin) || bin4.equals(bin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * China T-Union检测
     */
    private static boolean isChinaTUnion(String bin2) {
        return "31".equals(bin2);
    }

    /**
     * InstaPayment检测
     */
    private static boolean isInstaPayment(String bin3) {
        return "637".equals(bin3) || "638".equals(bin3) || "639".equals(bin3);
    }

    /**
     * Interpayment检测
     */
    private static boolean isInterPayment(String bin3) {
        return "636".equals(bin3);
    }

    /**
     * 获取卡号的建议长度
     */
    public static int getSuggestedLength(CardType cardType) {
        switch (cardType) {
            case VISA:
            case MASTERCARD:
            case DISCOVER:
            case UNIONPAY:
            case JCB:
            case MAESTRO:
            case VISA_ELECTRON:
                return 16;
            case AMEX:
                return 15;
            case DINERS_CLUB:
                return 14;
            default:
                return 16;
        }
    }

    /**
     * 获取卡号的验证算法
     */
    public static boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) {
            return false;
        }

        String cleaned = cardNumber.replaceAll("\\s+", "");
        int sum = 0;
        boolean alternate = false;

        for (int i = cleaned.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleaned.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * 完整的卡号验证
     */
    public static boolean isValidCard(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }

        String cleaned = cardNumber.replaceAll("\\s+", "");

        // 检查是否为纯数字
        if (!cleaned.matches("\\d+")) {
            return false;
        }

        // 检测卡类型
        CardType cardType = detectCardType(cleaned);
        if (cardType == CardType.UNKNOWN) {
            return false;
        }

        // 检查长度
        int suggestedLength = getSuggestedLength(cardType);
        if (cleaned.length() != suggestedLength) {
            return false;
        }

        // Luhn算法验证
        return isValidLuhn(cleaned);
    }
}