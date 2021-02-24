package server;

import ListJob.ListClientsThreads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    public static final int PORT = 8081;

    private List<ClientHandler> clients;
    private AuthService authService;
    private ListClientsThreads listClientsThreads;
    public MyServer() {
        //подключаемся к базе пользователей

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            try{
                authService = new BaseAuthService();
                authService.start();
            } catch(SQLException e){
                e.printStackTrace();
            }

            clients = new ArrayList<>();
            //hashmap для хранения ID потоков клиентов
            listClientsThreads = new ListClientsThreads();

            while (true) {
                System.out.println("Ожидаем поключение клиентов");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket,listClientsThreads);
            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getNick()).append(" ");
        }
        Message message = new Message();
        message.setMessage(sb.toString());
        broadcastMessage(message);
    }

    public synchronized void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nickTo)) {
                System.out.printf("Отправляем личное сообщение от %s, кому %s", from.getNick(), nickTo);
                Message message = new Message();
                message.setNick(from.getNick());
                message.setMessage(msg);
                client.sendMessage(message);
                return;
            }
        }
        System.out.printf("Клиент с ником %s не подюклчен к чату", nickTo);
        Message message = new Message();
        message.setMessage("Клиент с этим ником не подключен к чату");
        from.sendMessage(message);
    }

    public synchronized void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }
}
