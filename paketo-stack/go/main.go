package main

import (
	"embed"
	"fmt"
	"html/template"
	"log"
	"net"
	"net/http"
	"os"
	"strconv"
	"sync/atomic"
	"time"
)

const (
	responseEnvKey = "RESPONSE_TEXT"
	portEnvKey     = "PORT"
	exitEnvKey     = "DEBUG_EXIT"
	realIPHeader   = "X-Forwarded-For"
	defaultPort    = 8080
)

//go:embed index.html.tmpl
var content embed.FS

var ready atomic.Bool

func main() {
	if val, ok := os.LookupEnv(exitEnvKey); ok {
		code, err := strconv.Atoi(val)
		if err != nil {
			log.Fatalf("error parsing exit code %s to int: %s", val, err)
		}
		log.Printf("exiting with code %v requested by env %s", code, exitEnvKey)
		os.Exit(code)
	}

	tmpl, err := template.ParseFS(content, "*.tmpl")
	if err != nil {
		log.Fatal(err)
	}

	// Simulate warm-up: mark ready after 5s
	go func() {
		time.Sleep(5 * time.Second)
		ready.Store(true)
		log.Println("App is now ready")
	}()

	mux := http.NewServeMux()

	mux.HandleFunc("GET /", func(w http.ResponseWriter, req *http.Request) {
		ip, _, err := net.SplitHostPort(req.RemoteAddr)
		if err != nil {
			fmt.Fprintf(w, "req.RemoteAddr: %s is not ip:port", req.RemoteAddr)
			http.Error(w, http.StatusText(http.StatusInternalServerError), http.StatusInternalServerError)
			return
		}
		if realIP := req.Header.Get(realIPHeader); len(realIP) != 0 {
			ip = realIP
		}

		log.Printf("%s on %s request from %s\n", req.Method, req.URL.Path, ip)
		tmpl.Execute(w, struct{ Text string }{
			Text: os.Getenv(responseEnvKey),
		})
	})

	// /healthz is a simple example of a custom health probe endpoint.
	// In Deploio, applications run on Kubernetes. By default, a pod is marked as
	// ready as soon as the web server starts listening on its port. If the app
	// still needs extra startup work (e.g., load data, run migrations), traffic
	// may be sent too early, causing temporary errors.
	// A custom health probe URL solves this by letting the app decide when it is
	// actually healthy and ready to serve requests. The platform will keep checking
	// this URL in the background and only start sending traffic once it succeeds.
	//
	// This endpoint is optional - if you don't specify one, the platform will use
	// its default health check configuration instead.
	//
	// In this demo, /healthz simulates that behavior: it reports "not ready" during
	// a short warm-up period, and then "OK" once the app is fully ready. You can
	// adapt this pattern in your own application to provide a more accurate signal
	// of application health.
	mux.HandleFunc("GET /healthz", func(w http.ResponseWriter, r *http.Request) {
		if !ready.Load() {
			http.Error(w, "not ready", http.StatusServiceUnavailable)
			return
		}
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK\n"))
	})

	// Catch-all 404
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		http.NotFound(w, r)
	})

	port := strconv.Itoa(defaultPort)
	if len(os.Getenv(portEnvKey)) != 0 {
		port = os.Getenv(portEnvKey)
	}
	addr := ":" + port

	s := &http.Server{
		Addr:              addr,
		Handler:           mux,
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Printf("starting HTTP server on %s", addr)
	log.Fatal(s.ListenAndServe())
}
