package cloudThread;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CloudPanel extends JPanel implements Runnable {
    private final List<Cloud> clouds = new ArrayList<>();
    private final int cloudCount = 5;
    private boolean running = true;

    public CloudPanel() {
        setOpaque(false); // 배경 투명
        for (int i = 0; i < cloudCount; i++) {
            clouds.add(new Cloud());
        }
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            for (Cloud cloud : clouds) {
                cloud.drift(getWidth());
            }
            repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopClouds() {
        running = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Cloud cloud : clouds) {
            g.setColor(new Color(102, 255, 255, 200)); // 반투명 흰색
            g.fillOval(cloud.x, cloud.y, cloud.width, cloud.height); // 본체
            g.fillOval(cloud.x - 20, cloud.y + 10, cloud.width, cloud.height); // 옆구름
            g.fillOval(cloud.x + 20, cloud.y + 10, cloud.width, cloud.height); // 옆구름
        }
    }

    static class Cloud {
        int x, y, width, height, speed;

        public Cloud() {
            reset(true);
        }

        public void drift(int panelWidth) {
            x += speed;
            if (x > panelWidth + 100) {
                reset(false);
            }
        }

        private void reset(boolean initial) {
            x = initial ? (int) (Math.random() * 800) : -200;
            y = 20 + (int) (Math.random() * 150);
            width = 60 + (int) (Math.random() * 40);
            height = 30 + (int) (Math.random() * 20);
            speed = 1 + (int) (Math.random() * 2); // 천천히 이동
        }
    }
}
