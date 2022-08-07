package com.andi.userservice.api;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


//use postman to pass the data or config some data whenever app start (see App entry) 

@Slf4j
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
	
	@GetMapping("/token/refresh")
	public void apiRefreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		log.info("in api/token/refresh: {}", authorizationHeader);
		
		if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			log.info("in authorization refreshtoken path: {}", authorizationHeader);
			try {
				
				//get the refresh token, verify, get the user from the sub; create a new access token 
				String refresh_token = authorizationHeader.substring("Bearer ".length());
				Algorithm algorithm = Algorithm.HMAC256(System.getenv("KW").getBytes());
				JWTVerifier verifier = JWT.require(algorithm).build();
				DecodedJWT decodedJWT = verifier.verify(refresh_token);
				String username = decodedJWT.getSubject();
				User user = userService.getUser(username);
				
				//withClaim - not getting roles from user.getAuthorities coz the user in line94 is from our user class & not core.userdetails User
				String access_token = JWT.create()
						.withSubject(user.getUsername())
						.withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
						.withIssuer(request.getRequestURL().toString())
						.withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
						.sign(algorithm);

				
				//send it back to client as json response body
				Map<String, String> tokens = new HashMap<>();
				tokens.put("access_token", access_token);
				tokens.put("refresh_token", refresh_token);
				
				response.setContentType(MediaType.APPLICATION_JSON_VALUE); 
				new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	
				//no need to do another authentication hence can remove UsernamePasswordAuthenticationToken & SecurityContextHolder
				
			} catch ( Exception exception) {
				
				log.error("Refresh token error: {}", exception.getMessage());
				response.setHeader("refres_token_error", exception.getMessage());
				
				response.setStatus(HttpStatus.FORBIDDEN.value());
				Map<String, String> error = new HashMap<>();
				error.put("error_msg", exception.getMessage());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), error);
				
			}
			
		} else {
			//if theres no authorization header with Bearer xxx (means no refresh/access token)
			throw new RuntimeException("Refresh token is missing");
		}
	
	}
	
}


@Data
class RoleToUserForm {
	private String username;
	private String roleName;
	
}
