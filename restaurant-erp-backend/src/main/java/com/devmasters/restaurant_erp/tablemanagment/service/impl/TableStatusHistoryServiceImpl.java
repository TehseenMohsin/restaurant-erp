package com.devmasters.restaurant_erp.tablemanagment.service.impl;

import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableStatusHistorySearchCriteria;
import com.devmasters.restaurant_erp.tablemanagment.respository.TableStatusHistoryRepository;
import com.devmasters.restaurant_erp.tablemanagment.service.TableStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableStatusHistoryServiceImpl implements TableStatusHistoryService {

    private final TableStatusHistoryRepository repository;

    @Override
    public TableStatusHistory create(TableStatusHistory history) {

        if (history == null) {
            throw new RuntimeException(
                    "Table status history is required."
            );
        }

        if (history.getTable() == null ||
                history.getTable().getId() == null) {
            throw new RuntimeException(
                    "Table is required."
            );
        }

        if (history.getOrganization() == null ||
                history.getOrganization().getId() == null) {
            throw new RuntimeException(
                    "Organization is required."
            );
        }

        if (history.getOldStatus() == null) {
            throw new RuntimeException(
                    "Old status is required."
            );
        }

        if (history.getNewStatus() == null) {
            throw new RuntimeException(
                    "New status is required."
            );
        }

        if (history.getOldStatus()
                .equals(history.getNewStatus())) {
            throw new RuntimeException(
                    "Old and new status cannot be the same."
            );
        }

        if (history.getChangedAt() == null) {
            history.setChangedAt(
                    LocalDateTime.now()
            );
        }

        history.setIsActive(true);

        return repository.save(history);
    }

    @Override
    public TableStatusHistory findById(UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Table status history not found."
                        ));
    }

    @Override
    public Page<TableStatusHistory> search(
            TableStatusHistorySearchCriteria criteria,
            Pageable pageable) {

        return repository.search(
                criteria,
                pageable
        );
    }

    @Override
    public List<TableStatusHistory> findByTable(
            UUID tableId,
            UUID organizationId) {

        return repository
                .findByTable_IdAndOrganization_IdOrderByChangedAtDesc(
                        tableId,
                        organizationId
                );
    }

    @Override
    public TableStatusHistory delete(UUID id) {

        TableStatusHistory history = findById(id);

        history.setIsActive(false);
        history.setUpdatedAt(
                LocalDateTime.now()
        );

        return repository.save(history);
    }

    @Override
    public TableStatusHistory restore(UUID id) {

        TableStatusHistory history = findById(id);

        if (Boolean.TRUE.equals(
                history.getIsActive())) {

            throw new RuntimeException(
                    "History is already active."
            );
        }

        history.setIsActive(true);
        history.setUpdatedAt(
                LocalDateTime.now()
        );

        return repository.save(history);
    }
}
