package com.jbsapp.web.member.domain;

import com.jbsapp.web.common.domain.Time;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Member extends Time {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(unique = true)
  private String username;

  @Column
  private String password;

  @Column
  private String roles;

  @Column
  private boolean removeYn;

}
