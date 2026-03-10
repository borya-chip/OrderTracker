package com.order.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NPlusOneDemoResponse {

    private long ordersCount;
    private long queriesWithNPlusOne;
    private long queriesWithEntityGraph;
    private long savedQueries;
}
