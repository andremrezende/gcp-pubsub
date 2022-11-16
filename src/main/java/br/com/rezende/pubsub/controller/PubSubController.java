package br.com.rezende.pubsub.controller;

import br.com.rezende.pubsub.service.PubSubService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PubSubController {
    private final PubSubService pubSubService;

    public PubSubController(PubSubService pubSubService) {
        this.pubSubService = pubSubService;
    }
    @GetMapping("/publish")
    public String publish() throws Exception {
        return pubSubService.publish();
    }

    @GetMapping("/receive")
    public String receive() throws Exception {
        return pubSubService.receive();
    }
}
