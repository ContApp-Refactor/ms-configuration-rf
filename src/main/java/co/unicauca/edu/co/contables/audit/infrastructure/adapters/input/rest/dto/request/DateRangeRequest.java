package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request;

import java.time.ZonedDateTime;

public interface DateRangeRequest {

    ZonedDateTime getDateFrom();

    ZonedDateTime getDateTo();
    
}
