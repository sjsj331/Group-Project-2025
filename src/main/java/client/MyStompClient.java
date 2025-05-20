package client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.rsocket.server.RSocketServer.Transport;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.sockjs.client.SockJsClient;

public class MyStompClient {
	
	private StompSession session;
	private String usernae;
	
	public MyStompClient(String username) {
		this.usernae = username;
		
		List<Transport> transports = new ArrayList<>();
	}

}
