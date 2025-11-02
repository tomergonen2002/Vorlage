package htw.webtech.financeMaster.service;

import htw.webtech.financeMaster.persistence.entity.Transaction;
import htw.webtech.financeMaster.persistence.repository.TransactionRepository;
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

    public FinanceService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
}
