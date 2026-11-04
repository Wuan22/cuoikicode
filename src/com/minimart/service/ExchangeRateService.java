package com.minimart.service;

import com.minimart.exception.ApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gọi API tỷ giá miễn phí (open.er-api.com) để quy đổi tổng tiền hóa đơn sang USD.
 * Mọi lỗi mạng / dữ liệu sai đều được gói thành ApiException để giao diện xử lý.
 */
public class ExchangeRateService {
    private static final String URL = "https://open.er-api.com/v6/latest/USD";
    private static final Pattern VND_PATTERN = Pattern.compile("\"VND\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /** @return số VND tương ứng 1 USD. */
    public double fetchVndPerUsd() throws ApiException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("API trả về mã lỗi HTTP " + response.statusCode());
            }
            return parseVndRate(response.body());
        } catch (IOException e) {
            throw new ApiException("Không kết nối được tới API tỷ giá: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Yêu cầu API bị gián đoạn.", e);
        }
    }

    /** Tách riêng để dễ kiểm thử mà không cần mạng. */
    public static double parseVndRate(String json) throws ApiException {
        Matcher m = VND_PATTERN.matcher(json);
        if (!m.find()) {
            throw new ApiException("Phản hồi API không chứa tỷ giá VND.");
        }
        try {
            return Double.parseDouble(m.group(1));
        } catch (NumberFormatException e) {
            throw new ApiException("Tỷ giá VND sai định dạng.", e);
        }
    }
}
