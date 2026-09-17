package com.devmasters.restaurant_erp.tablemanagment.respository.custom;


import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableStatusHistorySearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TableStatusHistoryCustomRepository {

    Page<TableStatusHistory> search(
            TableStatusHistorySearchCriteria criteria,
            Pageable pageable
    );
}
