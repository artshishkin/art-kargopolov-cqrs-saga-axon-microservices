package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/management")
@RequiredArgsConstructor
public class EventsReplayController {

    private final EventProcessingConfiguration eventProcessingConfiguration;

    @PostMapping("/eventProcessor/{processorName}/reset")
    public ResponseEntity<String> resetEvents(@PathVariable String processorName) {
        Optional<TrackingEventProcessor> eventProcessorOptional = eventProcessingConfiguration.eventProcessor(processorName, TrackingEventProcessor.class);
        if (eventProcessorOptional.isPresent()) {

            TrackingEventProcessor eventProcessor = eventProcessorOptional.get();
            eventProcessor.shutDown();
            eventProcessor.resetTokens();
            eventProcessor.start();

            return ResponseEntity.ok(String.format("The event processor with the name `%s` has been reset", processorName));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(String.format("The event processor with the name `%s` is not a tracking event processor", processorName));
        }
    }
}
