package com.lms.repository;

import com.lms.model.BorrowingRecord;
import com.lms.model.BorrowStatus;
import com.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    
    // Find all records for a user
    List<BorrowingRecord> findByUser(User user);
    
    // Find all records for a user ordered by borrow date
    List<BorrowingRecord> findByUserOrderByBorrowDateDesc(User user);
    
    // Find active records for a user (not returned)
    List<BorrowingRecord> findByUserAndStatus(User user, BorrowStatus status);
    
    // Count active borrowings for a user
    long countByUserAndStatus(User user, BorrowStatus status);
    
    // Check if user has overdue books
    boolean existsByUserAndStatus(User user, BorrowStatus status);
    
    // Calculate total unpaid fines for a user
    @Query("SELECT COALESCE(SUM(br.fineAmount), 0) FROM BorrowingRecord br WHERE br.user = :user AND br.finePaid = false")
    BigDecimal sumUnpaidFinesByUser(@Param("user") User user);
    
    // Find records that are overdue
    @Query("SELECT br FROM BorrowingRecord br WHERE br.dueDate < CURRENT_DATE AND br.status = 'ACTIVE'")
    List<BorrowingRecord> findOverdueRecords();
    
    // Find records due soon (within next 3 days)
    @Query("SELECT br FROM BorrowingRecord br WHERE br.dueDate BETWEEN CURRENT_DATE AND :futureDate AND br.status = 'ACTIVE'")
    List<BorrowingRecord> findRecordsDueSoon(@Param("futureDate") LocalDate futureDate);
}