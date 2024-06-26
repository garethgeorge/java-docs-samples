/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.asset.v1.ContentType;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.pubsub.v1.TopicName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/** Tests for real time feed sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RealTimeFeedIT {
  private static final String topicId = "topicId";
  private static final String feedId = UUID.randomUUID().toString();
  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private final String projectNumber = getProjectNumber(projectId);
  private final String feedName = String.format("projects/%s/feeds/%s", projectNumber, feedId);
  private final String[] assetNames = {UUID.randomUUID().toString()};
  private static final TopicName topicName = TopicName.of(projectId, topicId);
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private String getProjectNumber(String projectId) {
    ResourceManager resourceManager = ResourceManagerOptions.getDefaultInstance().getService();
    ProjectInfo project = resourceManager.get(projectId);
    return Long.toString(project.getProjectNumber());
  }

  @BeforeClass
  public static void createTopic() throws Exception {
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.createTopic(topicName);
    }
  }

  @AfterClass
  public static void deleteTopic() throws Exception {
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topicName);
    }
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void test1CreateFeedExample() throws Exception {
    CreateFeedExample.createFeed(
        assetNames, feedId, topicName.toString(), projectId, ContentType.RESOURCE);
    String got = bout.toString();
    assertThat(got).contains("Feed created successfully: " + feedName);
  }

  @Test
  public void test1CreateFeedRelationshipExample() throws Exception {
    CreateFeedExample.createFeed(
        assetNames,
        feedId + "relationship",
        topicName.toString(),
        projectId,
        ContentType.RELATIONSHIP);
    String got = bout.toString();
    assertThat(got).contains("Feed created successfully: " + feedName);
  }

  @Test
  public void test2GetFeedExample() throws Exception {
    GetFeedExample.getFeed(feedName);
    String got = bout.toString();
    assertThat(got).contains("Get a feed: " + feedName);
  }

  @Test
  public void test3ListFeedsExample() throws Exception {
    ListFeedsExample.listFeeds(projectId);
    String got = bout.toString();
    assertThat(got).contains("Listed feeds under: " + projectId);
  }

  @Test
  public void test4UpdateFeedExample() throws Exception {
    UpdateFeedExample.updateFeed(feedName, topicName.toString());
    String got = bout.toString();
    assertThat(got).contains("Feed updated successfully: " + feedName);
  }

  @Test
  public void test5DeleteFeedExample() throws Exception {
    DeleteFeedExample.deleteFeed(feedName);
    DeleteFeedExample.deleteFeed(feedName + "relationship");
    String got = bout.toString();
    assertThat(got).contains("Feed deleted");
  }
}
