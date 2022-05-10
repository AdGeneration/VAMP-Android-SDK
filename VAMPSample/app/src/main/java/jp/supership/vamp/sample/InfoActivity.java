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
        addKeyValue(info, "AD_ID", VAMPAdActivity.VAMP_AD_ID);
        addValue(info, "--------------------");
        addKeyValue(info, "SDK_Ver(VAMP)", getVersion("VAMP"));
        addKeyValue(info, "SDK_Ver(Admob)", getVersion("Admob"));
        addKeyValue(info, "SDK_Ver(FAN)", getVersion("FAN"));
        addKeyValue(info, "SDK_Ver(maio)", getVersion("maio"));
        addKeyValue(info, "SDK_Ver(nend)", getVersion("nend"));
        addKeyValue(info, "SDK_Ver(Tapjoy)", getVersion("Tapjoy"));
        addKeyValue(info, "SDK_Ver(UnityAds)", getVersion("UnityAds"));
        addKeyValue(info, "SDK_Ver(LINEAds)", getVersion("LINEAds"));
        addKeyValue(info, "SDK_Ver(Pangle)", getVersion("Pangle"));
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
        addKeyValue(info, "widthDips", String.valueOf((int) ((matrics.widthPixels / matrics.density) + 0.5f)));
        addKeyValue(info, "heightDips", String.valueOf((int) ((matrics.heightPixels / matrics.density) + 0.5f)));
        addValue(info, "--------------------");

        // ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo n_info = cm.getActiveNetworkInfo();
        if (n_info != null) {
            addKeyValue(info, n_info.getTypeName() + "[" + n_info.getState().name() + "]", n_info.isConnectedOrConnecting() ? "接続あり" : "接続なし");
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
        @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo w_info = wm.getConnectionInfo();
        addKeyValue(info, "Wifi SSID", w_info.getSSID());
        int ip = w_info.getIpAddress();
        addKeyValue(info, "Wifi IP Adrress", String.format(Locale.US, "%02d.%02d.%02d.%02d", (ip >> 0) & 0xff, (ip >> 8) & 0xff, (ip >> 16) & 0xff, (ip >> 24) & 0xff));
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
     * @param adnw
     * @return
     */
    private String getVersion(String adnw) {
        String version = "nothing";
        switch (adnw) {
            case "VAMP":
                version = VAMP.SDKVersion();
                break;
            case "Admob":
                Resources res = getResources();
                int versionId = res.getIdentifier("google_play_services_version", "integer", getPackageName());
                if (versionId != 0) {
                    try {
                        version = String.valueOf(getResources().getInteger(versionId));
                    } catch (Exception ignored) {
                    }
                }
                break;
            case "FAN":
                try {
                    Class<?> cls = Class.forName("com.facebook.ads.BuildConfig");
                    version = (String) cls.getField("VERSION_NAME").get(null);
                } catch (Exception ignored) {
                }
                break;
            case "maio":
                try {
                    Class<?> cls = Class.forName("jp.maio.sdk.android.MaioAds");
                    Method getSdkVersion = cls.getMethod("getSdkVersion");
                    version = (String) getSdkVersion.invoke(null);
                } catch (Exception ignored) {
                }
                break;
            case "nend":
                try {
                    Class<?> cls = Class.forName("net.nend.android.BuildConfig");
                    version = (String) cls.getField("NEND_SDK_VERSION").get(null);
                } catch (Exception ignored) {
                }
                break;
            case "Tapjoy":
                try {
                    Class<?> cls = Class.forName("com.tapjoy.Tapjoy");
                    Method getVersion = cls.getMethod("getVersion");
                    version = (String) getVersion.invoke(null);
                } catch (Exception ignored) {
                }
                break;
            case "UnityAds":
                try {
                    Class<?> cls = Class.forName("com.unity3d.ads.UnityAds");
                    Method getVersion = cls.getMethod("getVersion");
                    version = (String) getVersion.invoke(null);
                } catch (Exception ignored) {
                }
                break;
            case "LINEAds":
                try {
                    Class<?> cls = Class.forName("com.five_corp.ad.FiveAd");
                    Method isInitialized = cls.getMethod("isInitialized");
                    if ((boolean) isInitialized.invoke(cls)) {
                        Method getSingleton = cls.getMethod("getSingleton");
                        Object fiveAd = getSingleton.invoke(cls);

                        Method getVersion = cls.getMethod("getVersion");
                        version = String.valueOf(getVersion.invoke(fiveAd));
                    }
                } catch (Exception ignored) {
                }
                break;
            case "Pangle":
                try {
                    Class<?> sdkCls = Class.forName("com.bytedance.sdk.openadsdk.TTAdSdk");
                    Method getAdManager = sdkCls.getMethod("getAdManager");
                    Object adManager = getAdManager.invoke(null);
                    Class<?> adManagerCls = Class.forName("com.bytedance.sdk.openadsdk.TTAdManager");
                    Method getSDKVersion = adManagerCls.getMethod("getSDKVersion");
                    version = (String) getSDKVersion.invoke(adManager);

                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return version;
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
                    addKeyValue(info, "isLimitAdTrackingEnabled", String.valueOf(limitAdTrackingEnabled));
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
