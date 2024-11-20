package com.urlshortener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UrlData {
    private String originalURL;
    private long expirationTime;
}
