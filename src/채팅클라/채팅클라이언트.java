package 채팅클라;

import java.io.*;
import java.net.*;

public class 채팅클라이언트 {
    private static final String 서버주소 = "localhost";
    private static final int 포트번호 = 12345;

    public static void main(String[] args) {
        try (
            Socket 소켓 = new Socket(서버주소, 포트번호);
            BufferedReader 입력 = new BufferedReader(new InputStreamReader(소켓.getInputStream()));
            PrintWriter 출력 = new PrintWriter(소켓.getOutputStream(), true);
            BufferedReader 사용자입력 = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("서버에 연결됨.");

            // 수신용 스레드
            Thread 수신스레드 = new Thread(() -> {
                String 받은메시지;
                try {
                    while ((받은메시지 = 입력.readLine()) != null) {
                        System.out.println(받은메시지);
                    }
                } catch (IOException 예외) {
                    System.out.println("서버와 연결이 끊어졌습니다.");
                }
            });
            수신스레드.start();

            // 사용자 입력 -> 서버로 전송
            String 입력메시지;
            while ((입력메시지 = 사용자입력.readLine()) != null) {
                출력.println(입력메시지);
            }

        } catch (IOException 예외) {
            예외.printStackTrace();
        }
    }
}