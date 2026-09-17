package com.devmasters.restaurant_erp.tablemanagment.service;


import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableStatusHistorySearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TableStatusHistoryService {

    TableStatusHistory create(TableStatusHistory history);

    TableStatusHistory findById(UUID id);

    Page<TableStatusHistory> search(
            TableStatusHistorySearchCriteria criteria,
            Pageable pageable
    );

    List<TableStatusHistory> findByTable(
            UUID tableId,
            UUID organizationId
    );

    TableStatusHistory delete(UUID id);

    TableStatusHistory restore(UUID id);
}
