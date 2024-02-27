package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import jp.supership.vamp.VAMPError;
import jp.supership.vamp.VAMPEventDispatcher;
import jp.supership.vamp.VAMPRequest;
import jp.supership.vamp.VAMPRewardedAd;
import jp.supership.vamp.VAMPRewardedAdListener;
import jp.supership.vamp.VAMPRewardedAdLoadListener;

public class VAMPAd2Activity extends BaseActivity {

    /** 広告枠IDを設定してください 59756 : Androidテスト用ID (このIDのままリリースしないでください) */
    public static String VAMP_AD_ID = "59756";

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_ad2);
        setTitle(R.string.vamp_ad2);

        // 広告をプリロードします。
        // 広告を取得するのに時間がかかるため、
        // 事前に広告をダウンロードし、ユーザに待ち時間無く広告を表示することができます。
        // ※VAMPRewardedAdLoadListenerリスナーはnullでも構いません。
        VAMPRewardedAd.load(this, VAMP_AD_ID, new VAMPRequest.Builder().build(), null);

        addLog("Preloading...");

        Button loadButton = (Button) findViewById(R.id.button_load);
        loadButton.setText(R.string.load_show);
        loadButton.setOnClickListener(
                v -> {
                    VAMPRewardedAd rewardedAd = VAMPRewardedAd.of(VAMP_AD_ID);

                    // 広告の表示準備ができているか確認します。
                    // rewardedAdオブジェクトがnullでなければ、表示の準備ができています。
                    if (rewardedAd != null) {
                        // 準備が完了していた場合、動画広告を再生します。
                        rewardedAd.show(VAMPAd2Activity.this);

                        addLog("[LOAD & SHOW] show()");
                    } else {
                        // 広告の取得を開始します。
                        VAMPRewardedAd.load(
                                VAMPAd2Activity.this,
                                VAMP_AD_ID,
                                new VAMPRequest.Builder().build(),
                                new VAMPRewardedAdLoadListener() {
                                    @Override
                                    public void onReceived(@NonNull final String placementId) {
                                        // 広告表示が可能になると通知されます。
                                        addLog(String.format("onReceived(%s)", placementId));

                                        // 広告を取得できたため、動画広告を再生します。
                                        VAMPRewardedAd rewardedAd = VAMPRewardedAd.of(placementId);
                                        if (rewardedAd != null) {
                                            rewardedAd.show(VAMPAd2Activity.this);
                                        }
                                    }

                                    @Override
                                    public void onFailedToLoad(
                                            @NonNull final String placementId,
                                            @NonNull final VAMPError error) {
                                        addLog(
                                                String.format(
                                                        "onFailedToLoad(%s, %s)",
                                                        placementId, error),
                                                Color.RED);
                                    }
                                });

                        addLog("[LOAD & SHOW] load()");
                    }
                });

        TextView idTextView = findViewById(R.id.vamp_id);
        idTextView.setText(getString(R.string.vamp_ad_id, VAMP_AD_ID));

        mLogView = findViewById(R.id.logs);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // VAMPイベントを受け取るためにVAMPRewardedAdListenerリスナーを登録します。
        VAMPEventDispatcher.getInstance().addListener(VAMP_AD_ID, rewardedAdListener);

        if (getIntent() != null) {
            // VAMPEventDispatcherは、VAMPRewardedAdListenerリスナーが登録されていない場合、
            // showメソッド呼び出しから広告が閉じられるまでに発生したVAMPイベントを
            // ディスパッチャー内に蓄積します。
            //
            // flushメソッドは、指定した広告枠IDに関するすべての蓄積された
            // VAMPイベントをリスナーに通知します。
            // リスナーに通知後、VAMPイベントは破棄されます。
            // 蓄積されているVAMPイベントがない場合は何もしません。
            //
            // この実装例では、onDestroyからonCreateまでの間に発生したVAMPイベントを
            // ディスパッチャー内に蓄積しておき、Activity再生成時にリスナーに通知しています。
            // なお、Activityに特化したVAMPActivityEventDispatcherを代わりに使うこともできます。
            VAMPEventDispatcher.getInstance().flush(VAMP_AD_ID);
        }

        addLog("onCreate(" + this + ")");
    }

    @Override
    protected void onDestroy() {
        // VAMPRewardedAdListenerリスナーを解除します。
        VAMPEventDispatcher.getInstance().removeListener(VAMP_AD_ID);

        addLog("onDestroy(" + this + ")");

        super.onDestroy();
    }

    private final VAMPRewardedAdListener rewardedAdListener =
            new VAMPRewardedAdListener() {
                @Override
                public void onFailedToShow(
                        @NonNull final String placementId, @NonNull final VAMPError error) {
                    addLog(
                            String.format("onFailedToShow(%s, %s)", placementId, error),
                            EventType.FAILED);
                }

                @Override
                public void onCompleted(@NonNull final String placementId) {
                    addLog(String.format("onCompleted(%s)", placementId), EventType.COMPLETED);
                }

                @Override
                public void onOpened(@NonNull final String placementId) {
                    addLog(String.format("onOpened(%s)", placementId), EventType.OPENED);
                }

                @Override
                public void onClosed(@NonNull final String placementId, final boolean adClicked) {
                    addLog(
                            String.format("onClosed(%s, %s)", placementId, adClicked),
                            EventType.CLOSED);

                    // 必要に応じて次に表示する広告をプリロード
                    VAMPRewardedAd.load(
                            VAMPAd2Activity.this,
                            VAMP_AD_ID,
                            new VAMPRequest.Builder().build(),
                            null);
                }

                @Override
                public void onExpired(@NonNull final String placementId) {
                    addLog(String.format("onExpired(%s)", placementId), EventType.EXPIRED);
                }
            };
}
