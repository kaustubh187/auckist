package com.pbop.config;

import com.pbop.repositories.UserRepo;
import com.pbop.services.JwtService;
import com.pbop.services.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSecurity
public class WebSocketSecurityConfig {

    // Inject all required services (for interceptor and SpEL check)
    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;


    // Use constructor injection
    public WebSocketSecurityConfig(
            JwtService jwtService,
            MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        System.out.println("--- WS CONFIG: Defining Authorization Manager ---");

        MessageMatcherDelegatingAuthorizationManager.Builder messages =
                MessageMatcherDelegatingAuthorizationManager.builder();

        messages.simpSubscribeDestMatchers("/topic/auction/*").authenticated()
                .simpSubscribeDestMatchers("/user/queue/errors").permitAll()
                .simpDestMatchers("/app/**").authenticated()
                .anyMessage().denyAll();

        return messages.build();
    }

    @Bean
    public WebSocketMessageBrokerConfigurer webSocketMessageBrokerConfigurer() {
        return new WebSocketMessageBrokerConfigurer() {
            @Override
            public void configureClientInboundChannel(ChannelRegistration registration) {
                System.out.println("--- WS CONFIG: Registering JWT Interceptor ---");
                // Register the custom interceptor
                registration.interceptors(new JwtChannelInterceptor(
                        WebSocketSecurityConfig.this.jwtService, // Use outer class services
                        WebSocketSecurityConfig.this.userDetailsService));
            }
        };
    }


}
