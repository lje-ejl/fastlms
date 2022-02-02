package com.zerobase.fastlms.admin.service;

import com.zerobase.fastlms.admin.dto.BannerDto;
import com.zerobase.fastlms.admin.model.BannerInput;
import com.zerobase.fastlms.admin.model.BannerParam;
import com.zerobase.fastlms.course.dto.CourseDto;
import com.zerobase.fastlms.course.model.CourseParam;

import java.util.List;

public interface BannerService {
    
    
    /**
     * 배너 등록
     */
    boolean add(BannerInput parameter);


    /**
     * 배너 목록
     */
    List<BannerDto> list(BannerParam parameter);

    /**
     * 사용 배너
     */
    List<BannerDto> usingBanner();

    /**
     * 배너 상세정보
     */
    BannerDto getById(long id);

    /**
     * 배너 수정
     */
    boolean set(BannerInput parameter);

    /**
     * 배너 삭제
     */
    boolean del(String idList);
}
