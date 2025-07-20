package com.store.mgmt.organization.service;

import com.store.mgmt.organization.model.dto.CreateUserAssignmentDTO;
import com.store.mgmt.organization.model.dto.UpdateUserAssignmentDTO;
import com.store.mgmt.organization.model.dto.UserAssignmentDTO;

public interface UserAssignmentService {
    UserAssignmentDTO createUserAssignment(CreateUserAssignmentDTO dto);
    UserAssignmentDTO getUserAssignmentById(String id);
    UserAssignmentDTO updateUserAssignment(String id, UpdateUserAssignmentDTO dto);
}
