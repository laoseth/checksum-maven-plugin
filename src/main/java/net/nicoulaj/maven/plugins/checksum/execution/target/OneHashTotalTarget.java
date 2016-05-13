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
package net.nicoulaj.maven.plugins.checksum.execution.target;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import edu.emory.mathcs.backport.java.util.Arrays;
import net.nicoulaj.maven.plugins.checksum.Constants;
import net.nicoulaj.maven.plugins.checksum.mojo.ChecksumFile;

/**
 * An {@link ExecutionTarget} that writes digests of multiple files to a single property file per algorithm.
 *
 * @author <a href="mailto:julien.nicoulaud@gmail.com">Julien Nicoulaud</a>
 * @since 1.0
 */
public class OneHashTotalTarget
    implements ExecutionTarget
{
    /**
     * Encoding to use for generated files.
     */
	protected Integer maxHashLength;
	protected String propertyNamePrefix;
    protected MavenProject project;
    protected Map<String, List<String>> filesHashcodes;
	private Log logger;

    /**
     * Build a new instance of {@link OneHashTotalTarget}.
     *
     * @param encoding the encoding to use for generated files.
     * @param artifactListeners
     */
    public OneHashTotalTarget(MavenProject project, String propertyNamePrefix, Integer maxHashLength)
    {
    	 this.project = project;
         this.propertyNamePrefix=propertyNamePrefix;
         this.maxHashLength=maxHashLength;
    }

    public OneHashTotalTarget(MavenProject project, String propertyNamePrefix, Integer maxHashLength, Log log) {
    	 this.project = project;
         this.propertyNamePrefix=propertyNamePrefix;
         this.maxHashLength=maxHashLength;
         this.logger = log;
	}

	/**
     * {@inheritDoc}
     */
    public void init()
    {
    	filesHashcodes = new HashMap<String, List<String>>();
    }

    /**
     * {@inheritDoc}
     */
    public void write( String digest, ChecksumFile file, String algorithm )
        throws ExecutionTargetWriteException
    {
    	if(!filesHashcodes.containsKey(algorithm))
    		filesHashcodes.put( algorithm, new ArrayList<String>() );
    	
    	filesHashcodes.get(algorithm).add(digest);
    		
    		
    	
    }


    @SuppressWarnings("unchecked")
	private static final List<String> base10 = Arrays.asList(Constants.BASE_10_ALGORITHMS);
    /**
     * {@inheritDoc}
     */
    public void close(String subPath)
    {
    	for(Entry<String, List<String>> entry: filesHashcodes.entrySet()){
    		String alg = entry.getKey();
    		int base;
    		if(base10.contains(alg))
    			base=10;
    		else
    			base=16;
    		BigInteger total =  BigInteger.ZERO;
    		for(String amount: entry.getValue())
    			total=total.xor(new BigInteger(amount,base));
    		String combinedHash = total.toString(base);
    		if(maxHashLength!=null && combinedHash.length()>maxHashLength)
    			combinedHash = combinedHash.substring(0, maxHashLength);
    		project.getProperties().put(propertyNamePrefix+entry.getKey(), combinedHash);
    		if(logger!=null)
    			logger.info("Set the property '"+propertyNamePrefix+entry.getKey()+"' to "+combinedHash);
    	}
    }
}
