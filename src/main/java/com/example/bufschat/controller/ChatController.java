package com.example.bufschat.controller;

import com.example.bufschat.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final Set<String> userSet = ConcurrentHashMap.newKeySet();

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/join")
    public synchronized void handleJoin(ChatMessage msg) {
        // ⚫️ 사용자 목록을 먼저 갱신
        userSet.add(msg.getSender());
        template.convertAndSend("/topic/users", new ArrayList<>(userSet));

        // ✅ 입장 메시지를 나중에 전송
        ChatMessage joinNotice = new ChatMessage("시스템", msg.getSender() + "님이 입장했습니다.");
        template.convertAndSend("/topic/messages", joinNotice);
    }

    @MessageMapping("/send")
    public void handleMessage(ChatMessage msg) {
        System.out.println("📩 받은 메시지: " + msg.getSender() + ": " + msg.getContent());
        template.convertAndSend("/topic/messages", msg);
    }

    @MessageMapping("/requestUsers")
    @SendToUser("/queue/users")
    public List<String> handleUserListRequest() {
        return new ArrayList<>(userSet);
    }

    @MessageMapping("/leave")
    public void handleLeave(ChatMessage msg) {
        userSet.remove(msg.getSender());
        template.convertAndSend("/topic/users", new ArrayList<>(userSet));

        ChatMessage leaveMsg = new ChatMessage("시스템", msg.getSender() + "님이 퇴장했습니다.");
        template.convertAndSend("/topic/messages", leaveMsg);
    }
}