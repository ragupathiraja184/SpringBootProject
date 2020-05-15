package com.zeptoh.benchmarking.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.zeptoh.benchmarking.model.Login;
import com.zeptoh.benchmarking.repository.interfaces.LoginRepositoryCustom;

// No need implementation, just one interface, and you have CRUD, thanks Spring Data
public interface LoginRepository extends MongoRepository<Login, Long>/*, LoginRepositoryCustom*/ {
    Login findByUserIdAndPassword(String userId, String password);
    Login findByUserId(String userId);
    Login findById(String userId);
    List<Login> findByClientId(String clientId);
	List<Login> findByRole(String role);
	List<Login> findByClientIdAndRole(String clientId,String role);
}