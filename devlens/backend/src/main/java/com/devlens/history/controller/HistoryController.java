package com.devlens.history.controller;

import com.devlens.common.config.UserPrincipal;
import com.devlens.history.dto.HistoryDetailDto;
import com.devlens.history.dto.HistoryPageResponse;
import com.devlens.history.dto.UpdateHistoryNotesRequest;
import com.devlens.history.dto.UpdateHistoryNotesResponse;
import com.devlens.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for Analysis History search, details, and notes editing
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<HistoryPageResponse> getHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        HistoryPageResponse response = historyService.getHistory(
                principal.getId(), search, type, from, to, page, size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryDetailDto> getHistoryDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        HistoryDetailDto detail = historyService.getHistoryDetail(id, principal.getId());
        return ResponseEntity.ok(detail);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateHistoryNotesResponse> updateNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateHistoryNotesRequest request) {
        UpdateHistoryNotesResponse response = historyService.updateNotes(id, principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        historyService.deleteHistory(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
