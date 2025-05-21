package 연습01;

// ChatClient.java

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static String nickname;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        System.out.println("✅ 서버에 연결되었습니다.");

        nickname = generateNickname();
        System.out.println("✨ 나의 닉네임: " + nickname);
        System.out.println("상대방과 대화하세요!");

        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // 상대방 메시지 수신 쓰레드
        Thread reader = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException e) {
                System.out.println("❗ 연결이 종료되었습니다.");
            }
        });
        reader.start();

        // 내 메시지 전송
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String msg = scanner.nextLine();
            out.println(nickname + ": " + msg);
        }
    }

    public static String generateNickname() {
        String[] adjectives = {"감성", "귀여운", "용감한", "조용한", "명랑한", "슬픈", "행복한"};
        String[] animals = {"고양이", "강아지", "곰", "토끼", "여우", "사자", "호랑이"};

        String adj = adjectives[(int)(Math.random() * adjectives.length)];
        String animal = animals[(int)(Math.random() * animals.length)];
        int number = 1000 + (int)(Math.random() * 9000);

        return adj + animal + "#" + number;
    }
}
