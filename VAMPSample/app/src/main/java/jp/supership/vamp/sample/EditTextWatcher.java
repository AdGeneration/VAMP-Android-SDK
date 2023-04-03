package jp.supership.vamp.sample;

import android.text.Editable;
import android.text.TextWatcher;

public class EditTextWatcher implements TextWatcher {
    private TextChangedListener listener;

    public EditTextWatcher(TextChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (listener != null) {
            listener.onTextChanged(s.toString());
        }
    }
}
