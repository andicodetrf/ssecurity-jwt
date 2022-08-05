package com.andi.userservice.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.andi.userservice.domain.Role;
import com.andi.userservice.domain.User;
import com.andi.userservice.repo.RoleRepo;
import com.andi.userservice.repo.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Transactional 
@Slf4j
public class UserServiceImpl implements UserService {
	
	private final UserRepo userRepo;
	private final RoleRepo roleRepo;

	@Override
	public User saveUser(User user) {
		log.info("saving new user {} to db", user.getName());
		return userRepo.save(user);
	}

	@Override
	public Role saveRole(Role role) {
		log.info("saving new role {} to db", role.getName());
		return roleRepo.save(role);
	}

	@Override
	public void addRoleToUser(String username, String roleName) {
		log.info("adding role {} to user {}", roleName, username);
		User user = userRepo.findByUsername(username);
		Role role= roleRepo.findByName(roleName);
		user.getRoles().add(role);
	}

	@Override
	public User getUser(String username) {
		log.info("fetching user {}", username);
		return userRepo.findByUsername(username);
	}

	@Override
	public List<User> getUsers() {
		log.info("fetching all users");
		return userRepo.findAll();
	}

}
