package com.sand5.privacyscreen.events;

/**
 * Created by jeetdholakia on 5/2/17.
 */

public class SetWallpaperEvent {

    private String url;

    public SetWallpaperEvent(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
