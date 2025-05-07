package main

import (
	"context"
	"crypto/tls"
	"fmt"
	"log"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

var rdb *redis.Client
var ctx = context.Background()

func main() {
	kvsHost := os.Getenv("KVS_HOST")
	kvsPassword := os.Getenv("KVSCLI_AUTH")
	if kvsHost == "" || kvsPassword == "" {
		log.Fatal("KVS_HOST and KVSCLI_AUTH environment variables must be set")
	}

	rdb = redis.NewClient(&redis.Options{
		Addr:     kvsHost + ":6379",
		Password: kvsPassword,
		TLSConfig: &tls.Config{
			InsecureSkipVerify: true,
		},
	})

	pong, err := rdb.Ping(ctx).Result()
	if err != nil {
		log.Fatalf("Failed to connect to KVS: %v", err)
	}
	fmt.Printf("Connected to KVS: %s\n", pong)

	err = rdb.Set(ctx, "message", "Hello from Nine KVS!", 0).Err()
	if err != nil {
		log.Fatalf("Could not set message in KVS: %v", err)
	}

	router := gin.Default()
	router.LoadHTMLGlob("templates/*")

	router.GET("/", func(c *gin.Context) {
		val, err := rdb.Get(ctx, "message").Result()
		if err != nil {
			val = "Could not fetch message"
		}
		c.HTML(http.StatusOK, "index.tmpl", gin.H{
			"Message": val,
		})
	})

	log.Println("Starting web server on :8080")
	router.Run(":8080")
}
