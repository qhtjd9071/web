package com.jbsapp.web.security.oauth2;

import com.jbsapp.web.common.exception.WebException;
import com.jbsapp.web.security.oauth2.provider.GoogleUserInfo;
import com.jbsapp.web.security.oauth2.provider.KakaoUserInfo;
import com.jbsapp.web.security.oauth2.provider.NaverUserInfo;
import com.jbsapp.web.security.oauth2.provider.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Slf4j
public class OAuth2ProviderFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(OAuth2User oAuth2User, String provider) {
        return switch (provider) {
            case "google" -> new GoogleUserInfo(oAuth2User.getAttributes());
            case "naver" -> new NaverUserInfo(oAuth2User.getAttributes());
            case "kakao" -> new KakaoUserInfo(oAuth2User.getAttributes());
            default -> throw new WebException("지원하지 않는 로그인 방식입니다.");
        };
    }
}
