package com.devmasters.restaurant_erp.tablemanagment.controller;

import com.devmasters.restaurant_erp.common.model.ApiResponse;
import com.devmasters.restaurant_erp.common.model.pagination.PageResponse;
import com.devmasters.restaurant_erp.tablemanagment.handler.TableCombinationHandler;
import com.devmasters.restaurant_erp.tablemanagment.model.TableCombinationModel;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableCombinationSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/table-combination")
@RequiredArgsConstructor
public class TableCombinationController {

    private final TableCombinationHandler combinationHandler;

    private final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();

    @PostMapping
    public ResponseEntity<ApiResponse<TableCombinationModel>> create(
            @RequestBody TableCombinationModel model) {

        TableCombinationModel result =
                combinationHandler.create(model);

        sendTableCombinationEvent(
                "TABLE_COMBINATION_CREATED",
                result
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<TableCombinationModel>builder()
                                .success(true)
                                .message("Table combination created successfully")
                                .data(result)
                                .build()
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableCombinationModel>> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.<TableCombinationModel>builder()
                        .success(true)
                        .message("Table combination fetched successfully")
                        .data(combinationHandler.getById(id))
                        .build()
        );
    }

    @PostMapping("/search")
    public ResponseEntity<
            ApiResponse<PageResponse<TableCombinationModel>>> search(
            @RequestBody TableCombinationSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.fromString(direction),
                        sortBy
                )
        );

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<TableCombinationModel>>builder()
                        .success(true)
                        .message("Table combinations fetched successfully")
                        .data(
                                combinationHandler.search(
                                        criteria,
                                        pageable
                                )
                        )
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TableCombinationModel>> update(
            @PathVariable UUID id,
            @RequestBody TableCombinationModel model) {

        TableCombinationModel result =
                combinationHandler.update(id, model);

        sendTableCombinationEvent(
                "TABLE_COMBINATION_UPDATED",
                result
        );

        return ResponseEntity.ok(
                ApiResponse.<TableCombinationModel>builder()
                        .success(true)
                        .message("Table combination updated successfully")
                        .data(result)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TableCombinationModel>> delete(
            @PathVariable UUID id) {

        TableCombinationModel result =
                combinationHandler.delete(id);

        sendTableCombinationEvent(
                "TABLE_COMBINATION_DELETED",
                result
        );

        return ResponseEntity.ok(
                ApiResponse.<TableCombinationModel>builder()
                        .success(true)
                        .message("Table combination deleted successfully")
                        .data(result)
                        .build()
        );
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TableCombinationModel>> restore(
            @PathVariable UUID id) {

        TableCombinationModel result =
                combinationHandler.restore(id);

        sendTableCombinationEvent(
                "TABLE_COMBINATION_RESTORED",
                result
        );

        return ResponseEntity.ok(
                ApiResponse.<TableCombinationModel>builder()
                        .success(true)
                        .message("Table combination restored successfully")
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/stream")
    public SseEmitter stream() {

        SseEmitter emitter =
                new SseEmitter(30 * 60 * 1000L);

        emitters.add(emitter);

        emitter.onCompletion(
                () -> emitters.remove(emitter)
        );

        emitter.onTimeout(
                () -> emitters.remove(emitter)
        );

        emitter.onError(
                ex -> emitters.remove(emitter)
        );

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("CONNECTED")
                            .data("Table combination stream connected")
            );
        } catch (Exception e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    private void sendTableCombinationEvent(
            String eventName,
            TableCombinationModel model) {

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(model)
                );
            } catch (Exception e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}