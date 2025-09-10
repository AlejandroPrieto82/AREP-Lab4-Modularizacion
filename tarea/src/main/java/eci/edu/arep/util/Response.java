package eci.edu.arep.util;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Response {
    private final PrintWriter writer;

    public Response(PrintWriter writer) {
        this.writer = writer;
    }

    public void send(String body) {
        writer.print("HTTP/1.1 200 OK\r\n");
        writer.print("Content-Type: text/plain\r\n");
        writer.print("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
        writer.print("\r\n");
        writer.print(body);
        writer.flush();
    }
}
