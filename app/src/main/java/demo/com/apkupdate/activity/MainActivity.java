package demo.com.apkupdate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import demo.com.apkupdate.R;
import demo.com.apkupdate.update.UpdateService;
import demo.com.apkupdate.view.CommonDialog;

public class MainActivity extends AppCompatActivity {

    private Button updateBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateBtn = (Button) this.findViewById(R.id.updateBtn);
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
                intent.putExtra("apkUrl", "4.4.2");
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
