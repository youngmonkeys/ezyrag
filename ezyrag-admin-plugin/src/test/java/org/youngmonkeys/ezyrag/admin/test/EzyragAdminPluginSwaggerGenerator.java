package org.youngmonkeys.ezyrag.admin.test;

import org.youngmonkeys.devtools.swagger.SwaggerGenerator;

import java.io.File;

public class EzyRagAdminPluginSwaggerGenerator {

    public static void main(String[] args) throws Exception {
        SwaggerGenerator swaggerGenerator = new SwaggerGenerator(
            "org.youngmonkeys.ezyrag.admin.controller"
        );
        String filePath = "src/test/resources/static/files/swagger.yaml";
        File srcFolder = new File("src");
        if (!srcFolder.exists()) {
            filePath = "ezyrag-admin-plugin/" + filePath;
        }
        swaggerGenerator.generateToFile(filePath);
    }
}
