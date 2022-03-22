package com.jbsapp.web.board.repository;

import com.jbsapp.web.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

}
