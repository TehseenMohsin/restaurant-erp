package com.devmasters.restaurant_erp.tablemanagment.respository;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.respository.custom.TableStatusHistoryCustomRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TableStatusHistoryRepository extends MongoRepository<TableStatusHistory, UUID>, TableStatusHistoryCustomRepository {

    List<TableStatusHistory> findByTable_IdAndOrganization_IdOrderByChangedAtDesc(UUID tableId, UUID organizationId);

    List<TableStatusHistory> findByTable_IdAndOrganization_Id(UUID tableId, UUID organizationId);
}
