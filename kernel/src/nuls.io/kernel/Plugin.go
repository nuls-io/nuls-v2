package kernel

type Plugin struct {
	Name string
	Author string
	Homepage string
	loaded string `json:"loaded,omitempty"`
} 
