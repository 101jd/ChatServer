package org.example;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    ServerSocket serverSocket;

    BufferedReader reader;
    BufferedWriter writer;

    Socket socket;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen(){
        System.out.println("Server started");
        while (!serverSocket.isClosed())
            try {
                socket = serverSocket.accept();
                ClientManager manager = new ClientManager(socket);
                new Thread(manager).start();

            } catch (IOException e) {
                StreamManager.closeAll(new Closeable[]{serverSocket, socket});
            }
    }
}
