package com.example.robotcontrolsystembackend.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {

	private final ObjectMapper objectMapper = JsonMapper.builder()
			.findAndAddModules()
			.build();

	public String toJson(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(node);
		} catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Invalid JSON payload", ex);
		}
	}

	public Object parseOrRaw(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readTree(raw);
		} catch (Exception ex) {
			return raw;
		}
	}
}
