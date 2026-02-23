package com.subtracker.SubTracker.common;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public <T> PageResponseDto<T> pageToPageDto(Page<T> page){
        PageResponseDto<T> pageResponseDto = new PageResponseDto<>();
        pageResponseDto.setTotalPages(page.getTotalPages());
        pageResponseDto.setTotalElements(page.getTotalElements());
        pageResponseDto.setTotalPages(page.getTotalPages());
        pageResponseDto.setPageNumber(page.getNumber());
        pageResponseDto.setPageSize(page.getSize());
        pageResponseDto.setContent(page.getContent());
        return pageResponseDto;

    }
}


