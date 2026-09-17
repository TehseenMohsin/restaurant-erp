package com.devmasters.restaurant_erp.floor.transformer;

import com.devmasters.restaurant_erp.branch.transformer.BranchTransformer;
import com.devmasters.restaurant_erp.common.transformer.Transformer;
import com.devmasters.restaurant_erp.floor.domain.Floor;
import com.devmasters.restaurant_erp.floor.model.FloorModel;
import com.devmasters.restaurant_erp.organization.transformer.OrganizationTransformer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class FloorTransformer
        extends Transformer<Floor, FloorModel> {

    private final OrganizationTransformer organizationTransformer;
    private final BranchTransformer branchTransformer;

    @Override
    public Floor toEntity(FloorModel model) {

        if (model == null)
            return null;

        return Floor.builder()
                .id(model.getId() != null ? model.getId() : UUID.randomUUID())
                .floorName(model.getFloorName())
                .displayOrder(model.getDisplayOrder())
                .description(model.getDescription())
                .floorType(model.getFloorType())
                .organization(organizationTransformer.toEntity(model.getOrganizationModel()))
                .branch(branchTransformer.toEntity(model.getBranchModel()))
                .isActive(model.getIsActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    @Override
    public FloorModel toModel(Floor entity) {

        if (entity == null)
            return null;

        return FloorModel.builder()
                .id(entity.getId())
                .floorName(entity.getFloorName())
                .displayOrder(entity.getDisplayOrder())
                .description(entity.getDescription())
                .floorType(entity.getFloorType())
                .organizationModel(organizationTransformer.toModel(entity.getOrganization()))
                .branchModel(branchTransformer.toModel(entity.getBranch()))
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
