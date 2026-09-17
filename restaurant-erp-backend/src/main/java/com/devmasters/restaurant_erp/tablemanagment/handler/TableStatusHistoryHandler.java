package com.devmasters.restaurant_erp.tablemanagment.handler;

import com.devmasters.restaurant_erp.common.model.pagination.PageResponse;
import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.model.TableStatusHistoryModel;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableStatusHistorySearchCriteria;
import com.devmasters.restaurant_erp.tablemanagment.service.TableStatusHistoryService;
import com.devmasters.restaurant_erp.tablemanagment.transformer.TableStatusHistoryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TableStatusHistoryHandler {

    private final TableStatusHistoryService historyService;
    private final TableStatusHistoryTransformer historyTransformer;

    public TableStatusHistoryModel create(TableStatusHistoryModel model) {

        if (model == null) {
            throw new RuntimeException(
                    "Table status history is required."
            );
        }

        TableStatusHistory entity =
                historyTransformer.toEntity(model);

        entity.setIsActive(true);

        return historyTransformer.toModel(
                historyService.create(entity)
        );
    }

    public TableStatusHistoryModel getById(
            UUID id) {

        return historyTransformer.toModel(
                historyService.findById(id)
        );
    }

    public PageResponse<TableStatusHistoryModel> search(TableStatusHistorySearchCriteria criteria, Pageable pageable) {

        Page<TableStatusHistory> page =
                historyService.search(
                        criteria,
                        pageable
                );

        return PageResponse
                .<TableStatusHistoryModel>builder()
                .content(
                        historyTransformer.toModels(
                                page.getContent()
                        )
                )
                .totalElements(
                        page.getTotalElements()
                )
                .totalPages(
                        page.getTotalPages()
                )
                .page(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public TableStatusHistoryModel delete(
            UUID id) {

        return historyTransformer.toModel(
                historyService.delete(id)
        );
    }

    public TableStatusHistoryModel restore(
            UUID id) {

        return historyTransformer.toModel(
                historyService.restore(id)
        );
    }
}
