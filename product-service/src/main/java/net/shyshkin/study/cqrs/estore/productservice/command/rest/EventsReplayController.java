package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.EventProcessingConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/management")
@RequiredArgsConstructor
public class EventsReplayController {

    private final EventProcessingConfiguration eventProcessingConfiguration;

    @PostMapping("/eventProcessor/{processorName}/reset")
    public void resetEvents(@PathVariable String processorName) {

    }
}
