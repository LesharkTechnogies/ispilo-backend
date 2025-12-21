package com.ispilo.model.dto.request;

import com.ispilo.model.enums.ConversationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConversationRequest {

    @NotNull(message = "Conversation type is required")
    private ConversationType type;

    @NotEmpty(message = "At least one participant is required")
    private List<String> participantIds;
}

