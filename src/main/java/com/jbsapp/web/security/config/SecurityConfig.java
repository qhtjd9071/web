package com.jbsapp.web.security.config;

import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.jwt.JwtAuthenticationFilter;
import com.jbsapp.web.security.jwt.JwtAuthorizationFilter;
import com.jbsapp.web.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final MemberRepository memberRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtAuthorizationFilter(authenticationManager()))

                .authorizeRequests()
                .antMatchers("/h2-console/**")
                .permitAll()
                .antMatchers("/docs/**")
                .permitAll()
                .antMatchers("/api/member/**")
                .permitAll()
                .antMatchers("/test")
                .hasRole("MEMBER")
                .antMatchers("/**")
                .permitAll()
                .anyRequest().permitAll()
                .and()

                .formLogin().disable()
                .httpBasic().disable()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(principalOuath2UserService())
                ;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomOAuth2UserService principalOuath2UserService() {
        return new CustomOAuth2UserService(bCryptPasswordEncoder(), memberRepository);
    }
}
