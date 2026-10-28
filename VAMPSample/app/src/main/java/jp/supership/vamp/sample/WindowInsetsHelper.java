package jp.supership.vamp.sample;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class WindowInsetsHelper {

    private WindowInsetsHelper() {}

    // Android 15 以降、targetSdk 35 以上のアプリは edge-to-edge が強制され、画面が ActionBar・システムバーの下まで広がる。
    // content に渡される inset (ActionBar の高さを含む) を padding にして、ボタンや入力欄が隠れないようにする。
    // appcompat 1.7.0 以上が前提。1.7.0 未満では content に渡る inset に ActionBar の高さが含まれず、
    // このヘルパーだけを使っても ActionBar との被りが残る。
    public static void applySystemBarInsets(@NonNull Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (v, windowInsets) -> {
                    Insets insets =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.displayCutout());
                    v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });
    }
}
