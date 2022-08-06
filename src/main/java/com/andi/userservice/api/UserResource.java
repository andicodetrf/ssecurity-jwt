package com.andi.userservice.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.andi.userservice.domain.Role;
import com.andi.userservice.domain.User;
import com.andi.userservice.service.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;


//use postman to pass the data or config some data whenever app start (see App entry) 

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserResource {
	private final UserService userService;
	
	@GetMapping("/users")
	public ResponseEntity<List<User>>apiGetUsers(){
		//.ok() = 200
		return ResponseEntity.ok().body(userService.getUsers());
	}
	
	@PostMapping("/user/save")
	public ResponseEntity<User>apiSaveUser(@RequestBody User user){
		//.created() = 201		
		//can create whatever string or retrieve from current path - localhost8080/api/user/save - then turn it into uri string.
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
		//.created takes in a uri string
		//spring will take this uri and place it in the headers
		return ResponseEntity.created(uri).body(userService.saveUser(user));
	}
	
	@PostMapping("/role/save")
	public ResponseEntity<Role>apiSaveRole(@RequestBody Role role){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
		return ResponseEntity.created(uri).body(userService.saveRole(role));
	}
	
	//can make ResponseEntity to return whatever you want
	//but this method is going to return void so can use ? in ResponseEntity
	@PostMapping("/role/addtouser")
	public ResponseEntity<?>apiAddRoleToUser(@RequestBody RoleToUserForm form){
		//need a way to get the name and role from request to pass into addRoleToUser service - via form
		userService.addRoleToUser(form.getUsername(), form.getRoleName());
		
		//since not returning anything, just return ok to client
		return ResponseEntity.ok().build();
	}
}


@Data
class RoleToUserForm {
	private String username;
	private String roleName;
	
}
