package com.lms.lms.config;

import com.lms.lms.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }


        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            try {
                String authHeader = accessor.getFirstNativeHeader("Authorization");

                System.out.println("authHeader" + authHeader);

//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                throw new IllegalArgumentException("Missing Authorization header");
//            }

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    accessor.setLeaveMutable(true);
                    accessor.setUser(null);
                    return null;
                }


                String token = authHeader.substring(7);
                Authentication auth = jwtService.getAuthentication(token);

                accessor.setUser(auth);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ; // 🔍 YOU WILL NOW SEE THE REAL ERROR
                return null; // ❗ reject CONNECT
            }
        }


        return message;
    }
}
