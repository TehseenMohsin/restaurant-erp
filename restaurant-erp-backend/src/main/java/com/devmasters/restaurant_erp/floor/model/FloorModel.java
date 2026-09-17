package com.devmasters.restaurant_erp.floor.model;

import com.devmasters.restaurant_erp.branch.model.BranchModel;
import com.devmasters.restaurant_erp.common.enums.FloorType;
import com.devmasters.restaurant_erp.organization.model.OrganizationModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorModel {

    private UUID id;

    @NotBlank(message = "Floor name is required")
    @Size(min = 2, max = 100, message = "Floor name must be between 2 and 100 characters")
    private String floorName;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private FloorType floorType;

    @Valid
    @NotNull(message = "Organization is required")
    private OrganizationModel organizationModel;

    @Valid
    @NotNull(message = "Branch is required")
    private BranchModel branchModel;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
