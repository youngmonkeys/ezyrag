package org.youngmonkeys.ezyrag.web.test;

import com.tvd12.ezyhttp.server.boot.EzyHttpApplicationBootstrap;
import com.tvd12.ezyhttp.server.core.annotation.ComponentsScan;
import com.tvd12.ezyhttp.server.core.annotation.PropertiesSources;

@PropertiesSources({
    "config.properties",
    "setup.properties"
})
@ComponentsScan({
    "org.youngmonkeys.ezyplatform",
    "org.youngmonkeys.ezyrag",
    "org.youngmonkeys.ezyai"
})
public class EzyRagWebPluginStartupTest {

    public static void main(String[] args) throws Exception {
        EzyHttpApplicationBootstrap.start(EzyRagWebPluginStartupTest.class);
    }
}
