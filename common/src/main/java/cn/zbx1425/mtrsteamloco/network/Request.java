package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.Main;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Request {

    public final String url;
    public final String method;
    public final JsonObject payload;
    public final Consumer<JsonObject> callback;

    public Request(String url, Consumer<JsonObject> callback) {
        this(url, "GET", null, callback);
    }

    public Request(String url, String method, JsonObject payload, Consumer<JsonObject> callback) {
        this.url = url;
        this.method = method;
        this.payload = payload;
        this.callback = callback;
    }

    public void sendBlocking() {
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
            conn.setRequestMethod(method);
            if (payload != null) {
                Main.LOGGER.info(payload.toString());
                byte[] postDataBytes = payload.toString().getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);
            }

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) sb.append((char)c);
            Main.LOGGER.info(sb.toString());
            if (callback != null) {
                callback.accept(JsonParser.parseString(sb.toString()).getAsJsonObject());
            }
        } catch (Exception ex) {
            Main.LOGGER.warn(ex);
        }
    }

    public void sendAsync() {
        executor.submit(this::sendBlocking);
    }

    public static final ExecutorService executor =
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("mtrsteamloco-http-%d").build());
}
