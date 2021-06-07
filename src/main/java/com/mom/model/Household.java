package com.mom.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "household")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Household {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "housing_type", nullable = false)
	private String housingType;

	@OneToMany(mappedBy = "household", cascade = CascadeType.ALL)
	private List<FamilyMember> familyMembers;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHousingType() {
		return housingType;
	}

	public void setHousingType(String housingType) {
		this.housingType = housingType;
	}

	public List<FamilyMember> getFamilyMembers() {
		return familyMembers;
	}

	public void setFamilyMembers(List<FamilyMember> familyMembers) {
		this.familyMembers = familyMembers;
	}

}
