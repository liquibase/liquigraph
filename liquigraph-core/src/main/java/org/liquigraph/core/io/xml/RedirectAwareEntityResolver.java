/*
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.io.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class RedirectAwareEntityResolver implements EntityResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectAwareEntityResolver.class);
    private static final int MAX_REDIRECT_COUNT = 10;

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException {
        URL url = new URL(systemId);
        HttpURLConnection connection = openConnection(url, 0);
        return new InputSource(connection.getInputStream());
    }

    private HttpURLConnection openConnection(URL url, int redirectCount) throws IOException {
        if (redirectCount == MAX_REDIRECT_COUNT) {
            throw new RuntimeException(String.format("Already followed %d redirect(s), stopping now", MAX_REDIRECT_COUNT));
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (isRedirect(connection.getResponseCode())) {
            String location = connection.getHeaderField("Location");
            LOGGER.debug("Following redirect from {} to {}", url.toExternalForm(), location);
            return openConnection(new URL(location), redirectCount + 1);
        }
        return connection;
    }

    private boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER;
    }

}
