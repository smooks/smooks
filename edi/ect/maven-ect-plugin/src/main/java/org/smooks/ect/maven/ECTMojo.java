/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.ect.maven;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.smooks.ect.ECTUnEdifactExecutor;
import org.smooks.ect.EdiParseException;

import java.io.File;

/**
 * ECT Mojo.
 *
 * @author bardl
 */
@Execute(goal = "generate"
    , phase = LifecyclePhase.GENERATE_SOURCES
    , lifecycle = "generate-sources")
@Mojo(name = "generate"
    , requiresDependencyResolution = ResolutionScope.COMPILE)
public class ECTMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Parameter(required = true)
    private File src;

    @Parameter(required = true)
    private String srcType ;

    @Parameter(defaultValue = "target/ect", required = false)
    private File destDir;

    public void execute() throws MojoExecutionException {

        if(!src.exists()) {
        	throw new MojoExecutionException("EDI Specification file '" + src.getAbsolutePath() + "' not found.");
        }

        // Currently supports UN/EDIFACT only...
        if(srcType.equals("UNEDIFACT")) {
        	ECTUnEdifactExecutor ect = new ECTUnEdifactExecutor();

            try {
                ect.setUnEdifactZip(src);
                ect.setUrn(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
                ect.setMappingModelFolder(destDir);

                ect.execute();

                Resource resource = new Resource();
                resource.setDirectory(destDir.getPath());
                project.addResource(resource);

                getLog().info("UN/EDIFACT mapping model set for '" + src.getName() + "' generated in '" + destDir.getAbsolutePath() + "'.");
            } catch (EdiParseException e) {
                throw new MojoExecutionException("Error Executing ECT Maven Plugin.  See chained cause.", e);
            }
        } else {
            throw new MojoExecutionException("Unsupported ECT 'srcType' configuration value '" + srcType + "'.  Currently support 'UNEDIFACT' only.");
        }
    }
}
