package com.example.websocket_demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsoketConfig implements WebSocketMessageBrokerConfigurer{
	
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");                  // 메시지를 "구독자(브라우저)"에게 전달할 때 사용할 브로커 주소. /topic으로 시작하는 주소를 브로드캐스트용 주소로 사용한다.
		config.setApplicationDestinationPrefixes("/app");     // 클라이언트가 서버에 메시지를 보낼 때 사용하는 주소의 접두어. 예 : /app/chat.sendMessage
	}
	
	
	public void registerStompEndpoints(StompEndpointRegistry registry) {     
		registry.addEndpoint("/ws").withSockJS();
		/*
		 * S : Simple
		 * T : Text
		 * O : Oriented
		 * M : Message
		 * P : Protocol
		 */
	}
}
