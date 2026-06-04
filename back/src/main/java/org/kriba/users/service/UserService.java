package org.kriba.users.service;

import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.bookmarks.repository.SavedNewsRepository;
import org.kriba.subscriptions.repository.SubscriptionRepository;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.dto.ModifyRequest;
import org.kriba.users.dto.RegisterRequest;
import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {

    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SavedNewsRepository savedNewRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public UserService(
            InteractionRepository interactionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SavedNewsRepository savedNewRepository,
            SubscriptionRepository subscriptionRepository,
            EmailService emailService
    ) {
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.savedNewRepository = savedNewRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado en Kriba");
        }

        UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .verified(false)
                .verificationToken(uuid)
                .dailyAiLimit(3)
                .build();
        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(savedUser, uuid.toString());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .dailyAiLimit(savedUser.getDailyAiLimit())
                .build();
    }
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        if ((user.getVerified() == false)) {
            throw new IllegalArgumentException("Debes verificar tu correo antes de iniciar sesión");
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .dailyAiLimit(user.getDailyAiLimit())
                .build();
    }

    @Transactional
    public AuthResponse modifyData(ModifyRequest request) {
        AuthResponse currentAuth = login(request.loginRequest());
        User user = userRepository.findById(currentAuth.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (request.newEmail() != null && !request.newEmail().isBlank()
                && !request.newEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(request.newEmail())) {
                throw new IllegalArgumentException("El email ya está en uso");
            }

            UUID uuid = UUID.randomUUID();
            user.setEmail(request.newEmail());
            user.setVerified(false);
            user.setVerificationToken(uuid);

            userRepository.save(user);
            emailService.sendVerificationEmail(user, uuid.toString());
        }

        if (request.newUsername() != null && !request.newUsername().isBlank()) {
            user.setUsername(request.newUsername());
        }

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        User saved = userRepository.save(user);
        return AuthResponse.builder()
                .userId(saved.getId())
                .username(saved.getUsername())
                .dailyAiLimit(saved.getDailyAiLimit())
                .build();
    }

    @Transactional
    public void deleteAccount(LoginRequest request) {
        AuthResponse currentAuth = login(request);
        long userId = currentAuth.userId();
        interactionRepository.deleteAllByUserId(userId);
        savedNewRepository.deleteAll(savedNewRepository.findAllByUserId(userId));
        subscriptionRepository.deleteAll(subscriptionRepository.findAllByUserId(userId));
        userRepository.deleteById(userId);
    }

    @Transactional
    public void verifyAccount(UUID uuid) {
        User user = userRepository.findByVerificationToken(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetDailyAiLimits() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setDailyAiLimit(3));
        userRepository.saveAll(users);
    }
}