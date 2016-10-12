/*******************************************************************************
 * Copyright 2016 Intuit
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
 *******************************************************************************/
package com.intuit.wasabi.analyticsobjects.wrapper;

import com.intuit.wasabi.experimentobjects.Application;
import com.intuit.wasabi.experimentobjects.Bucket;
import com.intuit.wasabi.experimentobjects.Experiment;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


/**
 * This is the test class for {@link ExperimentDetail}.
 */
public class ExperimentDetailTest {

    Experiment.ID expId = Experiment.ID.newInstance();
    Experiment.Label expLabel = Experiment.Label.valueOf("TestLabelForExperimentDetail");
    Experiment.State expState = Experiment.State.RUNNING;
    Application.Name appName = Application.Name.valueOf("TestApplicationLabelForExperimentDetail");
    Calendar modTime = Calendar.getInstance();
    Calendar startTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();

    {
        modTime.set(2016, 8, 16);
        startTime.set(2016, 8, 1);
        endTime.set(2017, 10, 12);

    }

    Experiment exp = Experiment.withID(expId).withApplicationName(appName).withModificationTime(modTime.getTime())
            .withLabel(expLabel).withStartTime(startTime.getTime()).withState(Experiment.State.TERMINATED).build();


    @Test
    public void testConstructor() {
        ExperimentDetail expDetail = new ExperimentDetail(expId, expState, expLabel, appName, modTime.getTime(),
                startTime.getTime(), endTime.getTime());
        assertEquals(expDetail.getApplicationName(), appName);
        assertEquals(expDetail.getId(), expId);
        assertEquals(expDetail.getLabel(), expLabel);
        assertEquals(expDetail.getState(), expState);
        assertEquals(expDetail.getModificationTime(), modTime.getTime());
        assertEquals(expDetail.getEndTime(), endTime.getTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstraintsId() {
        new ExperimentDetail(null, expState, expLabel, appName, modTime.getTime(),
                startTime.getTime(), endTime.getTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstraintsState() {
        new ExperimentDetail(expId, null, expLabel, appName, modTime.getTime(),
                startTime.getTime(), endTime.getTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstraintsLabel() {
        new ExperimentDetail(expId, expState, null, appName, modTime.getTime(),
                startTime.getTime(), endTime.getTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstraintsAppName() {
        new ExperimentDetail(expId, expState, expLabel, null, modTime.getTime(),
                startTime.getTime(), endTime.getTime());
    }

    @Test
    public void testCreationFromExperiment() {

        ExperimentDetail expDetail = new ExperimentDetail(exp);

        assertEquals(exp.getID(), expDetail.getId());
        assertEquals(exp.getApplicationName(), expDetail.getApplicationName());
        assertEquals(exp.getModificationTime(), expDetail.getModificationTime());
        assertEquals(exp.getLabel(), expDetail.getLabel());
        assertEquals(exp.getStartTime(), expDetail.getStartTime());
        assertEquals(exp.getState(), expDetail.getState());

    }

    @Test
    public void testBuckets() {
        Bucket.Label labelA = Bucket.Label.valueOf("BucketA");
        Bucket.Label labelB = Bucket.Label.valueOf("BucketB");
        Bucket.Label labelC = Bucket.Label.valueOf("BucketC");

        List<Bucket> buckets = new ArrayList<>();
        Bucket bucketA = Bucket.newInstance(expId, labelA)
                .withAllocationPercent(0.8).withControl(true).build();
        Bucket bucketB = Bucket.newInstance(expId, labelB)
                .withAllocationPercent(0.1).withControl(false).build();
        Bucket bucketC = Bucket.newInstance(expId, labelC)
                .withAllocationPercent(0.1).withControl(false).build();
        buckets.add(bucketA);
        buckets.add(bucketB);
        buckets.add(bucketC);

        // create experimentdetail
        ExperimentDetail expDetail = new ExperimentDetail(exp);
        expDetail.addBuckets(buckets);

        assertEquals(expDetail.getBuckets().size(), 3);

        // check labels
        List<Bucket.Label> labels = buckets.stream().map(Bucket::getLabel).collect(Collectors.toList());
        assertEquals(labels, expDetail.getBuckets().stream().
                map(ExperimentDetail.BucketDetail::getLabel).
                collect(Collectors.toList()));

        // check allocation percent
        List<Double> percentage = buckets.stream().map(Bucket::getAllocationPercent).collect(Collectors.toList());
        assertEquals(percentage, expDetail.getBuckets().stream().
                map(ExperimentDetail.BucketDetail::getAllocationPercent).
                collect(Collectors.toList()));

        // check isControl
        List<Boolean> controls = buckets.stream().map(Bucket::isControl).collect(Collectors.toList());
        assertEquals(controls, expDetail.getBuckets().stream().
                map(ExperimentDetail.BucketDetail::isControl).
                collect(Collectors.toList()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidValueCounts() {
        Bucket.Label labelA = Bucket.Label.valueOf("BucketA");
        List<Bucket> buckets = new ArrayList<>();
        Bucket bucketA = Bucket.newInstance(expId, labelA)
                .withAllocationPercent(1.01).withControl(true).build();
        buckets.add(bucketA);

        ExperimentDetail expDetail = new ExperimentDetail(exp);
        expDetail.addBuckets(buckets);

        ExperimentDetail.BucketDetail buckDetails = expDetail.getBuckets().get(0);
        buckDetails.setCount(-1l);
    }

    @Test
    public void testAnalyticsFields() {
        Bucket.Label labelA = Bucket.Label.valueOf("BucketA");

        List<Bucket> buckets = new ArrayList<>();
        Bucket bucketA = Bucket.newInstance(expId, labelA)
                .withAllocationPercent(0.8).withControl(true).build();
        buckets.add(bucketA);

        // create experimentdetail
        ExperimentDetail expDetail = new ExperimentDetail(exp);
        expDetail.addBuckets(buckets);


        // check actionRate
        double actionRate = 0.5;
        expDetail.getBuckets().get(0).setActionRate(actionRate);
        assertTrue(actionRate == expDetail.getBuckets().get(0).getActionRate());

        // check LowerBound
        double lowerBound = 0.12;
        expDetail.getBuckets().get(0).setLowerBound(lowerBound);
        assertTrue(lowerBound == expDetail.getBuckets().get(0).getLowerBound());

        // check UpperBound
        double upperBound = 0.42;
        expDetail.getBuckets().get(0).setUpperBound(upperBound);
        assertTrue(upperBound == expDetail.getBuckets().get(0).getUpperBound());

        // check Count
        long count = 17284l;
        expDetail.getBuckets().get(0).setCount(count);
        assertTrue(count == expDetail.getBuckets().get(0).getCount());

        // check winner
        Boolean winnerSoFar = true;
        expDetail.getBuckets().get(0).setWinnerSoFar(winnerSoFar);
        assertTrue(expDetail.getBuckets().get(0).isWinnerSoFar());

        assertTrue(expDetail.getBuckets().get(0).isLoserSoFar() == null);

    }
}

