package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.supership.vamp.VAMPAdvancedListener;
import jp.supership.vamp.VAMPError;
import jp.supership.vamp.VAMPRequest;
import jp.supership.vamp.VAMPRewardedAd;

public class VAMPAd2Activity extends BaseActivity {

    /**
     * 広告枠IDを設定してください
     * 59756 : Androidテスト用ID (このIDのままリリースしないでください)
     */
    public static final String VAMP_AD_ID = "59756";

    private VAMPRewardedAd rewardedAd;

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_ad2);
        setTitle(R.string.vamp_ad2);

        // VAMPインスタンスの取得
        rewardedAd = new VAMPRewardedAd(this, VAMP_AD_ID);
        rewardedAd.setListener(new AdListener());

        // preload v3.0〜
        // 新規追加された広告を事前に取得するメソッド
        // 広告を取得するのに時間がかかるため（動画ファイル、プレイアブルのダウンロード）、
        // 事前に在庫を確保しておき、ユーザーに待ち時間無く広告を表示するための機能。
        // ※リスナー（onLoadResult、onFailedToLoadなど）は受け取れません。
        rewardedAd.preload(new VAMPRequest.Builder().build());
        addLog("preload() start");

        Button loadButton = (Button) findViewById(R.id.button_load);
        loadButton.setText(R.string.load_show);
        loadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の表示準備ができているか確認
                if (!rewardedAd.isReady()) {
                    // 広告取得
                    rewardedAd.load(new VAMPRequest.Builder().build());
                    addLog("[LOAD & SHOW] load()");
                } else {
                    // 広告取得済みなので表示
                    rewardedAd.show(VAMPAd2Activity.this);
                    addLog("[LOAD & SHOW] isReady:true → show()");
                }
            }
        });

        TextView idTextView = findViewById(R.id.vamp_id);
        idTextView.setText(getString(R.string.vamp_ad_id, VAMP_AD_ID));

        mLogView = findViewById(R.id.logs);
    }

    private class AdListener implements VAMPAdvancedListener {

        @Override
        public void onReceived() {
            // 広告表示の準備完了
            addLog("onReceived()");
        }

        @Override
        public void onFailedToLoad(VAMPError vampError) {
            // 広告取得失敗
            // 広告が取得できなかったときに通知されます。
            // 例）在庫が無い、タイムアウトなど
            addLog(String.format("onFailedToLoad(%s)", vampError), Color.RED);

            // 必要に応じて広告の再ロードを試みます
//            if (/* 任意のリトライ条件 */) {
//                rewardedAd.load(new VAMPRequest.Builder().build());
//            }
            if (vampError == VAMPError.NO_ADSTOCK) {
                // 在庫が無いので、再度loadをしてもらう必要があります。
                // 連続で発生する場合、時間を置いてからloadをする必要があります。
            } else if (vampError == VAMPError.NO_ADNETWORK) {
                // アドジェネ管理画面でアドネットワークの配信がONになっていない、
                // またはEU圏からのアクセスの場合(GDPR)発生します。
            } else if (vampError == VAMPError.NEED_CONNECTION) {
                // ネットワークに接続できない状況です。
                // 電波状況をご確認ください。
            } else if (vampError == VAMPError.MEDIATION_TIMEOUT) {
                // アドネットワークSDKから返答が得られず、タイムアウトしました。
            }
        }

        @Override
        public void onFailedToShow(VAMPError vampError) {
            // 広告表示失敗
            // showを実行したが、何らかの理由で広告表示が失敗したときに通知されます。
            // AdMobは動画再生の途中でユーザーによるキャンセルが可能
            // @see https://github.com/AdGeneration/VAMP-Android-SDK/wiki/VAMP-Android-API-Errors
            addLog(String.format("onFailedToShow(%s)", vampError), Color.RED);

            if (vampError == VAMPError.USER_CANCEL) {
                // ユーザが広告再生を途中でキャンセルしました。
            }
        }

        @Override
        public void onOpened() {
            // 動画が表示したタイミングで通知
            // アドネットワークによって通知タイミングが異なる (動画再生直前、または動画再生時)
            addLog("onOpened()", Color.BLACK);
        }

        @Override
        public void onCompleted() {
            // インセンティブ付与が可能になったタイミングで通知
            // アドネットワークによって通知タイミングが異なる（動画再生完了時、またはエンドカードを閉じたタイミング）
            addLog("onCompleted()", Color.BLACK);
        }

        @Override
        public void onClosed(boolean clicked) {
            // 動画プレーヤーやエンドカードが表示終了
            // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompleteで判定すること＞
            addLog(String.format("onClosed(%s)", clicked), Color.BLACK);
        }

        @Override
        public void onExpired() {
            // 有効期限オーバー
            // ＜注意：onReceiveを受けてからの有効期限が切れた。showするには再度loadを行う必要が有ります＞
            addLog("onExpired()", Color.RED);
        }

        @Override
        public void onLoadStart(String adnwName) {
            // 優先順にアドネットワークごとの広告取得を開始
            addLog(String.format("onLoadStart(%s)", adnwName));
        }

        @Override
        public void onLoadResult(String adnwName, boolean success, String message) {
            // アドネットワークを１つずつ呼び出した結果、広告在庫が取得できたかをsuccessフラグで確認
            if (success) {
                // 広告取得できたので表示
                rewardedAd.show(VAMPAd2Activity.this);
                addLog(String.format("onLoadResult(%s, success:%s, message:%s)", adnwName, success, message), Color.BLACK);
            } else {
                // 失敗しても、次のアドネットワークがあれば、広告取得を試みます。
                // 最終的に全てのアドネットワークの広告在庫が無ければ
                // onFailedToLoadのNO_ADSTOCKが通知されます。
                addLog(String.format("onLoadResult(%s, success:%s, message:%s)", adnwName, success, message), Color.RED);
            }
        }
    }
}
