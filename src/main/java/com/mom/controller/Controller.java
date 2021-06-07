package com.mom.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mom.exception.ResourceNotFoundException;
import com.mom.model.FamilyMember;
import com.mom.model.Household;
import com.mom.repository.FamilyMemberRepository;
import com.mom.repository.HouseholdRepository;

@RestController
@RequestMapping("/api/v1")
public class Controller {

	@Autowired
	private HouseholdRepository householdRepository;

	@Autowired
	private FamilyMemberRepository familyMemberRepository;

	@GetMapping("/households")
	public List<Household> getAllHouseholds() {
		return householdRepository.findAll();
	}

	@GetMapping("/household/{id}")
	public ResponseEntity<Household> getHouseholdById(@PathVariable Long id) throws ResourceNotFoundException {
		Household household = householdRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Household not found. ID: " + id));
		return ResponseEntity.ok().body(household);
	}

	@PostMapping("/household")
	public Household createHousehold(@Valid @RequestBody Household household) {
		return householdRepository.save(household);
	}

	@DeleteMapping("/household/{id}")
	public Map<String, Boolean> deleteHouseholdById(@PathVariable(value = "id") Long id)
			throws ResourceNotFoundException {
		Household household = householdRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Household not found. ID: " + id));

		householdRepository.delete(household);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}

	@PostMapping("/household/{householdId}/familyMember")
	public FamilyMember createFamilyMember(@PathVariable(value = "householdId") Long householdId,
			@Valid @RequestBody FamilyMember familyMember) throws ResourceNotFoundException {
		Household household = householdRepository.findById(householdId)
				.orElseThrow(() -> new ResourceNotFoundException("Household not found. ID: " + householdId));
		familyMember.setHousehold(household);

		if (familyMember.getSpouse() != null && familyMember.getSpouse().getId() != null) {
			FamilyMember spouse = familyMemberRepository.findById(familyMember.getSpouse().getId()).orElseThrow(
					() -> new ResourceNotFoundException("Spouse not found. ID: " + familyMember.getSpouse().getId()));

			if (spouse.getHousehold().getId() != household.getId()) {
				throw new ResourceNotFoundException("Spouse (ID: " + familyMember.getSpouse().getId()
						+ ") not found in Household (ID: " + householdId + ")");
			}

			familyMember.setSpouse(spouse);
		}

		FamilyMember savedFamilyMember = familyMemberRepository.save(familyMember);

		if (savedFamilyMember.getSpouse() != null) {
			savedFamilyMember.getSpouse().setSpouse(savedFamilyMember);
			familyMemberRepository.save(savedFamilyMember.getSpouse());
		}

		return savedFamilyMember;
	}

	@DeleteMapping("/household/{householdId}/familyMember/{familyMemberId}")
	public Map<String, Boolean> deleteFamilyMemberById(@PathVariable(value = "householdId") Long householdId,
			@PathVariable(value = "familyMemberId") Long familyMemberId) throws ResourceNotFoundException {
		Household household = householdRepository.findById(householdId)
				.orElseThrow(() -> new ResourceNotFoundException("Household not found. ID: " + householdId));

		FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
				.orElseThrow(() -> new ResourceNotFoundException("Family Member not found. ID: " + familyMemberId));

		if (familyMember.getHousehold().getId() != household.getId()) {
			throw new ResourceNotFoundException(
					"Family Member (ID: " + familyMemberId + ") not found in Household (ID: " + householdId + ")");
		}

		if (familyMember.getSpouse() != null) {
			familyMember.getSpouse().setSpouse(null);
			familyMemberRepository.save(familyMember.getSpouse());
		}

		familyMemberRepository.delete(familyMember);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}
}
