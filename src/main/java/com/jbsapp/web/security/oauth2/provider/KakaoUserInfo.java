package com.jbsapp.web.security.oauth2.provider;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

        private final Map<String, Object> attributes;

        private final Map<String, Object> kakaoAccount;

        public KakaoUserInfo(Map<String, Object> attributes) {
                this.attributes = attributes;
                this.kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
        }

        @Override
        public String getProviderId() {
                return attributes.get("id").toString();
        }

        @Override
        public String getProvider() {
                return "kakao";
        }

        @Override
        public String getName() {
                Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");
                return profile.get("nickname").toString();
        }

        @Override
        public String getEmail() {
                return kakaoAccount.get("email").toString();
        }
}
