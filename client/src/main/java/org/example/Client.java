package org.example;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Client {
    String name;

    Socket socket;
    SocketAddress endpoint;

    BufferedReader reader;
    BufferedWriter writer;

    boolean auth;

    public Client(Socket socket){
        this.socket = socket;

        this.endpoint = socket.getRemoteSocketAddress();

        initStreams();

    }

    public void listenForMessages(){
//        AtomicReference<String> message = null;

        System.out.println(socket.getRemoteSocketAddress());
        new Thread(() -> {
            while (socket.isConnected()){
                try {
                    String message = reader.readLine();
                    if (message != null)
                        System.out.println(message);
                }catch (IOException e){
                    closeAll(new Closeable[]{reader, writer});
                    initStreams();
                }
            }
        }).start();
    }

    public void sendMessage() {
        Scanner scanner = new Scanner(System.in);
        while (socket.isConnected()) {
            try {
                writer.write(scanner.nextLine());
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                closeAll(new Closeable[]{reader, writer});
                initStreams();
            }
        }
    }

    public String getName() {
        return name;
    }

    private void closeAll(Closeable[] closeables){
        Arrays.stream(closeables).filter(closeable -> closeable != null).forEach(closeable -> {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void initStreams() {
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeAll(new Closeable[]{this.socket, this.reader, this.writer});
        }
    }

    private void sendName(){
        System.out.println("Enter your name");
            this.name = new Scanner(System.in).nextLine();
            try {
                writer.write(name);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                closeAll(new Closeable[]{reader, writer});
                initStreams();
                System.out.println("Client has problems with streams");
            }
    }

    public void tryName(){
        sendName();
        try {
            if (reader.readLine().equals("Name is busy")){
                tryName();
            }
        } catch (IOException e) {
            closeAll(new Closeable[]{socket, reader, writer});
        }
    }
}