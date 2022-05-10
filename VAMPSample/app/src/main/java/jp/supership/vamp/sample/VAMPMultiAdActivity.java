package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import jp.supership.vamp.VAMPAdvancedListener;
import jp.supership.vamp.VAMPError;
import jp.supership.vamp.VAMPRequest;
import jp.supership.vamp.VAMPRewardedAd;

public class VAMPMultiAdActivity extends BaseActivity implements VAMPAdvancedListener {

    private static final String VAMP_MULTIAD_ID1 = "59756"; // 広告枠ID1を設定してください
    private static final String VAMP_MULTIAD_ID2 = "*****"; // 広告枠ID2を設定してください

    private VAMPRewardedAd rewardedAd1;
    private VAMPRewardedAd rewardedAd2;

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_multi_ad);
        setTitle(R.string.vamp_multi);

        rewardedAd1 = new VAMPRewardedAd(this, VAMP_MULTIAD_ID1);
        rewardedAd1.setListener(this);

        rewardedAd2 = new VAMPRewardedAd(this, VAMP_MULTIAD_ID2);
        rewardedAd2.setListener(this);

        findViewById(R.id.button_load1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の取得を開始
                rewardedAd1.load(new VAMPRequest.Builder().build());
                addLog(getIndex(VAMP_MULTIAD_ID1), "[LOAD1] load()");
            }
        });

        findViewById(R.id.button_load2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の取得を開始
                rewardedAd2.load(new VAMPRequest.Builder().build());
                addLog(getIndex(VAMP_MULTIAD_ID2), "[LOAD2] load()");
            }
        });

        findViewById(R.id.button_show1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の表示準備ができているか確認
                if (rewardedAd1.isReady()) {
                    // 広告を表示
                    rewardedAd1.show(VAMPMultiAdActivity.this);
                    addLog(getIndex(VAMP_MULTIAD_ID1), "[SHOW1] show()");
                } else {
                    addLog(getIndex(VAMP_MULTIAD_ID1), "[SHOW1] isReady:false");
                }
            }
        });

        findViewById(R.id.button_show2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (rewardedAd1.isReady()) {
                    // 動画の準備が完了していた場合
                    // 動画を再生する
                    rewardedAd1.show(VAMPMultiAdActivity.this);
                    addLog(getIndex(VAMP_MULTIAD_ID2), "[SHOW2] show()");
                } else {
                    addLog(getIndex(VAMP_MULTIAD_ID2), "[SHOW2] isReady:false");
                }
            }
        });

        ((TextView) findViewById(R.id.vamp_id1)).setText(getString(R.string.vamp_ad_id, VAMP_MULTIAD_ID1));
        ((TextView) findViewById(R.id.vamp_id2)).setText(getString(R.string.vamp_ad_id, VAMP_MULTIAD_ID2));
        mLogView = findViewById(R.id.logs);
    }

    private int getIndex(String placementId) {
        if (VAMP_MULTIAD_ID1.length() > 0 || VAMP_MULTIAD_ID2.length() > 0) {
            if (VAMP_MULTIAD_ID1.equals(placementId)) {
                return 1;
            } else if (VAMP_MULTIAD_ID2.equals(placementId)) {
                return 2;
            }
        }
        return -1;
    }

    private void addLog(int index, String message) {
        addLog("[" + index + "]" + message);
    }

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
        // @see https://github.com/AdGeneration/VAMP-Android-SDK/wiki/VAMP-Android-API-Errors
        addLog(String.format("onFailedToLoad(%s)", vampError), Color.RED);

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
        // show実行したが、何らかの理由で広告表示が失敗したときに通知されます。
        // 例）ユーザーが広告再生を途中でキャンセルなど
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
        // インセンティブ付与OK
        // インセンティブ付与が可能になったタイミング（動画再生完了時、またはエンドカードを閉じたタイミング）で通知
        // アドネットワークによって通知タイミングが異なる
        addLog("onCompleted()", Color.BLUE);
    }

    @Override
    public void onClosed(boolean clicked) {
        // 動画プレーヤーやエンドカードが表示終了
        // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompleteで判定すること＞
        addLog(String.format("onClosed(Click:%s)", clicked), Color.BLACK);
    }

    @Override
    public void onExpired() {
        // 有効期限オーバー
        // ＜注意：onReceiveを受けてからの有効期限が切れました。showするには再度loadを行う必要が有ります＞
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
            addLog(String.format("onLoadResult(%s, success, %s)", adnwName, message), Color.BLACK);
        } else {
            // 失敗しても、次のアドネットワークがあれば、広告取得を試みます。
            // 最終的に全てのアドネットワークの広告在庫が無ければ
            // onFailedToLoadのNO_ADSTOCKが通知されます。
            addLog(String.format("onLoadResult(%s, fail, %s)", adnwName, message));
        }
    }
}

