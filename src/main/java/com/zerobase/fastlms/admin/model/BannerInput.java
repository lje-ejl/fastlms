package com.zerobase.fastlms.admin.model;

import lombok.Data;

@Data
public class BannerInput {

    long id;

    String bannerName;
    String linkPath;
    boolean target;
    int sortValue;
    boolean usingYn;

    String filename;
    String urlFilename;

    //삭제용
    String idList;
}
