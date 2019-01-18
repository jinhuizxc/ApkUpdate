package demo.com.apkupdate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ToastUtils;

import demo.com.apkupdate.R;
import demo.com.apkupdate.update.UpdateService;
import demo.com.apkupdate.view.CommonDialog;

/**
 *  解决 Android N 7.0 上 报错：android.os.FileUriExposedException
 *  https://blog.csdn.net/yy1300326388/article/details/52787853
 *
 *  git：(https://github.com/jinhuizxc/ApkUpdate/blob/master/app/release/app-release.apk?raw=true)
 *   versionCode 2
 *   versionName "2.0"
 *
 *   本地:
 *    versionCode 1
 *    versionName "1.0"
 *
 *    测试升级apk
 */
public class MainActivity extends AppCompatActivity {

    private Button updateBtn;
    private TextView tv_versionCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateBtn = (Button) this.findViewById(R.id.updateBtn);
        tv_versionCode = this.findViewById(R.id.tv_versionCode);

        int versionCode = AppUtils.getAppVersionCode();
        Log.e("test", "versionCode: " + versionCode);
        tv_versionCode.setText("版本号: " + versionCode);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVersion();

            }
        });
        checkVersion();
    }

    /**
     * 检查是否有新版本
     */
    private void checkVersion() {

        // getAppVersionName                 : 获取 App 版本号
        //getAppVersionCode                 : 获取 App 版本码
        int versionCode = AppUtils.getAppVersionCode();
        Log.e("test", "versionCode: " + versionCode);
        String versionName = AppUtils.getAppVersionName();
        Log.e("test", "versionName: " + versionName);

        if (versionCode < 2){  // 版本2是git上面的apk的版本号
            showUpdateDialog();
        }else {
            ToastUtils.showShort("您已经更新到最新版本啦！");
        }

    }

    private void showUpdateDialog() {
        final CommonDialog dialog = new CommonDialog(this);
        dialog.setTitle("ApkUpdate");
        dialog.setContent("发现新版本，请及时更新");
        dialog.setLeftBtnText("立即更新");
        dialog.setRightBtnText("稍后再说");
        dialog.setOnYesClickListener(new CommonDialog.OnYesClickListener() {
            @Override
            public void yesClick() {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, UpdateService.class);
//                intent.putExtra("apkUrl", "http://121.42.53.175:8080/hello_project/resources/upload/TianQiBao201605231.apk");
                intent.putExtra("apkUrl", "https://github.com/jinhuizxc/ApkUpdate/blob/master/app/release/app-release.apk?raw=true");
                startService(intent);
            }
        });

        dialog.setOnNoClickListener(new CommonDialog.OnNoClickListener() {
            @Override
            public void noClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
