package com.example.chat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class ChatClient { 
    public static void main(String[] args) throws Exception {
         System.out.print("닉네임을 입력하세요: ");
         BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
         String name = keyboard.readLine().trim();

         Socket socket = new Socket("127.0.0.1", 23456);
         PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

          // 3) 서버로부터 오는 메시지를 별도의 스레드에서 읽기
         new InputThread(in).start();

         // 4) 사용자 입력을 읽고, 항상 "닉네임: 메시지" 형식으로 전송
         String line;
         while ((line = keyboard.readLine()) != null) {
             if (line.isEmpty()) continue;  // 빈줄은 전송하지 않음
             out.println(name + ": " + line);
        }

     socket.close();
    }

}



class InputThread extends Thread {
    private BufferedReader in;
    public InputThread(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    
}