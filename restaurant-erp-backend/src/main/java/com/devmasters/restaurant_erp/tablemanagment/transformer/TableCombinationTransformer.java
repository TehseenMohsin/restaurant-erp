package com.devmasters.restaurant_erp.tablemanagment.transformer;

import com.devmasters.restaurant_erp.branch.transformer.BranchTransformer;
import com.devmasters.restaurant_erp.common.transformer.Transformer;
import com.devmasters.restaurant_erp.organization.transformer.OrganizationTransformer;
import com.devmasters.restaurant_erp.tablemanagment.domain.RestaurantTable;
import com.devmasters.restaurant_erp.tablemanagment.domain.TableCombination;
import com.devmasters.restaurant_erp.tablemanagment.model.RestaurantTableModel;
import com.devmasters.restaurant_erp.tablemanagment.model.TableCombinationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TableCombinationTransformer extends Transformer<TableCombination,TableCombinationModel> {

    private final RestaurantTableTransformer tableTransformer;
    private final OrganizationTransformer organizationTransformer;
    private final BranchTransformer branchTransformer;

    public TableCombination toEntity(
            TableCombinationModel model) {

        if (model == null) {
            return null;
        }

        return TableCombination.builder()
                .id(model.getId())
                .combinationNumber(model.getCombinationNumber())
                .name(model.getName())
                .description(model.getDescription())
                .tables(
                        model.getTableModels() == null
                                ? null
                                : model.getTableModels()
                                  .stream()
                                  .map(tableTransformer::toEntity)
                                  .toList()
                )
                .capacity(model.getCapacity())
                .isActive(model.getIsActive())
                .organization(
                        organizationTransformer.toEntity(
                                model.getOrganizationModel()
                        )
                )
                .branch(
                        branchTransformer.toEntity(
                                model.getBranchModel()
                        )
                )
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    public TableCombinationModel toModel(
            TableCombination entity) {

        if (entity == null) {
            return null;
        }

        return TableCombinationModel.builder()
                .id(entity.getId())
                .combinationNumber(entity.getCombinationNumber())
                .name(entity.getName())
                .description(entity.getDescription())
                .tableModels(
                        entity.getTables() == null
                                ? null
                                : entity.getTables()
                                  .stream()
                                  .map(tableTransformer::toModel)
                                  .toList()
                )
                .capacity(entity.getCapacity())
                .isActive(entity.getIsActive())
                .organizationModel(
                        organizationTransformer.toModel(
                                entity.getOrganization()
                        )
                )
                .branchModel(
                        branchTransformer.toModel(
                                entity.getBranch()
                        )
                )
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<TableCombinationModel> toModels(
            List<TableCombination> entities) {

        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .toList();
    }
}
