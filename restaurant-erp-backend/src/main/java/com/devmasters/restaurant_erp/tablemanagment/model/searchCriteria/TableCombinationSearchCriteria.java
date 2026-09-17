package com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableCombinationSearchCriteria {

    private String keyword;
    private UUID organizationId;
    private UUID branchId;
    private UUID tableId;
    private Boolean isActive;
}
