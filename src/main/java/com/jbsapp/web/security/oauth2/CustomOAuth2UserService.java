package com.jbsapp.web.security.oauth2;

import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.auth.CustomUserDetails;
import com.jbsapp.web.security.oauth2.provider.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MemberRepository memberRepository;

    // 테스트를 위한 변수
    @Setter
    private OAuth2User testOAuth2User;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User;
        if (testOAuth2User != null) {
            oAuth2User = testOAuth2User;
        } else {
            oAuth2User = super.loadUser(userRequest);
        }

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = OAuth2ProviderFactory.getOAuth2UserInfo(oAuth2User, provider);

        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;
        // TODO : 외부로 변수 빼서 사용하기
        String password = bCryptPasswordEncoder.encode("defaultPassword");

        Member member = memberRepository.findByUsername(username);

        if (member == null) {
            member = Member.builder()
                    .username(username)
                    .password(password)
                    .name(oAuth2UserInfo.getName())
                    .email(oAuth2UserInfo.getEmail())
                    .roles("ROLE_MEMBER")
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberRepository.save(member);
        }

        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }
}
