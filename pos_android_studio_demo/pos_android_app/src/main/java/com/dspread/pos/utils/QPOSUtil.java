package com.dspread.pos.utils;

import android.content.Context;
import android.media.AudioManager;

import com.dspread.xpos.Util;
import com.dspread.xpos.utils.AESUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Hashtable;
import java.util.Map;

import me.goldze.mvvmhabit.utils.ToastUtils;


public class QPOSUtil {
    static final String HEXES = "0123456789ABCDEF";

    public static String byteArray2Hex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    //根据n和e获取公钥
    public static RSAPublicKey getPublicKey(String modulus, String publicExponent) {
        BigInteger m = new BigInteger(modulus, 16);
        BigInteger e = new BigInteger(publicExponent, 16);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);

        KeyFactory keyFactory;
        RSAPublicKey publicKey = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return publicKey;
    }

    /*
     * Convert hex value to ascii code
    **/
    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }

    /**
     * convert int to byte[]
     * @param i need to be converted to byte array
     * @return byte array
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[2];
//		result[0] = (byte)((i >> 24) & 0xFF);
//		result[1] = (byte)((i >> 16) & 0xFF);
        result[0] = (byte)((i >> 8) & 0xFF);
        result[1] = (byte)(i & 0xFF);
        return result;
    }

    //16 byte xor
    public static String xor16(byte[] src1, byte[] src2){
        byte[] results = new byte[16];
        for (int i = 0; i < results.length; i++){
            results[i] = (byte)(src1[i] ^ src2[i]);
        }
        return QPOSUtil.byteArray2Hex(results);
    }

    /**
     * convert a string in hexadecimal format to byte in hexadecimal format 44 --> byte 0x44
     *
     * @param hexString
     * @return
     */
    public static byte[] HexStringToByteArray(String hexString) {//
        if (hexString == null || hexString.equals("")) {
            return new byte[]{};
        }
        if (hexString.length() == 1 || hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * convert Chinese string into hexadecimal array
     *
     * @param str
     * @return
     */
    public static byte[] CNToHex(String str) {
        // String string = "";
        // for (int i = 0; i < str.length(); i++) {
        // String s = String.valueOf(str.charAt(i));
        // byte[] bytes = null;
        // try {
        // bytes = s.getBytes("gbk");
        // } catch (UnsupportedEncodingException e) {
        // e.printStackTrace();
        // }
        // for (int j = 0; j < bytes.length; j++) {
        // string += Integer.toHexString(bytes[j] & 0xff);
        // }
        // }
        byte[] b = null;
        try {
            b = str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return b;
    }

    //Low position ahead
    public static byte[] intToBytes( int value )
    {
        byte[] src = new byte[2];
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }

    public static String intToHex2(int i) {
        String string = null;
        if (i >= 0 && i < 10) {
            string = "0" + i;
        } else {
            string = Integer.toHexString(i);
        }
        if(string.length() == 2){
            string = "00" + string;
        }else if (string.length() == 1) {
            string = "000" + string;
        }else if(string.length() == 3){
            string = "0" + string;
        }
        return string;
    }

    /**
     * convert byte to hexadecimal string
     *
     * @param b
     * @return
     */
    public static String getHexString(byte[] b) {
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < b.length; i++) {
            result.append("0x" + Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1) + ",");
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * convert int to hexadecimal byte
     *
     * @param i
     * @return
     */
    public static byte[] IntToHex(int i) {
        String string = null;
        if (i >= 0 && i < 10) {
            string = "0" + i;
        } else {
            string = Integer.toHexString(i);
        }
        return HexStringToByteArray(string);
    }

    /**
     * convert the specified byte array to hexadecimal and print
     *
     * @param b
     */
    public static void printHexString(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }

    }

    /**
     * convert hexadecimal byte to int
     *
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b) {
        int result = 0;
        for (int i = 0; i < b.length; i++) {
            result <<= 8;
            result |= (b[i] & 0xff); //
        }
        return result;
    }

    /**
     * XOR input byte stream
     *
     * @param b
     * @param startPos
     * @param Len
     * @return
     */
    public static byte XorByteStream(byte[] b, int startPos, int Len) {
        byte bRet = 0x00;
        for (int i = 0; i < Len; i++) {
            bRet ^= b[startPos + i];
        }
        return bRet;
    }

    /**
     * Gets the subarray from <tt>array</tt> that starts at <tt>offset</tt>.
     */
    public static byte[] get(byte[] array, int offset) {
        return get(array, offset, array.length - offset);
    }

    /**
     * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that
     * starts at <tt>offset</tt>.
     */
    public static byte[] get(byte[] array, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
    }

    public static void turnUpVolume(Context context, int factor) {
        int sv;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        sv = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sv * factor / 10, AudioManager.FLAG_PLAY_SOUND);
    }


    public static byte[] bcd2asc(byte[] src) {
        byte[] results = new byte[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            // high Nibble conversion
            if (((src[i] & 0xF0) >> 4) <= 9) {
                results[2 * i] = (byte) (((src[i] & 0xF0) >> 4) + 0x30);
            } else {
                results[2 * i] = (byte) (((src[i] & 0xF0) >> 4) + 0x37); // 大写A~F
            }
            // low Nibble conversion
            if ((src[i] & 0x0F) <= 9) {
                results[2 * i + 1] = (byte) ((src[i] & 0x0F) + 0x30);
            } else {
                results[2 * i + 1] = (byte) ((src[i] & 0x0F) + 0x37); // 大写A~F
            }
        }
        return results;
    }

    public static byte[] ecb(byte[] in) {

        byte[] a1 = new byte[8];

        for (int i = 0; i < (in.length / 8); i++) {
            byte[] temp = new byte[8];
            System.arraycopy(in, i * 8, temp, 0, temp.length);
            a1 = xor8(a1, temp);
        }
        if ((in.length % 8) != 0) {
            byte[] temp = new byte[8];
            System.arraycopy(in, (in.length / 8) * 8, temp, 0, in.length - (in.length / 8) * 8);
            a1 = xor8(a1, temp);
        }
        return bcd2asc(a1);
    }

    public static byte[] xor8(byte[] src1, byte[] src2) {
        byte[] results = new byte[8];
        for (int i = 0; i < results.length; i++) {
            results[i] = (byte) (src1[i] ^ src2[i]);
        }
        return results;
    }


    public static boolean checkStringAllZero(String str) {
        if (str.startsWith("0"))
            return true;
        boolean result = true;
//        Integer.MAX_VALUE  4 bytes
//       long MAX_VALUE = 0x7fffffffffffffffL;
        int byteCou = str.length() / 2;
        int count;
        if (byteCou % 4 == 0) {
            count = byteCou / 4;
        } else {
            count = byteCou / 4 + 1;
        }
        String sub = null;
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                sub = str.substring(i * 8, sub.length());
            } else {
                sub = str.substring(i * 8, (i + 1) * 8);
            }
            long l = Long.parseLong(sub, 16);
            if (l > 0) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static String getRequiredParam(Map<String, byte[]> params, String key) {
        String value = Util.byteArray2Hex(params.get(key));
        if (value == null || value.trim().isEmpty()) {
            ToastUtils.showLong("Missing required parameter: " + key);
            return null;
        }
        return value.trim();
    }

    private static String buildPinDataBlock(String userPin, String randomData) {
        int pinLength = userPin.length();

        StringBuilder pinBlockBuilder = new StringBuilder()
                .append("4")                            // ISO format4 label
                .append(Integer.toHexString(pinLength)) // PIN length
                .append(userPin);                       // PIN

        // fill 'A' to block lenth to 16
        for (int i = 0; i < 16 - 2 -pinLength; i++) {
            pinBlockBuilder.append("A");
        }

        // add random data
        pinBlockBuilder.append(randomData, 0, 16);

        return pinBlockBuilder.toString();
    }

    private static String buildPanDataBlock(String pan) {
        int panLength = pan.length();

        StringBuilder panBlockBuilder = new StringBuilder();

        if (panLength < 12) {
            panBlockBuilder.append("0");
            panBlockBuilder.append(String.format("%0" + (12 - panLength) + "d", 0));
            panBlockBuilder.append(pan);
        } else {
            int rightOffset = panLength - 12;
            panBlockBuilder.append(Integer.toHexString(rightOffset));
            panBlockBuilder.append(pan);
        }

        // fill the pan lentght to 32 with 0(16bytes)
        while (panBlockBuilder.length() < 32) {
            panBlockBuilder.append("0");
        }

        return panBlockBuilder.toString();
    }

    public static String buildISO4PinBlock(Hashtable<String, byte[]> pinParams, String userPin) {
        if (pinParams == null) {
            ToastUtils.showLong("PIN params can't be null");
            return null;
        }
        if (userPin == null || !userPin.matches("\\d{4,12}")) {
            ToastUtils.showLong("PIN length must be 4-12");
            return null;
        }

        String randomData = getRequiredParam(pinParams, "RandomData");
        String pan = getRequiredParam(pinParams, "PAN");
        String AESKey = getRequiredParam(pinParams, "AESKey");

        //iso-format4 pinblock
        String pinBlock = buildPinDataBlock(userPin, randomData);
        String panBlock = buildPanDataBlock(pan);

        String encryptedPinBlock = AESUtil.encrypt(AESKey, pinBlock);
        String xoredResult = Util.xor16(HexStringToByteArray(encryptedPinBlock), HexStringToByteArray(panBlock));
        return AESUtil.encrypt(AESKey, xoredResult);
    }

}
