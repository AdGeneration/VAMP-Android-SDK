package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 広告の取得を開始します。
                        VAMPRewardedAd.load(
                                VAMPAd1Activity.this,
                                VAMP_AD_ID,
                                new VAMPRequest.Builder().build(),
                                new VAMPRewardedAdLoadAdvancedListener() {
                                    @Override
                                    public void onStartedLoading(
                                            @NonNull String placementId,
                                            @NonNull String adNetworkName) {
                                        // アドネットワークごとの広告取得が開始されたときに通知されます。
                                        addLog(
                                                String.format(
                                                        "onStartedLoading(%s, %s)",
                                                        placementId, adNetworkName));
                                    }

                                    @Override
                                    public void onLoaded(
                                            @NonNull String placementId,
                                            @NonNull String adNetworkName,
                                            boolean success,
                                            @NonNull String message) {
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
                                    public void onReceived(@NonNull String placementId) {
                                        // 動画表示の準備が完了しました。
                                        addLog(String.format("onReceived(%s)", placementId));

                                        showButton.setEnabled(true);
                                    }

                                    @Override
                                    public void onFailedToLoad(
                                            @NonNull String placementId, VAMPError error) {
                                        // 広告取得失敗
                                        // 広告が取得できなかったときに通知されます。
                                        // 例）在庫が無い、タイムアウトなど
                                        addLog(
                                                String.format(
                                                        "onFailedToLoad(%s, %s)",
                                                        placementId, error),
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
                    }
                });

        showButton = findViewById(R.id.button_show);
        showButton.setEnabled(false);
        showButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
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
                public void onFailedToShow(@NonNull String placementId, VAMPError error) {
                    // 広告表示失敗
                    // showを実行したが、何らかの理由で広告表示が失敗したときに通知されます。
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
                public void onCompleted(@NonNull String placementId) {
                    // インセンティブ付与が可能になったタイミングで通知されます。
                    // ※アドネットワークによって通知タイミングが異なります。
                    // （動画再生完了時、またはエンドカードを閉じたタイミング）
                    addLog(String.format("onCompleted(%s)", placementId), EventType.COMPLETED);
                }

                @Override
                public void onOpened(@NonNull String placementId) {
                    // 動画が表示されたタイミングで通知されます。
                    // ※アドネットワークによって通知タイミングが異なります。
                    // (動画再生直前、または動画再生時)
                    addLog(String.format("onOpened(%s)", placementId), EventType.OPENED);
                }

                @Override
                public void onClosed(@NonNull String placementId, boolean adClicked) {
                    // 動画プレイヤーやエンドカードが閉じられたタイミングで通知されます。
                    // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompletedで判定してください＞
                    addLog(
                            String.format("onClosed(%s, %s)", placementId, adClicked),
                            EventType.CLOSED);
                }

                @Override
                public void onExpired(@NonNull String placementId) {
                    // 有効期限オーバーのときに通知されます。
                    // ＜注意：onReceivedを受けてからの有効期限が切れました。showするには再度loadを行う必要が有ります＞
                    addLog(String.format("onExpired(%s)", placementId), EventType.EXPIRED);

                    showButton.setEnabled(false);
                }
            };
}
