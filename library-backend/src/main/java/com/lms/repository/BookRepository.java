package com.lms.repository;

import com.lms.model.Book;
import com.lms.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Find book by ISBN
    Book findByIsbn(String isbn);
    
    // Find books by title containing (case-insensitive)
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Find books by author containing (case-insensitive)
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Find books by category containing (case-insensitive)
    List<Book> findByCategoryContainingIgnoreCase(String category);
    
    // Find books by status
    List<Book> findByStatus(BookStatus status);
    
    // Find available books (status = AVAILABLE and availableCopies > 0)
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.availableCopies > 0")
    List<Book> findAvailableBooks();
    
    // Search books by multiple criteria
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:category IS NULL OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:isbn IS NULL OR b.isbn LIKE CONCAT('%', :isbn, '%'))")
    List<Book> searchBooks(
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") String category,
            @Param("isbn") String isbn);
}