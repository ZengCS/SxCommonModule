package cn.sxw.android.base.okhttp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/08/21 06:11
 */
public final class GsonUtil {
    private static Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> cls) {
        return gson.fromJson(json, cls);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        return fromJson(json, typeToken.getType());
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}
