package eci.edu.arep.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private URI uri;
    private Map<String, String> queryParams;

    public Request(URI uri) {
        this.uri = uri;
        this.queryParams = parseQueryParams(uri);
    }

    public String getPath() {
        return uri.getPath();
    }

    public String getValues(String key) {
        return queryParams.get(key);
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length > 1) {
                    map.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return map;
    }
}

