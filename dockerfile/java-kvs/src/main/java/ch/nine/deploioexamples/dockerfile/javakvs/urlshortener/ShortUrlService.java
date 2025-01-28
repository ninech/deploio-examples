package ch.nine.deploioexamples.dockerfile.javakvs.urlshortener;

import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ShortUrlService {

    public static final String SHORT_CODE_FORMAT = "[a-zA-Z0-9]*";

    private final ReactiveHashOperations<String, String, String> redisHashOps;

    public ShortUrlService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisHashOps = redisTemplate.opsForHash();
    }

    public Mono<ShortUrl> get(String shortCode) {
        return redisHashOps.multiGet(shortCode, List.of("url", "visits"))
                .flatMap(values -> values != null && values.size() == 2
                        ? Mono.just(new ShortUrl(shortCode, values.get(0), Long.valueOf(values.get(1))))
                        : Mono.empty());
    }

    public Flux<ShortUrl> get(List<String> shortCodes) {
        return Flux.fromIterable(shortCodes)
                .flatMap(this::get);
    }

    public Mono<Boolean> exists(String shortCode) {
        return redisHashOps.size(shortCode).map(size -> size > 0);
    }

    public Mono<Boolean> create(ShortUrl shortUrl) {
        return exists(shortUrl.getShortCode())
                .flatMap(exists -> !exists
                        ? redisHashOps.putAll(shortUrl.getShortCode(), shortUrltoHash(shortUrl))
                        .thenReturn(true)
                        : Mono.just(false));
    }

    private Map<String, String> shortUrltoHash(ShortUrl shortUrl) {
        Map<String, String> hash = new HashMap<>();

        hash.put("url", shortUrl.getUrl());
        hash.put("visits", String.valueOf(shortUrl.getVisits()));

        return hash;
    }

    public Mono<ResponseEntity<String>> stats(String shortCodes, String scheme, String myHost) {
        return Mono.justOrEmpty(shortCodes)
                .filter(s -> !s.isBlank())
                .map(s -> Arrays.asList(s.split(",")))
                .flatMapMany(this::get)
                .map(shortUrl -> String.format("""
                                <tr>
                                    <td style='display: none;'>%s</td>
                                    <td><a href="%s://%s/%s" target='_blank'>%s</a></td>
                                    <td>%s</td>
                                    <td>%d</td>
                                </tr>""",
                        shortUrl.getShortCode(),
                        scheme, myHost, shortUrl.getShortCode(), shortUrl.getShortCode(),
                        shortUrl.getUrl(),
                        shortUrl.getVisits()))
                .collect(Collectors.joining())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(""));
    }

    public Mono<ResponseEntity<?>> validate(ShortUrl shortUrl) {
        String url = shortUrl.getUrl();
        String shortCode = shortUrl.getShortCode();

        if (url == null || url.isBlank() || url.length() > 2000) {
            return Mono.just(ResponseEntity.badRequest()
                    .body("URL must not be empty and no longer than 2000 characters"));
        }

        try {
            new URI(url).toURL();
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest()
                    .body("URL must be a valid address"));
        }

        if (shortCode != null) {
            if (!shortCode.matches("^" + SHORT_CODE_FORMAT + "$") || shortCode.length() > 50) {
                return Mono.just(ResponseEntity.badRequest()
                        .body("Short code must be alphanumeric and no longer than 50 characters"));
            }

            return exists(shortCode).flatMap(exists -> exists
                    ? Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("HX-Reswap", "innerHtml")
                    .body("Short code already exists"))
                    : Mono.just(ResponseEntity.ok().build())
            );
        }

        return Mono.just(ResponseEntity.ok().build());
    }

    public Mono<ResponseEntity<?>> shorten(ShortUrl shortUrl) {
        String url = shortUrl.getUrl();
        String shortCode = shortUrl.getShortCode();

        return validate(new ShortUrl(shortCode, url, 0L))
                .flatMap(validationResponse -> {
                    if (!validationResponse.hasBody()) {
                        return (shortCode != null && !shortCode.trim().isEmpty()
                                ? Mono.just(shortCode)
                                : generateUniqueShortCode())
                                .map(generatedShortCode -> new ShortUrl(generatedShortCode, url, 0L))
                                .flatMap(validShortUrl -> create(validShortUrl)
                                        .flatMap(success -> success
                                                ? Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                                                .contentType(MediaType.TEXT_HTML)
                                                .body(String.format("<tr><td>%s</td><td>%s</td><td>%d</td></tr>",
                                                        validShortUrl.getShortCode(),
                                                        validShortUrl.getUrl(),
                                                        validShortUrl.getVisits())))
                                                : Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                                .header("HX-Reswap", "innerHtml")
                                                .body("Short code already exists"))))
                                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .header("HX-Reswap", "innerHtml")
                                        .body(error.getMessage())));
                    } else {
                        return Mono.just(ResponseEntity.status(validationResponse.getStatusCode())
                                .header("HX-Reswap", "innerHtml")
                                .body(validationResponse.getBody()));
                    }
                });
    }

    public Mono<ResponseEntity<?>> visit(String shortCode) {
        return get(shortCode)
                .flatMap(shortUrl -> shortUrl != null
                        ? incrementVisit(shortCode).then(Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(shortUrl.getUrl())).build()))
                        : Mono.just(ResponseEntity.notFound().build()));
    }

    private Mono<Long> incrementVisit(String shortCode) {
        return redisHashOps.increment(shortCode, "visits", 1);
    }

    private Mono<String> generateUniqueShortCode() {
        return generateShortCodeUntilUnique()
                .repeatWhenEmpty(3, companion -> companion.delayElements(Duration.ofMillis(10)))
                .onErrorResume(_ -> Mono.error(new RuntimeException("Server is busy, try again in a few moments")));
    }

    private Mono<String> generateShortCodeUntilUnique() {
        String shortCode = generateRandomShortCode();
        return exists(shortCode)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.just(shortCode));
    }

    private String generateRandomShortCode() {
        SecureRandom rng = new SecureRandom();
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        int length = rng.nextInt(10 - 5 + 1) + 5;

        return IntStream.range(0, length)
                .map(_ -> rng.nextInt(chars.length()))
                .mapToObj(chars::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

}
