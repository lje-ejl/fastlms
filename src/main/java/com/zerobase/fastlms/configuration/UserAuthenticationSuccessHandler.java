package com.zerobase.fastlms.configuration;

import com.zerobase.fastlms.member.entity.LoginHistory;
import com.zerobase.fastlms.member.model.LoginHistoryInput;
import com.zerobase.fastlms.member.repository.LoginHistoryRepository;
import com.zerobase.fastlms.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.zerobase.fastlms.member.service.MemberService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;



public class UserAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


    private final MemberService memberService;

    public UserAuthenticationSuccessHandler(MemberService memberService){
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String userAgent = RequestUtils.getUserAgent(request);
        String clientIp = RequestUtils.getClientIP(request);

        LoginHistoryInput parameter = new LoginHistoryInput();

        parameter.setUserId(authentication.getName());
        parameter.setLoginDt(LocalDateTime.now());
        parameter.setLoginIp(clientIp);
        parameter.setLoginAgent(userAgent);

        memberService.loginReg(parameter);
        memberService.updateLoginDt(parameter.getUserId(), parameter.getLoginDt());



        super.onAuthenticationSuccess(request, response, authentication);
    }
}
