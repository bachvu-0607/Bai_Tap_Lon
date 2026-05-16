package com.uet.domain.event;
import java.io.Serializable;

public class ServerEvent implements Serializable{
    private static final long serialVersionUID = 1L;

    private final ServerEventType type;
    private final Object data;

    public ServerEvent(ServerEventType type, Object data){
        this.type = type;
        this.data = data;
    }

    public ServerEventType getType(){
        return this.type;
    }

    public Object getData(){
        return this.data;
    }
}
