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

    @Value("${app.default-password:User@123}")
    private String defaultPassword;

    public List<UserBean> getAllUsers() {
        return userRepository.findAll();
    }


    public UserBean login(String email, String rawPassword) {
        UserBean user = userRepository.findByEmail(email);

        if (user != null) {
            // Debugging logs to verify password matching issues
            System.out.println("DEBUG: Password typed in browser: [" + rawPassword + "]");
            System.out.println("DEBUG: Hash stored in Database: [" + user.getPassword() + "]");

            if (BCrypt.checkpw(rawPassword, user.getPassword())) {
                return user;
            } else {
                System.out.println("DEBUG: Password mismatch!");
                return null;
            }
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

    public void deleteUser(int userId) {
        userRepository.delete(userId);
    }
}