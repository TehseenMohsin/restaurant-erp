package com.devmasters.restaurant_erp.tablemanagment.model;

import com.devmasters.restaurant_erp.branch.model.BranchModel;
import com.devmasters.restaurant_erp.common.enums.TableStatus;
import com.devmasters.restaurant_erp.employee.model.EmployeeModel;
import com.devmasters.restaurant_erp.organization.model.OrganizationModel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableStatusHistoryModel {

    private UUID id;
    private TableStatus oldStatus;
    private TableStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;

    private RestaurantTableModel tableModel;
    private EmployeeModel changedByModel;
    private OrganizationModel organizationModel;
    private BranchModel branchModel;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}