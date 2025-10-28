package com.mysite.sbb.category;

import lombok.Value;
import java.util.List;
import java.util.Map;

@Value
public class DeleteResult {
    List<Long> deletedIds;
    Map<Long, String> failures;
}