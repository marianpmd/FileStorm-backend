package com.marian.owncloudbackend.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;


@Controller
public class SocketController {

    @MessageMapping("/notify")
    @SendTo("/topic/notify")
    public String broadcastNews(@Payload String message) {
        return message;
    }

    @MessageMapping("/fileUpdate")
    @SendToUser("/queue/fileUpdate")
    public String sendFileUpdate(Principal principal) {
        System.out.println("Called" + principal);
        return  principal.getName();
    }

}
