package com.softuni.finalexam.repository;

import com.softuni.finalexam.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
