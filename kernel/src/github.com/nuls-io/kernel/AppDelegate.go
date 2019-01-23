package kernel

import (
	"fmt"
	"github.com/nuls-io/kernel/repository"
	"github.com/nuls-io/kernel/rpc"
	"github.com/nuls-io/kernel/update"
	"os"
	"os/signal"
	"sync"
)

var once sync.Once
var instance *AppDelegate

type AppDelegate struct {
	quit       chan struct{}
	rpcService *rpc.RpcService
	updater    *update.Updater
	repository *repository.Repository
}

func GetAppDelegate() *AppDelegate {
	once.Do(func() {
		instance = &AppDelegate{}
	})
	return instance
}

func (self *AppDelegate) Run() {
	self.init()
	self.join()
	os.Exit(0)
}

func (self *AppDelegate) join() {
	s := make(chan os.Signal)
	signal.Notify(s, os.Interrupt, os.Kill)

	select {
	case <-self.quit:
	case <-s:
		fmt.Printf("\rReceive Kill Signal Quit\n")
	}
	signal.Stop(s)
}

func (self *AppDelegate) Quit() {
	self.quit <- struct{}{}
	close(self.quit)
}

func (self *AppDelegate) init() {
	self.quit = make(chan struct{})
	self.rpcService = new(rpc.RpcService)
	self.updater = new(update.Updater)
	self.repository = new(repository.Repository)

	self.rpcService.StartService()
	self.repository.ScanModules()
}

func (self *AppDelegate) runModules() {

}
