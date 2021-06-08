package com.mom.repository;

import java.util.List;
import java.util.Map;

import com.mom.model.Household;

public interface HouseholdRepositoryCustom {

	List<Household> search(Map<String, Object> searchParams);

}
