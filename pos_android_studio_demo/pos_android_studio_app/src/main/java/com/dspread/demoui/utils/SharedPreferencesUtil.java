package com.dspread.demoui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public class SharedPreferencesUtil {

    private static final String FILE_NAME="data";

    private static SharedPreferencesUtil mInstance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private SharedPreferencesUtil(){

    }

    public static SharedPreferencesUtil getInstance(Context context){
        if (mInstance == null){
            synchronized (SharedPreferencesUtil.class){
                if (mInstance == null){
                    mInstance =new SharedPreferencesUtil();
                    sharedPreferences =context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                }
            }
        }
        return mInstance;
    }
    /**
     *Deposit the data corresponding to the corresponding key
     *value can be a value of basic data types such as String, boolean, float, int, long, etc
     */
    public void put(String key,Object value){

        String type=value.getClass().getSimpleName();

        if ("Integer".equals(type)){
            editor.putInt(key,(Integer) value);
        }else if("Boolean".equals(type)){
            editor.putBoolean(key,(Boolean)value);
        }else if("String".equals(type)){
            editor.putString(key,(String) value);
        }else if ("Float".equals(type)){
            editor.putFloat(key,(Float) value);
        }else if ("Long".equals(type)){
            editor.putLong(key,(Long) value);
        }
        editor.apply();
    }

    /**
     *Gets the value corresponding to the specified key in the SharedPreferences data. If the key does not exist, the default value defValue is returned
     */
    @Nullable
    public Object get( String key, Object defValue){
        String type=defValue.getClass().getSimpleName();

        if ("Integer".equals(type)){
            return sharedPreferences.getInt(key,(Integer) defValue);
        }else if("Boolean".equals(type)){
            return sharedPreferences.getBoolean(key,(Boolean)defValue);
        }else if("String".equals(type)){
            return sharedPreferences.getString(key,(String) defValue);
        }else if ("Float".equals(type)){
            return sharedPreferences.getFloat(key,(Float) defValue);
        }else if ("Long".equals(type)){
            return sharedPreferences.getLong(key,(Long) defValue);
        }
        return null;
    }
}
