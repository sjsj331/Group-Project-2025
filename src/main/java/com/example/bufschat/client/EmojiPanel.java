package com.example.bufschat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

@SuppressWarnings("unused")
public class EmojiPanel {
    private final JDialog emojiDialog;
    private final PrintWriter out;
    private final String userName;
    private final JPanel chatPanel;
    private final JScrollPane scrollPane;

    public EmojiPanel(JFrame parent, PrintWriter out, String userName, JPanel chatPanel, JScrollPane scrollPane) {
        this.out = out;
        this.userName = userName;
        this.chatPanel = chatPanel;
        this.scrollPane = scrollPane;

        emojiDialog = new JDialog(parent, "이모티콘 선택", false);
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
                if (out != null) {
                    out.println(userName + ": [이모티콘]");
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

    private void appendImageToChatArea(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaled = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaled));
        chatPanel.add(imageLabel);
        chatPanel.revalidate();
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }
}