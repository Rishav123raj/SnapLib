package rishav.example.SpringRestDemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rishav.example.SpringRestDemo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);
}
