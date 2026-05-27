package org.kriba.users.service;

import org.kriba.users.dto.AuthResponse;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.dto.ModifyRequest;
import org.kriba.users.dto.ModifyResponse;
import org.kriba.users.dto.RegisterRequest;
import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
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

	public ModifyResponse modifyData(ModifyRequest request) {

	    User user = userRepository.findByEmail(request.loginRequest().email())
	            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

	    if (!passwordEncoder.matches(
	            request.loginRequest().password(),
	            user.getPassword())) {
	        throw new IllegalArgumentException("Contraseña incorrecta");
	    }
	    
	    String newEmail = request.newEmail();

	    if (newEmail != null && !newEmail.isBlank()
	            && !newEmail.equals(user.getEmail())
	            && userRepository.existsByEmail(newEmail)) {
	        throw new IllegalArgumentException("El email ya está en uso");
	    }
	    if (request.newUsername() != null && !request.newUsername().isBlank()) {
	        user.setUsername(request.newUsername());
	    }
	    if (newEmail != null && !newEmail.isBlank()) {
	        user.setEmail(newEmail);
	    }
	    if (request.newPassword() != null && !request.newPassword().isBlank()) {
	        user.setPassword(passwordEncoder.encode(request.newPassword()));
	    }

	    User userSaved = userRepository.save(user);

	    LoginRequest updatedLogin = new LoginRequest(
	            userSaved.getEmail(),
	            request.newPassword() != null && !request.newPassword().isBlank()
	                    ? request.newPassword()
	                    : request.loginRequest().password()
	    );

	    return new ModifyResponse(
	            updatedLogin,
	            userSaved.getUsername()
	    );
	}

}
