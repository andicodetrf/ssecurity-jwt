package com.andi.userservice;

import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.andi.userservice.domain.Role;
import com.andi.userservice.domain.User;
import com.andi.userservice.service.UserService;

@SpringBootApplication
public class UserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}
	
	//mock/seed data
	//add Bean annotation otherwise wont get picked up by Spring when app runs
	@Bean
	CommandLineRunner run(UserService userService) {
		//will run after the application has initialized
		return args -> {
			//1st arg is id - unnecessary to pass as Jpa will insert automatically, hence null
			userService.saveRole(new Role(null, "ROLE_USER"));
			userService.saveRole(new Role(null, "ROLE_MANAGER"));
			userService.saveRole(new Role(null, "ROLE_ADMIN"));
			userService.saveRole(new Role(null, "ROLE_SUPER_ADMIN"));
			
			
			userService.saveUser(new User(null, "Brad Pitt", "brad", "BD123", new ArrayList<>()));
			userService.saveUser(new User(null, "John Depp", "john", "JD123", new ArrayList<>()));
			userService.saveUser(new User(null, "Chris Hemsworth", "chris", "CH123", new ArrayList<>()));
			userService.saveUser(new User(null, "Adam Sandler", "adam", "AS123", new ArrayList<>()));
			
			
			userService.addRoleToUser("brad", "ROLE_MANAGER");
			
			userService.addRoleToUser("john", "ROLE_ADMIN");
			
			userService.addRoleToUser("adam", "ROLE_USER");
			userService.addRoleToUser("adam", "ROLE_MANAGER");
			
			userService.addRoleToUser("chris", "ROLE_SUPER_ADMIN");
			userService.addRoleToUser("chris", "ROLE_ADMIN");
			userService.addRoleToUser("chris", "ROLE_USER");
		};
	}

}

//api/users getUsers
//[
// {"id":5,"name":"Brad Pitt","username":"brad","password":"BD123","roles":[{"id":2,"name":"ROLE_MANAGER"}]},
// {"id":6,"name":"John Depp","username":"john","password":"JD123","roles":[{"id":3,"name":"ROLE_ADMIN"}]},
// {"id":7,"name":"Chris Hemsworth","username":"chris","password":"CH123","roles":[{"id":4,"name":"ROLE_SUPER_ADMIN"},{"id":3,"name":"ROLE_ADMIN"},{"id":1,"name":"ROLE_USER"}]},
// {"id":8,"name":"Adam Sandler","username":"adam","password":"AS123","roles":[{"id":1,"name":"ROLE_USER"},{"id":2,"name":"ROLE_MANAGER"}]}
//]
		
		