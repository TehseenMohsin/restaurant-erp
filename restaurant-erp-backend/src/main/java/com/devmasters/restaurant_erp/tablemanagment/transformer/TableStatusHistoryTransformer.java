package com.devmasters.restaurant_erp.tablemanagment.transformer;

import com.devmasters.restaurant_erp.branch.transformer.BranchTransformer;
import com.devmasters.restaurant_erp.common.transformer.Transformer;
import com.devmasters.restaurant_erp.employee.transformer.EmployeeTransformer;
import com.devmasters.restaurant_erp.organization.transformer.OrganizationTransformer;
import com.devmasters.restaurant_erp.tablemanagment.domain.TableStatusHistory;
import com.devmasters.restaurant_erp.tablemanagment.model.TableStatusHistoryModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TableStatusHistoryTransformer extends Transformer<TableStatusHistory, TableStatusHistoryModel> {

    private final RestaurantTableTransformer tableTransformer;
    private final EmployeeTransformer employeeTransformer;
    private final OrganizationTransformer organizationTransformer;
    private final BranchTransformer branchTransformer;

    public TableStatusHistory toEntity(TableStatusHistoryModel model) {

        if (model == null) {
            return null;
        }

        return TableStatusHistory.builder()
                .id(model.getId())
                .oldStatus(model.getOldStatus())
                .newStatus(model.getNewStatus())
                .reason(model.getReason())
                .changedAt(model.getChangedAt())
                .table(tableTransformer.toEntity(model.getTableModel()))
                .changedBy(employeeTransformer.toEntity(model.getChangedByModel()))
                .organization(organizationTransformer.toEntity(model.getOrganizationModel()))
                .branch(branchTransformer.toEntity(model.getBranchModel()))
                .isActive(model.getIsActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    public TableStatusHistoryModel toModel(
            TableStatusHistory entity) {

        if (entity == null) {
            return null;
        }

        return TableStatusHistoryModel.builder()
                .id(entity.getId())
                .oldStatus(entity.getOldStatus())
                .newStatus(entity.getNewStatus())
                .reason(entity.getReason())
                .changedAt(entity.getChangedAt())
                .tableModel(tableTransformer.toModel(entity.getTable()))
                .changedByModel(employeeTransformer.toModel(entity.getChangedBy()))
                .organizationModel(organizationTransformer.toModel(entity.getOrganization()))
                .branchModel(branchTransformer.toModel(entity.getBranch()))
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<TableStatusHistoryModel> toModels(
            List<TableStatusHistory> entities) {

        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .toList();
    }
}
