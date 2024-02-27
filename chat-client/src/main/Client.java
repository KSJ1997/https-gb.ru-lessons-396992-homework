import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final String name;

    public Client(Socket socket, String userName) {
        this.socket = socket;
        name = userName;

        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                if (message.startsWith("@")) { // Личное сообщение
                    String[] parts = message.split(" ", 2);
                    if (parts.length >= 2) {
                        String recipient = parts[0].substring(1); 
                        String text = parts[1];
                        bufferedWriter.write("private:" + recipient + ":" + text);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } else {
                        System.out.println("Неверный формат личного сообщения. Используйте @username message");
                    }
                } else { // Обычное сообщение
                    bufferedWriter.write("message:" + name + ": " + message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                while (socket.isConnected()) {
                    try {
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    } catch (Exception e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
}
