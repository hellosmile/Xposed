package io.github.hellosmile.xposed;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook 系统的 TelephonyManager。
 *
 * @author WangYixuan
 * @email devin.wang@ximalaya.com
 * @phoneNumber 18817333656
 * @wiki Wiki网址放在这里
 * @server 服务端开发人员放在这里
 * @since 2020/6/21
 */
public class Main implements IXposedHookLoadPackage {

    private static final String TAG = "Xposed";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Log.i(TAG, "packageName = " + lpparam.packageName);

        hookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getDeviceId", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Log.i(TAG, "Hook getDeviceId");

                Object object = param.getResult();;

                Log.i(TAG, "IMEI Args: " + object);

                param.setResult("1234567890");

                Log.i(TAG, "Replace IMEI Args: " + object);

                super.afterHookedMethod(param);
            }
        });

    }

    private void hookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
