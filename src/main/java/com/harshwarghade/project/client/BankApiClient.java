package com.harshwarghade.project.client;

import com.harshwarghade.project.dto.BankTxn;
import com.harshwarghade.project.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Component
@RequiredArgsConstructor
public class BankApiClient {

    private final RestTemplate restTemplate;

    private static final String BASE_URL =
            "http://localhost:8081/api/bank/transactions?page=%d&size=%d";

    public PageResponse<BankTxn> fetchTransactions(int page, int size) {

        String url = String.format(BASE_URL, page, size);

        ResponseEntity<PageResponse<BankTxn>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PageResponse<BankTxn>>() {}
                );

        return response.getBody();
    }
}
