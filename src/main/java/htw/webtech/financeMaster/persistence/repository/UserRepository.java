package htw.webtech.financeMaster.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import htw.webtech.financeMaster.persistence.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	java.util.Optional<User> findByEmail(String email);
}
