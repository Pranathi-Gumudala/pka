package com.pranathi.pka.dto;

import java.util.List;

public record AgentResponse(String answer, List<RagSource> sources) {
}
