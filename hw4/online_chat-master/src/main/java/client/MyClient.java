package client;

import History.StoryMessage;
import server.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class MyClient extends JFrame {

    private ServerService serverService;
    private StoryMessage storyMessage;

    public MyClient() {
        super("Чат");
        JPanel jPanel = new JPanel();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setSize(300, 50);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(400, 400, 500, 300);

        JTextArea mainChat = new JTextArea();
        mainChat.setSize(400, 250);

        initLoginPanel(mainChat);

        JTextField myMessage = new JTextField();

        JButton send = new JButton("Send");
        send.addActionListener(actionEvent -> sendMessage(myMessage));

        myMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage(myMessage);
                }
            }
        });

        serverService = new SocketServerService();
        serverService.openConnection();
        if (serverService.isConnected()) {
//            storyMessage.readMessage(mainChat);
            new Thread(() -> {
                while (true) {
                    printToUI(mainChat, serverService.readMessages());
                }
            }).start();
        }

        add(mainChat);
        jPanel.add(send);
        jPanel.add(myMessage);
        add(jPanel);
    }

    private void initLoginPanel(JTextArea mainChat) {
        JTextField login = new JTextField();
        login.setToolTipText("Логин");
        JPasswordField password = new JPasswordField();
        password.setToolTipText("Пароль");
//        JButton authButton = new JButton("Авторизоваться");

        JPanel jPanel = new JPanel();
        JButton authButton = new JButton("Авторизоваться");
        JButton nickButton = new JButton("Сменить ник");
        jPanel.add(authButton);
        jPanel.add(nickButton) ;


        JLabel authLabel = new JLabel("Offline");
        authButton.addActionListener(actionEvent -> {
            String lgn = login.getText();
            String psw = new String(password.getPassword());
            if (lgn != null && psw != null && !lgn.isEmpty() && !psw.isEmpty()) {
                try {
                    String nick = serverService.authorization(lgn, psw);

                    String[] tokens = nick.split("\\s");

                    if((!nick.isEmpty())  && (serverService.isConnected())){
                        authLabel.setText("Online, nick "+nick);
                        storyMessage = new StoryMessage();
                        storyMessage.readMessage(mainChat);
                    }else {
                        authLabel.setText("Offline: " + nick);
                        return;
                    }
                } catch (IOException e) {
                    return;
//                    e.printStackTrace();
                }
                new Thread(() -> {
                    while (true) {
                        printToUI(mainChat, serverService.readMessages());
                    }
                }).start();
            }
        });

        nickButton.addActionListener(actionEvent->{
            System.out.println("Сменить ник");
        });


        add(login);
        add(password);
//        add(authButton);
        add(jPanel);
        add(authLabel);

    }

    private void sendMessage(JTextField myMessage) {
        if (!serverService.isConnected()){
            return;
        }
        serverService.sendMessage(myMessage.getText());
       myMessage.setText("");
    }

    private void printToUI(JTextArea mainChat, Message message) {
        mainChat.append("\n");
        mainChat.append((message.getNick() != null ? message.getNick() : "Сервер") + " написал: " + message.getMessage());
        storyMessage.writeMessage("",(message.getNick() != null ? message.getNick() : "Сервер") + " написал: " + message.getMessage());
    }



}
