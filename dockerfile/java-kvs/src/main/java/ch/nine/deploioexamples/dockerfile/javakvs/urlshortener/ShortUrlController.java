package ch.nine.deploioexamples.dockerfile.javakvs.urlshortener;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{shortCode}")
    public Mono<ResponseEntity<?>> visit(@PathVariable String shortCode) {
        return shortUrlService.visit(shortCode);
    }

    @PostMapping("/api/validate")
    public Mono<ResponseEntity<?>> validate(@RequestBody ShortUrl shortUrl) {
        return shortUrlService.validate(shortUrl);
    }

    @PostMapping("/api/shorten")
    public Mono<ResponseEntity<?>> shorten(@RequestBody ShortUrl shortUrl) {
        return shortUrlService.shorten(shortUrl);
    }

    @GetMapping("/api/stats")
    public Mono<ResponseEntity<String>> stats(@RequestParam String shortCodes, ServerHttpRequest request) {
        return shortUrlService.stats(shortCodes, request.getURI().getScheme(), request.getHeaders().get("Host").getFirst());
    }

}
