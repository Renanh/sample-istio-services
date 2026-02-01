package com.github.renanh.callme.domain.event;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Name("ProcessingEvent")
@Label("Processing Event")
@Category("Sample Istio Services")
@Description("Records processing time for request handling")
@Getter
@RequiredArgsConstructor
public class ProcessingEvent extends Event {

    @Label("Event ID")
    @Description("Unique identifier for this processing event")
    private final String eventId;

    @Setter
    @Label("Processing Time (ms)")
    @Description("Time taken to process the request in milliseconds")
    private long processingTimeMs;

    @Setter
    @Label("Status")
    @Description("Processing status (SUCCESS, ERROR, TIMEOUT)")
    private String status;
}
