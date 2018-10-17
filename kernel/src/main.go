package main

import (
	"flag"
	"log"
	"nuls.io/kernel"
)

func init() {
	log.SetPrefix("\rDEBUG:")
	log.SetFlags(log.Ldate | log.Lmicroseconds | log.Llongfile)
}

func main() {
	flag.Parse()
	log.Println("App Init")
	app := new(kernel.AppDelegate)
	app.Run()
	log.Println("App Exit")
}
