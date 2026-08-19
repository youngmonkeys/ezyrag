package org.youngmonkeys.ezyrag.web.test;

import org.youngmonkeys.devtools.swagger.SwaggerGenerator;

import java.io.File;

public class EzyRagWebPluginSwaggerGenerator {

    public static void main(String[] args) throws Exception {
        SwaggerGenerator swaggerGenerator = new SwaggerGenerator(
            "org.youngmonkeys.ezyrag.web.controller"
        );
        String filePath = "src/test/resources/static/files/swagger.yaml";
        File srcFolder = new File("src");
        if (!srcFolder.exists()) {
            filePath = "ezyrag-web-plugin/" + filePath;
        }
        swaggerGenerator.generateToFile(filePath);
    }
}
