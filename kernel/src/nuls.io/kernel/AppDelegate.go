package kernel

import (
	"log"
	"nuls.io/kernel/service"
	"time"
)

type AppDelegate struct {
	quit chan struct{}
	service.PluginsService
	service.RPCService
	service.UpdateService
}

func (self *AppDelegate) Run() {
	self.quit = make(chan struct{})
	self.init()

	// Debug code,hangup main thread
	go func() {
		time.Sleep(time.Duration(10) * time.Second)
		log.Println("Receiver Quit Message Queue Signal")
		self.Quit()
	}()

	<-self.quit

	close(self.quit)
}

func (self *AppDelegate) Quit() {
	self.quit <- struct{}{}
}

func (self *AppDelegate) init() {
	self.StartRPC()
	self.ScanPlugins()
}
