package com.spring.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.spring.model.UserBean;
import com.spring.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.default-password}")
    private String defaultPassword;

    public List<UserBean> getAllUsers(boolean includeDeleted) {
        return userRepository.findAll(includeDeleted);
    }

    public int countActiveUsersByName(String keyword) {
        return userRepository.countActiveUsersByName(keyword);
    }

    public List<UserBean> getActiveUsersPageByName(String keyword, int offset, int limit) {
        return userRepository.findActiveUsersPageByName(keyword, offset, limit);
    }

    public static class LoginResult {
        private final UserBean user;
        private final String status; // success, invalid, suspended

        public LoginResult(UserBean user, String status) {
            this.user = user;
            this.status = status;
        }

        public UserBean getUser() {
            return user;
        }

        public String getStatus() {
            return status;
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.strip().replaceAll("\\s+", "").toLowerCase();
    }


    public LoginResult login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return new LoginResult(null, "invalid");
        }

        // Use strip() to remove Unicode whitespace (Chrome autofill can include it)
        String normalizedEmail = email.strip().replaceAll("\\s+", "").toLowerCase();
        String normalizedPassword = rawPassword.strip();

        UserBean user = userRepository.findByEmail(normalizedEmail);

        if (user != null) {
            if (user.getStatus() != null && user.getStatus() == 1) {
                return new LoginResult(null, "suspended");
            }

            // Debugging logs to verify password matching issues
            System.out.println("DEBUG: Password typed in browser: [" + normalizedPassword + "]");
            System.out.println("DEBUG: Hash stored in Database: [" + user.getPassword() + "]");

            String stored = user.getPassword();
            if (stored == null || stored.isBlank()) {
                System.out.println("DEBUG: No password stored for user!");
                return new LoginResult(null, "invalid");
            }

            // Support both BCrypt-hashed passwords and legacy plain-text passwords.
            // New users created via admin are stored as BCrypt.
            boolean looksBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
            try {
                if (looksBcrypt) {
                    if (BCrypt.checkpw(normalizedPassword, stored)) {
                        userRepository.resetFailedLoginAttempts(user.getUserId());
                        return new LoginResult(user, "success");
                    }
                } else {
                    // Legacy fallback (plain text stored in DB)
                    if (normalizedPassword.equals(stored)) {
                        userRepository.resetFailedLoginAttempts(user.getUserId());
                        return new LoginResult(user, "success");
                    }
                }
            } catch (IllegalArgumentException e) {
                // Happens when stored password is not a valid BCrypt string
                System.out.println("DEBUG: Stored password is not a valid BCrypt hash.");
            }

            userRepository.incrementFailedAttemptsAndAutoSuspend(user.getUserId());
            UserBean latest = userRepository.findById(user.getUserId());
            if (latest != null && latest.getStatus() != null && latest.getStatus() == 1) {
                return new LoginResult(null, "suspended");
            }

            System.out.println("DEBUG: Password mismatch!");
            return new LoginResult(null, "invalid");
        }
        return new LoginResult(null, "invalid");
    }


    public String addUser(UserBean user, Integer adminId) {
        if (user == null) return "invalid_input";
        if (user.getUserName() == null || user.getUserName().isBlank()) return "name_required";
        if (user.getEmail() == null || user.getEmail().isBlank()) return "email_required";
        if (user.getRoleId() == null || user.getRoleId() < 1 || user.getRoleId() > 4) return "invalid_role";

        String email = normalizeEmail(user.getEmail());
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "invalid_email";
        if (userRepository.existsActiveByEmail(email)) return "email_exists";

        // 1. Set the creator ID from the session
        user.setCreatedBy(adminId);
        user.setEmail(email);

        // 2. Hash the password before saving
        // This ensures the plain text password is never stored
        String hashedPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);

        // 3. Send to Repository
        userRepository.save(user);
        return null;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }


    public String updateUser(UserBean user, Integer adminId) {
        if (user == null || user.getUserId() == null) return "invalid_input";
        if (user.getUserName() == null || user.getUserName().isBlank()) return "name_required";
        if (user.getEmail() == null || user.getEmail().isBlank()) return "email_required";
        if (user.getRoleId() == null || user.getRoleId() < 1 || user.getRoleId() > 4) return "invalid_role";

        String email = normalizeEmail(user.getEmail());
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "invalid_email";
        if (userRepository.existsActiveByEmailExceptId(email, user.getUserId())) return "email_exists";

        // 1. Fetch the existing record from the database to protect the password
        UserBean existingUser = userRepository.findById(user.getUserId());

        if (existingUser != null) {
            // 2. Keep the old password (since the edit modal has no password field)
            user.setPassword(existingUser.getPassword());
            user.setEmail(email);

            // 3. Set the Admin ID who is performing this update
            user.setUpdatedBy(adminId);

            // 4. Save the changes through the repository
            userRepository.update(user);
            return null;
        }
        return "user_not_found";
    }


    public UserBean getUserById(Integer userId) {
        UserBean user = userRepository.findById(userId);
        if (user == null) {
            System.out.println("WARNING: User not found with ID: " + userId);
        }
        return user;
    }

    public void deleteUser(int userId, Integer adminId) {
        userRepository.delete(userId, adminId);
    }

    public void updateUserStatus(int userId, int status, Integer adminId) {
        userRepository.updateStatus(userId, status, adminId);
    }
}