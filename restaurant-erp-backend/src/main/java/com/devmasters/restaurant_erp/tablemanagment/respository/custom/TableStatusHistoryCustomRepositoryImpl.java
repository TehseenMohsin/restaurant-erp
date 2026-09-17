package com.devmasters.restaurant_erp.tablemanagment.respository.custom;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableStatusHistorySearchCriteria;
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
public class TableStatusHistoryCustomRepositoryImpl implements TableStatusHistoryCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<TableStatusHistory> search(TableStatusHistorySearchCriteria criteria, Pageable pageable) {

        Query query = new Query();
        List<Criteria> filters = new ArrayList<>();

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {

            String keyword = criteria.getKeyword().trim();

            filters.add(new Criteria().orOperator(Criteria.where("reason").regex(keyword, "i")));
        }

        if (criteria.getTableId() != null) {
            filters.add(Criteria.where("table.$id").is(criteria.getTableId()));
        }

        if (criteria.getChangedById() != null) {
            filters.add(Criteria.where("changedBy.$id").is(criteria.getChangedById()));
        }

        if (criteria.getOrganizationId() != null) {
            filters.add(Criteria.where("organization.$id").is(criteria.getOrganizationId()));
        }

        if (criteria.getBranchId() != null) {
            filters.add(Criteria.where("branch.$id").is(criteria.getBranchId()));
        }

        if (criteria.getOldStatus() != null) {
            filters.add(Criteria.where("oldStatus").is(criteria.getOldStatus()));
        }

        if (criteria.getNewStatus() != null) {
            filters.add(Criteria.where("newStatus").is(criteria.getNewStatus()));
        }

        if (criteria.getFromDate() != null) {
            filters.add(Criteria.where("changedAt").gte(criteria.getFromDate()));
        }

        if (criteria.getToDate() != null) {
            filters.add(Criteria.where("changedAt").lte(criteria.getToDate()));
        }

        if (criteria.getIsActive() != null) {
            filters.add(Criteria.where("isActive").is(criteria.getIsActive()));
        }

        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters));
        }

        long total = mongoTemplate.count(query, TableStatusHistory.class);

        query.with(pageable);

        return new PageImpl<>(mongoTemplate.find(query, TableStatusHistory.class), pageable, total);
    }
}
