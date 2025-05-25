package com.example.chat;

import java.util.Map;
import javax.swing.*;
import java.awt.*;

class ChatMessage {
    private String sender;
    private String content;
    private String timestamp;

    public ChatMessage() {}

    public ChatMessage(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

class EmojiUtil {
    private static final Map<String, String> emojiMap = Map.of(
        ":smile:", "\uD83D\uDE0A",  // 😊
        ":sad:", "\uD83D\uDE22",    // 😢
        ":heart:", "\u2764\uFE0F", // ❤️
        ":thumbsup:", "\uD83D\uDC4D" // 👍
    );

    public static String parseEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}

class ChatService {
    private JTextArea textArea;

    public ChatService(JTextArea textArea) {
        this.textArea = textArea;
        this.textArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
    }

    public void sendMessage(ChatMessage message) {
        String parsedContent = EmojiUtil.parseEmojis(message.getContent());
        message.setContent(parsedContent);
        textArea.append("[" + message.getSender() + "]: " + message.getContent() + "\n");
    }
}

// 사용 예시:
// JTextArea textArea = new JTextArea();
// ChatService chatService = new ChatService(textArea);
// chatService.sendMessage(new ChatMessage("dd", ":smile:", "2025-05-25"));