package jp.supership.vamp.sample;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import jp.supership.vamp.VAMP;
import jp.supership.vamp.VAMPPrivacySettings;

import java.lang.reflect.Method;

public class InfoActivity extends AppCompatActivity {

    private static final String TAG = "VAMPSAMPLE";
    private TextView mInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.info);

        mInfoTextView = findViewById(R.id.infos);

        initInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** 端末情報表示 */
    @SuppressWarnings({"MissingPermission"})
    private void initInfo() {
        StringBuffer info = new StringBuffer();
        addKeyValue(info, "サポート対象OS", String.valueOf(VAMP.isSupported()));

        addValue(info, "--------------------");
        addKeyValue(info, "SDK_Ver(VAMP)", VAMP.SDKVersion());

        addKeyValue(info, "SDK_Ver(Admob)", getAdnwVersion("AdMob"));
        addKeyValue(info, "SDK_Ver(FAN)", getAdnwVersion("FAN"));
        addKeyValue(info, "SDK_Ver(ironSource)", getAdnwVersion("IronSource"));
        addKeyValue(info, "SDK_Ver(maio)", getAdnwVersion("Maio"));
        addKeyValue(info, "SDK_Ver(nend)", getAdnwVersion("Nend"));
        addKeyValue(info, "SDK_Ver(Tapjoy)", getAdnwVersion("Tapjoy"));
        addKeyValue(info, "SDK_Ver(UnityAds)", getAdnwVersion("UnityAds"));
        addKeyValue(info, "SDK_Ver(LINEAds)", getAdnwVersion("LINEAds"));
        addKeyValue(info, "SDK_Ver(Pangle)", getAdnwVersion("Pangle"));
        addValue(info, "--------------------");

        addKeyValue(info, "Adapter_Ver(Admob)", getAdapterVersion("AdMob"));
        addKeyValue(info, "Adapter_Ver(FAN)", getAdapterVersion("FAN"));
        addKeyValue(info, "Adapter_Ver(ironSource)", getAdapterVersion("IronSource"));
        addKeyValue(info, "Adapter_Ver(maio)", getAdapterVersion("Maio"));
        addKeyValue(info, "Adapter_Ver(nend)", getAdapterVersion("Nend"));
        addKeyValue(info, "Adapter_Ver(Tapjoy)", getAdapterVersion("Tapjoy"));
        addKeyValue(info, "Adapter_Ver(UnityAds)", getAdapterVersion("UnityAds"));
        addKeyValue(info, "Adapter_Ver(LINEAds)", getAdapterVersion("LINEAds"));
        addKeyValue(info, "Adapter_Ver(Pangle)", getAdapterVersion("Pangle"));
        addValue(info, "--------------------");

        // PackageManager
        String package_name = getPackageName();
        PackageManager pm = getPackageManager();
        try {
            PackageInfo p_info = pm.getPackageInfo(package_name, PackageManager.GET_ACTIVITIES);
            addKeyValue(info, "アプリ名", (String) pm.getApplicationLabel(p_info.applicationInfo));
            addKeyValue(info, "パッケージ名", package_name);
            addKeyValue(info, "バージョンコード", String.valueOf(p_info.versionCode));
            addKeyValue(info, "バージョン名", p_info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "NameNotFoundException e=" + e.getMessage());
        }

        addValue(info, "--------------------");

        // Build
        addKeyValue(info, "Androidバージョン", Build.VERSION.RELEASE);
        addKeyValue(info, "API Level", String.valueOf(Build.VERSION.SDK_INT));
        addKeyValue(info, "メーカー名", Build.MANUFACTURER);
        addKeyValue(info, "モデル番号", Build.MODEL);
        addKeyValue(info, "ブランド名", Build.BRAND);

        addValue(info, "--------------------");

        // TelephonyManager
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        addKeyValue(info, "国コード", tm.getNetworkCountryIso());
        addKeyValue(info, "MCC+MNC", tm.getNetworkOperator());
        addKeyValue(info, "サービスプロバイダの名前", tm.getNetworkOperatorName());

        addValue(info, "--------------------");

        // Resources
        Resources res = getResources();
        addKeyValue(info, "locale", res.getConfiguration().locale.toString());

        addValue(info, "--------------------");

        // 設定値
        addKeyValue(info, "ChildDirected", VAMPPrivacySettings.getChildDirected().name());
        addKeyValue(
                info,
                "useMetaAudienceNetworkBidding",
                String.valueOf(VAMP.useMetaAudienceNetworkBidding()));
        addKeyValue(
                info,
                "isMetaAudienceNetworkBiddingTestMode",
                String.valueOf(VAMP.isMetaAudienceNetworkBiddingTestMode()));

        mInfoTextView.setText(info.toString());
    }

    /**
     * 指定adnwのSDKバージョン取得
     *
     * @param adnwName アドネットワーク名
     * @return the version of adnw
     */
    private String getAdnwVersion(String adnwName) {
        final String className =
                String.format(
                        "jp.supership.vamp.mediation.%s.%sAdapter",
                        adnwName.toLowerCase(), adnwName);

        try {
            Class<?> cls = Class.forName(className);
            Object adapter = cls.newInstance();
            Method method = cls.getMethod("getAdNetworkVersion");
            return (String) method.invoke(adapter);
        } catch (Exception ignored) {
            return "-";
        }
    }

    /**
     * 指定adnwのAdapterバージョン取得
     *
     * @param adnwName アドネットワーク名
     * @return the version of adapter
     */
    private String getAdapterVersion(String adnwName) {
        final String className =
                String.format("jp.supership.vamp.mediation.%s.BuildConfig", adnwName.toLowerCase());
        String version = "-";
        try {
            Class<?> cls = Class.forName(className);
            version = (String) cls.getField("VERSION_NAME").get(null);
        } catch (Exception ignored) {

        }
        return version;
    }

    private void addValue(StringBuffer buffer, String value) {
        if (buffer.length() > 0) {
            buffer.append("\n");
        }
        buffer.append(value);
    }

    private void addKeyValue(StringBuffer buffer, String key, String value) {
        if (buffer.length() > 0) {
            buffer.append("\n");
        }
        buffer.append(key);
        buffer.append("：");
        if (value != null && value.length() > 0) {
            buffer.append(value);
        } else {
            buffer.append("設定なし");
        }
    }
}
