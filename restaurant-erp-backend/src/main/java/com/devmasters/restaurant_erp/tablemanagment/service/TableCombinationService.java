package com.devmasters.restaurant_erp.tablemanagment.service;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableCombinationSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TableCombinationService {

    TableCombination create(TableCombination combination);

    TableCombination findById(UUID id);

    Page<TableCombination> search(
            TableCombinationSearchCriteria criteria,
            Pageable pageable
    );

    List<TableCombination> findByBranch(
            UUID organizationId,
            UUID branchId
    );

    TableCombination update(
            UUID id,
            TableCombination combination
    );

    TableCombination delete(UUID id);

    TableCombination restore(UUID id);
}