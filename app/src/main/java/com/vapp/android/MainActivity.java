package com.vapp.android;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.quick.core.baseapp.baseactivity.FrmBaseActivity;
import com.quick.jsbridge.bean.QuickBean;
import com.quick.jsbridge.view.QuickWebLoader;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends FrmBaseActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;

    private Button inputButton = null;
    private Button defaultButton = null;
    private Button prevButton = null;
    private Button scanButton = null;
    private Button callTest = null;
    private Button goToMessageButton = null;
    private Button selectImageButton = null;
    private ImageView showImage = null;

    private Context mContext = this;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static String prevUrlKey = "prevUrl";
    private static String defaultUrl = "https://www.zhihu.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nomalInit("https://student.traefik.99rongle.com/mobile/pages/client/login?version=1.0.0");
    }

    private void nomalInit(String url) {
        Intent mintent = new Intent(MainActivity.this, QuickWebLoader.class);

        QuickBean bean = new QuickBean(url);
        bean.pageStyle = -1;
        mintent.putExtra("bean", bean);
        mintent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);

        startActivity(mintent);
        this.finish();
    }

    private void compareUrl(String newUrl) {
        SharedPreferences sharedPreferences= getSharedPreferences("data", Context .MODE_PRIVATE);
        String oldUrl = sharedPreferences.getString("url","https://m.mspace.com.sg/mobile/");

        if (!newUrl.equals(oldUrl)) {
            nomalInit(newUrl);

            //??????1???????????????SharedPreferences??????
            SharedPreferences share = getSharedPreferences("data", Context.MODE_PRIVATE);
            //??????2??? ?????????SharedPreferences.Editor??????
            SharedPreferences.Editor editor = share.edit();
            //??????3????????????????????????????????????
            editor.putString("baseReqUrl", newUrl);
            //??????4?????????
            editor.commit();
        }
    }

    private void testInit() {
        setContentView(R.layout.activity_main);

        requestCodeQRCodePermissions();

        pageControl.getNbBar().hide();

        inputButton = findViewById(R.id.inputButton);
        defaultButton = findViewById(R.id.defaultButton);
        prevButton = findViewById(R.id.prevButton);
        scanButton = findViewById(R.id.scan_button);
        callTest = findViewById(R.id.goToCall);
        goToMessageButton = findViewById(R.id.goToMessage);
        selectImageButton = findViewById(R.id.selectImage);
        showImage = findViewById(R.id.showImage);

        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToWebView(mContext, defaultUrl);
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getPrveUrl();
                if (!url.isEmpty()) {
                    jumpToWebView(mContext, url);
                } else {
                    Toast.makeText(mContext, "?????????????????????",Toast.LENGTH_SHORT).show();
                }
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mintent = new Intent(MainActivity.this, QuickWebLoader.class);
                QuickBean bean = new QuickBean("https://www.baidu.com");
                mintent.putExtra("bean", bean);
                startActivity(mintent);
            }
        });
    }


    private void showInputDialog() {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("????????????")
                .setPlaceholder("?????????????????????????????????")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("??????", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("??????", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        if (isUrl(text.toString())) {
                            // ?????????URL ????????????????????????????????????

                            saveUrl(text.toString());
                            Log.d("MainActivity", text.toString());
                            jumpToWebView(mContext, text.toString());
                        } else {
                            // ????????????
                            Toast.makeText(mContext, "???????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void saveUrl(String url) {
        // ??????1???????????????SharedPreferences??????
        SharedPreferences sharedPreferences= getSharedPreferences("data",Context.MODE_PRIVATE);
        // ??????2??? ?????????SharedPreferences.Editor??????
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // ??????3????????????????????????????????????
        editor.putString(prevUrlKey, url);
        // ??????
        editor.commit();
    }

    private String getPrveUrl() {
        //??????1???????????????SharedPreferences??????
        SharedPreferences sharedPreferences= getSharedPreferences("data",Context.MODE_PRIVATE);
        //??????2??? ?????????SharedPreferences.Editor??????
        String url = sharedPreferences.getString(prevUrlKey, "");
        return url;
    }

    private void jumpToWebView(Context context, String url) {
        Intent mintent = new Intent(MainActivity.this, QuickWebLoader.class);
        QuickBean bean = new QuickBean(url);
        mintent.putExtra("bean", bean);
        startActivity(mintent);
    }

    private static String pattern = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$";

    /**
     * ?????? url ????????????
     */
    public static boolean isUrl(String url) {
        Pattern httpPattern = Pattern.compile(pattern);
        if (httpPattern.matcher(url).matches()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String [] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "??????????????????????????????????????????????????????", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    private void requestBaseUrl() {

        new Thread(new Runnable(){
            @Override
            public void run() {
                // Do network action in this function
                String url = "https://jiance.99rongle.com/prod-api/mate-component/config/get-h5-url";
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .readTimeout(60, TimeUnit.SECONDS) // ????????????????????????
                        .writeTimeout(60, TimeUnit.SECONDS) // ????????????????????????
                        .connectTimeout(60, TimeUnit.SECONDS) // ????????????????????????
                        .build();
                RequestBody body = RequestBody.create("", JSON);
                Request request = new Request.Builder().url(url).post(body).build();
                Log.i("test", "requestBaseUrl");
                try (Response response= client.newCall(request).execute()) {
                    JSONObject obj = new JSONObject(response.body().string());
                    String st = obj.optString("data");
                    Log.e("download url", st);
                    compareUrl(st);
                } catch (Exception e) {
                    Log.i("test", "test");
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
