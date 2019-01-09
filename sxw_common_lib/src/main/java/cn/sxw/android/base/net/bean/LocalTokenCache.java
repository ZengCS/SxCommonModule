package cn.sxw.android.base.net.bean;

import org.xutils.x;

import cn.sxw.android.base.cache.SxwCache;

/**
 * Created by ZengCS on 2018/5/24.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */

public class LocalTokenCache {
    private static String CURR_TIME_TOKEN = "KEY_CURR_TIME_TOKEN";
    private static String CURR_TIME_REFRESH_TOKEN = "KEY_CURR_TIME_REFRESH_TOKEN";

    public static String getLocalCacheToken() {
        return SxwCache.get(x.app()).getAsString(CURR_TIME_TOKEN);
    }

    public static void setLocalCacheToken(String token) {
        SxwCache.get(x.app()).put(CURR_TIME_TOKEN, token);
    }

    public static String getLocalCacheRefreshToken() {
        return SxwCache.get(x.app()).getAsString(CURR_TIME_REFRESH_TOKEN);
    }

    public static void setLocalCacheRefreshToken(String refreshToken) {
        SxwCache.get(x.app()).put(CURR_TIME_REFRESH_TOKEN, refreshToken);
    }
}
