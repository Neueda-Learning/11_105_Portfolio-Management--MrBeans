package com.portfolio.portfolio_management.repository;
import com.portfolio.portfolio_management.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
