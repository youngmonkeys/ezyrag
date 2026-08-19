package org.youngmonkeys.ezyrag.test;

import org.youngmonkeys.devtools.pagination.PaginationClassesGenerator;

public class EzyRagPaginationClassesGenerator {

    public static void main(String[] args) throws Exception {
        new PaginationClassesGenerator(Object.class)
            .generate();
    }
}
