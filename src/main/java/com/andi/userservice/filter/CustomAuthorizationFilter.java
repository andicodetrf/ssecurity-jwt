package com.andi.userservice.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

//to intercept every request and look at the token, process the token and determine the user's permission
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

	//filter each req, holds logic to check if the user has acces
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if(request.getServletPath().equals("/api/login") || request.getServletPath().equals("/api/token/refresh")) {
			log.info("let req through");
			//if the req is this path, dont need to do anything, just let it pass with below filterChain.doFilter 
			//so ppl can login or retrieve new access token with refresh token
			filterChain.doFilter(request, response);
		} else {
			
			//first time login with access token
			log.info("HERE 1");
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				try {
					
					String token = authorizationHeader.substring("Bearer ".length());
					Algorithm algorithm = Algorithm.HMAC256(System.getenv("KW").getBytes());
					JWTVerifier verifier = JWT.require(algorithm).build();
					DecodedJWT decodedJWT = verifier.verify(token);
					String username = decodedJWT.getSubject();
					String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
					
					//have to do this conversion that extends/comply GA and we can do that with SGA coz thats what SS is expecting as roles of users,
					// we can't pass as array of strings 
					Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
					Arrays.stream(roles).forEach(role -> {
						authorities.add(new SimpleGrantedAuthority(role));
					});
					
					//null 2nd arg coz we dont have the password
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
					
					
					//set user in SecurityContextHolder - this is how we tell SS that here's the user, here's the username, here's their roles
					//SS is gonna look at the user and roles and determine what they can access
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
					
					
					//finally, doFilter to let the req pass through
					filterChain.doFilter(request, response);
					
					
					
				} catch ( Exception exception) {
					log.info("HERE 3");
					log.error("Error logging in: {}", exception.getMessage());
					response.setHeader("errorsss", exception.getMessage());
//					response.sendError(HttpStatus.FORBIDDEN.value());
					
					//alternative to sendError; send json body message
					response.setStatus(HttpStatus.FORBIDDEN.value());
					Map<String, String> error = new HashMap<>();
					error.put("error_msg", exception.getMessage());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					new ObjectMapper().writeValue(response.getOutputStream(), error);
					
					
				}
				
			} else {
				log.info("HERE 2");
				//let req continue
				filterChain.doFilter(request, response);
				
			}
		}
		
	}
	
	

}
