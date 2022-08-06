package com.andi.userservice.filter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	//need a constructor to bring in the authentication manager - will be calling AM to authenticate user
	private final AuthenticationManager authenticationManager;
	
	public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	

	//whenever the user tries to log in
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		
		//to pass json object as reqbody, can use objectmapper. But we use form in this example
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		log.info("Username is: {}; Password is {}", username, password);
		
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		
		//after the above, we call AM, pass user credentials.
		return authenticationManager.authenticate(authenticationToken);

		
		//if attemptAuthentication is unsuccessful, SS will throw error.  
		//if attemptAuthentication is successful, successfulAuthentication method will be called,
		//then we have to send in the access token and refresh token to the user
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
		
		//User from core.userdetails
		//getPrincipal here returns an object which is the user that's successfully authenticated
		//because its returning an object, doing authentication.getPrincipal() is gonna raise error; hence we need to cast it to Type User 
		User user = (User)authentication.getPrincipal(); 
		
		
		//the signature
		Algorithm algorithm = Algorithm.HMAC256(System.getenv("KW").getBytes());
		
		
		//Token construct
		//withSubject can be anything to id the user
		//withIssuer - eg. company name or author of this token. this example uses the app's url
		//current time + 10 mins * 60 secs * 10000 (to conv to milisecs) 
		//withClaim - pass in all the roles associated to user. getAuthority simply gives you a string which you collect into a list.
		String access_token = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
				.withIssuer(request.getRequestURL().toString())
				.withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.sign(algorithm);

		
		//longer validity - example 30 mins, a week etc
		String refresh_token = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
				.withIssuer(request.getRequestURL().toString())
				.sign(algorithm);
		
		//send it back to client via response headers
		//response.setHeader("access_token", access_token);
		//response.setHeader("refresh_token", refresh_token);
		
		//send it back to client as json response body
		Map<String, String> tokens = new HashMap<>();
		tokens.put("access_token", access_token);
		tokens.put("refresh_token", refresh_token);
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE); 
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
		
	}

}
