package com.dspread.pos.common.manager;

import com.dspread.pos.posAPI.ConnectionServiceCallback;
import com.dspread.pos.posAPI.PaymentServiceCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QPOSCallbackManager {
    private static QPOSCallbackManager instance;

    public final Map<Class<?>, Object> callbackMap = new ConcurrentHashMap<>();

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

    /**
     * register payment callback
     */
    public void registerPaymentCallback(PaymentServiceCallback callback) {
        callbackMap.put(PaymentServiceCallback.class, callback);
    }

    /**
     * register connection service callback
     */
    public void registerConnectionCallback(ConnectionServiceCallback callback) {
        callbackMap.put(ConnectionServiceCallback.class, callback);
    }

    /**
     * unregister payment callback
     */
    public void unregisterPaymentCallback() {
        callbackMap.remove(PaymentServiceCallback.class);
    }

    /**
     * unregister connection service callback
     */
    public void unregisterConnectionCallback() {
        callbackMap.remove(ConnectionServiceCallback.class);
    }

    /**
     * get payment callback
     */
    @SuppressWarnings("unchecked")
    public PaymentServiceCallback getPaymentCallback() {
        return (PaymentServiceCallback) callbackMap.get(PaymentServiceCallback.class);
    }

    /**
     * get connection service callback
     */
    @SuppressWarnings("unchecked")
    public ConnectionServiceCallback getConnectionCallback() {
        return (ConnectionServiceCallback) callbackMap.get(ConnectionServiceCallback.class);
    }

}