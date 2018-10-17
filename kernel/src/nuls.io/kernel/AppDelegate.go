package kernel

import (
	"log"
	"time"
)

type AppDelegate struct {
	quit chan struct{}
	PluginManager
	UpdateManager
	RPCServices
}

func (self *AppDelegate) Run() {
	self.quit = make(chan struct{})
	self.StartRPC()
	self.ScanPlugins()

	// Debug code,hangup main thread
	go func() {
		time.Sleep(time.Duration(10) * time.Second)
		log.Println("Receiver Quit Message Queue Signal")
		self.Quit()
	}()

	<-self.quit

	close(self.quit)
	self.quit = nil
}

func (self *AppDelegate) Quit() {
	self.quit <- struct{}{}
}
