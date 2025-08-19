package ru.practicum.event.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class GetEventRequest {
    private String text;
    private List<Long> categoriesIds;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private Sort sort;
    public Integer from;
    public Integer size;

    public static GetEventRequest of(final String text,
                                     final List<Long> categoriesIds,
                                     final Boolean paid,
                                     final LocalDateTime rangeStart,
                                     final LocalDateTime rangeEnd,
                                     final Boolean onlyAvailable,
                                     final String sort,
                                     final Integer from,
                                     final Integer size) {
        GetEventRequest req = new GetEventRequest();
        if(text != null) {
            req.setText(text);
        }
        if(categoriesIds != null) {
            req.setCategoriesIds(categoriesIds);
        }
        if(paid != null) {
            req.setPaid(paid);
        }
        if(rangeStart != null) {
            req.setRangeStart(rangeStart);
        }
        if(rangeEnd != null) {
            req.setRangeEnd(rangeEnd);
        }
        if(onlyAvailable != null) {
            req.setOnlyAvailable(onlyAvailable);
        }
        if(sort != null) {
            req.setSort(Sort.valueOf(sort));
        }
        req.setFrom(from);
        req.setSize(size);
        return req;
    }

    public enum Sort {EVENT_DATE, VIEWS}
}
