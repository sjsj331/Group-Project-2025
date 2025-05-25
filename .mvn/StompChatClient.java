package com.example.bufschat.client;

import com.example.bufschat.model.ChatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;




public class StompChatClient extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private String username;
    private StompSession session;

    public StompChatClient(String username) {
        this.username = username;

        setTitle("Java 채팅앱 - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // 🔵 채팅 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // 🔵 사용자 목록
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setForeground(Color.BLUE); // 사용자 목록 파란색

        JLabel userLabel = new JLabel("현재 들어와있는 사용자 목록", SwingConstants.CENTER);
        userLabel.setForeground(Color.BLUE); // 레이블 파란색

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        // 🔧 비율 조정된 splitPane (80% : 20%)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScrollPane, userPanel);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerSize(5);
        splitPane.setEnabled(false); // 사용자 비율 조정 막기

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 입력창 + 버튼
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("전송");

        sendButton.setForeground(Color.BLACK);
        sendButton.setBackground(new Color(170, 255, 255));
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
        connectStomp();
    }

    private void connectStomp() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        Future<StompSession> future = stompClient.connect(
            "ws://localhost:8080/chat",
            new WebSocketHttpHeaders(),
            new MyStompSessionHandler()
        );

        try {
            this.session = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "❌ STOMP 서버 연결 실패:\n" + e.getMessage(),
                        "연결 오류", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

        private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty()) return;

        if (session == null || !session.isConnected()) {
            JOptionPane.showMessageDialog(this, "서버에 아직 연결되지 않았습니다.", "연결 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ChatMessage msg = new ChatMessage(username, content);
        session.send("/app/send", msg);

        messageField.setText(""); // 입력창만 비워주기
    }



    public static void main(String[] args) {
        String id = JOptionPane.showInputDialog("닉네임 입력:");
        if (id != null && !id.isBlank()) {
            SwingUtilities.invokeLater(() -> new StompChatClient(id));
        }
    }

    public class MyStompSessionHandler extends StompSessionHandlerAdapter {
   @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    StompChatClient.this.session = session;
    System.out.println("✅ STOMP 연결 성공: " + session.getSessionId());

    // ✅ 1. 서버에 접속 알림
    session.send("/app/join", new ChatMessage(username, ""));

    // ✅ 2. 채팅 메시지 구독
    session.subscribe("/topic/messages", new StompFrameHandler() {
        @Override
        public Class<ChatMessage> getPayloadType(StompHeaders headers) {
            return ChatMessage.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            ChatMessage msg = (ChatMessage) payload;
            SwingUtilities.invokeLater(() -> {
                String line = msg.getSender() + ": " + msg.getContent() + "\n";
                if (!msg.getSender().equals(username)) {
                    chatArea.setForeground(Color.BLUE);
                } else {
                    chatArea.setForeground(Color.BLACK);
                }
                chatArea.append(line);
                });
            }
    });

    // ✅ 3. 사용자 목록 구독
    session.subscribe("/topic/users", new StompFrameHandler() {
        @Override
        public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return new TypeReference<List<String>>() {}.getType();
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            @SuppressWarnings("unchecked")
            List<String> users = (List<String>) payload;
            SwingUtilities.invokeLater(() -> {
                userListModel.clear();
                for (String u : users) {
                    userListModel.addElement(u);
                }
            });
        }
    });
}

    public static class ChatMessage {
        private String sender;
        private String content;

        public ChatMessage() {}
        public ChatMessage(String sender, String content) {
            this.sender = sender;
            this.content = content;
        }

        public String getSender() { return sender; }
        public String getContent() { return content; }
        public void setSender(String sender) { this.sender = sender; }
        public void setContent(String content) { this.content = content; }
    }
    }
}