package com.mom.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestParam;
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

	@GetMapping("/households/getQualifyForStudentEncouragementBonus")
	public List<Household> getQualifyForStudentEncouragementBonus(
			@RequestParam(required = false) List<String> housingTypes,
			@RequestParam(required = false, defaultValue = "1") Integer householdSizeMin,
			@RequestParam(required = false) Integer householdSizeMax,
			@RequestParam(required = false) Double totalAnnualIncomeMin) {
		Map<String, Object> searchParams = new HashMap<>();
		if (housingTypes != null) {
			searchParams.put("housingTypes", housingTypes);
		}
		searchParams.put("householdSizeMin", householdSizeMin);
		if (householdSizeMax != null) {
			searchParams.put("householdSizeMax", householdSizeMax);
		}
		if (totalAnnualIncomeMin != null) {
			searchParams.put("totalAnnualIncomeMin", totalAnnualIncomeMin);
		}
		searchParams.put("totalAnnualIncomeMax", 150000.0);
		List<Household> households = householdRepository.search(searchParams);
		List<Household> qualifyForStudentEncouragementBonus = new ArrayList<>();

		for (Household household : households) {
			for (FamilyMember familyMember : household.getFamilyMembers()) {
				if (getAge(familyMember.getDateOfBirth()) < 16) {
					qualifyForStudentEncouragementBonus.add(household);
					break;
				}
			}
		}

		return qualifyForStudentEncouragementBonus;
	}

	@GetMapping("/households/getQualifyForFamilyTogethernessScheme")
	public List<Household> getQualifyForFamilyTogethernessScheme(
			@RequestParam(required = false) List<String> housingTypes,
			@RequestParam(required = false, defaultValue = "3") Integer householdSizeMin,
			@RequestParam(required = false) Integer householdSizeMax,
			@RequestParam(required = false) Double totalAnnualIncomeMin,
			@RequestParam(required = false) Double totalAnnualIncomeMax) {
		Map<String, Object> searchParams = new HashMap<>();
		if (housingTypes != null) {
			searchParams.put("housingTypes", housingTypes);
		}
		searchParams.put("householdSizeMin", householdSizeMin);
		if (householdSizeMax != null) {
			searchParams.put("householdSizeMax", householdSizeMax);
		}
		if (totalAnnualIncomeMin != null) {
			searchParams.put("totalAnnualIncomeMin", totalAnnualIncomeMin);
		}
		if (totalAnnualIncomeMax != null) {
			searchParams.put("totalAnnualIncomeMax", totalAnnualIncomeMax);
		}
		List<Household> households = householdRepository.search(searchParams);
		List<Household> qualifyForFamilyTogethernessScheme = new ArrayList<>();

		for (Household household : households) {
			Boolean hasChildLessThan18 = false;
			Boolean hasHusbandAndWife = false;
			for (FamilyMember familyMember : household.getFamilyMembers()) {
				if (getAge(familyMember.getDateOfBirth()) < 18) {
					hasChildLessThan18 = true;
				}
				if (familyMember.getSpouse() != null) {
					if ((familyMember.getGender().equals("Male") || familyMember.getSpouse().getGender().equals("Male"))
							&& (familyMember.getGender().equals("Female")
									|| familyMember.getSpouse().getGender().equals("Female"))) {
						hasHusbandAndWife = true;
					}
				}
				if (hasChildLessThan18 && hasHusbandAndWife) {
					break;
				}
			}
			if (hasChildLessThan18 && hasHusbandAndWife) {
				qualifyForFamilyTogethernessScheme.add(household);
			}
		}

		return qualifyForFamilyTogethernessScheme;
	}

	@GetMapping("/households/getQualifyForHDBElderBonus")
	public List<Household> getQualifyForHDBElderBonus(
			@RequestParam(required = false, defaultValue = "1") Integer householdSizeMin,
			@RequestParam(required = false) Integer householdSizeMax,
			@RequestParam(required = false) Double totalAnnualIncomeMin,
			@RequestParam(required = false) Double totalAnnualIncomeMax) {
		Map<String, Object> searchParams = new HashMap<>();
		List<String> housingTypes = new ArrayList<>();
		housingTypes.add("HDB");
		searchParams.put("housingTypes", housingTypes);
		searchParams.put("householdSizeMin", householdSizeMin);
		if (householdSizeMax != null) {
			searchParams.put("householdSizeMax", householdSizeMax);
		}
		if (totalAnnualIncomeMin != null) {
			searchParams.put("totalAnnualIncomeMin", totalAnnualIncomeMin);
		}
		if (totalAnnualIncomeMax != null) {
			searchParams.put("totalAnnualIncomeMax", totalAnnualIncomeMax);
		}
		List<Household> households = householdRepository.search(searchParams);
		List<Household> qualifyForHDBElderBonus = new ArrayList<>();

		for (Household household : households) {
			for (FamilyMember familyMember : household.getFamilyMembers()) {
				if (getAge(familyMember.getDateOfBirth()) > 50) {
					qualifyForHDBElderBonus.add(household);
					break;
				}
			}
		}

		return qualifyForHDBElderBonus;
	}

	@GetMapping("/households/getQualifyForBabySunshineGrant")
	public List<Household> getQualifyForBabySunshineGrant(@RequestParam(required = false) List<String> housingTypes,
			@RequestParam(required = false, defaultValue = "2") Integer householdSizeMin,
			@RequestParam(required = false) Integer householdSizeMax,
			@RequestParam(required = false) Double totalAnnualIncomeMin,
			@RequestParam(required = false) Double totalAnnualIncomeMax) {
		Map<String, Object> searchParams = new HashMap<>();
		if (housingTypes != null) {
			searchParams.put("housingTypes", housingTypes);
		}
		searchParams.put("householdSizeMin", householdSizeMin);
		if (householdSizeMax != null) {
			searchParams.put("householdSizeMax", householdSizeMax);
		}
		if (totalAnnualIncomeMin != null) {
			searchParams.put("totalAnnualIncomeMin", totalAnnualIncomeMin);
		}
		if (totalAnnualIncomeMax != null) {
			searchParams.put("totalAnnualIncomeMax", totalAnnualIncomeMax);
		}
		List<Household> households = householdRepository.search(searchParams);
		List<Household> qualifyForBabySunshineGrant = new ArrayList<>();

		for (Household household : households) {
			for (FamilyMember familyMember : household.getFamilyMembers()) {
				if (getAge(familyMember.getDateOfBirth()) < 5) {
					qualifyForBabySunshineGrant.add(household);
					break;
				}
			}
		}

		return qualifyForBabySunshineGrant;
	}

	@GetMapping("/households/getQualifyForYoloGstGrant")
	public List<Household> getQualifyForYoloGstGrant(
			@RequestParam(required = false, defaultValue = "1") Integer householdSizeMin,
			@RequestParam(required = false) Integer householdSizeMax,
			@RequestParam(required = false) Double totalAnnualIncomeMin) {
		Map<String, Object> searchParams = new HashMap<>();
		List<String> housingTypes = new ArrayList<>();
		housingTypes.add("HDB");
		searchParams.put("housingTypes", housingTypes);
		searchParams.put("householdSizeMin", householdSizeMin);
		if (householdSizeMax != null) {
			searchParams.put("householdSizeMax", householdSizeMax);
		}
		if (totalAnnualIncomeMin != null) {
			searchParams.put("totalAnnualIncomeMin", totalAnnualIncomeMin);
		}
		searchParams.put("totalAnnualIncomeMax", 100000.0);
		List<Household> households = householdRepository.search(searchParams);

		return households;
	}

	private int getAge(Date dateOfBirth) {
		Calendar c = Calendar.getInstance();
		c.setTime(dateOfBirth);
		LocalDate l = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));
		LocalDate now = LocalDate.now();
		Period diff = Period.between(l, now);
		return diff.getYears();
	}

}
