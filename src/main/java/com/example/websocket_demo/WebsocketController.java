package com.example.websocket_demo;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;


@Controller
public class WebsocketController {
	private final SimpMessagingTemplate messagingTemplate;
	
	@Autowired
	public WebsocketController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}
	
	@MessageMapping("/message")
	public void handleMessage(Message message) {
		System.out.println("Received message from user:"+message.getUser()+": "+message.getUser());
		messagingTemplate.convertAndSend("/topic/messages",message);
		System.out.println("Sent message to /topic/messages:"+message.getUser()+": "+message.getMessage());
	}
	 
	
	
	

}
