/*
 * Copyright 2018 Google LLC
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

package com.google.samples;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.jobs.v3.CloudTalentSolution;
import com.google.api.services.jobs.v3.CloudTalentSolutionScopes;
import com.google.api.services.jobs.v3.model.Company;
import com.google.api.services.jobs.v3.model.ListCompaniesResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.Collections;

/** The quickstart for Cloud Job Discovery */
public class JobServiceQuickstart {

  // [START job_search_quick_start]
  // [START quickstart]

  private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static final NetHttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();
  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient =
      createTalentSolutionClient(generateCredential());

  private static CloudTalentSolution createTalentSolutionClient(GoogleCredentials credential) {
    String url = "https://jobs.googleapis.com";

    HttpRequestInitializer requestInitializer =
        request -> {
          new HttpCredentialsAdapter(credential).initialize(request);
          request.setConnectTimeout(60000); // 1 minute connect timeout
          request.setReadTimeout(60000); // 1 minute read timeout
        };

    return new CloudTalentSolution.Builder(NET_HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName("JobServiceClientSamples")
        .setRootUrl(url)
        .build();
  }

  private static GoogleCredentials generateCredential() {
    try {
      // Credentials could be downloaded after creating service account
      // set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable, for example:
      // export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/key.json
      return GoogleCredentials.getApplicationDefault()
          .createScoped(Collections.singleton(CloudTalentSolutionScopes.JOBS));
    } catch (Exception e) {
      System.out.println("Error in generating credential");
      throw new RuntimeException(e);
    }
  }

  public static CloudTalentSolution getTalentSolutionClient() {
    return talentSolutionClient;
  }

  public static void main(String... args) throws Exception {
    try {
      ListCompaniesResponse listCompaniesResponse =
          talentSolutionClient.projects().companies().list(DEFAULT_PROJECT_ID).execute();
      System.out.println("Request Id is " + listCompaniesResponse.getMetadata().getRequestId());
      if (listCompaniesResponse.getCompanies() != null) {
        for (Company company : listCompaniesResponse.getCompanies()) {
          System.out.println(company.getName());
        }
      }
    } catch (IOException e) {
      System.out.println("Got exception while listing companies");
      throw e;
    }
  }

  // [END quickstart]
  // [END job_search_quick_start]
}
