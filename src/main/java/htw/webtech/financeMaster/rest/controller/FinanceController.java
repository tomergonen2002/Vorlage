package htw.webtech.financeMaster.rest.controller;

import htw.webtech.financeMaster.persistence.entity.Category;
import htw.webtech.financeMaster.persistence.repository.CategoryRepository;
import htw.webtech.financeMaster.persistence.entity.Transaction;
import htw.webtech.financeMaster.persistence.repository.TransactionRepository;
import htw.webtech.financeMaster.persistence.entity.User;
import htw.webtech.financeMaster.persistence.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import htw.webtech.financeMaster.rest.dto.CategoryDto;
import htw.webtech.financeMaster.rest.dto.TransactionDto;
import htw.webtech.financeMaster.rest.dto.StatsDto;
import java.time.LocalDate;
import htw.webtech.financeMaster.rest.dto.CreateCategoryRequest;
import htw.webtech.financeMaster.rest.dto.CreateTransactionRequest;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://*.onrender.com"})
public class FinanceController {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final htw.webtech.financeMaster.service.FinanceService financeService;

    public FinanceController(CategoryRepository categoryRepository, TransactionRepository transactionRepository, UserRepository userRepository, htw.webtech.financeMaster.service.FinanceService financeService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.financeService = financeService;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to FinanceMaster!\nYour personal finance tracker. Happy tracking!";
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            List<Category> userCats = categoryRepository.findByUser_Id(userId);
            if (userCats.isEmpty()) {
                // Ensure default categories exist after restarts
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    categoryRepository.save(new Category("Lebensmittel", "Ausgaben für Essen und Trinken", user));
                    categoryRepository.save(new Category("Gehalt", "Monatliches Einkommen", user));
                    userCats = categoryRepository.findByUser_Id(userId);
                }
            }
            return userCats.stream().map(c -> new CategoryDto(c.getId(), c.getName(), c.getDescription())).toList();
        }
    return categoryRepository.findAll().stream().map(c -> new CategoryDto(c.getId(), c.getName(), c.getDescription())).toList();
    }

    // Punkt 1: Kategorie anlegen
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CreateCategoryRequest request, @RequestParam(required = false) Long userId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null) {
            user = getOrCreateDummyUser();
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setUser(user);
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Punkt 2: Transaktionen
    @GetMapping("/transactions")
    public List<TransactionDto> getTransactions(@RequestParam(required = false) Long userId) {
        List<Transaction> list = (userId != null)
            ? transactionRepository.findByUser_Id(userId)
            : transactionRepository.findAll();
        return list.stream().map(t -> new TransactionDto(
            t.getId(),
            t.getType(),
            t.getAmount(),
            t.getDescription(),
            t.getDate() != null ? t.getDate().toString() : null,
            t.getCategory() != null ? t.getCategory().getId() : null,
            t.getCategory() != null ? t.getCategory().getName() : null
        )).toList();
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody CreateTransactionRequest request, @RequestParam(required = false) Long userId) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null) {
            user = getOrCreateDummyUser();
        }
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        LocalDate date = null;
        if (request.getDate() != null && !request.getDate().isBlank()) {
            try { date = LocalDate.parse(request.getDate()); } catch (Exception e) {}
        }
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setUser(user);
        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Dummy user logic
    private User getOrCreateDummyUser() {
        String dummyEmail = "guest@finance.local";
        return userRepository.findByEmail(dummyEmail)
            .orElseGet(() -> userRepository.save(new User("Gast", dummyEmail, "")));
    }

    // Reset endpoint for dummy user
    @PostMapping("/reset-dummy")
    public ResponseEntity<String> resetDummyData() {
        User dummy = getOrCreateDummyUser();
        // Delete dummy's transactions
        transactionRepository.findByUser_Id(dummy.getId()).forEach(t -> transactionRepository.deleteById(t.getId()));
        // Delete dummy's categories
        categoryRepository.findByUser_Id(dummy.getId()).forEach(c -> categoryRepository.deleteById(c.getId()));
        // Add example categories for dummy user
        categoryRepository.save(new Category("Lebensmittel", "Essen und Trinken", dummy));
        categoryRepository.save(new Category("Freizeit", "Sport, Kino, etc.", dummy));
        categoryRepository.save(new Category("Gehalt", "Monatliches Einkommen", dummy));
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