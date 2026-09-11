package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.CreateSnackRequest;
import com.mealmate.mealservice.dto.SnackResponse;
import com.mealmate.mealservice.dto.UpdateSnackRequest;
import com.mealmate.mealservice.entity.Snack;
import com.mealmate.mealservice.exception.SnackAlreadyExistsException;
import com.mealmate.mealservice.exception.SnackNotFoundException;
import com.mealmate.mealservice.mapper.SnackMapper;
import com.mealmate.mealservice.repository.SnackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SnackService {

    private final SnackRepository snackRepository;
    private final SnackMapper snackMapper;

    public SnackService(
            SnackRepository snackRepository,
            SnackMapper snackMapper
    ) {
        this.snackRepository = snackRepository;
        this.snackMapper = snackMapper;
    }

    @Transactional
    public SnackResponse createSnack(CreateSnackRequest request) {

        if (snackRepository.existsByNameIgnoreCase(request.name())) {
            throw new SnackAlreadyExistsException(
                    "Snack with this name already exists."
            );
        }

        Snack snack = new Snack();

        snack.setName(request.name());
        snack.setDefaultUnit(request.defaultUnit());

        Snack savedSnack = snackRepository.save(snack);

        return snackMapper.toResponse(savedSnack);
    }

    @Transactional(readOnly = true)
    public List<SnackResponse> getAllSnacks() {

        return snackRepository.findAll()
                .stream()
                .map(snackMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SnackResponse getSnackById(Long snackId) {

        Snack snack = snackRepository.findById(snackId)
                .orElseThrow(() ->
                        new SnackNotFoundException(
                                "Snack not found."
                        )
                );

        return snackMapper.toResponse(snack);
    }

    @Transactional
    public SnackResponse updateSnack(
            Long snackId,
            UpdateSnackRequest request
    ) {

        Snack snack = snackRepository.findById(snackId)
                .orElseThrow(() ->
                        new SnackNotFoundException(
                                "Snack not found."
                        )
                );

        if (!snack.getName().equalsIgnoreCase(request.name())
                && snackRepository.existsByNameIgnoreCase(request.name())) {

            throw new SnackAlreadyExistsException(
                    "Snack with this name already exists."
            );
        }

        snack.setName(request.name());
        snack.setDefaultUnit(request.defaultUnit());

        Snack updatedSnack = snackRepository.save(snack);

        return snackMapper.toResponse(updatedSnack);
    }

    @Transactional
    public void deleteSnack(Long snackId) {

        Snack snack = snackRepository.findById(snackId)
                .orElseThrow(() ->
                        new SnackNotFoundException(
                                "Snack not found."
                        )
                );

        snackRepository.delete(snack);
    }
}