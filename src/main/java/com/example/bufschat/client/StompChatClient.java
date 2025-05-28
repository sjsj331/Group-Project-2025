package com.example.bufschat.client;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.*;
import javax.swing.border.Border;
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

        CloudPanel rainPanel = new CloudPanel();
        rainPanel.setBounds(0, 0, 800, 600);

        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(rainPanel, new Integer(0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 245, 239));
        setContentPane(mainPanel);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(243, 237, 231));
        chatArea.setForeground(new Color(68, 44, 33));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setForeground(new Color(68, 44, 33));
        userList.setBackground(new Color(243, 237, 231));

        JLabel userLabel = new JLabel("현재 들어와있는 사용자 목록", SwingConstants.CENTER);
        userLabel.setForeground(new Color(68, 44, 33));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(250, 245, 239));
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        ImageIcon logoIcon = new ImageIcon("src/main/resources/img/logo.png");
        Image scaled = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // fh
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));
        userPanel.add(logoLabel, BorderLayout.SOUTH);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScrollPane, userPanel);
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(5);
        splitPane.setEnabled(false);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(250, 245, 239));
        messageField = new JTextField();
        messageField.setBackground(Color.WHITE);
        messageField.setForeground(new Color(68, 44, 33));

        JButton sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(100, 60));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(new Color(106, 60, 44));
        sendButton.setOpaque(true);

        JButton emojiButton = new JButton("😊");
        emojiButton.setPreferredSize(new Dimension(60, 60));
        emojiButton.setForeground(Color.BLACK);
        emojiButton.setBackground(new Color(255, 230, 230));
        emojiButton.setOpaque(true);

        emojiPanel = new EmojiPanel(this, (dest, msg) -> {
            if (session != null && session.isConnected()) {
                session.send(dest, msg);
            }
        }, username, chatArea, null);

        emojiButton.addActionListener(e -> emojiPanel.show());

        inputPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
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
        exitButton.setForeground(Color.WHITE);
        exitButton.setBackground(new Color(164, 117, 81));
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
        ImageIcon rawIcon = new ImageIcon("src/main/resources/img/logo.png");
        Image scaled = rawIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);

        UIManager.put("Button.background", new Color(212, 166, 120)); // 밝은 갈색
        UIManager.put("Button.foreground", Color.DARK_GRAY);         // 글씨색


        String id = (String) JOptionPane.showInputDialog(
            null,
            "닉네임을 입력하세요:",
            "로그인",
            JOptionPane.QUESTION_MESSAGE,
            icon,
            null,
            null
        );
        if (id != null && !id.isBlank()) {
            SwingUtilities.invokeLater(() -> new StompChatClient(id));
        }
    }

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
                    emojiPanel.renderIncomingMessage1(msg);  // 출력하지 않도록 수정해야 함 (chatArea 조작 제거)
                }
            });
        }
    }
}
