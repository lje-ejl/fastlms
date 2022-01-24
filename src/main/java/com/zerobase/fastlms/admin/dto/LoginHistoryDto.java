package com.zerobase.fastlms.admin.dto;

import com.zerobase.fastlms.member.entity.LoginHistory;
import com.zerobase.fastlms.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoginHistoryDto {

    LocalDateTime loginDt;
    String loginIp;
    String loginAgent;

    //추가컬럼
    long totalCount;
    long seq;
    
    
    public static LoginHistoryDto of(LoginHistory loginHistory) {
        
        return LoginHistoryDto.builder()
                .loginDt(loginHistory.getLoginDt())
                .loginIp(loginHistory.getLoginIp())
                .loginAgent(loginHistory.getLoginAgent())
                .build();
    }


}
