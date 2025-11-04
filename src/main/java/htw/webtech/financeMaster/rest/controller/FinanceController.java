package htw.webtech.financeMaster.rest.controller;

import htw.webtech.financeMaster.persistence.entity.Category;
import htw.webtech.financeMaster.persistence.entity.Transaction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import htw.webtech.financeMaster.rest.dto.CategoryDto;
import htw.webtech.financeMaster.rest.dto.TransactionDto;
import htw.webtech.financeMaster.rest.dto.StatsDto;
import java.time.LocalDate;
import htw.webtech.financeMaster.rest.dto.CreateCategoryRequest;
import htw.webtech.financeMaster.rest.dto.CreateTransactionRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.onrender.com"})
public class FinanceController {

    private final htw.webtech.financeMaster.service.FinanceService financeService;

    public FinanceController(htw.webtech.financeMaster.service.FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to FinanceMaster!\nYour personal finance tracker. Happy tracking!";
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(required = false) Long userId) {
        return financeService.getCategories(userId).stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
        Category updated = financeService.updateCategory(id, request.getName(), request.getDescription());
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CategoryDto(updated.getId(), updated.getName(), updated.getDescription()));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        boolean ok = financeService.deleteCategory(id);
        if (!ok) return ResponseEntity.badRequest().build();
        return ResponseEntity.noContent().build();
    }

    // Punkt 1: Kategorie anlegen
    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest request, @RequestParam(required = false) Long userId) {
        Category saved = financeService.createCategory(request.getName(), request.getDescription(), userId);
        CategoryDto dto = new CategoryDto(saved.getId(), saved.getName(), saved.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Punkt 2: Transaktionen
    @GetMapping("/transactions")
    public List<TransactionDto> getTransactions(@RequestParam(required = false) Long userId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to,
                                                @RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size) {
        java.time.LocalDate fromDate = null;
        java.time.LocalDate toDate = null;
        try { if (from != null && !from.isBlank()) fromDate = java.time.LocalDate.parse(from); } catch (Exception ignored) {}
        try { if (to != null && !to.isBlank()) toDate = java.time.LocalDate.parse(to); } catch (Exception ignored) {}
        return financeService.getTransactions(userId, categoryId, fromDate, toDate, page, size).stream().map(t -> new TransactionDto(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getDescription(),
                t.getDate() != null ? t.getDate().toString() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null
        )).toList();
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(@PathVariable Long id, @Valid @RequestBody CreateTransactionRequest request) {
        java.time.LocalDate date = null;
        if (request.getDate() != null && !request.getDate().isBlank()) {
            try { date = java.time.LocalDate.parse(request.getDate()); } catch (Exception e) { }
        }
        Transaction updated = financeService.updateTransaction(id, request.getType(), request.getAmount(), request.getDescription(), date, request.getCategoryId());
        if (updated == null) return ResponseEntity.notFound().build();
        TransactionDto dto = new TransactionDto(
                updated.getId(),
                updated.getType(),
                updated.getAmount(),
                updated.getDescription(),
                updated.getDate() != null ? updated.getDate().toString() : null,
                updated.getCategory() != null ? updated.getCategory().getId() : null,
                updated.getCategory() != null ? updated.getCategory().getName() : null
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        boolean ok = financeService.deleteTransaction(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody CreateTransactionRequest request, @RequestParam(required = false) Long userId) {
        LocalDate date = null;
        if (request.getDate() != null && !request.getDate().isBlank()) {
            try { date = LocalDate.parse(request.getDate()); } catch (Exception e) { }
        }
        Transaction saved = financeService.createTransaction(request.getType(), request.getAmount(), request.getDescription(), date, request.getCategoryId(), userId);
        if (saved == null) return ResponseEntity.badRequest().build();
        TransactionDto dto = new TransactionDto(
                saved.getId(),
                saved.getType(),
                saved.getAmount(),
                saved.getDescription(),
                saved.getDate() != null ? saved.getDate().toString() : null,
                saved.getCategory() != null ? saved.getCategory().getId() : null,
                saved.getCategory() != null ? saved.getCategory().getName() : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Dummy user logic
    // Reset endpoint for dummy user
    @PostMapping("/reset-dummy")
    public ResponseEntity<String> resetDummyData() {
        financeService.resetDummyData();
        return ResponseEntity.ok("Dummy-Daten zurückgesetzt!");
    }

    // New: aggregated stats endpoint
    @GetMapping("/stats")
    public StatsDto getStats(@RequestParam(required = false) Long userId) {
        return financeService.getStats(userId);
    }

    // New: recent transactions
    @GetMapping("/transactions/recent")
    public java.util.List<htw.webtech.financeMaster.rest.dto.RecentTransactionDto> getRecentTransactions(@RequestParam(required = false) Long userId, @RequestParam(defaultValue = "10") int limit) {
        return financeService.getRecentTransactions(userId, limit);
    }
}