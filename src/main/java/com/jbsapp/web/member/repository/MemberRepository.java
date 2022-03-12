package com.jbsapp.web.member.repository;

import com.jbsapp.web.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Member findByUsername(String username);
}
