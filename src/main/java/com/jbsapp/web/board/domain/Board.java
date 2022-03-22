package com.jbsapp.web.board.domain;

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
public class Board extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private String writer;

    @Column
    private String password;
    
    @Column
    private boolean removeYn;

}