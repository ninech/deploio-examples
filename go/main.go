package main

import (
	"embed"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"strconv"
	"text/template"
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

	http.HandleFunc("/", func(w http.ResponseWriter, req *http.Request) {
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

	port := strconv.Itoa(defaultPort)
	if len(os.Getenv(portEnvKey)) != 0 {
		port = os.Getenv(portEnvKey)
	}
	addr := ":" + port

	log.Printf("starting HTTP server on %s", addr)
	if err := http.ListenAndServe(addr, nil); err != nil {
		log.Fatal(err)
	}
}
