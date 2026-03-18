package com.aibe.team2.domain.admin.repository;

import com.aibe.team2.domain.admin.entity.OperationMetricHourly;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OperationMetricHourlyRepository extends JpaRepository<OperationMetricHourly, Long> {

    Optional<OperationMetricHourly> findByMetricDateAndMetricHourAndServiceType(
            LocalDate metricDate,
            Integer metricHour,
            ServiceType serviceType
    );

    List<OperationMetricHourly> findAllByMetricDateOrderByMetricHourAsc(LocalDate metricDate);

    List<OperationMetricHourly> findAllByMetricDateAndServiceTypeOrderByMetricHourAsc(
            LocalDate metricDate,
            ServiceType serviceType
    );
}