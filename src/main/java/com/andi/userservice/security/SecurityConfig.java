package com.andi.userservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.andi.userservice.filter.CustomAuthenticationFilter;

import lombok.RequiredArgsConstructor;


//config annotation so it'll be picked up by spring
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	//we need to override certain methods from web security adapter (main security class)
	
	//Interface provided by SS.
	//With RAC, it'll create a constructor & inject userDetailsService into the constructor - depinject. 
	private final UserDetailsService userDetailsService; //interface var
	private final BCryptPasswordEncoder bCryptPasswordEncoder; //class var 
	//need to create the above 2 beans to tell spring how we want to upload user (1st) and password encoder (2nd)
	//to override 1 method of UserDetailsService - we can implement it in our UserServiceImpl 
	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}
	
	
	//customize the session type to use JWT instead of default SS cookies
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//first, disable csrf
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeRequests().anyRequest().permitAll(); //we're gonna allow everyone access the app at this point
		
		//we need to add a filter - an authentication filter so we can check the user whenever they try to log in
		//To tell this config about the filter, we can add Filter with our custom auth filter
		//CustomAuthenticationFilter takes in authenticationManager as a param. 
		http.addFilter(new CustomAuthenticationFilter(authenticationManagerBean()));
	}
	
	

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	//the above is valid as its a method in WebSecurityConfigurerAdapter
	/**
	 * Override this method to expose the {@link AuthenticationManager} from
	 * {@link #configure(AuthenticationManagerBuilder)} to be exposed as a Bean. For
	 * example:
	 *
	 * <pre>
	 * &#064;Bean(name name="myAuthenticationManager")
	 * &#064;Override
	 * public AuthenticationManager authenticationManagerBean() throws Exception {
	 *     return super.authenticationManagerBean();
	 * }
	 * </pre>
	 * @return the {@link AuthenticationManager}
	 * @throws Exception
	 
	 * public AuthenticationManager authenticationManagerBean() throws Exception {
	 *	return new AuthenticationManagerDelegator(this.authenticationBuilder, this.context);
	 * }
	 **/
 
	

}
