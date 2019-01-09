package cn.sxw.android.lib.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import cn.sxw.android.lib.R;
import cn.sxw.android.lib.ui.base.CustomBaseActivity;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by ZengCS on 2019/1/3.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 * <p>
 * 权限申请测试界面
 */
@RuntimePermissions
public class PermissionActivity extends CustomBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
    }

    public void openCamera(View view) {
        PermissionActivityPermissionsDispatcher.openCameraWithPermissionCheck(this);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void openCamera() {
        Toast.makeText(this, "已具备访问相机的权限，可以在这里编写具体代码。", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void deniedForCamera() {
        Toast.makeText(this, "申请相机权限时被拒绝了。", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void neverAskForCamera() {
        // 申请相机权限时被拒绝了，并且不再询问。
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_tips_never_ask, "晓我课堂", "晓我课堂", "相机"))
                .setPositiveButton("应用设置", (dialog, button) -> openAppDetailSettings())
                .setNegativeButton("取消", (dialog, button) -> {
                    // do nothing
                })
                .show();
    }
}
