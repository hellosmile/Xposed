package io.github.hellosmile.xposed.detect;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Xposed 框架防范。
 *
 * @author WangYixuan
 * @email devin.wang@ximalaya.com
 * @phoneNumber 18817333656
 * @wiki Wiki网址放在这里
 * @server 服务端开发人员放在这里
 * @since 2020/6/21
 */
public class XposedDetect {
    private static final String TAG = "XposedDetect";

    /**
     * 方法一：通过 PackageManager 查看安装列表
     * <p>
     * 注意：
     * 通常情况下使用 Xposed Installer 框架都会屏蔽对其的检测，即 Hook 掉 PackageManager 的
     * getInstalledApplications 方法的返回值，以便过滤掉 de.robv.android.xposed.installer 来躲避这种检测。
     *
     * @param context 上下文环境
     * @return true 检测出 Xposed 框架 false 未检测出 Xposed 框架
     */
    public static boolean detectXposedByCheckPackageInfo(Context context) {
        if (null == context) {
            return false;
        }

        boolean isInstalled = false;
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            if ("de.robv.android.xposed.installer".equals(applicationInfo.packageName)) {
                isInstalled = true;
            }
        }

        Log.i(TAG, "isXposedInstaller, ");

        return isInstalled;
    }

    /**
     * 方法二：通过自造异常读取栈
     * <p>
     * Xposed Installer 框架对每个由 Zygote 孵化的 App 进程都会介入，因此在程序方法异常栈中就会出现 Xposed 相关的“身影”，
     * 我们可以通过自造异常 Catch 来读取异常堆栈的形式，用以检查其中是否存在 Xposed 的调用方法。
     *
     * @param context 上下文环境
     * @return true 检测出 Xposed 框架 false 未检测出 Xposed 框架
     */
    public static boolean detectXposedByCheckException(Context context) {
        if (null == context) {
            return false;
        }

        boolean isInstalled = false;

        try {
            throw new Exception("Detect");
        } catch (Exception e) {

            e.printStackTrace();

            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if (stackTraceElement.getClassName().contains("Xposed") || stackTraceElement.getMethodName().contains("Xposed")) {
                    isInstalled = true;
                }
            }
        }

        return isInstalled;
    }

    /**
     * 方法三：检查关键Java方法被变为Native JNI方法
     * <p>
     * 当一个 Android App 中的 Java 方法被莫名其妙地变成了 Native JNI 方法，则非常有可能被 Xposed Hook 了。由此可得，
     * 检查关键方法是不是变成 Native JNI 方法，也可以检测是否被 Hook。
     * <p>
     * 注意：
     * Xposed 同样可以篡改 isNative 这个方法的返回值。
     *
     * @param context 上下文环境
     * @return true 检测出 Xposed 框架 false 未检测出 Xposed 框架
     */
    public static boolean detectXposedByCheckMethodModifier(Context context) {
        if (null == context) {
            return false;
        }

        boolean isInstalled = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            Method method = telephonyManager.getClass().getMethod("getDeviceId");
            if (Modifier.isNative(method.getModifiers())) {
                isInstalled = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return isInstalled;
    }

    /**
     * 方法四：反射读取 XposedHelper 类字段
     * <p>
     * 通过反射遍历 XposedHelper 类中的 fieldCache、methodCache、constructorCache 变量，读取 HashMap 缓存字段，
     * 如字段项的 key 中包含 App 中唯一或敏感方法等，即可认为有 Xposed 注入。
     *
     * @param context 上下文环境
     * @return true 检测出 Xposed 框架 false 未检测出 Xposed 框架
     */
    public static boolean detectXposedByCheckXposedHelper(Context context) {
        if (null == context) {
            return false;
        }

        boolean isInstalled = false;


        String interName;
        Set keySet;
        String fieldName = "methodCache";
        try {
            Class xposedHelpers = Class.forName("de.robv.android.xposed.XposedHelpers");
            Field field = xposedHelpers.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            keySet = ((Map) field.get(xposedHelpers)).keySet();
            if (!keySet.isEmpty()) {
                for (Object ks : keySet) {
                    interName = ks.toString().toLowerCase();
                    if (interName.contains("hellosmile")) {
                        isInstalled = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isInstalled;
    }

    /**
     * 方法五：Native 检测
     *
     * 无论在 Java 层做何种检测，Xposed 都可以通过 Hook 相关的 API 并返回指定的结果来绕过检测，只要有方法就可以被 Hook。
     * 如果仅在 Java 层检测就显得很徒劳，为了有效提搞检测准确率，就须做到 Java 和 Native 层同时检测。每个App在系统中都有对应的加载库列表，
     * 这些加载库列表在 /proc/ 下对应的 pid/maps 文件中描述，在 Native 层读取 /proc/self/maps 文件不失为检测 Xposed Installer 的有效办法之一。
     * 由于 Xposed Installe r通常只能 Hook Java 层，因此在 Native 层使用 C 来解析 /proc/self/maps 文件，搜检App自身加载的库中是否存在 XposedBridge.jar、相关的 Dex、Jar 和 So 库等文件
     *
     * @param context 上下文环境
     * @return true 检测出 Xposed 框架 false 未检测出 Xposed 框架
     */
    public static boolean detectXposedByCheckProcMaps(Context context) {
        if (null == context) {
            return false;
        }

        return isXposed();
    }

    private static native boolean isXposed();
}
