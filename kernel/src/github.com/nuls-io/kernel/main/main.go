package main

import (
	"flag"
	_ "github.com/gorilla/websocket"
	"github.com/nuls-io/kernel"
	_ "github.com/satori/go.uuid"
	"log"
	"os"
)

func main() {
	flag.Parse()
	log.Println("App Init")
	app := kernel.GetAppDelegate()
	app.Run()
	log.Println("App Exit")
	os.Exit(0)
}
