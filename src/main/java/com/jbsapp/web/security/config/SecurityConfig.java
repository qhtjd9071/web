package com.jbsapp.web.security.config;

import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.jwt.JwtAuthenticationFailureHandler;
import com.jbsapp.web.security.jwt.JwtAuthenticationFilter;
import com.jbsapp.web.security.jwt.JwtAuthenticationSuccessHandler;
import com.jbsapp.web.security.jwt.JwtAuthorizationFilter;
import com.jbsapp.web.security.jwt.exception.JwtExceptionHandler;
import com.jbsapp.web.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .cors()
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .addFilterBefore(new JwtExceptionHandler(), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtAuthenticationSuccessHandler(), jwtAuthenticationFailureHandler()))
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

                .formLogin()
                .failureHandler(new JwtAuthenticationFailureHandler())
                .disable()
                .httpBasic().disable()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(principalOAuth2UserService())
                ;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationSuccessHandler jwtAuthenticationSuccessHandler() {
        return new JwtAuthenticationSuccessHandler();
    }

    @Bean
    public JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler() {
        return new JwtAuthenticationFailureHandler();
    }

    @Bean
    public CustomOAuth2UserService principalOAuth2UserService() {
        return new CustomOAuth2UserService(bCryptPasswordEncoder(), memberRepository);
    }

}
