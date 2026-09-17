package com.devmasters.restaurant_erp.tablemanagment.domain;


import com.devmasters.restaurant_erp.branch.domain.Branch;
import com.devmasters.restaurant_erp.common.domain.BaseEntity;
import com.devmasters.restaurant_erp.common.enums.TableStatus;
import com.devmasters.restaurant_erp.employee.domain.Employee;
import com.devmasters.restaurant_erp.organization.domain.Organization;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("table_status_history")
public class TableStatusHistory extends BaseEntity {

    private TableStatus oldStatus;
    private TableStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;

    @DBRef
    private RestaurantTable table;

    @DBRef
    private Employee changedBy;

    @DBRef
    private Organization organization;

    @DBRef
    private Branch branch;
}