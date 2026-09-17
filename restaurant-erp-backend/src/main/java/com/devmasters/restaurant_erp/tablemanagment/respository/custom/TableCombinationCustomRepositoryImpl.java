package com.devmasters.restaurant_erp.tablemanagment.respository.custom;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableCombinationSearchCriteria;
import com.devmasters.restaurant_erp.tablemanagment.respository.custom.TableCombinationCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TableCombinationCustomRepositoryImpl
        implements TableCombinationCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<TableCombination> search(
            TableCombinationSearchCriteria criteria,
            Pageable pageable) {

        Query query = new Query();
        List<Criteria> filters = new ArrayList<>();

        if (criteria.getKeyword() != null &&
                !criteria.getKeyword().isBlank()) {

            String keyword =
                    criteria.getKeyword().trim();

            filters.add(new Criteria().orOperator(
                    Criteria.where("combinationNumber")
                            .regex(keyword, "i"),
                    Criteria.where("name")
                            .regex(keyword, "i"),
                    Criteria.where("description")
                            .regex(keyword, "i")
            ));
        }

        if (criteria.getOrganizationId() != null) {
            filters.add(
                    Criteria.where("organization.$id")
                            .is(criteria.getOrganizationId())
            );
        }

        if (criteria.getBranchId() != null) {
            filters.add(
                    Criteria.where("branch.$id")
                            .is(criteria.getBranchId())
            );
        }

        if (criteria.getTableId() != null) {
            filters.add(
                    Criteria.where("tables.$id")
                            .is(criteria.getTableId())
            );
        }

        if (criteria.getIsActive() != null) {
            filters.add(
                    Criteria.where("isActive")
                            .is(criteria.getIsActive())
            );
        }

        if (!filters.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(filters)
            );
        }

        long total =
                mongoTemplate.count(
                        query,
                        TableCombination.class
                );

        query.with(pageable);

        return new PageImpl<>(
                mongoTemplate.find(
                        query,
                        TableCombination.class
                ),
                pageable,
                total
        );
    }
}