package com.dspread.pos.common.manager;

import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.Map;

public class FragmentCacheManager {
    private static FragmentCacheManager instance;
    private Map<Integer, Fragment> fragmentCache;

    private FragmentCacheManager() {
        fragmentCache = new HashMap<>();
    }

    public static FragmentCacheManager getInstance() {
        if (instance == null) {
            synchronized (FragmentCacheManager.class) {
                if (instance == null) {
                    instance = new FragmentCacheManager();
                }
            }
        }
        return instance;
    }

    public void putFragment(int id, Fragment fragment) {
        fragmentCache.put(id, fragment);
    }

    public Fragment getFragment(int id) {
        return fragmentCache.get(id);
    }

    public boolean hasFragment(int id) {
        return fragmentCache.containsKey(id);
    }

    public void clearCache() {
        fragmentCache.clear();
    }
}