package htw.webtech.financeMaster.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import htw.webtech.financeMaster.persistence.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	java.util.List<Category> findByUser_Id(Long userId);
	long countByUser_Id(Long userId);
}
