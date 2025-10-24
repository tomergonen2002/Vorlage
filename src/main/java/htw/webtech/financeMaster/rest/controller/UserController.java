package htw.webtech.financeMaster.rest.controller;

import htw.webtech.financeMaster.persistence.entity.User;
import htw.webtech.financeMaster.persistence.repository.UserRepository;
import htw.webtech.financeMaster.persistence.entity.Category;
import htw.webtech.financeMaster.persistence.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.onrender.com"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getEmail() == null || user.getPasswordHash() == null || user.getName() == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        String hashed = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashed);
        User saved = userRepository.save(user);
        // Ensure default categories for new user
        long categoryCount = categoryRepository.countByUser_Id(saved.getId());
        if (categoryCount == 0L) {
            categoryRepository.save(new Category("Lebensmittel", "Ausgaben für Essen und Trinken", saved));
            categoryRepository.save(new Category("Gehalt", "Monatliches Einkommen", saved));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (user.getEmail() == null || user.getPasswordHash() == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }
        
        // Special case: dummy user login (no password required)
        if (user.getEmail().equals("guest@finance.local")) {
            Optional<User> found = userRepository.findByEmail("guest@finance.local");
            User dummyUser;
            if (found.isEmpty()) {
                // Create dummy user if not exists
                User dummy = new User("Gast", "guest@finance.local", "");
                dummyUser = userRepository.save(dummy);
            } else {
                dummyUser = found.get();
            }
            
            // Check if dummy user has categories, if not create defaults
            long categoryCount = categoryRepository.countByUser_Id(dummyUser.getId());
            if (categoryCount == 0L) {
                Category lebensmittel = new Category("Lebensmittel", "Ausgaben für Essen und Trinken", dummyUser);
                Category gehalt = new Category("Gehalt", "Monatliches Einkommen", dummyUser);
                categoryRepository.save(lebensmittel);
                categoryRepository.save(gehalt);
            }
            
            return ResponseEntity.ok(dummyUser);
        }
        
        // Regular user login
        Optional<User> found = userRepository.findByEmail(user.getEmail());
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        User dbUser = found.get();
        if (BCrypt.checkpw(user.getPasswordHash(), dbUser.getPasswordHash())) {
            // Ensure default categories for returning user if none exist yet
            long categoryCount = categoryRepository.countByUser_Id(dbUser.getId());
            if (categoryCount == 0L) {
                categoryRepository.save(new Category("Lebensmittel", "Ausgaben für Essen und Trinken", dbUser));
                categoryRepository.save(new Category("Gehalt", "Monatliches Einkommen", dbUser));
            }
            return ResponseEntity.ok(dbUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }
    }
}
