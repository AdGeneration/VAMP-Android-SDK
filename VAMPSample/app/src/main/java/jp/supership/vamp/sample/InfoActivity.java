package jp.supership.vamp.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import jp.supership.vamp.VAMP;

public class InfoActivity extends AppCompatActivity {

    private static final String TAG = "VAMPSAMPLE";

    private AdInfoTask mAdInfoTask;
    private TextView mInfoTextView;
    private String mInfo;

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
        getGAID();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 端末情報表示
     */
    @SuppressWarnings({ "MissingPermission" })
    private void initInfo() {
        StringBuffer info = new StringBuffer();
        addKeyValue(info, "サポート対象OS", String.valueOf(VAMP.isSupported()));

        // id
        addValue(info, "--------------------");
        addKeyValue(info, "AD_ID", VAMPAd1Activity.VAMP_AD_ID);
        addValue(info, "--------------------");
        addKeyValue(info, "SDK_Ver(VAMP)", VAMP.SDKVersion());
        addKeyValue(info, "SDK_Ver(Admob)",
                getAdnwVersion("jp.supership.vamp.mediation.admob.AdMobAdapter"));
        addKeyValue(info, "SDK_Ver(FAN)",
                getAdnwVersion("jp.supership.vamp.mediation.fan.FANAdapter"));
        addKeyValue(info, "SDK_Ver(ironSource)",
                getAdnwVersion("jp.supership.vamp.mediation.ironsource.IronSourceAdapter"));
        addKeyValue(info, "SDK_Ver(LINEAds)",
                getAdnwVersion("jp.supership.vamp.mediation.lineads.LINEAdsAdapter"));
        addKeyValue(info, "SDK_Ver(maio)",
                getAdnwVersion("jp.supership.vamp.mediation.maio.MaioAdapter"));
        addKeyValue(info, "SDK_Ver(nend)",
                getAdnwVersion("jp.supership.vamp.mediation.nend.NendAdapter"));
        addKeyValue(info, "SDK_Ver(Pangle)",
                getAdnwVersion("jp.supership.vamp.mediation.pangle.PangleAdapter"));
        addKeyValue(info, "SDK_Ver(Tapjoy)",
                getAdnwVersion("jp.supership.vamp.mediation.tapjoy.TapjoyAdapter"));
        addKeyValue(info, "SDK_Ver(UnityAds)",
                getAdnwVersion("jp.supership.vamp.mediation.unityads.UnityAdsAdapter"));
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
        DisplayMetrics matrics = res.getDisplayMetrics();
        addKeyValue(info, "locale", res.getConfiguration().locale.toString());
        addKeyValue(info, "density", String.valueOf(matrics.density));
        Integer width = null;
        Integer height = null;
        Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point point = new Point();
            display.getRealSize(point);
            width = point.x;
            height = point.y;
        } else {
            Method getRawWidth = null;
            try {
                getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                width = (Integer) getRawWidth.invoke(display);
                height = (Integer) getRawHeight.invoke(display);
            } catch (Exception ignored) {
            }
        }
        if (width == null || height == null) {
            width = matrics.widthPixels;
            height = matrics.heightPixels;
        }
        addKeyValue(info, "dimensions.x", width.toString());
        addKeyValue(info, "dimensions.y", height.toString());
        addKeyValue(info, "widthDips",
                String.valueOf((int) ((matrics.widthPixels / matrics.density) + 0.5f)));
        addKeyValue(info, "heightDips",
                String.valueOf((int) ((matrics.heightPixels / matrics.density) + 0.5f)));
        addValue(info, "--------------------");

        // ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo n_info = cm.getActiveNetworkInfo();
        if (n_info != null) {
            addKeyValue(info, n_info.getTypeName() + "[" + n_info
                    .getState()
                    .name() + "]", n_info.isConnectedOrConnecting() ? "接続あり" : "接続なし");
        } else {
            addKeyValue(info, "connected", "NetworkInfo取得なし");
        }

        // Settings
        boolean is_airplane_mode = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            is_airplane_mode = Settings.System.getInt(getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            is_airplane_mode = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
        addKeyValue(info, "airplane_mode", String.valueOf(is_airplane_mode));

        // WifiManager（※ACCESS_WIFI_STATEのpermissionが必要）
        @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(
                WIFI_SERVICE);
        WifiInfo w_info = wm.getConnectionInfo();
        addKeyValue(info, "Wifi SSID", w_info.getSSID());
        int ip = w_info.getIpAddress();
        addKeyValue(info, "Wifi IP Adrress",
                String.format(Locale.US, "%02d.%02d.%02d.%02d", (ip >> 0) & 0xff, (ip >> 8) & 0xff,
                        (ip >> 16) & 0xff, (ip >> 24) & 0xff));
        addKeyValue(info, "Wifi MacAddress", w_info.getMacAddress());
        int rssi = w_info.getRssi();
        addKeyValue(info, "Wifi rssi", String.valueOf(rssi));
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        addKeyValue(info, "Wifi level", String.valueOf(level) + "/4");

        mInfo = info.toString();
        mInfoTextView.setText(mInfo);
    }

    /**
     * 指定adnwのSDKバージョン取得
     *
     * @param className アドネットワークアダプタのクラス名
     * @return the version of adnw
     */
    private String getAdnwVersion(String className) {
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
     * google advertising id取得＆表示
     */
    private void getGAID() {
        if (mAdInfoTask == null) {
            mAdInfoTask = new AdInfoTask(this, new AdInfoListener() {

                @Override
                public void AdInfoReady(String advertisingId, boolean limitAdTrackingEnabled) {
                    StringBuffer info = new StringBuffer();
                    addValue(info, mInfo);
                    addValue(info, "--------------------");
                    addKeyValue(info, "GAID", advertisingId);
                    addKeyValue(info, "isLimitAdTrackingEnabled",
                            String.valueOf(limitAdTrackingEnabled));
                    mInfoTextView.setText(info.toString());
                }
            });
            mAdInfoTask.execute();
        }
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

    interface AdInfoListener {

        void AdInfoReady(String advertisingId, boolean limitAdTrackingEnabled);
    }

    private class AdInfoTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private AdInfoListener mAdInfoListener;
        private AdvertisingIdClient.Info adInfo;

        AdInfoTask(Context context, AdInfoListener listener) {
            mContext = context;
            mAdInfoListener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                return true;
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.d(TAG, "GooglePlayServicesNotAvailableException e=" + e.getMessage());
            } catch (GooglePlayServicesRepairableException e) {
                Log.d(TAG, "GooglePlayServicesRepairableException e=" + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "IOException e=" + e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && adInfo != null) {
                mAdInfoListener.AdInfoReady(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
            }
        }
    }
}
