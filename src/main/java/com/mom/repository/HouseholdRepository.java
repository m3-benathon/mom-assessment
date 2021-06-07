package com.mom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mom.model.Household;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {

}
