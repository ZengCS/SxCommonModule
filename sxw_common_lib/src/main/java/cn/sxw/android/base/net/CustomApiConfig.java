package cn.sxw.android.base.net;

/**
 * Created by ZengCS on 2017/8/3.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 * <p>
 * 服务器地址管理
 */
public class CustomApiConfig {
    private static int apiIndex = 0;

    private static class RunEnvironment {
        static final String[] COMMON_SERVER_APIS = {
                "http://api2.dev.sxw.cn",// 开发
                "http://api2.test.sxw.cn",// 测试
                "http://api2.sxw.cn",// 预发布
                "http://api2.sxw.cn",// 生产
        };

        static final String[] UIC_APIS = {
                "http://192.168.2.83:8042/api/",// dev
                "http://192.168.2.202:8042/api/",// test
                "http://api.pre.sxw.cn/gateway/uic/api/",// 预发布
                "http://api2.sxw.cn/gateway/uic/api/",// 生产
        };

        static final String[] PLATFORM_APIS = {
                "http://192.168.2.83:8052/api/",// dev
                "http://192.168.2.202:8052/api/",// test
                "http://api.sxw.cn/gateway/platform/api/",// 预发布
                "http://api2.sxw.cn/gateway/platform/api/",// 生产
        };

        static final String[] OLD_PLATFORM_APIS = {
                "http://192.168.2.56:8080/SXWFrame/restful/",// dev
                "http://192.168.2.42:8080/SXWFrame/restful/",// test
                "http://120.55.92.178:8090/SXWFrame/restful/",// 预发布
                "http://frame.sxw.cn/SXWFrame/restful/",// 生产
        };
    }

    /**
     * @return 通用头
     */
    public static String getCommonServerHost() {
        return RunEnvironment.COMMON_SERVER_APIS[apiIndex];
    }

    /**
     * @return UIC接口头
     */
    public static String getUicHost() {
        return RunEnvironment.UIC_APIS[apiIndex];
    }

    /**
     * @return 云平台接口头
     */
    public static String getPlatformHost() {
        return RunEnvironment.PLATFORM_APIS[apiIndex];
    }

    /**
     * @return 老云平台接口头
     */
    public static String getOldPlatformHost() {
        return RunEnvironment.OLD_PLATFORM_APIS[apiIndex];
    }
}
