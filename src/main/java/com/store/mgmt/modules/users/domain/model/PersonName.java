package com.store.mgmt.modules.users.domain.model;

/**
 * Value object representing a person's name.
 */
public record PersonName(String firstName, String lastName) {

    public PersonName {
        // Both can be null/empty, but if provided, trim them
        firstName = firstName != null ? firstName.trim() : null;
        lastName = lastName != null ? lastName.trim() : null;
    }

    public static PersonName of(String firstName, String lastName) {
        return new PersonName(firstName, lastName);
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
