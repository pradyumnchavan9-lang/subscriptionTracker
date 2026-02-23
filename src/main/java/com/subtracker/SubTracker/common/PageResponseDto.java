package com.subtracker.SubTracker.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResponseDto<T> {

    private List<T> content;   // the actual list of items on this page
    private int pageNumber;    // current page number
    private int pageSize;      // page size
    private long totalElements; // total number of items in DB
    private int totalPages;     // total number of pages
    private boolean last;       // is this the last page?


}
