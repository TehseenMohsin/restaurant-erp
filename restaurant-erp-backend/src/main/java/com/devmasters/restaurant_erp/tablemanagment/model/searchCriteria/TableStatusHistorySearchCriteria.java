package com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria;


import com.devmasters.restaurant_erp.common.enums.TableStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableStatusHistorySearchCriteria {

    private String keyword;
    private UUID tableId;
    private UUID changedById;
    private UUID organizationId;
    private UUID branchId;
    private TableStatus oldStatus;
    private TableStatus newStatus;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Boolean isActive;
}
