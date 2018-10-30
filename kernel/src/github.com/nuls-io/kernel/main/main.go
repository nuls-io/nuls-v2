package main

import (
	"flag"
	_ "github.com/gorilla/websocket"
	_ "github.com/satori/go.uuid"
	"github.com/nuls-io/kernel"
	"log"
)

func main() {
	flag.Parse()
	log.Println("App Init")
	app := new(kernel.AppDelegate)
	app.Run()
	log.Println("App Exit")
}
