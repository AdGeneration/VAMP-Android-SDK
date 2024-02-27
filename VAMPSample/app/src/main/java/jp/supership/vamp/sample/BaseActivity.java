package jp.supership.vamp.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import jp.supership.vamp.VAMP;

import java.lang.reflect.Method;
import java.util.Calendar;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "VAMPSAMPLE";
    protected TextView mLogView;

    // Sound MediaPlayer
    private MediaPlayer mediaPlayer;
    private boolean isPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(
                    String.format("[Test:%s] [Debug:%s]", VAMP.isTestMode(), VAMP.isDebugMode()));
        }

        // メディアプレイヤー初期化
        initSound();

        if (savedInstanceState == null) {
            clearLog(); // ログ消去
        }
        onCreateLayout(savedInstanceState);
    }

    protected abstract void onCreateLayout(Bundle savedInstanceState);

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlay", isPlay);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isPlay = savedInstanceState.getBoolean("isPlay");
        loadLog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSound();

        try {
            Class<?> clazz =
                    Class.forName("jp.supership.vamp.mediation.ironsource.IronSourceAdapter");
            Method onPause = clazz.getMethod("onPause", Activity.class);
            onPause.invoke(null, this);
        } catch (Exception e) {
            android.util.Log.i(TAG, e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlay) {
            startSound();
        }
        loadLog();

        try {
            Class<?> clazz =
                    Class.forName("jp.supership.vamp.mediation.ironsource.IronSourceAdapter");
            Method onResume = clazz.getMethod("onResume", Activity.class);
            onResume.invoke(null, this);
        } catch (Exception e) {
            android.util.Log.i(TAG, e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

    protected void addLog(String message, int color) {
        android.util.Log.d(TAG, message);
        String hex_color = String.format("%06x", color & 0x00ffffff);
        String builder = getDateString() + "<font color=#" + hex_color + ">" + message + "</font>";

        saveLog(builder);
    }

    protected void addLog(String message, @NonNull EventType event) {
        final int color = event.getColor();
        addLog(message, color);
    }

    protected void addLog(String message) {
        android.util.Log.d(TAG, message);
        String builder = getDateString() + message;

        saveLog(builder);
    }

    private void clearLog() {
        SharedPreferences sp = getSharedPreferences("log", MODE_PRIVATE);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("reward_log", "");
            editor.apply();
        }

        updateLog("");
    }

    private void loadLog() {
        String log = "";
        SharedPreferences sp = getSharedPreferences("log", MODE_PRIVATE);
        if (sp != null) {
            log = sp.getString("reward_log", "");
        }

        updateLog(log);
    }

    private void saveLog(String message) {
        StringBuilder builder = new StringBuilder(message);
        String log = "";
        SharedPreferences sp = getSharedPreferences("log", MODE_PRIVATE);
        if (sp != null) {
            log = sp.getString("reward_log", "");

            if (log.length() > 0) {
                builder.append("<br>");
                builder.append(log);
            }

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("reward_log", builder.toString());
            editor.apply();
        }

        updateLog(builder.toString());
    }

    private String getDateString() {
        return (String) DateFormat.format("MM/dd kk:mm:ss ", Calendar.getInstance());
    }

    private void updateLog(final String message) {
        if (mLogView != null) {
            mLogView.setText(Html.fromHtml(message));
        }
    }

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

    private void startSound() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void pauseSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}
