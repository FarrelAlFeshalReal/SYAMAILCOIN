import java.io.*;
import java.net.*;
import java.util.*;

public class Node {
    private static final int PORT = 5000;
    private static final String BLOCK_FILE = "blockrecursive.jsonl";

    private static void appendBlock(String json) {
        try (FileWriter fw = new FileWriter(BLOCK_FILE, true)) {
            fw.write(json + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readBlocks() {
        List<String> blocks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BLOCK_FILE))) {
            String line;
            while ((line = br.readLine()) != null) blocks.add(line);
        } catch (IOException e) {
        }
        return blocks;
    }

    private static String lastBlock() {
        List<String> blocks = readBlocks();
        if (blocks.isEmpty()) return null;
        return blocks.get(blocks.size() - 1);
    }

    private static void send(PrintWriter out, String body, int code, String status) {
        out.println("HTTP/1.1 " + code + " " + status);
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + body.length());
        out.println();
        out.println(body);
    }

    public static void main(String[] args) {
        System.out.println("Syamailcoin Node berjalan di port " + PORT);
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try (Socket client = server.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                    String req = in.readLine();
                    if (req == null) continue;
                    String[] parts = req.split(" ");
                    if (parts.length < 2) continue;
                    String method = parts[0], path = parts[1];

                    String line;
                    int len = 0;
                    while (!(line = in.readLine()).isEmpty()) {
                        if (line.toLowerCase().startsWith("content-length:"))
                            len = Integer.parseInt(line.split(":")[1].trim());
                    }

                    char[] buf = new char[len];
                    if (len > 0) in.read(buf);
                    String bodyData = new String(buf);

                    if (method.equals("GET") && path.equals("/")) {
                        send(out, "{\"message\":\"SyamailCoin: Gödel's Untouched Money\",\"status\":\"running\"}", 200, "OK");
                    } else if (method.equals("GET") && path.equals("/status")) {
                        List<String> blocks = readBlocks();
                        String last = lastBlock();
                        String body = "{\"total\":" + blocks.size() + ",\"last\":" + (last == null ? "null" : last) + "}";
                        send(out, body, 200, "OK");
                    } else if (method.equals("POST") && path.equals("/tx")) {
                        String prev = (lastBlock() == null ? "genesis" : lastBlock());
                        String block = "{\"i\":" + readBlocks().size() + ",\"tx\":" + bodyData + ",\"p\":\"" + prev + "\"}";
                        appendBlock(block);
                        send(out, "{\"ok\":true,\"block\":" + block + "}", 200, "OK");
                    } else if (method.equals("GET") && path.equals("/blockrecursive")) {
                        List<String> blocks = readBlocks();
                        StringBuilder arr = new StringBuilder("[");
                        for (int i = 0; i < blocks.size(); i++) {
                            arr.append(blocks.get(i));
                            if (i < blocks.size() - 1) arr.append(",");
                        }
                        arr.append("]");
                        send(out, "{\"blocks\":" + arr + ",\"count\":" + blocks.size() + "}", 200, "OK");
                    } else {
                        send(out, "{\"error\":\"not found\"}", 404, "Not Found");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
