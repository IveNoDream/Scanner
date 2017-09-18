package com.uuzuche.lib_zxing.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

//import com.uuch.android_zxinglibrary.utils.CheckPermissionUtils;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.CaptureFragment;

import java.util.ArrayList;
import java.util.List;

import com.uuzuche.lib_zxing.R;

/**
 * 定制化显示扫描界面
 */
public class SecondActivity extends FragmentActivity {

    private CaptureFragment captureFragment;
    private static boolean isOpen = false;
    private static final int PERMISSION_REQUEST_CODE_KEY = 100;
    private static final int REQUEST_IMAGE = 101;
    //需要申请的权限
    private static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_second);
        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();

        initView();
        requestRuntimePermission(this,checkPermission(this));

    }

    private void requestRuntimePermission(Context context, String[] permissions) {
        List<String> permissionStrings = new ArrayList<String>();
        boolean isNeedRequest = false;   //is need request or not

        for (int i = 0; i < permissions.length; i++) {
            int isGranted = checkSelfPermission(permissions[i]);
            if (PackageManager.PERMISSION_GRANTED != isGranted) {
                permissionStrings.add(permissions[i]);
                isNeedRequest = true;
            }
        }

        if (isNeedRequest == true) {
            String[] mPermissionList = new String[permissionStrings.size()];
            mPermissionList = permissionStrings.toArray(mPermissionList);
            requestPermissions(mPermissionList,
                    PERMISSION_REQUEST_CODE_KEY);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = true;
        boolean mShowPermission = true;
        if (requestCode == PERMISSION_REQUEST_CODE_KEY) {
            granted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (!granted) {
                mShowPermission = shouldShowRequestPermissionRationale(permissions[0]);
                //refused
                gotoSettings();
            }

            if (granted == true && 0 == checkPermission(this).length) {
                //request permission success, do something
                Toast.makeText(SecondActivity.this,"申请成功", Toast.LENGTH_SHORT).show();
            } else if (granted == true && 0 < checkPermission(this).length){
                gotoSettings();
            }else if (!mShowPermission) {
                //Permission refused Toast no longer asked
                gotoSettings();
            }
        }
    }

    private void gotoSettings() {
        Toast.makeText(SecondActivity.this,R.string.permission_refused_to_settings, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:"+ getPackageName()));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isOpen) {
            CodeUtils.isLightEnable(false);
            isOpen = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 选择系统图片并解析
         */
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            Toast.makeText(SecondActivity.this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Toast.makeText(SecondActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initView() {
        ImageView flash = (ImageView) findViewById(R.id.iv_flash);
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpen) {
                    CodeUtils.isLightEnable(true);
                    isOpen = true;
                } else {
                    CodeUtils.isLightEnable(false);
                    isOpen = false;
                }

            }
        });

        ImageView gallery = (ImageView) findViewById(R.id.iv_gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }


    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Toast.makeText(SecondActivity.this, "解析结果:" + result, Toast.LENGTH_LONG).show();
//            Intent resultIntent = new Intent();
//            Bundle bundle = new Bundle();
//            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
//            bundle.putString(CodeUtils.RESULT_STRING, result);
//            resultIntent.putExtras(bundle);
//            SecondActivity.this.setResult(RESULT_OK, resultIntent);
//            SecondActivity.this.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Toast.makeText(SecondActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
//            Intent resultIntent = new Intent();
//            Bundle bundle = new Bundle();
//            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
//            bundle.putString(CodeUtils.RESULT_STRING, "");
//            resultIntent.putExtras(bundle);
//            SecondActivity.this.setResult(RESULT_OK, resultIntent);
//            SecondActivity.this.finish();
        }
    };


    //检测权限
    public static String[] checkPermission(Context context){
        List<String> data = new ArrayList<>();//存储未申请的权限
        for (String permission : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(context, permission);
            if(checkSelfPermission == PackageManager.PERMISSION_DENIED){//未申请
                data.add(permission);
            }
        }
        return data.toArray(new String[data.size()]);
    }
}
