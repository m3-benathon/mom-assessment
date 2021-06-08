package com.mom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mom.model.Household;

public interface HouseholdRepository extends JpaRepository<Household, Long>, HouseholdRepositoryCustom {

}
