package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.repository.spec.TransactionSpecification;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository        userRepository;

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User user = findUserByEmail(SecurityUtils.getCurrentUserEmail());
        Transaction t = Transaction.builder()
                .user(user)
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .isDeleted(false)
                .build();
        return TransactionResponse.from(transactionRepository.save(t));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getAll(String type, String category,
                                                      String search, LocalDate from, LocalDate to,
                                                      Pageable pageable) {
        Specification<Transaction> spec = TransactionSpecification.filter(
                type, category, search, from, to,
                SecurityUtils.getCurrentUserEmail(),
                SecurityUtils.isAdmin());
        return PagedResponse.from(
                transactionRepository.findAll(spec, pageable)
                        .map(TransactionResponse::from));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID id) {
        Transaction t = findActiveOrThrow(id);
        checkAccess(t);
        return TransactionResponse.from(t);
    }

    @Transactional
    public TransactionResponse update(UUID id, TransactionRequest request) {
        Transaction t = findActiveOrThrow(id);
        checkAccess(t);
        t.setAmount(request.amount());
        t.setType(request.type());
        t.setCategory(request.category());
        t.setDescription(request.description());
        t.setTransactionDate(request.transactionDate());
        return TransactionResponse.from(transactionRepository.save(t));
    }

    @Transactional
    public void softDelete(UUID id) {
        Transaction t = findActiveOrThrow(id);
        checkAccess(t);
        t.setIsDeleted(true);
        transactionRepository.save(t);
    }

    private Transaction findActiveOrThrow(UUID id) {
        return transactionRepository.findById(id)
                .filter(t -> !t.getIsDeleted())
                .orElseThrow(() -> AppException.notFound("Transaction not found: " + id));
    }

    /** ADMIN can access any transaction; ANALYST can only access their own. */
    private void checkAccess(Transaction t) {
        if (!SecurityUtils.isAdmin()
                && !t.getUser().getEmail().equals(SecurityUtils.getCurrentUserEmail())) {
            throw AppException.forbidden("You do not have access to this transaction");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found: " + email));
    }
}
