package com.andi.userservice.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserServiceImpl implements UserService, UserDetailsService {
	
	private final UserRepo userRepo;
	private final RoleRepo roleRepo;
	
	//encode the password before saving it in db
	private final PasswordEncoder passwordEncoder;
	
	
	//we override this UserDetailsService to customize the way SS find users 
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		if(user == null) {
			log.error("user not found in db");
			throw new UsernameNotFoundException("user not found in db");
		} else {
			log.info("user found in db: {} ", username);
			
		}
		
		//find user's roles in db & create a list of authorities for the user.
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		user.getRoles().forEach(role -> {
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		});
		
		//by default, spring security has its own config list of users that will be returned by UserDetailsService original method.
		//thus we override the method & we find our own users from db; 
		//ask SS to return the SS-user from the core.userdetails user with our found db-user instead. 
		
		//FQN below so it's different from the User in line 42.
		//check this User's implementation to see what params it needs.
		//since 3rd param authorities is needed, see its imp. Collection<? extends GrantedAuthority> authorities
		//see imp of SimpleGrantedAuthority - its implementing GrantedAuthority so its a valid type to return
		//thus create a collection of roles associated to a user with class SimpleGrantedAuthority.
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
		
		//ss will now return the above user, conduct password comparison & check authorities etc. 
	}

	@Override
	public User saveUser(User user) {
		log.info("saving new user {} to db", user.getName());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
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
