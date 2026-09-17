package com.devmasters.restaurant_erp.tablemanagment.model;

import com.devmasters.restaurant_erp.branch.model.BranchModel;
import com.devmasters.restaurant_erp.organization.model.OrganizationModel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableCombinationModel {

    private UUID id;
    private String combinationNumber;
    private String name;
    private String description;
    private List<RestaurantTableModel> tableModels;
    private Integer capacity;
    private Boolean isActive;
    private OrganizationModel organizationModel;
    private BranchModel branchModel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
