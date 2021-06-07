package com.mom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mom.model.FamilyMember;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

}
