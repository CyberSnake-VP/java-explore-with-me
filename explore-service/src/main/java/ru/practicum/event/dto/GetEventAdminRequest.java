package ru.practicum.event.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class GetEventAdminRequest {
    private List<Long> userIds;
    private List<State> states;
    private List<Long> categoryIds;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;

    public static GetEventAdminRequest of(final List<Long> userIds,
                                          final List<String> states,
                                          final List<Long> categoryIds,
                                          final LocalDateTime rangeStart,
                                          final LocalDateTime rangeEnd,
                                          final Integer from,
                                          final Integer size) {
        GetEventAdminRequest request = new GetEventAdminRequest();
        if (userIds != null) {
            request.setUserIds(userIds);
        }
        if (states != null) {
            request.setStates(states.stream().map(State::valueOf).toList());
        }
        if (categoryIds != null) {
            request.setCategoryIds(categoryIds);
        }
        if (rangeStart != null) {
            request.setRangeStart(rangeStart);
        }
        if (rangeEnd != null) {
            request.setRangeEnd(rangeEnd);
        }
        request.setFrom(from);
        request.setSize(size);
        return request;
    }
}
