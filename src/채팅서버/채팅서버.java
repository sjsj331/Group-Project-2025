package 채팅서버;

import java.io.*;
import java.net.*;
import java.util.*;

public class 채팅서버 {
    private static final int 포트번호 = 12345;
    private static Set<클라이언트정보> 클라이언트목록 = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("서버가 시작되었습니다. 포트: " + 포트번호);

        try (ServerSocket 서버소켓 = new ServerSocket(포트번호)) {
            while (true) {
                Socket 클라이언트소켓 = 서버소켓.accept();
                System.out.println("클라이언트 접속: " + 클라이언트소켓);
                new Thread(new 클라이언트처리기(클라이언트소켓)).start();
            }
        } catch (IOException 예외) {
            예외.printStackTrace();
        }
    }

    private static class 클라이언트정보 {
        String 닉네임;
        PrintWriter 출력;

        클라이언트정보(String 닉네임, PrintWriter 출력) {
            this.닉네임 = 닉네임;
            this.출력 = 출력;
        }
    }

    private static class 클라이언트처리기 implements Runnable {
        private Socket 소켓;
        private BufferedReader 입력;
        private PrintWriter 출력;
        private 클라이언트정보 내정보;

        public 클라이언트처리기(Socket 소켓) {
            this.소켓 = 소켓;
        }

        public void run() {
            try {
                입력 = new BufferedReader(new InputStreamReader(소켓.getInputStream()));
                출력 = new PrintWriter(소켓.getOutputStream(), true);

                출력.println("닉네임을 입력하세요:");
                String 닉네임 = 입력.readLine();
                내정보 = new 클라이언트정보(닉네임, 출력);
                클라이언트목록.add(내정보);

                모두에게보내기("[알림] " + 닉네임 + " 님이 입장했습니다.");

                String 메시지;
                while ((메시지 = 입력.readLine()) != null) {
                    모두에게보내기("[" + 닉네임 + "] " + 메시지);
                }
            } catch (IOException 예외) {
                System.out.println("클라이언트 연결 종료");
            } finally {
                try {
                    if (내정보 != null) {
                        클라이언트목록.remove(내정보);
                        모두에게보내기("[알림] " + 내정보.닉네임 + " 님이 퇴장했습니다.");
                    }
                    소켓.close();
                } catch (IOException 예외) {
                    예외.printStackTrace();
                }
            }
        }

        private void 모두에게보내기(String 메시지) {
            synchronized (클라이언트목록) {
                for (클라이언트정보 클 : 클라이언트목록) {
                    클.출력.println(메시지);
                }
            }
        }
    }
}