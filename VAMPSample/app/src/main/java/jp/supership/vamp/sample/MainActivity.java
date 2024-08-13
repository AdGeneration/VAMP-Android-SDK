package jp.supership.vamp.sample;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import jp.supership.vamp.VAMP;
import jp.supership.vamp.VAMPDebugUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        // テストモード設定（収益が発生しないテスト広告を表示する設定）
        // リリースする際は必ずコメントアウトしてください
        VAMP.setTestMode(true);

        // デバッグモード設定（デバッグモードで実行する）
        // リリースする際は必ずコメントアウトしてください
        VAMP.setDebugMode(true);

        Button ad1Button = findViewById(R.id.button_vamp_ad1);
        ad1Button.setOnClickListener(
                v -> startActivity(new Intent(MainActivity.this, VAMPAd1Activity.class)));

        Button ad2Button = findViewById(R.id.button_vamp_ad2);
        ad2Button.setOnClickListener(
                v -> startActivity(new Intent(MainActivity.this, VAMPAd2Activity.class)));

        Button infoButton = findViewById(R.id.button_info);
        infoButton.setOnClickListener(
                v -> startActivity(new Intent(MainActivity.this, InfoActivity.class)));

        // APP & VAMP SDK version
        StringBuilder builder = new StringBuilder();
        PackageManager pm = getPackageManager();
        try {
            PackageInfo p_info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            builder.append("APP v");
            builder.append(p_info.versionName);
            builder.append(" / ");
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        builder.append("SDK ");
        builder.append(VAMP.SDKVersion());
        final String version = builder.toString();

        final TextView sdkVerTextView = findViewById(R.id.sdk_version);
        sdkVerTextView.setText(version);

        EditText textAd1 = findViewById(R.id.text_ad1);
        textAd1.setText(VAMPAd1Activity.VAMP_AD_ID);
        textAd1.addTextChangedListener(
                new EditTextWatcher(text -> VAMPAd1Activity.VAMP_AD_ID = text));

        EditText textAd2 = findViewById(R.id.text_ad2);
        textAd2.setText(VAMPAd2Activity.VAMP_AD_ID);
        textAd2.addTextChangedListener(
                new EditTextWatcher(text -> VAMPAd2Activity.VAMP_AD_ID = text));

        CheckBox testModeCheckBox = findViewById(R.id.checkbox_test_mode);
        testModeCheckBox.setChecked(VAMP.isTestMode());
        testModeCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> VAMP.setTestMode(isChecked));

        CheckBox debugModeCheckBox = findViewById(R.id.checkbox_debug_mode);
        debugModeCheckBox.setChecked(VAMP.isDebugMode());
        debugModeCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> VAMP.setDebugMode(isChecked));

        // 2桁の国コードを取得して、広告枠IDを切り替える
        VAMP.getLocation(
                location -> {
                    StringBuilder sb = new StringBuilder(version);
                    sb.append(" / ");
                    sb.append(location.getCountryCode());

                    if (!TextUtils.isEmpty(location.getRegion())) {
                        sb.append("-").append(location.getRegion());
                    }

                    sdkVerTextView.setText(sb.toString());

                    //                if (location.getCountryCode().equals("US")) {
                    //                    // COPPA対象ユーザである場合はtrueを設定する
                    //
                    // VAMPPrivacySettings.setChildDirected(VAMPPrivacySettings.ChildDirected.TRUE);
                    //                }
                });

        //        VAMP.isEUAccess(this, new VAMPPrivacySettings.UserConsentListener() {
        //
        //            @Override
        //            public void onRequired(boolean isRequired) {
        //                if (!isRequired) {
        //                    // Nothing to do
        //                    return;
        //                }
        //
        //                new AlertDialog.Builder(MainActivity.this)
        //                    .setTitle("Personalized Ads")
        //                    .setMessage("Accept?")
        //                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
        //
        //                        @Override
        //                        public void onClick(DialogInterface dialog, int which) {
        //
        // VAMPPrivacySettings.setConsentStatus(VAMPPrivacySettings.ConsentStatus.ACCEPTED);
        //                        }
        //                    })
        //                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
        //
        //                        @Override
        //                        public void onClick(DialogInterface dialog, int which) {
        //
        // VAMPPrivacySettings.setConsentStatus(VAMPPrivacySettings.ConsentStatus.DENIED);
        //                        }
        //                    })
        //                    .create()
        //                    .show();
        //            }
        //        });

        VAMPDebugUtils.logSDKDetails(this);
    }
}
