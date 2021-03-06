/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.figis;

import it.geosolutions.figis.requester.requester.util.IEConfigUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author DamianoG
 *
 */
public class IEConfigUtilsTest {

    @Test
    public void compareNullAndWhitespaceSafeTest(){
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe(null,null));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("",null));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe(null,""));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("",""));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("  ",""));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("","         "));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("",null));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe(null,"         "));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("aaa","aaa"));
        Assert.assertTrue(IEConfigUtils.compareNullAndWhitespaceSafe("aaa "," aaa"));
        
        Assert.assertFalse(IEConfigUtils.compareNullAndWhitespaceSafe("aaa "," aa a"));
        Assert.assertFalse(IEConfigUtils.compareNullAndWhitespaceSafe(null,"."));
        Assert.assertFalse(IEConfigUtils.compareNullAndWhitespaceSafe("s",null));
    }
    
}
