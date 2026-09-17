package com.devmasters.restaurant_erp.tablemanagment.controller;

import com.devmasters.restaurant_erp.common.model.ApiResponse;
import com.devmasters.restaurant_erp.common.model.pagination.PageResponse;
import com.devmasters.restaurant_erp.tablemanagment.handler.TableStatusHistoryHandler;
import com.devmasters.restaurant_erp.tablemanagment.model.TableStatusHistoryModel;
import com.devmasters.restaurant_erp.tablemanagment.model.searchCriteria.TableStatusHistorySearchCriteria;
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
@RequestMapping("/api/table-status-history")
@RequiredArgsConstructor
public class TableStatusHistoryController {

    private final TableStatusHistoryHandler historyHandler;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @PostMapping
    public ResponseEntity<ApiResponse<TableStatusHistoryModel>> create(@RequestBody TableStatusHistoryModel model) {

        TableStatusHistoryModel result = historyHandler.create(model);

        sendTableStatusEvent(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<TableStatusHistoryModel>builder().success(true).message("Table status history created successfully").data(result).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableStatusHistoryModel>> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.<TableStatusHistoryModel>builder().success(true).message("Table status history fetched successfully").data(historyHandler.getById(id)).build());
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TableStatusHistoryModel>>> search(@RequestBody TableStatusHistorySearchCriteria criteria, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "changedAt") String sortBy, @RequestParam(defaultValue = "DESC") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));

        return ResponseEntity.ok(ApiResponse.<PageResponse<TableStatusHistoryModel>>builder().success(true).message("Table status history fetched successfully").data(historyHandler.search(criteria, pageable)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TableStatusHistoryModel>> delete(@PathVariable UUID id) {

        TableStatusHistoryModel result = historyHandler.delete(id);

        sendTableStatusEvent(result);

        return ResponseEntity.ok(ApiResponse.<TableStatusHistoryModel>builder().success(true).message("Table status history deleted successfully").data(result).build());
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TableStatusHistoryModel>> restore(@PathVariable UUID id) {

        TableStatusHistoryModel result = historyHandler.restore(id);

        sendTableStatusEvent(result);

        return ResponseEntity.ok(ApiResponse.<TableStatusHistoryModel>builder().success(true).message("Table status history restored successfully").data(result).build());
    }

    @GetMapping("/stream")
    public SseEmitter stream() {

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));

        emitter.onTimeout(() -> emitters.remove(emitter));

        emitter.onError(ex -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("Table status stream connected"));
        } catch (Exception e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    private void sendTableStatusEvent(TableStatusHistoryModel history) {

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("TABLE_STATUS_CHANGED").data(history));
            } catch (Exception e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}