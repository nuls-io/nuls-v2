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
	app := new(kernel.AppDelegate)
	app.Run()
	log.Println("App Exit")
	os.Exit(0)
}
