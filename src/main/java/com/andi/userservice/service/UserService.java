package com.andi.userservice.service;

import java.util.List;

import com.andi.userservice.domain.Role;
import com.andi.userservice.domain.User;

public interface UserService {
	//define methods that will be used
	
	User saveUser(User user);
	Role saveRole(Role role);
	void addRoleToUser(String username, String roleName);
	User getUser(String username);
	
	//in real app - will return a page instead of trying to loading all from db/system
	List<User>getUsers();  
}
