package ch.nine.deploioexamples.dockerfile.javakvs.urlshortener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ShortUrl {

    private String shortCode;
    private String url;
    private Long visits;

}
