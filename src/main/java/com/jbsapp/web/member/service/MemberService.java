package com.jbsapp.web.member.service;

import com.jbsapp.web.common.exception.WebException;
import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.RegisterRequest;
import com.jbsapp.web.member.model.UpdateRequest;
import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public Member join(RegisterRequest request) {
		Member member = Member.builder()
				.username(request.getUsername())
				.password(bCryptPasswordEncoder.encode(request.getPassword()))
				.roles(RoleType.MEMBER.getValue())
				.build();

		if (isIdDuplicated(member.getUsername())) {
			throw new WebException("이미 존재하는 아이디입니다.");
		}

		return memberRepository.save(member);
	}

	public boolean isIdDuplicated(String username) {
		return memberRepository.findByUsername(username) != null;
	}

	public Member update(UpdateRequest request) {
		Member member = memberRepository.findById(request.getId())
				.orElseThrow(() -> new WebException("존재하지 않는 회원입니다."));

		String encodedNew = bCryptPasswordEncoder.encode(request.getNewPassword());

		if (!bCryptPasswordEncoder.matches(request.getPrevPassword(), member.getPassword())) {
			throw new WebException("비밀번호가 일치하지 않습니다.");
		}

		member.setPassword(encodedNew);

		return member;
	}

	public Member find(String username) {
		Member member = memberRepository.findByUsername(username);

		if (member == null) {
			throw new WebException("존재하지 않는 회원입니다.");
		}

		return memberRepository.findByUsername(username);
	}
}
