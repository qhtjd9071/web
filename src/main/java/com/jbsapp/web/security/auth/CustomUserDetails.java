package com.jbsapp.web.security.auth;

import com.jbsapp.web.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    Collection<GrantedAuthority> authorities = new ArrayList<>();

    public CustomUserDetails(Member member) {
        this.member = member;

        String[] roles = member.getRoles().split(",");
        Arrays.stream(roles).forEach(role -> this.authorities.add(new SimpleGrantedAuthority(role)));
    }

    public Member getMember() {
        return this.member;
    }

    @Override
    public String getPassword() {
        return this.member.getPassword();
    }

    @Override
    public String getUsername() {
        return this.member.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO: 2022-01-18 : 가입날짜와 마지막 로그인 날짜를 비교해 1년이 넘으면 만료로 처리
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonExpired();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isAccountNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.isAccountNonExpired();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

}
