package com.edam.gensignature;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LeeJiEun";
    private PopupWindow statementWindow;
    private View contentView;
    private View mainView;
    private boolean isFirstLaunch = false;
    private boolean isNotShowStatementWindow = false;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String packageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isFirstLaunch = true;

        initStatusBar();

        initView();


        sp = getPreferences(Context.MODE_PRIVATE);
        editor = sp.edit();


    }

    private void initView() {
        TextView textView = findViewById(R.id.text_show_md5);
        Button buttonCopy = findViewById(R.id.bth_copy_md5);
        TextInputEditText packageEditText = findViewById(R.id.packagename_edit);
        final String[] md5String = {""};
        packageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, editable.toString());
                packageName = editable.toString();
            }
        });

        findViewById(R.id.btn_get_md5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SignatureTool signatureTool = new SignatureTool();
                if (packageName == null | packageName == "") {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.pkgname_isempty), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Signature[] signatures = signatureTool.getSignatures(getApplicationContext(), packageName);
                    String signValidString = signatureTool.getSignatureString(signatures[0].toByteArray());
                    md5String[0] = signValidString;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("MD5??? " + signValidString);
                    buttonCopy.setVisibility(View.VISIBLE);
                    Log.d(TAG, signValidString);
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    textView.setVisibility(View.VISIBLE);

                    textView.setText(getResources().getString(R.string.get_signature_exception));

                    buttonCopy.setVisibility(View.GONE);
                }
            }
        });


        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                md5String[0]
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, md5String[0]);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.paste), Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });


    }

    private void initStatusBar() {
        //?????????????????????
        //???FitsSystemWindows?????? true ?????????????????????????????????????????????????????? padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);
        //?????????????????????
        StatusBarUtil.setTranslucentStatus(this);
        //?????????????????????????????????????????????????????????, ???????????????????????????????????????, ?????????????????????????????????
        //??????????????????????????????,?????????????????????, ??????????????????????????????????????????, ???????????????????????????????????????if??????
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //????????????????????????????????? ???????????????????????????????????????????????????, ?????????????????????????????????????????????,
            //???????????????+???=???, ??????????????????????????????
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        boolean b = sp.getBoolean("isNotShowStatement", false);
        if (!b) {
            if (hasFocus) {
                if (isFirstLaunch) {
                    initStatementWindow();
                    isFirstLaunch = false;
                }
            }
        }

    }

    private void initStatementWindow() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        if (statementWindow == null) {
            contentView = mLayoutInflater.inflate(R.layout.statementwindow, null);
            mainView = mLayoutInflater.inflate(R.layout.activity_main, null);
            statementWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ColorDrawable cd = new ColorDrawable(0x000000);
        statementWindow.setBackgroundDrawable(cd);
        //????????????????????????
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        getWindow().setAttributes(lp);

        statementWindow.setOutsideTouchable(false);
        statementWindow.setFocusable(false);
        statementWindow.showAtLocation(mainView, Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);

        statementWindow.update();
        statementWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //???dismiss??????????????????
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        contentView.findViewById(R.id.state_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statementWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.state_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        AppCompatCheckBox compatCheckBox = (AppCompatCheckBox) contentView.findViewById(R.id.cb_ignore);
        compatCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isNotShow) {
                Log.d(TAG, String.valueOf(isNotShow));
                isNotShowStatementWindow = isNotShow;
                editor.putBoolean("isNotShowStatement", isNotShowStatementWindow)
                        .commit();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ??????????????????
        if (statementWindow != null) {
            statementWindow.dismiss();
        }
    }
}