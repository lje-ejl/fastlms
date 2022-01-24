package com.zerobase.fastlms.member.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Id;
import java.time.LocalDateTime;

@ToString
@Data
public class LoginHistoryInput {

    private String userId;
    private LocalDateTime loginDt;
    private String loginIp;
    private String loginAgent;


}
