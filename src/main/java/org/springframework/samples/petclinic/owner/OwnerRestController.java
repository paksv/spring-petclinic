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

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST controller for managing {@link Owner}s.
 *
 * @author Your Name
 */
@RestController
@RequestMapping("/api/owners")
public class OwnerRestController {

    private final OwnerRepository owners;

    public OwnerRestController(OwnerRepository owners) {
        this.owners = owners;
    }

    /**
     * Get all owners with pagination.
     *
     * @param page the page number (1-based)
     * @param size the page size
     * @return the list of owners
     */
    @GetMapping
    public ResponseEntity<Page<Owner>> getAllOwners(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(this.owners.findAll(pageable));
    }

    /**
     * Search owners by last name with pagination.
     *
     * @param lastName the last name to search for
     * @param page the page number (1-based)
     * @param size the page size
     * @return the list of owners matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Owner>> searchOwners(
            @RequestParam String lastName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(this.owners.findByLastNameStartingWith(lastName, pageable));
    }

    /**
     * Get a specific owner by ID.
     *
     * @param ownerId the ID of the owner
     * @return the owner
     */
    @GetMapping("/{ownerId}")
    public ResponseEntity<Owner> getOwner(@PathVariable("ownerId") int ownerId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        return optionalOwner.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new owner.
     *
     * @param owner the owner to create
     * @return the created owner
     */
    @PostMapping
    public ResponseEntity<Owner> createOwner(@Valid @RequestBody Owner owner) {
        if (owner.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Owner savedOwner = this.owners.save(owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOwner);
    }

    /**
     * Update an existing owner.
     *
     * @param ownerId the ID of the owner
     * @param ownerDetails the updated owner details
     * @return the updated owner
     */
    @PutMapping("/{ownerId}")
    public ResponseEntity<Owner> updateOwner(
            @PathVariable("ownerId") int ownerId,
            @Valid @RequestBody Owner ownerDetails) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();

        // Update owner properties
        owner.setFirstName(ownerDetails.getFirstName());
        owner.setLastName(ownerDetails.getLastName());
        owner.setAddress(ownerDetails.getAddress());
        owner.setCity(ownerDetails.getCity());
        owner.setTelephone(ownerDetails.getTelephone());

        Owner updatedOwner = this.owners.save(owner);
        return ResponseEntity.ok(updatedOwner);
    }

    /**
     * Delete an owner.
     *
     * @param ownerId the ID of the owner
     * @return no content if successful
     */
    @DeleteMapping("/{ownerId}")
    public ResponseEntity<Void> deleteOwner(@PathVariable("ownerId") int ownerId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.owners.delete(optionalOwner.get());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all pet types.
     *
     * @return the list of pet types
     */
    @GetMapping("/pettypes")
    public ResponseEntity<List<PetType>> getPetTypes() {
        return ResponseEntity.ok(this.owners.findPetTypes());
    }
}
