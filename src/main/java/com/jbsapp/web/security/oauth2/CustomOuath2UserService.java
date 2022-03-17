package com.jbsapp.web.security.oauth2;

import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.auth.CustomUserDetails;
import com.jbsapp.web.security.oauth2.provider.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOuath2UserService extends DefaultOAuth2UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = OAuth2ProviderFactory.getOAuth2MemberInfo(oAuth2User, provider);

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

        System.out.println("member : " + member);
        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }
}
