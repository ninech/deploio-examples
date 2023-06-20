package main

import (
	"io"
	"log"
	"net/http"
	"os"
	"strconv"
)

const (
	responseEnvKey = "RESPONSE_TEXT"
	portEnvKey     = "PORT"
	defaultPort    = 8080
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		log.Printf("got request from %s\n", r.RemoteAddr)
		io.WriteString(w, os.Getenv(responseEnvKey)+"\n")
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
