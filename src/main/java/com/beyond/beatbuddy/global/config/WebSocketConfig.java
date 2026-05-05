package com.beyond.beatbuddy.global.config;

import com.beyond.beatbuddy.global.security.StompHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler jwtChannelInterceptor;

    public WebSocketConfig(StompHandler jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    //해당 경로로 최초 핸드쉐이크 요청 들어옴
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // /sub으로 시작하는 경로는 메시지 브로커가 직접 처리
        registry.setApplicationDestinationPrefixes("/pub");  // /pub으로 시작하는 경로는 애플리케이션의 @MessageMapping 메소드로 연결
    }

    // 클라이언트가 메시지를 보낼 때 거치는 ChannelInterceptor를 등록
    // CONNECT, SEND 등의 메시지가 컨트롤러나 브로커로 전달되기 전에 가로채서 JWT 인증 수행
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}
