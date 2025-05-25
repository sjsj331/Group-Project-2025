package com.example.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame {
    @SuppressWarnings("unused")
    private JTextArea chatArea;
    private JTextField inputField;
    private PrintWriter out;
    private String userName;
    private JButton emojiButton;
    private JDialog emojiDialog;
    private JPanel chatPanel;
    private JScrollPane scrollPane;

    public ChatClient(String host, int port) {
        userName = JOptionPane.showInputDialog(this, "닉네임을 입력하세요:");
        if (userName == null || userName.trim().isEmpty()) System.exit(0);
        userName = userName.trim();

        setTitle("채팅 클라이언트 - " + userName);
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(chatPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        emojiButton = new JButton("이모티콘");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(emojiButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty() || out == null) return;
            appendTextToChatArea(userName + ": " + msg);
            out.println(userName + ": " + msg);
            inputField.setText("");
        });

        emojiButton.addActionListener(e -> openEmojiDialog());

        try {
            Socket socket = new Socket(host, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("<시스템>")) {
                            line = line.substring("<시스템>".length()).trim();
                        }
                        final String display = line;
                        SwingUtilities.invokeLater(() -> appendTextToChatArea(display));
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> appendTextToChatArea("[시스템] 연결이 끊어졌습니다."));
                }
            });
            t.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        setVisible(true);
    }

    private void openEmojiDialog() {
        if (emojiDialog == null) {
            emojiDialog = new JDialog(this, "이모티콘 선택", false);
            emojiDialog.setLayout(new FlowLayout());
            emojiDialog.setSize(250, 200);

            String[] emojiPaths = {"src/img/imo1.png", "src/img/imo2.gif", "src/img/imo3.png"};
            for (String path : emojiPaths) {
                ImageIcon icon = new ImageIcon(path);
                Image scaled = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                JButton emojiBtn = new JButton(new ImageIcon(scaled));
                emojiBtn.setPreferredSize(new Dimension(40, 40));

                emojiBtn.addActionListener(e -> {
                    appendImageToChatArea(path);
                    out.println(userName + ": [이모티콘]");
                    emojiDialog.setVisible(false);
                });
                emojiDialog.add(emojiBtn);
            }
        }
        emojiDialog.setLocationRelativeTo(this);
        emojiDialog.setVisible(true);
    }

    private void appendTextToChatArea(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.PLAIN, 14));
        chatPanel.add(label);
        chatPanel.revalidate();
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void appendImageToChatArea(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaled = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 이모티콘 크기 줄이기
        JLabel imageLabel = new JLabel(new ImageIcon(scaled));
        chatPanel.add(imageLabel);
        chatPanel.revalidate();
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient("127.0.0.1", 23456));
    }
}
