package jp.supership.vamp.sample;

import android.graphics.Color;

public enum EventType {
    OPENED(Color.BLACK),
    FAILED(Color.RED),
    COMPLETED(Color.parseColor("#00AA00")),
    CLOSED(Color.BLUE),
    EXPIRED(Color.RED);

    private final int color;

    EventType(final int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
