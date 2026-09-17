package com.devmasters.restaurant_erp.tablemanagment.respository;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.respository.custom.TableCombinationCustomRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TableCombinationRepository
        extends MongoRepository<TableCombination, UUID>,
        TableCombinationCustomRepository {

    List<TableCombination> findByOrganization_IdAndBranch_Id(
            UUID organizationId,
            UUID branchId
    );

    boolean existsByCombinationNumberIgnoreCaseAndOrganization_Id(
            String combinationNumber,
            UUID organizationId
    );
}