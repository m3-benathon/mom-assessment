package com.mom.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.mom.model.Household;

@Repository
public class HouseholdRepositoryImpl implements HouseholdRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<Household> search(Map<String, Object> searchParams) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Household> cq = cb.createQuery(Household.class);
		Root<Household> root = cq.from(Household.class);
		
		List<Predicate> predicates = new ArrayList<Predicate>();

		for (Map.Entry<String, Object> searchParam : searchParams.entrySet()) {
			if (searchParam.getKey().equals("housingTypes")) {
				In<String> in = cb.in(root.get("housingType"));
				for (String housingType : (List<String>) searchParams.get("housingTypes")) {
					in.value(housingType);
				}
				predicates.add(in);
			}
		}

		cq.select(root).where(predicates.toArray(new Predicate[]{}));
		return entityManager.createQuery(cq).getResultList();
	}

}
