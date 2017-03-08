package jp.supership.vamp.sample;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;

import jp.supership.vamp.VAMP;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "VAMPSAMPLE";
    protected TextView mLogView;

    // Sound Mediaplayer
    private MediaPlayer mediaPlayer;
    private boolean isPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // メディアプレイヤー初期化
        initSound();

        onCreateLayout(savedInstanceState);

        actionBar.setSubtitle(String.format("[Test:%s] [Debug:%s]", VAMP.isTestMode(), VAMP.isDebugMode()));

        if (savedInstanceState == null) {
            clearLog();  // ログ消去
        }
    }

    abstract protected void onCreateLayout(Bundle savedInstanceState);

    @Override
    protected void onDestroy() {
        // メディアプレイヤー破棄
        pauseSound();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlay", isPlay);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isPlay = savedInstanceState.getBoolean("isPlay");
        loadLog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlay) {
            startSound();
        }
        loadLog();
    }

    //region ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sound, menu);
        MenuItem menu_sound = menu.findItem(R.id.menu_sound);
        if (menu_sound != null) {
            if (isPlay) {
                menu_sound.setIcon(R.drawable.soundon);
                menu_sound.setTitle(R.string.sound_off);
            } else {
                menu_sound.setIcon(R.drawable.soundoff);
                menu_sound.setTitle(R.string.sound_on);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menu_id = item.getItemId();
        if (menu_id == android.R.id.home) {
            finish();
            return true;
        } else if (menu_id == R.id.menu_sound) {
            if (isPlay) {
                pauseSound();
            } else {
                startSound();
            }
            isPlay ^= true;
            supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region log

    /**
     * ログ追加
     *
     * @param message
     * @param color
     */
    protected void addLog(String message, int color) {
        android.util.Log.d(TAG, message);
        String hex_color = String.format("%06x", Integer.valueOf(color & 0x00ffffff));
        StringBuffer buffer = new StringBuffer();
        buffer.append(getDateString());
        buffer.append("<font color=#" + hex_color + ">");
        buffer.append(message);
        buffer.append("</font>");

        saveLog(buffer.toString());
    }

    /**
     * ログ追加
     *
     * @param message
     */
    protected void addLog(String message) {
        android.util.Log.d(TAG, message);
        StringBuffer buffer = new StringBuffer();
        buffer.append(getDateString());
        buffer.append(message);

        saveLog(buffer.toString());
    }

    /**
     * ログ消去
     */
    private void clearLog() {
        SharedPreferences sp = getSharedPreferences("log", MODE_PRIVATE);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("reward_log", "");
            editor.commit();
        }

        updateLog("");
    }

    /**
     * ログ読み込み
     */
    private void loadLog() {
        String log = "";
        SharedPreferences sp = getSharedPreferences("log", MODE_PRIVATE);
        if (sp != null) {
            log = sp.getString("reward_log", "");
        }

        updateLog(log);
    }

    /**
     * ログ保存
     *
     * @param message
     */
    private void saveLog(String message) {
        StringBuffer buffer = new StringBuffer(message);
        String log = "";
        SharedPreferences sp = getSharedPreferences("log", MODE_PRIVATE);
        if (sp != null) {
            log = sp.getString("reward_log", "");

            if (log != null && log.length() > 0) {
                buffer.append("<br>");
                buffer.append(log);
            }

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("reward_log", buffer.toString());
            editor.commit();
        }

        updateLog(buffer.toString());
    }

    /**
     * 日時文字列取得
     * @return
     */
    private String getDateString() {
        return (String) DateFormat.format("MM/dd kk:mm:ss ", Calendar.getInstance());
    }

    /**
     * ログ表示更新
     * @param message
     */
    private void updateLog(final String message) {
        if (mLogView != null) {
            mLogView.setText(Html.fromHtml(message));
        }
    }
    //endregion

    //region Sound Mediaplayer

    /**
     * メディアプレイヤー初期化
     */
    private void initSound() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.invisible);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.setLooping(true);

        if (isPlay) {
            startSound();
        } else {
            pauseSound();
        }
    }

    /**
     * メディアプレイヤー再生
     */
    private void startSound() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * メディアプレイヤー一時停止
     */
    private void pauseSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    //endregion
}
