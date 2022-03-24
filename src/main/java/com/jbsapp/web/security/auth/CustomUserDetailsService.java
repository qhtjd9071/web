package com.jbsapp.web.security.auth;

import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.jwt.exception.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username);

        if (member != null) {
            member.setLastLoginDate(LocalDateTime.now());
        } else {
            throw new JwtException("존재하지 않는 사용자입니다.");
        }

        return new CustomUserDetails(member);
    }
}
