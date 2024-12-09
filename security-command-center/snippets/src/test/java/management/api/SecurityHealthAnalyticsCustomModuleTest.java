/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package management.api;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.securitycentermanagement.v1.EffectiveSecurityHealthAnalyticsCustomModule;
import com.google.cloud.securitycentermanagement.v1.ListSecurityHealthAnalyticsCustomModulesRequest;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient.ListDescendantSecurityHealthAnalyticsCustomModulesPagedResponse;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient.ListEffectiveSecurityHealthAnalyticsCustomModulesPagedResponse;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient.ListSecurityHealthAnalyticsCustomModulesPagedResponse;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule.EnablementState;
import com.google.cloud.securitycentermanagement.v1.SimulateSecurityHealthAnalyticsCustomModuleResponse;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SecurityHealthAnalyticsCustomModuleTest {
  // TODO(Developer): Replace the below variable
  private static final String PROJECT_ID = System.getenv("SCC_PROJECT_ID");
  private static final String CUSTOM_MODULE_DISPLAY_NAME = "java_sample_custom_module_test";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule =
      new MultipleAttemptsRule(MAX_ATTEMPT_COUNT, INITIAL_BACKOFF_MILLIS);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("SCC_PROJECT_ID");
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    // Perform cleanup after running tests
    cleanupExistingCustomModules();
  }

  // cleanupExistingCustomModules clean up all the existing custom module
  private static void cleanupExistingCustomModules() throws IOException {
    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {
      ListSecurityHealthAnalyticsCustomModulesRequest request =
          ListSecurityHealthAnalyticsCustomModulesRequest.newBuilder()
              .setParent(String.format("projects/%s/locations/global", PROJECT_ID))
              .build();
      ListSecurityHealthAnalyticsCustomModulesPagedResponse response =
          client.listSecurityHealthAnalyticsCustomModules(request);
      // Iterate over the response and delete custom module one by one which start with
      // java_sample_custom_module
      for (SecurityHealthAnalyticsCustomModule module : response.iterateAll()) {
        if (module.getDisplayName().startsWith("java_sample_custom_module")) {
          String customModuleId = extractCustomModuleId(module.getName());
          deleteCustomModule(PROJECT_ID, customModuleId);
        }
      }
    }
  }

  // extractCustomModuleID extracts the custom module Id from the full name
  private static String extractCustomModuleId(String customModuleFullName) {
    if (!Strings.isNullOrEmpty(customModuleFullName)) {
      Pattern pattern = Pattern.compile(".*/([^/]+)$");
      Matcher matcher = pattern.matcher(customModuleFullName);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return "";
  }

  // createCustomModule method is for creating the custom module
  private static SecurityHealthAnalyticsCustomModule createCustomModule(
      String projectId, String customModuleDisplayName) throws IOException {
    if (!Strings.isNullOrEmpty(projectId) && !Strings.isNullOrEmpty(customModuleDisplayName)) {
      SecurityHealthAnalyticsCustomModule response =
          CreateSecurityHealthAnalyticsCustomModule.createSecurityHealthAnalyticsCustomModule(
              projectId, customModuleDisplayName);
      return response;
    }
    return null;
  }

  // deleteCustomModule method is for deleting the custom module
  private static void deleteCustomModule(String projectId, String customModuleId)
      throws IOException {
    if (!Strings.isNullOrEmpty(projectId) && !Strings.isNullOrEmpty(customModuleId)) {
      DeleteSecurityHealthAnalyticsCustomModule.deleteSecurityHealthAnalyticsCustomModule(
          projectId, customModuleId);
    }
  }

  @Test
  public void testCreateSecurityHealthAnalyticsCustomModule() throws IOException {
    SecurityHealthAnalyticsCustomModule response =
        CreateSecurityHealthAnalyticsCustomModule.createSecurityHealthAnalyticsCustomModule(
            PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);

    assertNotNull(response);
    assertThat(response.getDisplayName()).isEqualTo(CUSTOM_MODULE_DISPLAY_NAME);
  }

  @Test
  public void testDeleteSecurityHealthAnalyticsCustomModule() throws IOException {
    SecurityHealthAnalyticsCustomModule response =
        createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    String customModuleId = extractCustomModuleId(response.getName());
    assertTrue(
        DeleteSecurityHealthAnalyticsCustomModule.deleteSecurityHealthAnalyticsCustomModule(
            PROJECT_ID, customModuleId));
  }

  @Test
  public void testListSecurityHealthAnalyticsCustomModules() throws IOException {
    createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    ListSecurityHealthAnalyticsCustomModulesPagedResponse response =
        ListSecurityHealthAnalyticsCustomModules.listSecurityHealthAnalyticsCustomModules(
            PROJECT_ID);
    assertTrue(
        StreamSupport.stream(response.iterateAll().spliterator(), false)
            .anyMatch(module -> CUSTOM_MODULE_DISPLAY_NAME.equals(module.getDisplayName())));
  }

  @Test
  public void testGetSecurityHealthAnalyticsCustomModule() throws IOException {
    SecurityHealthAnalyticsCustomModule createCustomModuleResponse =
        createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    String customModuleId = extractCustomModuleId(createCustomModuleResponse.getName());
    SecurityHealthAnalyticsCustomModule getCustomModuleResponse =
        GetSecurityHealthAnalyticsCustomModule.getSecurityHealthAnalyticsCustomModule(
            PROJECT_ID, customModuleId);

    assertThat(getCustomModuleResponse.getDisplayName()).isEqualTo(CUSTOM_MODULE_DISPLAY_NAME);
    assertThat(extractCustomModuleId(getCustomModuleResponse.getName())).isEqualTo(customModuleId);
  }

  @Test
  public void testUpdateSecurityHealthAnalyticsCustomModule() throws IOException {
    SecurityHealthAnalyticsCustomModule createCustomModuleResponse =
        createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    String customModuleId = extractCustomModuleId(createCustomModuleResponse.getName());
    SecurityHealthAnalyticsCustomModule response =
        UpdateSecurityHealthAnalyticsCustomModule.updateSecurityHealthAnalyticsCustomModule(
            PROJECT_ID, customModuleId);
    assertNotNull(response);
    assertThat(response.getEnablementState().equals(EnablementState.DISABLED));
  }

  @Test
  public void testGetEffectiveSecurityHealthAnalyticsCustomModule() throws IOException {
    SecurityHealthAnalyticsCustomModule createCustomModuleResponse =
        createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    String customModuleId = extractCustomModuleId(createCustomModuleResponse.getName());
    EffectiveSecurityHealthAnalyticsCustomModule getEffectiveCustomModuleResponse =
        GetEffectiveSecurityHealthAnalyticsCustomModule
            .getEffectiveSecurityHealthAnalyticsCustomModule(PROJECT_ID, customModuleId);

    assertThat(getEffectiveCustomModuleResponse.getDisplayName())
        .isEqualTo(CUSTOM_MODULE_DISPLAY_NAME);
    assertThat(extractCustomModuleId(getEffectiveCustomModuleResponse.getName()))
        .isEqualTo(customModuleId);
  }

  @Test
  public void testListEffectiveSecurityHealthAnalyticsCustomModules() throws IOException {
    createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    ListEffectiveSecurityHealthAnalyticsCustomModulesPagedResponse response =
        ListEffectiveSecurityHealthAnalyticsCustomModules
            .listEffectiveSecurityHealthAnalyticsCustomModules(PROJECT_ID);
    assertTrue(
        StreamSupport.stream(response.iterateAll().spliterator(), false)
            .anyMatch(module -> CUSTOM_MODULE_DISPLAY_NAME.equals(module.getDisplayName())));
  }

  @Test
  public void testListDescendantSecurityHealthAnalyticsCustomModules() throws IOException {
    createCustomModule(PROJECT_ID, CUSTOM_MODULE_DISPLAY_NAME);
    ListDescendantSecurityHealthAnalyticsCustomModulesPagedResponse response =
        ListDescendantSecurityHealthAnalyticsCustomModules
            .listDescendantSecurityHealthAnalyticsCustomModules(PROJECT_ID);
    assertTrue(
        StreamSupport.stream(response.iterateAll().spliterator(), false)
            .anyMatch(module -> CUSTOM_MODULE_DISPLAY_NAME.equals(module.getDisplayName())));
  }

  @Test
  public void testSimulateSecurityHealthAnalyticsCustomModule() throws IOException {
    SimulateSecurityHealthAnalyticsCustomModuleResponse response =
        SimulateSecurityHealthAnalyticsCustomModule.simulateSecurityHealthAnalyticsCustomModule(
            PROJECT_ID);
    assertNotNull(response);
    assertThat(response.getResult().equals("no_violation"));
  }
}