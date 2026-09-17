package com.devmasters.restaurant_erp.tablemanagment.respository.custom;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableCombinationSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TableCombinationCustomRepository {

    Page<TableCombination> search(
            TableCombinationSearchCriteria criteria,
            Pageable pageable
    );
}