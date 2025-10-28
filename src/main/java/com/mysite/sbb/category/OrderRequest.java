package com.mysite.sbb.category;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class OrderRequest {
    private List<Long> orderedIds;
}