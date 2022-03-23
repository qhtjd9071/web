package com.jbsapp.web.security.auth;

import com.jbsapp.web.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Member member;

    Collection<GrantedAuthority> authorities = new ArrayList<>();

    private Map<String, Object> attributes;

    public CustomUserDetails(Member member) {
        this.member = member;

        String[] roles = member.getRoles().split(",");
        Arrays.stream(roles).forEach(role -> this.authorities.add(new SimpleGrantedAuthority(role)));
    }

    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.member = member;

        String[] roles = member.getRoles().split(",");
        Arrays.stream(roles).forEach(role -> this.authorities.add(new SimpleGrantedAuthority(role)));
        this.attributes = attributes;
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
        if (member.getLastLoginDate() == null) {
            return true;
        }
        LocalDateTime current = LocalDateTime.now();
        return !current.minusYears(1).isAfter(member.getLastLoginDate());
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

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    // TODO : 필요성 체크
    @Override
    public String getName() {
        return null;
    }
}
