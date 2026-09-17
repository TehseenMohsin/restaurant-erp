package com.devmasters.restaurant_erp.tablemanagment.service.impl;


import com.devmasters.restaurant_erp.tablemanagment.domain.RestaurantTable;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.RestaurantTableSearchCriteria;
import com.devmasters.restaurant_erp.tablemanagment.respository.RestaurantTableRepository;
import com.devmasters.restaurant_erp.tablemanagment.service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantTableServiceImpl
        implements RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    @Override
    public boolean existsByTableNumberIgnoreCaseAndBranch_Id(String tableNumber, UUID branchId) {
        return restaurantTableRepository
                .existsByTableNumberIgnoreCaseAndBranch_Id(
                        tableNumber,
                        branchId);
    }

    @Override
    public boolean existsByTableNameIgnoreCaseAndBranch_Id(String tableName, UUID branchId) {
        return restaurantTableRepository
                .existsByTableNameIgnoreCaseAndBranch_Id(
                        tableName,
                        branchId);
    }

    @Override
    public RestaurantTable create(RestaurantTable entity) {
        return restaurantTableRepository.save(entity);
    }

    @Override
    public Page<RestaurantTable> search(RestaurantTableSearchCriteria criteria, Pageable pageable) {
        return restaurantTableRepository.search(criteria, pageable);
    }

    @Override
    public RestaurantTable findById(UUID id) {
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found."));
    }

    @Override
    public RestaurantTable update(UUID id, RestaurantTable entity) {
        RestaurantTable existing = findById(id);

        existing.setTableNumber(entity.getTableNumber());
        existing.setTableName(entity.getTableName());
        existing.setCapacity(entity.getCapacity());
        existing.setOrganization(entity.getOrganization());
        existing.setBranch(entity.getBranch());
        existing.setFloor(entity.getFloor());
        existing.setStatus(entity.getStatus());
        existing.setQrCode(entity.getQrCode());
        existing.setReservable(entity.getReservable());
        existing.setMergedable(entity.getMergedable());
        existing.setNotes(entity.getNotes());
        existing.setIsActive(entity.getIsActive());

        return restaurantTableRepository.save(existing);
    }

    @Override
    public RestaurantTable delete(UUID id) {
        RestaurantTable table = findById(id);
        if (!Boolean.TRUE.equals(table.getIsActive())) {
            throw new RuntimeException("Table already deleted.");
        }
        table.setIsActive(false);
        return restaurantTableRepository.save(table);
    }

    @Override
    public RestaurantTable restore(UUID id) {
        RestaurantTable table = findById(id);
        table.setIsActive(true);
        return restaurantTableRepository.save(table);
    }

    @Override
    public boolean existsByTableNumberIgnoreCaseAndBranch_IdAndIdNot(String tableNumber, UUID branchId, UUID id) {

        return restaurantTableRepository
                .existsByTableNumberIgnoreCaseAndBranch_IdAndIdNot(
                        tableNumber,
                        branchId,
                        id);
    }

    @Override
    public boolean existsByTableNameIgnoreCaseAndBranch_IdAndIdNot(String tableName, UUID branchId, UUID id) {

        return restaurantTableRepository
                .existsByTableNameIgnoreCaseAndBranch_IdAndIdNot(
                        tableName,
                        branchId,
                        id);
    }
}
