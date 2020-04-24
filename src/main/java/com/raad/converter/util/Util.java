package com.raad.converter.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Util {

    public static boolean urlValidator(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException exception) {
            return false;
        } catch (MalformedURLException exception) {
            return false;
        }
    }
}
