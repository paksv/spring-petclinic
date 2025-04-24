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
 * REST controller for managing {@link Pet}s.
 *
 * @author Your Name
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets")
public class PetRestController {

    private final OwnerRepository owners;
    private final PetValidator petValidator;

    public PetRestController(OwnerRepository owners) {
        this.owners = owners;
        this.petValidator = new PetValidator();
    }

    /**
     * Get all pets for an owner.
     *
     * @param ownerId the ID of the owner
     * @return the list of pets
     */
    @GetMapping
    public ResponseEntity<Collection<Pet>> getAllPetsForOwner(@PathVariable("ownerId") int ownerId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        return ResponseEntity.ok(owner.getPets());
    }

    /**
     * Get a specific pet.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @return the pet
     */
    @GetMapping("/{petId}")
    public ResponseEntity<Pet> getPet(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pet);
    }

    /**
     * Create a new pet.
     *
     * @param ownerId the ID of the owner
     * @param pet the pet to create
     * @return the created pet
     */
    @PostMapping
    public ResponseEntity<Pet> createPet(@PathVariable("ownerId") int ownerId, @Valid @RequestBody Pet pet) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();

        // Check if pet with same name already exists
        if (pet.getName() != null && owner.getPet(pet.getName(), true) != null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate pet
        petValidator.validate(pet, null);

        owner.addPet(pet);
        this.owners.save(owner);

        return ResponseEntity.status(HttpStatus.CREATED).body(pet);
    }

    /**
     * Update an existing pet.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @param petDetails the updated pet details
     * @return the updated pet
     */
    @PutMapping("/{petId}")
    public ResponseEntity<Pet> updatePet(
            @PathVariable("ownerId") int ownerId,
            @PathVariable("petId") int petId,
            @Valid @RequestBody Pet petDetails) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if pet with same name already exists (excluding current pet)
        String newName = petDetails.getName();
        if (newName != null) {
            Pet existingPet = owner.getPet(newName, false);
            if (existingPet != null && !existingPet.getId().equals(pet.getId())) {
                return ResponseEntity.badRequest().build();
            }
        }

        // Validate pet
        petValidator.validate(petDetails, null);

        // Update pet properties
        pet.setName(petDetails.getName());
        pet.setBirthDate(petDetails.getBirthDate());
        pet.setType(petDetails.getType());

        this.owners.save(owner);

        return ResponseEntity.ok(pet);
    }

    /**
     * Delete a pet.
     *
     * @param ownerId the ID of the owner
     * @param petId the ID of the pet
     * @return no content if successful
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId) {
        Optional<Owner> optionalOwner = this.owners.findById(ownerId);
        if (optionalOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Owner owner = optionalOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }

        owner.getPets().remove(pet);
        this.owners.save(owner);

        return ResponseEntity.noContent().build();
    }
}
