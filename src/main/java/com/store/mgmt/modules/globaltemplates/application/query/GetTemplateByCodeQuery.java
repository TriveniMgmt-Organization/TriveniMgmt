package com.store.mgmt.modules.globaltemplates.application.query;

import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.shared.application.query.Query;

public record GetTemplateByCodeQuery(
        String code
) implements Query<GlobalTemplateResponseDTO> {
}
