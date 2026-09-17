package com.devmasters.restaurant_erp.tablemanagment.handler;

import com.devmasters.restaurant_erp.common.model.pagination.PageResponse;
import com.devmasters.restaurant_erp.common.service.Sequence.CodeGeneratorService;
import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.model.TableCombinationModel;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableCombinationSearchCriteria;
import com.devmasters.restaurant_erp.tablemanagment.service.TableCombinationService;
import com.devmasters.restaurant_erp.tablemanagment.transformer.TableCombinationTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TableCombinationHandler {

    private final TableCombinationService combinationService;
    private final TableCombinationTransformer combinationTransformer;
    private final CodeGeneratorService codeGeneratorService;

    public TableCombinationModel create(
            TableCombinationModel model) {

        if (model == null) {
            throw new RuntimeException(
                    "Table combination data is required."
            );
        }

        String combinationNumber =
                codeGeneratorService.generateCombinationNumber();

        model.setCombinationNumber(combinationNumber);

        TableCombination entity =
                combinationTransformer.toEntity(model);

        entity.setIsActive(true);

        return combinationTransformer.toModel(
                combinationService.create(entity)
        );
    }

    public TableCombinationModel getById(UUID id) {

        return combinationTransformer.toModel(
                combinationService.findById(id)
        );
    }

    public PageResponse<TableCombinationModel> search(
            TableCombinationSearchCriteria criteria,
            Pageable pageable) {

        Page<TableCombination> page =
                combinationService.search(
                        criteria,
                        pageable
                );

        return PageResponse.<TableCombinationModel>builder()
                .content(
                        combinationTransformer.toModels(
                                page.getContent()
                        )
                )
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public TableCombinationModel update(
            UUID id,
            TableCombinationModel model) {

        if (model == null) {
            throw new RuntimeException(
                    "Table combination data is required."
            );
        }

        TableCombination entity =
                combinationTransformer.toEntity(model);

        return combinationTransformer.toModel(
                combinationService.update(id, entity)
        );
    }

    public TableCombinationModel delete(UUID id) {

        return combinationTransformer.toModel(
                combinationService.delete(id)
        );
    }

    public TableCombinationModel restore(UUID id) {

        return combinationTransformer.toModel(
                combinationService.restore(id)
        );
    }
}