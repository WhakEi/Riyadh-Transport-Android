package com.riyadhtransport.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Retrofit service for AppWrite REST API
 */
public interface AppWriteApiService {
    
    /**
     * List documents from a collection
     * https://appwrite.io/docs/server/databases#listDocuments
     */
    @GET("databases/{databaseId}/collections/{collectionId}/documents")
    Call<Map<String, Object>> listDocuments(
            @Path("databaseId") String databaseId,
            @Path("collectionId") String collectionId,
            @Header("X-Appwrite-Project") String projectId
    );
}
