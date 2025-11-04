package com.riyadhtransport.api;

import com.riyadhtransport.models.StationDeparture;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RptStationService {
    @FormUrlEncoded
    @POST("en/web/guest/stationdetails?p_p_id=com_rcrc_stations_RcrcStationDetailsPortlet_INSTANCE_53WVbOYPfpUF&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=%2Fdeparture-monitor&p_p_cacheability=cacheLevelPage")
    Call<List<StationDeparture>> getStationDepartures(@FieldMap Map<String, String> fields);
}
