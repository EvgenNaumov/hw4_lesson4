package server;

import ListJob.ListClientsThreads;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private Socket socket;
    private MyServer myServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nick;

    private ListClientsThreads listClientsThreads;
    private ExecutorService ex;

    public ClientHandler(MyServer myServer, Socket socket, ListClientsThreads listClientsThreads) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());

            this.listClientsThreads = listClientsThreads;

            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        authentication();
                        listClientsThreads.setListThreads(nick);
                        readMessages();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        closeConnection();
                    }
                }
            });

//            new Thread(() -> {
//                try {
//                    authentication();
//                    readMessages();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    closeConnection();
//                }
//            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        Message message = new Message();
        message.setMessage(nick+" вышел из чата");
        myServer.broadcastMessage(message);
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();

            listClientsThreads.closeJob(this.nick, this.ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authentication() {
        while (true) {
            try {
                AuthMessage message = new Gson().fromJson(dataInputStream.readUTF(), AuthMessage.class);

                this.nick = myServer.getAuthService().getNickByLoginAndPass(message.getLogin(), message.getPassword());

                if (this.nick != null && !myServer.isNickBusy(nick)) {
                    message.setAuthenticated(true);
                    message.setNick(nick);
//                   this.nick = nick;
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                    Message broadcastMsg = new Message();
                    broadcastMsg.setMessage(this.nick + " вошел в чат");
                    myServer.broadcastMessage(broadcastMsg);
                    myServer.subscribe(this);
//                    this.nick = nick;
                    return;
                } else{
                    message.setAnswerServer("пользователь по логину и паролю не найден");
                    message.setAuthenticated(false);
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                }
            } catch (IOException ignored) {
                System.out.println(ignored);
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            Message message = new Gson().fromJson(dataInputStream.readUTF(), Message.class);
            message.setNick(nick);
            System.out.println(message);
            if (!message.getMessage().startsWith("/")) {
                myServer.broadcastMessage(message);
                continue;
            }
            // /<command> <message>
            String[] tokens = message.getMessage().split("\\s");
            switch (tokens[0]) {
                case "/end":{
                    return;
                }
                case "/w":{// /w <nick> <message>
                    if (tokens.length < 3) {
                        Message msg = new Message();
                        msg.setMessage("Не хватает параметров, необходимо отправить команду следующего вида: /w <ник> <сообщение>");
                        this.sendMessage(msg);
                    }
                    String nick = tokens[1];
                    String msg = tokens[2];
                    myServer.sendMsgToClient(this, nick, msg);
                    break;
                }
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            dataOutputStream.writeUTF(new Gson().toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
