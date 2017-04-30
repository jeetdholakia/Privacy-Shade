package com.sand5.privacyscreen.utils;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by jeetdholakia on 4/29/17.
 */

public class ImageUrlHelper {

    private static String baseUrl = "gs://privacy-screen.appspot.com/backgrounds/Image";

    public static ArrayList<String> getImageUrls() {
        ArrayList<String> urlList = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            String url = baseUrl + i + ".jpg";
            Logger.d("Url is: " + url);
            urlList.add(url);
        }
        return urlList;
    }

}
