import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskServer {
    private static final Map<Integer, Task> TASKS = new TreeMap<>();
    private static final AtomicInteger IDS = new AtomicInteger();
    record Task(int id, String title, boolean complete) {}

    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    private static String json(Task t) {
        return "{\"id\":" + t.id() + ",\"title\":\"" + jsonEscape(t.title()) + "\",\"complete\":" + t.complete() + "}";
    }
    private static void respond(HttpExchange x, int status, String body) throws java.io.IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        x.sendResponseHeaders(status, data.length);
        try (var out = x.getResponseBody()) { out.write(data); }
    }
    private static synchronized void handle(HttpExchange x) throws java.io.IOException {
        String path = x.getRequestURI().getPath();
        String method = x.getRequestMethod();
        if (path.equals("/tasks") && method.equals("GET")) {
            respond(x, 200, "[" + String.join(",", TASKS.values().stream().map(TaskServer::json).toList()) + "]");
        } else if (path.equals("/tasks") && method.equals("POST")) {
            String query = x.getRequestURI().getRawQuery();
            if (query == null || !query.startsWith("title=")) { respond(x, 400, "{\"error\":\"title required\"}"); return; }
            String title = URLDecoder.decode(query.substring(6), StandardCharsets.UTF_8);
            if (title.isBlank() || title.length() > 200) { respond(x, 400, "{\"error\":\"invalid title\"}"); return; }
            int id = IDS.incrementAndGet();
            Task task = new Task(id, title, false);
            TASKS.put(id, task);
            respond(x, 201, json(task));
        } else if (method.equals("POST") && path.matches("/tasks/[0-9]+/complete")) {
            int id;
            try { id = Integer.parseInt(path.split("/")[2]); }
            catch (NumberFormatException e) { respond(x, 400, "{\"error\":\"invalid id\"}"); return; }
            Task old = TASKS.get(id);
            if (old == null) { respond(x, 404, "{\"error\":\"not found\"}"); return; }
            Task updated = new Task(id, old.title(), true);
            TASKS.put(id, updated);
            respond(x, 200, json(updated));
        } else {
            respond(x, 404, "{\"error\":\"not found\"}");
        }
    }
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", TaskServer::handle);
        server.start();
        System.out.println("Task API listening on http://127.0.0.1:8080");
    }
}
