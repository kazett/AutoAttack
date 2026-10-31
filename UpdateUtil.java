// ##kazet (c) 2022-2026 All rights reserved##
package com.autoattack.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class UpdateUtil {
    private static String getJsonString(String sURL) {
        try {
            URL obj = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray getJsonArray(String sURL) {
        String jsonStr = getJsonString(sURL);
        if (jsonStr == null) return null;
        return JsonParser.parseString(jsonStr).getAsJsonArray();
    }

    public static JsonObject getJsonObject(String sURL) {
        String jsonStr = getJsonString(sURL);
        if (jsonStr == null) return null;
        return JsonParser.parseString(jsonStr).getAsJsonObject();
    }

    public static String getMinecraftVersion() {
        return SharedConstants.getGameVersion().getId();
    }

    public static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        return s1.compareTo(s2);
    }

    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + "s", s));
        }
        return sb.toString();
    }
}
// ##kazet (c) 2022-2026 All rights reserved##
