package org.example;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ClientManager implements Runnable {
    //Using HashMap for quick access to client in private messaging
    private static final HashMap<String, ClientManager> clients = new HashMap<>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String name;

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            StreamManager.closeAll(new Closeable[]{reader, writer, socket});
        }
    }

    private boolean addClient(String name){
        if (!clients.keySet().contains(this.name)){
            clients.put(this.name, this);
            System.out.println(this.name + " connected");
            return true;
        }
        return false;
    }

    @Override
    public void run(){
        String message;
        boolean flag = false;
        do {
            
            try {
                writer.write("Enter your name:");
                writer.newLine();
                writer.flush();
                this.name = reader.readLine();
                flag = addClient(name);
                if (!flag){
                    writer.write("Name is busy");
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                StreamManager.closeAll(new Closeable[]{reader, writer, socket});
            }
        } while (!flag);
        while (socket.isConnected()){
            try {
                message = reader.readLine();
                broadcast(message);
            } catch (IOException e) {
                StreamManager.closeAll(new Closeable[]{reader, writer, socket});
            }
        }
    }

    public void broadcast(String message){
        if (message.startsWith("@")){
            String target = message.replaceFirst("@", "")
                    .split(" ")[0];
            if (clients.keySet().contains(target)){
                try {
                    clients.get(target).writer.write(name + " writes: " + message
                            .replaceFirst("@", "")
                            .replaceFirst(target, ""));
                    clients.get(target).writer.newLine();
                    clients.get(target).writer.flush();
                } catch (IOException e) {
                    StreamManager.closeAll(new Closeable[]{reader, writer, socket});
                    removeClient(target);
                }
            }
        } else {
            for (ClientManager client : clients.values()) {
                if (!client.equals(this)) {
                    try {
                        client.writer.write(name + " writes: " + message);
                        client.writer.newLine();
                        client.writer.flush();
                    } catch (IOException e) {
                        StreamManager.closeAll(new Closeable[]{reader, writer, socket});
                        removeClient(client.name);
                    }
                }
            }
        }
    }

    private void removeClient(String name){
        clients.remove(name);
    }


}


