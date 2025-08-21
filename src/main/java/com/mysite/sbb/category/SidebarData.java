package com.mysite.sbb.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SidebarData {
    private final List<Category> roots;
    private final Map<Long, List<Category>> childrenMap;
}