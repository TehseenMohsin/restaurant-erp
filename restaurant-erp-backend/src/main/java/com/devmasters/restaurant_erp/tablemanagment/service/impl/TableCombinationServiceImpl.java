package com.devmasters.restaurant_erp.tablemanagment.service.impl;

import com.devmasters.restaurant_erp.tablemanagment.domain.RestaurantTable;
import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableCombinationSearchCriteria;
import com.devmasters.restaurant_erp.tablemanagment.respository.TableCombinationRepository;
import com.devmasters.restaurant_erp.tablemanagment.service.TableCombinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableCombinationServiceImpl
        implements TableCombinationService {

    private final TableCombinationRepository repository;

    @Override
    public TableCombination create(
            TableCombination combination) {

        validateCombination(combination);

        if (combination.getTables() == null ||
                combination.getTables().isEmpty()) {

            throw new RuntimeException(
                    "At least two tables are required."
            );
        }

        if (combination.getTables().size() < 2) {
            throw new RuntimeException(
                    "A table combination must contain at least two tables."
            );
        }

        validateTables(combination.getTables());

        combination.setIsActive(true);

        return repository.save(combination);
    }

    @Override
    public TableCombination findById(UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Table combination not found."
                        ));
    }

    @Override
    public Page<TableCombination> search(
            TableCombinationSearchCriteria criteria,
            Pageable pageable) {

        return repository.search(criteria, pageable);
    }

    @Override
    public List<TableCombination> findByBranch(
            UUID organizationId,
            UUID branchId) {

        return repository.findByOrganization_IdAndBranch_Id(
                organizationId,
                branchId
        );
    }

    @Override
    public TableCombination update(
            UUID id,
            TableCombination combination) {

        TableCombination existing = findById(id);

        validateCombination(combination);

        if (combination.getTables() == null ||
                combination.getTables().size() < 2) {

            throw new RuntimeException(
                    "A table combination must contain at least two tables."
            );
        }

        validateTables(combination.getTables());

        existing.setName(combination.getName());
        existing.setDescription(combination.getDescription());
        existing.setTables(combination.getTables());
        existing.setCapacity(combination.getCapacity());
        existing.setUpdatedAt(LocalDateTime.now());

        return repository.save(existing);
    }

    @Override
    public TableCombination delete(UUID id) {

        TableCombination combination = findById(id);

        combination.setIsActive(false);
        combination.setUpdatedAt(LocalDateTime.now());

        return repository.save(combination);
    }

    @Override
    public TableCombination restore(UUID id) {

        TableCombination combination = findById(id);

        combination.setIsActive(true);
        combination.setUpdatedAt(LocalDateTime.now());

        return repository.save(combination);
    }

    private void validateCombination(
            TableCombination combination) {

        if (combination == null) {
            throw new RuntimeException(
                    "Table combination is required."
            );
        }

        if (combination.getOrganization() == null ||
                combination.getOrganization().getId() == null) {

            throw new RuntimeException(
                    "Organization is required."
            );
        }

        if (combination.getBranch() == null ||
                combination.getBranch().getId() == null) {

            throw new RuntimeException(
                    "Branch is required."
            );
        }

        if (combination.getName() == null ||
                combination.getName().isBlank()) {

            throw new RuntimeException(
                    "Combination name is required."
            );
        }
    }

    private void validateTables(
            List<RestaurantTable> tables) {

        boolean duplicate =
                tables.stream()
                        .map(RestaurantTable::getId)
                        .distinct()
                        .count()
                        != tables.size();

        if (duplicate) {
            throw new RuntimeException(
                    "Duplicate tables are not allowed."
            );
        }
    }
}