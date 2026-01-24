package com.store.mgmt.modules.globaltemplates.application.query;

import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

public record GetTemplatesByTypeQuery(
        String type
) implements Query<List<GlobalTemplateResponseDTO>> {
}
