package com.vapp.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.vapp.android.webView.VWebView;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Button inputButton = null;
    private Button defaultButton = null;
    private Button prevButton = null;

    private Context mContext = this;

    private static String prevUrlKey = "prevUrl";
    private static String defaultUrl = "https://www.zhihu.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputButton = findViewById(R.id.inputButton);
        defaultButton = findViewById(R.id.defaultButton);
        prevButton = findViewById(R.id.prevButton);

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
                    Toast.makeText(mContext, "未输入过网址！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showInputDialog() {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("输入网址")
                .setPlaceholder("在此输入您要跳转的网址")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        if (isUrl(text.toString())) {
                            // 如果是URL 需要将这个保存下来并跳转

                            saveUrl(text.toString());
                            Log.d("MainActivity", text.toString());
                            jumpToWebView(mContext, text.toString());
                        } else {
                            // 提示不是
                            Toast.makeText(mContext, "请输入正确的地址！", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void saveUrl(String url) {
        // 步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= getSharedPreferences("data",Context.MODE_PRIVATE);
        // 步骤2： 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 步骤3：将获取过来的值放入文件
        editor.putString(prevUrlKey, url);
        // 提交
        editor.commit();
    }

    private String getPrveUrl() {
        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= getSharedPreferences("data",Context.MODE_PRIVATE);
        //步骤2： 实例化SharedPreferences.Editor对象
        String url = sharedPreferences.getString(prevUrlKey, "");
        return url;
    }

    private void jumpToWebView(Context context, String url) {
        Intent starter = new Intent(context, VWebView.class);
        starter.putExtra("loadUrl", url);
        startActivity(starter);
    }

    private static String pattern = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$";

    /**
     * 判断 url 是否合法
     */
    public static boolean isUrl(String url) {
        Pattern httpPattern = Pattern.compile(pattern);
        if (httpPattern.matcher(url).matches()) {
            return true;
        }
        return false;
    }

}
