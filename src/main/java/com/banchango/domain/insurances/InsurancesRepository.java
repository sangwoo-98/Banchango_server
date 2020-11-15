package com.banchango.domain.insurances;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InsurancesRepository extends JpaRepository<Insurances, Integer> {
    Insurances findByInsuranceId(Integer insuranceId);
    void deleteByInsuranceId(Integer insuranceId);
}
