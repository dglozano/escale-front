package com.dglozano.escale.ui;

public class Event<T> {

    private boolean hasBeenHandled;
    private T content;

    public Event(T content) {
        this.content = content;
        this.hasBeenHandled = false;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }

    public T handleContent() {
        if(hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }
}
