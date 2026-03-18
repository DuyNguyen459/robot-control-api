package com.example.robotcontrolsystembackend.application.dto.response.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchIngestResponse {
    private int total;
    private int successCount;
    private int failureCount;

    @Builder.Default
    private List<BatchIngestItemResult> items = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchIngestItemResult {
        private int index;
        private boolean success;
        private String eventId;
        private String error;
    }
}
