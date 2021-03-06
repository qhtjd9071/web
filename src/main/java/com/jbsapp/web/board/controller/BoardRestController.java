package com.jbsapp.web.board.controller;

import com.jbsapp.web.board.domain.Board;
import com.jbsapp.web.board.model.BoardRequest;
import com.jbsapp.web.board.model.DeleteRequest;
import com.jbsapp.web.board.service.BoardService;
import com.jbsapp.web.common.util.WebResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardRestController {

  private final BoardService boardService;

    @GetMapping("")
    public ResponseEntity<?> findAll(Pageable pageable) {

        Page<Board> boards = boardService.findAll(pageable);

        return WebResponseEntity.OK(boards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOne(@PathVariable Long id) {

        Board board = boardService.findOne(id);

        return WebResponseEntity.OK(board);
    }

    @PostMapping("")
    public ResponseEntity<?> create(Authentication authentication, @Valid @RequestBody BoardRequest request, BindingResult bindingResult) {

        String username;
        if (authentication == null) {
            username = "anonymous";
        } else {
            username = authentication.getName();
        }

        Board board = boardService.create(request, username);

        return WebResponseEntity.OK(board);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication authentication, @Valid @RequestBody BoardRequest request, BindingResult bindingResult, @PathVariable Long id) {

        String username;
        if (authentication == null) {
            username = "anonymous";
        } else {
            username = authentication.getName();
        }

        Board board = boardService.update(request, username, id);

        return WebResponseEntity.OK(board);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication authentication, @Valid @RequestBody DeleteRequest request, BindingResult bindingResult, @PathVariable Long id) {

        String username;
        if (authentication == null) {
            username = "anonymous";
        } else {
            username = authentication.getName();
        }

        Board board = boardService.delete(request, username, id);

        return WebResponseEntity.OK(board);
    }

}
