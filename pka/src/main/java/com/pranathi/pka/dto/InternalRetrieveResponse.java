package com.pranathi.pka.dto;

import java.util.List;

public record InternalRetrieveResponse(String context, List<RagSource> sources) {
}
