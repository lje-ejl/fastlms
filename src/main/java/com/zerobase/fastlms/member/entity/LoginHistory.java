package com.zerobase.fastlms.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@IdClass(LoginHistory.class)
public class LoginHistory implements Serializable {
    
    @Id
    private String userId;
    @Id
    private LocalDateTime loginDt;
    private String loginIp;
    private String loginAgent;

}
