package com.zerobase.fastlms.member.service.impl;

import com.zerobase.fastlms.admin.dto.LoginHistoryDto;
import com.zerobase.fastlms.admin.dto.MemberDto;
import com.zerobase.fastlms.admin.mapper.LoginHistoryMapper;
import com.zerobase.fastlms.admin.mapper.MemberMapper;
import com.zerobase.fastlms.admin.model.MemberParam;
import com.zerobase.fastlms.components.MailComponents;
import com.zerobase.fastlms.course.model.ServiceResult;
import com.zerobase.fastlms.member.entity.LoginHistory;
import com.zerobase.fastlms.member.entity.Member;
import com.zerobase.fastlms.member.entity.MemberCode;
import com.zerobase.fastlms.member.entity.Template;
import com.zerobase.fastlms.member.exception.MemberNotEmailAuthException;
import com.zerobase.fastlms.member.exception.MemberStopUserException;
import com.zerobase.fastlms.member.model.LoginHistoryInput;
import com.zerobase.fastlms.member.model.MemberInput;
import com.zerobase.fastlms.member.model.ResetPasswordInput;
import com.zerobase.fastlms.member.repository.LoginHistoryRepository;
import com.zerobase.fastlms.member.repository.MemberRepository;
import com.zerobase.fastlms.member.repository.TemplateRepository;
import com.zerobase.fastlms.member.service.MemberService;
import com.zerobase.fastlms.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class  MemberServiceImpl implements MemberService {
    
    private final MemberRepository memberRepository;
    private final TemplateRepository templateRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final MailComponents mailComponents;
    
    private final MemberMapper memberMapper;
    private final LoginHistoryMapper loginHistoryMapper;

    /**
     * 로그인 히스토리 저장
     */
    @Override
    public boolean loginReg(LoginHistoryInput parameter) {

        LoginHistory loginHistory = LoginHistory.builder()
                .userId(parameter.getUserId())
                .loginDt(LocalDateTime.now())
                .loginIp(parameter.getLoginIp())
                .loginAgent(parameter.getLoginAgent())
                .build();
        loginHistoryRepository.save(loginHistory);
        
        return true;
    }

    /**
     * 로그인 히스토리 리스트
     */
    @Override
    public List<LoginHistoryDto> loginList(MemberParam parameter) {

        List<LoginHistoryDto> list = loginHistoryMapper.selectList(parameter);

        return loginHistoryMapper.selectList(parameter);
    }

    @Override
    public boolean updateLoginDt(String userId, LocalDateTime loginDt) {

        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }

        Member member = optionalMember.get();

        member.setLoginDt(loginDt);
        memberRepository.save(member);

        return true;
    }

    /**
     * 회원 가입
     */
    @Override
    public boolean register(MemberInput parameter) {

        Optional<Member> optionalMember = memberRepository.findById(parameter.getUserId());
        if (optionalMember.isPresent()) {
            //현재 userId에 해당하는 데이터 존재
            return false;
        }


        Optional<Template> template = templateRepository.findById("reg");
        if (!template.isPresent()) {
            return false;
        }

        String encPassword = BCrypt.hashpw(parameter.getPassword(), BCrypt.gensalt());
        String uuid = UUID.randomUUID().toString();

        Member member = Member.builder()
                .userId(parameter.getUserId())
                .userName(parameter.getUserName())
                .phone(parameter.getPhone())
                .password(encPassword)
                .regDt(LocalDateTime.now())
                .emailAuthYn(false)
                .emailAuthKey(uuid)
                .userStatus(Member.MEMBER_STATUS_REQ)
                .build();
        memberRepository.save(member);

        //new
        String email = parameter.getUserId();
        String name = parameter.getUserName();
        String subject = template.get().getSubject();
        subject = subject.replace("{USER_NAME}", name);
        String text = template.get().getText();
        text = text.replace("{UUID}", uuid);
        mailComponents.sendMail(email, subject, text);

        return true;
    }
    
    @Override
    public boolean emailAuth(String uuid) {
        
        Optional<Member> optionalMember = memberRepository.findByEmailAuthKey(uuid);
        if (!optionalMember.isPresent()) {
            return false;
        }
        
        Member member = optionalMember.get();
        
        if (member.isEmailAuthYn()) {
            return false;
        }
        
        member.setUserStatus(Member.MEMBER_STATUS_ING);
        member.setEmailAuthYn(true);
        member.setEmailAuthDt(LocalDateTime.now());
        memberRepository.save(member);
        
        return true;
    }

    @Override
    public boolean sendResetPassword(ResetPasswordInput parameter) {

        Optional<Member> optionalMember = memberRepository.findByUserIdAndUserName(parameter.getUserId(), parameter.getUserName());
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }

        Optional<Template> template = templateRepository.findById("resetPw");
        if (!template.isPresent()) {
            return false;
        }

        Member member = optionalMember.get();

        String uuid = UUID.randomUUID().toString();

        member.setResetPasswordKey(uuid);
        member.setResetPasswordLimitDt(LocalDateTime.now().plusDays(1));
        memberRepository.save(member);


        String email = parameter.getUserId();
        String name = parameter.getUserName();
        String subject = template.get().getSubject();
        subject = subject.replace("{USER_NAME}", name);
        String text = template.get().getText();
        text = text.replace("{UUID}", uuid);
        mailComponents.sendMail(email, subject, text);


        return false;
    }
    
    @Override
    public List<MemberDto> list(MemberParam parameter) {
        
        long totalCount = memberMapper.selectListCount(parameter);
        
        List<MemberDto> list = memberMapper.selectList(parameter);
        if (!CollectionUtils.isEmpty(list)) {
            int i = 0;
            for(MemberDto x : list) {
                x.setTotalCount(totalCount);
                x.setSeq(totalCount - parameter.getPageStart() - i);
                i++;
            }
        }
        
        return list;
        
        //return memberRepository.findAll();
    }
    
    @Override
    public MemberDto detail(String userId) {
        
        Optional<Member> optionalMember  = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return null;
        }
        
        Member member = optionalMember.get();
        
        return MemberDto.of(member);
    }
    
    @Override
    public boolean updateStatus(String userId, String userStatus) {
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        member.setUserStatus(userStatus);
        memberRepository.save(member);
        
        return true;
    }
    
    @Override
    public boolean updatePassword(String userId, String password) {
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        String encPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        member.setPassword(encPassword);
        memberRepository.save(member);
    
        return true;
        
    }
    @Override
    public ServiceResult updateMember(MemberInput parameter) {
        
        String userId = parameter.getUserId();
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return new ServiceResult(false, "회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        member.setPhone(parameter.getPhone());
        member.setZipcode(parameter.getZipcode());
        member.setAddr(parameter.getAddr());
        member.setAddrDetail(parameter.getAddrDetail());
        member.setUdtDt(LocalDateTime.now());
        memberRepository.save(member);
        
        return new ServiceResult();
    }
    
    @Override
    public ServiceResult updateMemberPassword(MemberInput parameter) {
    
        String userId = parameter.getUserId();
        
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return new ServiceResult(false, "회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        if (!PasswordUtils.equals(parameter.getPassword(), member.getPassword())) {
            return new ServiceResult(false, "비밀번호가 일치하지 않습니다.");
        }
        
        String encPassword = PasswordUtils.encPassword(parameter.getNewPassword());
        member.setPassword(encPassword);
        memberRepository.save(member);
        
        return new ServiceResult(true);
    }
    
    @Override
    public ServiceResult withdraw(String userId, String password) {
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return new ServiceResult(false, "회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        if (!PasswordUtils.equals(password, member.getPassword())) {
            return new ServiceResult(false, "비밀번호가 일치하지 않습니다.");
        }
    
        member.setUserName("삭제회원");
        member.setPhone("");
        member.setPassword("");
        member.setRegDt(null);
        member.setUdtDt(null);
        member.setEmailAuthYn(false);
        member.setEmailAuthDt(null);
        member.setEmailAuthKey("");
        member.setResetPasswordKey("");
        member.setResetPasswordLimitDt(null);
        member.setUserStatus(MemberCode.MEMBER_STATUS_WITHDRAW);
        member.setZipcode("");
        member.setAddr("");
        member.setAddrDetail("");
        memberRepository.save(member);
        
        return new ServiceResult();
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> optionalMember = memberRepository.findById(username);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }

        Member member = optionalMember.get();
        
        if (Member.MEMBER_STATUS_REQ.equals(member.getUserStatus())) {
            throw new MemberNotEmailAuthException("이메일 활성화 이후에 로그인을 해주세요.");
        }
        
        if (Member.MEMBER_STATUS_STOP.equals(member.getUserStatus())) {
            throw new MemberStopUserException("정지된 회원 입니다.");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        if (member.isAdminYn()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        /*
        LoginHistory loginHistory = LoginHistory.builder()
                .userId(member.getUserId())
                .loginDt(LocalDateTime.now())

                .build();
        loginHistoryRepository.save(loginHistory);
        */


        return new User(member.getUserId(), member.getPassword(),grantedAuthorities);
    }
}
