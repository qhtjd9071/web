package com.jbsapp.web.board.service;

import com.jbsapp.web.board.domain.Board;
import com.jbsapp.web.board.model.BoardRequest;
import com.jbsapp.web.board.model.DeleteRequest;
import com.jbsapp.web.board.repository.BoardRepository;
import com.jbsapp.web.common.exception.WebException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Page<Board> findAll(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    public Board findOne(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new WebException("해당 게시글이 존재하지 않습니다."));
    }

    public Board create(BoardRequest request, String username) {
        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .writer(username)
                .removeYn(false)
                .build();

        return boardRepository.save(board);
    }

    @Transactional
    public Board update(BoardRequest request, String username, Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new WebException("해당 게시글이 존재하지 않습니다."));

        if (!username.equals(board.getWriter())) {
            throw new WebException("작성자만 수정 가능합니다.");
        }

        System.out.println("==========================" + bCryptPasswordEncoder.matches(request.getPassword(), board.getPassword()));
        if (!bCryptPasswordEncoder.matches(request.getPassword(), board.getPassword())) {
            throw new WebException("비밀번호가 일치하지 않습니다.");
        }

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        board.setPassword(request.getPassword());

        return board;
    }

    @Transactional
    public Board delete(DeleteRequest request, String username, Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new WebException("해당 게시글이 존재하지 않습니다."));

        if (!username.equals(board.getWriter())) {
            throw new WebException("작성자만 삭제 가능합니다.");
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), board.getPassword())) {
            throw new WebException("비밀번호가 일치하지 않습니다.");
        }

        if (board.isRemoveYn()) {
            throw new WebException("이미 삭제된 게시글입니다.");
        }

        board.setRemoveYn(true);

        return board;
    }
}
