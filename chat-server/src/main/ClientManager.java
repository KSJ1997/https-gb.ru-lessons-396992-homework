package main;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clients.add(this);
            name = bufferedReader.readLine();
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClient();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }

    private void broadcastMessage(String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.equals(this) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void sendPrivateMessage(String recipient, String message) {
        for (ClientManager client : clients) {
            try {
                if (client.name.equals(recipient)) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    return;
                }
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    @Override
    public void run() {
        String message;
        while (!socket.isClosed()) {
            try {
                message = bufferedReader.readLine();
                if (message.startsWith("private:")) { // Личное сообщение
                    String[] parts = message.split(":", 3);
                    if (parts.length >= 3) {
                        String recipient = parts[1];
                        String text = parts[2];
                        sendPrivateMessage(recipient, "private from " + name + ": " + text);
                    } else {
                        System.out.println("Неверный формат личного сообщения.");
                    }
                } else { // Обычное сообщение
                    broadcastMessage(message);
                }
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
}
