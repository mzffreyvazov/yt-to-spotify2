package com.example.application.model.youtube_dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;  

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {

    private int totalResults;
    private int resultsPerPage;
    
}
