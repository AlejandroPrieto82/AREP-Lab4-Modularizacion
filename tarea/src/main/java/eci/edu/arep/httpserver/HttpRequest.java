package eci.edu.arep.httpserver;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final URI requri;
    private final Map<String, String> queryParams = new HashMap<>();

    public HttpRequest(URI requri) {
        this.requri = requri;
        parseQuery(requri.getQuery());
    }

    private void parseQuery(String query) {
        if (query == null) return;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                queryParams.put(kv[0], kv[1]);
            }
        }
    }

    public String getValue(String paramName, String defaultValue) {
        return queryParams.getOrDefault(paramName, defaultValue);
    }

    public String getPath() {
        return requri.getPath();
    }
}
