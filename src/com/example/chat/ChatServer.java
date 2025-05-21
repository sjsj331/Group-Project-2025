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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChatServer { 
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9998);
        // 공유 객체에서 쓰레드에 안전한 리스트를 만든다.
        List<PrintWriter> outList = Collections.synchronizedList(new ArrayList<>());
        while (true) {
            Socket socket = serverSocket.accept(); // 클라이언트와 통신하기위해
            System.err.println("접속 : "+ socket);

            ChatThread chatThread = new ChatThread(socket, outList); //serverSocket이 접솔할때마다 socket을 가지고 ChatThread한테 별도로 이 socket과 통신하라고 하는 것
            chatThread.start(); 
        }
    }

}

class ChatThread extends Thread{
    
    private Socket socket;
    private List<PrintWriter> outList;
    private PrintWriter out;
    private BufferedReader in;

    public ChatThread(Socket socket, List<PrintWriter> outList){ // 이 Socket은 현재 연결되어있는 클라이언트랑만 통신하는 것 (하기위한 것)
        this.socket = socket;
        this.outList = outList;
        //1. socket으로 부터 읽어들일 수 있는 객체를 얻는다.
        //2. soket에게 쓰기 위한 객체를 얻는다. (현제 연결된 클라이언트에게 쓰는 객체)
        
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            outList.add(out);
        }catch(Exception ex){
            ex.printStackTrace();

        }
    }
    public void run(){

        //3. 클라이언트가 보낸 메시지를 읽는다.
        //4. 접속된 모든 클라이언트에게 매시지를 보낸다. (현재 접속된 모든 클라이언트에게 쓸수 있는 객체가 필요하다.)

       String Line = null;
       try {
            while (( Line = in.readLine())!= null){
                for(int i = 0; i< outList.size(); i++){
                    PrintWriter o = outList.get(i);
                    o.println(Line);
                    o.flush();
                }

             }

        }catch(Exception ex){
            ex.printStackTrace();
        }finally{ // 접속이 끊어질때
            try{
                outList.remove(out);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            for(int i = 0; i< outList.size(); i++){
                PrintWriter o = outList.get(i);
                o.println("어떤 클라이언트가 접속이 끊어졌어요.");
                o.flush();
            }
        }
        try {
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
       
       //한줄을 무한으로 읽어준다
        

    }
}