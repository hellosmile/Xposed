package io.github.hellosmile.xposed;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import io.github.hellosmile.R;
import io.github.hellosmile.xposed.detect.XposedDetect;

public class MainActivity extends AppCompatActivity {

    private TextView mTvDeviceId;
    private TextView mTvDetectResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();
    }

    private void initUi() {
        mTvDeviceId = findViewById(R.id.tv_device_id);
        mTvDetectResult = findViewById(R.id.tv_detect_result);

        findViewById(R.id.btn_get_device_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        findViewById(R.id.btn_detect_xposed_by_package_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = XposedDetect.detectXposedByCheckPackageInfo(getApplicationContext());
                setDetectResult(result);
            }
        });
        findViewById(R.id.btn_detect_xposed_by_exception).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = XposedDetect.detectXposedByCheckException(getApplicationContext());
                setDetectResult(result);
            }
        });
        findViewById(R.id.btn_detect_xposed_by_method_modifier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = XposedDetect.detectXposedByCheckMethodModifier(getApplicationContext());
                setDetectResult(result);
            }
        });
        findViewById(R.id.btn_detect_xposed_by_xposed_helper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = XposedDetect.detectXposedByCheckXposedHelper(getApplicationContext());
                setDetectResult(result);
            }
        });
        findViewById(R.id.btn_detect_xposed_by_maps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = XposedDetect.detectXposedByCheckProcMaps(getApplicationContext());
                setDetectResult(result);
            }
        });
    }

    private void loadData() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            String imei = telephonyManager.getDeviceId();
            mTvDeviceId.setText(imei);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setDetectResult(boolean result) {
        mTvDetectResult.setText(result ? "Has Xposed Installer" : "No Xposed Installer");
    }

    static {
        System.loadLibrary("detectxposed");
    }
}