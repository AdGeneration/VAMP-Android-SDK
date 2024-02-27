package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import jp.supership.vamp.VAMPActivityEventDispatcher;
import jp.supership.vamp.VAMPError;
import jp.supership.vamp.VAMPRequest;
import jp.supership.vamp.VAMPRewardedAd;
import jp.supership.vamp.VAMPRewardedAdListener;
import jp.supership.vamp.VAMPRewardedAdLoadAdvancedListener;

public class VAMPAd1Activity extends BaseActivity {

    /** 広告枠IDを設定してください 59756 : Androidテスト用ID (このIDのままリリースしないでください) */
    public static String VAMP_AD_ID = "59756";

    private Button showButton;

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_ad);
        setTitle(R.string.vamp_ad1);

        Button loadButton = findViewById(R.id.button_load);
        loadButton.setOnClickListener(
                v -> {
                    // 広告の取得を開始します。
                    VAMPRewardedAd.load(
                            VAMPAd1Activity.this,
                            VAMP_AD_ID,
                            new VAMPRequest.Builder().build(),
                            new VAMPRewardedAdLoadAdvancedListener() {
                                @Override
                                public void onStartedLoading(
                                        @NonNull final String placementId,
                                        @NonNull final String adNetworkName) {
                                    // アドネットワークごとの広告取得が開始されたときに通知されます。
                                    addLog(
                                            String.format(
                                                    "onStartedLoading(%s, %s)",
                                                    placementId, adNetworkName));
                                }

                                @Override
                                public void onLoaded(
                                        @NonNull final String placementId,
                                        @NonNull final String adNetworkName,
                                        final boolean success,
                                        @NonNull final String message) {
                                    // アドネットワークを1つずつ呼び出した結果、
                                    // 広告在庫が取得できたかをsuccessフラグで確認できます。
                                    if (success) {
                                        addLog(
                                                String.format(
                                                        "onLoaded(%s, %s, true, %s)",
                                                        placementId, adNetworkName, message),
                                                Color.BLACK);
                                    } else {
                                        // 失敗しても、次のアドネットワークがあれば、広告取得を試みます。
                                        // 最終的に全てのアドネットワークの広告在庫が無ければ
                                        // onFailedToLoadのNO_ADSTOCKが通知されます。
                                        addLog(
                                                String.format(
                                                        "onLoaded(%s, %s, false, %s)",
                                                        placementId, adNetworkName, message),
                                                Color.RED);
                                    }
                                }

                                @Override
                                public void onReceived(@NonNull final String placementId) {
                                    // 広告表示が可能になると通知されます。
                                    addLog(String.format("onReceived(%s)", placementId));

                                    showButton.setEnabled(true);
                                }

                                @Override
                                public void onFailedToLoad(
                                        @NonNull final String placementId,
                                        @NonNull final VAMPError error) {
                                    // 広告の取得に失敗すると通知されます。
                                    // 例) 広告取得時のタイムアウトや、全てのアドネットワークの在庫がない場合など。
                                    addLog(
                                            String.format(
                                                    "onFailedToLoad(%s, %s)", placementId, error),
                                            Color.RED);

                                    if (error == VAMPError.NO_ADSTOCK) {
                                        // 在庫が無いので、再度loadをしてもらう必要があります。
                                        // 連続で発生する場合、時間を置いてからloadをする必要があります。
                                    } else if (error == VAMPError.NO_ADNETWORK) {
                                        // アドジェネ管理画面でアドネットワークの配信がONになっていない、
                                        // またはEU圏からのアクセスの場合(GDPR)発生します。
                                    } else if (error == VAMPError.NEED_CONNECTION) {
                                        // ネットワークに接続できない状況です。
                                        // 電波状況をご確認ください。
                                    } else if (error == VAMPError.MEDIATION_TIMEOUT) {
                                        // アドネットワークSDKから返答が得られず、タイムアウトしました。
                                    }

                                    showButton.setEnabled(false);
                                }
                            });

                    addLog("[LOAD] load()");
                });

        showButton = findViewById(R.id.button_show);
        showButton.setEnabled(false);
        showButton.setOnClickListener(
                v -> {
                    VAMPRewardedAd rewardedAd = VAMPRewardedAd.of(VAMP_AD_ID);

                    // 広告の表示準備ができているか確認します。
                    // rewardedAdオブジェクトがnullでなければ、表示の準備ができています。
                    if (rewardedAd != null) {
                        // 準備が完了していた場合、動画広告を再生します。
                        rewardedAd.show(VAMPAd1Activity.this);
                        addLog("[SHOW] show()");
                    } else {
                        addLog("[SHOW] Not loaded");
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
        VAMPActivityEventDispatcher.getInstance().onCreate(bundle, VAMP_AD_ID, rewardedAdListener);

        addLog("onCreate(" + this + ")");
    }

    @Override
    protected void onDestroy() {
        // VAMPRewardedAdListenerリスナーを解除します。
        VAMPActivityEventDispatcher.getInstance().onDestroy(VAMP_AD_ID);

        addLog("onDestroy(" + this + ")");

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // VAMPActivityEventDispatcher#onCreate、
        // VAMPActivityEventDispatcher#onDestroy、および
        // VAMPActivityEventDispatcher#onSaveInstanceStateメソッドをそれぞれ呼ぶことで、
        // 広告の表示中にActivityが破棄された後、再生成された場合でもVAMPイベントを受け取ることができます。
        VAMPActivityEventDispatcher.getInstance().onSaveInstanceState(outState, VAMP_AD_ID);

        super.onSaveInstanceState(outState);
    }

    private final VAMPRewardedAdListener rewardedAdListener =
            new VAMPRewardedAdListener() {
                @Override
                public void onFailedToShow(
                        @NonNull final String placementId, @NonNull final VAMPError error) {
                    // 広告の表示に失敗すると通知されます。
                    // 例) 視聴完了する前にユーザがキャンセルするなど。
                    addLog(
                            String.format("onFailedToShow(%s, %s)", placementId, error),
                            EventType.FAILED);

                    // エラーにはユーザキャンセルも含まれます。
                    if (error == VAMPError.USER_CANCEL) {
                        // ユーザが広告再生を途中でキャンセルしました。
                    }

                    showButton.setEnabled(false);
                }

                @Override
                public void onCompleted(@NonNull final String placementId) {
                    // インセンティブ付与が可能になったタイミングで通知されます。
                    // ※ユーザが途中で再生をスキップしたり、動画視聴をキャンセルすると発生しません。
                    // ※アドネットワークによって発生タイミングが異なります。
                    addLog(String.format("onCompleted(%s)", placementId), EventType.COMPLETED);
                }

                @Override
                public void onOpened(@NonNull final String placementId) {
                    // 広告の表示が開始されると通知されます。
                    addLog(String.format("onOpened(%s)", placementId), EventType.OPENED);
                }

                @Override
                public void onClosed(@NonNull final String placementId, final boolean adClicked) {
                    // 広告が閉じられると通知されます。
                    // ユーザキャンセルなどの場合も通知されるため、インセンティブ付与はVAMPRewardedAdListener#onCompletedで判定してください。
                    addLog(
                            String.format("onClosed(%s, %s)", placementId, adClicked),
                            EventType.CLOSED);
                }

                @Override
                public void onExpired(@NonNull final String placementId) {
                    // RTBはロードが完了してから1時間経過すると、広告表示ができても無効扱いとなり、収益が発生しません。
                    // この通知を受け取ったらロードからやり直してください。
                    addLog(String.format("onExpired(%s)", placementId), EventType.EXPIRED);

                    showButton.setEnabled(false);
                }
            };
}
