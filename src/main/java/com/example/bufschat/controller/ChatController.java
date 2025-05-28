package com.example.bufschat.controller;

import com.example.bufschat.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    // ✅ 모든 사용자 정보를 저장하는 동시 안전 Set
    private final Set<String> userSet = ConcurrentHashMap.newKeySet();

    @Autowired
    private SimpMessagingTemplate template;

    // ✅ 입장 처리
    @MessageMapping("/join")
    public synchronized void handleJoin(ChatMessage msg) {
        userSet.add(msg.getSender());

        // ✅ 사용자 목록을 약간의 지연 후 브로드캐스트 (200ms 딜레이)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                template.convertAndSend("/topic/users", new ArrayList<>(userSet));
            }
        }, 200);

        // 입장 시스템 메시지 전송
        ChatMessage joinNotice = new ChatMessage("시스템", msg.getSender() + "님이 입장했습니다.");
        joinNotice.setTimestamp(LocalDateTime.now());
        template.convertAndSend("/topic/messages", joinNotice);
    }

    // ✅ 채팅 메시지 처리
    @MessageMapping("/send")
    public void handleMessage(ChatMessage msg) {
        msg.setTimestamp(LocalDateTime.now());
        template.convertAndSend("/topic/messages", msg);
    }

    // ✅ 사용자 목록 요청 처리
    @MessageMapping("/requestUsers")
    public List<String> handleUserListRequest() {
        return new ArrayList<>(userSet);
    }

    // ✅ 퇴장 처리
    @MessageMapping("/leave")
    public synchronized void handleLeave(ChatMessage msg) {
        userSet.remove(msg.getSender());

        // 사용자 목록 전체에게 브로드캐스트
        template.convertAndSend("/topic/users", new ArrayList<>(userSet));

        // 퇴장 시스템 메시지 전송
        ChatMessage leaveNotice = new ChatMessage("시스템", msg.getSender() + "님이 퇴장했습니다.");
        leaveNotice.setTimestamp(LocalDateTime.now());
        template.convertAndSend("/topic/messages", leaveNotice);
    }

    @RestController
    public class RootController {
        @GetMapping("/")
        public String home() {
            return "Bufschat API is up and running!";
        }
    }

}