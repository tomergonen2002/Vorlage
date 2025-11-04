package htw.webtech.financeMaster.service;

import htw.webtech.financeMaster.persistence.entity.Transaction;
import htw.webtech.financeMaster.persistence.entity.Category;
import htw.webtech.financeMaster.persistence.entity.User;
import htw.webtech.financeMaster.persistence.repository.TransactionRepository;
import htw.webtech.financeMaster.persistence.repository.CategoryRepository;
import htw.webtech.financeMaster.persistence.repository.UserRepository;
import htw.webtech.financeMaster.rest.dto.CategorySummaryDto;
import htw.webtech.financeMaster.rest.dto.RecentTransactionDto;
import htw.webtech.financeMaster.rest.dto.StatsDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinanceService {

        private final TransactionRepository transactionRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;

        public FinanceService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
                this.transactionRepository = transactionRepository;
                this.categoryRepository = categoryRepository;
                this.userRepository = userRepository;
        }

        // Category operations (simplified helpers)
        public java.util.List<Category> getCategories(Long userId) {
                return (userId != null) ? categoryRepository.findByUser_Id(userId) : categoryRepository.findAll();
        }

        public Category createCategory(String name, String description, Long userId) {
                User user = null;
                if (userId != null) user = userRepository.findById(userId).orElse(null);
                if (user == null) user = getOrCreateDummyUser();
                Category category = new Category();
                category.setName(name);
                category.setDescription(description);
                category.setUser(user);
                return categoryRepository.save(category);
        }

        private User getOrCreateDummyUser() {
                String dummyEmail = "guest@finance.local";
                return userRepository.findByEmail(dummyEmail)
                        .orElseGet(() -> userRepository.save(new User("Gast", dummyEmail, "")));
        }

    public StatsDto getStats(Long userId) {
        List<Transaction> list = (userId != null)
                ? transactionRepository.findByUser_Id(userId)
                : transactionRepository.findAll();

        double totalIncome = list.stream()
                .filter(t -> t.getType() != null && t.getType().toLowerCase().contains("einnah"))
                .mapToDouble(t -> t.getAmount() == null ? 0.0 : t.getAmount())
                .sum();

        double totalExpense = list.stream()
                .filter(t -> t.getType() != null && t.getType().toLowerCase().contains("ausgab"))
                .mapToDouble(t -> t.getAmount() == null ? 0.0 : t.getAmount())
                .sum();

        Map<String, CategorySummaryDto> byCat = list.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.collectingAndThen(Collectors.toList(), l -> {
                            double income = l.stream().filter(tx -> tx.getType() != null && tx.getType().toLowerCase().contains("einnah")).mapToDouble(tx -> tx.getAmount() == null ? 0.0 : tx.getAmount()).sum();
                            double expense = l.stream().filter(tx -> tx.getType() != null && tx.getType().toLowerCase().contains("ausgab")).mapToDouble(tx -> tx.getAmount() == null ? 0.0 : tx.getAmount()).sum();
                            return new CategorySummaryDto(l.get(0).getCategory().getId(), l.get(0).getCategory().getName(), income, expense);
                        })
                ));

        StatsDto stats = new StatsDto();
        stats.setTotalTransactions(list.size());
        stats.setTotalIncome(totalIncome);
        stats.setTotalExpense(totalExpense);
        stats.setNet(totalIncome - totalExpense);
        stats.setByCategory(byCat.values().stream().collect(Collectors.toList()));
        return stats;
    }

        public Category updateCategory(Long id, String name, String description) {
                Category category = categoryRepository.findById(id).orElse(null);
                if (category == null) return null;
                if (name != null && !name.isBlank()) category.setName(name);
                category.setDescription(description);
                return categoryRepository.save(category);
        }

        public boolean deleteCategory(Long id) {
                // Prevent deletion if transactions exist for this category
                long txCount = transactionRepository.countByCategory_Id(id);
                if (txCount > 0) return false;
                if (!categoryRepository.existsById(id)) return false;
                categoryRepository.deleteById(id);
                return true;
        }

    public List<RecentTransactionDto> getRecentTransactions(Long userId, int limit) {
        List<Transaction> list = (userId != null)
                ? transactionRepository.findByUser_Id(userId)
                : transactionRepository.findAll();

        return list.stream()
                .sorted(Comparator.comparing((Transaction t) -> t.getDate(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(t -> new RecentTransactionDto(
                        t.getId(),
                        t.getType(),
                        t.getAmount(),
                        t.getDescription(),
                        t.getDate() != null ? t.getDate().toString() : null,
                        t.getCategory() != null ? t.getCategory().getName() : null
                ))
                .collect(Collectors.toList());
    }

        // Transaction operations (simplified helpers)
        public java.util.List<Transaction> getTransactions(Long userId) {
                return (userId != null) ? transactionRepository.findByUser_Id(userId) : transactionRepository.findAll();
        }

        public java.util.List<Transaction> getTransactions(Long userId, Long categoryId, java.time.LocalDate fromDate, java.time.LocalDate toDate, Integer page, Integer size) {
                java.util.List<Transaction> list = getTransactions(userId);
                java.util.stream.Stream<Transaction> stream = list.stream();
                if (categoryId != null) {
                        stream = stream.filter(t -> t.getCategory() != null && categoryId.equals(t.getCategory().getId()));
                }
                if (fromDate != null) {
                        stream = stream.filter(t -> t.getDate() != null && !t.getDate().isBefore(fromDate));
                }
                if (toDate != null) {
                        stream = stream.filter(t -> t.getDate() != null && !t.getDate().isAfter(toDate));
                }
                java.util.List<Transaction> filtered = stream.collect(java.util.stream.Collectors.toList());
                if (page != null && size != null && page >= 0 && size > 0) {
                        int fromIndex = page * size;
                        if (fromIndex >= filtered.size()) return java.util.Collections.emptyList();
                        int toIndex = Math.min(filtered.size(), fromIndex + size);
                        return filtered.subList(fromIndex, toIndex);
                }
                return filtered;
        }

        public Transaction createTransaction(String type, Double amount, String description, java.time.LocalDate date, Long categoryId, Long userId) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category == null) return null;
                User user = null;
                if (userId != null) user = userRepository.findById(userId).orElse(null);
                if (user == null) user = getOrCreateDummyUser();
                Transaction transaction = new Transaction();
                transaction.setType(type);
                transaction.setAmount(amount);
                transaction.setDescription(description);
                transaction.setDate(date);
                transaction.setCategory(category);
                transaction.setUser(user);
                return transactionRepository.save(transaction);
        }

        public Transaction updateTransaction(Long id, String type, Double amount, String description, java.time.LocalDate date, Long categoryId) {
                Transaction transaction = transactionRepository.findById(id).orElse(null);
                if (transaction == null) return null;
                if (type != null) transaction.setType(type);
                if (amount != null) transaction.setAmount(amount);
                if (description != null) transaction.setDescription(description);
                if (date != null) transaction.setDate(date);
                if (categoryId != null) {
                    Category category = categoryRepository.findById(categoryId).orElse(null);
                    if (category != null) transaction.setCategory(category);
                }
                return transactionRepository.save(transaction);
        }

        public boolean deleteTransaction(Long id) {
                if (!transactionRepository.existsById(id)) return false;
                transactionRepository.deleteById(id);
                return true;
        }

        public void resetDummyData() {
                User dummy = getOrCreateDummyUser();
                transactionRepository.findByUser_Id(dummy.getId()).forEach(t -> transactionRepository.deleteById(t.getId()));
                categoryRepository.findByUser_Id(dummy.getId()).forEach(c -> categoryRepository.deleteById(c.getId()));
                categoryRepository.save(new Category("Lebensmittel", "Essen und Trinken", dummy));
                categoryRepository.save(new Category("Freizeit", "Sport, Kino, etc.", dummy));
                categoryRepository.save(new Category("Gehalt", "Monatliches Einkommen", dummy));
        }
}
