package com.example.mvccrud.global;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SortValidator {

    public void validate(Pageable pageable, Set<String> allowedFields) {
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();

            if (!allowedFields.contains(property)) {
                throw new InvalidSortException(property);
            }
        }
    }
}
