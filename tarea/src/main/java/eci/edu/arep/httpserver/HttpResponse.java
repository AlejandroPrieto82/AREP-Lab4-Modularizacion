package eci.edu.arep.httpserver;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private String status = "200 OK";
    private String body = "";
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse() {
        headers.put("Content-Type", "text/html");
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String buildResponse() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append("\r\n");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }
}
