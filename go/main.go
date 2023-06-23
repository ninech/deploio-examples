package main

import (
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"strconv"
)

const (
	responseEnvKey = "RESPONSE_TEXT"
	portEnvKey     = "PORT"
	realIPHeader   = "X-Forwarded-For"
	defaultPort    = 8080
)

func main() {
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
		io.WriteString(w, os.Getenv(responseEnvKey)+" Hi!\n")
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
