/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST controller for managing {@link Visit}s.
 *
 * @author Your Name
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/visits")
public class VisitRestController {

    private final OwnerRepository owners;

    public VisitRestController(OwnerRepository owners) {
        this.owners = owners;
    }

    /**
     * Get all visits for a pet.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @return the list of visits
     */
    @GetMapping
    public ResponseEntity<Collection<Visit>> getAllVisitsForPet(@PathVariable("ownerId") int ownerId,
            @PathVariable("petId") int petId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pet.getVisits());
    }

    /**
     * Get a specific visit.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @param visitId the ID of the visit
     * @return the visit
     */
    @GetMapping("/{visitId}")
    public ResponseEntity<Visit> getVisit(@PathVariable("ownerId") int ownerId,
            @PathVariable("petId") int petId, @PathVariable("visitId") int visitId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        Visit visit = findVisitById(pet, visitId);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(visit);
    }

    /**
     * Create a new visit.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @param visit the visit to create
     * @return the created visit
     */
    @PostMapping
    public ResponseEntity<Visit> createVisit(@PathVariable("ownerId") int ownerId,
            @PathVariable("petId") int petId, @Valid @RequestBody Visit visit) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        pet.addVisit(visit);
        this.owners.save(owner);

        return ResponseEntity.status(HttpStatus.CREATED).body(visit);
    }

    /**
     * Update an existing visit.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @param visitId the ID of the visit
     * @param visitDetails the updated visit details
     * @return the updated visit
     */
    @PutMapping("/{visitId}")
    public ResponseEntity<Visit> updateVisit(@PathVariable("ownerId") int ownerId,
            @PathVariable("petId") int petId, @PathVariable("visitId") int visitId,
            @Valid @RequestBody Visit visitDetails) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        Visit visit = findVisitById(pet, visitId);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }

        // Update visit properties
        visit.setDate(visitDetails.getDate());
        visit.setDescription(visitDetails.getDescription());

        this.owners.save(owner);

        return ResponseEntity.ok(visit);
    }

    /**
     * Delete a visit.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @param visitId the ID of the visit
     * @return no content if successful
     */
    @DeleteMapping("/{visitId}")
    public ResponseEntity<Void> deleteVisit(@PathVariable("ownerId") int ownerId,
            @PathVariable("petId") int petId, @PathVariable("visitId") int visitId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        Visit visit = findVisitById(pet, visitId);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }

        pet.getVisits().remove(visit);
        this.owners.save(owner);

        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to find a visit by ID in a pet's visit collection.
     *
     * @param pet the pet
     * @param visitId the ID of the visit
     * @return the visit, or null if not found
     */
    private Visit findVisitById(Pet pet, int visitId) {
        for (Visit visit : pet.getVisits()) {
            if (visit.getId() != null && visit.getId() == visitId) {
                return visit;
            }
        }
        return null;
    }
}
