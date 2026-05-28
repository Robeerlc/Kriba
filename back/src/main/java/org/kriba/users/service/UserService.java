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
import java.util.List;

@Service
public class UserService {

	private final InteractionRepository interactionRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SavedNewsRepository savedNewRepository;
	private final SubscriptionRepository subscriptionRepository;

	

	public UserService(InteractionRepository interactionRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder, SavedNewsRepository savedNewRepository,
			SubscriptionRepository subscriptionRepository) {
		this.interactionRepository = interactionRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.savedNewRepository = savedNewRepository;
		this.subscriptionRepository = subscriptionRepository;
	}

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.findByEmail(request.email()).isPresent())
			throw new IllegalArgumentException("El email ya está registrado en Kriba");

		User user = User.builder().username(request.username()).email(request.email())
				.password(passwordEncoder.encode(request.password())).build();
		User savedUser = userRepository.save(user);

		return AuthResponse.builder().userId(savedUser.getId()).username(savedUser.getUsername())
				.dailyAiLimit(savedUser.getDailyAiLimit()).build();
	}

	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (!passwordEncoder.matches(request.password(), user.getPassword()))
			throw new IllegalArgumentException("Contraseña incorrecta");

		return AuthResponse.builder().userId(user.getId()).username(user.getUsername())
				.dailyAiLimit(user.getDailyAiLimit()).build();
	}

	public AuthResponse modifyData(ModifyRequest request) {

		AuthResponse currentAuth = login(request.loginRequest());
		User user = userRepository.findById(currentAuth.userId()).orElseThrow();

		String newEmail = request.newEmail();

		if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
			if (userRepository.existsByEmail(newEmail)) {
				throw new IllegalArgumentException("El email ya está en uso");
			}
			user.setEmail(newEmail);

		}
		if (request.newUsername() != null && !request.newUsername().isBlank()) {
			user.setUsername(request.newUsername());
		}
		
		if (request.newPassword() != null && !request.newPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.newPassword()));
		}

		User userSaved = userRepository.save(user);

		return AuthResponse.builder().userId(userSaved.getId())
				.username(userSaved.getUsername())
				.dailyAiLimit(userSaved.getDailyAiLimit())
				.build();
	}
	
	public void deleteAccount(LoginRequest request) {
		AuthResponse currentAuth = login(request);
		long userId = currentAuth.userId();
		interactionRepository.deleteAllByUserId(userId);
		savedNewRepository.findAllByUserId(userId).forEach(savedNewRepository::delete);
		subscriptionRepository.findAllByUserId(userId).forEach(subscriptionRepository::delete);
		userRepository.deleteById(userId);
	}

	@Scheduled(cron = "0 0 0 * * ?")
	@org.springframework.transaction.annotation.Transactional
	public void resetDailyAiLimits() {
		List<User> allUsers = userRepository.findAll();
		allUsers.forEach(user -> user.setDailyAiLimit(3));
		userRepository.saveAll(allUsers);
	}

}
