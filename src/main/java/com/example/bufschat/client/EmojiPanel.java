package com.example.bufschat.client;

import com.example.bufschat.model.ChatMessage;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class EmojiPanel {
    private final JDialog emojiDialog;
    private final BiConsumer<String, Object> sendFunction;
    private final String userName;
    private final JTextPane chatArea;
    private final JScrollPane scrollPane;
    private final Map<String, ImageIcon> emojiIconMap = new HashMap<>();

    public EmojiPanel(JFrame parent, BiConsumer<String, Object> sendFunction, String userName, JTextPane chatArea, JScrollPane scrollPane) {
        this.sendFunction = sendFunction;
        this.userName = userName;
        this.chatArea = chatArea;
        this.scrollPane = scrollPane;

        emojiDialog = new JDialog(parent, "이모티콘 선택", false);
        emojiDialog.setLayout(new FlowLayout());
        emojiDialog.setSize(250, 200);

        String[] emojiPaths = {"/img/imo1.png", "/img/imo2.gif", "/img/imo3.png", "/img/imo4.png"};
        for (String path : emojiPaths) {
            URL imageUrl = getClass().getResource(path);
            if (imageUrl == null) {
                System.err.println("이미지 파일을 찾을 수 없습니다: " + path);
                continue;
            }
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            emojiIconMap.put(path, scaledIcon);

            JButton emojiBtn = new JButton(scaledIcon);
            emojiBtn.setPreferredSize(new Dimension(40, 40));

            emojiBtn.addActionListener(e -> {
                if (sendFunction != null) {
                    sendFunction.accept("/app/send", new ChatMessage(userName, "[이모티콘]" + path));
                }
                emojiDialog.setVisible(false);
            });
            emojiDialog.add(emojiBtn);
        }
    }

    public void show() {
        emojiDialog.setLocationRelativeTo(null);
        emojiDialog.setVisible(true);
    }

    private void insertEmojiToChatArea(String prefix, ImageIcon icon) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), prefix, null);
            Style style = chatArea.addStyle("emoji", null);
            StyleConstants.setIcon(style, icon);
            doc.insertString(doc.getLength(), "ignored", style);
            doc.insertString(doc.getLength(), "\n", null);

            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderIncomingMessage(com.example.bufschat.client.StompChatClient.MyStompSessionHandler.ChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            String content = msg.getContent();
            if (content.startsWith("[이모티콘]")) {
                String path = content.substring("[이모티콘]".length());
                ImageIcon icon = emojiIconMap.get(path);
                if (icon != null) {
                    insertEmojiToChatArea(msg.getSender() + ": ", icon);
                } else {
                    try {
                        StyledDocument doc = chatArea.getStyledDocument();
                        doc.insertString(doc.getLength(), msg.getSender() + ": [이모티콘 표시 실패: " + path + "]\n", null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    StyledDocument doc = chatArea.getStyledDocument();
                    doc.insertString(doc.getLength(), msg.getSender() + ": " + content + "\n", null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void renderIncomingMessage(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            String content = message.getContent();
            if (content.startsWith("[이모티콘]")) {
                String path = content.substring("[이모티콘]".length());
                ImageIcon icon = emojiIconMap.get(path);
                if (icon != null) {
                    insertEmojiToChatArea(message.getSender() + ": ", icon);
                } else {
                    try {
                        StyledDocument doc = chatArea.getStyledDocument();
                        doc.insertString(doc.getLength(), message.getSender() + ": [이모티콘 표시 실패: " + path + "]\n", null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    StyledDocument doc = chatArea.getStyledDocument();
                    doc.insertString(doc.getLength(), message.getSender() + ": " + content + "\n", null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}