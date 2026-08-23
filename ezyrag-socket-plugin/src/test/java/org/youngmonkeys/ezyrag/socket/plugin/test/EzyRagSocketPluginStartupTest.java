/*
 * Copyright 2026 youngmonkeys.org
 * 
 * Licensed under the ezyplatform, Version 1.0.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://youngmonkeys.org/licenses/ezyplatform-1.0.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.youngmonkeys.ezyrag.socket.plugin.test;

import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.constant.EzyMaxRequestPerSecondAction;
import com.tvd12.ezyfoxserver.embedded.EzyEmbeddedServer;
import com.tvd12.ezyfoxserver.ext.EzyAppEntry;
import com.tvd12.ezyfoxserver.ext.EzyPluginEntry;
import com.tvd12.ezyfoxserver.setting.*;
import org.youngmonkeys.ezyrag.socket.plugin.PluginEntryLoader;
import org.youngmonkeys.notification.message.app.entry.NotificationMessageAppEntryLoader;

public class EzyRagSocketPluginStartupTest {

    private static final String PLUGIN_NAME = "ezyrag";
    private static final String ZONE_NAME = "ezyrag";

    public static void main(String[] args) throws Exception {

        EzyPluginSettingBuilder pluginSettingBuilder = new EzyPluginSettingBuilder()
            .name(PLUGIN_NAME)
            .addListenEvent(EzyEventType.USER_LOGIN)
            .entryLoader(DecoratedPluginEntryLoader.class);

        EzyPluginSettingBuilder ezyAIPluginSettingBuilder = new EzyPluginSettingBuilder()
            .name("ezyai")
            .entryLoader(DecoratedEzyAIPluginEntryLoader.class);

        EzyPluginSettingBuilder graphqlPluginSettingBuilder = new EzyPluginSettingBuilder()
            .name("graphql")
            .entryLoader(DecoratedGraphQLPluginEntryLoader.class);

        EzyAppSettingBuilder ezychatAppSettingBuilder = new EzyAppSettingBuilder()
            .name("ezychat")
            .entryLoader(DecoratedEzyChatAppEntryLoader.class);

        EzyPluginSettingBuilder ezyChatPluginSettingBuilder = new EzyPluginSettingBuilder()
            .name("ezychat")
            .addListenEvent(EzyEventType.USER_LOGIN)
            .entryLoader(DecoratedPluginEntryLoader.class);

        EzyZoneSettingBuilder zoneSettingBuilder = new EzyZoneSettingBuilder()
            .name("chat")
            .plugin(pluginSettingBuilder.build())
            .plugin(graphqlPluginSettingBuilder.build())
            .plugin(ezyAIPluginSettingBuilder.build())
            .plugin(ezyChatPluginSettingBuilder.build())
            .application(ezychatAppSettingBuilder.build());

        EzyPluginSettingBuilder socketMonitorPluginSettingBuilder = new EzyPluginSettingBuilder()
            .name("socket-monitor")
            .addListenEvent(EzyEventType.USER_LOGIN)
            .entryLoader(com.ezyplatform.socket.monitor.plugin.EntryLoader.class);

        EzyAppSettingBuilder notificationMessageAppSettingBuilder =
            new EzyAppSettingBuilder()
                .name("notification-message")
                .entryLoader(NotificationMessageAppEntryLoader.class);

        EzyZoneSettingBuilder adminZoneSettingBuilder = new EzyZoneSettingBuilder()
            .name("admin")
            .plugin(socketMonitorPluginSettingBuilder.build())
            .application(notificationMessageAppSettingBuilder.build());

        EzySocketSettingBuilder socketSettingBuilder = new EzySocketSettingBuilder()
            .sslActive(true);

        EzySessionManagementSettingBuilder sessionManagementSettingBuilder =
            new EzySessionManagementSettingBuilder()
                .sessionMaxRequestPerSecond(
                    new EzySessionManagementSettingBuilder.EzyMaxRequestPerSecondBuilder()
                        .value(60)
                        .action(EzyMaxRequestPerSecondAction.DISCONNECT_SESSION)
                        .build()
                );

        EzySimpleSettings settings = new EzySettingsBuilder()
            .socket(socketSettingBuilder.build())
            .zone(zoneSettingBuilder.build())
            .zone(adminZoneSettingBuilder.build())
            .sessionManagement(sessionManagementSettingBuilder.build())
            .build();

        EzyEmbeddedServer server = EzyEmbeddedServer.builder()
            .settings(settings)
            .build();

        server.start();
    }

    public static class DecoratedPluginEntryLoader extends PluginEntryLoader {

        @Override
        public EzyPluginEntry load() {
            return new PluginEntry() {
                @Override
                protected String[] getScanablePackages() {
                    return new String[] { "org.youngmonkeys.ezyrag" };
                }
            };
        }
    }

    public static class DecoratedGraphQLPluginEntryLoader
        extends org.youngmonkeys.graphql.socket.plugin.PluginEntryLoader {

        @Override
        public EzyPluginEntry load() {
            return new PluginEntry() {
                @Override
                protected String[] getScanablePackages() {
                    return new String[] {
                        "org.youngmonkeys.graphql",
                        "org.youngmonkeys.ecommerce"
                    };
                }
            };
        }
    }

    public static class DecoratedEzyAIPluginEntryLoader
        extends org.youngmonkeys.ezyai.socket.plugin.PluginEntryLoader {

        @Override
        public EzyPluginEntry load() {
            return new PluginEntry() {};
        }
    }

    public static class DecoratedEzyChatAppEntryLoader
        extends org.youngmonkeys.ezychat.socket.app.AppEntryLoader {

        @Override
        public EzyAppEntry load() {
            return new AppEntry() {
                @Override
                protected String[] getScanablePackages() {
                    return new String[] {"org.youngmonkeys.ezychat"};
                }
            };
        }
    }
}
