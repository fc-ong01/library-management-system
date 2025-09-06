package com.lms.service;

import com.lms.model.Book;
import com.lms.model.BookStatus;
import com.lms.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Get all books
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Get book by ID
     */
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Add a new book
     */
    public Book addBook(Book book) {
        // Set available copies if not set
        if (book.getAvailableCopies() == null && book.getTotalCopies() != null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        
        // Set status based on available copies
        if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            book.setStatus(BookStatus.AVAILABLE);
        } else {
            book.setStatus(BookStatus.MAINTENANCE);
        }
        
        return bookRepository.save(book);
    }

    /**
     * Update book information
     */
    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setCategory(bookDetails.getCategory());
        book.setPublicationYear(bookDetails.getPublicationYear());
        
        // Handle copies update carefully
        if (bookDetails.getTotalCopies() != null) {
            int currentBorrowed = book.getTotalCopies() - book.getAvailableCopies();
            if (bookDetails.getTotalCopies() < currentBorrowed) {
                throw new RuntimeException("Cannot set total copies less than currently borrowed copies");
            }
            book.setTotalCopies(bookDetails.getTotalCopies());
            book.setAvailableCopies(bookDetails.getTotalCopies() - currentBorrowed);
        }
        
        // Update status based on available copies
        if (book.getAvailableCopies() > 0) {
            book.setStatus(BookStatus.AVAILABLE);
        } else {
            book.setStatus(BookStatus.BORROWED);
        }
        
        return bookRepository.save(book);
    }

    /**
     * Delete a book
     */
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        // Check if book is currently borrowed
        if (isBookBorrowed(id)) {
            throw new RuntimeException("Cannot delete book that is currently borrowed");
        }
        
        bookRepository.deleteById(id);
    }

    /**
     * Search books by various criteria
     */
    public List<Book> searchBooks(String title, String author, String category, String isbn) {
        return bookRepository.searchBooks(title, author, category, isbn);
    }

    /**
     * Get available books
     */
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    /**
     * Check if a book is borrowed (placeholder implementation)
     */
    private boolean isBookBorrowed(Long bookId) {
        // This would typically call a BorrowingRecordRepository
        // For now, we'll return false as a placeholder
        return false;
    }

    /**
     * Update book status
     */
    public Book updateBookStatus(Long id, BookStatus status) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        book.setStatus(status);
        return bookRepository.save(book);
    }
}