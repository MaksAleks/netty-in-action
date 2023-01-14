package max.learn.netty.echo.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class EchoBlockingServer {

    static ThreadLocal<Socket> clientTl = new ThreadLocal<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server started at 8080");
            ExecutorService tp = Executors.newFixedThreadPool(1);
            while (true) {
                Socket client = serverSocket.accept();
                tp.submit(() -> {
                    System.out.println("Connection accepted from " + client.getRemoteSocketAddress());
                    clientTl.set(client);
                    try (var reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                         var writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
                        read(reader).forEach(line -> {
                            write(line, writer);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(String str, Writer writer) {
        try {
            System.out.println("Sending: \"" + str + "\" to " + clientTl.get().getRemoteSocketAddress());
            if (!str.endsWith("\r\n")) {
                str = str + "\r\n";
            }
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Err sending to " + clientTl.get().getRemoteSocketAddress() + ": " + e.getLocalizedMessage());
        }
    }

    private static Stream<String> read(BufferedReader reader) {
        return reader.lines().peek(line -> System.out.println("New message from " + clientTl.get().getRemoteSocketAddress() + ": " + line));
    }
}
