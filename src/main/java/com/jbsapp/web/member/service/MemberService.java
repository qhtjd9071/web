package com.jbsapp.web.member.service;

import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.MemberRequest;
import com.jbsapp.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

	private final MemberRepository memberRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public Member join(MemberRequest request) {
		Member member = Member.builder()
				.username(request.getUsername())
				.password(bCryptPasswordEncoder.encode(request.getPassword()))
				.build();

		return memberRepository.save(member);
	}

}
