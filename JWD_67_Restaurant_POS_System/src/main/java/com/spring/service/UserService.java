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


    public UserBean login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return null;
        }

        // Use strip() to remove Unicode whitespace (Chrome autofill can include it)
        String normalizedEmail = email.strip().replaceAll("\\s+", "").toLowerCase();
        String normalizedPassword = rawPassword.strip();

        UserBean user = userRepository.findByEmail(normalizedEmail);

        if (user != null) {
            // Debugging logs to verify password matching issues
            System.out.println("DEBUG: Password typed in browser: [" + normalizedPassword + "]");
            System.out.println("DEBUG: Hash stored in Database: [" + user.getPassword() + "]");

            String stored = user.getPassword();
            if (stored == null || stored.isBlank()) {
                System.out.println("DEBUG: No password stored for user!");
                return null;
            }

            // Support both BCrypt-hashed passwords and legacy plain-text passwords.
            // New users created via admin are stored as BCrypt.
            boolean looksBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
            try {
                if (looksBcrypt) {
                    if (BCrypt.checkpw(normalizedPassword, stored)) {
                        return user;
                    }
                } else {
                    // Legacy fallback (plain text stored in DB)
                    if (normalizedPassword.equals(stored)) {
                        return user;
                    }
                }
            } catch (IllegalArgumentException e) {
                // Happens when stored password is not a valid BCrypt string
                System.out.println("DEBUG: Stored password is not a valid BCrypt hash.");
            }

            System.out.println("DEBUG: Password mismatch!");
            return null;
        }
        return null;
    }


    public void addUser(UserBean user, Integer adminId) {
        // 1. Set the creator ID from the session
        user.setCreatedBy(adminId);

        // 2. Hash the password before saving
        // This ensures the plain text password is never stored
        String hashedPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);

        // 3. Send to Repository
        userRepository.save(user);
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }


    public void updateUser(UserBean user, Integer adminId) {
        // 1. Fetch the existing record from the database to protect the password
        UserBean existingUser = userRepository.findById(user.getUserId());

        if (existingUser != null) {
            // 2. Keep the old password (since the edit modal has no password field)
            user.setPassword(existingUser.getPassword());

            // 3. Set the Admin ID who is performing this update
            user.setUpdatedBy(adminId);

            // 4. Save the changes through the repository
            userRepository.update(user);
        }
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