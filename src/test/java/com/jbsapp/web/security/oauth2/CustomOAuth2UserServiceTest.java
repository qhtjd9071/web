package com.jbsapp.web.security.oauth2;

import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.auth.CustomUserDetails;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private CustomOAuth2UserService customOuath2UserService;

    @Test
    @DisplayName("OAuth 2.0 테스트 - google")
    public void test01() {
        // given
        OAuth2UserRequest mockUserRequest = getMockUserRequest("google");

        Map<String, Object> mockAttributes = new HashMap<>();
        mockAttributes.put("sub", "1234");
        mockAttributes.put("name", "test");
        mockAttributes.put("email", "test@test.com");

        oAuth2User = new DefaultOAuth2User(new HashSet<>(), mockAttributes, "sub");

        // when
        customOuath2UserService.setTestOAuth2User(oAuth2User);
        CustomUserDetails ret = (CustomUserDetails) customOuath2UserService.loadUser(mockUserRequest);

        // then
        Member member = ret.getMember();
        assertThat(member.getUsername(), is("google_1234"));
        assertThat(member.getPassword(), is(IsNull.notNullValue()));
        assertThat(member.getName(), is("test"));
        assertThat(member.getEmail(), is("test@test.com"));
        assertThat(member.getProvider(), is("google"));
        assertThat(member.getProviderId(), is("1234"));
        assertThat(member.getRoles(), is("ROLE_MEMBER"));
    }

    @Test
    @DisplayName("OAuth 2.0 테스트 - naver")
    public void test02() {
        // given
        OAuth2UserRequest mockUserRequest = getMockUserRequest("naver");

        Map<String, Object> mockAttributes = new HashMap<>();
        mockAttributes.put("resultcode", "00");
        mockAttributes.put("message", "success");

        Map<String, Object> response = new HashMap<>();
        response.put("id", "1234");
        response.put("email", "test@test.com");
        response.put("name", "test");
        mockAttributes.put("response", response);

        oAuth2User = new DefaultOAuth2User(new HashSet<>(), mockAttributes, "response");

        // when
        customOuath2UserService.setTestOAuth2User(oAuth2User);
        CustomUserDetails ret = (CustomUserDetails) customOuath2UserService.loadUser(mockUserRequest);

        // then
        Member member = ret.getMember();
        assertThat(member.getUsername(), is("naver_1234"));
        assertThat(member.getPassword(), is(IsNull.notNullValue()));
        assertThat(member.getName(), is("test"));
        assertThat(member.getEmail(), is("test@test.com"));
        assertThat(member.getProvider(), is("naver"));
        assertThat(member.getProviderId(), is("1234"));
        assertThat(member.getRoles(), is("ROLE_MEMBER"));
    }

    @Test
    @DisplayName("OAuth 2.0 테스트 - kakao")
    public void test03() {
        // given
        OAuth2UserRequest mockUserRequest = getMockUserRequest("kakao");

        Map<String, Object> mockAttributes = new HashMap<>();
        mockAttributes.put("id", "1234");
        mockAttributes.put("name", LocalDateTime.now());
        mockAttributes.put("properties", Map.of("nickname", "test"));

        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("profile", Map.of("nickname", "test"));
        kakaoAccount.put("email", "test@test.com");

        mockAttributes.put("kakao_account", kakaoAccount);

        oAuth2User = new DefaultOAuth2User(new HashSet<>(), mockAttributes, "id");

        // when
        customOuath2UserService.setTestOAuth2User(oAuth2User);
        CustomUserDetails ret = (CustomUserDetails) customOuath2UserService.loadUser(mockUserRequest);

        // then
        Member member = ret.getMember();
        assertThat(member.getUsername(), is("kakao_1234"));
        assertThat(member.getPassword(), is(IsNull.notNullValue()));
        assertThat(member.getName(), is("test"));
        assertThat(member.getEmail(), is("test@test.com"));
        assertThat(member.getProvider(), is("kakao"));
        assertThat(member.getProviderId(), is("1234"));
        assertThat(member.getRoles(), is("ROLE_MEMBER"));
    }

    private OAuth2UserRequest getMockUserRequest(String provider) {
        ClientRegistration mockClientRegistration = getMockClientRegistration(provider);

        OAuth2AccessToken mockAccessToken = getMockAccessToken();

        return new OAuth2UserRequest(mockClientRegistration, mockAccessToken, new HashMap<>());
    }

    private ClientRegistration getMockClientRegistration(String provider) {
        return ClientRegistration.withRegistrationId(provider)
                .clientId("required")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("required")
                .authorizationUri("required")
                .tokenUri("required")
                .build();
    }

    private OAuth2AccessToken getMockAccessToken() {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "required",
                Instant.now(),
                Instant.now().plusSeconds(1),
                new HashSet<>());
    }
}
