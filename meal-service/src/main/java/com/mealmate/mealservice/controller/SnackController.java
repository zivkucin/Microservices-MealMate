package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.dto.CreateSnackRequest;
import com.mealmate.mealservice.dto.SnackResponse;
import com.mealmate.mealservice.dto.UpdateSnackRequest;
import com.mealmate.mealservice.service.SnackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/snacks")
public class SnackController {

    private final SnackService snackService;

    public SnackController(SnackService snackService) {
        this.snackService = snackService;
    }

    @Operation(
            summary = "Create a snack",
            description = "Creates a new snack in the snack catalog."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SnackResponse createSnack(
            @Valid @RequestBody CreateSnackRequest request
    ) {
        return snackService.createSnack(request);
    }

    @Operation(
            summary = "Get all snacks",
            description = "Returns all snacks from the snack catalog."
    )
    @GetMapping
    public List<SnackResponse> getAllSnacks() {
        return snackService.getAllSnacks();
    }

    @Operation(
            summary = "Get snack by ID",
            description = "Returns a snack with the specified ID."
    )
    @GetMapping("/{snackId}")
    public SnackResponse getSnackById(
            @PathVariable Long snackId
    ) {
        return snackService.getSnackById(snackId);
    }

    @Operation(
            summary = "Update a snack",
            description = "Updates the name and default measurement unit of a snack."
    )
    @PutMapping("/{snackId}")
    public SnackResponse updateSnack(
            @PathVariable Long snackId,
            @Valid @RequestBody UpdateSnackRequest request
    ) {
        return snackService.updateSnack(
                snackId,
                request
        );
    }

    @Operation(
            summary = "Delete a snack",
            description = "Deletes a snack from the snack catalog."
    )
    @DeleteMapping("/{snackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSnack(
            @PathVariable Long snackId
    ) {
        snackService.deleteSnack(snackId);
    }
}