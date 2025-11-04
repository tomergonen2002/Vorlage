package htw.webtech.financeMaster.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import htw.webtech.financeMaster.persistence.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	java.util.List<Transaction> findByUser_Id(Long userId);
	long countByCategory_Id(Long categoryId);
}
