package cn.sxw.android.base.cache;

import android.content.Context;
import android.content.SharedPreferences;

import cn.sxw.android.base.utils.BaseLogUtil;

/**
 * SharedPreferences的一个工具类，调用setParam就能保存String, Integer, Boolean, Float,
 * Long类型的参数,同样调用getParam就能获取到保存在手机里面的数据
 *
 * @author ZengCS
 * @since 2014年10月15日
 */
public class SharedPreferencesUtil {
    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "SXW_LAUNCHER_V1";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static final String KEY_REBOOT_FLAG = "KEY_REBOOT_FLAG";

    /**
     * 策略相关
     */
    public class Policy {
        public static final String SERVER_VERSION_CODE = "POLICY_SERVER_VERSION_CODE";
        public static final String LOCAL_VERSION_CODE = "POLICY_LOCAL_VERSION_CODE";
        public static final String HEART_TIME = "POLICY_HEART_TIME";
        public static final String SYNC_TIME = "POLICY_SYNC_TIME";
        public static final String CONTENT = "POLICY_CONTENT";
        public static final String TARGET_RULE = "POLICY_TARGET_RULE";
        public static final String TARGET_TIME_CATEGORY = "POLICY_TARGET_TIME_CATEGORY";
        public static final String APP_TYPES = "POLICY_APP_TYPES";
        public static final String RULE_VERSION_DTO = "POLICY_RULE_VERSION_DTO";
        public static final String ALL_TIME_APP_LIST = "POLICY_ALL_TIME_APP_LIST";
        public static final String USER_APP_LIST = "POLICY_USER_APP_LIST";
    }

    public class Account {
        public static final String IS_LOGIN = "IS_LOGIN";
        public static final String ACCOUNT_INFO = "ACCOUNT_INFO";
        public static final String ACCOUNT_BIND_INFO = "ACCOUNT_BIND_INFO";
        public static final String PUSH_CLIENT_ID = "PUSH_CLIENT_ID";
        public static final String KEY_CURR_ENVIRONMENT = "KEY_CURR_ENVIRONMENT_V2";
    }

    public static String getCustomKey(String key, String accountId) {
        return key + "_" + accountId;
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void setParam(Context context, String key, Object object) {
        try {
            if (object == null)
                return;

            String type = object.getClass().getSimpleName();
            SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            if ("String".equals(type)) {
                editor.putString(key, (String) object);
            } else if ("Integer".equals(type)) {
                editor.putInt(key, (Integer) object);
            } else if ("Boolean".equals(type)) {
                editor.putBoolean(key, (Boolean) object);
            } else if ("Float".equals(type)) {
                editor.putFloat(key, (Float) object);
            } else if ("Long".equals(type)) {
                editor.putLong(key, (Long) object);
            }

            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            BaseLogUtil.e(e);
        }
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getParam(Context context, String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        if ("String".equals(type)) {
            return sp.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    public static Boolean getBoolean(Context context, String key, Object defaultObject) {
        return (Boolean) getParam(context, key, defaultObject);
    }

    public static String getString(Context context, String key, Object defaultObject) {
        return (String) getParam(context, key, defaultObject);
    }

    public static int getInt(Context context, String key, Object defaultObject) {
        return (int) getParam(context, key, defaultObject);
    }

}
