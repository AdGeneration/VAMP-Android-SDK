package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.supership.vamp.AdvancedListener;
import jp.supership.vamp.VAMP;
import jp.supership.vamp.VAMPAd;
import jp.supership.vamp.VAMPError;
import jp.supership.vamp.VAMPListener;

public class VAMPAd2Activity extends BaseActivity {

    /**
     * 広告枠IDを設定してください
     * 59756 : Androidテスト用ID (このIDのままリリースしないでください)
     */
    public static final String VAMP_AD_ID = "59756";

    private VAMP vamp;

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_ad2);
        setTitle(R.string.vamp_ad2);

        // VAMPインスタンスの取得
        vamp = VAMP.getVampInstance(this, VAMP_AD_ID);
        vamp.setVAMPListener(new AdListener());
        vamp.setAdvancedListener(new AdvListener());

        // preload v3.0〜
        // 新規追加された広告を事前に取得するメソッド
        // 広告を取得するのに時間がかかるため（動画ファイル、プレイアブルのダウンロード）、
        // 事前に在庫を確保しておき、ユーザーに待ち時間無く広告を表示するための機能。
        // ※リスナー（onLoadResult、onFailedToLoadなど）は受け取れません。
        vamp.preload();
        addLog("preload() start");

        Button loadButton = (Button) findViewById(R.id.button_load);
        loadButton.setText(R.string.load_show);
        loadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の表示準備ができているか確認
                if (!vamp.isReady()) {
                    // 広告取得
                    vamp.load();
                    addLog("[LOAD & SHOW] load()");
                } else {
                    // 広告取得済みなので表示
                    vamp.show();
                    addLog("[LOAD & SHOW] isReady:true → show()");
                }
            }
        });

        TextView idTextView = (TextView) findViewById(R.id.vamp_id);
        idTextView.setText("ID:" + VAMP_AD_ID);

        mLogView = (TextView) findViewById(R.id.logs);
    }

    private class AdvListener implements AdvancedListener {

        @Override
        public void onLoadStart(VAMPAd vampAd) {
            // 優先順にアドネットワークごとの広告取得を開始
            String adnwName = vampAd.getAdnwName();
            addLog("onLoadStart(" + adnwName + ")");
        }

        @Override
        public void onLoadResult(VAMPAd vampAd, boolean success, String message) {
            // アドネットワークを１つずつ呼び出した結果、広告在庫が取得できたかをsuccessフラグで確認
            String adnwName = vampAd.getAdnwName();
            if (success) {
                // 広告取得できたので表示
                vamp.show();
                addLog("onLoadResult(" + adnwName + ",success:" + success + ") show()", Color.BLACK);
            } else {
                // 失敗しても、次のアドネットワークがあれば、広告取得を試みます。
                // 最終的に全てのアドネットワークの広告在庫が無ければ
                // onFailedToLoadのNO_ADSTOCKが通知されます。
                addLog("onLoadResult(" + adnwName + ",success:" + success + ") " + message, Color.RED);
            }
        }
    }

    private class AdListener implements VAMPListener {

        @Override
        public void onFailedToLoad(VAMPError vampError, VAMPAd vampAd) {
            // 広告取得失敗
            // 広告が取得できなかったときに通知されます。
            // 例）在庫が無い、タイムアウトなど
            addLog("onFailedToLoad(" + vampError + ")", Color.RED);

            // 必要に応じて広告の再ロードを試みます
//            if (/* 任意のリトライ条件 */) {
//                vamp.load();
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
        public void onFailedToShow(VAMPError vampError, VAMPAd vampAd) {
            // 広告表示失敗
            // showを実行したが、何らかの理由で広告表示が失敗したときに通知されます。
            // AdMobは動画再生の途中でユーザーによるキャンセルが可能
            // @see https://github.com/AdGeneration/VAMP-Android-SDK/wiki/VAMP-Android-API-Errors
            addLog("onFailedToShow(" + vampError + ")", Color.RED);

            if (vampError == VAMPError.USER_CANCEL) {
                // ユーザが広告再生を途中でキャンセルしました。
            }
        }

        @Override
        public void onOpen(VAMPAd vampAd) {
            // 動画が表示したタイミングで通知
            // アドネットワークによって通知タイミングが異なる (動画再生直前、または動画再生時)
            String adnwName = vampAd.getAdnwName();
            addLog("onOpen(" + adnwName + ")", Color.BLACK);
        }

        @Override
        public void onComplete(VAMPAd vampAd) {
            // インセンティブ付与が可能になったタイミングで通知
            // アドネットワークによって通知タイミングが異なる（動画再生完了時、またはエンドカードを閉じたタイミング）
            String adnwName = vampAd.getAdnwName();
            addLog("onComplete(" + adnwName + ")", Color.BLUE);
        }

        @Override
        public void onClose(VAMPAd vampAd, boolean adClicked) {
            // 動画プレーヤーやエンドカードが表示終了
            // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompleteで判定すること＞
            String adnwName = vampAd.getAdnwName();
            addLog("onClose(" + adnwName + ", Click:" + adClicked + ")", Color.BLACK);
        }

        @Override
        public void onExpired(String placementId) {
            // 有効期限オーバー
            // ＜注意：onReceiveを受けてからの有効期限が切れた。showするには再度loadを行う必要が有ります＞
            addLog("onExpired()", Color.RED);
        }

        @Override
        public void onReceive(VAMPAd vampAd) {
            // 広告表示の準備完了
            // v3.0〜　onLoadResult:successで判定する
        }
    }
}
