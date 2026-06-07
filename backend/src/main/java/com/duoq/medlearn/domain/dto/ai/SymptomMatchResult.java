package com.duoq.medlearn.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class SymptomMatchResult {
    private Long diseaseId;
    private String diseaseName;
    private String diseaseSlug;
    private Integer matchCount;           // Số symptom match được
    private Double matchScore;            // Điểm match (đã tính weight)
    private List<String> matchedSymptoms; // Danh sách symptom đã match
    private String shortDescription;      // Mô tả ngắn (lấy từ section đầu tiên)
}