/*
 * Copyright 2019-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package org.entando.kubernetes.model.entandohub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.entando.kubernetes.exception.EntandoComponentManagerException;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class EntandoHubRegistry {

    private String id;
    private String name;
    private String url;

    @JsonIgnore
    public URL getUrlAsURL() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new EntandoComponentManagerException("Error during URL parsing " + url);
        }
    }
}
