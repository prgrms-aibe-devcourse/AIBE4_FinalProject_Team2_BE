package com.aibe.team2.domain.admin.repository;

import com.aibe.team2.domain.admin.entity.OperationMetricDaily;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OperationMetricDailyRepository extends JpaRepository<OperationMetricDaily, Long> {

    Optional<OperationMetricDaily> findByMetricDateAndServiceType(LocalDate metricDate, ServiceType serviceType);

    List<OperationMetricDaily> findAllByMetricDate(LocalDate metricDate);
}