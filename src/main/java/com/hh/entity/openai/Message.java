package com.hh.entity.openai;

import lombok.Builder;
import lombok.Data;

/**
 * @author ab875
 */
@Data
@Builder
public class Message {
    private String role;
    private String content;
}
