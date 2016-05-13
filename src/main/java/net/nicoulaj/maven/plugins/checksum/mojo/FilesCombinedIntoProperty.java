/*
 * Copyright 2010-2016 Julien Nicoulaud <julien.nicoulaud@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.nicoulaj.maven.plugins.checksum.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import net.nicoulaj.maven.plugins.checksum.execution.target.ExecutionTarget;
import net.nicoulaj.maven.plugins.checksum.execution.target.OneHashTotalTarget;

/**
 * Compute specified files checksum digests, combines them, and then stores them in a property file per algorithm
 *
 * The files are not filtered.
 *
 * @author <a href="mailto:julien.nicoulaud@gmail.com">Julien Nicoulaud</a>
 * @since 1.0
 */
@Mojo(
    name = FilesCombinedIntoProperty.NAME,
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    requiresProject = true,
    inheritByDefault = false,
    threadSafe = true )
public class FilesCombinedIntoProperty
    extends AbstractChecksumMojo
{
    /**
     * The mojo name.
     */
    public static final String NAME = "combined";

    @Parameter( property = "project", required = true, readonly = true )
    protected MavenProject project;
    /**
     * The default file inclusion pattern.
     *
     * @see #getFilesToProcess()
     */
    protected static final String[] DEFAULT_INCLUDES = { "**/**" };

    /**
     * The list of files to process.
     * <p/>
     * <p> Use the following syntax:
     * <pre>&lt;fileSets&gt;
     *   &lt;fileSet&gt;
     *     &lt;directory&gt;...&lt;/directory&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;...&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;...&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/fileSet&gt;
     * &lt;/fileSets&gt;</pre>
     * </p>
     *
     * @since 1.1
     */
    @Parameter( required = true )
    protected List<FileSet> fileSets;

    @Parameter( defaultValue = "filesHash-" )
    protected String propertyNamePrefix;
    
    @Parameter( required=false )
    protected Integer maxHashCharLength;
   
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	otherTargets=new ArrayList<ExecutionTarget>();
    	if(quiet)
    		otherTargets.add(new OneHashTotalTarget(project, propertyNamePrefix,maxHashCharLength));
    	else
    		otherTargets.add(new OneHashTotalTarget(project, propertyNamePrefix,maxHashCharLength,getLog()));
    	super.execute();
    }
    @Override
    protected List<ChecksumFile> getFilesToProcess()
    {
        final List<ChecksumFile> filesToProcess = new ArrayList<ChecksumFile>();
        for ( final FileSet fileSet : fileSets )
        {
            final DirectoryScanner scanner = new DirectoryScanner();
            final String fileSetDirectory = (new File( fileSet.getDirectory() ) ).getPath();

            scanner.setBasedir( fileSetDirectory );
            String[] includes;
            if ( fileSet.getIncludes() != null && !fileSet.getIncludes().isEmpty() )
            {
                final List<String> fileSetIncludes = fileSet.getIncludes();
                includes = fileSetIncludes.toArray( new String[fileSetIncludes.size()] );
            }
            else
            {
                includes = DEFAULT_INCLUDES;
            }
            scanner.setIncludes( includes );

            if ( fileSet.getExcludes() != null && !fileSet.getExcludes().isEmpty() )
            {
                final List<String> fileSetExcludes = fileSet.getExcludes();
                scanner.setExcludes( fileSetExcludes.toArray( new String[fileSetExcludes.size()] ) );
            }

            scanner.addDefaultExcludes();

            scanner.scan();

            for ( String filePath : scanner.getIncludedFiles() )
            {
                filesToProcess.add( new ChecksumFile( (new File( fileSetDirectory ) ).getPath(), new File( fileSetDirectory, filePath ) ) );
            }
            }

        return filesToProcess;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIndividualFiles()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIndividualFilesOutputDirectory()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCsvSummary()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCsvSummaryFile()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isXmlSummary()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXmlSummaryFile()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isShasumSummary()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getShasumSummaryFile()
    {
        return null;
    }
    
    protected boolean isCombinedHash() {
		return true;
	}
}
