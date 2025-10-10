package fr.school42.sockets.services;

import fr.school42.sockets.models.User;

public interface UserService {
	public boolean signUp(String username, String password);
	public boolean signIn(String username, String password);
}
