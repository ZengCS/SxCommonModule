package cn.sxw.android.base.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by ZengCS on 2018/7/18.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 * <p>
 * 生学教育单点登录工具类
 */
public class SxwMobileSSOUtil {
    private static final String FILE_PATH = "sxw/app/common/auth/";
    private static final String FILE_NAME = "account.data";
    private static final String TAG = "SxwMobileSSOUtil";

    /**
     * 获取单点登录信息
     *
     * @return 学生登录信息&用户信息【JSON格式字符串】
     */
    public static String getSSOInfo() {
        try {
            String dest = readSdcardFile(getSDCardPath() + FILE_PATH + FILE_NAME);
            return AESUtils.Decrypt(dest);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(e);
        }
        return null;
    }

    /**
     * 此方法仅限管控学生登录成功后，将登录信息加密后保存到SD卡中，课堂课外请使用 getSSOInfo
     */
    public static boolean saveOSSInfo(@NonNull String src) {
        try {
            String encrypt = AESUtils.Encrypt(src);
            save2SDCard(FILE_PATH, FILE_NAME, encrypt);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "saveOSSInfo: 保存OSS登录信息失败", e);
            e.printStackTrace();
            LogUtil.e(e);
        }
        return false;
    }

    private static String readSdcardFile(String fileName) {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            // 这里文件较小，一次性读取全部
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(e);
        }
        return res;
    }

    /**
     * 保存文件数据到SD卡
     *
     * @param filePath
     * @param fileName
     * @param content
     * @throws Exception
     */
    private static void save2SDCard(String filePath, String fileName, String content)
            throws Exception {
        // 从API中获取SDCard的路径，解决各种Android系统的兼容性问题
        String path = mkdirs(filePath);
        if (path != null) {
            // 创建文件
            File file = new File(path, fileName);
            // 新建一个文件的输出流
            FileOutputStream outStream = new FileOutputStream(file);
            // 向文件中写入数据，将字符串转换为字节
            outStream.write(content.getBytes());
            // 关掉这个流
            outStream.close();
        }
    }

    /**
     * 按名称创建目录
     *
     * @param filePath eg:"test/temp/"
     * @return 文件夹完整路径
     */
    private static String mkdirs(String filePath) {
        String rootPath = getSDCardPath();
        if (rootPath != null) {
            File dir = new File(rootPath + filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        }
        return null;
    }

    /**
     * 检验SDcard状态
     */
    private static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SDCard的目录路径功能
     */
    private static String getSDCardPath() {
        return (checkSDCard() ? Environment.getExternalStorageDirectory().getAbsolutePath()
                : Environment.getDataDirectory().getAbsolutePath())
                + File.separator;
    }

}
