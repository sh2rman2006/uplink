package tech.sh2rman.coreservice.websocket.config;

//@Configuration
//@EnableWebSocketSecurity
//public class WebSocketSecurityConfig {
//
//    @Bean
//    AuthorizationManager<Message<?>> messageAuthorizationManager(
//            MessageMatcherDelegatingAuthorizationManager.Builder messages
//    ) {
//        messages
//                .nullDestMatcher().authenticated()
//
//                .simpSubscribeDestMatchers("/user/**").authenticated()
//                .simpSubscribeDestMatchers("/topic/**").authenticated()
//
//                .simpDestMatchers("/app/**").authenticated()
//
//                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
//
//                .anyMessage().denyAll();
//
//        return messages.build();
//    }
//}


