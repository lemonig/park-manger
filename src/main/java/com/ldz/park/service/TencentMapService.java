package com.ldz.park.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.model.Community;
import com.ldz.park.model.meta.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯位置服务（WebService API）POI 搜索代理。
 *
 * 用途：运营在后台建小区库时，按「城市 + 关键词」搜索真实 POI，
 * 取规范名称/省市区/坐标入库，避免人工录入产生脏数据与错误坐标。
 *
 * Key 说明：TENCENT_MAP_KEY 通过环境变量注入（需开通 WebService API），
 * 禁止写进仓库；key 只在本服务端使用，不下发给小程序端。
 */
@Service
@RequiredArgsConstructor
public class TencentMapService {

    private static final String POI_SEARCH_URL = "https://apis.map.qq.com/ws/place/v1/search";

    private final ObjectMapper objectMapper;

    @Value("${TENCENT_MAP_KEY:}")
    private String mapKey;

    /**
     * 关键词搜索 POI，归一化为小区候选（不含 id，入库前由运营确认）。
     *
     * @param keyword POI 名称关键词（必填）
     * @param city    城市，如「广州」（必填，腾讯 place/search 按 region 边界检索）
     * @return 归一化候选列表，最多 10 条
     */
    public List<Community> searchPoi(String keyword, String city) {
        if (mapKey == null || mapKey.isBlank()) {
            throw new ServerException("MAP_CONFIG_ERROR", "地图服务未配置，请联系管理员");
        }

        String url = UriComponentsBuilder.fromHttpUrl(POI_SEARCH_URL)
                .queryParam("key", mapKey)
                .queryParam("keyword", keyword)
                .queryParam("boundary", "region(" + city + ",0)")
                .queryParam("page_size", 10)
                .build()
                .toUriString();

        String response;
        try {
            response = new RestTemplate().getForObject(url, String.class);
        } catch (Exception e) {
            throw new ServerException("MAP_NETWORK_ERROR", "地图服务网络异常，请稍后重试");
        }
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new ServerException("MAP_RESPONSE_ERROR", "地图服务响应解析失败");
        }
        if (!json.has("status") || json.get("status").asInt() != 0) {
            // 120=key配额/权限问题，其余为请求问题，均透传给运营排查
            int status = json.path("status").asInt(-1);
            String message = json.path("message").asText("地图服务请求失败");
            throw new ServerException("MAP_API_ERROR", "地图服务请求失败(" + status + "): " + message);
        }

        List<Community> result = new ArrayList<>();
        JsonNode data = json.path("data");
        if (data.isArray()) {
            for (JsonNode poi : data) {
                Community community = new Community();
                community.setName(poi.path("title").asText(""));
                community.setAddress(poi.path("address").asText(""));
                JsonNode adInfo = poi.path("ad_info");
                community.setProvince(adInfo.path("province").asText(""));
                community.setCity(adInfo.path("city").asText(""));
                community.setArea(adInfo.path("district").asText(""));
                JsonNode location = poi.path("location");
                if (location.has("lat") && location.has("lng")) {
                    community.setLat(new BigDecimal(location.path("lat").asText()));
                    community.setLng(new BigDecimal(location.path("lng").asText()));
                }
                if (!community.getName().isBlank()) {
                    result.add(community);
                }
            }
        }
        return result;
    }
}
