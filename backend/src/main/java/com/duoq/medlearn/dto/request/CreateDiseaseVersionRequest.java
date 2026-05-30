package com.duoq.medlearn.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDiseaseVersionRequest {

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 2000, message = "Note must be at most 2000 characters")
    private String note;
}
