package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo {
    private String id;
    private String name;
    private String provider;
    private List<String> capabilities;
    private boolean supportsStreaming;
    private boolean supportsFunctions;
    private int contextWindow;
}
