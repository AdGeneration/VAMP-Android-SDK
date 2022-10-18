package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

    /**
     * 広告枠IDを設定してください
     * 59756 : Androidテスト用ID (このIDのままリリースしないでください)
     */
    public static final String VAMP_AD_ID = "59756";

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_ad2);
        setTitle(R.string.vamp_ad2);

        // 広告をプリロードします。
        // 広告を取得するのに時間がかかるため、
        // 事前に広告をダウンロードし、ユーザに待ち時間無く広告を表示することができます。
        // ※VAMPRewardedAdLoadListenerリスナーはnullでも構いません。
        VAMPRewardedAd.load(this,
                VAMP_AD_ID,
                new VAMPRequest.Builder().build(),
                null);

        addLog("load() start");

        Button loadButton = (Button) findViewById(R.id.button_load);
        loadButton.setText(R.string.load_show);
        loadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                VAMPRewardedAd rewardedAd = VAMPRewardedAd.of(VAMP_AD_ID);

                // 広告の表示準備ができているか確認します。
                // rewardedAdオブジェクトがnullでなければ、表示の準備ができています。
                if (rewardedAd != null) {
                    // 準備が完了していた場合、動画広告を再生します。
                    rewardedAd.show(VAMPAd2Activity.this);

                    addLog("[LOAD & SHOW] loaded → show()");
                } else {
                    // 広告の取得を開始します。
                    VAMPRewardedAd.load(VAMPAd2Activity.this,
                            VAMP_AD_ID,
                            new VAMPRequest.Builder().build(),
                            new VAMPRewardedAdLoadListener() {
                                @Override
                                public void onReceived(@NonNull String placementId) {
                                    // 動画表示の準備が完了しました。
                                    addLog(String.format("onReceived(%s)", placementId));

                                    // 広告を取得できたため、動画広告を再生します。
                                    VAMPRewardedAd rewardedAd = VAMPRewardedAd.of(placementId);
                                    rewardedAd.show(VAMPAd2Activity.this);
                                }

                                @Override
                                public void onFailedToLoad(@NonNull String placementId,
                                                           VAMPError error) {
                                    // 広告取得失敗
                                    // 広告が取得できなかったときに通知されます。
                                    // 例）在庫が無い、タイムアウトなど
                                    addLog(String.format("onFailedToLoad(%s, %s)",
                                            placementId, error), Color.RED);

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
                                }
                            });

                    addLog("[LOAD & SHOW] load()");
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
        VAMPEventDispatcher.getInstance()
                .addListener(VAMP_AD_ID, rewardedAdListener);

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

    private final VAMPRewardedAdListener rewardedAdListener = new VAMPRewardedAdListener() {
        @Override
        public void onFailedToShow(@NonNull String placementId, VAMPError error) {
            // 広告表示失敗
            // showを実行したが、何らかの理由で広告表示が失敗したときに通知されます。
            addLog(String.format("onFailedToShow(%s, %s)",
                    placementId, error), Color.RED);

            // エラーにはユーザキャンセルも含まれます。
            if (error == VAMPError.USER_CANCEL) {
                // ユーザが広告再生を途中でキャンセルしました。
            }
        }

        @Override
        public void onCompleted(@NonNull String placementId) {
            // インセンティブ付与が可能になったタイミングで通知されます。
            // ※アドネットワークによって通知タイミングが異なります。
            // （動画再生完了時、またはエンドカードを閉じたタイミング）
            addLog(String.format("onCompleted(%s)", placementId), Color.BLUE);
        }

        @Override
        public void onOpened(@NonNull String placementId) {
            // 動画が表示されたタイミングで通知されます。
            // ※アドネットワークによって通知タイミングが異なります。
            // (動画再生直前、または動画再生時)
            addLog(String.format("onOpened(%s)", placementId), Color.BLACK);
        }

        @Override
        public void onClosed(@NonNull String placementId, boolean adClicked) {
            // 動画プレイヤーやエンドカードが閉じられたタイミングで通知されます。
            // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompletedで判定してください＞
            addLog(String.format("onClosed(%s, %s)",
                    placementId, adClicked), Color.BLACK);
        }

        @Override
        public void onExpired(@NonNull String placementId) {
            // 有効期限オーバーのときに通知されます。
            // ＜注意：onReceivedを受けてからの有効期限が切れました。showするには再度loadを行う必要が有ります＞
            addLog(String.format("onExpired(%s)", placementId), Color.RED);
        }
    };
}
