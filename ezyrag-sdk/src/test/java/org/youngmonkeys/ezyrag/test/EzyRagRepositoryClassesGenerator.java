package org.youngmonkeys.ezyrag.test;

import org.youngmonkeys.devtools.repository.RepositoryClassesGenerator;

public class EzyRagRepositoryClassesGenerator {

    public static void main(String[] args) throws Exception {
        new RepositoryClassesGenerator(Object.class)
            .generate();
    }
}
