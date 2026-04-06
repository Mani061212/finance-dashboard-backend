package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions", description = "Financial record CRUD with filtering. " +
        "ANALYST sees own records only; ADMIN sees all. VIEWERs use /dashboard/recent.")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Create a transaction (ANALYST, ADMIN)")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "List transactions — filterable, paginated. " +
                         "ANALYST sees own; ADMIN sees all. VIEWERs use GET /dashboard/recent.")
    public ResponseEntity<PagedResponse<TransactionResponse>> getAll(
            @RequestParam(required = false)                                         String type,
            @RequestParam(required = false)                                         String category,
            @RequestParam(required = false)                                         String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")               int page,
            @RequestParam(defaultValue = "20")              int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc")            String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(
                transactionService.getAll(type, category, search, from, to,
                        PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get a single transaction by ID (ANALYST sees own; ADMIN sees all)")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Update a transaction (owner or ADMIN)")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Soft-delete a transaction — data preserved with is_deleted=true")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
