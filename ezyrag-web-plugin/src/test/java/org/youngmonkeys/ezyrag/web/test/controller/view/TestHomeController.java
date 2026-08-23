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

package org.youngmonkeys.ezyrag.web.test.controller.view;

import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.view.View;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class TestHomeController {

    @Authenticatable
    @DoGet("/")
    public View homeGet() {
        return View.builder()
            .template("home")
            .addVariable("pageTitle", "ezyai")
            .build();
    }

    @DoGet("/login")
    public View loginGet() {
        return View.builder()
            .template("home")
            .addVariable("showLogin", true)
            .addVariable("pageTitle", "ezyai")
            .build();
    }
}
