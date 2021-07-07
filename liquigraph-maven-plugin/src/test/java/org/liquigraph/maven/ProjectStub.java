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
package org.liquigraph.maven;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.shared.utils.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

class ProjectStub extends MavenProjectStub {

    private final File baseDir;

    ProjectStub(File baseDir) throws Exception {
        this.baseDir = baseDir;
        setModel(getBasedir());

        Build build = new Build();
        build.setDirectory(getBasedir() + "/target");
        setBuild(build);
        setCompileSourceRoots(Collections.singletonList(getBasedir() + "/src/main/resources"));
    }

    @Override
    public File getBasedir() {
        return baseDir;
    }

    private void setModel(File basedir) throws Exception {
        Model model = readPom(basedir);
        setModel(model);
        setGroupId(model.getGroupId());
        setArtifactId(model.getArtifactId());
        setVersion(model.getVersion());
        setName(model.getName());
        setUrl(model.getUrl());
        setPackaging(model.getPackaging());
    }

    private Model readPom(File basedir) throws IOException, XmlPullParserException {
        try (Reader reader = ReaderFactory.newXmlReader(new File(basedir, "pom.xml"))) {
            return new MavenXpp3Reader().read(reader);
        }
    }
}
