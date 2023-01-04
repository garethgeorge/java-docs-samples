/*
 * Copyright 2022 Google Inc.
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

// [START auth_cloud_restrict_api_key_server]

import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.Key;
import com.google.api.apikeys.v2.Restrictions;
import com.google.api.apikeys.v2.ServerKeyRestrictions;
import com.google.api.apikeys.v2.UpdateKeyRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RestrictApiKeyServer {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(Developer): Before running this sample,
    //  1. Replace the variable(s) below.
    String projectId = "GOOGLE_CLOUD_PROJECT_ID";

    // ID of the key to restrict. This ID is auto-created during key creation.
    // This is different from the key string. To obtain the key_id,
    // you can also use the lookup api: client.lookupKey()
    String keyId = "key_id";

    restrictApiKeyServer(projectId, keyId);
  }

  // Restricts the API key based on IP addresses. You can specify one or more IP addresses
  // of the callers, for example web servers or cron jobs, that are allowed to use your API key.
  public static void restrictApiKeyServer(String projectId, String keyId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `apiKeysClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {

      // Restrict the API key usage by specifying the IP addresses.
      // You can specify the IP addresses in IPv4 or IPv6 or a subnet using CIDR notation.
      Restrictions restrictions = Restrictions.newBuilder()
          .setServerKeyRestrictions(ServerKeyRestrictions.newBuilder()
              .addAllAllowedIps(Arrays.asList("198.51.100.0/24", "2000:db8::/64"))
              .build())
          .build();

      Key key = Key.newBuilder()
          .setName(String.format("projects/%s/locations/global/keys/%s", projectId, keyId))
          // Set the restriction(s).
          // For more information on API key restriction, see:
          // https://cloud.google.com/docs/authentication/api-keys
          .setRestrictions(restrictions)
          .build();

      // Initialize request and set arguments.
      UpdateKeyRequest updateKeyRequest = UpdateKeyRequest.newBuilder()
          .setKey(key)
          .setUpdateMask(FieldMask.newBuilder().addPaths("restrictions").build())
          .build();

      // Make the request and wait for the operation to complete.
      Key result = apiKeysClient.updateKeyAsync(updateKeyRequest).get(3, TimeUnit.MINUTES);

      // For authenticating with the API key, use the value in "result.getKeyString()".
      System.out.printf("Successfully updated the API key: %s", result.getName());
    }
  }
}
// [END auth_cloud_restrict_api_key_server]