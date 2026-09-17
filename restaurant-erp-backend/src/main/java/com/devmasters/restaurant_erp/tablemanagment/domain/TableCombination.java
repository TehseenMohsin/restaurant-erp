package com.devmasters.restaurant_erp.tablemanagment.domain;

import com.devmasters.restaurant_erp.branch.domain.Branch;
import com.devmasters.restaurant_erp.common.domain.BaseEntity;
import com.devmasters.restaurant_erp.organization.domain.Organization;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("table_combinations")
public class TableCombination extends BaseEntity {

    private String combinationNumber;
    private String name;
    private String description;

    @DBRef
    private List<RestaurantTable> tables;

    private Integer capacity;
    private Boolean isActive;

    @DBRef
    private Organization organization;

    @DBRef
    private Branch branch;
}