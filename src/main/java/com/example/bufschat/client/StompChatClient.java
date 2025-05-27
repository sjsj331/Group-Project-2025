package com.example.bufschat.client;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.example.bufschat.model.ChatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cloudThread.CloudPanel;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StompChatClient extends JFrame {

    private JTextPane chatArea;
    private JTextField messageField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private String username;
    private StompSession session;
    private EmojiPanel emojiPanel;

    public StompChatClient(String username) {
        this.username = username;

        setTitle("Java 채팅앱 - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
     // 1. RainPanel 생성 //
        CloudPanel rainPanel = new CloudPanel();
        rainPanel.setBounds(0, 0, 800, 600); // 프레임 전체 영역

        // 2. JLayeredPane 설정 //
        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(rainPanel, new Integer(0)); // 가장 아래 레이어에 추가

        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // 채팅 영역
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // 사용자 목록
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setForeground(Color.BLUE);

        JLabel userLabel = new JLabel("현재 들어와있는 사용자 목록", SwingConstants.CENTER);
        userLabel.setForeground(Color.BLUE);

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScrollPane, userPanel);
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(5);
        splitPane.setEnabled(false);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 입력창 + 버튼
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("전송");
        
        

        sendButton.setPreferredSize(new Dimension(100, 60));
        sendButton.setForeground(Color.BLACK);
        sendButton.setBackground(new Color(170, 255, 255));
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        JButton emojiButton = new JButton("😊");
        emojiButton.setPreferredSize(new Dimension(60, 60));
        emojiButton.setForeground(Color.BLACK);
        emojiButton.setBackground(new Color(255, 230, 230));
        emojiButton.setOpaque(true);
        emojiButton.setBorderPainted(false);
        
     // EmojiPanel 생성
        emojiPanel = new EmojiPanel(
            this,
            (dest, msg) -> {
                if (session != null && session.isConnected()) {
                    session.send(dest, msg);
                }
            },
            username,
            chatArea,
            // chatScrollPane는 생성자 내 지역변수라서, 위에 멤버 변수로 분리하거나 아래와 같이 참조 필요
            null // chatScrollPane 변수를 멤버변수로 바꾸거나, 생성자에 전달하는 방법을 사용하세요.
        );
        emojiButton.addActionListener(e -> emojiPanel.show());
        inputPanel.add(emojiButton, BorderLayout.WEST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
        connectStomp();
        
        

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (session != null && session.isConnected()) {
                    session.send("/app/leave", new ChatMessage(username, ""));
                }
                super.windowClosing(e);
            }
        });

        JButton exitButton = new JButton("나가기");
        exitButton.setForeground(Color.DARK_GRAY);
        exitButton.setBackground(Color.LIGHT_GRAY);
        exitButton.setPreferredSize(new Dimension(100, 30));
        exitButton.addActionListener(e -> {
            if (session != null && session.isConnected()) {
                session.send("/app/leave", new ChatMessage(username, ""));
            }
            dispose();
        });

        mainPanel.add(exitButton, BorderLayout.NORTH);
    }

    private void connectStomp() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);

        stompClient.setMessageConverter(converter);

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

        messageField.setText("");
    }

    public static void main(String[] args) {
        String id = JOptionPane.showInputDialog("닉네임 입력:");
        if (id != null && !id.isBlank()) {
            SwingUtilities.invokeLater(() -> new StompChatClient(id));
        }
    }

    // ✅ 내부 핸들러 클래스
    public class MyStompSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            StompChatClient.this.session = session;
            System.out.println("✅ STOMP 연결 성공: " + session.getSessionId());

            session.send("/app/join", new ChatMessage(username, ""));

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

            session.subscribe("/topic/messages", new StompFrameHandler() {
                @Override
                public Class<ChatMessage> getPayloadType(StompHeaders headers) {
                    return ChatMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    ChatMessage msg = (ChatMessage) payload;
                    SwingUtilities.invokeLater(() -> {
                        String line;

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        String time = msg.getTimestamp() != null ? msg.getTimestamp().format(formatter) : "??:??:??";

                        if ("시스템".equals(msg.getSender())) {
                            line = "[" + time + "] [알림] " + msg.getContent() + "\n";
                            chatArea.setForeground(Color.GRAY);
                        } else {
                            line = "[" + time + "] " + msg.getSender() + ": " + msg.getContent() + "\n";
                            chatArea.setForeground(msg.getSender().equals(username) ? Color.BLACK : Color.BLUE);
                        }

                        try {
                            Document doc = chatArea.getDocument();
                            doc.insertString(doc.getLength(), line, null);  // 텍스트 추가
                            chatArea.setCaretPosition(doc.getLength());    // 맨 아래로 스크롤
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        }
    }
}