package com.paseape.apipaseape.infrastructure.shared;

import org.springframework.stereotype.Service;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;

import java.util.Date;

@Service
public class RequestService {

    public HeaderDto getResponseHeader(Date startDatetime, Date endDatetime) {
        long duration = 0L;
        if (startDatetime != null && endDatetime != null) {
            duration = endDatetime.getTime() - startDatetime.getTime();
        }
        return HeaderDto.builder()
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .durationMs(duration)
                .build();
    }
}
