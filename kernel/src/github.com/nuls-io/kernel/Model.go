package kernel

import "github.com/gorilla/websocket"

type ModuleObject struct {
	name string	// 模块标识符
	conn websocket.Conn // 与模块通讯的 WS 连接
}
