package com.example.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChatServer {
    private static final int PORT = 23456;

    public static void main(String[] args) {
        System.out.println("=== 채팅 서버 시작 ===");
        List<PrintWriter> outList = Collections.synchronizedList(new ArrayList<>());
        List<String> nameList = Collections.synchronizedList(new ArrayList<>());

        // try-with-resources로 서버소켓 자동 클로징
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 포트 " + PORT + "에서 실행 중입니다...");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("클라이언트 접속: " + socket);
                    new ChatThread(socket, outList, nameList).start();
                } catch (IOException e) {
                    System.err.println("클라이언트 연결 수락 오류: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("서버 소켓 생성 실패: " + e.getMessage());
        }
    }
}

class ChatThread extends Thread {
    private final Socket socket;
    private final List<PrintWriter> outList;
    private final List<String> nameList;
    private PrintWriter out;
    private BufferedReader in;

    public ChatThread(Socket socket, List<PrintWriter> outList, List<String> nameList) {
        this.socket = socket;
        this.outList = outList;
        this.nameList = nameList;
    }

    public ChatThread(Test1 test1) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public void run() {
        String userName = null;
        // 각 클라이언트 연결에서 발생할 수 있는 모든 예외를 잡아 서버가 멈추지 않도록 처리
        try {
            // 스트림 초기화 및 autoFlush 설정
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // 첫 메시지를 닉네임으로 사용
            out.println("닉네임을 입력하세요:");
            userName = in.readLine();
            if (userName == null || userName.isEmpty()) {
                out.println("잘못된 닉네임, 연결을 종료합니다.");
                return;
            }
            nameList.add(userName);
            outList.add(out);

            // 환영 및 입장 브로드캐스트
            out.println("<시스템> " + userName + "님, 환영합니다!");
            broadcast("<시스템> " + userName + "님이 입장했습니다. 참여자 수: " + nameList.size());

            String line;
            while ((line = in.readLine()) != null) {
                broadcast(userName + ": " + line);
            }
        } catch (IOException e) {
            System.err.println("통신 오류(사용자:" + userName + "): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("알 수 없는 오류 발생: " + e.getMessage());
        } finally {
            // 퇴장 처리
            if (userName != null) {
                nameList.remove(userName);
                outList.remove(out);
                broadcast("<시스템> " + userName + "님이 퇴장했습니다. 참여자 수: " + nameList.size());
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("소켓 종료 실패: " + e.getMessage());
            }
        }
    }

    private void broadcast(String message) {
        synchronized (outList) {
            Iterator<PrintWriter> iter = outList.iterator();
            while (iter.hasNext()) {
                PrintWriter writer = iter.next();
                try {
                    writer.println(message);
                } catch (Exception ex) {
                    // 전송 실패 발생 시 해당 클라이언트 제거
                    iter.remove();
                }
            }
        }
    }
}