package com.fonepay.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fonepay.gateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class GatewayApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
		String raw = "password123";
		String generatedHash = passwordEncoder.encode(raw);
		System.out.println("NEW_HASH_FOR_PASSWORD123: " + generatedHash);

		userRepository.findAll().forEach(user -> {
			boolean matches = passwordEncoder.matches(raw, user.getPassword());
			System.out.println("USER: " + user.getEmail() + " | DB_HASH: " + user.getPassword() + " | MATCHES: " + matches);
		});
	}

}
