package com.duoq.medlearn.ai.prompt.entity;

import com.duoq.medlearn.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prompt_test_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prompt_code", nullable = false)
    private String promptCode;

    @Column(nullable = false)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String variables;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    private User executedBy;

    @Column(name = "executed_at")
    private OffsetDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        executedAt = OffsetDateTime.now();
    }
}
