package com.dspread.pos.common.manager;

import com.dspread.pos.posAPI.BaseQPOSCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QPOSCallbackManager {
    private static QPOSCallbackManager instance;
    public final Map<Class<? extends BaseQPOSCallback>, BaseQPOSCallback> callbackMap = new ConcurrentHashMap<>();

    public static QPOSCallbackManager getInstance() {
        if (instance == null) {
            synchronized (QPOSCallbackManager.class) {
                if (instance == null) {
                    instance = new QPOSCallbackManager();
                }
            }
        }
        return instance;
    }

    public <T extends BaseQPOSCallback> void registerCallback(Class<T> type, T callback) {
        callbackMap.put(type, callback);
    }

    public <T extends BaseQPOSCallback> void unregisterCallback(Class<T> type) {
        callbackMap.remove(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseQPOSCallback> T getCallback(Class<T> type) {
        return (T) callbackMap.get(type);
    }
}