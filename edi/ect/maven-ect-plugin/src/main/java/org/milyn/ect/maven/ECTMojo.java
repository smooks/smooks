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
package org.milyn.ect.maven;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.milyn.ect.ECTUnEdifactExecutor;
import org.milyn.ect.EdiParseException;

import java.io.File;

/**
 * ECT Mojo.
 * 
 * @author bardl
 */
@MojoGoal("generate")
@MojoPhase("generate-sources")
@MojoRequiresDependencyResolution
public class ECTMojo extends AbstractMojo {

    @MojoParameter(expression = "${project}", required = true, readonly = true)
    private MavenProject project;

    @MojoParameter(required = true, description = "The message definition file.  Depends on the message definition type ('srcType') e.g. for UN/EDIFACT, this is a ZIP file that can be downloaded from the web.")
    private File src;

    @MojoParameter(required = true, description = "The EDI message definition type.  Currently Supports 'UNEDIFACT' only.")
    private String srcType ;

    @MojoParameter(expression = "target/ect", required = false)
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
