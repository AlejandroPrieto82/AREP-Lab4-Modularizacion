package eci.edu.arep.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import eci.edu.arep.microspringboot.annotations.GetMapping;
import eci.edu.arep.microspringboot.annotations.RequestParam;
import eci.edu.arep.microspringboot.annotations.RestController;
import eci.edu.arep.util.Service;

public class HttpServer {

    private static final Map<String, Method> services = new ConcurrentHashMap<>();
    private static final Map<String, Object> controllers = new ConcurrentHashMap<>();
    private static final Map<String, Service> getRoutes = new ConcurrentHashMap<>();
    private static final int PORT = 35000;
    private static volatile String rootFiles = "www";

    // Concurrency
    private static volatile boolean running = false;
    private static ServerSocket serverSocket;
    private static final int THREAD_POOL_SIZE = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
    private static ExecutorService executor;

    // ----------- FRAMEWORK IoC -----------
    public static void loadServices(String[] args) {
        try {
            if (args == null || args.length == 0) {
                System.out.println("No controller classes provided en args, se puede usar API tipo Express con get(...)");
                return;
            }

            for (String clsName : args) {
                Class<?> c = Class.forName(clsName);
                System.out.println("Inicio carga de componentes: " + clsName);

                if (c.isAnnotationPresent(RestController.class)) {
                    Object controller = c.getDeclaredConstructor().newInstance();
                    controllers.put(c.getName(), controller);

                    for (Method m : c.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(GetMapping.class)) {
                            String mapping = m.getAnnotation(GetMapping.class).value();
                            System.out.println("Cargando método: " + m.getName() + " -> " + mapping);
                            services.put(mapping, m);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ----------- API tipo Express -----------
    public static void get(String path, Service service) {
        getRoutes.put(path, service);
    }

    public static void staticfiles(String path) {
        rootFiles = path;
    }

    // ----------- CORE DEL SERVIDOR -----------
    public static void runServer(String[] args) throws IOException, URISyntaxException {
        loadServices(args);

        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        serverSocket = new ServerSocket(PORT);
        serverSocket.setReuseAddress(true);
        running = true;

        System.out.println("Servidor escuchando en puerto " + PORT + " con pool de hilos = " + THREAD_POOL_SIZE);

        // Shutdown hook para graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("ShutdownHook activado. Intentando detener servidor...");
            try {
                stopServer();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));

        try {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(10_000); // 10s timeout por socket
                    executor.submit(() -> {
                        try {
                            handleClient(clientSocket);
                        } catch (Exception e) {
                            e.printStackTrace();
                            try { clientSocket.close(); } catch (IOException ignored) {}
                        }
                    });
                } catch (SocketException se) {
                    // ocurre cuando serverSocket.close() es llamado durante shutdown
                    if (running) {
                        System.err.println("SocketException inesperado: " + se.getMessage());
                    } else {
                        System.out.println("ServerSocket cerrado, saliendo del loop de accept.");
                    }
                }
            }
        } finally {
            // En caso de salir del loop, asegurar que el executor se apague
            stopExecutorAndWait();
        }
    }

    public static void stopServer() {
        running = false;
        System.out.println("Deteniendo servidor...");
        // cerrar server socket para romper accept()
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopExecutorAndWait();
        System.out.println("Servidor detenido.");
    }

    private static void stopExecutorAndWait() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown(); // no acepta nuevas tareas
            try {
                // esperar un tiempo razonable a que terminen las tareas actuales
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.out.println("Forzando shutdown del pool...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Manejo de cliente aislado
    private static void handleClient(Socket clientSocket) throws IOException {
        try (
            InputStream inRaw = clientSocket.getInputStream();
            OutputStream rawOut = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inRaw));
        ) {
            String inputLine = in.readLine();
            if (inputLine == null || inputLine.isEmpty()) return;

            String[] requestParts = inputLine.split(" ");
            String method = requestParts[0];
            String rawPath = requestParts[1];

            // Consumir headers
            while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {}

            URI requri = URI.create(rawPath);
            HttpRequest req = new HttpRequest(requri);
            HttpResponse res = new HttpResponse(); // si quieres usarlo
            String route = requri.getPath();

            if (services.containsKey(route)) {
                String responseText = invokeService(requri);
                writeStringResponse(rawOut, responseText);
            } else if (getRoutes.containsKey(route)) {
                String output = getRoutes.get(route).executeService(
                        new eci.edu.arep.util.Request(requri),
                        new eci.edu.arep.util.Response(new PrintWriter(rawOut, true))
                );
                if (!output.startsWith("HTTP/1.1")) {
                    // asumir body
                    String r = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: " + output.getBytes().length + "\r\n\r\n" + output;
                    writeStringResponse(rawOut, r);
                } else {
                    writeStringResponse(rawOut, output);
                }
            } else {
                if (route.equals("/")) route = "/index.html";
                serveStaticFile(route, rawOut);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/html\r\n\r\n<h1>500 Error</h1>";
            try {
                clientSocket.getOutputStream().write(err.getBytes());
            } catch (IOException ignored) {}
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private static void writeStringResponse(OutputStream rawOut, String response) throws IOException {
        rawOut.write(response.getBytes());
        rawOut.flush();
    }

    // ----------- INVOCAR CON REFLEXIÓN -----------
    private static String invokeService(URI requri) throws Exception {
        HttpRequest req = new HttpRequest(requri);
        String servicePath = requri.getPath();

        Method m = services.get(servicePath);
        if (m == null) return "HTTP/1.1 404 Not Found\r\n\r\n<h1>404 Not Found</h1>";

        Object controller = controllers.get(m.getDeclaringClass().getName());

        Parameter[] methodParams = m.getParameters();
        Object[] params = new Object[methodParams.length];
        for (int i = 0; i < methodParams.length; i++) {
            if (methodParams[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = methodParams[i].getAnnotation(RequestParam.class);
                String val = req.getValue(rp.value(), rp.defaultValue());
                params[i] = val;
            } else {
                params[i] = null; // si hay parámetros sin @RequestParam
            }
        }

        String result = (String) m.invoke(controller, params);
        return "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: " + result.getBytes().length + "\r\n\r\n" + result;
    }

    // ----------- ARCHIVOS ESTÁTICOS -----------
    private static void serveStaticFile(String path, OutputStream rawOut) throws IOException {
        InputStream resourceStream = HttpServer.class.getClassLoader().getResourceAsStream(rootFiles + path);

        if (resourceStream != null) {
            byte[] fileBytes = resourceStream.readAllBytes();
            String mimeType = guessMimeType(path);

            StringBuilder header = new StringBuilder();
            header.append("HTTP/1.1 200 OK\r\n");
            header.append("Content-Type: ").append(mimeType).append("\r\n");
            header.append("Content-Length: ").append(fileBytes.length).append("\r\n");
            header.append("\r\n");

            rawOut.write(header.toString().getBytes());
            rawOut.write(fileBytes);
            rawOut.flush();
            resourceStream.close();
        } else {
            String errorMessage = "<h1>404 Not Found</h1>";
            String resp = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length: "
                    + errorMessage.getBytes().length + "\r\n\r\n" + errorMessage;
            rawOut.write(resp.getBytes());
            rawOut.flush();
        }
    }

    private static String guessMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
