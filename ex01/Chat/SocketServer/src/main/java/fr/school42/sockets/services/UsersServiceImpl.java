package fr.school42.sockets.services;

import fr.school42.sockets.models.User;
import fr.school42.sockets.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersServiceImpl implements UserService 
{
	private final UsersRepository usersRepo;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public UsersServiceImpl(UsersRepository usersRepo, PasswordEncoder passwordEncoder) {
		this.usersRepo = usersRepo;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean signUp(String username, String password) {
	    if (username == null || username.trim().isEmpty() || password == null || password.isEmpty())
			throw new IllegalArgumentException("Username and password cannot be empty");
	
		Optional<User> existingUser = usersRepo.findByUsername(username);
		if (existingUser.isPresent()) return false;

		String hashedPassword = passwordEncoder.encode(password);
	
		User newUser = new User(username, hashedPassword);
		usersRepo.save(newUser);
		return true;
	}
	
	@Override
	public boolean signIn(String username, String password) {
		Optional<User> userO = usersRepo.findByUsername(username);
		if (userO.isEmpty()) return false;

		User user = userO.get();
		return passwordEncoder.matches(password, user.getPassword());
	}
	
	@Override
	public User getUserByUsername(String username) { 
	    return usersRepo.findByUsername(username);
	} // ou safy al7mar, ra andek user repo tma.
}
