package com.pbop.config;

import com.pbop.services.JwtService;
import com.pbop.services.MyUserDetailsService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    // Inject dependencies via constructor (used in WebSocketSecurityConfig)
    public JwtChannelInterceptor(JwtService jwtService, MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Only process the initial CONNECT command
        //assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            System.out.println("--- WS INTERCEPTOR: Processing CONNECT frame ---");

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String jwt = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);

                try {
                    // Validate token and extract username
                    username = jwtService.extractUsername(jwt);
                    System.out.println("WS INTERCEPTOR: Token valid. User: " + username);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Create and set the Authentication token into the WebSocket session
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        System.out.println("WS INTERCEPTOR: Principal set for user: " + username);
                    }
                } catch (Exception e) {
                    System.err.println("WS INTERCEPTOR: JWT AUTH FAILED! Error: " + e.getMessage());
                    // Deny connection by not setting the user principal
                }
            } else {
                System.out.println("WS INTERCEPTOR: Authorization header missing/invalid.");
            }
        }

        // This is where the SUBSCRIBE command passes through, and the principal is already set.
        return message;
    }
}