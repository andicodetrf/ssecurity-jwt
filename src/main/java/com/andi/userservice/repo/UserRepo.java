package com.andi.userservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.andi.userservice.domain.User;

public interface UserRepo extends JpaRepository<User, Long>{
	User findByUsername(String username);
}
